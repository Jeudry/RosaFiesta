package httpapi

import (
	"context"
	"net"
	"net/http"
	"strings"
	"sync"
	"time"
)

type clientIPContextKey struct{}

const maxRateLimitBuckets = 10000

// ClientIP devuelve la IP cliente resuelta de manera segura desde el contexto de la petición.
func ClientIP(ctx context.Context) string {
	if ctx == nil {
		return ""
	}
	ip, _ := ctx.Value(clientIPContextKey{}).(string)
	return ip
}

// SecurityHeadersMiddleware inyecta cabeceras HTTP de endurecimiento y defensa en profundidad.
func SecurityHeadersMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		h := w.Header()
		h.Set("X-Content-Type-Options", "nosniff")
		h.Set("X-Frame-Options", "DENY")
		h.Set("Referrer-Policy", "strict-origin-when-cross-origin")
		h.Set("Permissions-Policy", "camera=(), microphone=(), geolocation=()")
		h.Set("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'")
		h.Set("Strict-Transport-Security", "max-age=63072000; includeSubDomains")
		next.ServeHTTP(w, r)
	})
}

type ipBucket struct {
	count     int
	resetTime time.Time
}

// RateLimiter aplica un límite de peticiones por ventana de tiempo e IP.
type RateLimiter struct {
	mu             sync.Mutex
	maxRequests    int
	authMaxReqs    int
	window         time.Duration
	lastSweep      time.Time
	buckets        map[string]ipBucket
	trustedProxies []net.IPNet
}

// NewRateLimiter crea un limitador de velocidad en memoria por IP.
func NewRateLimiter(maxRequests int, window time.Duration) *RateLimiter {
	return NewRateLimiterWithAuth(maxRequests, maxRequests, window)
}

// NewRateLimiterWithAuth aplica un límite estricto a rutas sensibles de autenticación además del límite general.
func NewRateLimiterWithAuth(maxRequests, authMaxRequests int, window time.Duration) *RateLimiter {
	if authMaxRequests <= 0 {
		authMaxRequests = maxRequests
	}
	return &RateLimiter{
		maxRequests: maxRequests,
		authMaxReqs: authMaxRequests,
		window:      window,
		buckets:     make(map[string]ipBucket),
	}
}

// WithTrustedProxies configura los rangos CIDR cuyos encabezados X-Forwarded-For son confiables.
func (rl *RateLimiter) WithTrustedProxies(cidrs []string) *RateLimiter {
	parsed := make([]net.IPNet, 0, len(cidrs))
	for _, raw := range cidrs {
		trimmed := strings.TrimSpace(raw)
		if trimmed == "" {
			continue
		}
		if _, netBlock, err := net.ParseCIDR(trimmed); err == nil {
			parsed = append(parsed, *netBlock)
			continue
		}
		if ip := net.ParseIP(trimmed); ip != nil {
			maskBits := 32
			if ip.To4() == nil {
				maskBits = 128
			}
			parsed = append(parsed, net.IPNet{IP: ip, Mask: net.CIDRMask(maskBits, maskBits)})
		}
	}
	rl.mu.Lock()
	rl.trustedProxies = parsed
	rl.mu.Unlock()
	return rl
}

// Allow indica si la clave dada puede ejecutar una petición adicional dentro de la ventana.
func (rl *RateLimiter) Allow(key string, now time.Time) bool {
	return rl.allowWithLimit(key, rl.maxRequests, now)
}

func (rl *RateLimiter) allowWithLimit(key string, limit int, now time.Time) bool {
	rl.mu.Lock()
	defer rl.mu.Unlock()

	rl.evictExpiredBucketsLocked(now)
	bucket, exists := rl.buckets[key]
	if !exists {
		rl.ensureBucketCapacityLocked(now)
		rl.buckets[key] = ipBucket{count: 1, resetTime: now.Add(rl.window)}
		return true
	}
	if !now.Before(bucket.resetTime) {
		rl.buckets[key] = ipBucket{count: 1, resetTime: now.Add(rl.window)}
		return true
	}
	if bucket.count >= limit {
		return false
	}
	bucket.count++
	rl.buckets[key] = bucket
	return true
}

func (rl *RateLimiter) evictExpiredBucketsLocked(now time.Time) {
	shouldSweep := rl.lastSweep.IsZero()
	if !shouldSweep {
		shouldSweep = !now.Before(rl.lastSweep.Add(rl.window))
	}
	if !shouldSweep {
		return
	}
	for k, entry := range rl.buckets {
		if !now.Before(entry.resetTime) {
			delete(rl.buckets, k)
		}
	}
	rl.lastSweep = now
}

func (rl *RateLimiter) ensureBucketCapacityLocked(now time.Time) {
	if len(rl.buckets) < maxRateLimitBuckets {
		return
	}
	for k, entry := range rl.buckets {
		if !now.Before(entry.resetTime) {
			delete(rl.buckets, k)
		}
	}
	if len(rl.buckets) < maxRateLimitBuckets {
		return
	}
	oldestKey := ""
	var oldestTime time.Time
	for k, entry := range rl.buckets {
		if oldestKey == "" {
			oldestKey = k
			oldestTime = entry.resetTime
			continue
		}
		if entry.resetTime.Before(oldestTime) {
			oldestKey = k
			oldestTime = entry.resetTime
		}
	}
	if oldestKey != "" {
		delete(rl.buckets, oldestKey)
	}
}

// Middleware devuelve el middleware HTTP que rechaza excesos de tráfico con 429 estructurado.
func (rl *RateLimiter) Middleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		rl.mu.Lock()
		trusted := append([]net.IPNet(nil), rl.trustedProxies...)
		rl.mu.Unlock()
		clientIP := extractClientIP(r, trusted)
		bucketKey := clientIP
		policy := "RateLimitGeneral"
		limit := rl.maxRequests
		if isSensitiveAuthPath(r.URL.Path) {
			bucketKey = "auth:" + clientIP
			policy = "RateLimitAuthSensitive"
			if strings.HasPrefix(r.URL.Path, "/v1/auth/login") {
				policy = "RateLimitAuthLogin"
			}
			limit = rl.authMaxReqs
		}
		if !rl.allowWithLimit(bucketKey, limit, time.Now()) {
			endpoint := r.Method + " " + r.URL.Path
			apiErr := NewAPIError(
				http.StatusTooManyRequests,
				"RATE_LIMIT_EXCEEDED",
				"too many requests from client IP; please retry after cooldown window",
				endpoint,
				policy,
				"rate limit window exceeded for key "+clientIP,
			)
			WriteHTTPAPIError(w, apiErr)
			return
		}
		ctxWithIP := context.WithValue(r.Context(), clientIPContextKey{}, clientIP)
		next.ServeHTTP(w, r.WithContext(ctxWithIP))
	})
}

func isSensitiveAuthPath(path string) bool {
	sensitivePrefixes := []string{
		"/v1/auth/login",
		"/v1/auth/register",
		"/v1/auth/otp/",
		"/v1/auth/reset-password",
		"/v1/auth/refresh",
		"/v1/auth/oauth",
		"/v1/auth/recover-username",
	}
	for _, prefix := range sensitivePrefixes {
		if strings.HasPrefix(path, prefix) {
			return true
		}
	}
	return false
}

func extractClientIP(r *http.Request, trustedProxies []net.IPNet) string {
	remoteIP := remoteAddrHost(r.RemoteAddr)
	if isTrustedProxyIP(remoteIP, trustedProxies) {
		if clientIP := parseForwardedClientIP(r.Header.Get("X-Forwarded-For"), trustedProxies); clientIP != "" {
			return clientIP
		}
	}
	if remoteIP != "" {
		return remoteIP
	}
	if r.RemoteAddr != "" {
		return r.RemoteAddr
	}
	return "unknown"
}

func parseForwardedClientIP(rawHeader string, trustedProxies []net.IPNet) string {
	trimmed := strings.TrimSpace(rawHeader)
	if trimmed == "" {
		return ""
	}
	parts := strings.Split(trimmed, ",")
	for idx := len(parts) - 1; idx >= 0; idx-- {
		candidate := strings.TrimSpace(parts[idx])
		if candidate == "" {
			continue
		}
		if !isTrustedProxyIP(candidate, trustedProxies) {
			return candidate
		}
	}
	return strings.TrimSpace(parts[0])
}

func remoteAddrHost(remoteAddr string) string {
	trimmed := strings.TrimSpace(remoteAddr)
	host, _, err := net.SplitHostPort(trimmed)
	if err == nil {
		if host != "" {
			return host
		}
	}
	return trimmed
}

func isTrustedProxyIP(host string, trustedProxies []net.IPNet) bool {
	if len(trustedProxies) == 0 {
		return false
	}
	parsed := net.ParseIP(strings.TrimSpace(host))
	if parsed == nil {
		return false
	}
	for _, block := range trustedProxies {
		if block.Contains(parsed) {
			return true
		}
	}
	return false
}
