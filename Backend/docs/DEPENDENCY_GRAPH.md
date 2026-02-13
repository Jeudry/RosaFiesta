# Dependency Graph

This document explains how the application is wired up in `cmd/server/main.go`. We use **Manual Dependency Injection** (no reflection-based frameworks).

## Initialization Flow

```mermaid
graph TD
    Config[Config (Env)] --> DB[Database (Postgres)]
    Config --> Redis[Redis Cache]
    Config --> Mailer[GoMail Client]
    
    DB --> Store[Store (Repositories)]
    
    Store --> Services
    Mailer --> Services
    Redis --> Services
    Config --> Services
    
    Services[Services Layer] --> Handler[HTTP Handlers]
    
    Handler --> Router[Chi Router]
    Middleware --> Router
    
    Router --> Server[HTTP Server]
```

## Detailed Wiring

### 1. Infrastructure
- **Logger**: Zap SugaredLogger. Passed to almost all components.
- **Database**: `database/sql` connection pool.
- **Store**: `store.NewStorage(db)` initializes all repositories (`Users`, `Posts`, `Articles`, etc.).

### 2. Services
Services are initialized with their specific dependencies (Interface Segregation).

| Service             | Dependencies                                                                 |
| :------------------ | :--------------------------------------------------------------------------- |
| **AuthService**     | `store.Users`, `store.RefreshTokens`, `Config`, `JWTAuthenticator`, `Mailer` |
| **UserService**     | `store.Users`                                                                |
| **PostService**     | `store.Posts`, `store.Comments`                                              |
| **ArticleService**  | `store.Articles`                                                             |
| **CategoryService** | `store.Categories`, `store.Articles`                                         |
| **FeedService**     | `store.Posts` (UserFeedQuery)                                                |

### 3. Handlers
The `Handler` struct aggregates all services.
```go
type Handler struct {
    AuthService     services.AuthServicer
    UserService     services.UserServicer
    PostService     services.PostServicer
    ArticleService  services.ArticleServicer
    CategoryService services.CategoryServicer
    FeedService     services.FeedServicer
    // ...
}
```

### 4. Router
The `Router` (`internal/api/router`) binds HTTP paths to Handler methods and wraps them with Middleware (`RequestID`, `Logger`, `Recoverer`, `Auth`).
