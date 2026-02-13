include .envrc
MIGRATIONS_PATH = ./cmd/migrate/migrations

.PHONY: test
test:
	@go test -v ./...

.PHONY: migrate-create
migration:
	@migrate create -seq -ext sql -dir $(MIGRATIONS_PATH) $(filter-out $@,$(MAKECMDGOALS))

.PHONY: migrate-up
migrate-up:
	@migrate -path=$(MIGRATIONS_PATH) -database=$(DB_ADDR) up

.PHONY: migrate-down
migrate-down:
	@migrate -path=$(MIGRATIONS_PATH) -database=$(DB_ADDR) down $(filter-out $@,$(MAKECMDGOALS))

.PHONY: seed
seed:
	@go run cmd/migrate/seed/main.go

.PHONY: gen-docs
gen-docs:
	@swag init -g cmd/server/main.go -o docs

.PHONY: gen-mocks
gen-mocks:
	@go run go.uber.org/mock/mockgen -source=internal/store/repositories.go -destination=internal/store/mocks/repositories.go -package=mocks
	@go run go.uber.org/mock/mockgen -source=internal/services/interfaces.go -destination=internal/services/mocks/interfaces.go -package=mocks