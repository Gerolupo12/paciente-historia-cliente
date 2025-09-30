-- Crear base de datos
CREATE DATABASE IF NOT EXISTS gestion_pacientes;

USE gestion_pacientes;

-- Tabla HistoriaClinica (clase B)
CREATE TABLE
    HistoriaClinica (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        eliminado BOOLEAN DEFAULT FALSE,
        nro_historia VARCHAR(20) UNIQUE NOT NULL,
        grupo_sanguineo ENUM (
            'A_PLUS',
            'A_MINUS',
            'B_PLUS',
            'B_MINUS',
            'AB_PLUS',
            'AB_MINUS',
            'O_PLUS',
            'O_MINUS'
        ) NULL,
        antecedentes TEXT NULL,
        medicacion_actual TEXT NULL,
        observaciones TEXT NULL,
        -- Constraints
        CHECK (LENGTH (nro_historia) >= 4),
        CHECK (nro_historia LIKE 'HC-%')
    );

-- Tabla Paciente (clase A)
CREATE TABLE
    Paciente (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        eliminado BOOLEAN DEFAULT FALSE,
        nombre VARCHAR(80) NOT NULL,
        apellido VARCHAR(80) NOT NULL,
        dni VARCHAR(15) UNIQUE NOT NULL,
        fecha_nacimiento DATE NULL,
        historia_clinica_id BIGINT UNIQUE NULL,
        -- Constraints
        FOREIGN KEY (historia_clinica_id) REFERENCES HistoriaClinica (id) ON DELETE SET NULL ON UPDATE CASCADE,
        CHECK (LENGTH (dni) BETWEEN 7 AND 15),
        CHECK (fecha_nacimiento <= CURDATE ()),
        CHECK (YEAR (fecha_nacimiento) > 1900)
    );