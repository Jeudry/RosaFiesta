// Package redact censura datos personales, credenciales, referencias bancarias y detalles internos de SQL
// antes de registrarlos en logs o devolverlos en mensajes de error HTTP.
package redact

import (
	"regexp"
	"strings"
)

var (
	bearerRegex   = regexp.MustCompile(`(?i)\bBearer\s+[A-Za-z0-9\-._~+/]+=*`)
	jwtRegex      = regexp.MustCompile(`\beyJ[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\b`)
	secretKVRegex = regexp.MustCompile(`(?i)\b(password|passwd|secret|token|api_key|apikey)\s*([:=])\s*("[^"]*"|'[^']*'|[^\s,;]+)`)
	emailRegex    = regexp.MustCompile(`\b([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+\.[A-Za-z]{2,})\b`)
	phoneRegex    = regexp.MustCompile(`\b(809|829|849)[-\s]?(\d{3})[-\s]?(\d{4})\b`)
	bankRefRegex  = regexp.MustCompile(`\b(BHD|POP|BR|EFE|AZU)-(\d{2,4}-)?(\d{3,8})\b`)
	pgURLRegex    = regexp.MustCompile(`(?i)postgres(?:ql)?://[^\s"']+`)
	sqlStateRegex = regexp.MustCompile(`(?i)(SQLSTATE\s+[0-9A-Z]{5}|pq:\s+[^\n]+|ERROR:\s+duplicate key value violates unique constraint\s+"[^"]+")`)
)

// Text censura tokens, contraseñas, correos, teléfonos dominicanos, referencias bancarias y errores SQL crudos.
func Text(input string) string {
	trimmed := strings.TrimSpace(input)
	if trimmed == "" {
		return ""
	}
	out := bearerRegex.ReplaceAllString(trimmed, "Bearer [REDACTED]")
	out = jwtRegex.ReplaceAllString(out, "[JWT_REDACTED]")
	out = secretKVRegex.ReplaceAllString(out, "$1$2[REDACTED]")
	out = pgURLRegex.ReplaceAllString(out, "postgres://[REDACTED]")
	out = sqlStateRegex.ReplaceAllString(out, "[DB_DETAIL_REDACTED]")
	out = emailRegex.ReplaceAllStringFunc(out, Email)
	out = phoneRegex.ReplaceAllStringFunc(out, Phone)
	out = bankRefRegex.ReplaceAllStringFunc(out, Reference)
	return out
}

// Email enmascara la parte local de un correo electrónico dejando solo el primer y último carácter.
func Email(raw string) string {
	parts := strings.Split(strings.TrimSpace(raw), "@")
	if len(parts) != 2 {
		return "[EMAIL_REDACTED]"
	}
	local := parts[0]
	domain := parts[1]
	if len(local) <= 2 {
		return local[:1] + "***@" + domain
	}
	return local[:1] + "***" + local[len(local)-1:] + "@" + domain
}

// Phone enmascara un número telefónico dominicano conservando el código de área y los últimos 2 dígitos.
func Phone(raw string) string {
	cleaned := strings.TrimSpace(raw)
	if len(cleaned) < 4 {
		return "[PHONE_REDACTED]"
	}
	prefix := cleaned[:3]
	suffix := cleaned[len(cleaned)-2:]
	return prefix + "-***-**" + suffix
}

// Reference enmascara un código de referencia bancaria conservando el banco y los últimos 2 dígitos.
func Reference(raw string) string {
	cleaned := strings.TrimSpace(raw)
	dashIdx := strings.IndexByte(cleaned, '-')
	if dashIdx <= 0 || len(cleaned) < dashIdx+3 {
		return "[REF_REDACTED]"
	}
	bank := cleaned[:dashIdx]
	suffix := cleaned[len(cleaned)-2:]
	return bank + "-***-" + suffix
}

// Reasons aplica Text a una lista de motivos diagnósticos.
func Reasons(reasons ...string) []string {
	if len(reasons) == 0 {
		return nil
	}
	out := make([]string, 0, len(reasons))
	for _, reason := range reasons {
		redacted := Text(reason)
		if redacted != "" {
			out = append(out, redacted)
		}
	}
	return out
}
