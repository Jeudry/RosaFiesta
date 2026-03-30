# Home Screen Creative Variants — 2026-03-30

## Summary
4 radically creative home screen variants, each using a completely different aesthetic direction informed by ui-ux-pro-max style/color/typography recommendations. These are NOT conventional designs — each pushes boundaries in a different direction.

## Screenshots

### Variante 1 — Liquid Blossom (Liquid Glass + Organic Floral + Iridescent)
![Liquid Blossom](variant_1_liquid_blossom.png)

**Style**: Liquid Glass | **Fonts**: Syne + Manrope | **Palette**: Deep purple-black + iridescent cycling (pink/gold/teal/violet)
- Organic blob background with cubic bezier shapes morphing on 12s loop
- Iridescent animated ShaderMask on hero text "Creamos Magia"
- Floating photo cards at angles (scattered polaroid layout)
- Services as floating pills at staggered Y positions
- Categories with animated iridescent SweepGradient rings
- Stats as floating glass orbs at different positions
- Floating pill bottom nav with rainbow FAB
- Film grain CustomPainter overlay

---

### Variante 2 — Memphis Fiesta (80s Memphis Design + Postmodern)
![Memphis Fiesta](variant_2_memphis_fiesta.png)

**Style**: Memphis Design | **Fonts**: Abril Fatface + Merriweather | **Palette**: Warm cream + clashing pink/yellow/cyan/purple/coral/mint
- Memphis pattern background (dozens of geometric shapes: triangles, zigzags, squiggles, dots)
- Each letter of "ROSA FIESTA" a different color
- Hero tilted -3 degrees with zigzag pattern + circular photo cutout + marker highlight text
- Search bar rotated -1.5 degrees with thick black border
- Party type blocks at different rotations and shapes (circle, square, triangle, pill)
- Categories in geometric frames (triangle, circle, star, hexagon)
- Asymmetric featured event cards (different heights)
- Squiggly line separators via CustomPainter
- Yellow bouncy FAB with squiggly decoration

---

### Variante 3 — Neon Gala (Synthwave / Retro-Futurism / Cyberpunk)
![Neon Gala](variant_3_neon_gala.png)

**Style**: Retro-Futurism | **Fonts**: Space Grotesk (all weights) | **Palette**: Deep navy #0A0A1E + neon pink/cyan/blue/purple
- Perspective grid background vanishing to horizon
- CRT scanline overlay
- "NEON GALA" 56px with triple neon glow (pink + cyan + purple shadows)
- Sunset gradient hero (pink to orange to purple) with duotone photo filter
- Pulsing neon stat counters with colored top borders
- Categories with neon-colored borders (each different)
- Featured events with diagonal "TRENDING" stripe
- Animated scanning beam on bottom nav border
- Neon pink FAB with cyan glow shadow

---

### Variante 4 — Velvet Cinema (Vintage Film + Skeuomorphism + Editorial)
![Velvet Cinema](variant_4_velvet_cinema.png)

**Style**: Vintage Analog Film | **Fonts**: Cinzel + Josefin Sans | **Palette**: Faded cream #F5E6C8 + velvet burgundy #4A1528 + gold foil #B8860B
- Paper grain texture CustomPainter on entire screen
- Film strip header with sprocket holes
- Magazine editorial hero layout (photo 60% + text side by side)
- Pull quote with large gold quotation mark decoration
- Contact sheet photo grid (3x2, sepia filter, frame numbers)
- Two-column magazine editorial services layout with gold divider
- Polaroid-style category cards with slight rotation
- Stats as film frames with light leak gradient overlay
- Velvet burgundy bottom nav with gold embossed FAB

## Design System Source
Powered by `ui-ux-pro-max`:
- Styles: Liquid Glass, Memphis Design, Retro-Futurism, Vintage Analog Film
- Typography: Fashion Forward (Syne+Manrope), Retro Vintage (Abril Fatface+Merriweather), Neo Brutalism (Space Grotesk), Real Estate Luxury (Cinzel+Josefin Sans)
- Colors: Iridescent cycling, Memphis clashing, Neon cyberpunk, Warm sepia vintage

## Notes
- V1 (Liquid Blossom) most visually stunning but heaviest on GPU (blurs + animations)
- V2 (Memphis Fiesta) most unique/memorable but may feel chaotic for professional users
- V3 (Neon Gala) strongest dark theme with excellent neon effects
- V4 (Velvet Cinema) most editorial/premium feel with sepia film aesthetic
- All 4 compile with 0 errors and preserve full navigation/provider functionality
