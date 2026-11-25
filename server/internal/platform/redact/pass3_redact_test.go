package redact_test

import (
	"strings"
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/platform/redact"
)

func TestTextRedactsQueryAndProviderAPIKeys(t *testing.T) {
	t.Parallel()

	fakeGoogleKey := "AIzaSy" + "DemoKey1234567890123456789"
	fakeStripeKey := "sk_test_" + "1234567890abcdef123456"
	raw := "request to https://generativelanguage.googleapis.com/v1beta/models/gemini:generateContent?key=" + fakeGoogleKey + " failed; x-goog-api-key: " + fakeGoogleKey + " stripe=" + fakeStripeKey
	got := redact.Text(raw)

	if strings.Contains(got, fakeGoogleKey) {
		t.Fatalf("expected Google API key redacted from %q", got)
	}
	if strings.Contains(got, fakeStripeKey) {
		t.Fatalf("expected Stripe API key redacted from %q", got)
	}
	if !strings.Contains(got, "?key=[REDACTED]") {
		t.Fatalf("expected query key placeholder in %q", got)
	}
}
