-- ==========================================================
-- MEDICIÓN COMPARATIVA CON Y SIN ÍNDICES (Etapa 3)
-- ==========================================================
-- Se evalúa el impacto de los índices en tres tipos de consultas:
-- 1) Igualdad (WHERE =)
-- 2) Rango (LIKE / BETWEEN)
-- 3) JOIN (relaciones entre tablas)
-- Se utiliza EXPLAIN ANALYZE para obtener el plan y tiempo real de ejecución.
-- ==========================================================

USE GestionPacientes;

-- ==========================================================
-- ESCENARIO CON ÍNDICE
-- ==========================================================
-- Se verifican los índices ya creados en la Etapa 1 del proyecto.
SHOW INDEXES FROM Persona;
SHOW INDEXES FROM HistoriaClinica;
SHOW INDEXES FROM Paciente;

-- ==========================================================
-- CONSULTA 1: IGUALDAD
-- ==========================================================
-- Se busca un paciente por DNI.
-- Representa una búsqueda exacta que aprovecha el índice sobre 'dni'.
-- ==========================================================

-- Se limpian las métricas previas del servidor para obtener datos precisos.
FLUSH STATUS;

-- Se analiza el plan de ejecución y se mide el tiempo total.
EXPLAIN ANALYZE
SELECT 
    per.dni, 
    per.nombre, 
    per.apellido
FROM Persona per
WHERE per.dni = 35123456;

-- Esta consulta muestra cómo el índice permite acceder rápidamente 
-- a un registro específico, evitando recorrer toda la tabla.


-- ==========================================================
-- CONSULTA 2: RANGO
-- ==========================================================
-- Se listan personas cuyos apellidos comienzan con 'Gon'.
-- Representa una búsqueda por rango (LIKE) que puede usar parcialmente el índice.
-- ==========================================================

FLUSH STATUS;

EXPLAIN ANALYZE
SELECT 
    per.apellido, 
    per.nombre
FROM Persona per
WHERE per.apellido LIKE 'Gon%';

-- Esta consulta demuestra cómo un índice compuesto puede acelerar
-- búsquedas alfabéticas o por prefijo de texto.


-- ==========================================================
-- CONSULTA 3: JOIN
-- ==========================================================
-- Se muestran datos completos de pacientes activos,
-- uniendo tablas mediante claves foráneas. 
-- Representa una consulta compleja que aprovecha varios índices.
-- ==========================================================

FLUSH STATUS;

EXPLAIN ANALYZE
SELECT
    p.id AS paciente_id,
    CONCAT(per.apellido, ', ', per.nombre) AS nombre_completo,
    hc.nro_historia,
    CONCAT(per_prof.apellido, ', ', per_prof.nombre) AS profesional,
    prof.especialidad
FROM Paciente p
INNER JOIN Persona per ON p.persona_id = per.id
LEFT JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id
LEFT JOIN Profesional prof ON hc.profesional_id = prof.id
LEFT JOIN Persona per_prof ON prof.persona_id = per_prof.id
WHERE 
    per.eliminado = FALSE 
    AND p.eliminado = FALSE
ORDER BY 
    per.apellido, per.nombre;

-- En este caso se puede observar cómo el uso de índices en claves foráneas
-- mejora la velocidad de los JOIN, reduciendo el tiempo total de ejecución.


-- ==========================================================
-- ESCENARIO SIN ÍNDICE
-- ==========================================================
-- Se repiten las tres consultas pero forzando al optimizador 
-- a no usar los índices definidos (usando IGNORE INDEX).
-- ==========================================================

-- ==========================================================
-- CONSULTA 1: IGUALDAD SIN ÍNDICE
-- ==========================================================
FLUSH STATUS;

EXPLAIN ANALYZE
SELECT 
    per.dni, 
    per.nombre, 
    per.apellido
FROM Persona per IGNORE INDEX (idx_persona_apellido_nombre)
WHERE per.dni = 35123456;

-- Al no usar el índice, el motor debe realizar un escaneo completo de la tabla,
-- lo cual incrementa el costo y el tiempo de ejecución.


-- ==========================================================
-- CONSULTA 2: RANGO SIN ÍNDICE
-- ==========================================================
FLUSH STATUS;

EXPLAIN ANALYZE
SELECT 
    per.apellido, 
    per.nombre
FROM Persona per IGNORE INDEX (idx_persona_apellido_nombre)
WHERE per.apellido LIKE 'Gon%';

-- Esta consulta evidencia cómo, sin índices, las búsquedas por texto
-- se vuelven mucho más lentas, ya que MySQL compara fila por fila.


-- ==========================================================
-- CONSULTA 3: JOIN SIN ÍNDICE
-- ==========================================================
FLUSH STATUS;

EXPLAIN ANALYZE
SELECT
    p.id AS paciente_id,
    CONCAT(per.apellido, ', ', per.nombre) AS nombre_completo,
    hc.nro_historia,
    CONCAT(per_prof.apellido, ', ', per_prof.nombre) AS profesional,
    prof.especialidad
FROM Paciente p
INNER JOIN Persona per IGNORE INDEX (idx_persona_apellido_nombre) ON p.persona_id = per.id
LEFT JOIN HistoriaClinica hc IGNORE INDEX (idx_historia_clinica_profesional) ON p.historia_clinica_id = hc.id
LEFT JOIN Profesional prof ON hc.profesional_id = prof.id
LEFT JOIN Persona per_prof ON prof.persona_id = per_prof.id
WHERE 
    per.eliminado = FALSE 
    AND p.eliminado = FALSE
ORDER BY 
    per.apellido, per.nombre;

-- Aquí se observa una caída notable en el rendimiento,
-- debido a que el motor debe recorrer más registros por cada unión.


-- ==========================================================
-- CONCLUSIÓN
-- ==========================================================
-- Los resultados muestran que el uso de índices reduce significativamente
-- los tiempos de ejecución en consultas de tipo rango y JOIN, evidenciando
-- su impacto positivo en la optimización del rendimiento.
-- En la consulta por igualdad, los tiempos son similares, lo que sugiere que
-- el motor ya aprovecha optimizaciones internas, pero el índice mantiene
-- una ligera ventaja. En general, los índices reducen escaneos completos
-- y mejoran la eficiencia global del sistema.


-- ==========================================================
-- ASPECTOS A MEJORAR
-- ==========================================================
-- 1. Evaluar la creación de índices adicionales sobre columnas con alta frecuencia de filtrado.
-- 2. Analizar el impacto en inserciones y actualizaciones, ya que los índices pueden ralentizar operaciones de escritura.
-- 3. Repetir las pruebas con mayor volumen de datos para obtener mediciones más representativas.
-- 4. Automatizar las mediciones (por ejemplo, con scripts SQL o Performance Schema) para evitar variaciones humanas.


-- ==========================================================
-- RESULTADOS Y ANÁLISIS (documentación)
-- ==========================================================
-- Los resultados completos, incluyendo capturas de pantalla, 
-- tablas de tiempos y gráficos comparativos, se encuentran disponibles 
-- en los ANEXOS del trabajo, dentro del apartado “Capturas”. 
-- Se encuentran con los siguietes nombres: 
-- consulta1_igualdad_con_indice.PNG
-- consulta1_igualdad_sin_indice.PNG
-- consulta2_rango_con_indice.PNG
-- consulta2_rango_sin_indice.PNG
-- consulta3_join_con_indice.PNG
-- consulta3_join_sin_indice.PNG
-- mediciones_con_sin_indice_resultados.PNG
-- ==========================================================
