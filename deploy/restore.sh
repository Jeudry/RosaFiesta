#!/usr/bin/env bash
# Restaura un respaldo creado por backup.sh en la base actual de RosaFiesta.
set -euo pipefail

cd "$(dirname "$0")"
FILE="${1:?Indica el archivo de respaldo (.sql.gz)}"
set -a; source ./.env; set +a

read -r -p "Esto reemplaza TODOS los datos de ${POSTGRES_DB}. Escribe 'restaurar' para seguir: " answer
[ "$answer" = "restaurar" ] || { echo "Cancelado."; exit 1; }

docker compose stop api
docker compose exec -T db psql -U "$POSTGRES_USER" -d postgres -c "DROP DATABASE IF EXISTS ${POSTGRES_DB};" -c "CREATE DATABASE ${POSTGRES_DB};"
gunzip -c "$FILE" | docker compose exec -T db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1
docker compose start api
echo "Restaurado desde $FILE"
