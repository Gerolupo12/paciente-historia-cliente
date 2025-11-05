-- =====================================================================
-- SCRIPT DE CREACIÓN DE VISTAS Y SUS PRUEBAS
-- =====================================================================
USE GestionPacientes;

-- =====================================================================
-- PASO 0: LIMPIEZA INICIAL DE LA BASE DE DATOS
-- =====================================================================
-- Se utiliza DROP VIEW para eliminar las vistas.
DROP VIEW IF EXISTS vw_pacientes_activos;

DROP VIEW IF EXISTS vw_pacientes_publicos;

DROP VIEW IF EXISTS vw_profesionales_publicos;

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
CREATE
OR REPLACE VIEW vw_pacientes_activos AS
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
SELECT
    *
FROM
    vw_pacientes_activos
    -- Se filtran los pacientes cuyo médico tiene especialidad en Pediatría.
WHERE
    especialidad_medico = 'Pediatría';

-- =====================================================================
-- CREACIÓN DE VISTAS SEGURAS
-- =====================================================================
-- En este bloque se crean dos vistas pensadas para consultas públicas o de sistema.
-- Las vistas muestran solo la información necesaria, ocultando datos personales
-- o sensibles. Esto ayuda a proteger la privacidad sin limitar la funcionalidad.
-- =====================================================================
-- =====================================================================
-- VISTA 1: vw_pacientes_publicos
-- Muestra información general de pacientes sin exponer DNI ni fecha de nacimiento.
-- =====================================================================
CREATE
OR REPLACE VIEW vw_pacientes_publicos AS
SELECT
    pac.id AS id_paciente,
    CONCAT (per.apellido, ', ', per.nombre) AS nombre_completo,
    gs.simbolo AS grupo_sanguineo,
    hc.nro_historia,
    prof.especialidad AS especialidad
FROM
    Paciente pac
    JOIN Persona per ON pac.persona_id = per.id
    LEFT JOIN HistoriaClinica hc ON pac.historia_clinica_id = hc.id
    LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
    LEFT JOIN Profesional prof ON hc.profesional_id = prof.id
WHERE
    pac.eliminado = FALSE;

-- =====================================================================
-- VISTA 2: vw_profesionales_publicos
-- Muestra datos básicos de los profesionales, sin exponer su matrícula completa.
-- =====================================================================
-- Se crea una vista que muestra información general de los profesionales,
-- incluyendo su nombre completo, matrícula (parcialmente enmascarada) y especialidad.
-- Se une la tabla Profesional con Persona para obtener los datos personales,
-- y se excluyen los registros marcados como eliminados.
-- =====================================================================
CREATE
OR REPLACE VIEW vw_profesionales_publicos AS
SELECT
    prof.id AS id_profesional,
    CONCAT (per.apellido, ', ', per.nombre) AS nombre_completo,
    CONCAT (LEFT (prof.matricula, 5), '***') AS matricula_parcial,
    prof.especialidad
FROM
    Profesional prof
    INNER JOIN Persona per ON prof.persona_id = per.id
WHERE
    prof.eliminado = FALSE;

-- Nota:
-- Las vistas permiten compartir información segura con la aplicación o con usuarios
-- de consulta, sin mostrar datos sensibles. También facilitan la administración de
-- permisos, ya que es más sencillo limitar el acceso a una vista que a varias tablas.
-- =====================================================================
-- ---------------------------------------------------------------------
-- VISUALIZACIÓN DE RESULTADOS OBTENIDOS
-- ---------------------------------------------------------------------
-- Vista de pacientes
SELECT
    *
FROM
    vw_pacientes_publicos
LIMIT
    10;

-- Vista de profesionales
SELECT
    *
FROM
    vw_profesionales_publicos
LIMIT
    10;