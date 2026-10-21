.DEFAULT_GOAL := help
SHELL := /bin/bash

GOLANGCI_LINT_VERSION := v2.14.0
SQLC_VERSION := v1.30.0
# Base de datos desechable para las pruebas de integración del servidor (`make testdb`).
TEST_DATABASE_URL ?= postgres://rosafiesta:rosafiesta_test@localhost:55433/rosafiesta_test?sslmode=disable
SERVER_BIN := $(CURDIR)/server/bin

.PHONY: api seed server-dev sqlc testdb server-test-integration help check app-format app-check app-test app-ios app-screenshots app-screenshots-approve \
	server-tools server-lint server-test server-check openapi docker-build up down

help: ## Lista los comandos disponibles
	@grep -E '^[a-zA-Z_-]+:.*?## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

check: app-check server-check ## Formato, análisis estático y pruebas de todo el repositorio

# ---- App (Kotlin Multiplatform: Personal + Admin) ----

app-format: ## Aplica el formato ktlint a todo el código Kotlin
	cd app && ./gradlew spotlessApply

app-test: ## Pruebas del motor, dominio, datos y sistema de diseño
	cd app && ./gradlew :core:engine:jvmTest :core:domain:jvmTest :core:data:jvmTest :core:designsystem:jvmTest

app-check: ## Lo que corre el CI: formato, pruebas y compilación de las 2 apps
	tools/lint/test-names.sh
	cd app && ./gradlew spotlessCheck :core:engine:jvmTest :core:domain:jvmTest :core:data:jvmTest :core:designsystem:jvmTest :composeApp:jvmTest

app-ios: ## Compila el framework de iOS para Personal y Admin (solo en macOS)
	cd app && ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

app-screenshots: ## Regresión visual estricta contra las capturas aprobadas (app/composeApp/screenshots/approved)
	cd app && ./gradlew :composeApp:jvmTest -Pscreenshots

app-screenshots-approve: ## Aprueba las capturas nuevas o cambiadas (revisa antes build/screenshots/diff)
	cd app && UPDATE_SCREENSHOTS=1 ./gradlew :composeApp:jvmTest -Pscreenshots

# ---- Servidor (Go) ----

server-tools: ## Instala las herramientas del servidor (golangci-lint, sqlc) en server/bin
	GOBIN=$(SERVER_BIN) go install github.com/golangci/golangci-lint/v2/cmd/golangci-lint@$(GOLANGCI_LINT_VERSION)
	GOBIN=$(SERVER_BIN) go install github.com/sqlc-dev/sqlc/cmd/sqlc@$(SQLC_VERSION)

server-lint: ## Análisis estático del servidor
	cd server && $(SERVER_BIN)/golangci-lint run ./...

server-test: ## Pruebas del servidor (las de integración se omiten sin TEST_DATABASE_URL; usa `make testdb`)
	cd server && go test ./...

server-check: server-lint server-test ## Análisis estático + pruebas del servidor

seed: ## Llena la base de desarrollo con los datos de ejemplo del diseño (DATABASE_URL; nunca en producción)
	cd server && go run -tags dev ./cmd/seed

server-dev: ## Arranca la API con las herramientas de /dev (solo desarrollo)
	cd server && go run -tags dev ./cmd/api

sqlc: ## Regenera el código SQL tipado (server/internal/*/postgres/sqlcgen)
	cd server && $(SERVER_BIN)/sqlc generate

testdb: ## Levanta un PostgreSQL desechable para las pruebas de integración (puerto 55433)
	docker rm -f rosafiesta-testdb >/dev/null 2>&1 || true
	docker run -d --name rosafiesta-testdb -e POSTGRES_USER=rosafiesta -e POSTGRES_PASSWORD=rosafiesta_test -e POSTGRES_DB=rosafiesta_test -p 55433:5432 postgres:17-alpine
	@echo "Listo. Corre: make server-test-integration"

server-test-integration: ## Pruebas del servidor con PostgreSQL real (requiere `make testdb`)
	cd server && TEST_DATABASE_URL="$(TEST_DATABASE_URL)" go test -count=1 ./...

docker-build: ## Construye la imagen de producción de la API
	docker build -t rosafiesta-api server

openapi: ## Regenera api/openapi.json desde el código del servidor
	cd server && go run ./cmd/openapi

api: ## Corre el servidor en el puerto 8788; usa DATABASE_URL o la base de `make up`
	cd server && PORT=8788 go run ./cmd/api

up: ## Levanta PostgreSQL local (docker compose)
	docker compose up -d

down: ## Apaga los servicios locales
	docker compose down
