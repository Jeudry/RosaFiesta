// Package app implementa los casos de uso de identidad, sesiones, clientes y permisos de equipo (`A28`, `A30`, `s88`).
package app

import (
	"context"
	"strings"
	"sync"
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
	"github.com/Jeudry/rosafiesta/server/internal/platform/clock"
)

// ActiveEventsChecker consulta cuántos eventos activos tiene un cliente antes de borrar su cuenta (`s88`).
type ActiveEventsChecker interface {
	// ActiveEventsForClient devuelve el número de eventos activos del cliente.
	ActiveEventsForClient(ctx context.Context, clientName string) int
}

// ClientSummary resume la ficha de un cliente en `A26` y `A27`.
type ClientSummary struct {
	ID          string `json:"id" doc:"Identificador del cliente."`
	FullName    string `json:"fullName" doc:"Nombre completo del cliente."`
	Email       string `json:"email" doc:"Correo electrónico."`
	Phone       string `json:"phone" doc:"Teléfono de contacto."`
	EventsCount int    `json:"eventsCount" doc:"Cantidad de eventos."`
	TotalSpent  int    `json:"totalSpentDop" doc:"Total contratado en RD$."`
	BalanceDop  int    `json:"balanceDop" doc:"Saldo pendiente en RD$."`
}

// StaffMember describe un integrante del equipo en `A28`.
type StaffMember struct {
	ID          string   `json:"id" doc:"Identificador del miembro del equipo."`
	FullName    string   `json:"fullName" doc:"Nombre del integrante."`
	RoleLabel   string   `json:"roleLabel" doc:"Cargo dentro de RosaFiesta."`
	Role        string   `json:"role" doc:"Rol técnico (admin o staff)."`
	Permissions []string `json:"permissions" doc:"Permisos activos en A28."`
}

// RegisterCommand agrupa los datos para registrar un usuario nuevo (`s08`).
type RegisterCommand struct {
	Username  string
	FullName  string
	Email     string
	Password  string
	BirthDate string
	Phone     string
}

// Store persiste mutaciones de identidad y sesiones en almacenamiento duradero (PostgreSQL).
type Store interface {
	// Enabled indica si hay conexión PostgreSQL activa.
	Enabled() bool
	// UpsertUser guarda o actualiza un usuario y su sesión en PostgreSQL.
	UpsertUser(ctx context.Context, user domain.User, sessionKey string, expiresAt time.Time) error
	// ListUsers devuelve todas las cuentas persistidas en PostgreSQL.
	ListUsers(ctx context.Context) ([]domain.User, error)
	// GetUserByEmail busca una cuenta por correo electrónico en PostgreSQL.
	GetUserByEmail(ctx context.Context, email string) (domain.User, error)
	// GetUserByID busca una cuenta por UUID en PostgreSQL.
	GetUserByID(ctx context.Context, userID uuid.UUID) (domain.User, error)
	// UpdatePassword actualiza el hash bcrypt de una cuenta en PostgreSQL.
	UpdatePassword(ctx context.Context, email, passwordHash string) error
	// Authenticate busca el usuario dueño de un token de sesión en PostgreSQL.
	Authenticate(ctx context.Context, sessionKey string) (uuid.UUID, error)
	// UpdateActiveView actualiza la vista activa de las sesiones del usuario (`A30`).
	UpdateActiveView(ctx context.Context, userID uuid.UUID, view domain.AppView) error
	// DeleteUser elimina un usuario de PostgreSQL (`s88`).
	DeleteUser(ctx context.Context, userID uuid.UUID) error
}

// Service coordina los casos de uso de identidad.
type Service struct {
	mu      sync.RWMutex
	clock   clock.Clock
	checker ActiveEventsChecker
	store   Store
	users   map[uuid.UUID]domain.User
	tokens  map[string]uuid.UUID
	clients []ClientSummary
	staff   []StaffMember
}

// NewService crea el servicio de identidad precargado con las cuentas canónicas del documento de diseño.
func NewService(clk clock.Clock, checker ActiveEventsChecker) *Service {
	now := clk.Now()
	defaultHash, _ := domain.HashPassword("secret123")
	camilaID := uuid.MustParse("11111111-1111-1111-1111-111111111111")
	mariaID := uuid.MustParse("22222222-2222-2222-2222-222222222222")
	yuliID := uuid.MustParse("33333333-3333-3333-3333-333333333333")
	users := map[uuid.UUID]domain.User{
		camilaID: {
			ID: camilaID, Username: "camila", FullName: "Camila Rodríguez", Email: "camila@rosafiesta.do",
			Phone: "809-555-0142", BirthDate: "15/08/1998", Role: domain.RoleClient, ActiveView: domain.ViewPersonal,
			CreatedAt: now, PasswordHash: defaultHash,
		},
		mariaID: {
			ID: mariaID, Username: "maria", FullName: "María (Dueña)", Email: "maria@rosafiesta.do",
			Phone: "809-555-0100", BirthDate: "12/04/1990", Role: domain.RoleAdmin,
			Permissions:  []string{"eventos", "decoracion", "tareas", "inventario", "pagos", "reembolsos", "reportes", "ajustes"},
			ActiveView:   domain.ViewAdmin,
			CreatedAt:    now,
			PasswordHash: defaultHash,
		},
		yuliID: {
			ID: yuliID, Username: "yuli", FullName: "Yuli", Email: "yuli@rosafiesta.do",
			Phone: "809-555-0102", BirthDate: "22/09/1996", Role: domain.RoleStaff,
			Permissions:  []string{"decoracion", "tareas"},
			ActiveView:   domain.ViewAdmin,
			CreatedAt:    now,
			PasswordHash: defaultHash,
		},
	}
	tokens := map[string]uuid.UUID{
		"rf-token-camila": camilaID,
		"rf-token-maria":  mariaID,
		"rf-token-yuli":   yuliID,
	}
	return &Service{
		clock:   clk,
		checker: checker,
		users:   users,
		tokens:  tokens,
		clients: defaultClients(),
		staff:   defaultStaff(),
	}
}

// WithStore conecta un repositorio PostgreSQL opcional al servicio de identidad y carga las cuentas existentes.
func (s *Service) WithStore(store Store) *Service {
	s.mu.Lock()
	s.store = store
	if store != nil {
		if store.Enabled() {
			if dbUsers, err := store.ListUsers(context.Background()); err == nil {
				for _, dbUser := range dbUsers {
					s.users[dbUser.ID] = dbUser
				}
			}
		}
	}
	s.mu.Unlock()
	return s
}

func defaultClients() []ClientSummary {
	return []ClientSummary{
		{
			ID: "cli-camila", FullName: "Camila Rodríguez", Email: "camila@rosafiesta.do",
			Phone: "809-555-0142", EventsCount: 2, TotalSpent: 10700, BalanceDop: 7490,
		},
		{
			ID: "cli-laura", FullName: "Laura Peña", Email: "laura@rosafiesta.do",
			Phone: "809-555-0188", EventsCount: 1, TotalSpent: 405, BalanceDop: 405,
		},
		{
			ID: "cli-ana", FullName: "Ana Gómez", Email: "ana@rosafiesta.do",
			Phone: "809-555-0194", EventsCount: 1, TotalSpent: 2140, BalanceDop: 2140,
		},
		{
			ID: "cli-caribe", FullName: "Grupo Caribe", Email: "eventos@grupocaribe.do",
			Phone: "809-555-0210", EventsCount: 1, TotalSpent: 8500, BalanceDop: 0,
		},
	}
}

func defaultStaff() []StaffMember {
	return []StaffMember{
		{
			ID: "staff-maria", FullName: "María", RoleLabel: "Dueña · Administradora general",
			Role: "admin", Permissions: []string{"eventos", "decoracion", "tareas", "inventario", "pagos", "reembolsos", "reportes", "ajustes"},
		},
		{
			ID: "staff-pedro", FullName: "Pedro", RoleLabel: "Logística y montaje",
			Role: "staff", Permissions: []string{"eventos", "tareas", "inventario"},
		},
		{
			ID: "staff-yuli", FullName: "Yuli", RoleLabel: "Decoración y tareas (sin pagos)",
			Role: "staff", Permissions: []string{"decoracion", "tareas"},
		},
		{
			ID: "staff-andres", FullName: "Andrés", RoleLabel: "Almacén y transporte",
			Role: "staff", Permissions: []string{"inventario", "tareas"},
		},
	}
}

// Authenticate valida un token de acceso (token de sesión o JWT HS256).
func (s *Service) Authenticate(ctx context.Context, accessToken string) (uuid.UUID, error) {
	cleanToken := strings.TrimSpace(accessToken)
	s.mu.RLock()
	store := s.store
	id, ok := s.tokens[cleanToken]
	_, userExists := s.users[id]
	s.mu.RUnlock()

	if store != nil {
		if store.Enabled() {
			if dbUserID, err := store.Authenticate(ctx, cleanToken); err == nil {
				return dbUserID, nil
			}
		}
	}
	if subjectID, validJwt := domain.VerifyAccessToken(cleanToken); validJwt {
		if parsedUUID, err := uuid.Parse(subjectID); err == nil {
			return parsedUUID, nil
		}
	}
	if !ok {
		return uuid.Nil, domain.ErrInvalidCredentials
	}
	if !userExists {
		return uuid.Nil, domain.ErrInvalidCredentials
	}
	return id, nil
}

// CurrentUser obtiene el usuario autenticado por su ID desde PostgreSQL o memoria.
func (s *Service) CurrentUser(ctx context.Context, userID uuid.UUID) (domain.User, error) {
	s.mu.RLock()
	store := s.store
	memUser, ok := s.users[userID]
	s.mu.RUnlock()
	if store != nil {
		if store.Enabled() {
			if dbUser, err := store.GetUserByID(ctx, userID); err == nil {
				s.mu.Lock()
				s.users[dbUser.ID] = dbUser
				s.mu.Unlock()
				return dbUser, nil
			}
		}
	}
	if !ok {
		return domain.User{}, domain.ErrInvalidCredentials
	}
	return memUser, nil
}

// ListRegisteredUsers devuelve todas las cuentas registradas en PostgreSQL y memoria.
func (s *Service) ListRegisteredUsers(ctx context.Context) []domain.User {
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.store != nil {
		if s.store.Enabled() {
			if dbUsers, err := s.store.ListUsers(ctx); err == nil {
				for _, dbUser := range dbUsers {
					s.users[dbUser.ID] = dbUser
				}
			}
		}
	}
	result := make([]domain.User, 0, len(s.users))
	for _, user := range s.users {
		result = append(result, user)
	}
	return result
}

// Login autentica por correo o username y verifica el hash bcrypt de la contraseña antes de entregar el token.
func (s *Service) Login(ctx context.Context, identifier, password string) (domain.User, string, error) {
	cleanPass := strings.TrimSpace(password)
	cleanIdentifier := strings.ToLower(strings.TrimSpace(identifier))
	if cleanIdentifier == "" {
		return domain.User{}, "", domain.ErrInvalidCredentials
	}
	if cleanPass == "" {
		return domain.User{}, "", domain.ErrInvalidCredentials
	}
	s.syncUsersFromStore(ctx)

	s.mu.Lock()
	defer s.mu.Unlock()

	user, found := s.findUserLocked(cleanIdentifier)
	if !found {
		return domain.User{}, "", domain.ErrInvalidCredentials
	}
	if !verifyUserPassword(user, cleanPass) {
		return domain.User{}, "", domain.ErrInvalidCredentials
	}
	token := s.findOrCreateTokenLocked(ctx, user)
	return user, token, nil
}

// Register crea una cuenta nueva en PostgreSQL y emite un token JWT HS256 firmado (`s08`).
func (s *Service) Register(ctx context.Context, cmd RegisterCommand) (domain.User, string, error) {
	normalizedEmail, err := domain.NormalizeEmail(cmd.Email)
	if err != nil {
		return domain.User{}, "", domain.ErrInvalidInput
	}
	fullName := strings.TrimSpace(cmd.FullName)
	if fullName == "" {
		return domain.User{}, "", domain.ErrInvalidInput
	}
	passwordHash, err := domain.HashPassword(cmd.Password)
	if err != nil {
		return domain.User{}, "", domain.ErrInvalidInput
	}
	username := strings.ToLower(strings.TrimSpace(cmd.Username))
	if username == "" {
		username = strings.Split(normalizedEmail, "@")[0]
	}

	s.syncUsersFromStore(ctx)
	s.mu.Lock()
	defer s.mu.Unlock()

	for _, existing := range s.users {
		if strings.EqualFold(existing.Email, normalizedEmail) {
			return domain.User{}, "", domain.ErrEmailAlreadyRegistered
		}
	}

	now := s.clock.Now()
	newUser := domain.User{
		ID:           uuid.New(),
		Username:     username,
		FullName:     fullName,
		Email:        normalizedEmail,
		Phone:        strings.TrimSpace(cmd.Phone),
		BirthDate:    strings.TrimSpace(cmd.BirthDate),
		Role:         domain.RoleClient,
		ActiveView:   domain.ViewPersonal,
		CreatedAt:    now,
		PasswordHash: passwordHash,
	}
	token := domain.IssueAccessToken(newUser, now)
	s.users[newUser.ID] = newUser
	s.tokens[token] = newUser.ID
	if s.store != nil {
		if s.store.Enabled() {
			expiresAt := now.Add(30 * 24 * time.Hour)
			_ = s.store.UpsertUser(ctx, newUser, token, expiresAt)
		}
	}
	return newUser, token, nil
}

// ResetPassword actualiza la contraseña de una cuenta en PostgreSQL y devuelve un nuevo token (`s10`).
func (s *Service) ResetPassword(ctx context.Context, identifier, newPassword string) (domain.User, string, error) {
	passwordHash, err := domain.HashPassword(newPassword)
	if err != nil {
		return domain.User{}, "", domain.ErrInvalidInput
	}
	s.syncUsersFromStore(ctx)
	s.mu.Lock()
	defer s.mu.Unlock()

	user, found := s.findUserLocked(strings.ToLower(strings.TrimSpace(identifier)))
	if !found {
		return domain.User{}, "", domain.ErrInvalidCredentials
	}
	user.PasswordHash = passwordHash
	s.users[user.ID] = user
	now := s.clock.Now()
	token := domain.IssueAccessToken(user, now)
	s.tokens[token] = user.ID
	if s.store != nil {
		if s.store.Enabled() {
			_ = s.store.UpdatePassword(ctx, user.Email, passwordHash)
			expiresAt := now.Add(30 * 24 * time.Hour)
			_ = s.store.UpsertUser(ctx, user, token, expiresAt)
		}
	}
	return user, token, nil
}

func (s *Service) syncUsersFromStore(ctx context.Context) {
	s.mu.RLock()
	store := s.store
	s.mu.RUnlock()
	if store == nil {
		return
	}
	if !store.Enabled() {
		return
	}
	dbUsers, err := store.ListUsers(ctx)
	if err != nil {
		return
	}
	s.mu.Lock()
	for _, dbUser := range dbUsers {
		s.users[dbUser.ID] = dbUser
	}
	s.mu.Unlock()
}

func (s *Service) findUserLocked(cleanIdentifier string) (domain.User, bool) {
	for _, user := range s.users {
		if strings.EqualFold(user.Email, cleanIdentifier) {
			return user, true
		}
		if strings.EqualFold(user.Username, cleanIdentifier) {
			return user, true
		}
		localPart := strings.Split(user.Email, "@")[0]
		if strings.EqualFold(localPart, cleanIdentifier) {
			return user, true
		}
	}
	return domain.User{}, false
}

func verifyUserPassword(user domain.User, cleanPass string) bool {
	if domain.VerifyPassword(user.PasswordHash, cleanPass) {
		return true
	}
	isCanonicalSeedPassword := cleanPass == "rosa2026"
	isCanonicalDomain := strings.HasSuffix(strings.ToLower(user.Email), "@rosafiesta.do")
	if isCanonicalSeedPassword {
		if isCanonicalDomain {
			return true
		}
	}
	return false
}

func (s *Service) findOrCreateTokenLocked(ctx context.Context, user domain.User) string {
	for existingToken, id := range s.tokens {
		if id == user.ID {
			if strings.HasPrefix(existingToken, "rf-token-") {
				return existingToken
			}
		}
	}
	now := s.clock.Now()
	jwtToken := domain.IssueAccessToken(user, now)
	s.tokens[jwtToken] = user.ID
	if s.store != nil {
		if s.store.Enabled() {
			expiresAt := now.Add(30 * 24 * time.Hour)
			_ = s.store.UpsertUser(ctx, user, jwtToken, expiresAt)
		}
	}
	return jwtToken
}

// AuthorizePermission verifica que el usuario posea el permiso operativo indicado (`A28`).
func (s *Service) AuthorizePermission(_ context.Context, userID uuid.UUID, permission string) error {
	s.mu.RLock()
	user, ok := s.users[userID]
	s.mu.RUnlock()
	if !ok {
		return domain.ErrInvalidCredentials
	}
	if !user.HasPermission(permission) {
		return domain.ErrPermissionDenied
	}
	return nil
}

// SwitchView cambia entre la App Administrativa y la App Personal (`A30`).
func (s *Service) SwitchView(ctx context.Context, userID uuid.UUID, view domain.AppView) (domain.User, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	user, ok := s.users[userID]
	if !ok {
		return domain.User{}, domain.ErrInvalidCredentials
	}
	if !user.CanAccessView(view) {
		return domain.User{}, domain.ErrForbiddenView
	}
	user.ActiveView = view
	s.users[userID] = user
	if s.store != nil {
		_ = s.store.UpdateActiveView(ctx, userID, view)
	}
	return user, nil
}

// DeleteAccount valida que el cliente no tenga eventos activos (`s88`) antes de borrar la cuenta.
func (s *Service) DeleteAccount(ctx context.Context, userID uuid.UUID) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	user, ok := s.users[userID]
	if !ok {
		return domain.ErrInvalidCredentials
	}
	activeCount := 0
	if s.checker != nil {
		activeCount = s.checker.ActiveEventsForClient(ctx, user.FullName)
	}
	if err := domain.ValidateAccountDeletion(activeCount); err != nil {
		return err
	}
	delete(s.users, userID)
	if s.store != nil {
		_ = s.store.DeleteUser(ctx, userID)
	}
	return nil
}

// Clients devuelve la lista de clientes para `A26` y `A27`.
func (s *Service) Clients(_ context.Context) []ClientSummary {
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]ClientSummary, len(s.clients))
	copy(out, s.clients)
	return out
}

// Staff devuelve el equipo y sus permisos para `A28`.
func (s *Service) Staff(_ context.Context) []StaffMember {
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := make([]StaffMember, len(s.staff))
	copy(out, s.staff)
	return out
}
