# Hoja de Ruta (Roadmap) — RosaFiesta

## Estado actual: Completo ✅

- [x] **Fase 1 · Motor de reglas (`core:engine`) y Dominio (`core:domain`):**
  - Formateadores de moneda (`RD$ 1,350`, `RD$ 4.50`) y fechas cortas en español (`Vie 23 oct`, `2:00 p. m.`).
  - Motor de cotizaciones, anticipo del 30 %, saldo y vencimiento.
  - Política de cancelación (90 % >7d, 50 % 3–7d, 0 % <3d) y primer cambio de fecha gratis.
  - Validador de eliminación de cuenta (`s88`), permisos de equipo (`A28`) y flujo de Rosa IA.
- [x] **Fase 2 · Sistema de Diseño (`core:designsystem`):**
  - Tokens exactos de `theme.personal` (claro `s14` y oscuro `s15`) y `theme.admin`.
  - Tipografías empacadas **Bricolage Grotesque** (500–800) y **Figtree** (400–700).
  - Componentes oficiales: `Logo`, `Rosa`, `TabBar`, `AdminBar`, `Icon`, `Foto`, `Illo`, `Tarjeta`, `Chip`, `Tag`, `Campo`, `Segmentado`, `Toast`, `BannerSinConexion`, `HojaInferior`, `Esqueleto`.
- [x] **Fase 3 · App Personal (`p00`, `s01`–`s54`, `s75`–`s92`) y App Administrativa (`A00`–`A39`, `s55`–`s74`):**
  - Todos los tableros móviles (`390 × 844`) y la versión web administrativa (`A39` a `1440 × 900`).
  - Galería del modo desarrollador (`dev/ScreenGallery.kt`) y pruebas de regresión visual (`ScreenshotAssertions`).
- [x] **Fase 4 · Servidor Go + PostgreSQL (`server/`):**
  - Monolito modular (`identity`, `catalog`, `events`, `quotes`, `payments`, `operations`, `health`, `devtools`).
  - Contrato OpenAPI 3.1 autogenerado en `api/openapi.json` y validado en pruebas.
