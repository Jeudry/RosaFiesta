package main

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"Backend/cmd/main/configModels"
	"Backend/internal/ratelimiter"

	"github.com/stretchr/testify/assert"
)

func TestRateLimitMiddleware(t *testing.T) {
	// Use a small limit for testing
	limit := 20
	cfg := configModels.Config{
		RateLimiter: ratelimiter.Config{
			RequestsPerTimeFrame: limit,
			TimeFrame:            time.Second * 5,
			Enabled:              true,
		},
	}

	app := newTestApplication(t, cfg)
	ts := httptest.NewServer(app.Mount())
	defer ts.Close()

	client := &http.Client{}
	mockIp := "192.168.1.1"

	// Add margin to ensure we trigger the limit
	params := limit + 2

	for i := 0; i < params; i++ {
		req, err := http.NewRequest("GET", ts.URL+"/v1/health", nil)
		assert.NoError(t, err)

		req.Header.Add("X-Forwarded-For", mockIp)

		resp, err := client.Do(req)
		assert.NoError(t, err)
		defer resp.Body.Close()

		if i < limit {
			assert.Equal(t, http.StatusOK, resp.StatusCode, "Request %d should be OK", i)
		} else {
			assert.Equal(t, http.StatusTooManyRequests, resp.StatusCode, "Request %d should be Rate Limited", i)
		}
	}
}
