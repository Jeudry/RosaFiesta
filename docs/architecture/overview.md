# Arquitectura de RosaFiesta

## Visión general

```
         ┌──────────────────────── app/ (Kotlin Multiplatform + Compose Multiplatform) ────────────────────────┐
         │  PersonalApp (p00, s01–s92)  │  AdminApp (A00–A39, s55–s74, Web 1440×900)  │  dev/ (ScreenGallery)  │
         │                              ▼                                                                      │
         │                       core:designsystem (RfTheme.personal / RfTheme.admin)                          │
         │                              │                                                                      │
         │                              ▼                                                                      │
         │                ViewModels ──► Casos de uso (core:domain) ──► core:engine (reglas puras)             │
         │                                       │                                                             │
         │                                       ▼                                                             │
         │               core:data (SQLDelight offline-first · cliente Ktor contra OpenAPI)                    │
         └───────────────────────────────────────┬─────────────────────────────────────────────────────────────┘
                                                 │ HTTPS / JSON (OpenAPI 3.1)
         ┌───────────────────────────────────────▼─────────────── server/ (Go, monolito modular) ──────────────┐
         │ handlers (chi + huma) ─► servicios (app) ─► dominio (puro) ◄─ repositorios (sqlc) ─► PostgreSQL 17  │
         └─────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

## Principios

1. **Dos apps, un mismo producto:** La App Personal (`com.jeudry.rosafiesta`) y la App Administrativa (`com.jeudry.rosafiesta.admin`) comparten `core:engine`, `core:domain`, `core:data` y `core:designsystem`, diferenciándose por su tema (`RfTheme.personal` vs `RfTheme.admin`), su barra de navegación (`TabBar` vs `AdminBar` / barra lateral web `A39`) y los permisos del rol (`cliente`, `admin`, `equipo`).
2. **Local primero (offline-first):** La app guarda borradores, favoritos y cambios locales en SQLDelight (`s91`, `A37`) y sincroniza cuando vuelve la conexión.
3. **Reglas en un solo lugar (`core:engine`):** Kotlin puro (`explicitApi()`, `allWarningsAsErrors = true`), sin UI ni plataforma. Calcula anticipos (30 %), saldos (70 %), políticas de cancelación (90 % / 50 % / 0 %), primer cambio de fecha gratis, validación de eliminación de cuenta sin eventos activos (`s88`), permisos de equipo (`A28`), formato de moneda (`RD$ 1,350` / `RD$ 4.50`) y fechas cortas en español (`Vie 23 oct`, `2:00 p. m.`).
4. **Dependencias hacia el dominio:** Las capas externas conocen a las internas, nunca al revés. En Go lo verifica `depguard` dentro de `.golangci.yml`.
5. **Fronteras entre módulos:** Cada módulo del servidor tiene su propio paquete `sqlcgen` y no toca las tablas ni clases internas de otro módulo.
6. **Determinismo e idempotencia:** Reloj (`Clock`) y azar (`RandomSource`) siempre inyectados. Pagos, reembolsos y aprobaciones de cotización requieren clave de idempotencia.
