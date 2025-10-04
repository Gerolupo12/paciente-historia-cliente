-- INSERCIONES PARA VALIDAR CONSTRAINTS --
SELECT * FROM Persona;
SELECT * FROM Paciente;
SELECT * FROM HistoriaClinica;
SELECT * FROM GrupoSanguineo;

-- Inserción CORRECTA 1: Persona y Paciente con historia clínica válida
INSERT INTO
    Persona (nombre, apellido, dni, fecha_nacimiento)
VALUES
    ('Validación', 'Correcta', '1234567890', '1990-01-01');

INSERT INTO
    Paciente (persona_id, historia_clinica_id)
VALUES
    (LAST_INSERT_ID (), 6); -- LAST_INSERT_ID() es una función que devuelve el valor de la última columna AUTO_INCREMENT que se insertó en una tabla durante la sesión actual de la conexión.

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

-- Inserción ERRÓNEA 7: ID inexistente
INSERT INTO
    Paciente (persona_id, historia_clinica_id)
VALUES
    (LAST_INSERT_ID (), 999);

-- Inserción ERRÓNEA 8: Violación de UNIQUE en nro_historia
INSERT INTO HistoriaClinica (nro_historia, grupo_sanguineo_id)
VALUES ('HC-001', 1); -- nro_historia duplicado

-- Inserción ERRÓNEA 9: Violación de UNIQUE en persona_id (un paciente por persona)
INSERT INTO Paciente (persona_id, historia_clinica_id)
VALUES (1, NULL); -- persona_id 1 ya existe en Paciente

-- Inserción ERRÓNEA 10: Violación de UNIQUE en historia_clinica_id (una historia por paciente)
INSERT INTO Paciente (persona_id, historia_clinica_id)
VALUES (7, 1); -- historia_clinica_id 1 ya está asignada

-- Limpiar inserción problemática del ENUM si se creó
DELETE FROM GrupoSanguineo WHERE (tipo_grupo = '' AND factor_rh != '') OR (tipo_grupo != '' AND factor_rh = '');

SELECT * FROM Persona;
SELECT * FROM Paciente;
SELECT * FROM HistoriaClinica;
SELECT * FROM GrupoSanguineo;
