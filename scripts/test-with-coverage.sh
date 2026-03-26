#!/bin/bash
echo "📊 Ejecutando tests con análisis de cobertura..."
echo "================================================"

mvn clean test jacoco:report jacoco:check

# Abrir reporte de cobertura automáticamente (en sistemas con GUI)
if command -v xdg-open > /dev/null; then
    xdg-open target/site/jacoco/index.html
elif command -v open > /dev/null; then
    open target/site/jacoco/index.html
fi

echo ""
echo "✅ Análisis de cobertura completado"
