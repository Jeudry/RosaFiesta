# RosaFiesta Admin App — SPEC.md

> **App name**: RosaFiesta Admin (customer-facing app is just "RosaFiesta" / "RosaFiesta App")

## Concept & Vision

Una app de gestión interna para el equipo de RosaFiesta — no para clientes. Interfaz profesional pero con la misma personalidad cálida de la marca: colores ligeramente más oscuros que la app de cliente, bordes más definidos, más densidad de información. El admin vive frente a esta pantalla todo el día — debe ser eficiente, no bonita. Funciones pesadas primero, diseño al final.

---

## Diferencias visuales con la app de cliente

| Elemento | App Cliente | App Admin |
|---|---|---|
| Color primario | Hot pink `#FF3CAC` | Más oscuro `#D42A8F` |
| Acento | Coral `#FF6B6B` | Más profundo `#E05555` |
| Superficie | Glassmorphism suave | Bordes más definidos, cards más densas |
| Navegación | Bottom tabs (3-4) | Side drawer + tabs, más opciones |
| Tipografía | GoogleFonts.outfit bold | Same pero más uso de mono para datos |

---

## Módulos

### 1. Dashboard / Home

- **Métricas rápido**: eventos de hoy, eventos esta semana, ingresos del mes, cotizaciones pendientes
- **Alertas críticas**: pagos vencidos, eventos mañana sin confirmar, stock bajo
- **Accesos rápidos**: nuevo evento, buscar cliente, ver eventos pendientes
- **Mini-calendario**: preview de eventos próximos (7 días)

### 2. Gestión de Eventos (Admin)

- Lista de TODOS los eventos (filtros: estado, fecha, cliente)
- Cada evento muestra: cliente, fecha, estado, total, pendiente de pago
- **Detalle completo** — mismo que ve el cliente pero editable:
  - Modificar fecha, hora, dirección, notas
  - Agregar/quitar items del catálogo
  - Ajustar cotización (change quote → status = "adjusted")
  - Cambiar estado manualmente (draft → pending → confirmed → paid → completed)
  - Subir fotos al evento
  - Ver guest list + RSVP status
  - Generar/descargar contrato PDF
  - Notas internas del equipo (visibles solo para admin)

### 3. Gestión de Productos / Catálogo

- CRUD completo de artículos: nombre, descripción, categoría, variantes, precios
- **Precios rental vs venta** — editar ambos
- **Imágenes** — upload, reorder, delete
- **Stock por variante** — ajustar disponibilidad manualmente
- **low_stock_threshold** — editable por artículo
- **Categorías** — CRUD completo
- **Bundles** — crear/editar themed bundles con items ajustables
- Búsqueda rápida con filtros
- Toggle: activar/desactivar artículo (no delete, solo deactivate)

### 4. Cotizaciones (Quote Workflow)

- **Todas las cotizaciones** — pendientes, ajustadas, aprobadas, rechazadas
- Filtro por estado: `pending_quote`, `adjusted`, `paid`, `rejected`
- **Crear cotización nueva** para cliente (existente o nuevo):
  - Buscar cliente por nombre/email/teléfono
  - Si no existe → crear usuario temporal con solo nombre+teléfono (el cliente se registra después para pagar)
  - Agregar items manualmente o usar IA para generar propuesta
  - AI Assistant: el admin describe el evento → la IA propone items → admin ajusta → guarda como quote
  - Ver preview de la cotización antes de enviar
  - "Enviar al cliente" → se crea evento con status `pending_quote` y se notifica al cliente por WhatsApp/email
- **Aprobar/rechazar cotizaciones** desde la lista
- Historial de cotizaciones por cliente

### 5. Clientes

- Lista de todos los usuarios registrados
- Datos: nombre, email, teléfono, eventos, total gastado
- **Cliente sin registro**: cuando admin crea cotización para alguien nuevo, se guarda como "lead" (nombre + teléfono únicamente) — aparece en la lista de clientes con badge "Lead"
- Ver eventos de cada cliente
- Ver historial de pagos
- Bloquear/desbloquear usuario
- Agregar nota interna sobre el cliente

### 6. AI Assistant (Rosa IA) — Config

- **Mensajes y prompts**: el admin puede editar los mensajes del flow de la IA
  - Mensaje de bienvenida
  - Preguntas del flow (7 pasos)
  - Respuestas automáticas
  - Mensaje de confirmación final
- **Training data**: qué productos mostrar por defecto
- **Behavior flags**: permitir/confiar cotizar sin aprobación, montos mínimos para auto-approve
- **Historial de conversaciones**: ver las conversaciones que los usuarios tuvieron con Rosa IA (para mejorar)

### 7. Notificaciones — Config

- **Plantillas de email** — editar contenido de:
  - Invitación de registro (user_invitation.tmpl)
  - Recordatorio 7 días
  - Recordatorio 24h
  - Agradecimiento post-evento
  - Reset password
  - Contrato confirmado
- **Plantillas de WhatsApp** — editar mensajes automáticos
- **Toggle por notificación**: activar/desactivar cada tipo
- **Test**: enviar email de prueba a sí mismo

### 8. Analytics / Reportes

- **Resumen del mes**: eventos completados, ingresos, nuevos clientes
- **Gráfico de ingresos** por mes (últimos 12 meses)
- **Eventos por estado**: distribución (draft, pending, confirmed, paid, completed)
- **Productos más alquilados**: top 10
- **Tasa de conversión**: quotes enviados → pagados
- **Clientes nuevos por mes**
- **Pagos pendientes**: lista de installment payments vencidos
- **Exportar CSV**: datos crudos para excel
- **Reporte PDF**: resumen mensual formal para RosaFiesta

### 9. Configuración General

- **Delivery zones**: editar radios, tarifas, zonas
- **Métodos de pago**: activar/desactivar transferencia, efectivo, tarjeta
- **Horarios de trabajo**: cuándo ocurre el montaje/desmontaje
- **Mi cuenta**: cambiar password admin, nombre, email

### 10. Log de Actividad (Audit Trail)

- Todas las acciones de admins: quién cambió qué, cuándo
- Filtros: por admin, por tipo de acción, por fecha
- ej: "Jeudry ajustó cotización del cliente María — RD$500 → RD$600 — hace 2 horas"

---

## Mockups de Navegación

```
Drawer (hamburger menu):
├── Dashboard
├── Eventos
│   ├── Todos los eventos
│   ├── Crear nuevo evento
│   └── Estados: Pending / Confirmados / Completados
├── Cotizaciones
│   ├── Pendientes
│   ├── Ajustadas
│   ├── Creadas por mí
│   └── Crear nueva
├── Clientes
│   ├── Registrados
│   └── Leads
├── Productos
│   ├── Catálogo
│   ├── Categorías
│   └── Bundles
├── IA Rosa
│   ├── Config
│   └── Historial de conversaciones
├── Notificaciones
│   ├── Email templates
│   ├── WhatsApp templates
│   └── Config de triggers
├── Analytics
│   ├── Resumen
│   ├── Reporte mensual
│   └── Exportar datos
├── Config
│   ├── Delivery zones
│   ├── Métodos de pago
│   └── Mi cuenta
└── Log de actividad
```

---

## Technical Stack

- **Framework**: Flutter (separated from main frontend repo — new project under `/admin_app/`)
- **State**: Provider + BLoC for complex screens
- **Backend integration**: same API, authenticated with admin JWT role
- **Auth**: same JWT, role = "admin" or "moderator" required
- **Web**: deployable as web app for desktop/laptop use

---

## Flujo de Cotización Admin (detalle)

1. Admin abre "Crear cotización"
2. Busca cliente (existente o nuevo lead)
3. Define: fecha evento, tipo, ubicación
4. **Opción A — Manual**: busca artículos en catálogo, agrega uno por uno
5. **Opción B — IA**: describe el evento → la IA sugiere items → admin ajusta cantidades
6. Admin ve subtotal, aplica descuento si quiere
7. Guarda como "cotización" → se crea evento en status `pending_quote`
8. Envía por WhatsApp al cliente (link con la cotización)
9. Cliente aprueba → status `paid`
10. Admin puede cobrar presencial/transferencia y marcar como pagado

---

## Notas de implementación

- El backend ya tiene `adjustQuote` handler — admin accede via `PATCH /admin/events/{id}/adjust`
- Las rutas `POST /v1/admin/*` requieren middleware `RoleMiddleware("admin")`
- Para clientes sin registro: crear usuario con `is_active = false` y solo teléfono — el cliente completa registro después
- AI config: guardar prompts/mensajes en tabla `config` (key-value simple) o archivo JSON en el servidor
- Email templates editables: guardar en DB, no en archivo, para que el admin pueda cambiarlos desde la app

---

## TODO / PENDING

- [ ] Crear nuevo proyecto Flutter en `/admin_app/`
- [ ] Auth + role middleware
- [ ] Dashboard con métricas
- [ ] Eventos: lista + detalle editable
- [ ] Cotizaciones: crear, listar, enviar
- [ ] AI config: editar mensajes
- [ ] Notificaciones: editar templates
- [ ] Analytics + export
- [ ] Clientes + leads
- [ ] Productos CRUD
- [ ] Audit log viewer
- [ ] Deploy web app