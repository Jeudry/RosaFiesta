# RosaFiesta — Project Status

> Live inventory of what's implemented across the RosaFiesta full-stack monorepo.
> Last updated: 2026-04-05

## Overview

**RosaFiesta** is a decoration enterprise management system for event planning. It combines a Go REST API backend with a Flutter multi-platform frontend (customer app). A separate enterprise/admin app is planned for the future.

- **Backend**: Go 1.24, chi/v5, PostgreSQL 16.3, Redis 6.2
- **Frontend**: Flutter (customer-facing, focused on event planning and browsing)
- **Domain**: Event planning, quotation workflows, timeline tracking, real-time chat

---

## Backend (Go API)

### Endpoints by Handler

| Handler File | Responsibilities |
|---|---|
| `auth.go` | Register, login (token), refresh token, activate |
| `users.go` | Get user, user feed, update FCM token |
| `articles.go` | CRUD articles, by category, availability |
| `categories.go` | CRUD categories |
| `reviews_handler.go` | Article reviews (create, list) |
| `posts.go` | Posts + comments (legacy social) |
| `timeline.go` | Event timeline items CRUD |
| `cart.go` | Cart get/clear, add/update/remove items |
| `events.go` | CRUD events, pay, calendar, debrief, items |
| `guests.go` | Guest list CRUD per event |
| `event_tasks.go` | Event task checklist CRUD |
| `event_reviews_handler.go` | Post-event reviews |
| `messages_handler.go` | Event chat (HTTP + WebSocket) |
| `suppliers.go` | Supplier CRUD (moving to admin app) |
| `stats.go` | Analytics summary, quote adjustments |
| `health.go` | Health check |
| `calendar.go` | iCalendar export |
| `fcm.go` | Firebase Cloud Messaging |
| `hub.go` | WebSocket hub for real-time chat |
| `middleware.go` | Auth, rate limiting, RBAC |

### Database (39 migrations)

**Core tables**: `users`, `user_invitations`, `refresh_tokens`, `roles`, `articles`, `article_variants`, `attributes`, `dimensions`, `categories`, `carts`, `cart_items`, `events`, `event_items`, `event_tasks`, `event_timelines`, `event_messages`, `event_reviews`, `guests`, `suppliers`, `reviews`, `notification_logs`, `posts`, `comments`.

### Infrastructure

- **PostgreSQL 16.3** on `:5432`
- **Redis 6.2** on `:6379` (optional, `REDIS_ENABLED` env)
- Docker Compose orchestration

---

## Frontend (Flutter Customer App)

### Design System

Located in `lib/core/`:

- `app_colors.dart` — palette (hotPink, coral, amber, teal, violet, sky, gold)
- `app_theme.dart` — Material theme
- `design_system.dart` — shared widgets (`RfThemeToggle`, `RfLuxeButton`, `RfFormField`, `RfGradientOrbs`, `RfDecoLayer`, `RfGridPainter`) and `RfTheme` tokens
- `theme_provider.dart` — light/dark state
- Typography: `GoogleFonts.outfit` (headings), `GoogleFonts.dmSans` (body)

### Features

| Feature | Screens | Backend |
|---|---|---|
| **auth** | login, register, confirmation | ✅ |
| **home** | welcome_onboarding, home (Candy Pop redesign) | UI only |
| **products** | products_list, product_detail | ✅ |
| **categories** | (consumed by home) | ✅ |
| **shop** | shop, cart | ✅ |
| **profile** | profile | ✅ |
| **events** | create, list, detail, execution, timeline, calendar, chat, budget_analysis, checkout, debrief, reviews_sheet | ✅ |
| **guests** | guest_list | ✅ |
| **tasks** | event_task_list | ✅ |
| **suppliers** | supplier_list (accessed via "Más" menu) | ✅ |
| **stats** | — (backend only) | ✅ |
| **admin** | admin_analytics | ✅ |

### Core Services

- `api_client.dart` — Dio with JWT auto-injection
- `firebase_service.dart` — FCM
- `notification_service.dart` — local push
- `hive_service.dart` — local cache
- `pdf_export_service.dart` — PDF generation
- `sync_service.dart` — offline sync queue

### State (Providers in `main.dart`)

`ThemeProvider`, `AuthProvider`, `ProductsProvider`, `CartProvider`, `CategoriesProvider`, `ProfileProvider`, `EventsProvider`, `GuestsProvider`, `EventTasksProvider`, `SuppliersProvider`, `TimelineProvider`, `StatsProvider`, `ReviewsProvider`, `ChatProvider`, `DebriefProvider`.

### Key Dependencies

`provider`, `dio`, `http`, `hive`, `flutter_secure_storage`, `firebase_core`, `firebase_messaging`, `google_fonts`, `fl_chart`, `table_calendar`, `intl`, `pdf`, `printing`, `web_socket_channel`, `flutter_local_notifications`, `connectivity_plus`.

---

## Current Home Screen (RF-41, Candy Pop)

Recently redesigned with these components:

- **Header**: logo + gradient title, responsive theme toggle (icon-only on mobile), notifications bell with dot, cart icon, avatar (42px, pink border)
- **Sticky header**: appears on scroll-up, hides on scroll-down
- **Search bar**: 56px white card, search icon (#8D8E90), separate mic button box
- **Hero banner**: gradient (hotPink→violet→indigo), "Nueva Temporada 2026" badge, CTA
- **Quick stats**: 3 cards (Eventos, Años, Rating ★)
- **Nuestros Servicios**: horizontal pills (icon gradient + label)
- **Tendencias slider**: auto-advancing PageView with stats badges
- **Categories grid**: 2-column card grid from API
- **Floating pill bottom bar**:
  - 5 items: Inicio, Catálogo, Eventos, Calendario, Más
  - White background, rounded 35px
  - Active item: gradient pill (violet→hotPink) with icon + label, expands via `flex: 3`
  - Icon sizes tuned per-icon for visual balance (home 32, others 28-30)
- **AI Assistant FAB**: 76px gradient circle with `support_agent` icon, typing dots badge (upper area), one-time welcome tooltip with speech-bubble tail
- **More menu**: bottom sheet with Proveedores, Estadísticas, Configuración, Ayuda

### Auth Session Persistence

- `AuthProvider.tryRestoreSession()` reads JWT from `flutter_secure_storage` on startup
- `_AuthGate` widget in `main.dart` routes to `HomeScreen` or `WelcomeOnboardingScreen` based on restored session

---

## What's Next — Possible Directions

### UI Polish (continuing RF-41 style)
- Apply Candy Pop design to remaining screens (catalog, events list, event detail, profile, cart)
- Consistent empty states across all screens
- Loading skeletons to replace spinners
- Light/dark mode audit across all screens

### Features
- **AI Assistant screen**: wire the `_AiAssistantFab` tap → chat interface (currently a TODO)
- **Notifications screen**: bell icon has a dot, but no screen behind it
- **Search functionality**: search bar is UI-only
- **Real product data**: seed DB with realistic articles for demo
- **Event creation flow**: polish the multi-step wizard
- **Chat screen UX**: event chat exists but could use design refresh

### Technical
- Token refresh logic (TODO in `api_client.dart` line 31)
- Fetch user profile on session restore (currently creates empty `User(id: '', email: '')`)
- Unit tests for widgets and providers
- Integration tests for auth flow
- Backend: finish stats endpoints, review notification worker

### Documentation
- API endpoint docs (Swagger is already set up via `make gen-docs`)
- Component library documentation for the design system

---

## References

- Main project instructions: `CLAUDE.md`
- Product requirements: `PRD.md`
- Testing strategy: `TESTING.md`
- Screenshots: `frontend/screenshots/`
- UI design references: `/Users/sargon/Documents/Coding/ui/REFERENCES.md`
