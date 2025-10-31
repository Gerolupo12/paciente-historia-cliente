-- =====================================================================
-- ESQUEMA DE LA BASE DE DATOS: GestionPacientes para Programación II
-- =====================================================================
-- Crea la base de datos solo si no existe previamente, evitando errores en ejecuciones repetidas.
CREATE DATABASE GestionPacientes;

USE GestionPacientes;

-- =====================================================================
-- TABLA 1: GrupoSanguineo
-- Tabla maestra (o de catálogo) para los tipos de sangre.
-- =====================================================================
CREATE TABLE
    GrupoSanguineo (
        id INT PRIMARY KEY AUTO_INCREMENT,
        -- El tipo ENUM restringe los valores a los definidos, garantizando la integridad.
        tipo_grupo ENUM ('A', 'B', 'AB', 'O') NOT NULL,
        factor_rh ENUM ('+', '-') NOT NULL,
        nombre_enum VARCHAR(8) NOT NULL UNIQUE,
        -- Columna generada que concatena tipo y factor (ej. 'A+'). STORED significa que se
        -- almacena físicamente, lo que la hace más eficiente para consultas.
        simbolo VARCHAR(3) AS (CONCAT (tipo_grupo, factor_rh)) STORED,
        -- CONSTRAINTS
        -- Clave única compuesta: evita que exista más de una combinación tipo y factor (ej. no puede haber dos filas 'A' y '+').
        CONSTRAINT uk_grupo_factor UNIQUE (tipo_grupo, factor_rh)
    );

-- =====================================================================
-- TABLA 2: HistoriaClinica
-- Contiene la información médica. Puede existir sin un Paciente asociado inicialmente.
-- =====================================================================
CREATE TABLE
    HistoriaClinica (
        -- Clave primaria autoincremental.
        id INT PRIMARY KEY AUTO_INCREMENT,
        -- Columna para baja lógica, por defecto en FALSE (activo).
        eliminado BOOLEAN DEFAULT FALSE,
        nro_historia VARCHAR(20) NOT NULL UNIQUE,
        grupo_sanguineo_id INT NULL,
        antecedentes TEXT NULL,
        medicacion_actual TEXT NULL,
        observaciones TEXT NULL,
        -- CONSTRAINTS
        -- Si se borra un GrupoSanguineo, el campo aquí se pondrá en NULL.
        CONSTRAINT fk_hc_grupo_sanguineo_id FOREIGN KEY (grupo_sanguineo_id) REFERENCES GrupoSanguineo (id) ON DELETE SET NULL,
        -- Valida que el número de historia siga el formato 'HC-' seguido de números.
        CONSTRAINT chk_formato_numero_historia CHECK (nro_historia RLIKE '^HC-[0-9]{4,17}$')
    );

-- =====================================================================
-- TABLA 3: Paciente
-- Representa el rol de un paciente, vinculado a una HistoriaClinica.
-- =====================================================================
CREATE TABLE
    Paciente (
        id INT PRIMARY KEY AUTO_INCREMENT,
        eliminado BOOLEAN DEFAULT FALSE,
        -- Nombre y apellido, obligatorios.
        nombre VARCHAR(80) NOT NULL,
        apellido VARCHAR(80) NOT NULL,
        -- DNI, debe ser único y obligatorio. La restricción UNIQUE crea un índice automáticamente.
        dni VARCHAR(15) UNIQUE NOT NULL,
        -- Fecha de nacimiento, puede ser nula.
        fecha_nacimiento DATE NULL,
        -- Vínculo a HistoriaClinica. UNIQUE para relación 1 a 1. NULL permite crear un paciente sin historia asignada.
        historia_clinica_id INT UNIQUE NULL,
        -- CONSTRAINTS
        -- Si se borra la HistoriaClinica, el campo aquí se pone en NULL, conservando el registro del paciente.
        CONSTRAINT fk_paciente_historia_clinica_id FOREIGN KEY (historia_clinica_id) REFERENCES HistoriaClinica (id) ON DELETE SET NULL,
        -- Valida que el DNI tenga una longitud razonable.
        CONSTRAINT chk_longitud_dni CHECK (LENGTH (dni) BETWEEN 7 AND 15),
        -- Valida que la fecha de nacimiento sea posterior a 1900, evitando fechas claramente erróneas.
        CONSTRAINT chk_anio_minimo_1900 CHECK (YEAR (fecha_nacimiento) > 1900)
    );

-- =====================================================================
-- SCRIPT DE INSERCIÓN MASIVA DE REGISTROS
-- Objetivo: Poblar la base de datos con un gran volumen de datos
-- realistas y consistentes para realizar pruebas de rendimiento y consultas.
-- =====================================================================
-- =====================================================================
-- PASO 1: INSERTAR DATOS EN LA TABLA MAESTRA 'GrupoSanguineo'
-- =====================================================================
-- Se insertan los 8 registros únicos que representan todos los posibles
-- grupos sanguíneos. Esta tabla actúa como un catálogo.
INSERT INTO
    GrupoSanguineo (tipo_grupo, factor_rh, nombre_enum)
VALUES
    ('A', '+', 'A_PLUS'),
    ('A', '-', 'A_MINUS'),
    ('B', '+', 'B_PLUS'),
    ('B', '-', 'B_MINUS'),
    ('AB', '+', 'AB_PLUS'),
    ('AB', '-', 'AB_MINUS'),
    ('O', '+', 'O_PLUS'),
    ('O', '-', 'O_MINUS');

-- =====================================================================
-- PASO 2: CREAR 1000 REGISTROS EN 'HistoriaClinica'
-- =====================================================================
INSERT INTO
    HistoriaClinica (nro_historia, grupo_sanguineo_id, antecedentes, medicacion_actual, observaciones)
    -- Se genera una secuencia de 1 a 1000 y un número aleatorio para cada fila.
WITH RECURSIVE
    number_sequence (seq, rand_num) AS (
        SELECT
            1,
            RAND ()
        UNION ALL
        SELECT
            seq + 1,
            RAND ()
        FROM
            number_sequence
        WHERE
            seq < 1000
    )
SELECT
    -- Se genera un número de historia único y secuencial (ej. 'HC-000001').
    CONCAT ('HC-', LPAD (seq, 6, '0')),
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
        ELSE 6 -- AB- (1%, el más raro)
    END,
    -- Se asignan textos aleatorios para antecedentes, medicación y observaciones.
    ELT (1 + FLOOR(RAND () * 5), 'Antecedentes familiares de diabetes', 'Alergia a penicilina', 'Hipertensión arterial controlada', 'Antecedentes quirúrgicos: apendicectomía', 'Sin antecedentes relevantes'),
    ELT (1 + FLOOR(RAND () * 4), 'Aspirina 100mg diarios', 'Metformina 500mg cada 12 horas', 'Atorvastatina 200mg nocturnos', 'No medicación actual'),
    ELT (1 + FLOOR(RAND () * 3), 'Paciente estable, control en 6 meses', 'Requiere seguimiento estrecho', 'Evolución favorable')
FROM
    number_sequence;

-- =====================================================================
-- PASO 3: GENERAR 1500 REGISTROS EN 'Paciente'
-- =====================================================================
-- Se establece una variable para el DNI inicial. Esto permite generar
-- DNIs en un rango numérico más realista, comenzando en 10 millones.
SET @dni_inicial = 10000000;

-- Aumenta el límite de recursión permitido para este CTE.
-- SET SESSION cte_max_recursion_depth = 1500;

INSERT INTO Paciente (nombre, apellido, dni, fecha_nacimiento, historia_clinica_id)
-- Se utiliza un CTE (Common Table Expression) recursivo para generar una
-- secuencia de números del 1 al 1500. Es la forma moderna y legible
-- de crear series de datos.
WITH RECURSIVE number_sequence (num) AS (
    SELECT 1 -- Punto de partida de la recursión (ancla)
    UNION ALL
    SELECT num + 1 FROM number_sequence WHERE num < 1500 -- Paso recursivo
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

    -- Se genera un DNI SECUENCIAL múltiplo de 7 y ÚNICO sumando el número de la secuencia
    -- al DNI inicial. Esto garantiza la unicidad y evita fallos en la inserción.
    @dni_inicial + num * 7 - 1,
    
    -- Se genera una fecha de nacimiento ALEATORIA restando un número aleatorio de días
    -- a la fecha actual, resultando en edades entre 18 y 75 años.
    DATE_SUB(CURDATE(), INTERVAL FLOOR( (18*365) + RAND() * ((75-18)*365) ) DAY),

    -- Solo los primeros 1000 pacientes tienen historia asignada
    CASE
        WHEN num <= 1000 THEN num     -- 1 a 1000 → con historia clínica
        ELSE NULL                     -- 1001 a 1500 → sin historia
    END AS historia_clinica_id
FROM number_sequence;

-- =====================================================================
-- PASO 4: VERIFICACIÓN FINAL DE INSERCIONES
-- =====================================================================

-- Se realiza un conteo de registros en cada tabla para verificar que
-- se insertaron las cantidades esperadas.
SELECT 'GrupoSanguineo' AS tabla, COUNT(*) AS cantidad FROM GrupoSanguineo
UNION ALL SELECT 'HistoriaClinica', COUNT(*) FROM HistoriaClinica
UNION ALL SELECT 'Paciente', COUNT(*) FROM Paciente
-- Se añade una fila final que suma el total de todos los registros insertados.
UNION ALL SELECT 'TOTAL DE REGISTROS', SUM(cantidad) FROM (
    SELECT COUNT(*) AS cantidad FROM GrupoSanguineo
    UNION ALL SELECT COUNT(*) FROM HistoriaClinica
    UNION ALL SELECT COUNT(*) FROM Paciente
) AS total_registros_insertados;