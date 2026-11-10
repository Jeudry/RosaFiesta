package httpapi

import (
	"context"
	"net/http"

	"github.com/danielgtaylor/huma/v2"

	identityapp "github.com/Jeudry/rosafiesta/server/internal/identity/app"
	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type Module struct {
	Service *identityapp.Service
}

func (m Module) Register(api huma.API) {
	m.registerLoginAndOAuthRoutes(api)
	m.registerRegistrationAndOTPRoutes(api)
	m.registerSessionAndProfileRoutes(api)
	m.registerDeleteAccountRoute(api)
	m.registerAdminClientRoutes(api)
	m.registerAdminStaffRoutes(api)
}

func (m Module) registerLoginAndOAuthRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "auth-login",
		Method:      http.MethodPost,
		Path:        "/v1/auth/login",
		Summary:     "Authenticate user credentials with bcrypt",
		Tags:        []string{"Identity"},
	}, m.handleLogin)

	huma.Register(api, huma.Operation{
		OperationID: "auth-oauth",
		Method:      http.MethodPost,
		Path:        "/v1/auth/oauth",
		Summary:     "Authenticate with OAuth provider identity token",
		Tags:        []string{"Identity"},
	}, m.handleOAuthLogin)

	huma.Register(api, huma.Operation{
		OperationID: "auth-switch-view",
		Method:      http.MethodPost,
		Path:        "/v1/auth/switch-view",
		Summary:     "Switch between administrative and personal views",
		Tags:        []string{"Identity"},
		Security:    platformhttp.Secured(),
		Middlewares: huma.Middlewares{platformhttp.RequireAuth(api, m.Service)},
	}, m.handleSwitchView)
}

func (m Module) registerRegistrationAndOTPRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "auth-register",
		Method:      http.MethodPost,
		Path:        "/v1/auth/register",
		Summary:     "Register a new account and issue a signed JWT",
		Tags:        []string{"Identity"},
	}, m.handleRegister)

	huma.Register(api, huma.Operation{
		OperationID: "auth-otp-request",
		Method:      http.MethodPost,
		Path:        "/v1/auth/otp/request",
		Summary:     "Generate and dispatch a cryptographic six-digit verification code",
		Tags:        []string{"Identity"},
	}, m.handleOTPRequest)

	huma.Register(api, huma.Operation{
		OperationID: "auth-otp-verify",
		Method:      http.MethodPost,
		Path:        "/v1/auth/otp/verify",
		Summary:     "Verify OTP code and issue a single-use password reset token",
		Tags:        []string{"Identity"},
	}, m.handleOTPVerify)

	huma.Register(api, huma.Operation{
		OperationID: "auth-reset-password",
		Method:      http.MethodPost,
		Path:        "/v1/auth/reset-password",
		Summary:     "Reset account password using a verified reset token or OTP code",
		Tags:        []string{"Identity"},
	}, m.handleResetPassword)
}

func (m Module) registerSessionAndProfileRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "auth-logout",
		Method:      http.MethodPost,
		Path:        "/v1/auth/logout",
		Summary:     "Revoke and delete the current bearer session",
		Tags:        []string{"Identity"},
		Security:    platformhttp.Secured(),
		Middlewares: huma.Middlewares{platformhttp.RequireAuth(api, m.Service)},
	}, m.handleLogout)

	huma.Register(api, huma.Operation{
		OperationID: "auth-me",
		Method:      http.MethodGet,
		Path:        "/v1/auth/me",
		Summary:     "Return the authenticated user profile",
		Tags:        []string{"Identity"},
		Security:    platformhttp.Secured(),
		Middlewares: huma.Middlewares{platformhttp.RequireAuth(api, m.Service)},
	}, m.handleMe)

	huma.Register(api, huma.Operation{
		OperationID: "auth-update-me",
		Method:      http.MethodPatch,
		Path:        "/v1/auth/me",
		Summary:     "Update the authenticated user profile",
		Tags:        []string{"Identity"},
		Security:    platformhttp.Secured(),
		Middlewares: huma.Middlewares{platformhttp.RequireAuth(api, m.Service)},
	}, m.handleUpdateMe)

	huma.Register(api, huma.Operation{
		OperationID: "auth-change-password",
		Method:      http.MethodPost,
		Path:        "/v1/auth/change-password",
		Summary:     "Verify current password and update password hash",
		Tags:        []string{"Identity"},
		Security:    platformhttp.Secured(),
		Middlewares: huma.Middlewares{platformhttp.RequireAuth(api, m.Service)},
	}, m.handleChangePassword)

	huma.Register(api, huma.Operation{
		OperationID: "auth-list-users",
		Method:      http.MethodGet,
		Path:        "/v1/auth/users",
		Summary:     "List registered accounts for administrators",
		Tags:        []string{"Identity"},
		Security:    platformhttp.Secured(),
		Middlewares: huma.Middlewares{platformhttp.RequirePermission(api, m.Service, "ajustes")},
	}, m.handleListUsers)
}

func (m Module) registerDeleteAccountRoute(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "auth-delete-account",
		Method:      http.MethodDelete,
		Path:        "/v1/account",
		Summary:     "Delete authenticated account when no active events remain",
		Tags:        []string{"Identity"},
		Security:    platformhttp.Secured(),
		Middlewares: huma.Middlewares{platformhttp.RequireAuth(api, m.Service)},
	}, m.handleDeleteAccount)
}

func (m Module) registerAdminClientRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "admin-list-clients",
		Method:      http.MethodGet,
		Path:        "/v1/admin/clients",
		Summary:     "List clients and balances",
		Tags:        []string{"Identity"},
		Security:    platformhttp.Secured(),
		Middlewares: huma.Middlewares{platformhttp.RequirePermission(api, m.Service, "eventos")},
	}, m.handleListClients)

	huma.Register(api, huma.Operation{
		OperationID: "admin-update-client",
		Method:      http.MethodPatch,
		Path:        "/v1/admin/clients/{id}",
		Summary:     "Update administrative client contact details and notes",
		Tags:        []string{"Identity"},
		Security:    platformhttp.Secured(),
		Middlewares: huma.Middlewares{platformhttp.RequirePermission(api, m.Service, "eventos")},
	}, m.handleUpdateClient)
}

func (m Module) registerAdminStaffRoutes(api huma.API) {
	huma.Register(api, huma.Operation{
		OperationID: "admin-list-staff",
		Method:      http.MethodGet,
		Path:        "/v1/admin/staff",
		Summary:     "List operational staff members and permissions",
		Tags:        []string{"Identity"},
		Security:    platformhttp.Secured(),
		Middlewares: huma.Middlewares{platformhttp.RequirePermission(api, m.Service, "ajustes")},
	}, m.handleListStaff)

	huma.Register(api, huma.Operation{
		OperationID: "admin-invite-staff",
		Method:      http.MethodPost,
		Path:        "/v1/admin/staff",
		Summary:     "Invite a new staff member and configure permissions",
		Tags:        []string{"Identity"},
		Security:    platformhttp.Secured(),
		Middlewares: huma.Middlewares{platformhttp.RequirePermission(api, m.Service, "ajustes")},
	}, m.handleInviteStaff)

	huma.Register(api, huma.Operation{
		OperationID: "admin-update-staff",
		Method:      http.MethodPatch,
		Path:        "/v1/admin/staff/{id}",
		Summary:     "Update staff member role and operational permissions",
		Tags:        []string{"Identity"},
		Security:    platformhttp.Secured(),
		Middlewares: huma.Middlewares{platformhttp.RequirePermission(api, m.Service, "ajustes")},
	}, m.handleUpdateStaff)
}

func (m Module) handleLogin(ctx context.Context, in *LoginInput) (*LoginOutput, error) {
	user, token, err := m.Service.Login(ctx, in.Body.Email, in.Body.Password)
	if err != nil {
		reason := "invalid credentials for " + in.Body.Email
		return nil, platformhttp.NewAPIError(
			http.StatusUnauthorized,
			"AUTH_INVALID_CREDENTIALS",
			"invalid email or password",
			"POST /v1/auth/login",
			"VerifyCredentialsBcrypt",
			reason,
		)
	}
	return newLoginOutput(user, token), nil
}

func (m Module) handleOAuthLogin(ctx context.Context, in *OAuthLoginInput) (*LoginOutput, error) {
	user, token, err := m.Service.OAuthLogin(ctx, in.Body.Provider, in.Body.IDToken)
	if err != nil {
		return nil, mapOAuthError(err)
	}
	return newLoginOutput(user, token), nil
}

func (m Module) handleSwitchView(ctx context.Context, in *SwitchViewInput) (*SwitchViewOutput, error) {
	userID, _ := platformhttp.UserID(ctx)
	targetView := domain.AppView(in.Body.View)
	user, err := m.Service.SwitchView(ctx, userID, targetView)
	if err != nil {
		policy := "CanAccessView:" + in.Body.View
		return nil, platformhttp.NewAPIError(
			http.StatusForbidden,
			"FORBIDDEN_APP_VIEW",
			err.Error(),
			"POST /v1/auth/switch-view",
			policy,
			err.Error(),
		)
	}
	return newUserOutput(user), nil
}

func (m Module) handleRegister(ctx context.Context, in *RegisterInput) (*LoginOutput, error) {
	cmd := in.ToCommand()
	user, token, err := m.Service.Register(ctx, cmd)
	if err != nil {
		return nil, mapRegistrationError(err, in.Body.Email, "POST /v1/auth/register")
	}
	return newLoginOutput(user, token), nil
}

func (m Module) handleOTPRequest(ctx context.Context, in *OTPRequestInput) (*OTPRequestOutput, error) {
	res, err := m.Service.RequestOTP(ctx, in.Body.Email)
	if err != nil {
		return nil, mapOTPRequestError(err)
	}
	return &OTPRequestOutput{Body: res}, nil
}

func (m Module) handleOTPVerify(ctx context.Context, in *OTPVerifyInput) (*OTPVerifyOutput, error) {
	res, err := m.Service.VerifyOTP(ctx, in.Body.Email, in.Body.Code)
	if err != nil {
		return nil, platformhttp.NewAPIError(
			http.StatusUnauthorized,
			"AUTH_OTP_INVALID",
			err.Error(),
			"POST /v1/auth/otp/verify",
			"VerifyCryptographicOTP",
			err.Error(),
		)
	}
	return &OTPVerifyOutput{Body: res}, nil
}

func (m Module) handleResetPassword(ctx context.Context, in *ResetPasswordInput) (*LoginOutput, error) {
	cmd := in.ToCommand()
	user, token, err := m.Service.ResetPassword(ctx, cmd)
	if err != nil {
		return nil, mapPasswordResetError(err)
	}
	return newLoginOutput(user, token), nil
}

func (m Module) handleLogout(ctx context.Context, _ *struct{}) (*LogoutOutput, error) {
	rawToken, _ := platformhttp.AccessToken(ctx)
	if err := m.Service.Logout(ctx, rawToken); err != nil {
		return nil, platformhttp.NewAPIError(
			http.StatusUnauthorized,
			"AUTH_INVALID_TOKEN",
			err.Error(),
			"POST /v1/auth/logout",
			"RevokeSessionToken",
			err.Error(),
		)
	}
	out := &LogoutOutput{}
	out.Body.LoggedOut = true
	return out, nil
}

func (m Module) handleMe(ctx context.Context, _ *struct{}) (*SwitchViewOutput, error) {
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
	return newUserOutput(user), nil
}

func (m Module) handleUpdateMe(ctx context.Context, in *UpdateProfileInput) (*SwitchViewOutput, error) {
	userID, _ := platformhttp.UserID(ctx)
	cmd := in.ToCommand()
	user, err := m.Service.UpdateProfile(ctx, userID, cmd)
	if err != nil {
		emailHint := ""
		if in.Body.Email != nil {
			emailHint = *in.Body.Email
		}
		return nil, mapRegistrationError(err, emailHint, "PATCH /v1/auth/me")
	}
	return newUserOutput(user), nil
}

func (m Module) handleChangePassword(ctx context.Context, in *ChangePasswordInput) (*PasswordChangedOutput, error) {
	userID, _ := platformhttp.UserID(ctx)
	err := m.Service.ChangePassword(ctx, userID, in.Body.CurrentPassword, in.Body.NewPassword)
	if err != nil {
		return nil, mapChangePasswordError(err)
	}
	out := &PasswordChangedOutput{}
	out.Body.Updated = true
	return out, nil
}

func (m Module) handleListUsers(ctx context.Context, _ *struct{}) (*RegisteredUsersOutput, error) {
	out := &RegisteredUsersOutput{}
	out.Body.Users = m.Service.ListRegisteredUsers(ctx)
	return out, nil
}

func (m Module) handleDeleteAccount(ctx context.Context, _ *struct{}) (*DeleteAccountOutput, error) {
	userID, _ := platformhttp.UserID(ctx)
	if err := m.Service.DeleteAccount(ctx, userID); err != nil {
		return nil, mapDeleteAccountError(err)
	}
	out := &DeleteAccountOutput{}
	out.Body.Deleted = true
	return out, nil
}

func (m Module) handleListClients(ctx context.Context, _ *struct{}) (*ClientsOutput, error) {
	out := &ClientsOutput{}
	out.Body.Clients = m.Service.Clients(ctx)
	return out, nil
}

func (m Module) handleUpdateClient(ctx context.Context, in *UpdateClientInput) (*ClientOutput, error) {
	cmd := in.ToCommand()
	updated, err := m.Service.UpdateClient(ctx, in.ID, cmd)
	if err != nil {
		return nil, mapAdminMutationError(err, "PATCH /v1/admin/clients/"+in.ID)
	}
	out := &ClientOutput{}
	out.Body.Client = updated
	return out, nil
}

func (m Module) handleListStaff(ctx context.Context, _ *struct{}) (*StaffOutput, error) {
	out := &StaffOutput{}
	out.Body.Staff = m.Service.Staff(ctx)
	return out, nil
}

func (m Module) handleInviteStaff(ctx context.Context, in *InviteStaffInput) (*StaffMemberOutput, error) {
	cmd := in.ToCommand()
	created, err := m.Service.InviteStaff(ctx, cmd)
	if err != nil {
		return nil, mapAdminMutationError(err, "POST /v1/admin/staff")
	}
	out := &StaffMemberOutput{}
	out.Body.Member = created
	return out, nil
}

func (m Module) handleUpdateStaff(ctx context.Context, in *UpdateStaffInput) (*StaffMemberOutput, error) {
	cmd := in.ToCommand()
	updated, err := m.Service.UpdateStaff(ctx, in.ID, cmd)
	if err != nil {
		return nil, mapAdminMutationError(err, "PATCH /v1/admin/staff/"+in.ID)
	}
	out := &StaffMemberOutput{}
	out.Body.Member = updated
	return out, nil
}
