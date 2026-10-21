package httpapi

import (
	"net"
	"net/http"
	"strings"
	"sync"
	"time"
)

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
	mu          sync.Mutex
	maxRequests int
	window      time.Duration
	buckets     map[string]ipBucket
}

// NewRateLimiter crea un limitador de velocidad en memoria por IP.
func NewRateLimiter(maxRequests int, window time.Duration) *RateLimiter {
	return &RateLimiter{
		maxRequests: maxRequests,
		window:      window,
		buckets:     make(map[string]ipBucket),
	}
}

// Allow indica si la clave dada puede ejecutar una petición adicional dentro de la ventana.
func (rl *RateLimiter) Allow(key string, now time.Time) bool {
	rl.mu.Lock()
	defer rl.mu.Unlock()

	bucket, exists := rl.buckets[key]
	if !exists || !now.Before(bucket.resetTime) {
		rl.buckets[key] = ipBucket{count: 1, resetTime: now.Add(rl.window)}
		return true
	}
	if bucket.count >= rl.maxRequests {
		return false
	}
	bucket.count++
	rl.buckets[key] = bucket
	return true
}

// Middleware devuelve el middleware HTTP que rechaza excesos de tráfico con 429 estructurado.
func (rl *RateLimiter) Middleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		clientIP := extractClientIP(r)
		bucketKey := clientIP
		policy := "RateLimitGeneral"
		if strings.HasPrefix(r.URL.Path, "/v1/auth/login") {
			bucketKey = "auth:" + clientIP
			policy = "RateLimitAuthLogin"
		}
		if !rl.Allow(bucketKey, time.Now()) {
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
		next.ServeHTTP(w, r)
	})
}

func extractClientIP(r *http.Request) string {
	if forwarded := strings.TrimSpace(r.Header.Get("X-Forwarded-For")); forwarded != "" {
		first, _, _ := strings.Cut(forwarded, ",")
		return strings.TrimSpace(first)
	}
	host, _, err := net.SplitHostPort(strings.TrimSpace(r.RemoteAddr))
	if err == nil && host != "" {
		return host
	}
	if r.RemoteAddr != "" {
		return r.RemoteAddr
	}
	return "unknown"
}
