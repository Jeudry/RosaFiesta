package httpapi

import (
	"context"
	"net/http"
	"strings"

	"github.com/danielgtaylor/huma/v2"
	"github.com/google/uuid"
)

const SecurityScheme = "bearer"

type (
	userKey      struct{}
	tokenKey     struct{}
	principalKey struct{}
)

type Principal struct {
	ID          uuid.UUID
	Email       string
	FullName    string
	Role        string
	Permissions []string
}

func (p Principal) IsClient() bool {
	return p.Role == "client"
}

func (p Principal) IsAdminOrStaff() bool {
	if p.Role == "admin" {
		return true
	}
	return p.Role == "staff"
}

func (p Principal) HasPermission(permission string) bool {
	if p.Role == "admin" {
		return true
	}
	if p.Role != "staff" {
		return false
	}
	trimmedTarget := strings.TrimSpace(permission)
	target := strings.ToLower(trimmedTarget)
	for _, rawPerm := range p.Permissions {
		trimmedItem := strings.TrimSpace(rawPerm)
		if strings.ToLower(trimmedItem) == target {
			return true
		}
	}
	return false
}

type Authenticator interface {
	Authenticate(ctx context.Context, accessToken string) (uuid.UUID, error)
}

type PermissionAuthorizer interface {
	Authenticator
	AuthorizePermission(ctx context.Context, userID uuid.UUID, permission string) error
}

type PrincipalResolver interface {
	PermissionAuthorizer
	ResolvePrincipal(ctx context.Context, userID uuid.UUID) (Principal, error)
}

func RequireAuth(_ huma.API, auth Authenticator) func(huma.Context, func(huma.Context)) {
	return func(ctx huma.Context, next func(huma.Context)) {
		endpoint := buildEndpointLabel(ctx)
		token, id, apiErr := authenticateContext(ctx, auth, endpoint, "RequireBearerToken")
		if apiErr != nil {
			WriteAPIError(ctx, apiErr)
			return
		}
		enriched := attachIdentityValues(ctx, auth, id, token)
		next(enriched)
	}
}

func RequirePermission(_ huma.API, auth PermissionAuthorizer, permission string) func(huma.Context, func(huma.Context)) {
	policy := "RequirePermission:" + permission
	return func(ctx huma.Context, next func(huma.Context)) {
		endpoint := buildEndpointLabel(ctx)
		token, id, apiErr := authenticateContext(ctx, auth, endpoint, policy)
		if apiErr != nil {
			WriteAPIError(ctx, apiErr)
			return
		}
		authErr := auth.AuthorizePermission(ctx.Context(), id, permission)
		if authErr != nil {
			forbidden := NewAPIError(
				http.StatusForbidden,
				"FORBIDDEN_PERMISSION",
				"user lacks required operational permission",
				endpoint,
				policy,
				"missing required permission: "+permission,
			)
			WriteAPIError(ctx, forbidden)
			return
		}
		enriched := attachIdentityValues(ctx, auth, id, token)
		next(enriched)
	}
}

func RequireAnyPermission(_ huma.API, auth PermissionAuthorizer, permissions ...string) func(huma.Context, func(huma.Context)) {
	joined := strings.Join(permissions, "|")
	policy := "RequirePermission:" + joined
	return func(ctx huma.Context, next func(huma.Context)) {
		endpoint := buildEndpointLabel(ctx)
		token, id, apiErr := authenticateContext(ctx, auth, endpoint, policy)
		if apiErr != nil {
			WriteAPIError(ctx, apiErr)
			return
		}
		allowed := hasAnyAuthorizedPermission(ctx.Context(), auth, id, permissions)
		if !allowed {
			forbidden := NewAPIError(
				http.StatusForbidden,
				"FORBIDDEN_PERMISSION",
				"user lacks required operational permission",
				endpoint,
				policy,
				"missing required permission: "+joined,
			)
			WriteAPIError(ctx, forbidden)
			return
		}
		enriched := attachIdentityValues(ctx, auth, id, token)
		next(enriched)
	}
}

func OptionalAuth(_ huma.API, auth Authenticator) func(huma.Context, func(huma.Context)) {
	return func(ctx huma.Context, next func(huma.Context)) {
		rawHeader := strings.TrimSpace(ctx.Header("Authorization"))
		if rawHeader == "" {
			next(ctx)
			return
		}
		endpoint := buildEndpointLabel(ctx)
		token, id, apiErr := authenticateContext(ctx, auth, endpoint, "OptionalBearerToken")
		if apiErr != nil {
			WriteAPIError(ctx, apiErr)
			return
		}
		enriched := attachIdentityValues(ctx, auth, id, token)
		next(enriched)
	}
}

func authenticateContext(
	ctx huma.Context,
	auth Authenticator,
	endpoint string,
	policy string,
) (string, uuid.UUID, *APIError) {
	rawHeader := ctx.Header("Authorization")
	token, ok := bearerToken(rawHeader)
	if !ok {
		missingErr := NewAPIError(
			http.StatusUnauthorized,
			"AUTH_MISSING_TOKEN",
			"missing bearer token",
			endpoint,
			policy,
			"Authorization header with Bearer scheme is required",
		)
		return "", uuid.Nil, missingErr
	}
	reqCtx := ctx.Context()
	id, err := auth.Authenticate(reqCtx, token)
	if err != nil {
		invalidErr := NewAPIError(
			http.StatusUnauthorized,
			"AUTH_INVALID_TOKEN",
			"invalid or expired token",
			endpoint,
			policy,
			"Bearer token is invalid or session has expired",
		)
		return "", uuid.Nil, invalidErr
	}
	return token, id, nil
}

func hasAnyAuthorizedPermission(
	ctx context.Context,
	auth PermissionAuthorizer,
	userID uuid.UUID,
	permissions []string,
) bool {
	for _, perm := range permissions {
		if err := auth.AuthorizePermission(ctx, userID, perm); err == nil {
			return true
		}
	}
	return false
}

func attachIdentityValues(ctx huma.Context, auth Authenticator, userID uuid.UUID, token string) huma.Context {
	withUser := huma.WithValue(ctx, userKey{}, userID)
	withToken := huma.WithValue(withUser, tokenKey{}, token)
	resolver, ok := auth.(PrincipalResolver)
	if !ok {
		return withToken
	}
	reqCtx := ctx.Context()
	principal, err := resolver.ResolvePrincipal(reqCtx, userID)
	if err != nil {
		return withToken
	}
	return huma.WithValue(withToken, principalKey{}, principal)
}

func buildEndpointLabel(ctx huma.Context) string {
	reqURL := ctx.URL()
	return ctx.Method() + " " + reqURL.Path
}

func UserID(ctx context.Context) (uuid.UUID, bool) {
	id, ok := ctx.Value(userKey{}).(uuid.UUID)
	return id, ok
}

func AccessToken(ctx context.Context) (string, bool) {
	token, ok := ctx.Value(tokenKey{}).(string)
	return token, ok
}

func CurrentPrincipal(ctx context.Context) (Principal, bool) {
	principal, ok := ctx.Value(principalKey{}).(Principal)
	return principal, ok
}

func Secured() []map[string][]string {
	return []map[string][]string{{SecurityScheme: {}}}
}

func BearerToken(header string) (string, bool) {
	return bearerToken(header)
}

func bearerToken(header string) (string, bool) {
	const prefix = "Bearer "
	if len(header) <= len(prefix) {
		return "", false
	}
	hasPrefix := strings.EqualFold(header[:len(prefix)], prefix)
	if !hasPrefix {
		return "", false
	}
	trimmed := strings.TrimSpace(header[len(prefix):])
	if trimmed == "" {
		return "", false
	}
	return trimmed, true
}
