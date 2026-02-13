# Architecture Overview

This project follows a strict **Layered Architecture** designed for maintainability, testability, and clear separation of concerns.

## 1. High-Level Layers

```mermaid
graph TD
    Client[Client (Web/Mobile)] -->|HTTP Request| API[API Routing (Chi)]
    API -->|1. Request/Response| Handler[Handler Layer]
    Handler -->|2. Business Logic| Service[Service Layer]
    Service -->|3. Data Access| Repository[Repository Layer (Store)]
    Repository -->|SQL| DB[(PostgreSQL)]
```

## 2. Layer Responsibilities

### Handler Layer (`internal/api/handlers`)
- **Responsibility**: Interface with the outside world (HTTP).
- **Inputs**: `http.Request`, `http.ResponseWriter`.
- **Outputs**: JSON Response, HTTP Status Code.
- **Actions**:
  1.  Parse request body into DTOs.
  2.  Validate DTOs using `validator`.
  3.  Call Service methods.
  4.  Map service errors to HTTP errors (`apperrors`).
  5.  Format success response.
- **Rules**: NO business logic here. No direct DB access.

### Service Layer (`internal/services`)
- **Responsibility**: Business logic and domain rules.
- **Inputs**: DTOs or Domain Models.
- **Outputs**: Domain Models, Error.
- **Actions**:
  1.  Apply business rules (e.g., "User must be active").
  2.  Coordinate multiple repositories (e.g., "Create Post" + "Update User Stats").
  3.  Call external services (Email, S3).
- **Rules**: Independent of HTTP. Pure Go logic. Returns `apperrors`.

### Repository Layer (`internal/store`)
- **Responsibility**: Persistence and Data Retrieval.
- **Inputs**: Domain Models, IDs.
- **Outputs**: Domain Models, Error (`sql.ErrNoRows` wrapped).
- **Actions**:
  1.  Execute SQL queries.
  2.  Map SQL rows to structs.
  3.  Handle DB transactions.
- **Rules**: SQL lives here. No busines logic (except data integrity).

## 3. Key Design Decisions

- **Chi Router**: Lightweight, standard `net/http` compatible.
- **Dependency Injection**: All layers receive dependencies via constructors. This enables unit testing with mocks.
- **Centralized Error Handling**: Custom `apperrors` package maps domain errors to HTTP statuses, keeping handlers clean.
- **DTOs vs Models**:
    - **DTOs (`internal/dtos`)**: Data shape for API transport (JSON).
    - **Models (`internal/store/models`)**: Data shape for Database/Domain.
    - *Why?* Decouples API contract from DB schema.
