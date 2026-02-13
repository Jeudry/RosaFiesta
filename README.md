# 🌸 RosaFiesta

**Integrated Event Management & Creative Planning Platform**

RosaFiesta is a comprehensive cross-platform application (Android, iOS, Web, Desktop) designed to automate event logistics and offer AI-driven creative consulting.

## 🎨 Creative Vision
*   **Primary:** Pink (`#FFC0CB`) - Creativity and Warmth.
*   **Secondary:** Purple (`#800080`) - Elegance.
*   **Accent:** Green (`#4CAF50`) - Growth.
*   **Action:** Yellow (`#FFD700`) - Energy.

## 🏗 Architecture (Monorepo)

### 🔙 Backend (Go)
*   **Location**: [`/Backend`](./Backend)
*   **Stack**: Go (Chi Router), PostgreSQL.
*   **Core Modules**:
    *   **Inventory/Articles**: Real-time furniture & decor tracking.
    *   **Shopping Cart**: Order drafting.
    *   **Event Engine**: Logistics & Status management (Planned).
    *   **AI Gateway**: Gemini Integration (Planned).

### 📱 Mobile App (Flutter)
*   **Location**: [`/MobileApp`](./MobileApp)
*   **Stack**: Flutter (Dart).
*   **Platforms**: Android, iOS, Web, Desktop.
*   **Features**: Client Portal, Admin Dashboard, AI Moodboards.

## 🚀 Getting Started

### Backend
```bash
cd Backend
go run cmd/main/main.go
```

### Mobile App
```bash
cd MobileApp
flutter run
```

For the full architectural vision and roadmap, see [docs/VISION.md](docs/VISION.md).
