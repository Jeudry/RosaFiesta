# ADR 0001 — Kotlin Multiplatform + Compose Multiplatform para las dos apps (Personal y Admin)

## Estado
Aceptado.

## Contexto
RosaFiesta consta de dos aplicaciones ("App Personal" para clientes y "App Administrativa" para la dueña y su equipo) que deben correr en Android, iOS, escritorio (JVM) y web (WasmJS) compartiendo las reglas del negocio, el modelo de dominio, la persistencia local y los componentes base con dos temas diferenciados (`RfTheme.personal` y `RfTheme.admin`).

## Decisión
Usar Kotlin Multiplatform + Compose Multiplatform con separación estricta en `core:engine`, `core:domain`, `core:data`, `core:designsystem`, `composeApp`, `androidApp` (App Personal) y `adminAndroidApp` (App Administrativa).
