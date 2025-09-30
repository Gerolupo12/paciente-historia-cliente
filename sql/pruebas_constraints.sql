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
        1
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

-- Inserción ERRÓNEA 2: Violación CHECK en fecha futura
INSERT INTO
    Paciente (nombre, apellido, dni, fecha_nacimiento)
VALUES
    ('Fecha', 'Futura', '9999999', '2030-01-01');