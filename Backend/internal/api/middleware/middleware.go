package middleware

import (
	"context"
	"encoding/base64"
	"fmt"
	"net/http"
	"strings"

	"Backend/internal/app"
	"Backend/internal/store/models"
	"Backend/internal/utils"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

type Middleware struct {
	app       *app.Application
	responder *utils.Responder
}

func NewMiddleware(app *app.Application, responder *utils.Responder) *Middleware {
	return &Middleware{app: app, responder: responder}
}

func (m *Middleware) CheckRole(role string, next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		user := GetUserFromCtx(r)

		allowed, err := m.checkRolePrecedence(r.Context(), user, role)
		if err != nil {
			m.responder.InternalServerError(w, r, err)
			return
		}

		if !allowed {
			m.responder.Forbidden(w, r, fmt.Errorf("you do not have permission to access this resource"))
			return
		}

		next(w, r)
	}
}

func (m *Middleware) CheckPostOwnerShip(role string, next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		user := GetUserFromCtx(r)
		post := GetPostFromCtx(r)

		if post.UserID == user.ID {
			next(w, r)
			return
		}

		allowed, err := m.checkRolePrecedence(r.Context(), user, role)
		if err != nil {
			m.responder.InternalServerError(w, r, err)
			return
		}

		if !allowed {
			m.responder.Forbidden(w, r, fmt.Errorf("you do not have permission to access this resource"))
			return
		}

		next.ServeHTTP(w, r)
	}
}

func (m *Middleware) checkRolePrecedence(ctx context.Context, user *models.User, roleName string) (bool, error) {
	role, err := m.app.Store.Roles.RetrieveByName(ctx, roleName)
	if err != nil {
		return false, err
	}

	return role.Level <= user.Role.Level, nil
}

func (m *Middleware) RoleMiddleware(roles ...string) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			user := GetUserFromCtx(r)

			for _, role := range roles {
				allowed, err := m.checkRolePrecedence(r.Context(), user, role)
				if err != nil || !allowed {
					m.responder.Forbidden(w, r, fmt.Errorf("you do not have permission to access this resource"))
					return
				}
			}

			next.ServeHTTP(w, r)
		})
	}
}

func (m *Middleware) APIKeyMiddleware() func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			apiKeyHeader := m.app.Config.Auth.ApiKey.Header
			apiKeyValue := m.app.Config.Auth.ApiKey.Value

			if apiKeyHeader == "" || apiKeyValue == "" {
				m.app.Logger.Warn("API Key authentication is not properly configured")
				next.ServeHTTP(w, r)
				return
			}

			requestKey := r.Header.Get(apiKeyHeader)

			if requestKey == "" {
				m.responder.Unauthorized(w, r, fmt.Errorf("missing API key in header %s", apiKeyHeader))
				return
			}

			if requestKey != apiKeyValue {
				m.responder.Unauthorized(w, r, fmt.Errorf("invalid API key"))
				return
			}

			// Provide a virtual user for auditing purposes
			virtualUser := &models.User{
				UserName: "API-Key-User",
				Role: models.Role{
					Name:  "admin",
					Level: 2, // Admin level
				},
			}
			ctx := context.WithValue(r.Context(), UserCtx, virtualUser)
			next.ServeHTTP(w, r.WithContext(ctx))
		})
	}
}

func (m *Middleware) AuthTokenMiddleware(roles ...string) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			authHeader := r.Header.Get("Authorization")

			if authHeader == "" {
				m.responder.Unauthorized(w, r, fmt.Errorf("authorization header required"))
				return
			}

			parts := strings.Split(authHeader, " ")

			if len(parts) != 2 || parts[0] != "Bearer" {
				m.responder.Unauthorized(w, r, fmt.Errorf("authorization header format must be 'Bearer token'"))
				return
			}

			token := parts[1]

			m.app.Logger.Infow("token", "token", token)

			jwtToken, err := m.app.Auth.ValidateToken(token)

			m.app.Logger.Infow("jwtToken", "jwtToken", jwtToken)

			if err != nil {
				m.responder.Unauthorized(w, r, err)
				return
			}

			claims, _ := jwtToken.Claims.(jwt.MapClaims)
			userIDStr, ok := claims["sub"].(string)
			if !ok {
				m.responder.Unauthorized(w, r, fmt.Errorf("invalid token claims"))
				return
			}

			userID, err := uuid.Parse(userIDStr)
			if err != nil {
				m.responder.Unauthorized(w, r, err)
				return
			}

			ctx := r.Context()

			user, err := m.app.GetUser(ctx, userID)
			if err != nil {
				m.responder.Unauthorized(w, r, err)
				return
			}

			for _, role := range roles {
				allowed, err := m.checkRolePrecedence(ctx, user, role)
				if err != nil || !allowed {
					m.responder.Forbidden(w, r, fmt.Errorf("you do not have permission to access this resource"))
					return
				}
			}

			ctx = context.WithValue(r.Context(), UserCtx, user)
			next.ServeHTTP(w, r.WithContext(ctx))
		})
	}
}

func (m *Middleware) BasicAuthMiddleware() func(handler http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			authHeader := r.Header.Get("Authorization")
			if authHeader == "" {
				m.responder.BasicUnauthorized(w, r, fmt.Errorf("no authorization header provided"))
				return
			}

			parts := strings.Split(authHeader, " ")
			if len(parts) != 2 || parts[0] != "Basic" {
				m.responder.BasicUnauthorized(w, r, fmt.Errorf("authorization header format must be 'Basic base64encodedstring'"))
				return
			}

			decoded, err := base64.StdEncoding.DecodeString(parts[1])
			if err != nil {
				m.responder.BasicUnauthorized(w, r, err)
				return
			}

			userName := m.app.Config.Auth.Basic.User
			password := m.app.Config.Auth.Basic.Pass

			creds := strings.SplitN(string(decoded), ":", 2)

			if len(creds) != 2 {
				m.responder.BasicUnauthorized(w, r, fmt.Errorf("authorization header must contain a username and password separated by a colon"))
				return
			}

			if creds[0] != userName || creds[1] != password {
				m.responder.BasicUnauthorized(w, r, fmt.Errorf("invalid credentials"))
				return
			}

			next.ServeHTTP(w, r)
		})
	}
}

func (m *Middleware) RateLimiterMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if m.app.Config.RateLimiter.Enabled {
			if allow, retryAfter := m.app.RateLimiter.Allow(r.RemoteAddr); !allow {
				m.responder.RateLimitExceededResponse(w, r, retryAfter.String())
				return
			}
		}

		next.ServeHTTP(w, r)
	})
}
