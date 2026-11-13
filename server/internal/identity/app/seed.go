package app

import (
	"time"

	"github.com/google/uuid"

	"github.com/Jeudry/rosafiesta/server/internal/identity/domain"
)

func buildAdminSeedUsers(now time.Time) map[uuid.UUID]domain.User {
	defaultHash := canonicalSeedPasswordHash()
	mariaID := uuid.MustParse("22222222-2222-2222-2222-222222222222")
	yuliID := uuid.MustParse("33333333-3333-3333-3333-333333333333")
	return map[uuid.UUID]domain.User{
		mariaID: {
			ID:           mariaID,
			Username:     "maria",
			FullName:     "María (Dueña)",
			Email:        "maria@rosafiesta.do",
			Phone:        "809-555-0100",
			BirthDate:    "12/04/1990",
			Role:         domain.RoleAdmin,
			Permissions:  []string{"eventos", "decoracion", "tareas", "inventario", "pagos", "reembolsos", "reportes", "ajustes"},
			ActiveView:   domain.ViewAdmin,
			CreatedAt:    now,
			PasswordHash: defaultHash,
		},
		yuliID: {
			ID:           yuliID,
			Username:     "yuli",
			FullName:     "Yuli",
			Email:        "yuli@rosafiesta.do",
			Phone:        "809-555-0102",
			BirthDate:    "22/09/1996",
			Role:         domain.RoleStaff,
			Permissions:  []string{"decoracion", "tareas"},
			ActiveView:   domain.ViewAdmin,
			CreatedAt:    now,
			PasswordHash: defaultHash,
		},
	}
}

func buildAdminDevStaticSessions(now time.Time) map[string]sessionEntry {
	expiresAt := now.Add(defaultSessionTTL)
	mariaHash := domain.HashToken("rf-token-maria")
	yuliHash := domain.HashToken("rf-token-yuli")
	return map[string]sessionEntry{
		mariaHash: {
			UserID:    uuid.MustParse("22222222-2222-2222-2222-222222222222"),
			ExpiresAt: expiresAt,
		},
		yuliHash: {
			UserID:    uuid.MustParse("33333333-3333-3333-3333-333333333333"),
			ExpiresAt: expiresAt,
		},
	}
}

func DefaultAdminStaff(now time.Time) []domain.StaffRecord {
	return defaultAdminStaff(now)
}

func defaultAdminStaff(now time.Time) []domain.StaffRecord {
	mariaID := uuid.MustParse("22222222-2222-2222-2222-222222222222")
	yuliID := uuid.MustParse("33333333-3333-3333-3333-333333333333")
	return []domain.StaffRecord{
		{
			ID:          "staff-maria",
			UserID:      mariaID,
			FullName:    "María",
			Email:       "maria@rosafiesta.do",
			Phone:       "809-555-0100",
			RoleLabel:   "Dueña · Administradora general",
			Role:        "admin",
			Permissions: []string{"eventos", "decoracion", "tareas", "inventario", "pagos", "reembolsos", "reportes", "ajustes"},
			Active:      true,
			CreatedAt:   now,
		},
		{
			ID:          "staff-yuli",
			UserID:      yuliID,
			FullName:    "Yuli",
			Email:       "yuli@rosafiesta.do",
			Phone:       "809-555-0102",
			RoleLabel:   "Decoración y tareas (sin pagos)",
			Role:        "staff",
			Permissions: []string{"decoracion", "tareas"},
			Active:      true,
			CreatedAt:   now,
		},
	}
}

func buildCanonicalUsers(now time.Time) map[uuid.UUID]domain.User {
	defaultHash := canonicalSeedPasswordHash()
	camilaID := uuid.MustParse("11111111-1111-1111-1111-111111111111")
	mariaID := uuid.MustParse("22222222-2222-2222-2222-222222222222")
	yuliID := uuid.MustParse("33333333-3333-3333-3333-333333333333")
	return map[uuid.UUID]domain.User{
		camilaID: {
			ID:           camilaID,
			Username:     "camila",
			FullName:     "Camila Rodríguez",
			Email:        "camila@rosafiesta.do",
			Phone:        "809-555-0142",
			BirthDate:    "15/08/1998",
			Role:         domain.RoleClient,
			Permissions:  []string{},
			ActiveView:   domain.ViewPersonal,
			CreatedAt:    now,
			PasswordHash: defaultHash,
		},
		mariaID: {
			ID:           mariaID,
			Username:     "maria",
			FullName:     "María (Dueña)",
			Email:        "maria@rosafiesta.do",
			Phone:        "809-555-0100",
			BirthDate:    "12/04/1990",
			Role:         domain.RoleAdmin,
			Permissions:  []string{"eventos", "decoracion", "tareas", "inventario", "pagos", "reembolsos", "reportes", "ajustes"},
			ActiveView:   domain.ViewAdmin,
			CreatedAt:    now,
			PasswordHash: defaultHash,
		},
		yuliID: {
			ID:           yuliID,
			Username:     "yuli",
			FullName:     "Yuli",
			Email:        "yuli@rosafiesta.do",
			Phone:        "809-555-0102",
			BirthDate:    "22/09/1996",
			Role:         domain.RoleStaff,
			Permissions:  []string{"decoracion", "tareas"},
			ActiveView:   domain.ViewAdmin,
			CreatedAt:    now,
			PasswordHash: defaultHash,
		},
	}
}

func buildDevStaticSessions(now time.Time) map[string]sessionEntry {
	expiresAt := now.Add(defaultSessionTTL)
	camilaHash := domain.HashToken("rf-token-camila")
	mariaHash := domain.HashToken("rf-token-maria")
	yuliHash := domain.HashToken("rf-token-yuli")
	return map[string]sessionEntry{
		camilaHash: {
			UserID:    uuid.MustParse("11111111-1111-1111-1111-111111111111"),
			ExpiresAt: expiresAt,
		},
		mariaHash: {
			UserID:    uuid.MustParse("22222222-2222-2222-2222-222222222222"),
			ExpiresAt: expiresAt,
		},
		yuliHash: {
			UserID:    uuid.MustParse("33333333-3333-3333-3333-333333333333"),
			ExpiresAt: expiresAt,
		},
	}
}

func DefaultClients(now time.Time) []domain.AdminClient {
	return defaultClients(now)
}

func DefaultStaff(now time.Time) []domain.StaffRecord {
	return defaultStaff(now)
}

func defaultClients(now time.Time) []domain.AdminClient {
	return []domain.AdminClient{
		{
			ID:          "cli-camila",
			FullName:    "Camila Rodríguez",
			Email:       "camila@rosafiesta.do",
			Phone:       "809-555-0142",
			Notes:       "VIP client with wedding and corporate celebrations.",
			EventsCount: 2,
			TotalSpent:  10700,
			BalanceDop:  7490,
			UpdatedAt:   now,
		},
		{
			ID:          "cli-laura",
			FullName:    "Laura Peña",
			Email:       "laura@rosafiesta.do",
			Phone:       "809-555-0188",
			Notes:       "Children birthday setup in Arroyo Hondo.",
			EventsCount: 1,
			TotalSpent:  405,
			BalanceDop:  405,
			UpdatedAt:   now,
		},
		{
			ID:          "cli-ana",
			FullName:    "Ana Gómez",
			Email:       "ana@rosafiesta.do",
			Phone:       "809-555-0194",
			Notes:       "Quinceanera package in Naco.",
			EventsCount: 1,
			TotalSpent:  2140,
			BalanceDop:  2140,
			UpdatedAt:   now,
		},
		{
			ID:          "cli-caribe",
			FullName:    "Grupo Caribe",
			Email:       "eventos@grupocaribe.do",
			Phone:       "809-555-0210",
			Notes:       "Corporate gala paid in full.",
			EventsCount: 1,
			TotalSpent:  8500,
			BalanceDop:  0,
			UpdatedAt:   now,
		},
	}
}

func defaultStaff(now time.Time) []domain.StaffRecord {
	mariaID := uuid.MustParse("22222222-2222-2222-2222-222222222222")
	yuliID := uuid.MustParse("33333333-3333-3333-3333-333333333333")
	return []domain.StaffRecord{
		{
			ID:          "staff-maria",
			UserID:      mariaID,
			FullName:    "María",
			Email:       "maria@rosafiesta.do",
			Phone:       "809-555-0100",
			RoleLabel:   "Dueña · Administradora general",
			Role:        "admin",
			Permissions: []string{"eventos", "decoracion", "tareas", "inventario", "pagos", "reembolsos", "reportes", "ajustes"},
			Active:      true,
			CreatedAt:   now,
		},
		{
			ID:          "staff-pedro",
			UserID:      uuid.Nil,
			FullName:    "Pedro",
			Email:       "pedro@rosafiesta.do",
			Phone:       "809-555-0101",
			RoleLabel:   "Logística y montaje",
			Role:        "staff",
			Permissions: []string{"eventos", "tareas", "inventario"},
			Active:      true,
			CreatedAt:   now,
		},
		{
			ID:          "staff-yuli",
			UserID:      yuliID,
			FullName:    "Yuli",
			Email:       "yuli@rosafiesta.do",
			Phone:       "809-555-0102",
			RoleLabel:   "Decoración y tareas (sin pagos)",
			Role:        "staff",
			Permissions: []string{"decoracion", "tareas"},
			Active:      true,
			CreatedAt:   now,
		},
		{
			ID:          "staff-andres",
			UserID:      uuid.Nil,
			FullName:    "Andrés",
			Email:       "andres@rosafiesta.do",
			Phone:       "809-555-0103",
			RoleLabel:   "Almacén y transporte",
			Role:        "staff",
			Permissions: []string{"inventario", "tareas"},
			Active:      true,
			CreatedAt:   now,
		},
	}
}
