# =======================================================
# Script interactivo para generar src/main/resources/db.properties
# Compatible con PowerShell 5+ / 7+
# =======================================================

# Forzar salida en UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "=============================================" -ForegroundColor Green
Write-Host "🩺 Configurador de conexión MySQL" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green
Write-Host ""

$target = "src/main/resources/db.properties"

# Verificar si el archivo ya existe
if (Test-Path $target) {
    Write-Host "⚠️  Ya existe un archivo db.properties." -ForegroundColor Yellow
    $overwrite = Read-Host "¿Deseas sobrescribirlo? (s/n)"
    if ($overwrite -ne 's' -and $overwrite -ne 'S') {
        Write-Host "Operación cancelada." -ForegroundColor Red
        exit
    }
}

# Solicitar datos
$DB_USER = Read-Host "Usuario MySQL (default: root)"
if ([string]::IsNullOrWhiteSpace($DB_USER)) { $DB_USER = "root" }

$DB_PASS = Read-Host -AsSecureString "Contraseña MySQL"
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_PASS)
$PlainDB_PASS = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
[System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($BSTR)

$DB_NAME = Read-Host "Base de datos (default: GestionPacientes)"
if ([string]::IsNullOrWhiteSpace($DB_NAME)) { $DB_NAME = "GestionPacientes" }

$DB_PORT = Read-Host "Puerto MySQL (default: 3306)"
if ([string]::IsNullOrWhiteSpace($DB_PORT)) { $DB_PORT = "3306" }

# Crear carpeta si no existe
$dir = Split-Path $target
if (!(Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }

# Crear archivo con codificación UTF-8
@"
# Archivo generado automáticamente por setup-db.ps1
# Codificación: UTF-8
db.driverClass=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:$DB_PORT/$DB_NAME
db.user=$DB_USER
db.password=$PlainDB_PASS
"@ | Out-File -FilePath $target -Encoding utf8 -Force

Write-Host ""
Write-Host "✅ Archivo creado correctamente en: $target" -ForegroundColor Green
Write-Host "👉 Verifica la configuración antes de ejecutar la aplicación." -ForegroundColor Yellow
