// Package httpapi publica los endpoints de autenticación, cambio de vista (`A30`), clientes (`A26`/`A27`) y equipo (`A28`).
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
		Email    string `json:"email" doc:"Correo electrónico del usuario."`
		Password string `json:"password" doc:"Contraseña de la cuenta."`
	}
}

// LoginOutput devuelve el usuario autenticado y su token.
type LoginOutput struct {
	Body struct {
		AccessToken string      `json:"accessToken" doc:"Token Bearer para llamadas autenticadas."`
		User        domain.User `json:"user" doc:"Datos del usuario y vista activa."`
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
	m.registerAuthRoutes(api)
	m.registerAdminRoutes(api)
}

func (m Module) registerAuthRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "auth-login",
		Method:      http.MethodPost,
		Path:        "/v1/auth/login",
		Summary:     "Inicia sesión en RosaFiesta (s07)",
		Tags:        []string{"Identity"},
	}, func(ctx context.Context, in *LoginInput) (*LoginOutput, error) {
		user, token, err := m.Service.Login(ctx, in.Body.Email, in.Body.Password)
		if err != nil {
			return nil, huma.Error401Unauthorized("invalid email or password")
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
			return nil, huma.Error403Forbidden(err.Error())
		}
		out := &SwitchViewOutput{}
		out.Body.User = user
		return out, nil
	})

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
				return nil, huma.Error409Conflict("cannot delete account while active events exist")
			}
			return nil, huma.Error400BadRequest(err.Error())
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
	}, func(ctx context.Context, _ *struct{}) (*StaffOutput, error) {
		out := &StaffOutput{}
		out.Body.Staff = m.Service.Staff(ctx)
		return out, nil
	})
}
