// Package app implementa los casos de uso de identidad, sesiones, clientes y permisos de equipo (`A28`, `A30`, `s88`).
package app

import (
	"context"
	"strings"
	"sync"

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

// Store persiste mutaciones de identidad y sesiones en almacenamiento duradero (PostgreSQL).
type Store interface {
	// Enabled indica si hay conexión PostgreSQL activa.
	Enabled() bool
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
			ID: camilaID, FullName: "Camila Rodríguez", Email: "camila@rosafiesta.do",
			Phone: "809-555-0142", Role: domain.RoleClient, ActiveView: domain.ViewPersonal,
			CreatedAt: now, PasswordHash: defaultHash,
		},
		mariaID: {
			ID: mariaID, FullName: "María (Dueña)", Email: "maria@rosafiesta.do",
			Phone: "809-555-0100", Role: domain.RoleAdmin,
			Permissions:  []string{"eventos", "decoracion", "tareas", "inventario", "pagos", "reembolsos", "reportes", "ajustes"},
			ActiveView:   domain.ViewAdmin,
			CreatedAt:    now,
			PasswordHash: defaultHash,
		},
		yuliID: {
			ID: yuliID, FullName: "Yuli", Email: "yuli@rosafiesta.do",
			Phone: "809-555-0102", Role: domain.RoleStaff,
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

// WithStore conecta un repositorio PostgreSQL opcional al servicio de identidad.
func (s *Service) WithStore(store Store) *Service {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.store = store
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

// Authenticate valida un token de acceso.
func (s *Service) Authenticate(ctx context.Context, accessToken string) (uuid.UUID, error) {
	s.mu.RLock()
	store := s.store
	id, ok := s.tokens[strings.TrimSpace(accessToken)]
	_, userExists := s.users[id]
	s.mu.RUnlock()
	if store != nil && store.Enabled() {
		if dbUserID, err := store.Authenticate(ctx, accessToken); err == nil {
			return dbUserID, nil
		}
	}
	if !ok || !userExists {
		return uuid.Nil, domain.ErrInvalidCredentials
	}
	return id, nil
}

// Login autentica por correo y verifica el hash bcrypt de la contraseña antes de entregar el token.
func (s *Service) Login(_ context.Context, email, password string) (domain.User, string, error) {
	normalized, err := domain.NormalizeEmail(email)
	if err != nil || strings.TrimSpace(password) == "" {
		return domain.User{}, "", domain.ErrInvalidCredentials
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	for token, id := range s.tokens {
		user := s.users[id]
		if user.Email == normalized {
			if !domain.VerifyPassword(user.PasswordHash, password) {
				return domain.User{}, "", domain.ErrInvalidCredentials
			}
			return user, token, nil
		}
	}
	return domain.User{}, "", domain.ErrInvalidCredentials
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
