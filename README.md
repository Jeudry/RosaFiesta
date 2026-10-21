# 🌸 RosaFiesta

Plataforma multiplataforma (Android, iOS, escritorio y web) para la planificación, alquiler de mobiliario y decoración, cotización asistida por **Rosa IA** y operación integral de eventos.

## Dos apps, un mismo producto

| | App personal (clientes) | App administrativa (negocio) |
|---|---|---|
| **Quién la usa** | Clientes que planean un evento | Dueña (María) y equipo (Pedro, Yuli, Andrés) |
| **Tema** | Rosa → violeta, fondo rubor cálido (`RfTheme.personal`) | Índigo → turquesa, fondo lavanda frío (`RfTheme.admin`) |
| **Navegación** | `TabBar`: Inicio · Catálogo · **Rosa** · Eventos · Favoritos | `AdminBar`: Inicio · Eventos · **Rosa** · Inventario · Más (y barra lateral en Web `A39`) |
| **Rosa IA** | Arma la lista y la cotización del cliente | Crea borradores de eventos, cotiza y redacta respuestas |
| **Splash** | `p00` | `A00` |

## Estructura del repositorio

```
app/       Kotlin Multiplatform + Compose Multiplatform (Android, iOS, Desktop JVM, Web WasmJS)
           ├── core/engine         Reglas puras del dominio (Kotlin puro, explicitApi, 0 warnings)
           ├── core/domain         Modelos inmutables, casos de uso, datos canónicos
           ├── core/data           SQLDelight local (offline-first) + cliente HTTP Ktor
           ├── core/designsystem   Tokens (RfColors, RfTypography, RfDimens), fotos e ilustraciones
           ├── composeApp          UI compartida (App Personal, App Admin, Web 1440×900, Galería Dev)
           ├── androidApp          App Android Personal (com.jeudry.rosafiesta)
           └── adminAndroidApp     App Android Administrativa (com.jeudry.rosafiesta.admin)
server/    Backend en Go + PostgreSQL (monolito modular con chi + huma + sqlc)
api/       Especificación OpenAPI 3.1 autogenerada (api/openapi.json)
docs/      Diseño (Variante B), producto, arquitectura, pruebas de capturas y ADRs
tools/     Scripts de validación y auditoría
```

## Empezar

Requisitos: JDK 21, Go 1.26+, Docker, Xcode (iOS) y Android SDK.

```bash
make help        # lista de comandos disponibles
make check       # formato, análisis estático y pruebas de todo el repositorio
```

## Documentación

Empieza por [`docs/README.md`](docs/README.md) y [`docs/design/entrega-de-diseno.md`](docs/design/entrega-de-diseno.md). Reglas de trabajo: [`CLAUDE.md`](CLAUDE.md).

## Identificadores de las aplicaciones

- **App Personal:** `com.jeudry.rosafiesta`
- **App Administrativa:** `com.jeudry.rosafiesta.admin`
