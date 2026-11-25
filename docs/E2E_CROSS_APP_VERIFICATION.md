# Verificación E2E Cruzada (App Personal ↔ Backend/PostgreSQL/SSE ↔ App Admin)

**Fecha de ejecución:** 2026-11-25  
**Rama / Base:** `main` (auditorías y remediaciones previas `fd95abb`, `543b8c2`, `177a70c`, `139d367` + correcciones de esta corrida)

---

## 1. Resumen Ejecutivo

Se ejecutó un recorrido integral simultáneo entre **App Personal (cliente)** y **App Administrativa (staff/admin)** contra el servidor **Go + PostgreSQL 17 reales** y **dos flujos SSE `/v1/stream` concurrentes** (sesión Cliente A y sesión Admin María conectadas al mismo tiempo, más Cliente B para pruebas de aislamiento/IDOR), complementado con pruebas de integración PostgreSQL (`make server-test-integration`), carrera Go (`go test -race -count=1 ./...`) y compilación multiplataforma (`JVM`, `WasmJs`, `Android`, `iOS Simulator Arm64`).

Durante el recorrido real aparecieron **6 hallazgos de transición, validación y coherencia entre Personal y Admin** (detallados en la sección 3), los cuales fueron **corregidos y re-verificados** en esta misma entrega hasta alcanzar **61/61 comprobaciones E2E en vivo en verde (0 fallos)**.

---

## 2. Entorno y Suites Ejecutadas

| Suite / Entorno | Alcance verificado | Resultado |
|---|---|---|
| **Recorrido E2E simultáneo en vivo (`http://localhost:8788`)** | Flujos cruzados Personal ↔ Admin con PostgreSQL 17 (`rosafiesta-db`) + 2 listeners SSE concurrentes | **61 PASS / 0 FAIL** |
| **`make openapi`** | Regeneración de [openapi.json](file:///Users/sargon/Documents/Coding/RosaFiesta/api/openapi.json) y validación contra DTOs KMP (`ContractTest`) | **OK** |
| **`make check`** | `tools/lint/test-names.sh`, `spotlessCheck`, `:core:engine:jvmTest`, `:core:domain:jvmTest`, `:core:data:jvmTest`, `:core:designsystem:jvmTest`, `:composeApp:jvmTest`, `golangci-lint` (0 incidencias), `go test ./...` | **OK** (100 % verde) |
| **`make server-test-integration`** | Pruebas de integración contra PostgreSQL 17 efímero (`rosafiesta-testdb` en `:55433`, migraciones 0001–0016 + repositorios reales) | **OK** |
| **`cd server && go test -race -count=1 ./...`** | Detector de condiciones de carrera en todos los paquetes Go del servidor | **OK** |
| **Compilación Multiplataforma App KMP** | `:composeApp:compileKotlinWasmJs`, `:composeApp:compileAndroidMain`, `:composeApp:compileKotlinIosSimulatorArm64` | **BUILD SUCCESSFUL** |

---

## 3. Hallazgos Detectados y Corregidos Durante el Recorrido

1. **Desfase de ITBIS entre Admin (`A31`) y Personal (`s50` / checkout / contrato)**
   - **Problema encontrado**: Admin podía guardar `itbisPercent = 0%` (o cualquier tasa custom) en `PUT /v1/operations/settings`, pero `GET /v1/operations/settings/public` devolvía `PublicBusinessSettings` **sin el campo `itbisPercent`**. Como resultado, la App Personal (`RosaFiestaBackendMappers.fromPublicSettings`) heredaba el `18%` por defecto y calculaba montos y cláusulas legales desfasados frente al servidor. Además, `UpdateSettings` no emitía evento SSE para refrescar sesiones abiertas.
   - **Corrección**:
     - Se añadió `ItbisPercent int` a `PublicBusinessSettings` en [backoffice.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/operations/domain/backoffice.go) y `itbisPercent` en [RosaFiestaApiExtendedModels.kt](file:///Users/sargon/Documents/Coding/RosaFiesta/app/core/data/src/commonMain/kotlin/com/jeudry/rosafiesta/data/RosaFiestaApiExtendedModels.kt).
     - Se corrigió `fromPublicSettings` en [RosaFiestaBackendMappers.kt](file:///Users/sargon/Documents/Coding/RosaFiesta/app/core/data/src/commonMain/kotlin/com/jeudry/rosafiesta/data/RosaFiestaBackendMappers.kt) para usar `dto.itbisPercent`.
     - `UpdateSettings` en [backoffice.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/operations/app/backoffice.go) ahora emite `catalog.updated` (`AudienceAll`) con `settings.updated` para sincronizar ITBIS, zonas y depósito en vivo.

2. **Eventos nuevos sin logística ni checklist (`404 LOGISTICS_NOT_FOUND` y `403 FORBIDDEN_PERMISSION` en Personal)**
   - **Problema encontrado**: Al crear un evento nuevo desde Personal (`POST /v1/events`), no se inicializaba su registro en `event_logistics` ni su checklist por defecto (`buildDefaultEventChecklist`), provocando `404 LOGISTICS_NOT_FOUND` al abrir el checklist o la logística hasta que se firmara una cotización aparte. Adicionalmente, tras abrir el `PATCH` del checklist al cliente dueño, el endpoint de lectura `GET /v1/operations/events/{eventId}` seguía restringido solo a permisos de staff (`fieldMW`), devolviendo `403` cuando Personal intentaba sincronizar el checklist real del evento.
   - **Corrección**:
     - Se creó `EnsureDraftLogistics` en [ops_integrations.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/operations/app/ops_integrations.go) (crea logística y checklist sin ensuciar paradas de ruta antes de confirmar; y al confirmar, `EnsureLogisticsForConfirmedEvent` agrega las paradas faltantes).
     - Se conectó vía `LogisticsProvisioner` en [service.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/events/app/service.go), [adapters.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/app/adapters.go) y [modules.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/app/modules.go) al crear eventos.
     - Se habilitó `authMW` + `GetEventLogisticsForCaller` en [handler.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/operations/httpapi/handler.go) y [service.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/operations/app/service.go) validando dueño del evento o staff autorizado (`403` para terceros).
     - `toggleChecklistItemAuthorized` ahora emite SSE `logistics.updated` para sincronizar Admin y Personal al instante.

3. **Brechas de eventos SSE entre Go y `RosaFiestaViewModel` + filtro real de `Recordatorios`**
   - **Problema encontrado**:
     - El servidor definía `quote.created`, pero `CreateQuoteForCaller` nunca lo publicaba al Admin; `DeclineQuoteForCaller` tampoco emitía evento SSE ni existía `quote.declined`; y `CreateReview`/`ReplyReviewWithVerification` no notificaban en tiempo real.
     - En [RosaFiestaViewModel.kt](file:///Users/sargon/Documents/Coding/RosaFiesta/app/composeApp/src/commonMain/kotlin/com/jeudry/rosafiesta/ui/RosaFiestaViewModel.kt), `handleRealtimeStreamEvent()` escuchaba `"whatsapp.message"` en lugar del evento real `"chat.message"`, ignoraba `"quote.created"`, `"quote.sent"`, `"quote.declined"`, `"payment.pending_verification"`, `"logistics.updated"` y `"damage.reported"`, y `RealtimeStreamEvent` no leía `payload.category`, impidiendo silenciar avisos locales de recordatorios emitidos como `notification.created` cuando `Recordatorios` estaba apagado.
   - **Corrección**:
     - Añadido `EventQuoteDeclined` en [realtime.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/realtime/domain/realtime.go) y publicaciones completas en [quotes/app/service.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/quotes/app/service.go) y [operations/app/backoffice.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/operations/app/backoffice.go).
     - Añadido `category` en [RosaFiestaExtendedModels.kt](file:///Users/sargon/Documents/Coding/RosaFiesta/app/core/domain/src/commonMain/kotlin/com/jeudry/rosafiesta/domain/RosaFiestaExtendedModels.kt), `payload` en `RealtimeEventDto` y extracción de `category` en [RosaFiestaBackendMappers.kt](file:///Users/sargon/Documents/Coding/RosaFiesta/app/core/data/src/commonMain/kotlin/com/jeudry/rosafiesta/data/RosaFiestaBackendMappers.kt).
     - Actualizado `handleRealtimeStreamEvent()` en [RosaFiestaViewModel.kt](file:///Users/sargon/Documents/Coding/RosaFiesta/app/composeApp/src/commonMain/kotlin/com/jeudry/rosafiesta/ui/RosaFiestaViewModel.kt) para atender `"chat.message"` (y `"whatsapp.message"`), diferenciar avisos por rol (Admin vs Personal) y aplicar la compuerta de `remindersEnabled` sobre notificaciones con categoría `recordatorios`/`reminder`.

4. **`DispatchPaymentReminder` ignoraba `EventRemindersEnabled`**
   - **Problema encontrado**: [adapters.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/app/adapters.go) validaba `PushNotificationsEnabled`, pero ignoraba `EventRemindersEnabled` y registraba la notificación como `"pagos"` en vez de `"recordatorios"`.
   - **Corrección**: Ahora requiere ambos flags (`PushNotificationsEnabled` y `EventRemindersEnabled`) para despachar push/notificación de recordatorio y usa categoría `"recordatorios"`.

5. **Rechazo de transferencia con motivo en blanco aceptado silenciosamente (`POST /v1/payments/{id}/reject`)**
   - **Problema encontrado**: Aunque la UI (`AdminRejectPaymentDialog`) exigía al menos 4 caracteres, el backend aplicaba un fallback genérico `"Comprobante rechazado por finanzas"` si recibía `"reason": "   "`, permitiendo rechazos irreversibles sin explicación real vía API.
   - **Corrección**: Añadido `ErrInvalidRejectionReason` (`INVALID_REJECTION_REASON`, HTTP 400) en [payments.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/payments/domain/payments.go), [dto.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/payments/httpapi/dto.go) y [fiscal_gateway.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/payments/app/fiscal_gateway.go) exigiendo al menos 4 caracteres no vacíos.

---

## 4. Matriz Detallada de Flujos Probados (Simultáneos Personal ↔ Admin)

| Flujo | Escenarios y validaciones probadas (positivo + negativo) | Tiempo Real (SSE) | Estado |
|---|---|---|---|
| **Salud y disponibilidad** | `GET /healthz` (200 `ok`), `GET /readyz` (200 `ready` con DB/almacenamiento real) | — | **PASS** |
| **Autenticación, perfiles, media y preferencias** | Login Admin (`maria`), registro Cliente A y B, bloqueo cliente en `/v1/admin/clients` (403), subida PNG `> 750 KiB` (201, sin 413), `PATCH /v1/auth/me` (`avatarKey`), `eventRemindersEnabled` ON/OFF persistente, cambio de contraseña válido/inválido, revocación de tokens antiguos (401), reset OTP inválido rechazado | Streams concurrentes Admin + Cliente A activos | **PASS** |
| **Configuración comercial y catálogo (`A31` ↔ Personal)** | Admin guarda `0%` ITBIS + `whatsappAutoAlertsEnabled=true` + nueva zona; Personal lee `itbisPercent=0` y zona nueva en `/v1/operations/settings/public`; Admin elimina zona y restaura `18%`; zona eliminada no resucita; catálogo disponible | Sincronización de ajustes verificada en `cross_app_e2e_test.go` (`catalog.updated`) | **PASS** |
| **Eventos, croquis (`A18`), montaje (`A16`) y checklist (`s43`/`s46`)** | Cliente A crea evento con `sketchJson` + `contactPhone`; Cliente B bloqueado (`403`); Admin actualiza invitados y `setupPhotoUrls` sin sobrescribir `sketchJson`; Cliente A lee su logística (`200`) y marca ítem de checklist (`200`); Cliente B bloqueado en lectura y PATCH checklist (`403`) | Admin recibe `event.updated` al crear evento y `logistics.updated` al marcar checklist; Cliente A recibe `event.updated` al actualizar montaje/fotos | **PASS** |
| **Cotizaciones, rechazo, firma y contrato PDF** | Cliente A solicita cotización con `contactPhone`; Admin la envía; Cliente A la rechaza con motivo; Admin la re-envía; Cliente A la firma (`signedByUserId`, `contractHashSha256`); doble firma rechazada (`409`); descarga PDF autenticado (`%PDF-`) | Admin recibe `quote.created`, `quote.declined` y `quote.signed`; Cliente A recibe `quote.sent` | **PASS** |
| **Pagos, rechazo/aprobación NCF y DGII 607** | Cliente A envía comprobante `> 750 KiB`; rechazo con motivo vacío bloqueado (`400`); rechazo con motivo válido (`rejected`); nuevo envío + aprobación Admin (`confirmed` + NCF `B02...`); exportación DGII 607 CSV válida | Admin recibe `payment.pending_verification`; Cliente A recibe `payment.rejected`, `payment.verified` y `notification.created` | **PASS** |
| **Notificaciones y recordatorios (`ntf-`)** | Cliente A lista notificaciones reales `ntf-` generadas por pagos y las marca como leídas (`isRead=true`); con `eventRemindersEnabled=false`, `DispatchPaymentReminder` no genera notificaciones ni dispara alerta en `RosaFiestaViewModel` cuando `category=recordatorios` | `notification.created` entregado en vivo a Cliente A; `ScreenCatalogAndFlowTest` verifica gating por `category` | **PASS** |
| **Reseñas y verificación (`s71` ↔ `A34`)** | Cliente B bloqueado al intentar reseñar evento de Cliente A (IDOR `403`); Cliente A crea reseña; Admin responde primero con `verified=false` y luego `verified=true` validando persistencia real de ambos estados | `EventEventUpdated` / `EventCatalogUpdated` emitidos al crear y responder | **PASS** |
| **Mensajería en vivo (`s62`/`s63` ↔ `A39`)** | Cliente A escribe en hilo del evento; Admin responde en el mismo hilo; `RosaFiestaViewModel` reacciona a `chat.message` | Admin y Cliente A reciben `chat.message` bidireccional por SSE | **PASS** |

---

## 5. Qué Queda Exclusivamente para Entornos / Dispositivos Físicos Reales

Las siguientes verificaciones dependen de credenciales productivas o hardware externo que no se sustituyen en un entorno local:
1. **Dispositivos físicos iOS y Android**: captura real con cámara/micrófono para STT, notificaciones push nativas APNs/FCM y comportamiento táctil/modo oscuro en pantalla física.
2. **Pasarela Stripe, SMTP y OAuth externos en vivo**: cobro real con webhook firmado (`STRIPE_WEBHOOK_SECRET`), envío real de correos OTP/recibos (`SMTP_HOST`) y flujo completo Apple/Google OAuth con dominios reales en Caddy.
3. **Despliegue Caddy + Docker en servidor (`app.` / `admin.` / `api.rosafiesta.do`)**: validar certificados TLS, cabeceras de proxy (`TRUSTED_PROXIES`), descarga WasmJS y ejecución de `backup.sh` / `restore.sh` sobre el VPS/servidor destino.
