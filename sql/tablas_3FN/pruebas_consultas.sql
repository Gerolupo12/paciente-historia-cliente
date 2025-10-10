-- CONSULTA 1: Información completa de todos los pacientes
SELECT
    p.id AS paciente_id,
    per.nombre,
    per.apellido,
    per.dni,
    per.fecha_nacimiento,
    hc.nro_historia,
    gs.simbolo AS grupo_sanguineo,
    hc.antecedentes,
    hc.medicacion_actual,
    hc.observaciones,
    per.eliminado AS persona_eliminada
FROM
    Paciente p
    INNER JOIN Persona per ON p.persona_id = per.id
    LEFT JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id
    LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
ORDER BY
    per.dni;

-- CONSULTA 2: Información básica de pacientes con formato más legible
SELECT
    CONCAT (per.apellido, ', ', per.nombre) AS paciente,
    per.dni,
    TIMESTAMPDIFF (YEAR, per.fecha_nacimiento, CURDATE()) AS edad,
    hc.nro_historia,
    gs.simbolo AS grupo_sanguineo,
    hc.medicacion_actual
FROM
    Paciente p
    INNER JOIN Persona per ON p.persona_id = per.id
    LEFT JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id
    LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
ORDER BY
    per.apellido,
    per.nombre;

-- CONSULTA 3: Pacientes con historias clínicas (solo los que tienen)
SELECT
    p.id AS paciente_id,
    CONCAT (per.apellido, ', ', per.nombre) AS paciente,
    per.dni,
    hc.nro_historia,
    gs.simbolo AS grupo_sanguineo,
    hc.antecedentes,
    hc.medicacion_actual
FROM
    Paciente p
    INNER JOIN Persona per ON p.persona_id = per.id
    INNER JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id
    LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
WHERE
    p.historia_clinica_id IS NOT NULL
ORDER BY
	gs.simbolo;

-- CONSULTA 4: Pacientes SIN historias clínicas
SELECT
    p.id AS paciente_id,
    CONCAT (per.apellido, ', ', per.nombre) AS paciente,
    per.dni,
    per.fecha_nacimiento,
    'SIN HISTORIA CLÍNICA' AS estado
FROM
    Paciente p
    INNER JOIN Persona per ON p.persona_id = per.id
WHERE
    p.historia_clinica_id IS NULL;

-- CONSULTA 5: Información detallada por grupo sanguíneo
SELECT
    gs.simbolo AS grupo_sanguineo,
    COUNT(p.id) AS cantidad_pacientes,
    GROUP_CONCAT(CONCAT(per.apellido, ', ', per.nombre) SEPARATOR ' | ') AS pacientes
FROM
    GrupoSanguineo gs
    LEFT JOIN HistoriaClinica hc ON gs.id = hc.grupo_sanguineo_id
    LEFT JOIN Paciente p ON hc.id = p.historia_clinica_id
    LEFT JOIN Persona per ON p.persona_id = per.id
WHERE
    hc.eliminado = FALSE
GROUP BY
    gs.id,
    gs.simbolo
ORDER BY
    gs.simbolo;

-- CONSULTA 6: Información de un paciente específico por ID
SELECT
    p.id AS paciente_id,
    per.nombre,
    per.apellido,
    per.dni,
    per.fecha_nacimiento,
    hc.nro_historia,
    gs.tipo_grupo,
    gs.factor_rh,
    hc.antecedentes,
    hc.medicacion_actual,
    hc.observaciones
FROM
    Paciente p
    INNER JOIN Persona per ON p.persona_id = per.id
    LEFT JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id
    LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
WHERE
    p.id = 1;

-- CONSULTA 7: Resumen estadístico
SELECT
    COUNT(*) AS total_pacientes,
    COUNT(p.historia_clinica_id) AS pacientes_con_historia,
    COUNT(*) - COUNT(p.historia_clinica_id) AS pacientes_sin_historia,
    COUNT(DISTINCT hc.grupo_sanguineo_id) AS grupos_sanguineos_distintos
FROM
    Paciente p
    LEFT JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id;
