-- SCRIPT DE CONSULTAS AVANZADAS Y REPORTES (ETAPA 3 TFI)
USE GestionPacientes;

/*	=====================================================================
	CONSULTA 1: JOIN - Ficha Completa de Pacientes Activos
	===================================================================== */

-- UTILIDAD: Esta consulta es fundamental para el sistema, ya que provee una vista
-- completa de un paciente, uniendo sus datos personales, su historia clínica y el
-- médico tratante. Es la consulta base para la pantalla "Ver Detalle de Paciente".
-- Filtra por `eliminado = FALSE` para mostrar solo los registros activos.

SELECT 
    p.id AS paciente_id,
    per.dni,
    CONCAT(per.apellido, ', ', per.nombre) AS nombre_completo,
    TIMESTAMPDIFF(YEAR, per.fecha_nacimiento, CURDATE()) AS edad,
    hc.nro_historia,
    gs.simbolo AS grupo_sanguineo,
    CONCAT(per_prof.apellido, ', ', per_prof.nombre) AS profesional_asignado,
    prof.especialidad
FROM 
    Paciente p
    INNER JOIN Persona per ON p.persona_id = per.id
    LEFT JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id
    LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
    LEFT JOIN Profesional prof ON hc.profesional_id = prof.id
    LEFT JOIN Persona per_prof ON prof.persona_id = per_prof.id
WHERE
    per.eliminado = FALSE AND p.eliminado = FALSE
ORDER BY 
    per.apellido, per.nombre;

/*	=====================================================================
	CONSULTA 2: JOIN - Búsqueda de Pacientes por Especialidad Médica
	===================================================================== */

-- UTILIDAD: Permite a un administrativo o a un médico generar un listado de todos
-- los pacientes que están siendo atendidos por una especialidad específica, por
-- ejemplo, 'Cardiología'. Es útil para auditorías o para organizar turnos.

SELECT
    prof.especialidad,
    CONCAT(per_pro.apellido, ', ', per_pro.nombre) AS profesional,
    hc.nro_historia,
    per_pac.dni AS dni_paciente,
    CONCAT(per_pac.apellido, ', ', per_pac.nombre) AS nombre_paciente
FROM HistoriaClinica hc
    INNER JOIN Profesional prof ON hc.profesional_id = prof.id
    INNER JOIN Persona per_pro ON prof.persona_id = per_pro.id
    INNER JOIN Paciente pac ON hc.id = pac.historia_clinica_id
    INNER JOIN Persona per_pac ON pac.persona_id = per_pac.id
WHERE
    prof.especialidad = 'Cardiología' AND hc.eliminado = FALSE
ORDER BY
    prof.matricula;

/*	=====================================================================
	CONSULTA 3: GROUP BY + HAVING - Grupos Sanguíneos Minoritarios
	===================================================================== */

-- UTILIDAD: Esta consulta genera un reporte estadístico para identificar los grupos
-- sanguíneos menos comunes en la base de datos (con 10000 o menos pacientes, por ejemplo).
-- Es de gran utilidad para campañas de donación de sangre o estudios epidemiológicos.

SELECT 
    gs.simbolo AS grupo_sanguineo,
    COUNT(p.id) AS cantidad_pacientes
FROM 
    GrupoSanguineo gs
    LEFT JOIN HistoriaClinica hc ON gs.id = hc.grupo_sanguineo_id
    LEFT JOIN Paciente p ON hc.id = p.historia_clinica_id
WHERE 
    p.eliminado = FALSE
GROUP BY 
    gs.simbolo
HAVING 
    COUNT(p.id) <= 10000 -- Mostramos solo grupos con 10000 o menos pacientes
ORDER BY 
    cantidad_pacientes ASC;

/*	=====================================================================
	CONSULTA 4: SUBCONSULTA - Historias Clínicas sin Profesional Asignado
	===================================================================== */

-- UTILIDAD: Genera un listado de "tareas pendientes" para el personal administrativo,
-- mostrando todas las historias clínicas activas que aún no tienen un médico
-- profesional asignado. Esto ayuda a asegurar que cada paciente reciba seguimiento.

SELECT
    hc.nro_historia,
    hc.antecedentes,
    (SELECT CONCAT(per.apellido, ', ', per.nombre) 
     FROM Persona per 
     JOIN Paciente p ON per.id = p.persona_id 
     WHERE p.historia_clinica_id = hc.id) AS paciente
FROM
    HistoriaClinica hc
WHERE
    hc.profesional_id IS NULL AND hc.eliminado = FALSE;

/*	=====================================================================
	VISTA (VIEW) - Lista Simplificada de Pacientes Activos
	===================================================================== */

-- UTILIDAD: Una vista es como una tabla virtual. Esta vista simplifica el acceso a
-- los datos más comunes de los pacientes activos. El equipo de desarrollo puede usarla
-- para poblar listados en la aplicación sin tener que escribir el complejo JOIN cada vez,
-- además de asegurar que siempre se trabaje con datos no eliminados.

CREATE OR REPLACE VIEW vw_pacientes_activos AS
SELECT 
    p.id AS paciente_id,
    per.dni,
    per.nombre,
    per.apellido,
    hc.nro_historia,
    prof.especialidad AS especialidad_medico
FROM 
    Paciente p
    INNER JOIN Persona per ON p.persona_id = per.id
    LEFT JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id
    LEFT JOIN Profesional prof ON hc.profesional_id = prof.id
WHERE
    p.eliminado = FALSE AND per.eliminado = FALSE;

-- Ejemplo de cómo usar la VISTA
SELECT * FROM vw_pacientes_activos WHERE especialidad_medico = 'Pediatría';