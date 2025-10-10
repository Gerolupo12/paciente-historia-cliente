-- INSERCIONES CORRECTAS --
-- historias clínicas
INSERT INTO
    HistoriaClinica (
        nro_historia,
        grupo_sanguineo,
        antecedentes,
        medicacion_actual,
        observaciones
    )
VALUES
    (
        'HC-001',
        'A_PLUS',
        'Ninguno',
        'Ninguna',
        'Paciente sano'
    ),
    (
        'HC-002',
        'O_PLUS',
        'Hipertensión arterial',
        'Losartan 50mg',
        'Control mensual'
    ),
    (
        'HC-003',
        'B_MINUS',
        'Diabetes tipo 2',
        'Metformina 850mg',
        'Control trimestral'
    );

-- pacientes
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
        'Juan Carlos',
        'Pérez',
        '12345678',
        '1980-05-15',
        1
    ),
    (
        'María Elena',
        'Gómez',
        '87654321',
        '1990-08-22',
        2
    ),
    (
        'Carlos Alberto',
        'López',
        '11223344',
        '1975-12-10',
        3
    );

-- INSERCIONES EXTRA --
-- historias clínicas
INSERT INTO
    HistoriaClinica (
        nro_historia,
        grupo_sanguineo,
        antecedentes,
        medicacion_actual,
        observaciones
    )
VALUES
    (
        'HC-004',
        'AB_PLUS',
        'Asma leve',
        'Salbutamol inhalador',
        'Usar en crisis'
    ),
    (
        'HC-005',
        'O_MINUS',
        'Ninguno',
        'Ninguna',
        'Donante universal'
    ),
    (
        'HC-006',
        NULL,
        'Cirugía cadera 2010',
        'Analgésicos',
        'Control post-operatorio'
    );

-- pacientes (algunos sin historia clínica)
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
        'Ana María',
        'Martínez',
        '44332211',
        '1988-03-20',
        4
    ),
    (
        'Pedro José',
        'Ramírez',
        '55667788',
        '2000-01-15',
        5
    ),
    (
        'Laura Beatriz',
        'Silva',
        '99887766',
        '1995-07-30',
        NULL -- Sin historia clínica
    ),
    (
        'Roberto Carlos',
        'Díaz',
        '66778899',
        '1982-11-08',
        NULL -- Sin historia clínica
    );