package main

import (
	"context"
	"encoding/base64"
	"fmt"
	"net/http"
	"strings"

	"Backend/internal/store/models"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

func (app *Application) CheckRole(role string, next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		user := GetUserFromCtx(r)

		allowed, err := app.checkRolePrecedence(r.Context(), user, role)
		if err != nil {
			app.internalServerError(w, r, err)
			return
		}

		if !allowed {
			app.forbidden(w, r, fmt.Errorf("you do not have permission to access this resource"))
			return
		}

		next(w, r)
	}
}

func (app *Application) CheckPostOwnerShip(role string, next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		user := GetUserFromCtx(r)
		post := GetPostFromCtx(r)

		if post.UserID == user.ID {
			next(w, r)
			return
		}

		allowed, err := app.checkRolePrecedence(r.Context(), user, role)
		if err != nil {
			app.internalServerError(w, r, err)
			return
		}

		if !allowed {
			app.forbidden(w, r, fmt.Errorf("you do not have permission to access this resource"))
			return
		}

		next.ServeHTTP(w, r)
	}
}

func (app *Application) checkRolePrecedence(ctx context.Context, user *models.User, roleName string) (bool, error) {
	role, err := app.Store.Roles.RetrieveByName(ctx, roleName)
	if err != nil {
		return false, err
	}

	return role.Level <= user.Role.Level, nil
}

func (app *Application) RoleMiddleware(roles ...string) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			user := GetUserFromCtx(r)

			for _, role := range roles {
				allowed, err := app.checkRolePrecedence(r.Context(), user, role)
				if err != nil || !allowed {
					app.forbidden(w, r, fmt.Errorf("you do not have permission to access this resource"))
					return
				}
			}

			next.ServeHTTP(w, r)
		})
	}
}

func (app *Application) APIKeyMiddleware() func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			apiKeyHeader := app.Config.Auth.ApiKey.Header
			apiKeyValue := app.Config.Auth.ApiKey.Value

			if apiKeyHeader == "" || apiKeyValue == "" {
				app.Logger.Warn("API Key authentication is not properly configured")
				next.ServeHTTP(w, r)
				return
			}

			requestKey := r.Header.Get(apiKeyHeader)

			if requestKey == "" {
				app.unauthorized(w, r, fmt.Errorf("missing API key in header %s", apiKeyHeader))
				return
			}

			if requestKey != apiKeyValue {
				app.unauthorized(w, r, fmt.Errorf("invalid API key"))
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

func (app *Application) AuthTokenMiddleware(roles ...string) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			authHeader := r.Header.Get("Authorization")

			if authHeader == "" {
				app.unauthorized(w, r, fmt.Errorf("authorization header required"))
				return
			}

			parts := strings.Split(authHeader, " ")

			if len(parts) != 2 || parts[0] != "Bearer" {
				app.unauthorized(w, r, fmt.Errorf("authorization header format must be 'Bearer token'"))
				return
			}

			token := parts[1]

			app.Logger.Infow("token", "token", token)

			jwtToken, err := app.Auth.ValidateToken(token)

			app.Logger.Infow("jwtToken", "jwtToken", jwtToken)

			if err != nil {
				app.unauthorized(w, r, err)
				return
			}

			claims, _ := jwtToken.Claims.(jwt.MapClaims)

			userID, err := uuid.Parse(claims["sub"].(string))
			if err != nil {
				app.unauthorized(w, r, err)
				return
			}

			ctx := r.Context()

			user, err := app.GetUser(ctx, userID)
			if err != nil {
				app.unauthorized(w, r, err)
				return
			}

			for _, role := range roles {
				allowed, err := app.checkRolePrecedence(ctx, user, role)
				if err != nil || !allowed {
					app.forbidden(w, r, fmt.Errorf("you do not have permission to access this resource"))
					return
				}
			}

			ctx = context.WithValue(r.Context(), UserCtx, user)
			next.ServeHTTP(w, r.WithContext(ctx))
		})
	}
}

func (app *Application) BasicAuthMiddleware() func(handler http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			authHeader := r.Header.Get("Authorization")
			if authHeader == "" {
				app.basicUnauthorized(w, r, fmt.Errorf("no authorization header provided"))
				return
			}

			parts := strings.Split(authHeader, " ")
			if len(parts) != 2 || parts[0] != "Basic" {
				app.basicUnauthorized(w, r, fmt.Errorf("authorization header format must be 'Basic base64encodedstring'"))
				return
			}

			decoded, err := base64.StdEncoding.DecodeString(parts[1])
			if err != nil {
				app.basicUnauthorized(w, r, err)
				return
			}

			userName := app.Config.Auth.Basic.User
			password := app.Config.Auth.Basic.Pass

			creds := strings.SplitN(string(decoded), ":", 2)

			if len(creds) != 2 {
				app.basicUnauthorized(w, r, fmt.Errorf("authorization header must contain a username and password separated by a colon"))
				return
			}

			if creds[0] != userName || creds[1] != password {
				app.basicUnauthorized(w, r, fmt.Errorf("invalid credentials"))
				return
			}

			next.ServeHTTP(w, r)
		})
	}
}

func (app *Application) GetUser(ctx context.Context, userID uuid.UUID) (*models.User, error) {
	if !app.Config.Redis.Enabled {
		user, err := app.Store.Users.RetrieveById(ctx, userID)
		if err != nil {
			return nil, err
		}
		return user, nil
	}

	user, err := app.CacheStorage.Users.Get(ctx, userID)
	if err != nil {
		return nil, err
	}

	if user == nil {
		user, err = app.Store.Users.RetrieveById(ctx, userID)
		if err != nil {
			return nil, err
		}

		err = app.CacheStorage.Users.Set(ctx, user)
		if err != nil {
			return nil, err
		}
	}

	return user, nil
}

func (app *Application) RateLimiterMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if app.Config.RateLimiter.Enabled {
			if allow, retryAfter := app.RateLimiter.Allow(r.RemoteAddr); !allow {
				app.rateLimitExceededResponse(w, r, retryAfter.String())
				return
			}
		}

		next.ServeHTTP(w, r)
	})
}
