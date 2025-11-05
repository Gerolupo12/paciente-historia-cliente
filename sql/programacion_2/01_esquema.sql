-- =====================================================================
-- ESQUEMA DE LA BASE DE DATOS: GestionPacientes para Programación II
-- =====================================================================
-- Crea la base de datos solo si no existe previamente, evitando errores en ejecuciones repetidas.
CREATE DATABASE IF NOT EXISTS GestionPacientes;

USE GestionPacientes;

-- =====================================================================
-- TABLA 1: GrupoSanguineo
-- Tabla maestra (o de catálogo) para los tipos de sangre.
-- =====================================================================
CREATE TABLE
    GrupoSanguineo (
        id INT PRIMARY KEY AUTO_INCREMENT,
        -- El tipo ENUM restringe los valores a los definidos, garantizando la integridad.
        tipo_grupo ENUM ('A', 'B', 'AB', 'O') NOT NULL,
        factor_rh ENUM ('+', '-') NOT NULL,
        nombre_enum VARCHAR(8) NOT NULL UNIQUE,
        -- Columna generada que concatena tipo y factor (ej. 'A+'). STORED significa que se
        -- almacena físicamente, lo que la hace más eficiente para consultas.
        simbolo VARCHAR(3) AS (CONCAT (tipo_grupo, factor_rh)) STORED,
        -- CONSTRAINTS
        -- Clave única compuesta: evita que exista más de una combinación tipo y factor (ej. no puede haber dos filas 'A' y '+').
        CONSTRAINT uk_grupo_factor UNIQUE (tipo_grupo, factor_rh)
    );

-- =====================================================================
-- TABLA 2: HistoriaClinica
-- Contiene la información médica. Puede existir sin un Paciente asociado inicialmente.
-- =====================================================================
CREATE TABLE
    HistoriaClinica (
        -- Clave primaria autoincremental.
        id INT PRIMARY KEY AUTO_INCREMENT,
        -- Columna para baja lógica, por defecto en FALSE (activo).
        eliminado BOOLEAN DEFAULT FALSE,
        nro_historia VARCHAR(20) NOT NULL UNIQUE,
        grupo_sanguineo_id INT NULL,
        antecedentes TEXT NULL,
        medicacion_actual TEXT NULL,
        observaciones TEXT NULL,
        -- CONSTRAINTS
        -- Si se borra un GrupoSanguineo, el campo aquí se pondrá en NULL.
        CONSTRAINT fk_hc_grupo_sanguineo_id FOREIGN KEY (grupo_sanguineo_id) REFERENCES GrupoSanguineo (id) ON DELETE SET NULL,
        -- Valida que el número de historia siga el formato 'HC-' seguido de números.
        CONSTRAINT chk_formato_numero_historia CHECK (nro_historia RLIKE '^HC-[0-9]{4,17}$')
    );

-- =====================================================================
-- TABLA 3: Paciente
-- Representa el rol de un paciente, vinculado a una HistoriaClinica.
-- =====================================================================
CREATE TABLE
    Paciente (
        id INT PRIMARY KEY AUTO_INCREMENT,
        eliminado BOOLEAN DEFAULT FALSE,
        -- Nombre y apellido, obligatorios.
        nombre VARCHAR(80) NOT NULL,
        apellido VARCHAR(80) NOT NULL,
        -- DNI, debe ser único y obligatorio. La restricción UNIQUE crea un índice automáticamente.
        dni VARCHAR(15) UNIQUE NOT NULL,
        -- Fecha de nacimiento, puede ser nula.
        fecha_nacimiento DATE NULL,
        -- Vínculo a HistoriaClinica. UNIQUE para relación 1 a 1. NULL permite crear un paciente sin historia asignada.
        historia_clinica_id INT UNIQUE NULL,
        -- CONSTRAINTS
        -- Si se borra la HistoriaClinica, el campo aquí se pone en NULL, conservando el registro del paciente.
        CONSTRAINT fk_paciente_historia_clinica_id FOREIGN KEY (historia_clinica_id) REFERENCES HistoriaClinica (id) ON DELETE SET NULL,
        -- Valida que el DNI tenga una longitud razonable.
        CONSTRAINT chk_longitud_dni CHECK (LENGTH (dni) BETWEEN 7 AND 15),
        -- Valida que la fecha de nacimiento sea posterior a 1900, evitando fechas claramente erróneas.
        CONSTRAINT chk_anio_minimo_1900 CHECK (YEAR (fecha_nacimiento) > 1900)
    );