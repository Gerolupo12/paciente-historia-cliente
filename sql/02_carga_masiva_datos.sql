-- SCRIPT DE INSERCIÓN MASIVA DE REGISTROS
USE GestionPacientes;

-- LIMPIAR TODO
-- Desactivar temporalmente las revisiones de claves foráneas
SET FOREIGN_KEY_CHECKS = 0;
-- Vacíar tablas de forma eficiente
TRUNCATE TABLE Paciente;
TRUNCATE TABLE HistoriaClinica;
TRUNCATE TABLE Profesional;
TRUNCATE TABLE Persona;
TRUNCATE TABLE GrupoSanguineo;
-- Volver a activar las revisiones
SET FOREIGN_KEY_CHECKS = 1;

-- 1. INSERTAR 8 REGISTROS EN GRUPOS SANGUÍNEOS (TABLA MAESTRA)
INSERT INTO GrupoSanguineo (tipo_grupo, factor_rh) VALUES
('A', '+'), ('A', '-'), ('B', '+'), ('B', '-'),
('AB', '+'), ('AB', '-'), ('O', '+'), ('O', '-');

-- 2. GENERAR 200.000 PERSONAS ALEATORIAMENTE (VERSIÓN MODERNA CON RECURSIVIDAD)
-- Definimos el número inicial para los DNI
SET @dni_inicial = 1000000;

INSERT INTO Persona (nombre, apellido, dni, fecha_nacimiento)
WITH RECURSIVE number_sequence (num) AS (
    -- Inicia la secuencia en 1
    SELECT 1
    UNION ALL
    -- Continúa sumando 1 hasta llegar al límite
    SELECT num + 1 FROM number_sequence WHERE num < 200000
)
SELECT 
    CONCAT(ELT(1 + (num % 20), 
        'Juan', 'María', 'Carlos', 'Ana', 'Luis', 'Laura', 'Pedro', 'Sofía',
        'Miguel', 'Elena', 'Francisco', 'Isabel', 'Javier', 'Carmen', 'Antonio', 
        'Lucía', 'David', 'Patricia', 'José', 'Marta'), ' ', CHAR(65 + (num % 26))),
    CONCAT(ELT(1 + (num % 16),
        'Gómez', 'López', 'Rodríguez', 'García', 'Martínez', 'Pérez', 'Fernández', 
        'González', 'Sánchez', 'Romero', 'Díaz', 'Torres', 'Vázquez', 'Rojas', 
        'Moreno', 'Álvarez'), ' ', CHAR(65 + ((num + 7) % 26))),
    CONCAT('DNI ', LPAD(@dni_inicial + num - 1, 8, '0')),
    DATE_ADD('1940-01-01', INTERVAL (num * 53) % 31025 DAY)
FROM number_sequence;

-- 3. CREAR 2.000 PROFESIONALES
INSERT INTO Profesional (persona_id, matricula, especialidad)
SELECT 
    id,
    CONCAT('MP-', LPAD(ROW_NUMBER() OVER (ORDER BY id), 5, '0')),
    ELT(1 + (ROW_NUMBER() OVER (ORDER BY id) % 10), 
        'Cardiología', 'Pediatría', 'Traumatología', 'Dermatología', 
        'Neurología', 'Oftalmología', 'Ginecología', 'Psiquiatría', 
        'Cirugía', 'Medicina General')
FROM Persona 
ORDER BY id 
LIMIT 2000;

-- 4. CREAR 150.000 HISTORIAS CLÍNICAS CON PROFESIONALES ASIGNADOS (VERSIÓN MODERNA CON RECURSIVIDAD)
INSERT INTO HistoriaClinica (nro_historia, grupo_sanguineo_id, profesional_id, antecedentes, medicacion_actual, observaciones)
WITH RECURSIVE number_sequence (seq) AS (
    SELECT 1
    UNION ALL
    SELECT seq + 1 FROM number_sequence WHERE seq < 150000
)
SELECT 
    CONCAT('HC-', LPAD(seq, 6, '0')),
    1 + ((seq - 1) % 8),
    ((seq - 1) % 2000) + 1, -- LÍNEA MODIFICADA PARA ASIGNACIÓN CÍCLICA
    ELT(1 + (seq % 5), 
        'Antecedentes familiares de diabetes',
        'Alergia a penicilina', 
        'Hipertensión arterial controlada',
        'Antecedentes quirúrgicos: apendicectomía',
        'Sin antecedentes relevantes'),
    ELT(1 + (seq % 4),
        'Aspirina 100mg diarios',
        'Metformina 500mg cada 12 horas', 
        'Atorvastatina 20mg nocturnos',
        'No medicación actual'),
    ELT(1 + (seq % 3),
        'Paciente estable, control en 6 meses',
        'Requiere seguimiento estrecho', 
        'Evolución favorable')
FROM number_sequence;

-- 5. CREAR 150.000 PACIENTES
INSERT INTO Paciente (persona_id, historia_clinica_id)
SELECT 
    p.id,
    hc.id
FROM Persona p
INNER JOIN HistoriaClinica hc ON hc.id = p.id - 2000
WHERE p.id > 2000
LIMIT 150000;

-- VERIFICACIÓN FINAL
SELECT '=== CARGA COMPLETADA - REQUISITOS CUMPLIDOS ===' AS Estado;
SELECT 
    'GrupoSanguineo' AS tabla, COUNT(*) AS cantidad FROM GrupoSanguineo
UNION ALL SELECT 'Persona', COUNT(*) FROM Persona
UNION ALL SELECT 'Profesional', COUNT(*) FROM Profesional  
UNION ALL SELECT 'HistoriaClinica', COUNT(*) FROM HistoriaClinica
UNION ALL SELECT 'Paciente', COUNT(*) FROM Paciente;
