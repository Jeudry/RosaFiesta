#!/usr/bin/env bash
# Kotlin/Native (iOS) rechaza nombres de prueba con , . : ; ( ) ? ! / \ y similares.
# Falla si alguna prueba de Kotlin tiene uno, para detectarlo antes de compilar iOS.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
bad=$(grep -rnE 'fun `[^`]*[,.:;()?!/\\"][^`]*`\(' --include='*.kt' "$ROOT/app" | grep -E '/(commonTest|jvmTest|iosTest|androidTest)/' || true)
if [[ -n "$bad" ]]; then
  echo "Nombres de prueba con caracteres no permitidos en Kotlin/Native:" >&2
  echo "$bad" >&2
  exit 1
fi
echo "Nombres de prueba OK"
