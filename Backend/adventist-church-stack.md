# Stack para Proyecto de Gestión de Iglesia Adventista

Documento de referencia para el tercer proyecto personal: una plataforma integral de gestión para iglesias adventistas. Prioridad del stack: **demanda real del mercado laboral**, con la restricción adicional de no repetir tecnologías de los proyectos anteriores (RosaFiesta y app de memorización bíblica).

---

## Concepto del producto

Plataforma completa para administrar la vida de una iglesia adventista:

- **Miembros y familias**: historial espiritual, bautismos, transferencias entre iglesias.
- **Diezmos y ofrendas**: contabilidad auditable con event sourcing.
- **Ministerios**: escuela sabática, jóvenes, conquistadores, música, salud.
- **Eventos y calendario**: cultos, reuniones, campañas de evangelismo.
- **Asistencia**: registro de sábados, escuela sabática, reuniones de oración (time-series).
- **Comunicaciones**: anuncios, notificaciones push, email, SMS, tiempo real.
- **Biblioteca de contenido**: sermones, estudios bíblicos, recursos multimedia.
- **Workflows largos**: proceso de bautismo, transferencia de membresía, disciplina eclesiástica.

---

## Backend — Arquitectura

### Paradigma
**Microservicios con comunicación event-driven + coreografía de sagas + database-per-service.**

Cada servicio es dueño absoluto de su base de datos. La integración se hace por eventos publicados en Kafka. Las transacciones distribuidas se resuelven con sagas coreografiadas (sin orquestador central, excepto workflows específicos en Temporal).

### División de servicios

```
├── identity-service        → auth (delegado a Keycloak), usuarios, roles
├── members-service         → miembros, familias, historial espiritual
├── finance-service         → diezmos, ofrendas, presupuestos (event sourcing)
├── ministries-service      → escuela sabática, jóvenes, conquistadores
├── events-service          → cultos, reuniones, calendario
├── attendance-service      → asistencia (time-series)
├── communications-service  → anuncios, notificaciones, tiempo real
├── content-service         → sermones, estudios, multimedia
├── analytics-service       → reportes y dashboards (lee de BigQuery)
└── api-gateway             → punto de entrada único (Kong)
```

---

## Backend — Stack técnico (market-first)

### Core Go

| Tecnología | Por qué |
|---|---|
| **Go 1.24+** | Lenguaje base |
| **Gin** | Framework HTTP más pedido en ofertas Go. Distinto a chi (RosaFiesta) y Connect-RPC (bíblico) |
| **gRPC + Protobuf** | Comunicación síncrona entre servicios. Estándar en microservicios |
| **GORM** | ORM más pedido en Go. Cierra el trío: SQL crudo (RosaFiesta) → sqlc (bíblico) → GORM |
| **Wire** | Inyección de dependencias en compile-time |
| **Viper** | Configuración |
| **Zerolog** | Logging estructurado |

### Persistencia (polyglot)

| Servicio | DB | Motivo |
|---|---|---|
| identity | Keycloak gestiona su propia DB | Delegado |
| members | **Cloud SQL for PostgreSQL** | Relaciones familia/miembros complejas |
| finance | **Cloud SQL for PostgreSQL** + event store | Auditoría contable estricta con event sourcing |
| ministries | **Cloud SQL for PostgreSQL** | Relacional simple |
| events | **Cloud SQL for PostgreSQL** | Calendario y recurrencias |
| attendance | **Cloud SQL for PostgreSQL** + particionado temporal | Time-series pragmática |
| communications | **Firestore** | Tiempo real nativo para anuncios y chat |
| content | **Cloud SQL** (metadata) + **Cloud Storage** (archivos) | Archivos binarios fuera de la DB |
| analytics | **BigQuery** | Warehouse para reportes y dashboards |
| Cache compartido | **Memorystore (Redis)** | Cache y rate limiting |

### Event sourcing en finance-service

El servicio financiero guarda **eventos** en vez de estado mutable:

- `TitheReceived`, `OfferingReceived`, `ExpenseRecorded`, `BudgetApproved`.
- El balance actual es una proyección.
- Auditoría total e inmutable → crítico en un dominio financiero eclesiástico.
- Muy pedido en entrevistas senior como demostración de arquitectura avanzada.

---

## Mensajería y workflows

| Tecnología | Uso |
|---|---|
| **Apache Kafka** (self-hosted en GKE con **Strimzi operator**) | Event bus principal. Aprender Kafka real vale oro en ofertas. |
| **Temporal** (self-hosted) | Workflows largos: proceso de bautismo, transferencia de membresía, campañas de evangelismo multi-etapa |
| **Google Pub/Sub** (secundario) | Eventos triviales cloud-nativos (opcional, para tener ambos en el CV) |

---

## Autenticación

| Tecnología | Uso |
|---|---|
| **Keycloak** | OIDC, SAML, gestión de usuarios, roles, consola admin. Enterprise real. |
| **Workload Identity** (GCP) | Autenticación de servicios a servicios GCP sin keys |

Keycloak reemplaza el JWT casero de RosaFiesta y es el estándar enterprise más pedido.

---

## API Gateway y Service Mesh

| Tecnología | Rol |
|---|---|
| **Kong** | API Gateway externo: rate limiting, auth, transformaciones |
| **Istio** | Service mesh: mTLS automático, traffic management, observabilidad interna |

Istio es complejo pero es lo que piden en ofertas senior. Vale la pena aprenderlo aquí.

---

## Observabilidad

Stack completo obligatorio en microservicios:

- **OpenTelemetry SDK** en todos los servicios (traces + metrics + logs unificados).
- **Grafana Tempo** → distributed tracing.
- **Grafana Loki** → logs agregados.
- **Prometheus** → métricas.
- **Grafana** → dashboards.
- **Google Cloud Operations Suite** → segunda capa de observabilidad nativa GCP (para tener experiencia con ambas).
- **Grafana Pyroscope** → continuous profiling (extra diferenciador).

---

## Cloud e infraestructura — Google Cloud Platform

### Compute híbrido
- **GKE Autopilot** → servicios pesados (Kafka, Keycloak, Temporal, microservicios principales). Control plane sin costo extra, pagas solo pods.
- **Cloud Run** → servicios ligeros o con tráfico variable (communications workers, jobs periódicos). Escala a cero, perfecto para una iglesia con carga irregular.

### Servicios GCP usados
| Servicio | Uso |
|---|---|
| **GKE Autopilot** | Orquestación principal |
| **Cloud Run** | Servicios serverless |
| **Cloud SQL for PostgreSQL** | Bases de datos relacionales |
| **Firestore** | Tiempo real y comunicaciones |
| **BigQuery** | Analytics y reportes |
| **Cloud Storage (GCS)** | Archivos multimedia |
| **Memorystore (Redis)** | Cache |
| **Secret Manager** | Secretos |
| **Artifact Registry** | Imágenes Docker |
| **Cloud Build** | CI/CD complementario |
| **Cloud CDN** | CDN para contenido estático |
| **Eventarc** | Eventos cloud-nativos |
| **Workload Identity** | Auth entre servicios |
| **Cloud Operations Suite** | Observabilidad nativa |

### IaC y GitOps
- **Terraform** con **Google provider** → infra declarativa.
- **Helm** → charts por servicio.
- **ArgoCD** → GitOps sobre GKE.
- **GitHub Actions** → pipelines principales.
- **Cloud Build** → triggers nativos GCP.

### Desarrollo local
- **kind** o **k3s** para desarrollar sin gastar cloud.
- `docker-compose` para el stack mínimo local (Postgres, Redis, Kafka, Keycloak).

---

## Frontend — Flutter

Stack deliberadamente distinto a los otros dos proyectos:

| Área | Elección | Justificación |
|---|---|---|
| State | **BLoC** | El más pedido en ofertas Flutter. Provider (RosaFiesta) → Riverpod (bíblico) → BLoC (iglesia) |
| Routing | **auto_route** | Distinto a go_router del bíblico; muy pedido |
| Models | **Freezed** + **json_serializable** | Estándar |
| DI | **get_it** + **injectable** | Estándar enterprise |
| HTTP | **Dio + Retrofit** | Retrofit es muy pedido en ofertas Flutter |
| gRPC | **grpc_dart** | Para comunicación directa con algunos servicios |
| DB local | **sqflite** directo | Distinto a Drift (bíblico); el más clásico |
| Realtime | **cloud_firestore** SDK | Integración nativa con Firestore del backend |
| Notificaciones | **firebase_messaging** + **flutter_local_notifications** | Push + locales |
| Testing | **bloc_test** + **mocktail** + **Patrol** (E2E) | Stack market-first |
| Monorepo | **Melos** | Paquetes compartidos |

---

## Resumen final del stack

### Backend
- **Lenguaje**: Go 1.24+
- **Framework HTTP**: Gin
- **RPC**: gRPC + Protobuf
- **ORM**: GORM
- **Arquitectura**: microservicios + event-driven + saga coreografía + event sourcing en finance
- **Message broker**: Kafka self-hosted (Strimzi) + Pub/Sub secundario
- **Workflows**: Temporal
- **DBs**: Cloud SQL Postgres (principal) + Firestore (realtime) + BigQuery (analytics) + Cloud Storage (files)
- **Cache**: Memorystore Redis
- **Auth**: Keycloak + Workload Identity
- **API Gateway**: Kong
- **Service Mesh**: Istio
- **Observabilidad**: OpenTelemetry + Grafana LGTM + Pyroscope + Cloud Operations Suite

### Cloud (GCP)
- **Compute**: GKE Autopilot + Cloud Run (híbrido)
- **IaC**: Terraform (Google provider) + Helm
- **GitOps**: ArgoCD
- **CI/CD**: GitHub Actions + Cloud Build
- **Registry**: Artifact Registry
- **Secretos**: Secret Manager

### Frontend
- **Framework**: Flutter
- **State**: BLoC
- **Routing**: auto_route
- **HTTP**: Dio + Retrofit
- **gRPC**: grpc_dart
- **DB local**: sqflite
- **Realtime**: cloud_firestore
- **Push**: firebase_messaging
- **Testing**: bloc_test + mocktail + Patrol
- **Monorepo**: Melos

---

## Roadmap de aprendizaje sugerido

1. **Fase 1 — Fundamentos GCP y K8s**
   Terraform + GKE Autopilot + Artifact Registry + Cloud SQL. Un servicio "hola mundo" desplegado end-to-end.

2. **Fase 2 — Primer microservicio real**
   members-service en Gin + GORM + gRPC + Keycloak integrado. Helm chart + ArgoCD.

3. **Fase 3 — Event-driven**
   Kafka en GKE con Strimzi. finance-service con event sourcing publicando eventos. Un segundo servicio consumiendo.

4. **Fase 4 — Service mesh y observabilidad**
   Istio + OpenTelemetry + stack Grafana LGTM completo.

5. **Fase 5 — Workflows complejos**
   Temporal para el proceso de bautismo (multi-etapa, con humanos en el loop).

6. **Fase 6 — Analytics**
   BigQuery pipeline: eventos de Kafka → BigQuery → dashboards en Grafana y Looker Studio.

7. **Fase 7 — Frontend Flutter**
   App completa con BLoC, gRPC, Firestore en tiempo real, notificaciones push.

8. **Fase 8 — Hardening**
   Istio mTLS estricto, políticas OPA, secrets rotation, disaster recovery, backups.

---

## Por qué este stack maximiza el CV

| Tecnología | Presencia en ofertas Go/Flutter senior 2025 |
|---|---|
| Go + Gin | Alta — el framework más pedido |
| gRPC + Protobuf | Alta — estándar microservicios |
| GORM | Alta — ORM Go más común |
| Kafka | Muy alta — skill #1 en backend senior |
| Kubernetes + Helm | Muy alta — casi obligatorio mid/senior |
| GCP (GKE, Cloud Run, BigQuery) | Alta y creciendo rápido |
| Terraform | Muy alta |
| ArgoCD | Alta — GitOps es el estándar |
| Istio | Media-alta — senior/platform roles |
| Keycloak | Alta — enterprise |
| Temporal | Creciendo rápido — senior roles |
| OpenTelemetry | Muy alta |
| BigQuery | Alta — data-oriented |
| Event sourcing | Alta como diferenciador arquitectónico |
| Flutter + BLoC | El combo más pedido en Flutter |
| Retrofit (Dart) | Alta |
| Firestore realtime | Media-alta |

---

## Comparativa rápida entre los tres proyectos

| Dimensión | RosaFiesta | Memorización bíblica | Iglesia adventista |
|---|---|---|---|
| Framework Go | chi | Connect-RPC / Huma | **Gin** |
| Acceso a datos | SQL crudo | sqlc | **GORM** |
| Arquitectura | Layered / MVC | Modular monolith + Hexagonal + CQRS | **Microservicios + Event-driven + Saga + Event sourcing** |
| Mensajería | — | Redis streams | **Kafka + Temporal + Pub/Sub** |
| DB | Postgres | Postgres + pgvector | **Cloud SQL + Firestore + BigQuery + GCS** |
| Auth | JWT casero | JWT casero | **Keycloak** |
| Cloud | Local | Libre | **GCP** |
| Flutter state | Provider | Riverpod | **BLoC** |
| Flutter routing | Hash | go_router | **auto_route** |
| Flutter DB local | — | Drift | **sqflite** |
| IA | — | LLM + pgvector | — (deliberadamente sin IA para no repetir) |

Cada proyecto cubre un ángulo distinto del mercado sin solaparse.

---

## Notas finales

- **Presupuesto cloud**: GCP tiene free tier decente + $300 de créditos iniciales. GKE Autopilot + Cloud Run sin tráfico cuestan muy poco. Para desarrollo intensivo tirar el cluster cuando no se usa.
- **Kafka self-hosted** es el punto más exigente del stack. Asignar tiempo suficiente en la Fase 3.
- **Event sourcing solo en finance-service** — no extender al resto para evitar complejidad innecesaria.
- **Todo open source en GitHub** como portfolio vivo. ADRs en `docs/adr/` documentando decisiones arquitectónicas.
- **Dominio real**: la gestión de una iglesia adventista tiene reglas ricas (transferencias entre iglesias, proceso de membresía, contabilidad eclesiástica) que hacen el proyecto memorable en entrevistas, más allá de la tecnología.
