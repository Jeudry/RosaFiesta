// Package push dispatches remote push notifications via a configured HTTP push provider.
package push

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"
)

const defaultFCMEndpoint = "https://fcm.googleapis.com/fcm/send"

// ErrNotConfigured is returned when no push gateway key is configured.
var ErrNotConfigured = errors.New("push provider is not configured")

// Config holds HTTP connection details for dispatching push notifications.
type Config struct {
	EndpointURL string
	ServerKey   string
	Timeout     time.Duration
}

// Message represents a remote push notification payload targeting a device token.
type Message struct {
	Token      string
	Platform   string
	AppVariant string
	Title      string
	Body       string
	Category   string
}

// Sender sends push notifications to registered devices.
type Sender interface {
	IsConfigured() bool
	SendPush(ctx context.Context, msg Message) error
}

// HTTPSender implements Sender against FCM legacy/HTTP or a custom compatible endpoint.
type HTTPSender struct {
	cfg    Config
	client *http.Client
}

// NewHTTPSender creates a push notification sender from configuration.
func NewHTTPSender(cfg Config) *HTTPSender {
	timeout := cfg.Timeout
	if timeout <= 0 {
		timeout = 10 * time.Second
	}
	endpoint := strings.TrimSpace(cfg.EndpointURL)
	if endpoint == "" {
		endpoint = defaultFCMEndpoint
	}
	cfg.EndpointURL = endpoint
	cfg.Timeout = timeout
	return &HTTPSender{
		cfg:    cfg,
		client: &http.Client{Timeout: timeout},
	}
}

// IsConfigured reports whether a remote push authorization key is present.
func (s *HTTPSender) IsConfigured() bool {
	if s == nil {
		return false
	}
	return strings.TrimSpace(s.cfg.ServerKey) != ""
}

// SendPush delivers a notification payload to a registered device token.
func (s *HTTPSender) SendPush(ctx context.Context, msg Message) error {
	if !s.IsConfigured() {
		return ErrNotConfigured
	}
	cleanToken := strings.TrimSpace(msg.Token)
	if cleanToken == "" {
		return errors.New("device token is required")
	}
	reqBody, err := buildRequestPayload(cleanToken, msg)
	if err != nil {
		return err
	}
	return s.postPayload(ctx, reqBody)
}

func buildRequestPayload(token string, msg Message) ([]byte, error) {
	payload := map[string]any{
		"to": token,
		"notification": map[string]string{
			"title": strings.TrimSpace(msg.Title),
			"body":  strings.TrimSpace(msg.Body),
		},
		"data": map[string]string{
			"platform":   strings.TrimSpace(msg.Platform),
			"appVariant": strings.TrimSpace(msg.AppVariant),
			"category":   strings.TrimSpace(msg.Category),
		},
	}
	return json.Marshal(payload)
}

func (s *HTTPSender) postPayload(ctx context.Context, body []byte) error {
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, s.cfg.EndpointURL, bytes.NewReader(body))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", resolveAuthHeader(s.cfg.ServerKey))
	resp, err := s.client.Do(req)
	if err != nil {
		return err
	}
	defer func() { _ = resp.Body.Close() }()
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		rawErr, _ := io.ReadAll(io.LimitReader(resp.Body, 512))
		return fmt.Errorf("push provider returned status %d: %s", resp.StatusCode, strings.TrimSpace(string(rawErr)))
	}
	return nil
}

func resolveAuthHeader(rawKey string) string {
	trimmed := strings.TrimSpace(rawKey)
	if strings.HasPrefix(trimmed, "Bearer ") || strings.HasPrefix(trimmed, "key=") {
		return trimmed
	}
	return "key=" + trimmed
}
