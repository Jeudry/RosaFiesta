# CLAUDE.md — RosaFiesta

Plataforma integral de planificación, alquiler de mobiliario/decoración y operación de eventos con Rosa IA.
Lee esto completo antes de tocar código. Detalle en `docs/`. Cada carpeta (`app/`, `server/`) tiene su propio `CLAUDE.md`.

## Principios de producto y diseño (no negociables)

- **Dos apps, un mismo producto (`docs/design/entrega-de-diseno.md`):**
  1. **App personal (clientes):** Rosa → violeta, fondo rubor cálido (`RfTheme.personal`). `TabBar`: Inicio · Catálogo · **Rosa** · Eventos · Favoritos (`p00`, `s01`–`s54`, `s75`–`s92`).
  2. **App administrativa (negocio — María, Pedro, Yuli, Andrés):** Índigo → turquesa, fondo lavanda frío (`RfTheme.admin`). `AdminBar`: Inicio · Eventos · **Rosa** · Inventario · Más (`A00`–`A39`, `s55`–`s74`), incluyendo la versión web `A39` a 1440 × 900 con barra lateral.
- **Regla estricta de Rosa IA en Admin:** En la app administrativa, Rosa IA usa **siempre** el degradado admin (`#7C5CF0` → `#5B6CF0` → `#2FB8C8`), jamás el rosa.
- **Tokens obligatorios (`RfTheme.personal` y `RfTheme.admin`):** Prohibido escribir colores, radios o medidas sueltas fuera de `core:designsystem`.
- **Formato de moneda y fechas:** Moneda `RD$ 1,350` con separador de miles `,` y sin decimales salvo precios unitarios con centavos (`RD$ 4.50`). Fechas cortas en español: `Vie 23 oct`, `2:00 p. m.`.
- **Política de cancelación y cuenta:** Más de 7 días antes: 90 % de reembolso; de 3 a 7 días: 50 %; menos de 3 días: sin reembolso (0 %). El primer cambio de fecha es gratis. Al eliminar la cuenta (`s88`), se valida primero que no existan eventos activos.

## Idioma

- **Código, identificadores, commits y mensajes de error técnicos: inglés.**
- **Lo que ve el usuario (UI, etiquetas, estados, notificaciones): español.**
- **Documentación (`docs/`), KDoc y comentarios doc en Go: español.**

## Arquitectura (resumen; detalle en `docs/architecture/overview.md`)

- **Apps (`app/`):** Kotlin Multiplatform + Compose Multiplatform (Android, iOS, Desktop JVM, Web WasmJS). Módulos:
  - `core:engine` (reglas puras del negocio en Kotlin puro, `explicitApi()`, `allWarningsAsErrors = true`, sin UI ni plataforma),
  - `core:domain` (modelos inmutables, casos de uso, interfaces de repositorio, datos de ejemplo canónicos),
  - `core:data` (SQLDelight offline-first + cliente Ktor tipado contra `api/openapi.json`),
  - `core:designsystem` (tokens `RfColors`, `RfTypography` con Bricolage Grotesque y Figtree empacadas, `RfDimens` y componentes de la sección 3),
  - `composeApp` (pantallas de la App Personal y la App Administrativa, versión web `A39` 1440×900, galería del modo desarrollador `dev/`),
  - `androidApp` (ejecutable Android de App Personal `com.jeudry.rosafiesta`),
  - `adminAndroidApp` (ejecutable Android de App Administrativa `com.jeudry.rosafiesta.admin`).
- **Backend (`server/`):** Go + PostgreSQL, **monolito modular** (sin microservicios). Enrutador `chi/v5` + `huma/v2` (OpenAPI 3.1 autogenerado en `api/openapi.json`). SQL tipado con `sqlc`. Capas: `httpapi` (handlers) → `app` (casos de uso) → `domain` (reglas puras, verificado por `depguard`) ← `postgres` (repositorios `sqlc`).
- **SOLID y Clean Code:** Las dependencias apuntan hacia el dominio. Un módulo no toca las tablas ni los paquetes internos de otro. Reloj y azar siempre inyectados (`Clock`, `RandomSource`). Idempotencia en pagos, reembolsos y cotizaciones.

## Pruebas (obligatorias)

- Toda regla del dominio (`docs/product/reglas-del-negocio.md`) y cada dato de ejemplo (`docs/design/entrega-de-diseno.md` sección 6) tiene prueba unitaria en `core:engine` y `server/internal/*/domain`.
- Servidor: pruebas unitarias, contrato OpenAPI (`internal/app/openapi_test.go`) y pruebas de integración con PostgreSQL real (`internal/*/postgres`).
- Capturas de pantalla (`ScreenshotAssertions`): cada pantalla (`p00`, `s01`–`s92`, `A00`–`A39`) se registra como `Board` en `dev/` y se verifica con `make app-screenshots`.
- Nada se da por terminado sin: `make check` en verde (formato `spotlessCheck`, pruebas KMP, `golangci-lint` y `go test ./...`).
