# CLAUDE.md — app/ (Kotlin Multiplatform: Personal + Admin)

Reglas generales en `../CLAUDE.md`. Arquitectura en `../docs/architecture/overview.md`.

## Comandos (desde `app/` o con `make` en la raíz)

```bash
./gradlew spotlessApply                 # formato (ktlint); o: make app-format
make app-check                          # lo que corre el CI (formato, pruebas, compilación)
make app-screenshots                    # regresión visual estricta; `make app-screenshots-approve` para aprobar
make app-ios                            # framework de iOS (solo macOS)
```

## Módulos

- `core:engine` — **reglas puras del negocio, Kotlin puro**. Prohibido importar Compose, Android, iOS, red o base de datos. Modo `explicitApi()` y **cero advertencias** (`allWarningsAsErrors = true`). Todo lo público lleva KDoc en español.
- `core:domain` — modelos inmutables, casos de uso, interfaces de repositorio y datos de ejemplo canónicos (`SampleData`). Modo `explicitApi()` y `allWarningsAsErrors = true`.
- `core:data` — SQLDelight local (`RosaFiestaDatabase`) + cliente HTTP Ktor tipado contra `api/openapi.json`.
- `core:designsystem` — tokens de `docs/design/entrega-de-diseno.md` (`RfColors`, `RfTypography`, `RfDimens`), `RfTheme` (`RfTheme.personal`, `RfTheme.admin`, `RfTheme.personalDark`), fotos reales empacadas e ilustraciones vectoriales.
- `composeApp` — interfaz compartida (Android, iOS, escritorio, web) para **ambas aplicaciones**:
  - `personal/`: App Personal (`p00`, `s01`–`s54`, `s75`–`s92`).
  - `admin/`: App Administrativa (`A00`–`A39`, `s55`–`s74`, incluyendo Web `A39` 1440×900).
  - `dev/`: Galería de tableros (`ScreenGallery`, `DesignGallery`, `DevPanelScreen`).
- `androidApp` — punto de entrada Android de la App Personal (`com.jeudry.rosafiesta`).
- `adminAndroidApp` — punto de entrada Android de la App Administrativa (`com.jeudry.rosafiesta.admin`).
