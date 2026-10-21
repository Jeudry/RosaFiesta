# Reglas del Negocio — RosaFiesta

Todas las reglas aquí descritas se implementan de forma pura y determinista en `core:engine` (Kotlin) y en `server/internal/*/domain` (Go), y cada tabla tiene prueba unitaria directa.

## 1. Precios por día del catálogo (`docs/design/entrega-de-diseno.md` sección 6)

| Clave (`Foto`) | Artículo | Precio por día (`RD$`) |
|---|---|---|
| `silla-tiffany` | Silla Tiffany | `RD$ 4` |
| `mesa-plegable` | Mesa plegable | `RD$ 20` |
| `mesa-redonda` | Mesa redonda | `RD$ 25` |
| `centro-floral` | Centro floral | `RD$ 35` |
| `arco-rosa` | Arco de globos | `RD$ 95` |
| `mesa-helados` | Mesa dulce | `RD$ 150` |

## 2. Anticipo (30 %) y Saldo (70 %)

- **Anticipo obligatorio:** `30 %` del total de la cotización aprobada (`deposit = round(total * 0.30)`).
- **Saldo pendiente:** `total - pagado`.
- **Vencimiento del saldo:** 1 a 2 días hábiles antes del evento (ej. Boda Real Rosa & Dorado el `Vie 23 oct` → vence el `jue 22 oct`; Quinceañera Valentina Crystal el `Lun 2 nov` → vence el `vie 30 oct`).

| Evento | Cliente | Fecha | Total | Anticipo (30 %) | Saldo (70 %) | Vencimiento |
|---|---|---|---|---|---|---|
| Boda Real Rosa & Dorado | Camila Rodríguez | Vie 23 oct | RD$ 4,500 | RD$ 1,350 | RD$ 3,150 | Jue 22 oct |
| Quinceañera Valentina Crystal | Camila | Lun 2 nov | RD$ 6,200 | RD$ 1,860 | RD$ 4,340 | Vie 30 oct |
| Cumpleaños de Mateo | Laura Peña | Sáb 7 nov | RD$ 405 | RD$ 122 | RD$ 283 | Vie 6 nov |
| Graduación Promo 2026 | Ana Gómez | Vie 13 nov | RD$ 2,140 | RD$ 642 | RD$ 1,498 | Jue 12 nov |
| Gala Corporativa Grupo Caribe | Grupo Caribe | Lun 12 oct | RD$ 8,500 | RD$ 2,550 | RD$ 0 (Pagado) | — |

## 3. Política de cancelación y reembolso (`s85`, `A32`, `A33`)

El porcentaje de reembolso sobre el monto pagado depende de los días completos de anticipación respecto a la fecha del evento:

| Anticipación antes del evento | Porcentaje de reembolso | Ejemplo sobre anticipo de RD$ 1,350 |
|---|---|---|
| Más de 7 días (`daysBefore > 7`) | **90 %** | `RD$ 1,215` |
| De 3 a 7 días (`3 <= daysBefore <= 7`) | **50 %** | `RD$ 675` |
| Menos de 3 días (`daysBefore < 3`) | **0 %** (sin reembolso) | `RD$ 0` |

## 4. Cambio de fecha (`s84`)

- **Primer cambio de fecha (`dateChangesCount == 0`):** Gratis (`RD$ 0`).
- **Cambios posteriores (`dateChangesCount >= 1`):** Cargo administrativo fijo o reajuste según disponibilidad.
- No se permite cambiar la fecha a un día en el pasado respecto al reloj inyectado (`Clock`).

## 5. Eliminación de cuenta (`s88`)

- Antes de eliminar la cuenta de un cliente, se valida que **no tenga eventos activos** (estados `draft`, `quote_pending`, `quote_ready`, `confirmed`, `in_execution`).
- Si tiene al menos un evento activo, la operación se rechaza con `ActiveEventsPreventDeletion` y el botón de eliminar permanece deshabilitado (al 35 % de opacidad en `s88`).

## 6. Roles y permisos del equipo (`A28`, `A30`)

- `cliente`: solo accede a la App Personal.
- `admin` (María, dueña): acceso total a la App Administrativa y puede entrar como cliente mediante `A30` para inspeccionar la App Personal.
- `equipo` (Pedro, Yuli, Andrés): accede a la App Administrativa según sus permisos configurados en `A28` (por ejemplo, Yuli tiene `decoracion` y `tareas`, pero **no** `pagos` ni `reembolsos`).
