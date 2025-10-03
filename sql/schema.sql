-- Crear base de datos
CREATE DATABASE IF NOT EXISTS gestion_pacientes;

USE gestion_pacientes;

-- Tabla Persona (clase A)
CREATE TABLE
    Persona (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        eliminado BOOLEAN DEFAULT FALSE,
        nombre VARCHAR(80) NOT NULL,
        apellido VARCHAR(80) NOT NULL,
        dni VARCHAR(15) UNIQUE NOT NULL,
        fecha_nacimiento DATE NULL,
        -- Constraints
        CHECK (LENGTH (dni) BETWEEN 7 AND 15),
        CHECK (YEAR (fecha_nacimiento) > 1900)
    );

-- Tabla GrupoSanguineo (clase B)
CREATE TABLE
    GrupoSanguineo (
        id INT PRIMARY KEY AUTO_INCREMENT,
        tipo_grupo ENUM ('A', 'B', 'AB', 'O') NOT NULL,
        factor_rh ENUM ('+', '-') NOT NULL,
        simbolo VARCHAR(3) AS (CONCAT (tipo_grupo, factor_rh)) STORED,
        -- Constraints
        -- Evita duplicados en la lógica de negocio (combinación tipo + factor).
        -- Ej: No puede haber dos registros con (A, +)
        UNIQUE KEY uk_grupo_factor (tipo_grupo, factor_rh)
    );

-- Tabla HistoriaClinica (clase B)
CREATE TABLE
    HistoriaClinica (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        nro_historia VARCHAR(20) NOT NULL UNIQUE,
        grupo_sanguineo_id INT NULL,
        antecedentes TEXT NULL,
        medicacion_actual TEXT NULL,
        observaciones TEXT NULL,
        -- Constraints
        FOREIGN KEY (grupo_sanguineo_id) REFERENCES GrupoSanguineo (id) ON DELETE SET NULL
    );

-- Tabla Paciente (clase A)
CREATE TABLE
    Paciente (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        eliminado BOOLEAN DEFAULT FALSE,
        persona_id BIGINT UNIQUE NOT NULL,
        historia_clinica_id BIGINT UNIQUE NULL,
        -- Constraints
        FOREIGN KEY (persona_id) REFERENCES Persona (id) ON DELETE CASCADE,
        FOREIGN KEY (historia_clinica_id) REFERENCES HistoriaClinica (id) ON DELETE SET NULL
    );