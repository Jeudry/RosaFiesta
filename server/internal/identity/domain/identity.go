// Package domain define las reglas puras de identidad, roles, permisos de equipo y eliminación de cuenta.
package domain

import (
	"errors"
	"strings"
	"time"

	"github.com/google/uuid"
)

// Role representa el rol de un usuario dentro de RosaFiesta.
type Role string

const (
	// RoleClient solo ve la App Personal.
	RoleClient Role = "client"
	// RoleAdmin (dueña María) ve toda la App Administrativa y puede entrar como cliente en A30.
	RoleAdmin Role = "admin"
	// RoleStaff (equipo: Pedro, Yuli, Andrés) ve la App Administrativa con los permisos de A28.
	RoleStaff Role = "staff"
)

// AppView representa cuál de las dos aplicaciones está activa en la sesión (`A30`).
type AppView string

const (
	// ViewPersonal es la App Personal (clientes).
	ViewPersonal AppView = "personal"
	// ViewAdmin es la App Administrativa (negocio).
	ViewAdmin AppView = "admin"
)

var (
	// ErrInvalidCredentials indica correo o contraseña incorrectos.
	ErrInvalidCredentials = errors.New("invalid email or password")
	// ErrEmailAlreadyRegistered indica que el correo ya está registrado.
	ErrEmailAlreadyRegistered = errors.New("email already registered")
	// ErrInvalidInput indica datos de entrada vacíos o inválidos.
	ErrInvalidInput = errors.New("invalid identity input")
	// ErrActiveEventsPreventDeletion impide eliminar la cuenta si existen eventos activos (`s88`).
	ErrActiveEventsPreventDeletion = errors.New("cannot delete account with active events")
	// ErrForbiddenView indica que el rol no puede entrar en la vista solicitada (`A30`).
	ErrForbiddenView = errors.New("role is not allowed to enter requested app view")
	// ErrPermissionDenied indica que el miembro del equipo no posee el permiso requerido (`A28`).
	ErrPermissionDenied = errors.New("staff member lacks required permission")
)

// User representa una cuenta registrada en RosaFiesta.
type User struct {
	ID          uuid.UUID `json:"id"`
	FullName    string    `json:"fullName"`
	Email       string    `json:"email"`
	Phone       string    `json:"phone"`
	Role        Role      `json:"role"`
	Permissions []string  `json:"permissions"`
	ActiveView  AppView   `json:"activeView"`
	CreatedAt   time.Time `json:"createdAt"`
}

// CanAccessView valida si el usuario puede abrir la vista indicada (`A30`).
func (u User) CanAccessView(view AppView) bool {
	if view == ViewPersonal {
		return u.Role == RoleClient || u.Role == RoleAdmin
	}
	if view == ViewAdmin {
		return u.Role == RoleAdmin || u.Role == RoleStaff
	}
	return false
}

// HasPermission valida si el usuario posee un permiso operativo (`A28`).
func (u User) HasPermission(permission string) bool {
	if u.Role == RoleAdmin {
		return true
	}
	if u.Role != RoleStaff {
		return false
	}
	target := strings.TrimSpace(strings.ToLower(permission))
	for _, item := range u.Permissions {
		if strings.TrimSpace(strings.ToLower(item)) == target {
			return true
		}
	}
	return false
}

// ValidateAccountDeletion aplica la regla `s88`: no se puede eliminar la cuenta con eventos activos.
func ValidateAccountDeletion(activeEventsCount int) error {
	if activeEventsCount > 0 {
		return ErrActiveEventsPreventDeletion
	}
	return nil
}

// NormalizeEmail limpia y normaliza un correo electrónico.
func NormalizeEmail(raw string) (string, error) {
	cleaned := strings.ToLower(strings.TrimSpace(raw))
	if len(cleaned) < 5 || !strings.Contains(cleaned, "@") || !strings.Contains(cleaned, ".") {
		return "", ErrInvalidInput
	}
	return cleaned, nil
}
