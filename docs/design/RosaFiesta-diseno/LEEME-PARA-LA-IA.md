# RosaFiesta · Paquete de diseño para implementar

Este paquete es la **fuente de verdad visual** del rediseño. Se exportó directamente del lienzo de Claude Design, así que ya no hace falta abrir el enlace privado.

## Qué hay aquí

| Carpeta / archivo | Qué es | Para qué usarlo |
|---|---|---|
| `index.html` | Galería con todas las pantallas, agrupadas por sección | Ábrela primero para ubicarte |
| `pantallas/*.png` | Captura exacta de cada pantalla a 2x (780 × 1688 para móvil, 2880 × 1800 para web) | **Míralas.** Así se tiene que ver la app, píxel por píxel |
| `html/*.html` | Cada pantalla como HTML + CSS estático, ya “aplanado” (sin plantillas) | Sacar valores exactos de colores, degradados, tamaños, radios, sombras, márgenes y SVG. Inspecciona el elemento y copia |
| `assets/` | Logo oficial y fotos reales, con el nombre que usa el diseño (`silla-tiffany.jpg`, `boda.jpg`, `logo-rosafiesta.png`…) | Usar tal cual |
| `entrega-de-diseno.md` | Tokens, componentes, roles, flujos de navegación, estados y datos de ejemplo | Para la arquitectura y el tema |
| `fuente/*.dc.html` | Código original de cada pantalla y componente (con plantillas `{{ }}`, `sc-for`, `dc-import`) | Copiar SVG exactos: iconos (`Icon.dc.html`), avatar de Rosa (`Rosa.dc.html`), ilustraciones (`Illo.dc.html`) y barras (`TabBar`, `AdminBar`) |

Los nombres coinciden en todas las carpetas: `s14-inicio-claro.png` ↔ `html/s14-inicio-claro.html` ↔ `fuente/s14-inicio-claro.dc.html`.

## Reglas (no negociables)

1. **Lo que no aparece en el PNG no va en la UI.**
   - No agregues marcos de teléfono, barras de estado falsas (9:41, 5G, batería) ni etiquetas de depuración (`s14 · Inicio`, `A01`).
   - Tampoco textos sacados del documento de especificación (“Índigo → Turquesa”, “KPIs de 30 px”…).
2. Los títulos de la galería (`14 · Inicio (claro)`) son **nombres de archivo, no texto de la app**.
3. Cada pantalla mide **390 × 844**. Los primeros ~54 px de arriba están vacíos a propósito: ahí va la barra de estado real del sistema (safe area).
4. **Fuentes:** Figtree para el texto y Bricolage Grotesque para títulos y montos (Google Fonts).
   - Los PNG se renderizaron con Inter como sustituto, así que la forma de las letras puede variar un poco.
   - Los tamaños, pesos y posiciones sí son los correctos.
5. **Tema administrativo:** índigo → turquesa, tanto en las pantallas `a*` como en `s55`–`s74`. La app personal usa rosa → violeta.
   - Rosa IA en admin usa el degradado admin.
   - El logo va siempre sobre un círculo blanco.
6. Los datos (Camila, RD$ 4,500…) son de ejemplo. Conéctalos al backend, pero conserva el formato: `RD$ 1,350`, `Vie 23 oct`, `2:00 p. m.`.

## Cómo trabajar (una pantalla a la vez)

1. Abre `pantallas/<nombre>.png` y `html/<nombre>.html`.
2. Implementa la pantalla en el framework de la app, sacando del HTML los valores exactos (no los estimes a ojo).
3. Captura tu app con un viewport de **390 × 844** (o 1440 × 900 para `a39`).
4. Pon tu captura **al lado** del PNG de referencia y corrige cada diferencia: espaciados, tamaños de letra, colores, radios, sombras, iconos y textos.
5. **No te autoapruebes.** Muestra la comparación lado a lado al dueño del proyecto antes de marcar una pantalla como terminada.

Orden sugerido:
1. Componentes base: tokens, `Logo`, `Rosa`, `TabBar`, `AdminBar`, tarjeta, botón, chip y campo.
2. `p00` y `s14`.
3. Catálogo (`s18`, `s19`).
4. Flujo de Rosa.
5. Eventos.
6. Pago.
7. Admin: `a00`, `a01`, `a39`…
