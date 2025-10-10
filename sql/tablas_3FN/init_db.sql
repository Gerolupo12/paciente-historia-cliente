-- INSERCIONES CORRECTAS --
-- Insertar grupos sanguíneos primero
INSERT INTO
    GrupoSanguineo (tipo_grupo, factor_rh)
VALUES
    ('A', '+'),
    ('A', '-'),
    ('B', '+'),
    ('B', '-'),
    ('AB', '+'),
    ('AB', '-'),
    ('O', '+'),
    ('O', '-');

-- Obtener los IDs reales de los grupos sanguíneos recién insertados
SET @A_PLUS = (SELECT id FROM GrupoSanguineo WHERE tipo_grupo = 'A' AND factor_rh = '+');
SET @A_MINUS = (SELECT id FROM GrupoSanguineo WHERE tipo_grupo = 'A' AND factor_rh = '-');
SET @B_PLUS = (SELECT id FROM GrupoSanguineo WHERE tipo_grupo = 'B' AND factor_rh = '+');
SET @B_MINUS = (SELECT id FROM GrupoSanguineo WHERE tipo_grupo = 'B' AND factor_rh = '-');
SET @AB_PLUS = (SELECT id FROM GrupoSanguineo WHERE tipo_grupo = 'AB' AND factor_rh = '+');
SET @AB_MINUS = (SELECT id FROM GrupoSanguineo WHERE tipo_grupo = 'AB' AND factor_rh = '-');
SET @O_PLUS = (SELECT id FROM GrupoSanguineo WHERE tipo_grupo = 'O' AND factor_rh = '+');
SET @O_MINUS = (SELECT id FROM GrupoSanguineo WHERE tipo_grupo = 'O' AND factor_rh = '-');

-- personas
INSERT INTO
    Persona (nombre, apellido, dni, fecha_nacimiento)
VALUES
    ('Juan Carlos', 'Pérez', '12345678', '1980-05-15'),
    ('María Elena', 'Gómez', '87654321', '1990-08-22'),
    ('Carlos Alberto', 'López', '11223344', '1975-12-10');

-- historias clínicas
INSERT INTO
    HistoriaClinica (nro_historia, grupo_sanguineo_id, antecedentes, medicacion_actual, observaciones)
VALUES
    (
        'HC-001',
        @A_PLUS,
        'Ninguno',
        'Ninguna',
        'Paciente sano'
    ),
    (
        'HC-002',
        @O_PLUS,
        'Hipertensión arterial',
        'Losartan 50mg',
        'Control mensual'
    ),
    (
        'HC-003',
        @B_MINUS,
        'Diabetes tipo 2',
        'Metformina 850mg',
        'Control trimestral'
    );

-- pacientes
INSERT INTO
    Paciente (persona_id, historia_clinica_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 3);

-- INSERCIONES EXTRA --
-- historias clínicas adicionales
INSERT INTO
    HistoriaClinica (nro_historia, grupo_sanguineo_id, antecedentes, medicacion_actual, observaciones)
VALUES
    (
        'HC-004', @AB_PLUS,
        'Asma leve',
        'Salbutamol inhalador',
        'Usar en crisis'
    ),
    (
        'HC-005',
        @O_MINUS, 
        'Ninguno',
        'Ninguna',
        'Donante universal'
    ),
    ('HC-006', NULL, 'Cirugía cadera 2010', 'Analgésicos', 'Control post-operatorio');

-- personas adicionales
INSERT INTO
    Persona (nombre, apellido, dni, fecha_nacimiento)
VALUES
    ('Ana María', 'Martínez', '44332211', '1988-03-20'),
    ('Pedro José', 'Ramírez', '55667788', '2000-01-15'),
    ('Laura Beatriz', 'Silva', '99887766', '1995-07-30'),
    ('Roberto Carlos', 'Díaz', '66778899', '1982-11-08');

-- pacientes adicionales (algunos sin historia clínica)
INSERT INTO
    Paciente (persona_id, historia_clinica_id)
VALUES
    (4, 4),
    (5, 5),
    (6, NULL), -- Sin historia clínica
    (7, NULL); -- Sin historia clínica
