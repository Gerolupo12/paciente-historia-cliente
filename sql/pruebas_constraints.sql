-- SCRIPT DE VALIDACIÓN DE CONSTRAINTS PARA EL ESQUEMA NORMALIZADO
USE GestionPacientes;

/*	=====================================================================
	SECCIÓN 1: INSERCIONES CORRECTAS
	Demuestran el flujo de trabajo válido para crear entidades relacionadas.
	===================================================================== */

-- Inserción CORRECTA 1: Crear una Persona y un Profesional asociado.
-- 1. Crear la Persona.
INSERT INTO Persona (nombre, apellido, dni, fecha_nacimiento) 
VALUES ('Mariana', 'Lopez', '28123456', '1980-05-10');
-- 2. Usar su ID para crear el Profesional.
INSERT INTO Profesional (persona_id, matricula, especialidad) 
VALUES (LAST_INSERT_ID(), 'MP-112233', 'Cardiología');

-- Inserción CORRECTA 2: Crear una Persona, una Historia Clínica y un Paciente.
-- Pasos: 1. Crear la Persona.
INSERT INTO Persona (nombre, apellido, dni, fecha_nacimiento) 
VALUES ('Carlos', 'Gomez', '35987654', '1992-11-20');
SET @persona_carlos_id = LAST_INSERT_ID(); -- Guardamos el ID de la persona para usarlo después
-- 2. Crear la Historia Clínica.
INSERT INTO HistoriaClinica (nro_historia, grupo_sanguineo_id, profesional_id) 
VALUES ('HC-99999', 1, 1); -- Asignamos el primer grupo sanguíneo y el primer profesional
SET @hc_carlos_id = LAST_INSERT_ID(); -- Guardamos el ID de la historia
-- 3. Crear el Paciente vinculando a los dos anteriores.
INSERT INTO Paciente (persona_id, historia_clinica_id) 
VALUES (@persona_carlos_id, @hc_carlos_id);

SELECT '==> Inserciones correctas realizadas con éxito.' AS 'Estado';

/*	=====================================================================
	SECCIÓN 2: INSERCIONES ERRÓNEAS
	Cada una de estas inserciones debe fallar y devolver un error específico.
	===================================================================== */

-- ERROR 1: Violación de UNIQUE en Persona(dni)
-- Razón: El DNI '28123456' ya fue insertado para la profesional Mariana Lopez.
INSERT INTO Persona (nombre, apellido, dni, fecha_nacimiento) 
VALUES ('Juan', 'Perez', '28123456', '1981-01-01');

-- ERROR 2: Violación de CHECK en Persona(fecha_nacimiento)
-- Razón: El año 1899 no cumple la restricción de ser > 1900.
INSERT INTO Persona (nombre, apellido, dni, fecha_nacimiento) 
VALUES ('Ana', 'Antigua', '1111111', '1899-12-31');

-- ERROR 3: Violación de CHECK en Persona(dni)
-- Razón: El DNI '123' es demasiado corto (el mínimo es 7).
INSERT INTO Persona (nombre, apellido, dni, fecha_nacimiento) 
VALUES ('DNI', 'Corto', '123', '2000-01-01');

-- ERROR 4: Violación de UNIQUE en Profesional(matricula)
-- Razón: La matrícula 'MP-112233' ya pertenece a la Dra. Lopez.
INSERT INTO Persona (nombre, apellido, dni) VALUES ('Otro', 'Medico', '99999999');
INSERT INTO Profesional (persona_id, matricula, especialidad) 
VALUES (LAST_INSERT_ID(), 'MP-112233', 'Pediatría');

-- ERROR 5: Violación de FOREIGN KEY en Paciente(persona_id)
-- Razón: Se intenta crear un paciente para una persona con id 210000, que no existe.
INSERT INTO Paciente (persona_id, historia_clinica_id) 
VALUES (210000, NULL);

-- ERROR 6: Violación de UNIQUE en Paciente(persona_id)
-- Razón: La persona con el id de Carlos Gomez ya tiene un registro como Paciente. Una persona no puede ser paciente dos veces.
INSERT INTO Paciente (persona_id, historia_clinica_id) 
VALUES (@persona_carlos_id, NULL);

-- ERROR 7: Violación de UNIQUE en Paciente(historia_clinica_id)
-- Razón: La historia clínica HC-99999 ya está asignada al paciente Carlos Gomez.
INSERT INTO Persona (nombre, apellido, dni) VALUES ('Otra', 'Persona', '88888888');
INSERT INTO Paciente (persona_id, historia_clinica_id) 
VALUES (LAST_INSERT_ID(), @hc_carlos_id);

-- ERROR 8: Violación de CHECK en HistoriaClinica(nro_historia)
-- Razón: El formato 'INVALIDO-123' no cumple con la expresión regular '^HC-[0-9]{4,}$'.
INSERT INTO HistoriaClinica (nro_historia) 
VALUES ('INVALIDO-123');