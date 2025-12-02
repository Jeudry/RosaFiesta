#!/bin/bash

# Script para sincronizar el repositorio con GitHub
# Este script resuelve el conflicto de historias no relacionadas

echo "===================================="
echo "Sincronizando con GitHub..."
echo "===================================="

cd /Users/sargon/Documents/Coding/KMP/RosaFiesta

echo ""
echo "1. Haciendo fetch del repositorio remoto..."
git fetch origin

echo ""
echo "2. Verificando el estado actual..."
git status

echo ""
echo "3. Haciendo merge con la rama remota (permitiendo historias no relacionadas)..."
git merge origin/main --allow-unrelated-histories -m "Merge remote repository with local" || {
    echo "⚠️  Si hay conflictos, resuélvelos y ejecuta: git merge --continue"
    exit 1
}

echo ""
echo "4. Subiendo cambios a GitHub..."
git push origin main

echo ""
echo "===================================="
echo "✅ ¡Repositorio sincronizado exitosamente!"
echo "===================================="
echo ""
echo "Puedes ver tu repositorio en: https://github.com/jeudry/RosaFiesta"