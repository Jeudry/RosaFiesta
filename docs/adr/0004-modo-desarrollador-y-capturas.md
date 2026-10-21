# ADR 0004 — Modo desarrollador y pruebas de capturas deterministas

## Estado
Aceptado.

## Decisión
Cada pantalla del documento de entrega (`p00`, `s01`–`s92`, `A00`–`A39`) se registra como un `Board` en `composeApp` con los datos de ejemplo de la sección 6. La galería del modo desarrollador (`dev/ScreenGallery.kt`) y las pruebas de capturas (`ScreenshotAssertions.kt`) consumen exactamente esa misma lista de tableros.
