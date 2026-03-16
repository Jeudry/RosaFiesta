# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

RosaFiesta is a full-stack monorepo for a decoration enterprise management system. Backend is a Go REST API, frontend is a Flutter multi-platform app. Business domain: event planning, quotation workflows, supplier management, timeline tracking, real-time chat.

## Build & Run Commands

```bash
# Start local databases
docker-compose up

# Run backend (port 3000)
go run cmd/main/*.go

# Live reload backend
air

# Run Flutter frontend
cd frontend && flutter run

# Run all backend tests
make test

# Run a single test
go test -v -run TestFunctionName ./cmd/main/

# Database migrations
make migrate-up                          # Apply all migrations
make migrate-down 1                      # Rollback 1 step
make migration name=add_field            # Create new migration

# Seed database
make seed

# Generate Swagger docs
make gen-docs

# Generate mocks (from project root)
mockery

# Flutter code generation
cd frontend && flutter pub run build_runner build

# Flutter localization (after editing .arb files)
cd frontend && flutter gen-l10n
```

## Architecture

### Backend (Go 1.24, chi/v5, PostgreSQL, Redis)

**Layered request flow:**
```
Routes (cmd/main/api.go) → Middleware (auth, CORS, rate limiting) → Handlers (cmd/main/*.go) → Store (internal/store/) → PostgreSQL
```

- **No ORM** — raw SQL with postgres driver, all queries wrapped in `QueryTimeoutDuration` context (5s)
- **Transactions** via `withTx()` helper in store layer
- **Auth**: JWT (Bearer token) + API Key (X-Api-Key header) + Basic Auth (admin debug endpoint)
- **Role-based access**: `User.Role.Level` compared against role prerequisites in middleware
- **Response envelope**: `{data: T}` on success, `{error, message, status}` on failure
- **Error helpers**: `badRequest()`, `internalServerError()`, `unauthorized()`, `forbidden()`
- **Store errors**: `ErrNotFound`, `ErrConflict` (check these for control flow)
- **Cache**: Redis is optional, controlled by `REDIS_ENABLED` env var

**Key entry points:**
- `cmd/main/main.go` — server init, config loading, dependency injection
- `cmd/main/api.go` — all route registration (`Mount()`)
- `cmd/main/middleware.go` — auth, CORS, rate limiting, context injection
- `internal/store/store.go` — `Storage` interface (all data access methods)

### Frontend (Flutter/Dart, Provider)

**Feature-driven architecture** under `frontend/lib/features/`:
- Each feature has `data/` (repositories) and `presentation/` (screens, providers)
- State management: `ChangeNotifierProvider` + `MultiProvider`
- HTTP client: Dio with JWT auto-injection interceptor (`frontend/lib/core/api_client.dart`)
- Localization: ARB files in `frontend/lib/l10n/` (English + Spanish), access via `AppLocalizations.of(context)`
- Routing: Hash-based (`/#/login`, `/#/events`, `/#/catalog`)

## Testing

- **Backend unit tests**: `cmd/main/*_test.go` using `testify/mock`
- **Integration tests**: `testcontainers` for PostgreSQL (spins up real DB in Docker)
- **Test helpers**: `newTestApplication()`, `executeRequest()`, `checkResponseCode()`
- **Mock generation**: `mockery` generates mocks from `Storage` interface into `internal/store/mocks/`
- **Test account**: `v2_tester@example.com` / `Password123!`

## Git Conventions

- Branch naming: `feature/RF-{number}_{name}`
- Main branch: `main`
