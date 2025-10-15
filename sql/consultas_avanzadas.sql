-- =====================================================================
-- SCRIPT DE CONSULTAS AVANZADAS Y REPORTES (ETAPA 3 TFI)
-- Objetivo: Diseñar consultas complejas y vistas para agregar valor
-- al sistema, cumpliendo con los requisitos del trabajo final.
-- =====================================================================
USE GestionPacientes;

-- =====================================================================
-- CONSULTA 1: JOIN - Ficha Completa de Pacientes Activos
-- =====================================================================
-- UTILIDAD: Esta consulta es fundamental para el sistema, ya que provee una vista
-- completa de un paciente, uniendo sus datos personales, su historia clínica y el
-- médico tratante. Es la consulta base para la pantalla "Ver Detalle de Paciente".
-- Filtra por `eliminado = FALSE` para mostrar solo los registros activos.
SELECT
    p.id AS paciente_id,
    per.dni,
    -- Se utiliza CONCAT para presentar el nombre completo en un formato legible.
    CONCAT (per.apellido, ', ', per.nombre) AS nombre_completo,
    -- Se utiliza TIMESTAMPDIFF para calcular la edad actual del paciente dinámicamente.
    TIMESTAMPDIFF (YEAR, per.fecha_nacimiento, CURDATE ()) AS edad,
    hc.nro_historia,
    gs.simbolo AS grupo_sanguineo,
    -- Se vuelve a usar CONCAT para el nombre del profesional.
    CONCAT (per_prof.apellido, ', ', per_prof.nombre) AS profesional_asignado,
    prof.especialidad
FROM
    -- Se parte de la tabla Paciente, que es el centro de nuestro dominio.
    Paciente p
    -- Se une con Persona (INNER JOIN) porque un Paciente DEBE ser una Persona.
    INNER JOIN Persona per ON p.persona_id = per.id
    -- Se une con HistoriaClinica (LEFT JOIN) porque un Paciente PUEDE NO tener una historia asignada.
    LEFT JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id
    -- Se une con GrupoSanguineo (LEFT JOIN) porque una Historia PUEDE NO tener un grupo sanguíneo definido.
    LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
    -- Se une con Profesional (LEFT JOIN) porque una Historia PUEDE NO tener un profesional asignado.
    LEFT JOIN Profesional prof ON hc.profesional_id = prof.id
    -- Se une por segunda vez a Persona (LEFT JOIN) para obtener los datos del Profesional. Se usa un alias diferente (per_prof).
    LEFT JOIN Persona per_prof ON prof.persona_id = per_prof.id
WHERE
    -- Se filtran los registros marcados con baja lógica para mostrar solo los activos.
    per.eliminado = FALSE
    AND p.eliminado = FALSE
    -- Se ordena el resultado alfabéticamente por apellido y nombre para fácil visualización.
ORDER BY
    per.apellido,
    per.nombre;

-- =====================================================================
-- CONSULTA 2: JOIN - Búsqueda de Pacientes por Especialidad Médica
-- =====================================================================
-- UTILIDAD: Permite a un administrativo o a un médico generar un listado de todos
-- los pacientes que están siendo atendidos por una especialidad específica, por
-- ejemplo, 'Cardiología'. Es útil para auditorías o para organizar turnos.
SELECT
    prof.especialidad,
    -- Se obtienen y formatean los nombres del profesional y del paciente.
    CONCAT (per_pro.apellido, ', ', per_pro.nombre) AS profesional,
    hc.nro_historia,
    per_pac.dni AS dni_paciente,
    CONCAT (per_pac.apellido, ', ', per_pac.nombre) AS nombre_paciente
FROM
    HistoriaClinica hc
    -- Se usan INNER JOIN en toda la cadena porque se buscan pacientes que SÍ O SÍ
    -- están vinculados a un profesional de una especialidad concreta.
    INNER JOIN Profesional prof ON hc.profesional_id = prof.id
    -- Se une a Persona (con alias per_pro) para obtener los datos del Profesional.
    INNER JOIN Persona per_pro ON prof.persona_id = per_pro.id
    -- Se une a Paciente para encontrar qué paciente posee esa historia clínica.
    INNER JOIN Paciente pac ON hc.id = pac.historia_clinica_id
    -- Se une a Persona (con alias per_pac) para obtener los datos del Paciente.
    INNER JOIN Persona per_pac ON pac.persona_id = per_pac.id
WHERE
    -- Se filtra por una especialidad específica y por historias clínicas activas.
    prof.especialidad = 'Cardiología'
    AND hc.eliminado = FALSE
    -- Se ordena por matrícula para agrupar los pacientes de un mismo médico.
ORDER BY
    prof.matricula;

-- =====================================================================
-- CONSULTA 3: GROUP BY + HAVING - Grupos Sanguíneos Minoritarios
-- =====================================================================
-- UTILIDAD: Esta consulta genera un reporte estadístico para identificar los grupos
-- sanguíneos menos comunes en la base de datos. Es de gran utilidad para
-- campañas de donación de sangre o estudios epidemiológicos.
SELECT
    gs.simbolo AS grupo_sanguineo,
    -- Se usa COUNT(p.id) para contar cuántos pacientes hay en cada grupo.
    COUNT(p.id) AS cantidad_pacientes
FROM
    GrupoSanguineo gs
    -- Se usa LEFT JOIN para incluir incluso aquellos grupos sanguíneos que no tengan ningún paciente.
    LEFT JOIN HistoriaClinica hc ON gs.id = hc.grupo_sanguineo_id
    LEFT JOIN Paciente p ON hc.id = p.historia_clinica_id
WHERE
    -- Se asegura de contar solo pacientes activos.
    p.eliminado = FALSE
    -- Se agrupan las filas por símbolo de grupo sanguíneo para que COUNT() funcione correctamente.
GROUP BY
    gs.simbolo
    -- Se utiliza HAVING para filtrar los GRUPOS ya agregados. WHERE filtra filas antes de agrupar.
HAVING
    COUNT(p.id) <= 19000 -- Mostramos solo grupos con una cantidad menor o igual a un umbral.
    -- Se ordena por cantidad para ver los más raros primero.
ORDER BY
    cantidad_pacientes ASC;

-- =====================================================================
-- CONSULTA 4: SUBCONSULTA - Historias Clínicas sin Profesional Asignado
-- =====================================================================
-- UTILIDAD: Genera un listado de "tareas pendientes" para el personal administrativo,
-- mostrando todas las historias clínicas activas que aún no tienen un médico
-- profesional asignado. Esto ayuda a asegurar que cada paciente reciba seguimiento.
SELECT
    hc.nro_historia,
    hc.antecedentes,
    -- Esta es una subconsulta CORRELACIONADA. Se ejecuta una vez por cada fila
    -- de la consulta principal (HistoriaClinica).
    -- Busca el nombre del paciente cuya historia_clinica_id coincide con el id
    -- de la historia clínica actual (hc.id).
    (
        SELECT
            CONCAT (per.apellido, ', ', per.nombre)
        FROM
            Persona per
            JOIN Paciente p ON per.id = p.persona_id
        WHERE
            p.historia_clinica_id = hc.id
    ) AS paciente
FROM
    HistoriaClinica hc
WHERE
    -- El filtro principal busca las filas donde la clave foránea del profesional es nula.
    hc.profesional_id IS NULL
    AND hc.eliminado = FALSE;

-- =====================================================================
-- VISTA (VIEW) - Lista Simplificada de Pacientes Activos
-- =====================================================================
-- UTILIDAD: Una vista es una consulta almacenada que se comporta como una tabla virtual.
-- Esta vista simplifica el acceso a los datos más comunes de los pacientes activos.
-- El equipo de desarrollo puede usarla para poblar listados en la aplicación sin
-- tener que escribir el complejo JOIN cada vez, además de abstraer la lógica
-- del filtro de `eliminado = FALSE`.
CREATE
OR REPLACE VIEW vw_pacientes_activos AS
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
    p.eliminado = FALSE
    AND per.eliminado = FALSE;

-- Ejemplo de cómo usar la VISTA: la sintaxis es mucho más simple que la consulta original.
SELECT
    *
FROM
    vw_pacientes_activos
WHERE
    especialidad_medico = 'Pediatría';