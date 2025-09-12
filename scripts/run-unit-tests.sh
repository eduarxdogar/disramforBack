#!/bin/bash
echo "🧪 Ejecutando tests unitarios..."
echo "=================================="

# Ejecutar solo tests unitarios (excluye integración)
mvn test -Dtest="**/*Test" -DfailIfNoTests=false

echo ""
echo "✅ Tests unitarios completados"
echo "📊 Revisa el reporte de cobertura en: target/site/jacoco/index.html"

# Script: scripts/run-integration-tests.sh
#!/bin/bash
echo "🔧 Ejecutando tests de integración..."
echo "====================================="

# Verificar que Docker esté corriendo
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker no está corriendo. Los tests de integración requieren Docker para TestContainers."
    exit 1
fi

# Ejecutar tests de integración
mvn verify -Dtest="**/*IntegrationTest,**/*IT" -DfailIfNoTests=false

echo ""
echo "✅ Tests de integración completados"

# Script: scripts/run-all-tests.sh
#!/bin/bash
echo "🚀 Ejecutando TODOS los tests..."
echo "================================"

# Verificar Docker
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker no está corriendo."
    exit 1
fi

# Limpiar proyecto
mvn clean

# Ejecutar todos los tests con cobertura
mvn verify jacoco:report

echo ""
echo "✅ Todos los tests completados"
echo "📊 Reportes disponibles:"
echo "   - Cobertura: target/site/jacoco/index.html"
echo "   - Surefire: target/surefire-reports/"
echo "   - Failsafe: target/failsafe-reports/"