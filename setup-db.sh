#!/bin/bash
# =======================================================
# Script interactivo para generar src/main/resources/db.properties
# =======================================================

# Fuerza UTF-8 en todo el entorno (para Git Bash, Linux, macOS)
export LC_ALL=en_US.UTF-8
export LANG=en_US.UTF-8

# Colores para una mejor legibilidad
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # Sin color

echo -e "${GREEN}=============================================${NC}"
echo -e "${GREEN}🩺 Configurador de conexión MySQL${NC}"
echo -e "${GREEN}=============================================${NC}"

TARGET="src/main/resources/db.properties"

# Verificar si el archivo ya existe
if [ -f "$TARGET" ]; then
  echo -e "${YELLOW}⚠️  Ya existe un archivo db.properties.${NC}"
  read -p "¿Deseas sobrescribirlo? (s/n): " overwrite
  if [[ "$overwrite" != "s" && "$overwrite" != "S" ]]; then
    echo "Operación cancelada."
    exit 0
  fi
fi

# Solicitar datos de conexión
read -p "Usuario MySQL (default: root): " DB_USER
DB_USER=${DB_USER:-root}

read -s -p "Contraseña MySQL: " DB_PASS
echo ""

read -p "Base de datos (default: GestionPacientes): " DB_NAME
DB_NAME=${DB_NAME:-GestionPacientes}

read -p "Puerto MySQL (default: 3306): " DB_PORT
DB_PORT=${DB_PORT:-3306}

# Crear carpeta si no existe
mkdir -p "$(dirname "$TARGET")"

# Generar archivo con codificación UTF-8
cat > "$TARGET" <<EOL
# Archivo generado automáticamente por setup-db.sh
# Codificación: UTF-8
db.driverClass=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:$DB_PORT/$DB_NAME
db.user=$DB_USER
db.password=$DB_PASS
EOL

echo ""
echo -e "${GREEN}✅ Archivo creado correctamente en:${NC} $TARGET"
echo -e "${YELLOW}👉 Verifica la configuración antes de ejecutar la aplicación.${NC}"
