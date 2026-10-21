# Pruebas de capturas de pantalla

Objetivo: que cada pantalla de la App Personal (`p00`, `s01`–`s92`) y de la App Administrativa (`A00`–`A39`, `s55`–`s74`) cumpla fielmente las especificaciones del Documento de entrega de diseño (`docs/design/entrega-de-diseno.md`) y que, una vez aprobada, no cambie por accidente.

## Cómo se dibuja cada tablero en las capturas

1. **Móvil (390 × 844):** Cada tablero móvil se renderiza a `390 × 844 dp` con densidad `3f` (1170 × 2532 px reales, como un iPhone 14/15) y se reduce con filtrado lineal a `390 × 844 px`.
2. **Web Administrativa (`A39`, 1440 × 900):** El tablero web `A39` se renderiza a `1440 × 900 dp` y se verifica con la barra lateral y los KPIs de 30 sp.
3. **Determinismo total:**
   - Fuentes oficiales empacadas en `core:designsystem` (**Bricolage Grotesque** 500–800 y **Figtree** 400–700).
   - Fotos reales de producto y evento empacadas en `core:designsystem`.
   - `ProvideReduceMotion(reduce = true)` desactiva animaciones infinitas para que las capturas sean 100 % deterministas.

## Comandos

```bash
make app-screenshots           # verifica todas las capturas contra screenshots/approved/
make app-screenshots-approve   # genera o actualiza las capturas aprobadas (UPDATE_SCREENSHOTS=1)
```
