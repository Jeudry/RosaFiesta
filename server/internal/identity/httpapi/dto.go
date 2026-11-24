package httpapi

import (
	"errors"
	"net/http"

	identityapp "github.com/Jeudry/rosafiesta/server/internal/identity/app"
	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	platformhttp "github.com/Jeudry/rosafiesta/server/internal/platform/httpapi"
)

type LoginInput struct {
	Body struct {
		Email    string `json:"email" doc:"Email address or username."`
		Password string `json:"password" doc:"Account password."`
	}
}

type RegisterInput struct {
	Body struct {
		Username  string `json:"username" doc:"Account username."`
		FullName  string `json:"fullName" doc:"Client full name."`
		Email     string `json:"email" doc:"Client email address."`
		Password  string `json:"password" doc:"Account password (minimum 6 characters)."`
		BirthDate string `json:"birthDate,omitempty" doc:"Birth date (DD/MM/YYYY)."`
		Phone     string `json:"phone,omitempty" doc:"Contact phone number."`
	}
}

func (in *RegisterInput) ToCommand() identityapp.RegisterCommand {
	return identityapp.RegisterCommand{
		Username:  in.Body.Username,
		FullName:  in.Body.FullName,
		Email:     in.Body.Email,
		Password:  in.Body.Password,
		BirthDate: in.Body.BirthDate,
		Phone:     in.Body.Phone,
	}
}

type OTPRequestInput struct {
	Body struct {
		Email string `json:"email" doc:"Account email address to receive the verification code."`
	}
}

type OTPRequestOutput struct {
	Body identityapp.OTPRequestResult
}

type OTPVerifyInput struct {
	Body struct {
		Email string `json:"email" doc:"Account email address."`
		Code  string `json:"code" doc:"Six-digit verification code."`
	}
}

type OTPVerifyOutput struct {
	Body identityapp.OTPVerifyResult
}

type ResetPasswordInput struct {
	Body struct {
		Identifier  string `json:"identifier,omitempty" doc:"Account email address or username."`
		Email       string `json:"email,omitempty" doc:"Account email address."`
		ResetToken  string `json:"resetToken,omitempty" doc:"Verified single-use reset token."`
		OTPCode     string `json:"otpCode,omitempty" doc:"Six-digit verification code if not pre-exchanged."`
		NewPassword string `json:"newPassword" doc:"New password (minimum 6 characters)."`
	}
}

func (in *ResetPasswordInput) ToCommand() identityapp.ResetPasswordCommand {
	return identityapp.ResetPasswordCommand{
		Identifier:  in.Body.Identifier,
		Email:       in.Body.Email,
		ResetToken:  in.Body.ResetToken,
		OTPCode:     in.Body.OTPCode,
		NewPassword: in.Body.NewPassword,
	}
}

type OAuthLoginInput struct {
	Body struct {
		Provider string `json:"provider" doc:"OAuth identity provider (google or apple)."`
		IDToken  string `json:"idToken" doc:"OIDC identity token issued by the provider."`
	}
}

type OAuthStartInput struct {
	Provider      string `path:"provider" doc:"OAuth identity provider (google or apple)."`
	RedirectURI   string `query:"redirectUri" doc:"Callback URI registered for the OAuth client."`
	CodeChallenge string `query:"codeChallenge" doc:"PKCE S256 code challenge."`
	State         string `query:"state" doc:"Optional opaque CSRF state token."`
}

type OAuthStartOutput struct {
	Body identityapp.OAuthStartResult
}

type OAuthExchangeInput struct {
	Body struct {
		Provider     string `json:"provider" doc:"OAuth identity provider (google or apple)."`
		Code         string `json:"code" doc:"Authorization code returned to the redirect URI."`
		CodeVerifier string `json:"codeVerifier" doc:"PKCE code verifier matching the authorization challenge."`
		RedirectURI  string `json:"redirectUri" doc:"Callback URI used during authorization start."`
		State        string `json:"state" doc:"Anti-CSRF OAuth state issued by /v1/auth/oauth/{provider}/start."`
	}
}

type PushTokenInput struct {
	Body struct {
		Platform   string `json:"platform" doc:"Client platform (android, ios, web, desktop)."`
		Token      string `json:"token" doc:"Device or browser push registration token."`
		AppVariant string `json:"appVariant,omitempty" doc:"Application variant (personal or admin)."`
	}
}

type PushTokenOutput struct {
	Body struct {
		Registered bool                   `json:"registered"`
		Device     domain.DevicePushToken `json:"device"`
	}
}

type DeletePushTokenInput struct {
	Token string `query:"token" doc:"Optional push token to disable; disables all tokens for user if omitted."`
}

type DeletePushTokenOutput struct {
	Body struct {
		Disabled bool `json:"disabled"`
	}
}

type LogoutOutput struct {
	Body struct {
		LoggedOut bool `json:"loggedOut" doc:"Indicates whether the session was terminated."`
	}
}

type UpdateProfileInput struct {
	Body struct {
		FullName  *string `json:"fullName,omitempty" doc:"Updated full name."`
		Email     *string `json:"email,omitempty" doc:"Updated email address."`
		Phone     *string `json:"phone,omitempty" doc:"Updated phone number."`
		BirthDate *string `json:"birthDate,omitempty" doc:"Updated birth date."`
		Username  *string `json:"username,omitempty" doc:"Updated username."`
		AvatarKey *string `json:"avatarKey,omitempty" doc:"Updated avatar identifier."`
	}
}

func (in *UpdateProfileInput) ToCommand() identityapp.UpdateProfileCommand {
	return identityapp.UpdateProfileCommand{
		FullName:  in.Body.FullName,
		Email:     in.Body.Email,
		Phone:     in.Body.Phone,
		BirthDate: in.Body.BirthDate,
		Username:  in.Body.Username,
		AvatarKey: in.Body.AvatarKey,
	}
}

type UpdatePreferencesInput struct {
	Body struct {
		PushNotificationsEnabled *bool   `json:"pushNotificationsEnabled,omitempty"`
		WhatsAppAlertsEnabled    *bool   `json:"whatsappAlertsEnabled,omitempty"`
		EmailReceiptsEnabled     *bool   `json:"emailReceiptsEnabled,omitempty"`
		BiometricLockEnabled     *bool   `json:"biometricLockEnabled,omitempty"`
		AutoSyncWifiOnly         *bool   `json:"autoSyncWifiOnly,omitempty"`
		EventRemindersEnabled    *bool   `json:"eventRemindersEnabled,omitempty"`
		PreferredLanguage        *string `json:"preferredLanguage,omitempty"`
		DarkThemeEnabled         *bool   `json:"darkThemeEnabled,omitempty"`
		SavedPaymentMethod       *string `json:"savedPaymentMethod,omitempty"`
		ProfileZone              *string `json:"profileZone,omitempty"`
	}
}

func (in *UpdatePreferencesInput) ToCommand() identityapp.UpdatePreferencesCommand {
	return identityapp.UpdatePreferencesCommand{
		PushNotificationsEnabled: in.Body.PushNotificationsEnabled,
		WhatsAppAlertsEnabled:    in.Body.WhatsAppAlertsEnabled,
		EmailReceiptsEnabled:     in.Body.EmailReceiptsEnabled,
		BiometricLockEnabled:     in.Body.BiometricLockEnabled,
		AutoSyncWifiOnly:         in.Body.AutoSyncWifiOnly,
		EventRemindersEnabled:    in.Body.EventRemindersEnabled,
		PreferredLanguage:        in.Body.PreferredLanguage,
		DarkThemeEnabled:         in.Body.DarkThemeEnabled,
		SavedPaymentMethod:       in.Body.SavedPaymentMethod,
		ProfileZone:              in.Body.ProfileZone,
	}
}

type FavoriteArticleInput struct {
	ArticleID string `path:"articleId" doc:"Catalog article identifier."`
}

type FavoritesOutput struct {
	Body struct {
		Favorites []string `json:"favorites"`
	}
}

type DeleteStaffInput struct {
	ID string `path:"id" doc:"Staff member identifier."`
}

type DeleteStaffOutput struct {
	Body struct {
		Deleted bool `json:"deleted"`
	}
}

type RecoverUsernameInput struct {
	Body struct {
		Identifier string `json:"identifier" minLength:"3" doc:"Registered email, phone, or full name."`
	}
}

type RecoverUsernameOutput struct {
	Body struct {
		Sent    bool   `json:"sent"`
		Message string `json:"message"`
	}
}

type OAuthConfigOutput struct {
	Body identityapp.OAuthConfigResult
}

type ChangePasswordInput struct {
	Body struct {
		CurrentPassword string `json:"currentPassword" doc:"Current account password."`
		NewPassword     string `json:"newPassword" doc:"New account password (minimum 6 characters)."`
	}
}

type PasswordChangedOutput struct {
	Body struct {
		Updated bool `json:"updated" doc:"Indicates whether the password was updated."`
	}
}

type LoginOutput struct {
	Body struct {
		AccessToken  string      `json:"accessToken" doc:"Bearer token for authenticated requests."`
		RefreshToken string      `json:"refreshToken,omitempty" doc:"Rotating refresh token used to obtain a fresh access token."`
		ExpiresIn    int         `json:"expiresIn,omitempty" doc:"Access token lifetime in seconds."`
		User         domain.User `json:"user" doc:"Authenticated user and active view."`
	}
}

type RefreshTokenInput struct {
	Body struct {
		RefreshToken string `json:"refreshToken" doc:"Rotating refresh token obtained at sign-in."`
	}
}

type LogoutInput struct {
	RefreshToken string `header:"X-Refresh-Token" doc:"Optional refresh token family to revoke on logout."`
}

func newLoginOutputFromSession(session identityapp.AuthSession) *LoginOutput {
	out := &LoginOutput{}
	out.Body.AccessToken = session.AccessToken
	out.Body.RefreshToken = session.RefreshToken
	out.Body.ExpiresIn = session.ExpiresIn
	out.Body.User = session.User
	return out
}

type RegisteredUsersOutput struct {
	Body struct {
		Users []domain.User `json:"users" doc:"Registered accounts."`
	}
}

type SwitchViewInput struct {
	Body struct {
		View string `json:"view" doc:"Target view: personal or admin."`
	}
}

type SwitchViewOutput struct {
	Body struct {
		User domain.User `json:"user" doc:"User with active view."`
	}
}

func newUserOutput(user domain.User) *SwitchViewOutput {
	out := &SwitchViewOutput{}
	out.Body.User = user
	return out
}

type ClientsOutput struct {
	Body struct {
		Clients []identityapp.ClientSummary `json:"clients" doc:"Registered clients list."`
	}
}

type ClientOutput struct {
	Body struct {
		Client identityapp.ClientSummary `json:"client" doc:"Updated client summary."`
	}
}

type UpdateClientInput struct {
	ID   string `path:"id" doc:"Client identifier."`
	Body struct {
		FullName *string `json:"fullName,omitempty" doc:"Client full name."`
		Email    *string `json:"email,omitempty" doc:"Client email address."`
		Phone    *string `json:"phone,omitempty" doc:"Client phone number."`
		Notes    *string `json:"notes,omitempty" doc:"Administrative client notes."`
	}
}

func (in *UpdateClientInput) ToCommand() identityapp.UpdateClientCommand {
	return identityapp.UpdateClientCommand{
		FullName: in.Body.FullName,
		Email:    in.Body.Email,
		Phone:    in.Body.Phone,
		Notes:    in.Body.Notes,
	}
}

type StaffOutput struct {
	Body struct {
		Staff []identityapp.StaffMember `json:"staff" doc:"Operational staff members and permissions."`
	}
}

type StaffMemberOutput struct {
	Body struct {
		Member identityapp.StaffMember `json:"member" doc:"Staff member record."`
	}
}

// InviteStaffInput representa la invitación de un miembro de equipo administrativo.
type InviteStaffInput struct {
	Body struct {
		FullName        string   `json:"fullName" doc:"Staff member full name."`
		Email           string   `json:"email" doc:"Staff member email address."`
		Phone           string   `json:"phone,omitempty" doc:"Staff member phone number."`
		RoleLabel       string   `json:"roleLabel,omitempty" doc:"Operational role title."`
		Role            string   `json:"role,omitempty" doc:"Technical role (staff or admin)."`
		Permissions     []string `json:"permissions" doc:"Assigned permissions."`
		Password        string   `json:"password,omitempty" doc:"Optional initial password."`
		InitialPassword string   `json:"initialPassword,omitempty" doc:"Optional initial password for immediate login."`
	}
}

// ToCommand convierte InviteStaffInput al comando InviteStaffCommand.
func (in *InviteStaffInput) ToCommand() identityapp.InviteStaffCommand {
	return identityapp.InviteStaffCommand{
		FullName:        in.Body.FullName,
		Email:           in.Body.Email,
		Phone:           in.Body.Phone,
		RoleLabel:       in.Body.RoleLabel,
		Role:            in.Body.Role,
		Permissions:     in.Body.Permissions,
		Password:        in.Body.Password,
		InitialPassword: in.Body.InitialPassword,
	}
}

type UpdateStaffInput struct {
	ID   string `path:"id" doc:"Staff member identifier."`
	Body struct {
		FullName    *string   `json:"fullName,omitempty" doc:"Updated staff member full name."`
		RoleLabel   *string   `json:"roleLabel,omitempty" doc:"Updated role title."`
		Role        *string   `json:"role,omitempty" doc:"Updated technical role."`
		Permissions *[]string `json:"permissions,omitempty" doc:"Updated operational permissions."`
		Active      *bool     `json:"active,omitempty" doc:"Updated active flag."`
	}
}

func (in *UpdateStaffInput) ToCommand() identityapp.UpdateStaffCommand {
	return identityapp.UpdateStaffCommand{
		FullName:    in.Body.FullName,
		RoleLabel:   in.Body.RoleLabel,
		Role:        in.Body.Role,
		Permissions: in.Body.Permissions,
		Active:      in.Body.Active,
	}
}

type DeleteAccountOutput struct {
	Body struct {
		Deleted bool `json:"deleted" doc:"Indicates whether the account was deleted."`
	}
}

func mapRegistrationError(err error, email string, endpoint string) *platformhttp.APIError {
	if errors.Is(err, domain.ErrEmailAlreadyRegistered) {
		return platformhttp.NewAPIError(
			http.StatusConflict,
			"AUTH_EMAIL_TAKEN",
			"email already registered",
			endpoint,
			"UniqueEmailConstraint",
			"email already registered: "+email,
		)
	}
	if errors.Is(err, domain.ErrUsernameAlreadyTaken) {
		return platformhttp.NewAPIError(
			http.StatusConflict,
			"AUTH_USERNAME_TAKEN",
			"username already taken",
			endpoint,
			"UniqueUsernameConstraint",
			err.Error(),
		)
	}
	return platformhttp.NewAPIError(
		http.StatusBadRequest,
		"AUTH_INVALID_REGISTRATION",
		err.Error(),
		endpoint,
		"ValidateRegistrationInput",
		err.Error(),
	)
}

func mapOTPRequestError(err error) *platformhttp.APIError {
	if errors.Is(err, domain.ErrSMTPNotConfigured) {
		return platformhttp.NewAPIError(
			http.StatusServiceUnavailable,
			"SMTP_PROVIDER_NOT_CONFIGURED",
			err.Error(),
			"POST /v1/auth/otp/request",
			"RequireConfiguredSMTPProvider",
			err.Error(),
		)
	}
	status := http.StatusBadRequest
	if errors.Is(err, domain.ErrUserNotFound) {
		status = http.StatusNotFound
	}
	return platformhttp.NewAPIError(
		status,
		"AUTH_OTP_REQUEST_FAILED",
		err.Error(),
		"POST /v1/auth/otp/request",
		"GenerateCryptographicOTP",
		err.Error(),
	)
}

func mapChangePasswordError(err error) *platformhttp.APIError {
	status := http.StatusBadRequest
	code := "AUTH_INVALID_PASSWORD"
	if errors.Is(err, domain.ErrInvalidCredentials) {
		status = http.StatusUnauthorized
		code = "AUTH_INVALID_CURRENT_PASSWORD"
	}
	return platformhttp.NewAPIError(
		status,
		code,
		err.Error(),
		"POST /v1/auth/change-password",
		"VerifyAndHashPassword",
		err.Error(),
	)
}

func mapPasswordResetError(err error) *platformhttp.APIError {
	if errors.Is(err, domain.ErrResetTokenInvalid) {
		return platformhttp.NewAPIError(
			http.StatusUnauthorized,
			"AUTH_RESET_TOKEN_INVALID",
			err.Error(),
			"POST /v1/auth/reset-password",
			"RequireVerifiedOTPResetToken",
			err.Error(),
		)
	}
	if errors.Is(err, domain.ErrOTPInvalidOrExpired) {
		return platformhttp.NewAPIError(
			http.StatusUnauthorized,
			"AUTH_OTP_INVALID",
			err.Error(),
			"POST /v1/auth/reset-password",
			"VerifyOTPCode",
			err.Error(),
		)
	}
	return platformhttp.NewAPIError(
		http.StatusBadRequest,
		"AUTH_PASSWORD_RESET_FAILED",
		err.Error(),
		"POST /v1/auth/reset-password",
		"ResetUserPasswordBcrypt",
		err.Error(),
	)
}

func mapOAuthError(err error) *platformhttp.APIError {
	if errors.Is(err, domain.ErrOAuthProviderNotConfigured) {
		return platformhttp.NewAPIError(
			http.StatusServiceUnavailable,
			"OAUTH_PROVIDER_NOT_CONFIGURED",
			err.Error(),
			"POST /v1/auth/oauth",
			"RequireConfiguredOAuthProvider",
			err.Error(),
		)
	}
	if errors.Is(err, domain.ErrOAuthInvalidToken) {
		return platformhttp.NewAPIError(
			http.StatusUnauthorized,
			"OAUTH_INVALID_TOKEN",
			err.Error(),
			"POST /v1/auth/oauth",
			"VerifyOIDCToken",
			err.Error(),
		)
	}
	return platformhttp.NewAPIError(
		http.StatusBadRequest,
		"OAUTH_INVALID_REQUEST",
		err.Error(),
		"POST /v1/auth/oauth",
		"ValidateOAuthInput",
		err.Error(),
	)
}

func mapDeleteAccountError(err error) *platformhttp.APIError {
	if errors.Is(err, domain.ErrActiveEventsPreventDeletion) {
		return platformhttp.NewAPIError(
			http.StatusConflict,
			"ACCOUNT_HAS_ACTIVE_EVENTS",
			"cannot delete account while active events exist",
			"DELETE /v1/account",
			"NoActiveEventsOnDelete",
			err.Error(),
		)
	}
	return platformhttp.NewAPIError(
		http.StatusBadRequest,
		"ACCOUNT_DELETE_INVALID",
		err.Error(),
		"DELETE /v1/account",
		"ValidateAccountDeletion",
		err.Error(),
	)
}

func mapAdminMutationError(err error, endpoint string) *platformhttp.APIError {
	if errors.Is(err, domain.ErrUserNotFound) {
		return platformhttp.NewAPIError(
			http.StatusNotFound,
			"IDENTITY_RECORD_NOT_FOUND",
			err.Error(),
			endpoint,
			"LookupIdentityRecord",
			err.Error(),
		)
	}
	if errors.Is(err, domain.ErrEmailAlreadyRegistered) {
		return platformhttp.NewAPIError(
			http.StatusConflict,
			"AUTH_EMAIL_TAKEN",
			err.Error(),
			endpoint,
			"UniqueEmailConstraint",
			err.Error(),
		)
	}
	if errors.Is(err, domain.ErrCannotDeleteLastAdmin) {
		return platformhttp.NewAPIError(
			http.StatusConflict,
			"ADMIN_PROTECTION_VIOLATION",
			err.Error(),
			endpoint,
			"ProtectOwnerAndLastAdmin",
			err.Error(),
		)
	}
	return platformhttp.NewAPIError(
		http.StatusBadRequest,
		"IDENTITY_INVALID_MUTATION",
		err.Error(),
		endpoint,
		"ValidateIdentityMutation",
		err.Error(),
	)
}
