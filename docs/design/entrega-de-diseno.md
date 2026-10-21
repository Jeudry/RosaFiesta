# RosaFiesta · Documento de entrega de diseño

Guía para implementar el rediseño (Variante B) en la app. El diseño completo está en el lienzo de Claude Design:
https://claude.ai/artifact/CH3bM79mqHRHXF2VgzWE8U

Si hay dudas, manda lo que se ve en el lienzo. Cada pantalla tiene un código (p. ej. `s48`, `A13`) que se usa en todo este documento.

---

## 1. Estructura: dos apps, un mismo producto

| | App personal (clientes) | App administrativa (negocio) |
|---|---|---|
| Quién la usa | Clientes que planean un evento | Dueña (María) y equipo (Pedro, Yuli, Andrés) |
| Tema | Rosa → violeta, fondo rubor cálido | Índigo → turquesa, fondo lavanda frío |
| Navegación | `TabBar`: Inicio · Catálogo · **Rosa** · Eventos · Favoritos | `AdminBar`: Inicio · Eventos · **Rosa** · Inventario · Más |
| Rosa IA | Arma la lista y la cotización del cliente | Crea borradores de eventos, cotiza y redacta respuestas |
| Splash | `p00` | `A00` |

**Roles.** `cliente` solo ve la app personal. `admin` (dueña) ve todo. `equipo` ve la app administrativa con los permisos de `A28` (por ejemplo, Yuli: decoración y tareas, sin pagos). `A30` permite que un admin entre como cliente para revisar la app personal.

**Plataformas.** Móvil 390 × 844 (iPhone 14/15). La app administrativa tiene además una versión web, `A39`, a 1440 × 900 con barra lateral. El resto de pantallas web siguen el mismo patrón de barra lateral y contenido.

---

## 2. Tokens de diseño

### 2.1 Comunes a las dos apps

| Token | Valor | Uso |
|---|---|---|
| `sol` | `#FFD45C` (texto `#2E1A47`) | Contadores, insignias “Nuevo”, acentos del logo |
| `sol-tint` | `#FFF2C9` (texto `#7A5700`) | Avisos, “vence pronto”, stock bajo |
| `lima` | `#B9D84A` | Icono de éxito en los toasts |
| `lima-tint` | `#EEF6D6` (texto `#4E6A10`) | Pagado, disponible, confirmado |
| `teal-tint` | `#DDF4F3` (texto `#1F8A84`) | Información, “Ajustado” |
| `rosa-tint` | `#FDE6F2` (texto `#C2338F`) | Etiquetas y avatares |
| `violeta-tint` | `#EFE8FD` (texto `#7A4FD8`) | Etiquetas y avatares |
| `error` | `#B42345`, tint `#FCE3E8` | Errores, cancelar, eliminar |
| `toast` | `#1A1A2E` (personal) / `#151729` (admin) | Fondo de los toasts y del banner sin conexión |

### 2.2 App personal

| Token | Valor |
|---|---|
| `ink` | `#1A1A2E` |
| `muted` | `#62627A` |
| `bg` | `radial-gradient(110% 40% at 0% 0%,#F9E3F1 0%,rgba(249,227,241,0) 60%), radial-gradient(80% 35% at 100% 15%,#EEE7FB 0%,rgba(238,231,251,0) 60%), #FAFAFC` |
| `btn-primary` | `linear-gradient(90deg,#EA4EA9 0%,#B754CB 50%,#865DEC 100%)` + sombra `0 12px 26px -12px rgba(200,70,180,.7)` |
| `hero` (tarjetas destacadas, chip activo, avatar Rosa) | `linear-gradient(135deg,#E04FAF 0%,#945AE1 55%,#6664E9 100%)` |
| `texto-gradiente` (precios) | `linear-gradient(90deg,#EA4EA9,#865DEC)` recortado al texto |
| `rosa-asistente-bg` | `linear-gradient(180deg,#DED5F7 0%,#F2EAF7 40%,#F5EEF7 60%,#D9D0F2 100%)` |
| `line` | `#F2EDF5` (bordes de tarjeta), `#F2F0F5` (separadores) |
| `chip` | `#F1EFF5` |

### 2.3 App administrativa

| Token | Valor |
|---|---|
| `ink` | `#151729` |
| `muted` | `#5F6377` |
| `bg` | `radial-gradient(110% 40% at 100% 0%,#E6E7FF 0%,rgba(230,231,255,0) 60%), radial-gradient(80% 35% at 0% 12%,#F1E9FB 0%,rgba(241,233,251,0) 60%), #F6F6FB` |
| `primary` | `linear-gradient(135deg,#7C5CF0 0%,#5B6CF0 100%)` + sombra `0 12px 24px -12px rgba(91,108,240,.75)` |
| `hero` | `linear-gradient(135deg,#7C5CF0 0%,#5B6CF0 55%,#2FB8C8 100%)` |
| `rosa-admin-bg` (avatar de Rosa) | `linear-gradient(145deg,#7C5CF0 0%,#5B6CF0 55%,#2FB8C8 100%)` |
| `rosa-asistente-bg` | `linear-gradient(180deg,#E3E1FB 0%,#F1EEFA 40%,#F4F1FA 60%,#DCDDF8 100%)` |
| `indigo-tint` | `#ECEBFF` (texto `#4B47D6`) |
| `line` | `#ECECF5` (bordes), `#E3E4EF` (campos), `#F0F0F6` (separadores) |
| `chip` / `segmentado` | `#F2F2F8` |

> Regla: en la app administrativa, Rosa IA usa **siempre** el degradado admin (índigo → turquesa), nunca el rosa.

### 2.4 Tipografía (Google Fonts)

- **Bricolage Grotesque** (500–800), con `letter-spacing:-0.02em`: títulos, montos y KPIs.
- **Figtree** (400–700): todo el texto.

| Estilo | Tamaño / peso |
|---|---|
| Título de pestaña raíz (Catálogo, Mis eventos) | 26 / 800 |
| Título de pantalla con “volver” | 20 / 800 |
| Título de diálogo u hoja, estado vacío | 22 / 800 |
| Monto grande (recibo, reembolso) | 32–34 / 800 |
| KPI (web) | 30 / 800 |
| Cuerpo | 15 / 400–600 |
| Secundario | 13–14 / 400 en `muted` |
| Etiqueta de sección (`cap`) | 12 / 700, mayúsculas, `letter-spacing:.08em` |
| Etiqueta de campo (`lbl`) | 13 / 700 |

### 2.5 Forma, espaciado y sombras

| Elemento | Medidas |
|---|---|
| Márgenes de pantalla | 20 px a los lados; contenido desde y = 54 (debajo de la barra de estado) |
| Separación entre bloques | 12–14 px |
| Tarjeta | radio 20; personal `0 0 0 1px #F2EDF5, 0 14px 30px -20px rgba(200,80,170,.45)`; admin `0 0 0 1px #ECECF5, 0 12px 28px -22px rgba(70,80,180,.45)` |
| Botón | alto 54, radio 16, texto 16 / 700 |
| Botón fijo inferior | 20 px a los lados, 36 px sobre el borde |
| Botón icono (`ib`) | círculo de 44, fondo blanco |
| Chip | alto 36, radio completo |
| Campo | alto 50–52, radio 14–16 |
| Mosaico de icono (`ti`) | 40 × 40, radio 12, fondo de color suave |
| Etiqueta (`tag`) | alto 24, radio completo, 12 / 700 |
| Hoja inferior | radio 28 arriba, asa de 40 × 5, fondo atenuado `rgba(30,20,50,.45)` |
| Barra inferior | alto 96; Rosa de 56 px en el centro, sobresaliendo 26 px |
| Interruptor | 46 × 28 |

---

## 3. Componentes

| Componente | Props | Notas |
|---|---|---|
| `Logo` | `s` (tamaño) | Logo oficial **siempre sobre fondo blanco**: círculo blanco con anillo `rgba(234,78,169,.14)`. |
| `Rosa` | `s`, `glow`, `bg` | Mujer con audífonos y micrófono. `bg` es el degradado `hero` en personal y `rosa-admin-bg` en admin. |
| `TabBar` | `a` (inicio, catalogo, eventos, favoritos, paquetes, entrar), `dark` | Pestaña activa: píldora con el degradado `hero`. |
| `AdminBar` | `a` (inicio, eventos, inventario, mas) | Pestaña activa: píldora con `primary`. |
| `Icon` | `k`, `s`, `w` (grosor), `f` (relleno) | Trazo de 2 px y esquinas redondeadas. Se puede usar Lucide o Phosphor si lo prefieres. |
| `Foto` | `k`, `w`, `h`, `r`, `pos` | Fotos reales de producto y evento. Salen de `frontend/assets/images`, con la clave como nombre (`silla-tiffany`, `mesa-redonda`, `centro-floral`, `arco-rosa`, `mesa-helados`, `mesa-plegable`, `boda`, `quinceanera`, `cumple-infantil`, `baby-shower`, `graduacion`, `corporativo`…). |
| `Illo` | `k` | Ilustraciones **solo** para categorías, momentos y estados vacíos (`globos`, `candado`, `exito`, `correo`…). Los productos siempre llevan foto. El arco de globos se mantiene. |
| Tarjeta / Chip / Tag / Campo / Segmentado | — | Ver la sección 2.5. El chip activo usa `hero` (personal) o `primary` (admin). |
| Toast | texto + acción | Fondo `toast`, círculo lima con ✓ y acción en `#FFD45C`. Dura 4 s. En personal va sobre la barra inferior; en admin, arriba. |
| Banner sin conexión | — | Franja oscura sobre la barra de estado, con icono wifi tachado. |

---

## 4. Pantallas y flujos

### 4.1 App personal

**Acceso.** `p00` Splash → `s03`–`s05` Onboarding → `s06` Bienvenida → `s07` Iniciar sesión / `s08` Registro → `s11`, `s12` Confirmaciones → `s13` Sesión iniciada. Para recuperar la contraseña: `s09` → `s10`. Como invitado: `s01` Catálogo, `s02` Favoritos y `s75` (hoja que pide iniciar sesión).

**Descubrir.**
- `s14` / `s15` Inicio (claro/oscuro): la campana lleva a `s79` Notificaciones.
- `s18` Catálogo → `s80` Resultados de búsqueda → `s81` Filtros (hoja).
- `s19` Artículo · `s20` Categorías · `s21` / `s22` Paquetes · `s23` Tienda · `s24` Favoritos · `s16` / `s17` Acerca de y reseñas.

**Rosa IA.** `s26` Tipo de evento → `s27` / `s28` Ayuda → `s29` Sugerencias → `s30` Categorías → `s31` Detalles → `s32` Resumen → `s33` Enviado. Variantes: `s34` Chat y `s35` Voz. `s36` Boceto → `s78` Confirmar boceto (incluye “Ayúdame con IA”).

**Mis eventos.**
- `s37` Lista (vacía: `s89`) → `s38` Nuevo · `s39` Calendario.
- `s40` Detalle: el menú ⋮ lleva a `s84` Cambiar fecha / `s85` Cancelar.
- Dentro del evento: `s41` / `s42` Chat · `s43` Modo ejecución · `s44` Cronograma · `s45` Tareas · `s46` Checklist · `s47` Invitados.
- `s25` Mi evento (borrador).

**Cotización y pago.** `s48` Cotización → `s50` Pago → `s51` Confirmado → `s52` Post-evento. Además: `s49` Historial · `s53` Reservas · `s77` Reseña del evento.

**Cuenta.** `s54` Perfil →
- `s82` Mis pagos → `s83` Recibo.
- `s86` Ajustes → `s87` Términos y privacidad, `s88` Eliminar cuenta.

`s76` es la hoja de “Cómo planificar”.

### 4.2 App administrativa

**Entrada.** `A00` Splash → `A30` Elegir cómo entrar → `A01` Inicio (la campana lleva a `A29` Notificaciones).

**Crear.**
- `A04` Elegir modo:
  - `A08` Con Rosa IA.
  - Manual: `A05` Datos → `A06` Artículos → `A07` Resumen.
- `A02` Solicitudes (vacío: `A35`) → `A03` Cotizar → `A22` Historial de cotizaciones.

**Operar eventos.**
- `A11` Eventos → `A13` Detalle (toast de éxito: `A38`).
- Desde el detalle: `A15` Chat · `A16` Ejecución · `A17` Tareas · `A18` Boceto → plano · `A19` Cierre.
- `A14` Bandeja de chats.

**Dinero.** `A20` Pagos → `A21` Registrar pago. `A32` Cancelaciones → `A33` Procesar reembolso.

**Catálogo.** `A09` Inventario (error: `A37`) → `A23` Artículo / `A10` Nuevo. `A24` Paquetes → `A25` Editar. `s63` Disponibilidad.

**Personas.** `A26` Clientes → `A27` Ficha. `A28` Equipo y permisos.

**Más (`A12`).**
- `s58`–`s62` Panel · `s67`–`s70` Reportes.
- `s64`–`s66` Leads · `s55` / `s56` Proveedores.
- `s71` Reseñas → `A34` Responder reseña.
- `s72`–`s74` Bot de WhatsApp · `A31` Ajustes del negocio · `A39` Versión web.

---

## 5. Estados (aplican a todas las pantallas)

| Estado | Personal | Admin | Regla |
|---|---|---|---|
| Vacío | `s89` | `A35` | Ilustración o icono grande, título de 22, una frase y una o dos acciones. |
| Cargando | `s90` | `A36` | Esqueleto con la misma estructura que la pantalla y un brillo de izquierda a derecha de 1.2 s. Nada de spinners a pantalla completa. |
| Sin conexión / error | `s91` | `A37` | Banner oscuro arriba, “Reintentar” y guardado local. Error de servidor: “Algo salió mal de nuestro lado…”. |
| Éxito | `s92` | `A38` | Toast con acción (“Deshacer” o “Ver”). Las confirmaciones grandes (`s33`, `s51`) son pantallas completas. |
| Deshabilitado | `s88` | — | Botón con el color de su acción al 35 % de opacidad. |

---

## 6. Datos de ejemplo (los mismos en todo el diseño)

- **Boda Real Rosa & Dorado:** Camila Rodríguez · Vie 23 oct · Jardín Botánico · 120 invitados · 145 artículos · total RD$ 4,500 · anticipo 30 % RD$ 1,350 (transferencia, 18 oct) · saldo RD$ 3,150, vence el jue 22 oct.
- **Quinceañera Valentina Crystal:** Camila · Lun 2 nov · total RD$ 6,200 · pagado RD$ 1,860 (efectivo, 15 oct) · saldo RD$ 4,340, vence el vie 30 oct.
- **Cumpleaños de Mateo:** Laura Peña · Sáb 7 nov · 40 invitados · est. RD$ 405.
- **Graduación Promo 2026:** Ana Gómez · Vie 13 nov · est. RD$ 2,140.
- **Gala Corporativa Grupo Caribe:** 12 oct · RD$ 8,500.
- **Precios por día:** silla Tiffany 4 · mesa plegable 20 · mesa redonda 25 · centro floral 35 · arco de globos 95 · mesa dulce 150.
- **Negocio:** ingresos de octubre RD$ 12,450 · por cobrar RD$ 7,724 · 4 eventos activos · 2 solicitudes nuevas.
- **Política de cancelación (ejemplo):** más de 7 días antes, 90 %; de 3 a 7 días, 50 %; menos de 3 días, sin reembolso. El primer cambio de fecha es gratis.

Los textos legales de `s87` son de ejemplo y los debe redactar el negocio.

---

## 7. Notas de implementación

1. Usa los tokens de la sección 2 como variables de tema (`theme.personal` y `theme.admin`). No escribas colores sueltos en el código.
2. Moneda: `RD$ 1,350`, con separador de miles “,” y sin decimales, salvo en precios unitarios (`4.50`).
3. Fechas cortas en español: `Vie 23 oct`, `2:00 p. m.`.
4. Accesibilidad:
   - Zonas táctiles de al menos 44 px.
   - `aria-label` o `accessibilityLabel` en los botones de solo icono.
   - Contraste AA: el texto sobre tints usa siempre su color oscuro pareado.
5. El modo oscuro de la app personal está definido en `s15`. La app administrativa por ahora solo tiene modo claro.
6. Las hojas inferiores se cierran deslizando hacia abajo o tocando el fondo.
7. Al eliminar la cuenta, primero se valida que no haya eventos activos (`s88`).
