package redact_test

import (
	"strings"
	"testing"

	"github.com/Jeudry/rosafiesta/server/internal/platform/redact"
)

func TestTextRedactsSensitiveFields(t *testing.T) {
	raw := `login failed for camila@rosafiesta.do phone 809-555-0142 with Authorization: Bearer rf-token-camila password="MySecretPassword123" ref BHD-2026-8841 on postgres://user:pass@localhost:5432/rf (SQLSTATE 23505)`
	got := redact.Text(raw)

	forbidden := []string{
		"camila@rosafiesta.do",
		"809-555-0142",
		"rf-token-camila",
		"MySecretPassword123",
		"BHD-2026-8841",
		"user:pass@localhost",
		"SQLSTATE 23505",
	}
	for _, secret := range forbidden {
		if strings.Contains(got, secret) {
			t.Fatalf("expected %q to be redacted in %q", secret, got)
		}
	}

	expected := []string{
		"c***a@rosafiesta.do",
		"809-***-**42",
		"Bearer [REDACTED]",
		"password=[REDACTED]",
		"BHD-***-41",
		"postgres://[REDACTED]",
		"[DB_DETAIL_REDACTED]",
	}
	for _, mask := range expected {
		if !strings.Contains(got, mask) {
			t.Fatalf("expected mask %q in %q", mask, got)
		}
	}
}

func TestEmailAndPhoneMaskingEdgeCases(t *testing.T) {
	if got := redact.Email("ab@rosafiesta.do"); got != "a***@rosafiesta.do" {
		t.Fatalf("unexpected short email mask: %q", got)
	}
	if got := redact.Phone("829-441-9980"); got != "829-***-**80" {
		t.Fatalf("unexpected phone mask: %q", got)
	}
	if got := redact.Reference("POP-992104"); got != "POP-***-04" {
		t.Fatalf("unexpected reference mask: %q", got)
	}
}
