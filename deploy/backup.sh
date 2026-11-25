#!/usr/bin/env bash
# Respaldo de PostgreSQL de RosaFiesta: volcado comprimido con fecha y limpieza de copias antiguas.
set -euo pipefail

DEST="${1:-/var/backups/rosafiesta}"
case "$DEST" in
  /*) ;;
  *) DEST="$(pwd -P)/$DEST" ;;
esac
cd "$(dirname "$0")"
RETENTION_DAYS="${RETENTION_DAYS:-14}"
[ -f ./.env ] || { echo "Falta deploy/.env"; exit 1; }
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

mkdir -p "$DEST"
FILE="$DEST/rosafiesta-$(date +%Y%m%d-%H%M%S).sql.gz"
trap 'rm -f "${FILE}.tmp"' EXIT

docker compose exec -T db pg_dump --no-owner --no-acl -U "$POSTGRES_USER" "$POSTGRES_DB" | gzip > "${FILE}.tmp"
gunzip -t "${FILE}.tmp"
mv "${FILE}.tmp" "$FILE"
find "$DEST" -name 'rosafiesta-*.sql.gz' -mtime +"$RETENTION_DAYS" -delete
echo "Respaldo listo: $FILE"
