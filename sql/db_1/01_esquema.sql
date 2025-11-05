-- =====================================================================
-- SCRIPT DE DEFINICIÓN DE BASE DE DATOS
-- =====================================================================
USE GestionPacientes;

-- =====================================================================
-- PASO 0: LIMPIEZA INICIAL DE LA BASE DE DATOS
-- =====================================================================
-- Se utiliza DROP TABLE para eliminar la tabla  de forma segura.
DROP TABLE IF EXISTS Paciente CASCADE;

DROP TABLE IF EXISTS HistoriaClinica CASCADE;

DROP TABLE IF EXISTS Profesional CASCADE;

DROP TABLE IF EXISTS GrupoSanguineo CASCADE;

DROP TABLE IF EXISTS Persona CASCADE;

-- Se utiliza DROP DATABASE para eliminar la base de datos de forma segura.
DROP DATABASE GestionPacientes IF EXISTS
-- =====================================================================
-- ESQUEMA DE LA BASE DE DATOS: GestionPacientes
-- =====================================================================
-- Crea la base de datos solo si no existe previamente, evitando errores en ejecuciones repetidas.
CREATE DATABASE GestionPacientes;

USE GestionPacientes;

-- =====================================================================
-- TABLA 1: Persona
-- Centraliza los datos personales para evitar redundancia (cumple 3FN).
-- =====================================================================
CREATE TABLE
    Persona (
        -- Clave primaria autoincremental, tipo BIGINT para soportar millones de registros.
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        -- Columna para baja lógica, por defecto en FALSE (activo).
        eliminado BOOLEAN DEFAULT FALSE,
        -- Nombre y apellido, obligatorios.
        nombre VARCHAR(80) NOT NULL,
        apellido VARCHAR(80) NOT NULL,
        -- DNI, debe ser único y obligatorio. La restricción UNIQUE crea un índice automáticamente.
        dni VARCHAR(15) UNIQUE NOT NULL,
        -- Fecha de nacimiento, puede ser nula.
        fecha_nacimiento DATE NULL,
        -- CONSTRAINTS
        -- Valida que el DNI tenga una longitud razonable.
        CONSTRAINT chk_longitud_dni CHECK (LENGTH (dni) BETWEEN 7 AND 15),
        -- Valida que la fecha de nacimiento sea posterior a 1900, evitando fechas claramente erróneas.
        CONSTRAINT chk_anio_minimo_1900 CHECK (YEAR (fecha_nacimiento) > 1900)
    );

-- =====================================================================
-- TABLA 2: GrupoSanguineo
-- Tabla maestra (o de catálogo) para los tipos de sangre.
-- =====================================================================
CREATE TABLE
    GrupoSanguineo (
        id INT PRIMARY KEY AUTO_INCREMENT,
        -- El tipo ENUM restringe los valores a los definidos, garantizando la integridad.
        tipo_grupo ENUM ('A', 'B', 'AB', 'O') NOT NULL,
        factor_rh ENUM ('+', '-') NOT NULL,
        -- Columna generada que concatena tipo y factor (ej. 'A+'). STORED significa que se
        -- almacena físicamente, lo que la hace más eficiente para consultas.
        simbolo VARCHAR(3) AS (CONCAT (tipo_grupo, factor_rh)) STORED,
        -- CONSTRAINTS
        -- Clave única compuesta: evita que exista más de una combinación tipo y factor (ej. no puede haber dos filas 'A' y '+').
        CONSTRAINT uk_grupo_factor UNIQUE (tipo_grupo, factor_rh)
    );

-- =====================================================================
-- TABLA 3: Profesional
-- Representa el rol de un profesional médico, vinculado a una Persona.
-- =====================================================================
CREATE TABLE
    Profesional (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        eliminado BOOLEAN DEFAULT FALSE,
        -- Vínculo a la tabla Persona. Es UNIQUE para asegurar una relación 1 a 1 (una Persona solo puede ser un Profesional una vez).
        persona_id BIGINT UNIQUE NOT NULL,
        matricula VARCHAR(20) NOT NULL UNIQUE,
        especialidad VARCHAR(80) NOT NULL,
        -- CONSTRAINTS
        -- Clave foránea que referencia a Persona. ON DELETE CASCADE significa que si se borra una Persona,
        -- su registro de Profesional asociado también se borrará automáticamente.
        CONSTRAINT fk_profesional_persona_id FOREIGN KEY (persona_id) REFERENCES Persona (id) ON DELETE CASCADE,
        -- Valida que la matrícula siga el formato 'MP-', 'MN-' o 'MI-' seguido de números.
        CONSTRAINT chk_formato_matricula CHECK (matricula RLIKE '^(MP|MN|MI)-[0-9]{5,17}$')
    );

-- =====================================================================
-- TABLA 4: HistoriaClinica
-- Contiene la información médica. Puede existir sin un Paciente asociado inicialmente.
-- =====================================================================
CREATE TABLE
    HistoriaClinica (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        eliminado BOOLEAN DEFAULT FALSE,
        nro_historia VARCHAR(20) NOT NULL UNIQUE,
        grupo_sanguineo_id INT NULL,
        antecedentes TEXT NULL,
        medicacion_actual TEXT NULL,
        observaciones TEXT NULL,
        -- Puede ser nulo si aún no se ha asignado un profesional.
        profesional_id BIGINT NULL,
        -- CONSTRAINTS
        -- Si se borra un GrupoSanguineo, el campo aquí se pondrá en NULL.
        CONSTRAINT fk_hc_grupo_sanguineo_id FOREIGN KEY (grupo_sanguineo_id) REFERENCES GrupoSanguineo (id) ON DELETE SET NULL,
        -- Si se borra un Profesional, su ID en la historia se pondrá en NULL, conservando el registro clínico.
        CONSTRAINT fk_hc_profesional_id FOREIGN KEY (profesional_id) REFERENCES Profesional (id) ON DELETE SET NULL,
        -- Valida que el número de historia siga el formato 'HC-' seguido de números.
        CONSTRAINT chk_formato_numero_historia CHECK (nro_historia RLIKE '^HC-[0-9]{4,17}$')
    );

-- =====================================================================
-- TABLA 5: Paciente
-- Representa el rol de un paciente, vinculando una Persona a una HistoriaClinica.
-- =====================================================================
CREATE TABLE
    Paciente (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        eliminado BOOLEAN DEFAULT FALSE,
        -- Vínculo a Persona. UNIQUE para relación 1 a 1.
        persona_id BIGINT UNIQUE NOT NULL,
        -- Vínculo a HistoriaClinica. UNIQUE para relación 1 a 1. NULL permite crear un paciente sin historia asignada.
        historia_clinica_id BIGINT UNIQUE NULL,
        -- CONSTRAINTS
        -- Si se borra la Persona, el rol de Paciente también se elimina.
        CONSTRAINT fk_paciente_persona_id FOREIGN KEY (persona_id) REFERENCES Persona (id) ON DELETE CASCADE,
        -- Si se borra la HistoriaClinica, el campo aquí se pone en NULL, conservando el registro del paciente.
        CONSTRAINT fk_paciente_historia_clinica_id FOREIGN KEY (historia_clinica_id) REFERENCES HistoriaClinica (id) ON DELETE SET NULL
    );