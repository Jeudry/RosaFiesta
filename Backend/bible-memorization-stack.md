# Stack para Proyecto de Memorización Bíblica

Documento de referencia para un futuro proyecto personal enfocado en **memorizar la Biblia**. El objetivo es diferenciarse del stack actual (RosaFiesta: Go + chi + SQL crudo + Flutter + Provider) y aprender tecnologías con alta demanda laboral.

---

## Concepto del producto

App offline-first para memorizar versículos usando un sistema **SRS (Spaced Repetition System)** tipo Anki, con:

- Búsqueda semántica de versículos por significado (no solo texto).
- Historial completo de repasos (event sourcing).
- Recordatorios locales de repaso.
- LLM integrado para generar preguntas de comprensión sobre lo memorizado.
- Sincronización multi-dispositivo.

---

## Backend (Go)

### Core
- **Go 1.24+**
- **Connect-RPC** (Buf) o **Huma** — contratos fuertes + OpenAPI automático. Alternativa moderna a chi + REST puro.
- **sqlc** — genera código Go tipado desde SQL. Lo piden mucho más que ORMs hoy.
- **PostgreSQL** con extensiones:
  - **pgvector** → embeddings para búsqueda semántica de versículos.
  - **LISTEN/NOTIFY** → eventos en tiempo real.
- **Redis / Valkey** — cache + streams para eventos.
- **Goose** o **golang-migrate** — migraciones.

### Arquitectura
**Modular Monolith + Hexagonal (Ports & Adapters) + CQRS ligero**

```
cmd/
  api/
internal/
  memorization/          # módulo
    domain/              # entities, value objects, events
    application/         # use cases (commands + queries separados)
    ports/               # interfaces (repos, eventbus)
    adapters/
      persistence/       # sqlc + postgres
      http/              # handlers connect-rpc
  verses/                # módulo
    ...
  shared/
    eventbus/
    telemetry/
```

- **Commands** (write side): `StartMemorizationSession`, `RecordReview`, `MarkVerseAsLearned`.
- **Queries** (read side): proyecciones denormalizadas para dashboards.
- **Domain events**: `VerseMemorized`, `ReviewCompleted`, `StreakBroken` — persistidos como event store para reconstruir el progreso histórico.

### Observabilidad y Ops
- **OpenTelemetry** (traces + metrics + logs).
- **Prometheus + Grafana** — dashboards.
- **Docker** + **Kubernetes** (kind local, luego GKE/EKS).
- **GitHub Actions** — CI/CD.
- **Terraform** — IaC desde el día uno.

### IA / LLM
- **Ollama** local (Llama 3, Mistral) o **Claude API** para:
  - Generar preguntas de comprensión sobre versículos memorizados.
  - Explicar contexto histórico/teológico.
  - Sugerir versículos relacionados semánticamente.
- **Embeddings** con `nomic-embed-text` o similar → guardar en `pgvector`.

---

## Frontend (Flutter)

### Core
- **Flutter 3.x** (última estable)
- **Riverpod 2.x** con code generation — reemplaza a Provider, muy pedido.
- **go_router** — routing declarativo (más profesional que hash routing).
- **Freezed** + **json_serializable** — modelos inmutables y unions.
- **flutter_hooks** — combina bien con Riverpod.

### Persistencia local (clave para offline-first)
- **Drift** (SQL tipado sobre SQLite) o **Isar / ObjectBox** (NoSQL rápido).
- Toda la biblia + progreso de memorización cacheado localmente.
- Sincronización diferida con el backend vía cola local.

### UX / UI
- **Rive** o **Lottie** — animaciones fluidas de progreso.
- **GoogleFonts** — tipografía.
- **flutter_local_notifications** + **workmanager** — recordatorios de repaso SRS en background.

### Red
- **Dio** con interceptores (auth, retry, logging).
- **connect_dart** si usas Connect-RPC en el backend → contratos compartidos.

### Testing
- **Patrol** o **integration_test** — E2E.
- **mocktail** — mocks.
- **golden tests** — regresión visual.

### Monorepo
- **Melos** — gestionar múltiples paquetes Dart (core, features, ui-kit).

---

## Algoritmo SRS (lógica de dominio)

Implementar **SM-2** (el de Anki clásico) o **FSRS** (más moderno):

- Cada versículo tiene: `easeFactor`, `interval`, `repetitions`, `nextReviewAt`.
- Tras cada repaso el usuario califica (Again / Hard / Good / Easy).
- El algoritmo recalcula el próximo intervalo.
- Los eventos de repaso se guardan como event store → reconstruible cualquier momento.

---

## Servicios externos opcionales

- **Supabase** o **Firebase Auth** — si no quiero implementar auth desde cero.
- **Sentry** — error tracking.
- **PostHog** — analytics self-hosted.
- **Cloudflare R2** / **S3** — audio de versículos narrados.

---

## Roadmap de aprendizaje sugerido

1. **Fase 1 — Backend core**
   Connect-RPC + sqlc + Postgres + arquitectura hexagonal con un solo módulo (verses).
2. **Fase 2 — Dominio SRS**
   CQRS + event sourcing para el historial de repasos.
3. **Fase 3 — Flutter offline-first**
   Riverpod + Drift + go_router + sincronización.
4. **Fase 4 — IA**
   pgvector + embeddings + LLM para preguntas generadas.
5. **Fase 5 — Ops**
   OpenTelemetry + Docker + Kubernetes + Terraform + CI/CD.
6. **Fase 6 — Pulido**
   Animaciones Rive, notificaciones SRS, tests E2E con Patrol.

---

## Por qué este stack diferencia en el mercado

| Tecnología | Por qué suma al CV |
|---|---|
| sqlc | Reemplaza ORMs, muy pedido en ofertas Go 2025 |
| Connect-RPC / gRPC | Estándar en microservicios modernos |
| Hexagonal + CQRS + Event Sourcing | Demuestra criterio de arquitectura senior |
| pgvector + LLM | Te mete en la ola IA sin ser "AI engineer" |
| OpenTelemetry | Observabilidad seria, muy valorada |
| Kubernetes + Terraform | Roles mid/senior casi siempre lo piden |
| Riverpod + go_router | Provider ya se considera básico |
| Drift offline-first | Pocos candidatos tienen apps offline reales |
| FSRS / SM-2 | Lógica de dominio no trivial = proyecto memorable |

---

## Notas finales

- Empezar **pequeño**: un solo módulo, un solo versículo, un solo repaso. Crecer desde ahí.
- Priorizar **offline-first** desde el día uno — cambia radicalmente el diseño.
- Todo en **open source** en GitHub → sirve como portfolio vivo.
- Documentar decisiones arquitectónicas en ADRs (`docs/adr/`).
