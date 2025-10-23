-- =====================================================================
-- SCRIPT DE CREACIÓN DE ÍNDICES
-- =====================================================================
USE GestionPacientes;

-- =====================================================================
-- PASO 0: LIMPIEZA INICIAL DE ÍNDICES DE LA BASE DE DATOS
-- =====================================================================
-- Se desactivan temporalmente las revisiones de claves foráneas para
-- permitir el vaciado de tablas en cualquier orden sin errores.
SET
    FOREIGN_KEY_CHECKS = 0;

-- Se utiliza DROP INDEX para eliminar los índices.
DROP INDEX IF EXISTS idx_persona_apellido_nombre ON Persona;

DROP INDEX IF EXISTS idx_profesional_especialidad ON Profesional;

DROP INDEX IF EXISTS idx_historia_clinica_profesional ON HistoriaClinica;

DROP INDEX IF EXISTS idx_paciente_historia_clinica ON Paciente;

-- Se reactivan las revisiones de claves foráneas para mantener la
-- integridad de los datos durante las inserciones.
SET
    FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- Se crean después de las tablas para mantener el script organizado.
-- =====================================================================
-- Índice compuesto para buscar y ordenar personas por apellido y nombre.
CREATE INDEX idx_persona_apellido_nombre ON Persona (apellido, nombre);

-- Índice para filtrar profesionales rápidamente por su especialidad.
CREATE INDEX idx_profesional_especialidad ON Profesional (especialidad);

-- Índices en claves foráneas para acelerar JOINs y búsquedas inversas.
CREATE INDEX idx_historia_clinica_profesional ON HistoriaClinica (profesional_id);

CREATE INDEX idx_paciente_historia_clinica ON Paciente (historia_clinica_id);