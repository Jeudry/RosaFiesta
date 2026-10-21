# CLAUDE.md — server/ (Go + PostgreSQL)

Reglas generales en `../CLAUDE.md`. Arquitectura en `../docs/architecture/overview.md`.

## Comandos (con `make` desde la raíz)

```bash
make server-tools   # instala golangci-lint y sqlc en server/bin
make server-check   # análisis estático + pruebas
make openapi        # regenera api/openapi.json (el CI falla si está desactualizado)
make up             # PostgreSQL local con docker compose
make seed           # datos de ejemplo canónicos del diseño en la base de DATABASE_URL
make server-dev     # API con herramientas de /dev (etiqueta `dev`; no existe en producción)
```

## Estructura

```
cmd/api/           arranque del servidor
cmd/openapi/       genera api/openapi.json
cmd/seed/          carga los datos canónicos del diseño
internal/app/      ensambla los módulos (único que los conoce a todos)
internal/platform/ config, httpapi (chi + huma), db, dbtest, clock, mail
internal/<modulo>/ un módulo por dominio: identity, catalog, events, quotes, payments, operations, health, devtools
```

Dentro de cada módulo: `domain/` (reglas puras, verificadas con `depguard`) · `app/` (casos de uso / servicios) · `httpapi/` (handlers Huma) · `postgres/` (repositorios con `sqlc`).
