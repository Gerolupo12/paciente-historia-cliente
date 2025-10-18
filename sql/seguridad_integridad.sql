-- =====================================================================
-- ETAPA 4 – SEGURIDAD E INTEGRIDAD
-- =====================================================================
-- En esta etapa se agregan medidas de seguridad y se prueban las restricciones
-- de integridad del sistema. El objetivo es proteger los datos sensibles, limitar
-- los permisos de acceso y verificar que las reglas definidas en el modelo funcionen
-- correctamente.

-- =====================================================================
-- 1. CREACIÓN DE UN USUARIO CON PRIVILEGIOS LIMITADOS
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

-- =====================================================================
-- 2. CREACIÓN DE VISTAS SEGURAS
-- =====================================================================
-- En este bloque se crean dos vistas pensadas para consultas públicas o de sistema.
-- Las vistas muestran solo la información necesaria, ocultando datos personales
-- o sensibles. Esto ayuda a proteger la privacidad sin limitar la funcionalidad.
-- =====================================================================

-- =====================================================================
-- VISTA 1: vw_pacientes_publicos
-- Muestra información general de pacientes sin exponer DNI ni fecha de nacimiento.
-- =====================================================================
CREATE OR REPLACE VIEW vw_pacientes_publicos AS
SELECT 
    pac.id AS id_paciente,
    CONCAT(per.apellido, ', ', per.nombre) AS nombre_completo,
    gs.simbolo AS grupo_sanguineo,
    hc.nro_historia,
    prof.especialidad AS especialidad
FROM Paciente pac
    JOIN Persona per ON pac.persona_id = per.id
    LEFT JOIN HistoriaClinica hc ON pac.historia_clinica_id = hc.id
    LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
    LEFT JOIN Profesional prof ON hc.profesional_id = prof.id
WHERE pac.eliminado = FALSE;

-- =====================================================================
-- VISTA 2: vw_profesionales_publicos
-- Muestra datos básicos de los profesionales, sin exponer su matrícula completa.
-- =====================================================================
-- Se crea una vista que muestra información general de los profesionales,
-- incluyendo su nombre completo, matrícula (parcialmente enmascarada) y especialidad.
-- Se une la tabla Profesional con Persona para obtener los datos personales,
-- y se excluyen los registros marcados como eliminados.
-- =====================================================================

CREATE OR REPLACE VIEW vw_profesionales_publicos AS
SELECT 
    prof.id AS id_profesional,
    CONCAT(per.apellido, ', ', per.nombre) AS nombre_completo,
    CONCAT(LEFT(prof.matricula, 5), '***') AS matricula_parcial,
    prof.especialidad
FROM Profesional prof
    INNER JOIN Persona per ON prof.persona_id = per.id
WHERE prof.eliminado = FALSE;

-- Nota:
-- Las vistas permiten compartir información segura con la aplicación o con usuarios
-- de consulta, sin mostrar datos sensibles. También facilitan la administración de
-- permisos, ya que es más sencillo limitar el acceso a una vista que a varias tablas.
-- =====================================================================

-- ---------------------------------------------------------------------
-- VISUALIZACIÓN DE RESULTADOS OBTENIDOS
-- ---------------------------------------------------------------------
-- Vista de pacientes
SELECT * FROM vw_pacientes_publicos LIMIT 10;

-- Vista de profesionales
SELECT * FROM vw_profesionales_publicos LIMIT 10;

-- =====================================================================
-- 3. PRUEBAS DE INTEGRIDAD
-- =====================================================================
-- A continuación, se realizan pruebas controladas para comprobar que las
-- restricciones definidas en el modelo (UNIQUE y CHECK) funcionan correctamente.
-- Cada prueba intenta generar un error a propósito para confirmar que
-- la base impide operaciones inválidas.
-- =====================================================================

-- ---------------------------------------------------------------------
-- PRUEBA 1: Violación de UNIQUE (DNI duplicado)
-- ---------------------------------------------------------------------
INSERT INTO Persona (nombre, apellido, dni, fecha_nacimiento)
VALUES ('Juan', 'Pérez', '10000001', '1980-01-01');
-- Error esperado: ERROR 1062 (Duplicate entry for key 'dni')
-- Confirma que no se pueden registrar dos personas con el mismo DNI.

-- ---------------------------------------------------------------------
-- PRUEBA 2: Violación de CHECK (formato de matrícula incorrecto)
-- ---------------------------------------------------------------------
INSERT INTO Profesional (persona_id, matricula, especialidad)
VALUES (1, '123456', 'Cardiología');
-- Error esperado: ERROR 3819 (Check constraint 'chk_formato_matricula' is violated)
-- Garantiza que la matrícula cumpla con el formato establecido (MP-, MN-, MI-...).

-- ---------------------------------------------------------------------
-- PRUEBA 3: Violación de FK (referencia inexistente)
-- ---------------------------------------------------------------------
-- Se intenta crear un registro en 'Paciente' con un persona_id que no existe
-- en la tabla 'Persona'. Esto debe generar un error por violar la restricción
-- de clave foránea que asegura la existencia de la persona antes de asignarle
-- el rol de paciente.
INSERT INTO Paciente (persona_id, historia_clinica_id)
VALUES (9999999, 1);
-- Error esperado: ERROR 1452 (Cannot add or update a child row: a foreign key constraint fails)
-- Confirma que no se pueden crear registros dependientes sin su referencia válida,
-- =====================================================================


-- =====================================================================
-- 4. CONSULTA SEGURA EN JAVA
-- =====================================================================
-- A continuación, se muestra un ejemplo de cómo se ejecutan consultas
-- seguras desde el código Java usando PreparedStatement.
-- Esta técnica evita inyección SQL al separar la consulta de los valores
-- ingresados por el usuario.
-- =====================================================================

/* 
public boolean loginSeguro(String usuario, String password) {
    String sql = "SELECT * FROM usuarios WHERE dni = ?";
    
    -- Preparar la declaración
    PreparedStatement stmt = connection.prepareStatement(sql);
    
    -- Establecer parámetros DNI
    stmt.setString(1, dni);  // Primer parámetro (?)
    
    -- Ejecutar
    ResultSet rs = stmt.executeQuery();
    
    return rs.next();
}
*/
-- Vectores de Ataque Probados:
-- 1. `40123456' OR '1'='1` - Inyección para obtener todos los registros
-- 2. `40123456' --` - Intento de comentar condiciones adicionales  
-- 3. `' OR dni LIKE '40%` - Inyección con operador LIKE
-- 4. `'; DROP TABLE usuarios; --` - Ataque de eliminación de datos

-- Comentario:
-- PreparedStatement valida que el DNI sea un string
-- Los caracteres especiales como ' se convierten en \'
-- Con este método el valor del DNI se pasa como parámetro, evitando que
-- alguien inserte código malicioso dentro de la consulta.
-- Es una buena práctica de seguridad que siempre debe aplicarse
-- al trabajar con datos ingresados por usuarios.
-- =====================================================================


-- =====================================================================
-- 5. INTERACCIÓN CON IA (EVIDENCIA PEDAGÓGICA)
-- =====================================================================
-- Prompt utilizado:
-- "¿Cómo puedo crear un usuario con privilegios limitados en MySQL
-- para que la aplicación solo pueda leer e insertar datos sin poder
-- borrar ni alterar tablas?"
--
-- Respuesta resumida:
-- "Asigná permisos SELECT, INSERT y UPDATE sobre las tablas necesarias
-- y creá vistas para exponer solo la información útil. Evitá permisos
-- como DELETE, DROP o ALTER."
--
-- Reflexión:
-- "La sugerencia me ayudó a entender cómo aplicar la regla de privilegios
-- mínimos y a usar vistas para proteger datos sensibles. Aprendí que
-- la seguridad no solo depende del código, sino también del diseño de la base."
-- =====================================================================
