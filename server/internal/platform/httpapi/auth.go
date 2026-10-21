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

// RequireAuth devuelve el middleware que exige `Authorization: Bearer <token>` y deja el usuario en el contexto.
func RequireAuth(api huma.API, auth Authenticator) func(huma.Context, func(huma.Context)) {
	return func(ctx huma.Context, next func(huma.Context)) {
		token, ok := bearerToken(ctx.Header("Authorization"))
		if !ok {
			_ = huma.WriteErr(api, ctx, http.StatusUnauthorized, "missing bearer token")
			return
		}
		id, err := auth.Authenticate(ctx.Context(), token)
		if err != nil {
			_ = huma.WriteErr(api, ctx, http.StatusUnauthorized, "invalid or expired token")
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
