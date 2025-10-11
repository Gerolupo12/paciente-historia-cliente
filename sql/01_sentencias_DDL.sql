-- Crear base de datos GestionPacientes
CREATE DATABASE IF NOT EXISTS GestionPacientes;

USE GestionPacientes;

-- Tabla Persona
CREATE TABLE
    Persona (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        eliminado BOOLEAN DEFAULT FALSE,
        nombre VARCHAR(80) NOT NULL,
        apellido VARCHAR(80) NOT NULL,
        dni VARCHAR(15) UNIQUE NOT NULL,
        fecha_nacimiento DATE NULL,
        -- Constraints
        CONSTRAINT chk_longitud_dni CHECK (LENGTH (dni) BETWEEN 7 AND 15),
        CONSTRAINT chk_anio_minimo_1900 CHECK (YEAR (fecha_nacimiento) > 1900)
    );

-- Tabla GrupoSanguineo
CREATE TABLE
    GrupoSanguineo (
        id INT PRIMARY KEY AUTO_INCREMENT,
        tipo_grupo ENUM ('A', 'B', 'AB', 'O') NOT NULL,
        factor_rh ENUM ('+', '-') NOT NULL,
        simbolo VARCHAR(3) AS (CONCAT (tipo_grupo, factor_rh)) STORED, -- Crea una columna calculada. STORED asegura que el resultado de esta concatenación se almacene físicamente en la tabla
        -- Constraints
        CONSTRAINT uk_grupo_factor UNIQUE (tipo_grupo, factor_rh) -- Evita duplicados en la lógica de negocio (combinación tipo + factor). Ej: No puede haber dos registros con (A, +)
    );

-- Tabla Profesional
CREATE TABLE
    Profesional (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        eliminado BOOLEAN DEFAULT FALSE,
        persona_id BIGINT UNIQUE NOT NULL,
        matricula VARCHAR(20) NOT NULL UNIQUE,
        especialidad VARCHAR(80) NOT NULL,
        -- Constraints
        CONSTRAINT fk_profesional_persona_id FOREIGN KEY (persona_id) REFERENCES Persona (id) ON DELETE CASCADE,
        CONSTRAINT chk_longitud_matricula CHECK (LENGTH (matricula) BETWEEN 5 AND 20)
    );

-- Tabla HistoriaClinica
CREATE TABLE
    HistoriaClinica (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        eliminado BOOLEAN DEFAULT FALSE,
        nro_historia VARCHAR(20) NOT NULL UNIQUE,
        grupo_sanguineo_id INT NULL,
        antecedentes TEXT NULL,
        medicacion_actual TEXT NULL,
        observaciones TEXT NULL,
        profesional_id BIGINT NULL,
        -- Constraints
        CONSTRAINT fk_grupo_sanguineo_id FOREIGN KEY (grupo_sanguineo_id) REFERENCES GrupoSanguineo (id) ON DELETE SET NULL,
        CONSTRAINT fk_profesional_id FOREIGN KEY (profesional_id) REFERENCES Profesional (id) ON DELETE SET NULL,
        CONSTRAINT chk_formato_numero_historia CHECK (nro_historia RLIKE '^HC-[0-9]{4,}$')
    );

-- Tabla Paciente
CREATE TABLE
    Paciente (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        eliminado BOOLEAN DEFAULT FALSE,
        persona_id BIGINT UNIQUE NOT NULL,
        historia_clinica_id BIGINT UNIQUE NULL,
        -- Constraints
        CONSTRAINT fk_paciente_persona_id FOREIGN KEY (persona_id) REFERENCES Persona (id) ON DELETE CASCADE,
        CONSTRAINT fk_historia_clinica_id FOREIGN KEY (historia_clinica_id) REFERENCES HistoriaClinica (id) ON DELETE SET NULL
    );