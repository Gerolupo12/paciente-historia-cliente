-- =====================================================================
-- INSERTAR DATOS EN LA TABLA MAESTRA 'GrupoSanguineo'
-- =====================================================================
-- Se insertan los 8 registros únicos que representan todos los posibles
-- grupos sanguíneos. Esta tabla actúa como un catálogo.
INSERT INTO
    GrupoSanguineo (tipo_grupo, factor_rh, nombre_enum)
VALUES
    ('A', '+', 'A_PLUS'),
    ('A', '-', 'A_MINUS'),
    ('B', '+', 'B_PLUS'),
    ('B', '-', 'B_MINUS'),
    ('AB', '+', 'AB_PLUS'),
    ('AB', '-', 'AB_MINUS'),
    ('O', '+', 'O_PLUS'),
    ('O', '-', 'O_MINUS');