-- INSERCIONES PARA VALIDAR CONSTRAINTS --
-- Inserción CORRECTA 1: Paciente con historia clínica válida
INSERT INTO
    Paciente (
        nombre,
        apellido,
        dni,
        fecha_nacimiento,
        historia_clinica_id
    )
VALUES
    (
        'Validación',
        'Correcta',
        '1234567890',
        '1990-01-01',
        6
    );

-- Inserción CORRECTA 2: Historia clínica con formato válido
INSERT INTO
    HistoriaClinica (nro_historia)
VALUES
    ('HC-VALIDO');

-- Inserción ERRÓNEA 1: Violación UNIQUE en DNI
INSERT INTO
    Paciente (nombre, apellido, dni, fecha_nacimiento)
VALUES
    ('Duplicado', 'DNI', '12345678', '1990-01-01');

-- Inserción ERRÓNEA 2: Violación CHECK en fecha pasada
INSERT INTO
    Paciente (nombre, apellido, dni, fecha_nacimiento)
VALUES
    ('Fecha', 'Futura', '9999999', '1830-01-01');

-- Inserción ERRÓNEA 3: Violación de ENUM
INSERT INTO
    HistoriaClinica (nro_historia, grupo_sanguineo)
VALUES
    ('HC-007', 'A+');