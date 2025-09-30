-- Crear base de datos
CREATE DATABASE IF NOT EXISTS gestion_pacientes;

USE gestion_pacientes;

-- Tabla HistoriaClinica
CREATE TABLE
    IF NOT EXISTS HistoriaClinica (
        id INT PRIMARY KEY AUTO_INCREMENT,
        eliminado BOOLEAN DEFAULT FALSE,
        nro_historia VARCHAR(20) UNIQUE,
        grupo_sanguineo ENUM (
            'A_PLUS',
            'A_MINUS',
            'B_PLUS',
            'B_MINUS',
            'AB_PLUS',
            'AB_MINUS',
            'O_PLUS',
            'O_MINUS'
        ),
        antecedentes TEXT,
        medicacion_actual TEXT,
        observaciones TEXT
    );

-- Tabla Paciente
CREATE TABLE
    IF NOT EXISTS Paciente (
        id INT PRIMARY KEY AUTO_INCREMENT,
        eliminado BOOLEAN DEFAULT FALSE,
        nombre VARCHAR(80) NOT NULL,
        apellido VARCHAR(80) NOT NULL,
        dni VARCHAR(8) NOT NULL UNIQUE,
        fecha_nacimiento DATE,
        historia_clinica INT UNIQUE,
        FOREIGN KEY (historia_clinica) REFERENCES HistoriaClinica (id)
    );