# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

RosaFiesta is a full-stack monorepo for a decoration enterprise management system. Backend is a Go REST API, frontend is a Flutter multi-platform app. Business domain: event planning, quotation workflows, supplier management, timeline tracking, real-time chat.

### Business model — rental-first

RosaFiesta is **primarily a rental company**. Tables, chairs, lighting, linens, backdrops, floral arches, glassware, neon signs, centerpieces and most decor items are **rented** to the client for the duration of their event and returned afterwards. This is the default assumption — if you don't know, assume an article is rental.

A **small subset** of items is sold outright (`type = 'Sale'`):
- Consumables (candles, confetti, balloons, sparklers, gifts)
- Custom-made pieces the client keeps (personalized neon signs, printed banners, custom cake toppers)
- Add-on materials the client uses up during the event

UI implications:
- **Do NOT show an `ALQUILER` badge on product cards** — it's noise because everything is rental by default. Cards should feel clean.
- **Only show a `VENTA` badge** on `type = 'Sale'` items so the client understands *this one is different, they keep it*.
- Section headers in the catalog should say "Artículos de alquiler" when they're showing the default rental grid.
- Rental pricing is `rental_price` per event/day; sale pricing is `sale_price`. Both exist on `article_variants` but only one applies depending on `type`.

### Two Frontend Apps (planned)

- **Customer App** (current `frontend/`): The client-facing app for end users who want to plan events, browse catalogs, create events, and manage their celebrations. This app does NOT focus on supplier management — suppliers are accessed via a "More" menu as a secondary feature.
- **Admin/Enterprise App** (future, separate frontend): A dedicated app for RosaFiesta administrators and staff. This app WILL focus on supplier management, inventory, quotation workflows, analytics, and internal operations. Not yet implemented.

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

### Frontend Design System (MANDATORY for all new screens)

**Single import**: `import 'package:frontend/core/design_system.dart';`
This file is the authoritative source of truth for all visual language. Do NOT define local color constants or duplicate widget classes.

**Color tokens** (`frontend/lib/core/app_colors.dart` — re-exported by `design_system.dart`):
| Token | Value | Usage |
|---|---|---|
| `AppColors.hotPink` | `#FF3CAC` | Primary CTAs, gradient titles |
| `AppColors.coral` | `#FF6B6B` | Errors, secondary accents |
| `AppColors.amber` | `#FFB800` | Gold highlights |
| `AppColors.teal` | `#00D4AA` | Success, accents |
| `AppColors.violet` | `#8B5CF6` | Orbs, backgrounds |
| `AppColors.sky` | `#4FC3F7` | Light accents |
| `AppColors.titleGradient` | hotPink→amber→teal | ShaderMask titles |
| `AppColors.buttonGradient` | hotPink→violet | CTA buttons |

**Theme tokens** (`RfTheme`) — dark/light surfaces, text colors, borders:
- Resolve from provider: `final t = RfTheme.of(context);`
- Or directly: `RfTheme.dark` / `RfTheme.light`
- Fields: `t.base`, `t.card`, `t.textPrimary`, `t.textMuted`, `t.textDim`, `t.borderFaint`, `t.isDark`

**Shared widgets** (all from `design_system.dart`):
- `RfThemeToggle(t: t)` — pill toggle that reads/writes `ThemeProvider`
- `RfLuxeButton(label:, onTap:, loading:)` — gradient CTA; `filled: false, t: t` for ghost variant
- `RfFormField(label:, icon:, controller:, t:, obscure:, validator:)` — styled input
- `RfGradientOrbs(controller:, color1:, color2:, isDark:)` — animated background blobs
- `RfDecoLayer(floatController:, decoController:, pulseController:, baseOpacity:)` — floating particles
- `RfGridPainter(color:)` — subtle background grid

**Global theme state**: `ThemeProvider` (in `MultiProvider` in `main.dart`)
- Default: light theme
- Read: `context.watch<ThemeProvider>().isDark`
- Toggle: `context.read<ThemeProvider>().toggle()`

**Typography**: `GoogleFonts.outfit()` for headings/display, `GoogleFonts.dmSans()` for body/UI

**Auth screen pattern** (reference implementations: `login_screen.dart`, `register_screen.dart`):
```dart
final t = RfTheme.of(context);  // resolves dark/light from ThemeProvider
// Layers: RfGradientOrbs → RfDecoLayer → RfGridPainter → SafeArea content
// Top bar: back button (ghost pill) + RfThemeToggle
// Title: ShaderMask with AppColors.titleGradient + GoogleFonts.outfit 42px w800
// Card: glassmorphism (dark) / opaque white (light) with RfFormField + RfLuxeButton
```

## Testing

- **Backend unit tests**: `cmd/main/*_test.go` using `testify/mock`
- **Integration tests**: `testcontainers` for PostgreSQL (spins up real DB in Docker)
- **Test helpers**: `newTestApplication()`, `executeRequest()`, `checkResponseCode()`
- **Mock generation**: `mockery` generates mocks from `Storage` interface into `internal/store/mocks/`
- **Test account**: `v2_tester@example.com` / `Password123!`

## Git Conventions

- Branch naming: `feature/RF-{number}_{name}`
- Main branch: `main`

---

## Feature Inventory

All features currently in the system, organized by status. **Always keep this list up to date** — when a feature is completed, move it from pending to done; when a new feature is requested, add it to pending with the label `NEW`.

### Completed

| # | Feature | Backend | Frontend | Notes |
|---|---------|---------|----------|-------|
| 1 | User auth (register, login, JWT, activate) | ✅ | ✅ | |
| 2 | Product catalog (categories, articles, variants, images) | ✅ | ✅ | rental-first model |
| 3 | Event lifecycle (create, draft, items, quote request) | ✅ | ✅ | |
| 4 | Supplier management (CRUD, contact info) | ✅ | ✅ | |
| 5 | Timeline / tracking items | ✅ | ✅ | |
| 6 | Real-time chat (WebSocket hub, messages) | ✅ | ✅ | |
| 7 | Push notifications (FCM, worker) | ✅ | ✅ | |
| 8 | Email notifications (templates, reminders) | ✅ | ✅ | |
| 9 | PDF quote generation | ✅ | ✅ | |
| 10 | Product search + filters (server-side, ILIKE) | ✅ | ✅ | |
| 11 | Image compression + R2 upload | ✅ | ✅ | max 1920px, JPEG 85% |
| 12 | Quote adjust + approve/reject flow | ✅ | ✅ | |
| 13 | Chat real-time UI redesign (WhatsApp-style) | ✅ | ✅ | |
| 14 | Public catalog (no-login browse + AuthRequiredSheet) | ✅ | ✅ | |
| 15 | Quote approval screen | ✅ | ✅ | |
| 16 | Onboarding redesign (catalog/AI/WhatsApp slides) | ✅ | ✅ | |
| 17 | Favorites without login (Hive local + sync on login) | ✅ | ✅ | |
| 18 | AI Assistant flow (Rosa IA, 7 steps) | ✅ | ✅ | |
| 19 | WhatsApp Business API integration | ✅ | ✅ | |
| 20 | Checkout + payment methods (transfer, cash, mock card) | ✅ | ✅ | phone field at checkout |
| 21 | Low stock badge on product cards | ✅ | ✅ | threshold DEFAULT 5 |
| 22 | Pending events on login response | ✅ | ✅ | |
| 23 | Activity / audit logs | ✅ | ✅ | event create/update/pay/adjust |
| 24 | Event photo gallery (R2) | ✅ | ✅ | |
| 25 | Multi-language (EN/ES toggle, Hive persist) | ✅ | ✅ | |
| 26 | Guest management per event | ✅ | ✅ | |
| 27 | Reviews (article + event + company) | ✅ | ✅ | |
| 28 | Email verification flow | ✅ | ✅ | verify-email/{token} screen |
| 29 | Password reset | ✅ | ✅ | forgot-password/reset-password screens |
| 30 | Order confirmation screen | ✅ | ✅ | post-checkout success screen |
| 31 | Email reminder triggers | ✅ | — | worker cron: 7d/24h/post-event |
| 32 | Push notification triggers | ✅ | — | FCM on quote adjusted/approved/rejected |
| 33 | Deep linking | ✅ | ✅ | iOS/Android universal links + hash routing |

### Pending

| # | Feature | Description | Priority |
|---|---------|-------------|----------|
| 34 | Admin quotation workflow | Admin adjusts quote → client approves/rejects | medium |
| 35 | Admin analytics dashboard | Stats endpoint, admin-only summary view | low |
| 36 | Enterprise app (separate frontend) | Admin-focused app for RosaFiesta staff | low |

### Legend
- `NEW` — just added, not yet started
- Priority: high / medium / low
- When a feature is completed: remove from Pending, add to Completed with ✅ in both columns
- When starting a feature: note the branch name in Pending
