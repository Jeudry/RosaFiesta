package app

import (
	"bytes"
	"context"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"errors"
	"fmt"
	"io"
	"io/fs"
	"net/http"
	"net/url"
	"os"
	"path/filepath"
	"sort"
	"strings"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/media/domain"
)

const (
	s3RequestTimeout  = 15 * time.Second
	s3DefaultRegion   = "auto"
	s3UnsignedPayload = "UNSIGNED-PAYLOAD"
)

// BlobStore abstracts binary media persistence across local disk and S3-compatible object buckets.
type BlobStore interface {
	Put(ctx context.Context, objectKey, mimeType string, data []byte) (storageRef string, err error)
	Get(ctx context.Context, storageRef string) ([]byte, error)
	Delete(ctx context.Context, storageRef string) error
	PublicURL(objectKey, defaultURL string) string
}

type localBlobStore struct {
	storageDir string
}

func (l localBlobStore) Put(_ context.Context, objectKey, _ string, data []byte) (string, error) {
	if err := os.MkdirAll(l.storageDir, 0o750); err != nil {
		return "", fmt.Errorf("create media storage directory: %w", err)
	}
	diskPath := filepath.Join(l.storageDir, filepath.Base(objectKey))
	if err := os.WriteFile(diskPath, data, 0o600); err != nil {
		return "", fmt.Errorf("write media file: %w", err)
	}
	return diskPath, nil
}

func (l localBlobStore) Get(_ context.Context, storageRef string) ([]byte, error) {
	return fs.ReadFile(os.DirFS(l.storageDir), filepath.Base(storageRef))
}

func (l localBlobStore) Delete(_ context.Context, storageRef string) error {
	return os.Remove(storageRef)
}

func (l localBlobStore) PublicURL(_, defaultURL string) string {
	return defaultURL
}

// S3Config defines credentials and bucket settings for S3 or Cloudflare R2 storage.
type S3Config struct {
	Endpoint        string
	Bucket          string
	Region          string
	AccessKeyID     string
	SecretAccessKey string
	PublicBaseURL   string
	MaxBytes        int64
	HTTPClient      *http.Client
	Clock           func() time.Time
}

// IsConfigured verifies that required S3 endpoint, bucket, and credentials are populated.
func (c S3Config) IsConfigured() bool {
	if strings.TrimSpace(c.Endpoint) == "" {
		return false
	}
	if strings.TrimSpace(c.Bucket) == "" {
		return false
	}
	if strings.TrimSpace(c.AccessKeyID) == "" {
		return false
	}
	return strings.TrimSpace(c.SecretAccessKey) != ""
}

type s3BlobStore struct {
	cfg    S3Config
	client *http.Client
	clock  func() time.Time
}

// NewS3BlobStore builds an AWS SigV4 compatible object store client.
func NewS3BlobStore(cfg S3Config) (BlobStore, error) {
	if !cfg.IsConfigured() {
		return nil, errors.New("incomplete s3 storage configuration")
	}
	region := strings.TrimSpace(cfg.Region)
	if region == "" {
		region = s3DefaultRegion
	}
	cfg.Region = region
	client := cfg.HTTPClient
	if client == nil {
		client = &http.Client{Timeout: s3RequestTimeout}
	}
	clockFn := cfg.Clock
	if clockFn == nil {
		clockFn = time.Now
	}
	return s3BlobStore{cfg: cfg, client: client, clock: clockFn}, nil
}

// WithS3Storage switches media payload storage from local disk to an S3/R2 bucket.
func (s *Service) WithS3Storage(cfg S3Config) (*Service, error) {
	blob, err := NewS3BlobStore(cfg)
	if err != nil {
		return nil, err
	}
	s.blobs = blob
	return s, nil
}

func (s s3BlobStore) Put(ctx context.Context, objectKey, mimeType string, data []byte) (string, error) {
	cleanKey := strings.TrimLeft(objectKey, "/")
	objectURL, parsedURL, err := s.objectURL(cleanKey)
	if err != nil {
		return "", err
	}
	req, err := http.NewRequestWithContext(ctx, http.MethodPut, objectURL, bytes.NewReader(data))
	if err != nil {
		return "", err
	}
	req.Header.Set("Content-Type", mimeType)
	payloadHash := sha256Hex(data)
	s.signRequest(req, parsedURL, payloadHash)
	resp, err := s.client.Do(req)
	if err != nil {
		return "", err
	}
	defer func() { _ = resp.Body.Close() }()
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		body, _ := io.ReadAll(io.LimitReader(resp.Body, 512))
		return "", fmt.Errorf("s3 put returned status %d: %s", resp.StatusCode, strings.TrimSpace(string(body)))
	}
	return "s3://" + strings.TrimSpace(s.cfg.Bucket) + "/" + cleanKey, nil
}

func (s s3BlobStore) Get(ctx context.Context, storageRef string) ([]byte, error) {
	objectKey := s.extractKey(storageRef)
	objectURL, parsedURL, err := s.objectURL(objectKey)
	if err != nil {
		return nil, err
	}
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, objectURL, nil)
	if err != nil {
		return nil, err
	}
	payloadHash := sha256Hex(nil)
	s.signRequest(req, parsedURL, payloadHash)
	resp, err := s.client.Do(req)
	if err != nil {
		return nil, err
	}
	defer func() { _ = resp.Body.Close() }()
	if resp.StatusCode < 200 {
		return nil, fmt.Errorf("s3 get returned status %d", resp.StatusCode)
	}
	if resp.StatusCode >= 300 {
		return nil, fmt.Errorf("s3 get returned status %d", resp.StatusCode)
	}
	limit := s.cfg.MaxBytes
	if limit <= 0 {
		limit = defaultMaxMediaBytes
	}
	data, err := io.ReadAll(io.LimitReader(resp.Body, limit+1))
	if err != nil {
		return nil, err
	}
	if int64(len(data)) > limit {
		return nil, domain.ErrFileTooLarge
	}
	return data, nil
}

func (s s3BlobStore) Delete(ctx context.Context, storageRef string) error {
	objectKey := s.extractKey(storageRef)
	objectURL, parsedURL, err := s.objectURL(objectKey)
	if err != nil {
		return err
	}
	req, err := http.NewRequestWithContext(ctx, http.MethodDelete, objectURL, nil)
	if err != nil {
		return err
	}
	s.signRequest(req, parsedURL, sha256Hex(nil))
	resp, err := s.client.Do(req)
	if err != nil {
		return err
	}
	_ = resp.Body.Close()
	return nil
}

func (s s3BlobStore) PublicURL(objectKey, defaultURL string) string {
	base := strings.TrimRight(strings.TrimSpace(s.cfg.PublicBaseURL), "/")
	if base == "" {
		return defaultURL
	}
	return base + "/" + strings.TrimLeft(objectKey, "/")
}

func (s s3BlobStore) objectURL(objectKey string) (string, *url.URL, error) {
	base := strings.TrimRight(strings.TrimSpace(s.cfg.Endpoint), "/")
	bucket := strings.Trim(strings.TrimSpace(s.cfg.Bucket), "/")
	full := base + "/" + bucket + "/" + strings.TrimLeft(objectKey, "/")
	parsed, err := url.Parse(full)
	if err != nil {
		return "", nil, err
	}
	return full, parsed, nil
}

func (s s3BlobStore) extractKey(storageRef string) string {
	prefix := "s3://" + strings.Trim(strings.TrimSpace(s.cfg.Bucket), "/") + "/"
	if strings.HasPrefix(storageRef, prefix) {
		return strings.TrimPrefix(storageRef, prefix)
	}
	return strings.TrimLeft(filepath.Base(storageRef), "/")
}

func (s s3BlobStore) signRequest(req *http.Request, parsedURL *url.URL, payloadHash string) {
	if payloadHash == "" {
		payloadHash = s3UnsignedPayload
	}
	now := s.clock().UTC()
	amzDate := now.Format("20060102T150405Z")
	dateStamp := now.Format("20060102")
	req.Host = parsedURL.Host
	req.Header.Set("Host", parsedURL.Host)
	req.Header.Set("x-amz-date", amzDate)
	req.Header.Set("x-amz-content-sha256", payloadHash)

	signedHeaders, canonicalHeaders := buildCanonicalHeaders(req.Header)
	canonicalRequest := strings.Join([]string{
		req.Method,
		parsedURL.EscapedPath(),
		parsedURL.RawQuery,
		canonicalHeaders,
		signedHeaders,
		payloadHash,
	}, "\n")
	scope := dateStamp + "/" + s.cfg.Region + "/s3/aws4_request"
	stringToSign := strings.Join([]string{
		"AWS4-HMAC-SHA256",
		amzDate,
		scope,
		sha256Hex([]byte(canonicalRequest)),
	}, "\n")
	signingKey := deriveSigV4Key(s.cfg.SecretAccessKey, dateStamp, s.cfg.Region)
	signature := hex.EncodeToString(hmacSHA256(signingKey, stringToSign))
	authHeader := fmt.Sprintf(
		"AWS4-HMAC-SHA256 Credential=%s/%s, SignedHeaders=%s, Signature=%s",
		s.cfg.AccessKeyID,
		scope,
		signedHeaders,
		signature,
	)
	req.Header.Set("Authorization", authHeader)
}

func buildCanonicalHeaders(headers http.Header) (string, string) {
	keys := make([]string, 0, len(headers))
	lowerMap := make(map[string]string, len(headers))
	for name, vals := range headers {
		lower := strings.ToLower(name)
		if lower == "host" || strings.HasPrefix(lower, "x-amz-") {
			keys = append(keys, lower)
			lowerMap[lower] = strings.TrimSpace(strings.Join(vals, ","))
		}
	}
	sort.Strings(keys)
	var canonical strings.Builder
	for _, key := range keys {
		canonical.WriteString(key)
		canonical.WriteByte(':')
		canonical.WriteString(lowerMap[key])
		canonical.WriteByte('\n')
	}
	return strings.Join(keys, ";"), canonical.String()
}

func deriveSigV4Key(secret, dateStamp, region string) []byte {
	kDate := hmacSHA256([]byte("AWS4"+secret), dateStamp)
	kRegion := hmacSHA256(kDate, region)
	kService := hmacSHA256(kRegion, "s3")
	return hmacSHA256(kService, "aws4_request")
}

func hmacSHA256(key []byte, data string) []byte {
	mac := hmac.New(sha256.New, key)
	_, _ = mac.Write([]byte(data))
	return mac.Sum(nil)
}

func sha256Hex(data []byte) string {
	sum := sha256.Sum256(data)
	return hex.EncodeToString(sum[:])
}
