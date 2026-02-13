# Coding Conventions

## File Structure

```
.
├── cmd/
│   └── server/         # Entry point (main.go)
├── internal/
│   ├── api/            # HTTP Layer
│   │   ├── handlers/   # Request parsing, validation, response formatting
│   │   ├── middleware/ # Interceptors (Auth, Logging)
│   │   └── router/     # Route definitions
│   ├── config/         # Configuration structs
│   ├── dtos/           # JSON Request/Response structs
│   ├── services/       # Business Logic (Interfaces & Implementations)
│   ├── store/          # Data Access (Repositories & Models)
│   └── utils/          # Shared utilities (Validation, Errors)
└── docs/               # Documentation
```

## Naming Conventions

- **Interfaces**: End with `-er` suffix where possible, or descriptive name.
    - Service Interfaces: `AuthServicer`, `UserServicer` (defined in `internal/services/interfaces.go`).
    - Repository Interfaces: `UserRepository`, `PostRepository` (defined in `internal/store/repositories.go`).
- **Structs**: PascalCase.
- **Variables**: camelCase. acronyms should be consistent (e.g., `ServeHTTP`, `userID` or `userId` - pick one, currently `userID` is preferred).

## Architectural Rules

1.  **Handlers**:
    - MUST NOT contain business logic.
    - MUST returns `apperrors` via `RespondWithError`.
    - MUST use `internal/dtos` for input/output.
2.  **Services**:
    - MUST NOT import `handlers` or `http`.
    - MUST return `error` types from `apperrors` (e.g. `apperrors.NotFoundError`).
3.  **Repositories**:
    - MUST return domain models (`internal/store/models`).
    - MUST NOT return `dtos`.

## Error Handling

- **Repository**: Return `sql.ErrNoRows` or wrapped errors.
- **Service**: Map `sql.ErrNoRows` to `apperrors.NotFoundError`.
- **Handler**: Call `RespondWithError(w, err)` which handles the mapping to HTTP status.

## Testing

- **Unit Tests**: Place in `_test.go` files next to the code.
- **Mocks**: generate with `make gen-mocks`.
- **Integration Tests**: Place in `tests/integration/`.
