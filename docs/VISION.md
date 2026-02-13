# 🌸 RosaFiesta App - Vision & Architecture

## 1. Overview
**RosaFiesta** is a cross-platform (Android, iOS, Desktop, Web) platform designed for comprehensive event management, decoration, and planning. The app aims to automate internal logistics and offer a creative consulting experience driven by AI.

### Visual Harmony (Color Palette)
*   **Primary:** Pink (`#FFC0CB`) - Creativity and Warmth.
*   **Secondary:** Purple (`#800080`) - Elegance and Exclusivity.
*   **Accent:** Green (`#4CAF50`) - Growth, Nature, and Success.
*   **Action:** Yellow (`#FFD700`) - Energy and Attention (CTAs).

---

## 2. Technology Stack
*   **Backend:** Go (Golang) + Chi Router.
*   **Database:** PostgreSQL (Relational integrity).
*   **Frontend:** Flutter (Dart) - Cross-platform (Mobile, Web, Desktop).
*   **Communication:** REST API (Current) / GraphQL (Planned for complex event data).
*   **AI:** Google Gemini API (Event modeling, idea generation, and stock analysis).

---

## 3. System Architecture

### A. Backend Modules (Go)
*   **Event Engine:** Management of dates, locations, and statuses (Planning, Setup, Finalized).
*   **Inventory/Product API**: Real-time control of furniture, linens, and flowers. *Currently implemented as Articles/Products.*
*   **Shopping Cart:** Management of user selections and draft orders. *Currently implemented.*
*   **Financial Suite:** Dynamic budget generation, invoicing, and payments.
*   **AI Integration Service:** Gateway to connect with Gemini for design suggestions.
*   **Auth System:** Secure user management and role-based access. *Currently implemented.*

### B. Frontend Modules (Flutter)
*   **Shared UI Core:** Common components (RosaFiesta Buttons, inputs, cards) using the defined color palette.
*   **Client Portal:** Moodboard visualization and event tracking.
*   **Admin Dashboard:** Inventory management (QR scanning) and financial reports.

---

## 4. Artificial Intelligence Features
1.  **AI Moodboard Assistant:** Client describes a concept; AI generates a list of materials/ideas based on real stock.
2.  **Smart Budgeter:** Calculates budgets based on complexity and historical pricing.
3.  **Chatbot Consultant:** Auto-responses for FAQs on availability and services.
4.  **Content Generator:** Text creation for invitations and post-event posts.

---

## 5. Core Data Schema (PostgreSQL)
*   **Users:** (id, name, email, role [ADMIN, CLIENT]). *Implemented*
*   **Articles/Inventory:** (id, name, category, total_qty, available_qty, rental_price). *Implemented*
*   **Carts:** (id, user_id, items). *Implemented*
*   **Events:** (id, client_id, date, location, estimated_budget, status).
*   **Budgets:** (id, event_id, total, json_breakdown, paid).

---

## 6. Development Roadmap

### Phase 1: Foundations (Backend & DB) ✅
*   [x] Go Backend Setup & PostgreSQL Connection.
*   [x] Auth, Articles, and Cart Modules.
*   [x] Database Migrations.

### Phase 2: Cross-Platform Logic (In Progress) 🚧
*   [x] Flutter Project Setup (Monorepo).
*   [ ] Implementation of Material 3 Theme with RosaFiesta palette.
*   [ ] Porting Auth and Product screens to Flutter.

### Phase 3: Event Engine & Financials
*   [ ] Event Management Module.
*   [ ] Budgeting and Invoicing.

### Phase 4: AI Integration
*   [ ] Google Gemini SDK Connection.
*   [ ] Decorative Suggestion Assistant.

### Phase 5: Deployment
*   [ ] Docker Containerization.
*   [ ] CI/CD for Android (.apk), iOS, and Web.
