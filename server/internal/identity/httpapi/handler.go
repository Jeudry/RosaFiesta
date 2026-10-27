// Package httpapi publica los endpoints de autenticación, registro, cambio de vista (`A30`), clientes (`A26`/`A27`) y equipo (`A28`).
package httpapi

import (
	"context"
	"errors"
	"net/http"

	"github.com/danielgtaylor/huma/v2"

	identityapp "github.com/Jeudry/rosafiesta/server/internal/identity/app"
	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

// Module conecta el servicio de identidad al enrutador Huma.
type Module struct {
	Service *identityapp.Service
}

// LoginInput es el cuerpo de `/v1/auth/login` (`s07`).
type LoginInput struct {
	Body struct {
		Email    string `json:"email" doc:"Correo electrónico o username del usuario."`
		Password string `json:"password" doc:"Contraseña de la cuenta."`
	}
}

// RegisterInput es el cuerpo de `/v1/auth/register` (`s08`).
type RegisterInput struct {
	Body struct {
		Username  string `json:"username" doc:"Nombre de usuario de la cuenta."`
		FullName  string `json:"fullName" doc:"Nombre completo del cliente."`
		Email     string `json:"email" doc:"Correo electrónico del cliente."`
		Password  string `json:"password" doc:"Contraseña de la cuenta (mínimo 6 caracteres)."`
		BirthDate string `json:"birthDate,omitempty" doc:"Fecha de nacimiento (DD/MM/AAAA)."`
		Phone     string `json:"phone,omitempty" doc:"Teléfono de contacto."`
	}
}

// ResetPasswordInput es el cuerpo de `/v1/auth/reset-password` (`s10`).
type ResetPasswordInput struct {
	Body struct {
		Identifier  string `json:"identifier" doc:"Correo electrónico o username de la cuenta."`
		NewPassword string `json:"newPassword" doc:"Nueva contraseña (mínimo 6 caracteres)."`
	}
}

// LoginOutput devuelve el usuario autenticado y su token.
type LoginOutput struct {
	Body struct {
		AccessToken string      `json:"accessToken" doc:"Token Bearer para llamadas autenticadas."`
		User        domain.User `json:"user" doc:"Datos del usuario y vista activa."`
	}
}

// RegisteredUsersOutput devuelve la lista de cuentas registradas en PostgreSQL.
type RegisteredUsersOutput struct {
	Body struct {
		Users []domain.User `json:"users" doc:"Cuentas registradas en PostgreSQL."`
	}
}

// SwitchViewInput permite que un administrador alterne entre la App Administrativa y la App Personal (`A30`).
type SwitchViewInput struct {
	Body struct {
		View string `json:"view" doc:"Vista destino: personal o admin."`
	}
}

// SwitchViewOutput devuelve el usuario con la vista actualizada.
type SwitchViewOutput struct {
	Body struct {
		User domain.User `json:"user" doc:"Usuario con su nueva vista activa."`
	}
}

// ClientsOutput devuelve los clientes registrados (`A26`).
type ClientsOutput struct {
	Body struct {
		Clients []identityapp.ClientSummary `json:"clients" doc:"Lista de clientes de RosaFiesta."`
	}
}

// StaffOutput devuelve el equipo y sus permisos (`A28`).
type StaffOutput struct {
	Body struct {
		Staff []identityapp.StaffMember `json:"staff" doc:"Equipo operativo y sus permisos."`
	}
}

// DeleteAccountOutput confirma el estado de eliminación de cuenta (`s88`).
type DeleteAccountOutput struct {
	Body struct {
		Deleted bool `json:"deleted" doc:"Indica si la cuenta fue eliminada."`
	}
}

// Register registra las operaciones HTTP del módulo identity.
func (m Module) Register(api huma.API) {
	m.registerLoginAndSwitchRoutes(api)
	m.registerRegistrationRoutes(api)
	m.registerSessionRoutes(api)
	m.registerDeleteAccountRoute(api)
	m.registerAdminRoutes(api)
}

func (m Module) registerLoginAndSwitchRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "auth-login",
		Method:      http.MethodPost,
		Path:        "/v1/auth/login",
		Summary:     "Inicia sesión en RosaFiesta (s07)",
		Tags:        []string{"Identity"},
	}, func(ctx context.Context, in *LoginInput) (*LoginOutput, error) {
		user, token, err := m.Service.Login(ctx, in.Body.Email, in.Body.Password)
		if err != nil {
			return nil, platformhttp.NewAPIError(
				http.StatusUnauthorized,
				"AUTH_INVALID_CREDENTIALS",
				"invalid email or password",
				"POST /v1/auth/login",
				"VerifyCredentialsBcrypt",
				"credenciales inválidas para "+in.Body.Email,
			)
		}
		out := &LoginOutput{}
		out.Body.AccessToken = token
		out.Body.User = user
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "auth-switch-view",
		Method:      http.MethodPost,
		Path:        "/v1/auth/switch-view",
		Summary:     "Permite a un admin entrar como cliente o volver a negocio (A30)",
		Tags:        []string{"Identity"},
		Security:    platformhttp.Secured(),
		Middlewares: huma.Middlewares{platformhttp.RequireAuth(api, m.Service)},
	}, func(ctx context.Context, in *SwitchViewInput) (*SwitchViewOutput, error) {
		userID, _ := platformhttp.UserID(ctx)
		user, err := m.Service.SwitchView(ctx, userID, domain.AppView(in.Body.View))
		if err != nil {
			return nil, platformhttp.NewAPIError(
				http.StatusForbidden,
				"FORBIDDEN_APP_VIEW",
				err.Error(),
				"POST /v1/auth/switch-view",
				"CanAccessView:"+in.Body.View,
				err.Error(),
			)
		}
		out := &SwitchViewOutput{}
		out.Body.User = user
		return out, nil
	})
}

func (m Module) registerRegistrationRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "auth-register",
		Method:      http.MethodPost,
		Path:        "/v1/auth/register",
		Summary:     "Registra una cuenta nueva en PostgreSQL y emite su JWT (s08)",
		Tags:        []string{"Identity"},
	}, func(ctx context.Context, in *RegisterInput) (*LoginOutput, error) {
		cmd := identityapp.RegisterCommand{
			Username:  in.Body.Username,
			FullName:  in.Body.FullName,
			Email:     in.Body.Email,
			Password:  in.Body.Password,
			BirthDate: in.Body.BirthDate,
			Phone:     in.Body.Phone,
		}
		user, token, err := m.Service.Register(ctx, cmd)
		if err != nil {
			return nil, mapRegistrationError(err, in.Body.Email)
		}
		out := &LoginOutput{}
		out.Body.AccessToken = token
		out.Body.User = user
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "auth-reset-password",
		Method:      http.MethodPost,
		Path:        "/v1/auth/reset-password",
		Summary:     "Actualiza la contraseña de una cuenta en PostgreSQL (s10)",
		Tags:        []string{"Identity"},
	}, func(ctx context.Context, in *ResetPasswordInput) (*LoginOutput, error) {
		user, token, err := m.Service.ResetPassword(ctx, in.Body.Identifier, in.Body.NewPassword)
		if err != nil {
			return nil, platformhttp.NewAPIError(
				http.StatusBadRequest,
				"AUTH_PASSWORD_RESET_FAILED",
				err.Error(),
				"POST /v1/auth/reset-password",
				"ResetUserPasswordBcrypt",
				err.Error(),
			)
		}
		out := &LoginOutput{}
		out.Body.AccessToken = token
		out.Body.User = user
		return out, nil
	})
}

func (m Module) registerSessionRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "auth-me",
		Method:      http.MethodGet,
		Path:        "/v1/auth/me",
		Summary:     "Devuelve el usuario autenticado por su token Bearer",
		Tags:        []string{"Identity"},
		Security:    platformhttp.Secured(),
		Middlewares: huma.Middlewares{platformhttp.RequireAuth(api, m.Service)},
	}, func(ctx context.Context, _ *struct{}) (*SwitchViewOutput, error) {
		userID, _ := platformhttp.UserID(ctx)
		user, err := m.Service.CurrentUser(ctx, userID)
		if err != nil {
			return nil, platformhttp.NewAPIError(
				http.StatusUnauthorized,
				"AUTH_INVALID_TOKEN",
				"session user not found",
				"GET /v1/auth/me",
				"ResolveSessionUser",
				err.Error(),
			)
		}
		out := &SwitchViewOutput{}
		out.Body.User = user
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "auth-list-users",
		Method:      http.MethodGet,
		Path:        "/v1/auth/users",
		Summary:     "Lista las cuentas registradas en PostgreSQL",
		Tags:        []string{"Identity"},
	}, func(ctx context.Context, _ *struct{}) (*RegisteredUsersOutput, error) {
		out := &RegisteredUsersOutput{}
		out.Body.Users = m.Service.ListRegisteredUsers(ctx)
		return out, nil
	})
}

func mapRegistrationError(err error, email string) *platformhttp.APIError {
	if errors.Is(err, domain.ErrEmailAlreadyRegistered) {
		return platformhttp.NewAPIError(
			http.StatusConflict,
			"AUTH_EMAIL_TAKEN",
			"email already registered",
			"POST /v1/auth/register",
			"UniqueEmailConstraint",
			"el correo ya está registrado: "+email,
		)
	}
	return platformhttp.NewAPIError(
		http.StatusBadRequest,
		"AUTH_INVALID_REGISTRATION",
		err.Error(),
		"POST /v1/auth/register",
		"ValidateRegistrationInput",
		err.Error(),
	)
}

func (m Module) registerDeleteAccountRoute(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "auth-delete-account",
		Method:      http.MethodDelete,
		Path:        "/v1/account",
		Summary:     "Elimina la cuenta validando que no existan eventos activos (s88)",
		Tags:        []string{"Identity"},
		Security:    platformhttp.Secured(),
		Middlewares: huma.Middlewares{platformhttp.RequireAuth(api, m.Service)},
	}, func(ctx context.Context, _ *struct{}) (*DeleteAccountOutput, error) {
		userID, _ := platformhttp.UserID(ctx)
		if err := m.Service.DeleteAccount(ctx, userID); err != nil {
			if errors.Is(err, domain.ErrActiveEventsPreventDeletion) {
				return nil, platformhttp.NewAPIError(
					http.StatusConflict,
					"ACCOUNT_HAS_ACTIVE_EVENTS",
					"cannot delete account while active events exist",
					"DELETE /v1/account",
					"NoActiveEventsOnDelete",
					err.Error(),
				)
			}
			return nil, platformhttp.NewAPIError(
				http.StatusBadRequest,
				"ACCOUNT_DELETE_INVALID",
				err.Error(),
				"DELETE /v1/account",
				"ValidateAccountDeletion",
				err.Error(),
			)
		}
		out := &DeleteAccountOutput{}
		out.Body.Deleted = true
		return out, nil
	})
}

func (m Module) registerAdminRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "admin-list-clients",
		Method:      http.MethodGet,
		Path:        "/v1/admin/clients",
		Summary:     "Lista los clientes y su saldo (A26, A27)",
		Tags:        []string{"Identity"},
		Middlewares: huma.Middlewares{platformhttp.EnforcePermissionWhenAuthenticated(api, m.Service, "eventos")},
	}, func(ctx context.Context, _ *struct{}) (*ClientsOutput, error) {
		out := &ClientsOutput{}
		out.Body.Clients = m.Service.Clients(ctx)
		return out, nil
	})

	huma.Register(api, huma.Operation{
		OperationID: "admin-list-staff",
		Method:      http.MethodGet,
		Path:        "/v1/admin/staff",
		Summary:     "Lista el equipo y sus permisos operativos (A28)",
		Tags:        []string{"Identity"},
		Middlewares: huma.Middlewares{platformhttp.EnforcePermissionWhenAuthenticated(api, m.Service, "ajustes")},
	}, func(ctx context.Context, _ *struct{}) (*StaffOutput, error) {
		out := &StaffOutput{}
		out.Body.Staff = m.Service.Staff(ctx)
		return out, nil
	})
}
