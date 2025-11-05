-- =====================================================================
-- ETAPA 4 – SEGURIDAD E INTEGRIDAD
-- =====================================================================
-- En esta etapa se agregan medidas de seguridad y se prueban las restricciones
-- de integridad del sistema. El objetivo es proteger los datos sensibles, limitar
-- los permisos de acceso y verificar que las reglas definidas en el modelo funcionen
-- correctamente.

-- =====================================================================
-- PASO 0: LIMPIEZA INICIAL DE USUARIOS
-- =====================================================================

-- Se utiliza DROP USER para eliminar el usuario.
DROP USER IF EXISTS 'user_gestion'@'localhost';

-- =====================================================================
-- CREACIÓN DE UN USUARIO CON PRIVILEGIOS LIMITADOS
-- =====================================================================
-- En este paso se crea un usuario específico para la aplicación.
-- La idea es que pueda usar la base de datos normalmente (consultar, agregar y
-- actualizar datos) sin tener acceso a eliminar registros o modificar la estructura.
-- Esto reduce riesgos y mejora la seguridad del sistema.
-- =====================================================================

-- Se crea el usuario solo si no existe.
CREATE USER IF NOT EXISTS 'user_gestion'@'localhost' IDENTIFIED BY 'Pacientes2025';

-- Se asignan permisos mínimos sobre las Tablas Principales del sistema:
-- Persona, Paciente e HistoriaClinica.  
-- Estos permisos permiten al usuario realizar las operaciones necesarias
-- para el funcionamiento normal de la aplicación:
--   - SELECT: consultar registros existentes (lectura de datos),
--   - INSERT: agregar nuevos registros (alta de pacientes, historias, etc.),
--   - UPDATE: modificar datos ya cargados (correcciones o actualizaciones).
-- No se otorgan permisos de DELETE ni de ALTER para evitar eliminaciones
-- accidentales o cambios en la estructura de la base de datos.
-- De esta manera, el usuario puede operar con la información diaria sin
-- comprometer la integridad ni la seguridad del sistema.
GRANT SELECT, INSERT, UPDATE ON GestionPacientes.Persona TO 'user_gestion'@'localhost';
GRANT SELECT, INSERT, UPDATE ON GestionPacientes.Paciente TO 'user_gestion'@'localhost';
GRANT SELECT, INSERT, UPDATE ON GestionPacientes.HistoriaClinica TO 'user_gestion'@'localhost';

-- Se otorgan permisos de solo lectura sobre las Tablas de Referencia: GrupoSanguineo y Profesional.
-- La aplicación solo necesita consultar estos datos (no modificarlos),
-- ya que su contenido es estable y administrado por el sistema.
GRANT SELECT ON GestionPacientes.GrupoSanguineo TO 'user_gestion'@'localhost';
GRANT SELECT ON GestionPacientes.Profesional TO 'user_gestion'@'localhost';

-- Se actualizan los privilegios del sistema para aplicar los cambios
-- y asegurar que el nuevo usuario tenga los permisos asignados.
FLUSH PRIVILEGES;

-- El usuario 'user_gestion' tiene ahora un acceso controlado:
-- puede realizar las operaciones normales del sistema sin el riesgo
-- de borrar datos o alterar la estructura de la base.
-- =====================================================================

-- =====================================================================
-- VERIFICACIÓN FINAL
-- =====================================================================
-- Se realizan consultas para confirmar que la configuración de seguridad 
-- e integridad se aplicó correctamente en el sistema de gestión de pacientes.
-- =====================================================================

-- 1. VERIFICAR CREACIÓN DEL USUARIO
-- Se confirma que el usuario fue creado exitosamente
SELECT user, host FROM mysql.user 
WHERE user = 'user_gestion' AND host = 'localhost';

-- 2. CONFIRMAR PRIVILEGIOS ASIGNADOS  
-- Se listan todos los permisos otorgados al usuario de la aplicación
SHOW GRANTS FOR 'user_gestion'@'localhost';

-- 3. VALIDAR ESTRUCTURA DE INTEGRIDAD
-- Se verifica que las relaciones entre tablas estén correctamente definidas
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'GestionPacientes'
    AND REFERENCED_TABLE_NAME IS NOT NULL;