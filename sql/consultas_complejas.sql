-- =====================================================================
-- SCRIPT DE CONSULTAS AVANZADAS Y REPORTES (ETAPA 3 TFI)
-- Objetivo: Diseñar consultas complejas y vistas para agregar valor
-- al sistema, cumpliendo con los requisitos del trabajo final.
-- =====================================================================
USE GestionPacientes;

-- =====================================================================
-- CONSULTA 1: JOIN - Ficha Completa de Pacientes Activos
-- =====================================================================
-- DESCRIPCIÓN / UTILIDAD:
-- =====================================================================
-- Esta consulta muestra un resultado completo con la información de cada paciente,
-- incluyendo sus datos personales, edad, número de historia clínica, grupo sanguíneo,
-- médico asignado y su especialidad.
-- Es la consulta base utilizada en la pantalla “Ver detalle de paciente”.
-- Su objetivo es reunir en un solo resultado los datos esenciales del paciente activo,
-- asegurando que se muestre información completa incluso si algunos datos relacionados aún no existen.
-- Al estar optimizada con INNER y LEFT JOINs, permite visualizar correctamente cada paciente
-- sin omitir registros activos ni depender de múltiples consultas.
-- =====================================================================

SELECT
    p.id AS paciente_id,
    per.dni,
    -- Se utiliza CONCAT para presentar el nombre completo en un formato legible.
    CONCAT(per.apellido, ', ', per.nombre) AS nombre_completo,
    -- Se utiliza TIMESTAMPDIFF para calcular la edad actual del paciente dinámicamente.
    TIMESTAMPDIFF(YEAR, per.fecha_nacimiento, CURDATE()) AS edad,
    hc.nro_historia,
    gs.simbolo AS grupo_sanguineo,
    -- Se vuelve a usar CONCAT para el nombre del profesional.
    CONCAT(per_prof.apellido, ', ', per_prof.nombre) AS profesional_asignado,
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
-- DESCRIPCIÓN / UTILIDAD:
-- =====================================================================
-- Esta consulta permite obtener un listado de pacientes atendidos por una especialidad médica específica,
-- por ejemplo 'Cardiología'. Incluye datos del paciente, su historia clínica y del profesional asignado,
-- asegurando que se muestren solo pacientes activos con historias clínicas vigentes. 
-- Es útil para auditorías, organización de turnos o revisiones internas de pacientes por área de atención,
-- y garantiza que todos los registros reflejen relaciones completas entre pacientes, historias clínicas y profesionales.
-- =====================================================================

SELECT
    prof.especialidad,
    -- Se obtienen y formatean los nombres del profesional y del paciente.
    CONCAT(per_pro.apellido, ', ', per_pro.nombre) AS profesional,
    hc.nro_historia,
    per_pac.dni AS dni_paciente,
    CONCAT(per_pac.apellido, ', ', per_pac.nombre) AS nombre_paciente
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
-- DESCRIPCIÓN / UTILIDAD:
-- =====================================================================
-- Esta consulta genera un reporte estadístico de los grupos sanguíneos menos comunes
-- en la base de datos, mostrando cuántos pacientes pertenecen a cada grupo.
-- Incluye todos los grupos sanguíneos, incluso aquellos sin pacientes asignados,
-- y filtra únicamente pacientes activos. Es especialmente útil para campañas
-- de donación de sangre, estudios epidemiológicos o análisis de disponibilidad de recursos,
-- permitiendo identificar rápidamente los grupos minoritarios que requieren atención prioritaria.
-- =====================================================================

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
-- CONSULTA 4: SUBCONSULTA - Profesionales con más pacientes que el promedio
-- =====================================================================
-- DESCRIPCIÓN / UTILIDAD:
-- Esta consulta identifica a los profesionales que atienden más pacientes
-- que el promedio general del sistema.
-- Permite detectar sobrecarga asistencial y analizar la distribución de la
-- demanda entre las distintas especialidades.
-- Es útil para la gestión de recursos humanos y planificación de turnos,
-- ayudando a equilibrar la carga de trabajo en el área médica.
-- =====================================================================

SELECT 
    -- Se muestra el nombre completo del profesional en formato “Apellido, Nombre”.
    CONCAT(per.apellido, ', ', per.nombre) AS profesional,
    -- Se muestra la especialidad médica del profesional.
    prof.especialidad,
    -- Se cuenta la cantidad total de pacientes atendidos por cada profesional.
    COUNT(pac.id) AS total_pacientes
FROM Profesional prof
    -- Se relaciona cada profesional con su persona correspondiente (datos personales).
    JOIN Persona per ON prof.persona_id = per.id
    -- Se une con la historia clínica para conocer qué historias están a cargo del profesional.
    JOIN HistoriaClinica hc ON hc.profesional_id = prof.id
    -- Se une con los pacientes asociados a esas historias clínicas.
    JOIN Paciente pac ON pac.historia_clinica_id = hc.id
-- Se agrupa los resultados por profesional y especialidad para consolidar los conteos.
GROUP BY prof.id, profesional, prof.especialidad
-- Se aplica una subconsulta que calcula el promedio de pacientes por profesional,
-- y filtra solo a quienes superan ese promedio general.
HAVING COUNT(pac.id) > (
    SELECT AVG(pacientes_por_prof)
    FROM (
        -- Subconsulta interna: calcula cuántos pacientes tiene cada profesional.
        SELECT COUNT(p2.id) AS pacientes_por_prof
        FROM Profesional prof2
        JOIN HistoriaClinica hc2 ON hc2.profesional_id = prof2.id
        JOIN Paciente p2 ON p2.historia_clinica_id = hc2.id
        GROUP BY prof2.id
    ) AS sub
)
-- Se ordena los resultados mostrando primero a los profesionales con más pacientes.
ORDER BY total_pacientes DESC;

-- =====================================================================
-- VISTA (VIEW) - Lista Simplificada de Pacientes Activos
-- =====================================================================
-- DESCRIPCIÓN / UTILIDAD:
-- Esta vista proporciona un acceso simplificado a la información esencial de pacientes activos,
-- incluyendo ID, DNI, nombre y apellido, número de historia clínica y la especialidad del médico asignado.
-- Permite que desarrolladores o el equipo administrativo generen listados y filtros fácilmente
-- (por ejemplo, todos los pacientes de 'Pediatría') sin tener que repetir los JOINs complejos ni
-- preocuparse por el filtro de pacientes eliminados. Actúa como una “tabla virtual” que abstrae
-- la lógica de la consulta original, facilitando mantenimiento y consistencia de la información.
-- =====================================================================

-- Se crea o reemplaza la vista vw_pacientes_activos para simplificar consultas sobre pacientes activos.
CREATE OR REPLACE VIEW vw_pacientes_activos AS
SELECT
    -- Se obtiene el ID del paciente.
    p.id AS paciente_id,
    -- Se muestra el DNI de la persona asociada al paciente.
    per.dni,
    -- Se muestra el nombre de la persona.
    per.nombre,
    -- Se muestra el apellido de la persona.
    per.apellido,
    -- Se muestra el número de historia clínica del paciente.
    hc.nro_historia,
    -- Se muestra la especialidad del médico que atiende al paciente.
    prof.especialidad AS especialidad_medico
FROM
    Paciente p
    -- Se relaciona cada paciente con sus datos personales.
    INNER JOIN Persona per ON p.persona_id = per.id
    -- Se une con la historia clínica si el paciente tiene una asignada.
    LEFT JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id
    -- Se une con el profesional que atiende la historia clínica, si existe.
    LEFT JOIN Profesional prof ON hc.profesional_id = prof.id
-- Se filtran los pacientes que no han sido eliminados y cuyas personas asociadas tampoco lo están.
WHERE
    p.eliminado = FALSE
    AND per.eliminado = FALSE;

-- Se muestra un ejemplo de uso de la vista, donde la sintaxis es más simple que la consulta original.
SELECT *
FROM
    vw_pacientes_activos
-- Se filtran los pacientes cuyo médico tiene especialidad en Pediatría.
WHERE
    especialidad_medico = 'Pediatría';
    
    
