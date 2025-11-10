-- =====================================================================
-- SCRIPT DE INSERCIÓN MASIVA DE REGISTROS
-- Objetivo: Poblar la base de datos con un gran volumen de datos
-- realistas y consistentes para realizar pruebas de rendimiento y consultas.
-- =====================================================================

-- =====================================================================
-- PASO 1: CREAR 1000 REGISTROS EN 'HistoriaClinica'
-- =====================================================================

-- Aumenta el límite de recursión permitido para este CTE.
-- SET SESSION cte_max_recursion_depth = 1500;

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
-- PASO 2: GENERAR 1500 REGISTROS EN 'Paciente'
-- =====================================================================
-- Se establece una variable para el DNI inicial. Esto permite generar
-- DNIs en un rango numérico más realista, comenzando en 10 millones.
SET @dni_inicial = 10000000;

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