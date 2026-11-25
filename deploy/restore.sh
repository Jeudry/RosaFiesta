#!/usr/bin/env bash
# Restaura un respaldo creado por backup.sh en la base actual de RosaFiesta.
set -euo pipefail

FILE="${1:?Indica el archivo de respaldo (.sql.gz)}"
case "$FILE" in
  /*) ;;
  *) FILE="$(pwd -P)/$FILE" ;;
esac
cd "$(dirname "$0")"
[ -f "$FILE" ] || { echo "Archivo no encontrado: $FILE"; exit 1; }
[ -f ./.env ] || { echo "Falta deploy/.env"; exit 1; }
gunzip -t "$FILE"
read_env_value() {
  local key="$1"
  local raw
  raw="$(grep -E "^${key}=" ./.env | tail -n 1 | cut -d '=' -f 2-)"
  raw="${raw%\"}"
  raw="${raw#\"}"
  raw="${raw%\'}"
  raw="${raw#\'}"
  printf '%s' "$raw"
}
POSTGRES_USER="$(read_env_value POSTGRES_USER)"
POSTGRES_DB="$(read_env_value POSTGRES_DB)"
[ -n "$POSTGRES_USER" ] && [ -n "$POSTGRES_DB" ] || { echo "Faltan POSTGRES_USER o POSTGRES_DB en deploy/.env"; exit 1; }

read -r -p "Esto reemplaza TODOS los datos de ${POSTGRES_DB}. Escribe 'restaurar' para seguir: " answer
[ "$answer" = "restaurar" ] || { echo "Cancelado."; exit 1; }

trap 'docker compose start api >/dev/null 2>&1 || true' ERR
docker compose stop api
docker compose exec -T db psql -U "$POSTGRES_USER" -d postgres \
  -c "DROP DATABASE IF EXISTS \"${POSTGRES_DB}\" WITH (FORCE);" \
  -c "CREATE DATABASE \"${POSTGRES_DB}\";"
gunzip -c "$FILE" | docker compose exec -T db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1
docker compose start api
echo "Restaurado desde $FILE"
