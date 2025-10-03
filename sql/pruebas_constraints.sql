-- INSERCIONES PARA VALIDAR CONSTRAINTS --
-- Inserción CORRECTA 1: Persona y Paciente con historia clínica válida
INSERT INTO
    Persona (nombre, apellido, dni, fecha_nacimiento)
VALUES
    ('Validación', 'Correcta', '1234567890', '1990-01-01');

INSERT INTO
    Paciente (persona_id, historia_clinica_id)
VALUES
    (LAST_INSERT_ID (), 6);

-- Inserción CORRECTA 2: Historia clínica con formato válido
INSERT INTO
    HistoriaClinica (nro_historia, grupo_sanguineo_id)
VALUES
    ('HC-VALIDO', 1);

-- Inserción ERRÓNEA 1: Violación UNIQUE en DNI
INSERT INTO
    Persona (nombre, apellido, dni, fecha_nacimiento)
VALUES
    ('Duplicado', 'DNI', '12345678', '1990-01-01');

-- Inserción ERRÓNEA 2: Violación CHECK en fecha pasada
INSERT INTO
    Persona (nombre, apellido, dni, fecha_nacimiento)
VALUES
    ('Fecha', 'Futura', '99999999', '1830-01-01');

-- Inserción ERRÓNEA 3: Violación de ENUM en GrupoSanguineo
INSERT INTO
    GrupoSanguineo (tipo_grupo, factor_rh)
VALUES
    ('X', '+');

-- Inserción ERRÓNEA 4: Violación de ENUM en GrupoSanguineo
INSERT INTO
    GrupoSanguineo (tipo_grupo, factor_rh)
VALUES
    ('A', 'X');

-- Inserción ERRÓNEA 5: DNI demasiado corto
INSERT INTO
    Persona (nombre, apellido, dni, fecha_nacimiento)
VALUES
    ('DNI', 'Corto', '123', '1990-01-01');

-- Inserción ERRÓNEA 6: DNI demasiado largo
INSERT INTO
    Persona (nombre, apellido, dni, fecha_nacimiento)
VALUES
    ('DNI', 'Largo', '1234567890123456', '1990-01-01');

-- Inserción ERRÓNEA 7: Violación FOREIGN KEY (historia_clinica_id inexistente)
INSERT INTO
    Persona (nombre, apellido, dni, fecha_nacimiento)
VALUES
    ('Test', 'FK', '11111111', '1990-01-01');

-- Inserción ERRÓNEA 8: ID inexistente
INSERT INTO
    Paciente (persona_id, historia_clinica_id)
VALUES
    (LAST_INSERT_ID (), 999);