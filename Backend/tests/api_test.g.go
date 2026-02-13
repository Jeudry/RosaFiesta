package test

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"Backend/internal/config"
	"Backend/internal/ratelimiter"
)

func TestRateLimitMiddleware(t *testing.T) {
	cfg := config.Config{
		RateLimiter: ratelimiter.Config{
			RequestsPerTimeFrame: 20,
			TimeFrame:            time.Second * 5,
			Enabled:              true,
		},
	}

	app := newTestApplication(t, cfg)

	ts := httptest.NewServer(app.Mount())

	defer ts.Close()

	client := &http.Client{}
	mockIp := "192.168.1.1"
	marginOfError := 2

	for i := 0; i < cfg.RateLimiter.RequestsPerTimeFrame+marginOfError; i++ {
		req, err := http.NewRequest("GET", ts.URL, nil)
		if err != nil {
			t.Fatalf("could not create request %v", err)
		}
		req.Header.Add("X-Forwarded-For", mockIp)

		resp, err := client.Do(req)
		if err != nil {
			t.Fatalf("could not send request %v", err)
		}

		if i < cfg.RateLimiter.RequestsPerTimeFrame {
			if resp.StatusCode != http.StatusOK {
				t.Errorf("Expected response code %d. Got %d", http.StatusOK, resp.StatusCode)
			}
		} else {
			if resp.StatusCode != http.StatusTooManyRequests {
				t.Errorf("Expected response code %d. Got %d", http.StatusTooManyRequests, resp.StatusCode)
			}
		}
	}
}
