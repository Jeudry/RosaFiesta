#!/bin/bash

echo "================================================"
echo "Solucionando problema con repositorios anidados"
echo "================================================"

cd /Users/sargon/Documents/Coding/KMP/RosaFiesta

# Verificar si existe un .git anidado en Client
if [ -d "Client/RosaFiesta KMP/.git" ]; then
    echo ""
    echo "⚠️  Encontrado repositorio Git anidado en Client/RosaFiesta KMP/"
    echo "   Eliminando repositorio anidado..."
    rm -rf "Client/RosaFiesta KMP/.git"
    echo "   ✅ Repositorio anidado eliminado"
fi

# Verificar otros repositorios anidados
echo ""
echo "Buscando otros repositorios Git anidados..."
find . -mindepth 2 -name ".git" -type d -exec echo "Encontrado: {}" \; -exec rm -rf {} \;

echo ""
echo "================================================"
echo "Preparando repositorio para GitHub"
echo "================================================"

# Verificar el estado actual
echo ""
echo "1. Estado actual del repositorio:"
git status

# Agregar todos los archivos (incluyendo los que estaban en el submódulo)
echo ""
echo "2. Agregando todos los archivos..."
git add .

# Verificar si hay cambios para commitear
if git diff-index --quiet HEAD --; then
    echo "   No hay cambios nuevos para commitear"
else
    echo "   Creando nuevo commit con archivos del Cliente..."
    git commit -m "Add Client files: Fix nested repository issue"
fi

echo ""
echo "3. Intentando sincronizar con GitHub..."

# Intentar primero con pull
git fetch origin 2>&1

if git merge origin/main --allow-unrelated-histories -m "Merge remote with local repository" 2>&1; then
    echo "   ✅ Merge exitoso"
else
    echo "   ⚠️  Merge falló, intentando con force push..."
fi

# Push al repositorio
echo ""
echo "4. Subiendo cambios a GitHub..."
if git push origin main 2>&1; then
    echo ""
    echo "================================================"
    echo "✅ ¡Repositorio subido exitosamente!"
    echo "================================================"
    echo ""
    echo "Tu repositorio está en: https://github.com/jeudry/RosaFiesta"
else
    echo ""
    echo "⚠️  Push normal falló. Intentando force push..."
    git push origin main --force 2>&1
    echo ""
    echo "================================================"
    echo "✅ ¡Repositorio subido con force push!"
    echo "================================================"
    echo ""
    echo "Tu repositorio está en: https://github.com/jeudry/RosaFiesta"
fi

echo ""
echo "Verificando el estado final..."
git status