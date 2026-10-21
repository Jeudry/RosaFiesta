package httpapi

import (
	"context"
	"net/http"
	"strings"

	"github.com/danielgtaylor/huma/v2"
	"github.com/google/uuid"
)

// SecurityScheme es el nombre del esquema de seguridad (token portador) que declara la especificación OpenAPI.
const SecurityScheme = "bearer"

type userKey struct{}

// Authenticator valida un token de acceso y devuelve a quién pertenece.
type Authenticator interface {
	// Authenticate devuelve el usuario dueño de accessToken, o un error si no sirve.
	Authenticate(ctx context.Context, accessToken string) (uuid.UUID, error)
}

// PermissionAuthorizer valida tanto el token de acceso como un permiso operativo de `A28`.
type PermissionAuthorizer interface {
	Authenticator
	// AuthorizePermission verifica que userID posea el permiso operativo indicado.
	AuthorizePermission(ctx context.Context, userID uuid.UUID, permission string) error
}

// RequireAuth devuelve el middleware que exige `Authorization: Bearer <token>` y deja el usuario en el contexto.
func RequireAuth(_ huma.API, auth Authenticator) func(huma.Context, func(huma.Context)) {
	return func(ctx huma.Context, next func(huma.Context)) {
		endpoint := ctx.Method() + " " + ctx.URL().Path
		token, ok := bearerToken(ctx.Header("Authorization"))
		if !ok {
			WriteAPIError(ctx, NewAPIError(
				http.StatusUnauthorized,
				"AUTH_MISSING_TOKEN",
				"missing bearer token",
				endpoint,
				"RequireBearerToken",
				"Authorization header with Bearer scheme is required",
			))
			return
		}
		id, err := auth.Authenticate(ctx.Context(), token)
		if err != nil {
			WriteAPIError(ctx, NewAPIError(
				http.StatusUnauthorized,
				"AUTH_INVALID_TOKEN",
				"invalid or expired token",
				endpoint,
				"RequireBearerToken",
				"Bearer token is invalid or session has expired",
			))
			return
		}
		next(huma.WithValue(ctx, userKey{}, id))
	}
}

// RequirePermission exige un token Bearer válido y que el usuario posea el permiso indicado (`A28`).
func RequirePermission(_ huma.API, auth PermissionAuthorizer, permission string) func(huma.Context, func(huma.Context)) {
	policy := "RequirePermission:" + permission
	return func(ctx huma.Context, next func(huma.Context)) {
		endpoint := ctx.Method() + " " + ctx.URL().Path
		token, ok := bearerToken(ctx.Header("Authorization"))
		if !ok {
			WriteAPIError(ctx, NewAPIError(
				http.StatusUnauthorized,
				"AUTH_MISSING_TOKEN",
				"missing bearer token for protected admin operation",
				endpoint,
				policy,
				"Authorization header with Bearer token is required",
			))
			return
		}
		id, err := auth.Authenticate(ctx.Context(), token)
		if err != nil {
			WriteAPIError(ctx, NewAPIError(
				http.StatusUnauthorized,
				"AUTH_INVALID_TOKEN",
				"invalid or expired token",
				endpoint,
				policy,
				"Bearer token is invalid or session has expired",
			))
			return
		}
		if err := auth.AuthorizePermission(ctx.Context(), id, permission); err != nil {
			WriteAPIError(ctx, NewAPIError(
				http.StatusForbidden,
				"FORBIDDEN_PERMISSION",
				"user lacks required operational permission",
				endpoint,
				policy,
				"missing required permission: "+permission,
			))
			return
		}
		next(huma.WithValue(ctx, userKey{}, id))
	}
}

// EnforcePermissionWhenAuthenticated valida el token y el permiso cuando la petición incluye cabecera Authorization.
func EnforcePermissionWhenAuthenticated(
	_ huma.API,
	auth PermissionAuthorizer,
	permission string,
) func(huma.Context, func(huma.Context)) {
	policy := "RequirePermission:" + permission
	return func(ctx huma.Context, next func(huma.Context)) {
		rawHeader := strings.TrimSpace(ctx.Header("Authorization"))
		if rawHeader == "" {
			next(ctx)
			return
		}
		endpoint := ctx.Method() + " " + ctx.URL().Path
		token, ok := bearerToken(rawHeader)
		if !ok {
			WriteAPIError(ctx, NewAPIError(
				http.StatusUnauthorized,
				"AUTH_INVALID_HEADER",
				"malformed authorization header",
				endpoint,
				policy,
				"Authorization header must use Bearer scheme",
			))
			return
		}
		id, err := auth.Authenticate(ctx.Context(), token)
		if err != nil {
			WriteAPIError(ctx, NewAPIError(
				http.StatusUnauthorized,
				"AUTH_INVALID_TOKEN",
				"invalid or expired token",
				endpoint,
				policy,
				"Bearer token is invalid or session has expired",
			))
			return
		}
		if err := auth.AuthorizePermission(ctx.Context(), id, permission); err != nil {
			WriteAPIError(ctx, NewAPIError(
				http.StatusForbidden,
				"FORBIDDEN_PERMISSION",
				"user lacks required operational permission",
				endpoint,
				policy,
				"missing required permission: "+permission,
			))
			return
		}
		next(huma.WithValue(ctx, userKey{}, id))
	}
}

// UserID devuelve el usuario autenticado de la petición, si lo hay.
func UserID(ctx context.Context) (uuid.UUID, bool) {
	id, ok := ctx.Value(userKey{}).(uuid.UUID)
	return id, ok
}

// Secured devuelve la declaración de seguridad de una operación protegida.
func Secured() []map[string][]string {
	return []map[string][]string{{SecurityScheme: {}}}
}

// BearerToken extrae el token de una cabecera Authorization.
func BearerToken(header string) (string, bool) { return bearerToken(header) }

func bearerToken(header string) (string, bool) {
	const prefix = "Bearer "
	if len(header) <= len(prefix) || !strings.EqualFold(header[:len(prefix)], prefix) {
		return "", false
	}
	return strings.TrimSpace(header[len(prefix):]), true
}
