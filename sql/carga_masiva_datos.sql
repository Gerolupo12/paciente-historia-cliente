-- =====================================================================
-- SCRIPT DE INSERCIÓN MASIVA DE REGISTROS
-- Objetivo: Poblar la base de datos con un gran volumen de datos
-- realistas y consistentes para realizar pruebas de rendimiento y consultas.
-- =====================================================================

USE GestionPacientes;

-- =====================================================================
-- PASO 0: LIMPIEZA INICIAL DE LA BASE DE DATOS
-- =====================================================================
-- Se desactivan temporalmente las revisiones de claves foráneas para
-- permitir el vaciado de tablas en cualquier orden sin errores.
SET FOREIGN_KEY_CHECKS = 0;

-- Se utiliza TRUNCATE TABLE para vaciar todas las tablas de forma eficiente.
TRUNCATE TABLE Paciente;
TRUNCATE TABLE HistoriaClinica;
TRUNCATE TABLE Profesional;
TRUNCATE TABLE Persona;
TRUNCATE TABLE GrupoSanguineo;

-- Se reactivan las revisiones de claves foráneas para mantener la
-- integridad de los datos durante las inserciones.
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- PASO 1: INSERTAR DATOS EN LA TABLA MAESTRA 'GrupoSanguineo'
-- =====================================================================
-- Se insertan los 8 registros únicos que representan todos los posibles
-- grupos sanguíneos. Esta tabla actúa como un catálogo.
INSERT INTO GrupoSanguineo (tipo_grupo, factor_rh) VALUES
('A', '+'), ('A', '-'), ('B', '+'), ('B', '-'),
('AB', '+'), ('AB', '-'), ('O', '+'), ('O', '-');

-- =====================================================================
-- PASO 2: GENERAR 200000 REGISTROS EN 'Persona'
-- =====================================================================
-- Se establece una variable para el DNI inicial. Esto permite generar
-- DNIs en un rango numérico más realista, comenzando en 10 millones.
SET @dni_inicial = 10000000;

INSERT INTO Persona (nombre, apellido, dni, fecha_nacimiento)
-- Se utiliza un CTE (Common Table Expression) recursivo para generar una
-- secuencia de números del 1 al 200000. Es la forma moderna y legible
-- de crear series de datos.
WITH RECURSIVE number_sequence (num) AS (
    SELECT 1 -- Punto de partida de la recursión (ancla)
    UNION ALL
    SELECT num + 1 FROM number_sequence WHERE num < 200000 -- Paso recursivo
)
SELECT 
    -- Se genera un nombre aleatorio concatenando un valor de la lista expandida,
    -- un espacio y una inicial aleatoria para mayor variedad.
    CONCAT(ELT(1 + FLOOR(RAND() * 50), 
        'Juan', 'María', 'Carlos', 'Ana', 'Luis', 'Laura', 'Pedro', 'Sofía',
        'Miguel', 'Elena', 'Francisco', 'Isabel', 'Javier', 'Carmen', 'Antonio', 
        'Lucía', 'David', 'Patricia', 'José', 'Marta', 'Daniel', 'Paula', 'Alejandro',
        'Cristina', 'Manuel', 'Sara', 'Sergio', 'Eva', 'Fernando', 'Raquel', 'Jorge',
        'Beatriz', 'Ricardo', 'Nuria', 'Rubén', 'Verónica', 'Óscar', 'Lorena',
        'Guillermo', 'Silvia', 'Adrián', 'Mónica', 'Enrique', 'Pilar', 'Diego',
        'Alba', 'Ivan', 'Rocío', 'Andrés', 'Teresa'), ' ', CHAR(65 + (num % 26)), '.'),
    
    -- Se genera un apellido aleatorio de forma similar al nombre.
    CONCAT(ELT(1 + FLOOR(RAND() * 50),
        'García', 'Rodríguez', 'González', 'Fernández', 'López', 'Martínez',
        'Sánchez', 'Pérez', 'Gómez', 'Martín', 'Jiménez', 'Ruiz', 'Hernández',
        'Díaz', 'Moreno', 'Muñoz', 'Álvarez', 'Romero', 'Alonso', 'Gutiérrez',
        'Navarro', 'Torres', 'Domínguez', 'Vázquez', 'Ramos', 'Gil', 'Ramírez',
        'Serrano', 'Blanco', 'Molina', 'Morales', 'Suárez', 'Ortega', 'Delgado',
        'Castro', 'Ortiz', 'Rubio', 'Marín', 'Sanz', 'Iglesias', 'Nuñez', 'Medina',
        'Garrido', 'Santos', 'Castillo', 'Cortés', 'Lozano', 'Guerrero', 'Cano',
        'Prieto'), ' ', CHAR(65 + ((num + 7) % 26)), '.'),

    -- Se genera un DNI SECUENCIAL y ÚNICO sumando el número de la secuencia
    -- al DNI inicial. Esto garantiza la unicidad y evita fallos en la inserción.
    @dni_inicial + num - 1,
    
    -- Se genera una fecha de nacimiento ALEATORIA restando un número aleatorio de días
    -- a la fecha actual, resultando en edades entre 18 y 75 años.
    DATE_SUB(CURDATE(), INTERVAL FLOOR( (18*365) + RAND() * ((75-18)*365) ) DAY)
FROM number_sequence;

-- =====================================================================
-- PASO 3: CREAR 2000 REGISTROS EN 'Profesional'
-- =====================================================================
-- Se seleccionan las primeras 2000 personas de la tabla 'Persona' y se les
-- asigna el rol de profesional.
INSERT INTO Profesional (persona_id, matricula, especialidad)
SELECT 
    id, -- El ID de la persona se convierte en el persona_id del profesional.
    -- Se genera una matrícula única y secuencial (ej. 'MP-00001') usando la
    -- función de ventana ROW_NUMBER(), que numera las filas seleccionadas.
    CONCAT('MP-', LPAD(ROW_NUMBER() OVER (ORDER BY id), 5, '0')),
    -- Se asigna una de las 10 especialidades de forma cíclica.
    ELT(1 + (ROW_NUMBER() OVER (ORDER BY id) % 10), 
        'Cardiología', 'Pediatría', 'Traumatología', 'Dermatología', 
        'Neurología', 'Oftalmología', 'Ginecología', 'Psiquiatría', 
        'Cirugía', 'Medicina General')
FROM Persona 
ORDER BY id 
LIMIT 2000;

-- =====================================================================
-- PASO 4: CREAR 150000 REGISTROS EN 'HistoriaClinica'
-- =====================================================================
INSERT INTO HistoriaClinica (nro_historia, grupo_sanguineo_id, profesional_id, antecedentes, medicacion_actual, observaciones)
-- Se genera una secuencia de 1 a 150000 y un número aleatorio para cada fila.
WITH RECURSIVE number_sequence (seq, rand_num) AS (
    SELECT 1, RAND()
    UNION ALL
    SELECT seq + 1, RAND() FROM number_sequence WHERE seq < 150000
)
SELECT 
    -- Se genera un número de historia único y secuencial (ej. 'HC-000001').
    CONCAT('HC-', LPAD(seq, 6, '0')),
    
    -- Se asigna un grupo sanguíneo usando una DISTRIBUCIÓN PONDERADA para simular
    -- porcentajes reales en la población (O+ y A+ son los más comunes).
    CASE 
        WHEN rand_num < 0.38 THEN 7 -- O+  (38% de probabilidad)
        WHEN rand_num < 0.72 THEN 1 -- A+  (34%)
        WHEN rand_num < 0.81 THEN 3 -- B+  (9%)
        WHEN rand_num < 0.88 THEN 8 -- O-  (7%)
        WHEN rand_num < 0.94 THEN 2 -- A-  (6%)
        WHEN rand_num < 0.97 THEN 5 -- AB+ (3%)
        WHEN rand_num < 0.99 THEN 4 -- B-  (2%)
        ELSE 6                      -- AB- (1%, el más raro)
    END,

    -- Se asigna un profesional_id aleatoriamente. El 95% de las veces se asigna
    -- un ID de profesional de forma cíclica, y el 5% se deja como NULL para
    -- simular historias clínicas pendientes de asignación.
    CASE
        WHEN rand_num < 0.95 THEN ((seq - 1) % 2000) + 1
        ELSE NULL
    END,

    -- Se asignan textos aleatorios para antecedentes, medicación y observaciones.
    ELT(1 + FLOOR(RAND() * 5), 
        'Antecedentes familiares de diabetes', 'Alergia a penicilina', 
        'Hipertensión arterial controlada', 'Antecedentes quirúrgicos: apendicectomía',
        'Sin antecedentes relevantes'),
    ELT(1 + FLOOR(RAND() * 4),
        'Aspirina 100mg diarios', 'Metformina 500mg cada 12 horas', 
        'Atorvastatina 200mg nocturnos', 'No medicación actual'),
    ELT(1 + FLOOR(RAND() * 3),
        'Paciente estable, control en 6 meses', 'Requiere seguimiento estrecho', 
        'Evolución favorable')
FROM number_sequence;

-- =====================================================================
-- PASO 5: CREAR 150000 REGISTROS EN 'Paciente'
-- =====================================================================
-- Este es el paso final que vincula las Personas restantes con las Historias Clínicas.
INSERT INTO Paciente (persona_id, historia_clinica_id)
SELECT 
    p.id,
    hc.id
FROM Persona p
-- La clave está en este JOIN: establece una relación 1 a 1 perfecta.
-- La Persona con id=2001 se une a la HistoriaClinica con id=1 (2001-2000).
-- La Persona con id=2002 se une a la HistoriaClinica con id=2 (2002-2000), y así sucesivamente.
INNER JOIN HistoriaClinica hc ON hc.id = p.id - 2000
-- Se seleccionan únicamente las Personas que NO son profesionales (id > 2000).
WHERE p.id > 2000
LIMIT 150000;

-- =====================================================================
-- PASO 6: VERIFICACIÓN FINAL
-- =====================================================================
-- Se muestra un mensaje de éxito.
SELECT '=== CARGA COMPLETADA - REQUISITOS CUMPLIDOS ===' AS Estado;

-- Se realiza un conteo de registros en cada tabla para verificar que
-- se insertaron las cantidades esperadas.
SELECT 'GrupoSanguineo' AS tabla, COUNT(*) AS cantidad FROM GrupoSanguineo
UNION ALL SELECT 'Persona', COUNT(*) FROM Persona
UNION ALL SELECT 'Profesional', COUNT(*) FROM Profesional  
UNION ALL SELECT 'HistoriaClinica', COUNT(*) FROM HistoriaClinica
UNION ALL SELECT 'Paciente', COUNT(*) FROM Paciente
-- Se añade una fila final que suma el total de todos los registros insertados.
UNION ALL SELECT 'TOTAL DE REGISTROS', SUM(cantidad) FROM (
    SELECT COUNT(*) AS cantidad FROM GrupoSanguineo
    UNION ALL SELECT COUNT(*) FROM Persona
    UNION ALL SELECT COUNT(*) FROM Profesional
    UNION ALL SELECT COUNT(*) FROM HistoriaClinica
    UNION ALL SELECT COUNT(*) FROM Paciente
) AS total_registros_insertados;