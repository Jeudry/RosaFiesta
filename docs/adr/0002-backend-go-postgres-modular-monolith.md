# ADR 0002 — Backend en Go + PostgreSQL como monolito modular

## Estado
Aceptado.

## Contexto
El backend debe gestionar identidad y permisos de equipo (`A28`), catálogo e inventario por fecha (`s63`), eventos, cotizaciones con Rosa IA, pagos con anticipo del 30 %, reembolsos escalonados y operaciones (CRM, proveedores, reseñas, bot de WhatsApp y reportes).

## Decisión
Implementar un monolito modular en Go 1.26 con `chi/v5` + `huma/v2` (OpenAPI 3.1 autogenerado en `api/openapi.json`) y `pgx/v5` + `sqlc` sobre PostgreSQL 17. Cada módulo posee `domain/` (puro, protegido con `depguard`), `app/`, `httpapi/` y `postgres/`.
