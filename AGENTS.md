# AGENTS.md — RosaFiesta

Directivas y memoria para agentes de Inteligencia Artificial en este proyecto.

## Regla de Oro de Entrega (Obligatoria y No Negociable)
**SIEMPRE realizar commits incrementales claros en git cada vez que se entregue una corrección, ajuste o funcionalidad terminada al usuario, antes de dar la respuesta o tarea por cerrada.**
- Ningún avance debe quedar sin commitear.
- Mensajes semánticos con Conventional Commits (`feat(engine): ...`, `feat(app): ...`, `feat(server): ...`, `fix(...): ...`).

## Principios Técnicos Clave
1. **Dos Aplicaciones Multiplataforma (Personal + Admin):**
   - **App Personal (`PersonalApp`):** Tema `RfTheme.personal` (rosa → violeta, fondo rubor cálido). Modo claro (`s14`) y modo oscuro (`s15`).
   - **App Administrativa (`AdminApp`):** Tema `RfTheme.admin` (índigo → turquesa, fondo lavanda frío). Rosa IA usa siempre `rosa-admin-bg` (índigo → turquesa). Incluye vista móvil (390 × 844) y vista web de escritorio `A39` (1440 × 900 con barra lateral).
2. **Fidelidad Pixel a Pixel al Documento de Entrega (`docs/design/entrega-de-diseno.md`):**
   - Tipografías oficiales empacadas: **Bricolage Grotesque** (500–800, `letter-spacing: -0.02em`) y **Figtree** (400–700).
   - Márgenes laterales de 20 dp; contenido desde `y = 54 dp`; tarjetas con radio 20 dp; botones de 54 dp de alto y radio 16 dp; barra inferior de 96 dp con Rosa de 56 dp sobresaliendo 26 dp en el centro.
   - Fotos reales de producto y evento empacadas en `core:designsystem` (`silla-tiffany`, `mesa-redonda`, `centro-floral`, `arco-rosa`, `mesa-helados`, `mesa-plegable`, `boda`, `quinceanera`, `cumple-infantil`, `baby-shower`, `graduacion`, `corporativo`).
3. **Nunca bloquear el hilo principal (Main Thread):**
   - Todas las consultas SQLDelight y llamadas de red Ktor se ejecutan fuera del hilo principal con corrutinas estructuradas.
4. **Calidad y Verificación Continua (`make check`):**
   - `core:engine` y `core:domain` compilan con `explicitApi()` y `allWarningsAsErrors = true`.
   - El servidor Go valida pureza de dominio con `depguard` en `.golangci.yml` y sincronización estricta de `api/openapi.json`.
