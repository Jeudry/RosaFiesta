# Verificación E2E Cruzada (App Personal ↔ Backend/PostgreSQL/SSE ↔ App Admin)

**Fecha de ejecución:** 2026-11-25  
**Rama / Base:** `main` (auditorías y remediaciones previas `fd95abb`, `543b8c2`, `177a70c`, `139d367` + correcciones de esta corrida)

---

## 1. Resumen Ejecutivo

Se ejecutó un recorrido integral simultáneo entre **App Personal (cliente)** y **App Administrativa (staff/admin)** contra el servidor **Go + PostgreSQL 17 reales**, **dos flujos SSE `/v1/stream` concurrentes** y navegador real **WasmJS** (`http://localhost:8090`), complementado con pruebas de integración PostgreSQL (`make server-test-integration`), carrera Go (`go test -race -count=1 ./...`) y compilación multiplataforma (`JVM`, `WasmJs`, `Android`, `iOS Simulator Arm64`).

Durante dos rondas exhaustivas aparecieron **8 hallazgos de transición, seguridad, layout web y coherencia entre Personal y Admin** (5 documentados en Ronda 1 y 3 descubiertos en Ronda 2), todos **corregidos y re-verificados** hasta alcanzar **128/128 comprobaciones E2E en vivo en verde (`61/61` Ronda 1 + `67/67` Ronda 2, 0 fallos)** y verificación visual limpia en Chrome.

---

## 2. Entorno y Suites Ejecutadas

| Suite / Entorno | Alcance verificado | Resultado |
|---|---|---|
| **Ronda 1 — Recorrido E2E simultáneo (`http://localhost:8788`)** | Flujos cruzados Personal ↔ Admin (auth, ajustes ITBIS/zonas, eventos/croquis/checklist, cotizaciones/PDF, pagos/NCF/DGII 607, notificaciones, reseñas, chat) con PostgreSQL 17 + 2 SSE concurrentes | **61 PASS / 0 FAIL** |
| **Ronda 2 — Recorrido E2E avanzado (`http://localhost:8788`)** | Cancelación y reembolsos con liberación de stock, bloqueo de sobreventa y duplicación, `date-change`, incidencias de daño y `closeout`, RBAC granular de staff (`yuli` vs `maria`) + cola offline, invitados/cronograma/tareas/favoritos, handoff WhatsApp bot (`A39`), reportes y analítica | **67 PASS / 0 FAIL** |
| **Navegador WasmJS en vivo (`http://localhost:8090`)** | Personal (`s14`) y Admin (`A39`, modo `709x604` compacto y `1280x800` escritorio) vía WebView / Chrome DevTools MCP contra `:8788` real (`47/47` requests HTTP `200`, 0 errores JS/CORS) | **OK** |
| **`make openapi`** | Regeneración de [openapi.json](file:///Users/sargon/Documents/Coding/RosaFiesta/api/openapi.json) y validación contra DTOs KMP (`ContractTest`) | **OK** |
| **`make check`** | `tools/lint/test-names.sh`, `spotlessCheck`, `:core:engine:jvmTest`, `:core:domain:jvmTest`, `:core:data:jvmTest`, `:core:designsystem:jvmTest`, `:composeApp:jvmTest`, `golangci-lint` (0 incidencias), `go test ./...` | **OK** (100 % verde) |
| **`make server-test-integration`** | Pruebas de integración contra PostgreSQL 17 efímero (`rosafiesta-testdb` en `:55433`, migraciones 0001–0016 + repositorios reales) | **OK** |
| **`cd server && go test -race -count=1 ./...`** | Detector de condiciones de carrera en todos los paquetes Go del servidor | **OK** |
| **Compilación Multiplataforma App KMP** | `:composeApp:compileKotlinWasmJs`, `:composeApp:wasmJsBrowserDistribution`, `:composeApp:compileAndroidMain`, `:composeApp:compileKotlinIosSimulatorArm64` | **BUILD SUCCESSFUL** |

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

6. **Cola offline (`/v1/operations/offline-queue/sync`): brecha RBAC + desajuste de nombres de operación (Ronda 2)**
   - **Problema encontrado**: El adaptador `offlineDispatcherAdapter.DispatchMutation` solo reconocía nombres con punto (`checklist.toggle`, `payment.register`) mientras la app KMP (`SqlRosaFiestaRepository.kt`) encola nombres con guion bajo (`toggle_checklist`, `register_payment`, etc.). Además, la rama `default:` devolvía `nil` en lugar de error, permitiendo que una cuenta staff sin permiso `finanzas` (ej. `yuli@rosafiesta.do`) sincronizara operaciones desconocidas o privilegiadas (`VERIFY_PAYMENT`) recibiendo `200 synced: true`.
   - **Corrección**:
     - Añadida interfaz `OfflineMutationAuthorizer` (`AuthorizeMutation`) en [service.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/operations/app/service.go) y validación previa al encolado (`authorizeIncomingOfflineMutations`) en [ops_integrations.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/operations/app/ops_integrations.go).
     - Alineados los alias con punto y guion bajo, verificación explícita de permisos `pagos`/`finanzas` y rechazo `ErrOfflineActionForbidden` (`403 OFFLINE_ACTION_FORBIDDEN`) en rama `default:` dentro de [adapters.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/app/adapters.go).
     - Añadida prueba de regresión `TestOfflineQueueSyncRejectsUnauthorizedStaffAndUnknownOps` en [cross_app_e2e_test.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/app/cross_app_e2e_test.go).

7. **Colisión de JWT dentro del mismo segundo tras cambiar contraseña y re-iniciar sesión (Ronda 2)**
   - **Problema encontrado**: `jwtClaimsPayload` en [jwt.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/identity/domain/jwt.go) solo incluía `iat`/`exp` en segundos Unix enteros sin identificador único (`jti`). Si un usuario ejecutaba `POST /v1/auth/change-password` (que revoca hashes activos) y volvía a iniciar sesión (`POST /v1/auth/login`) dentro del mismo segundo reloj, el nuevo JWT era bit a bit idéntico al revocado y fallaba con `401` inmediatamente después.
   - **Corrección**: Añadido `jti` criptográfico aleatorio (`newTokenID()` de 16 bytes) en [jwt.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/identity/domain/jwt.go) más pruebas de regresión deterministas `TestTokenManagerIssuesUniqueTokensAtSameTimestamp` en [identity_test.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/identity/domain/identity_test.go) y `TestChangePasswordAndImmediateReloginWithinSameSecondSucceeds` en [pass3_identity_test.go](file:///Users/sargon/Documents/Coding/RosaFiesta/server/internal/identity/app/pass3_identity_test.go).

8. **Encabezado de Admin `A39` comprimido en viewport web compacto `709px` (WasmJS)**
   - **Problema encontrado**: Al inspeccionar `http://localhost:8090/?mode=admin` en navegador real a `709x604`, la barra de búsqueda fija de `340.dp` + fila de botones comprimía el título de fecha (`"25 de noviembre 2026"`) en una columna vertical de un carácter por línea y empujaba botones fuera de pantalla.
   - **Corrección**: Refactorizado `AdminDesktopTopBar` en [AdminAndWebScreens.kt](file:///Users/sargon/Documents/Coding/RosaFiesta/app/composeApp/src/commonMain/kotlin/com/jeudry/rosafiesta/ui/AdminAndWebScreens.kt) con `BoxWithConstraints` (`< 760.dp` apila título + campana, buscador a ancho completo y píldoras con scroll horizontal), manteniendo intacto el layout de escritorio (`1280x800`) y los snapshots aprobados; re-verificado visualmente en Chrome DevTools MCP (`709x604` y `1280x800`).

---

## 4. Matriz Detallada de Flujos Probados (Simultáneos Personal ↔ Admin)

| Flujo | Escenarios y validaciones probadas (positivo + negativo) | Tiempo Real (SSE) | Estado |
|---|---|---|---|
| **Salud y disponibilidad** | `GET /healthz` (200 `ok`), `GET /readyz` (200 `ready` con DB/almacenamiento real) | — | **PASS** |
| **Autenticación, perfiles, media y preferencias** | Login Admin (`maria`), registro Cliente A y B, bloqueo cliente en `/v1/admin/clients` (403), subida PNG `> 750 KiB` (201, sin 413), `PATCH /v1/auth/me` (`avatarKey`), `eventRemindersEnabled` ON/OFF persistente, cambio de contraseña válido/inválido, revocación de tokens antiguos (401), relogin en el mismo segundo con `jti` único, reset OTP inválido rechazado | Streams concurrentes Admin + Cliente A activos | **PASS** |
| **Configuración comercial y catálogo (`A31` ↔ Personal)** | Admin guarda `0%` ITBIS + `whatsappAutoAlertsEnabled=true` + nueva zona; Personal lee `itbisPercent=0` y zona nueva en `/v1/operations/settings/public`; Admin elimina zona y restaura `18%`; zona eliminada no resucita; catálogo disponible | Sincronización de ajustes verificada en `cross_app_e2e_test.go` (`catalog.updated`) | **PASS** |
| **Eventos, croquis (`A18`), montaje (`A16`) y checklist (`s43`/`s46`)** | Cliente A crea evento con `sketchJson` + `contactPhone`; Cliente B bloqueado (`403`); Admin actualiza invitados y `setupPhotoUrls` sin sobrescribir `sketchJson`; Cliente A lee su logística (`200`) y marca ítem de checklist (`200`); Cliente B bloqueado en lectura y PATCH checklist (`403`) | Admin recibe `event.updated` al crear evento y `logistics.updated` al marcar checklist; Cliente A recibe `event.updated` al actualizar montaje/fotos | **PASS** |
| **Cotizaciones, rechazo, firma y contrato PDF** | Cliente A solicita cotización con `contactPhone`; Admin la envía; Cliente A la rechaza con motivo; Admin la re-envía; Cliente A la firma (`signedByUserId`, `contractHashSha256`); doble firma rechazada (`409`); descarga PDF autenticado (`%PDF-`) | Admin recibe `quote.created`, `quote.declined` y `quote.signed`; Cliente A recibe `quote.sent` | **PASS** |
| **Pagos, rechazo/aprobación NCF y DGII 607** | Cliente A envía comprobante `> 750 KiB`; rechazo con motivo vacío bloqueado (`400`); rechazo con motivo válido (`rejected`); nuevo envío + aprobación Admin (`confirmed` + NCF `B02...`); exportación DGII 607 CSV válida y analítica mensual (`/v1/payments/analytics`) | Admin recibe `payment.pending_verification`; Cliente A recibe `payment.rejected`, `payment.verified` y `notification.created` | **PASS** |
| **Notificaciones y recordatorios (`ntf-`)** | Cliente A lista notificaciones reales `ntf-` generadas por pagos y las marca como leídas (`isRead=true`); con `eventRemindersEnabled=false`, `DispatchPaymentReminder` no genera notificaciones ni dispara alerta en `RosaFiestaViewModel` cuando `category=recordatorios` | `notification.created` entregado en vivo a Cliente A; `ScreenCatalogAndFlowTest` verifica gating por `category` | **PASS** |
| **Reseñas y verificación (`s71` ↔ `A34`)** | Cliente B bloqueado al intentar reseñar evento de Cliente A (IDOR `403`); Cliente A crea reseña; Admin responde primero con `verified=false` y luego `verified=true` validando persistencia real de ambos estados | `EventEventUpdated` / `EventCatalogUpdated` emitidos al crear y responder | **PASS** |
| **Mensajería en vivo (`s62`/`s63` ↔ `A39`) y Bot WhatsApp** | Cliente A escribe en hilo del evento y Admin responde en el mismo hilo; webhook WhatsApp con keyword de handoff crea hilo pausado (`botPaused=true`) en `A39` | Admin y Cliente A reciben `chat.message` bidireccional e inbound WhatsApp por SSE | **PASS** |
| **Cancelación, reembolsos y liberación de stock (Ronda 2)** | Reserva de stock completo + depósito verificado; disponibilidad en `0`; cancelación de evento con cálculo de `refundRD`; liberación inmediata del stock en la fecha; listado y aprobación en `/v1/refunds`; cliente bloqueado (`403`); doble aprobación rechazada (`409`) | Cliente A recibe `notification.created` tras aprobación de reembolso | **PASS** |
| **Sobreventa, cambio de fecha, duplicación, daños y closeout (Ronda 2)** | Sobreventa en fecha llena rechazada; `date-change` libera fecha anterior y bloquea IDOR (`403`); duplicación bloquea sobreventa (`409`) o clona en fecha libre reseteando depósito; reporte/resolución de daños incrementa y restaura `damagedStock`; `closeout` restringido a Admin (`403` cliente) | Admin recibe `damage.reported` por SSE en vivo | **PASS** |
| **RBAC granular de Staff y cola offline (Ronda 2)** | `yuli` (`decoracion,tareas`) lee logística y queda bloqueada (`403`) en `dgii-607`, `/v1/refunds`, `PUT /v1/operations/settings`, `/v1/admin/staff` y `VERIFY_PAYMENT` en cola offline; otorgar/revocar `finanzas` y `reembolsos` surte efecto inmediato sin re-login | — | **PASS** |
| **Sub-flujos de evento, favoritos y reportes (Ronda 2)** | CRUD de invitados (`guests`), cronograma (`schedule`), tareas (`tasks`) y favoritos (`/v1/auth/me/favorites`) con aislamiento IDOR `403` para Cliente B; exportación de reporte operativo `/v1/reports/export` | — | **PASS** |
| **Web WasmJS en vivo (`Personal s14` y `Admin A39`)** | Render Skiko Canvas en `:8090` contra API `:8788` en vivo; validación visual responsiva `709x604` y `1280x800`; `47/47` recursos/API HTTP `200`, 0 errores CORS o de consola | — | **PASS** |

---

## 5. Qué Queda Exclusivamente para Entornos / Dispositivos Físicos Reales

Las siguientes verificaciones dependen de credenciales productivas o hardware externo que no se sustituyen en un entorno local:
1. **Dispositivos físicos iOS y Android**: captura real con cámara/micrófono para STT, notificaciones push nativas APNs/FCM y comportamiento táctil/modo oscuro en pantalla física.
2. **Pasarela Stripe, SMTP y OAuth externos en vivo**: cobro real con webhook firmado (`STRIPE_WEBHOOK_SECRET`), envío real de correos OTP/recibos (`SMTP_HOST`) y flujo completo Apple/Google OAuth con dominios reales en Caddy.
3. **Despliegue Caddy + Docker en servidor (`app.` / `admin.` / `api.rosafiesta.do`)**: validar certificados TLS, cabeceras de proxy (`TRUSTED_PROXIES`), descarga WasmJS y ejecución de `backup.sh` / `restore.sh` sobre el VPS/servidor destino.
