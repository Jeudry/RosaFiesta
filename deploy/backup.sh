#!/usr/bin/env bash
# Respaldo de PostgreSQL de RosaFiesta: volcado comprimido con fecha y limpieza de copias antiguas.
set -euo pipefail

cd "$(dirname "$0")"
DEST="${1:-/var/backups/rosafiesta}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"
[ -f ./.env ] || { echo "Falta deploy/.env"; exit 1; }
set -a; source ./.env; set +a

mkdir -p "$DEST"
FILE="$DEST/rosafiesta-$(date +%Y%m%d-%H%M%S).sql.gz"
trap 'rm -f "${FILE}.tmp"' EXIT

docker compose exec -T db pg_dump --no-owner --no-acl -U "$POSTGRES_USER" "$POSTGRES_DB" | gzip > "${FILE}.tmp"
gunzip -t "${FILE}.tmp"
mv "${FILE}.tmp" "$FILE"
find "$DEST" -name 'rosafiesta-*.sql.gz' -mtime +"$RETENTION_DAYS" -delete
echo "Respaldo listo: $FILE"
