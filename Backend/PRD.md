# RosaFiesta — Product Requirements Document (PRD)

**Version:** 1.1.0
**Last Updated:** March 16, 2026
**Platform:** Go backend + Flutter frontend (multi-platform)

---

## 1. Executive Summary

RosaFiesta is a full-stack event management and decoration enterprise system. It enables event planners, administrators, and end customers to manage the complete event lifecycle — from browsing a product catalog and building quotations to coordinating day-of execution with real-time chat and checklists.

**Target Users:**
- Event planners and coordinators
- Decoration enterprise administrators
- Suppliers managing inventory
- End customers booking events

**Core Value Proposition:**
- Centralized quotation and approval workflow
- Real-time event coordination (WebSocket chat, day-of checklists)
- Product catalog with variant-level inventory and availability checking
- Cross-platform Flutter app (iOS, Android, Web, macOS, Windows, Linux)
- Bilingual support (English + Spanish)

---

## 2. User Roles & Permissions

| Role | Level | Capabilities |
|------|-------|-------------|
| **User** | 0 | Create/manage events, browse catalog, manage guests/tasks/timeline/suppliers, chat, pay for events, export .ics calendar |
| **Moderator** | 1 | All User capabilities + create/update/delete articles, categories, and reviews |
| **Admin** | 2 | All Moderator capabilities + adjust quotations, view analytics dashboard, debug access |

Authorization is enforced via `User.Role.Level` checks in middleware. Users can only access their own events, guests, tasks, suppliers, and cart.

---

## 3. Core Features

### 3.1 Authentication & User Management

- **Registration**: Email + password, with async email verification (72h token expiry)
- **Login**: Returns JWT (7-day expiry) + refresh token (30-day expiry)
- **Token refresh**: Auto-rotate refresh tokens
- **Activation**: Email link → `{FRONTEND_URL}/confirm/{token}`
- **FCM token management**: Users register device tokens for push notifications

**User model:** UUID, username, first/last name, email (unique), phone, avatar URL, born date, bcrypt password, role FK, FCM token, isActive flag.

### 3.2 Event Management (Core Business Logic)

**Event lifecycle statuses:**

```
planning → requested → adjusted → confirmed → paid → completed
```

| Status | Trigger |
|--------|---------|
| `planning` | User creates event |
| `requested` | User submits quotation request |
| `adjusted` | Admin adds costs and notes via `/admin/events/{id}/adjust` |
| `confirmed` | User accepts adjusted quotation |
| `paid` | User completes payment via `/events/{id}/pay` |
| `completed` | Event has occurred |

**Event model:** UUID, userID, name, date, location, guest count, budget, additional costs (admin), admin notes, status, payment status (pending/completed), payment method, paid-at timestamp.

**Business rules:**
- Payment only allowed when status = `confirmed`
- Admin adjustment sets status to `adjusted` and triggers FCM notification
- Users can only view/modify their own events

### 3.3 Quotation & Approval Workflow

1. User creates event with initial budget and selects items from catalog
2. User requests quotation (status → `requested`)
3. Admin reviews and calls `PATCH /admin/events/{id}/adjust` with additional costs and notes
4. Status → `adjusted`, FCM notification sent to user
5. User reviews adjusted quotation and proceeds to payment
6. `POST /events/{id}/pay` → status `paid`, FCM confirmation sent

### 3.4 Product Catalog & Inventory

**Articles** (product templates):
- Name, description, type (`Rental` or `Sale`), stock quantity, category FK
- Average rating and review count (calculated)

**Article Variants** (specific SKUs):
- SKU (unique), name, image URL, stock, rental price, sale price, replacement cost
- Attributes (JSON: color, size, material), dimensions (height, width, depth, weight)

**Availability checking:**
- `GetAvailability(articleID, eventDate)` returns available stock for a specific date
- Prevents overbooking of rental items when adding to events

**Reviews:** Rating (1–5), comment, linked to article and user. Average rating stored on article.

**Categories:** Hierarchical product organization with name and description.

### 3.5 Shopping Cart

- Auto-creates on first access (no empty state for user)
- Items reference article + optional variant + quantity
- No stock validation at cart time (validated when converting to event items)

### 3.6 Event Items

- Items added to a specific event from the catalog
- Availability validated against event date at add time
- Price captured at order time for historical accuracy

### 3.7 Guest Management

- Per-event guest list with RSVP tracking
- Fields: name, email, phone, RSVP status (`pending`/`confirmed`/`declined`), plus-one flag, dietary restrictions
- No automated email to guests (future feature)

### 3.8 Event Tasks & Checklists

- Per-event to-do items with title, description, due date, completion status
- Used in day-of execution mode as real-time checklist

### 3.9 Event Timeline (Schedule/Agenda)

- Per-event schedule items with title, description, start/end times
- Exported to .ics file for calendar sync
- Displayed in execution mode sorted chronologically

### 3.10 Calendar Integration (.ics Export)

- `GET /events/{id}/calendar.ics` — RFC 5545 compliant
- Includes main event (4-hour duration) + timeline items as sub-events
- Compatible with Google Calendar, Outlook, Apple Calendar

### 3.11 Real-Time Chat (WebSocket)

- Per-event chat rooms via WebSocket (`/events/{id}/messages/ws?token=<JWT>`)
- JWT authentication via query parameter
- Hub pattern: broadcast channel per event, read/write pumps per client
- HTTP fallback: `GET /POST /events/{id}/messages`

### 3.12 Supplier Management

- Personal supplier rolodex per user
- Fields: name, contact name, email, phone, website, notes
- No direct ordering integration (future feature)

### 3.13 Admin Dashboard & Analytics

- `GET /admin/stats` returns: total revenue, total events, revenue by month, events by status
- Frontend: pie charts, revenue graphs, event distribution charts

### 3.14 Notifications

- **Push**: Firebase Cloud Messaging (currently mock implementation, logs to console)
- **Email**: GoMail with MailTrap/SendGrid/Gmail support
- **Triggers**: Event status changes, quotation adjustments, payment confirmations

### 3.15 Day-of-Event Execution Mode

- Unified screen combining tasks + timeline items
- Sorted chronologically for real-time coordination
- Check-off items as completed during the event
- Chat available for team coordination

### 3.16 Localization

- English (`app_en.arb`) and Spanish (`app_es.arb`)
- Auto-generated via Flutter localization tool
- Automatic device locale detection

---

## 4. API Endpoints Summary

### Authentication (Public)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/authentication/register` | Register user |
| POST | `/authentication/token` | Login |
| POST | `/authentication/refresh` | Refresh JWT |
| PUT | `/users/active/{token}` | Activate account |

### Users (Authenticated)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/users/{userId}` | Get profile |
| GET | `/users/feed` | Get user feed |
| PUT | `/users/fcm-token` | Update FCM token |

### Events (Authenticated)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/events` | Create event |
| GET | `/events` | List user's events |
| GET | `/events/{id}` | Get event |
| PUT | `/events/{id}` | Update event |
| DELETE | `/events/{id}` | Delete event |
| POST | `/events/{id}/pay` | Record payment |
| GET | `/events/{id}/calendar.ics` | Export .ics |

### Event Items
| Method | Path | Description |
|--------|------|-------------|
| POST | `/events/{id}/items` | Add item |
| GET | `/events/{id}/items` | List items |
| DELETE | `/events/{id}/items/{itemId}` | Remove item |

### Guests
| Method | Path | Description |
|--------|------|-------------|
| POST | `/events/{id}/guests` | Add guest |
| GET | `/events/{id}/guests` | List guests |
| PUT | `/guests/{guestId}` | Update guest |
| DELETE | `/guests/{guestId}` | Remove guest |

### Tasks
| Method | Path | Description |
|--------|------|-------------|
| POST | `/events/{id}/tasks` | Create task |
| GET | `/events/{id}/tasks` | List tasks |
| PUT | `/tasks/{taskId}` | Update task |
| DELETE | `/tasks/{taskId}` | Delete task |

### Timeline
| Method | Path | Description |
|--------|------|-------------|
| POST | `/events/{id}/timeline` | Create item |
| GET | `/events/{id}/timeline` | List items |
| PUT | `/timeline/{itemId}` | Update item |
| DELETE | `/timeline/{itemId}` | Delete item |

### Messages & Chat
| Method | Path | Description |
|--------|------|-------------|
| GET | `/events/{id}/messages` | Message history |
| POST | `/events/{id}/messages` | Send message |
| GET | `/events/{id}/messages/ws` | WebSocket connection |

### Suppliers
| Method | Path | Description |
|--------|------|-------------|
| POST | `/suppliers` | Add supplier |
| GET | `/suppliers` | List suppliers |
| GET | `/suppliers/{id}` | Get supplier |
| PATCH | `/suppliers/{id}` | Update supplier |
| DELETE | `/suppliers/{id}` | Delete supplier |

### Articles (API Key required)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/articles` | Create article |
| GET | `/articles` | List articles |
| GET | `/articles/{id}` | Get article |
| PUT | `/articles/{id}` | Update (moderator+) |
| DELETE | `/articles/{id}` | Delete (moderator+) |
| GET | `/articles/{id}/reviews` | Get reviews |
| POST | `/articles/{id}/reviews` | Leave review |

### Categories (API Key required)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/categories` | List categories |
| POST | `/categories` | Create (moderator+) |
| GET | `/categories/{id}` | Get category |
| GET | `/categories/{id}/articles` | Articles in category |
| PUT | `/categories/{id}` | Update (moderator+) |
| DELETE | `/categories/{id}` | Delete (moderator+) |

### Cart (Authenticated)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/cart` | Get cart (auto-creates) |
| POST | `/cart/items` | Add item |
| PATCH | `/cart/items/{itemId}` | Update quantity |
| DELETE | `/cart/items/{itemId}` | Remove item |
| DELETE | `/cart` | Clear cart |

### Admin (Admin role only)
| Method | Path | Description |
|--------|------|-------------|
| PATCH | `/admin/events/{id}/adjust` | Adjust quotation |
| GET | `/admin/stats` | Analytics dashboard |

### System
| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Health check (public) |
| GET | `/debug/vars` | Debug info (basic auth) |
| GET | `/swagger/*` | API documentation |

---

## 5. Database Schema

### Tables
| Table | Description |
|-------|-------------|
| `users` | User accounts with role FK |
| `roles` | Role definitions (user, moderator, admin) |
| `refresh_tokens` | JWT refresh tokens |
| `invitations` | Email activation tokens |
| `articles` | Product templates |
| `article_variants` | SKU-level product variations |
| `article_attributes` | Product attributes (color, size, etc.) |
| `article_dimensions` | Physical dimensions |
| `categories` | Product categories |
| `carts` | Shopping carts (1:1 with user) |
| `cart_items` | Items in cart |
| `events` | Event records |
| `event_items` | Items ordered for event |
| `guests` | Guest list per event |
| `event_tasks` | Task checklist per event |
| `event_timelines` | Schedule items per event |
| `event_messages` | Chat messages per event |
| `suppliers` | Vendor contacts per user |
| `reviews` | Product reviews |
| `posts` | Social feed posts (legacy/future) |
| `comments` | Post comments (legacy/future) |

### Key Relationships
- User → Events (1:many)
- Event → Items, Guests, Tasks, Timeline, Messages (1:many each)
- Article → Variants, Reviews (1:many)
- Article → Category (many:1)
- User → Cart (1:1), User → Suppliers (1:many)

---

## 6. Security

- **JWT**: 7-day access tokens, 30-day refresh tokens
- **Passwords**: bcrypt hashed
- **SQL injection**: Prevented via parameterized queries (no ORM)
- **Rate limiting**: 100 requests per 5 seconds per IP (configurable)
- **CORS**: Configurable allowed origins
- **Ownership checks**: All user-scoped resources verified in handlers
- **API Key**: Required for catalog endpoints (service-to-service)
- **Basic Auth**: Debug endpoints only

---

## 7. Technical Considerations

| Area | Current State | Production Recommendation |
|------|--------------|--------------------------|
| FCM | Mock (logs to console) | Integrate Firebase Admin SDK |
| Payments | Mock (records in DB only) | Integrate Stripe/PayPal |
| Rate limiter | In-memory | Use Redis for distributed |
| WebSocket | Single-process hub | Redis pub/sub for scaling |
| Stock management | Availability check, no reservation | Add reservation/blocking system |
| CORS | Allows all origins | Restrict to known domains |

---

## 8. Future Roadmap

### Near-term
- [ ] Real payment processing (Stripe integration)
- [ ] Guest email invitations and RSVP links
- [ ] PDF export (invoices, event summaries, guest lists)
- [ ] Enhanced admin reporting with drill-down

### Medium-term
- [ ] AI decoration suggestions (Google Gemini integration)
- [ ] Supplier ordering portal
- [ ] QR code inventory scanning
- [ ] Multi-user teams with permission management

### Long-term
- [ ] GraphQL API
- [ ] Marketplace for freelance planners
- [ ] Subscription/premium tiers
- [ ] Audit logs and webhooks
