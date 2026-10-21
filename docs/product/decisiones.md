# Decisiones de Producto y Diseño

1. **Dos apps sobre un mismo núcleo multiplataforma (`app/`):**
   - La App Personal (`com.jeudry.rosafiesta`) y la App Administrativa (`com.jeudry.rosafiesta.admin`) comparten `core:engine`, `core:domain`, `core:data` y `core:designsystem`.
   - En `A30` ("Elegir cómo entrar"), un administrador puede alternar en vivo entre la vista de negocio (`AdminApp`) y la vista como cliente (`PersonalApp`).
2. **Rosa IA con identidad cromática estricta por app:**
   - En la App Personal, Rosa IA usa el degradado `hero` (`#E04FAF` → `#945AE1` → `#6664E9`) y fondo `rosa-asistente-bg` (`#DED5F7` → `#D9D0F2`).
   - En la App Administrativa, Rosa IA usa **siempre** el degradado `rosa-admin-bg` (`#7C5CF0` → `#5B6CF0` → `#2FB8C8`) y fondo `#E3E1FB` → `#DCDDF8`, nunca rosa.
3. **Versión Web Administrativa (`A39`):**
   - En pantallas de escritorio/web (`1440 × 900`), la App Administrativa presenta barra lateral fija y tarjetas de KPI con tipografía `Bricolage Grotesque 30 / 800`.
4. **Esqueletos en lugar de spinners:**
   - Los estados de carga (`s90`, `A36`) usan esqueletos con la misma estructura geométrica de la pantalla y barrido luminoso de 1.2 s; prohibidos los spinners de pantalla completa.
