-- =====================================================================
-- SCRIPT DE INSERCIÓN GRUPOS SANGUÍNEOS
-- =====================================================================
USE GestionPacientes;

-- =====================================================================
-- PASO 0: LIMPIEZA INICIAL DE LA BASE DE DATOS
-- =====================================================================
-- Se desactivan temporalmente las revisiones de claves foráneas para
-- permitir el vaciado de tablas en cualquier orden sin errores.
SET
    FOREIGN_KEY_CHECKS = 0;

-- Se utiliza TRUNCATE TABLE para vaciar la tabla de forma eficiente.
TRUNCATE TABLE GrupoSanguineo;

-- Se reactivan las revisiones de claves foráneas para mantener la
-- integridad de los datos durante las inserciones.
SET
    FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- PASO 1: INSERTAR DATOS EN LA TABLA MAESTRA 'GrupoSanguineo'
-- =====================================================================
-- Se insertan los 8 registros únicos que representan todos los posibles
-- grupos sanguíneos. Esta tabla actúa como un catálogo.
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