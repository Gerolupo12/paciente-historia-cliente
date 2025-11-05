-- =====================================================================
-- ETAPA 5 – CONCURRENCIA Y TRANSACCIONES
-- =====================================================================
-- En esta etapa se diseñan y ejecutan pruebas que muestran el comportamiento de la 
-- base ante accesos simultaneos, implementando transaccionesy realizando bloqueos,
-- deadlocks y niveles de aislamiento.

-- =====================================================================
-- 1. SIMULACION DE DOS SESIONES PARA GENERAR BLOQUEOS Y DEADLOCKS
-- =====================================================================
-- En este paso se crean dos simulaciones donde pondremos a prueba los bloqueos y
-- deadlocks para poder llevar a cabo las transacciones, y luego documentar
-- los errores que se mostraron
-- =====================================================================
-- SESIÓN 1 
-- ==========================================

-- Usar la base de datos existente del sistema clínico, aqui seleccionamos la base
-- de datos donde estan las tablas de los pacientes.
USE GestionPacientes;

-- INICIAR TRANSACCIÓN EN SESIÓN 1
START TRANSACTION;

SELECT 'SESION 1: Iniciando transacción...' AS estado;

-- BLOQUEO 1: Actualizar paciente 1 (obtiene lock exclusivo)
-- A continuacion actualizamos el telefono del paciente 1 con un numero aleatorio,
-- esta operacion lo que hace es obtener un LOCK EXCLUSIVO en la fila del
-- paciente numero 1
UPDATE historiaclinica 
SET observaciones = 'Actualizado por Sesión 1' 
WHERE id = 1;

SELECT 'SESION 1: Paciente 1 bloqueado, esperando 10 segundos...' AS estado;
-- Espera 10 segundos para dar tiempo a la Sesion 2, para que se ejecute su 
-- UPDATE
SELECT SLEEP(10);  


-- ESTO CAUSARÁ DEADLOCK: Intentar acceder a paciente 2 (bloqueado por Sesión 2)
SELECT 'SESION 1: Intentando acceder a paciente 2...' AS estado;
UPDATE gruposanguineo 
SET tipo_grupo = 'A' 
WHERE id = 1;
-- Aqui lo que hace es intentar actualizar el paciente 2, pero esta BLOQUEADO por  
-- la Sesion 2, esta parte CAUSA EL DEADLOCK porque la sesion 2 tambien esta esperando  
-- por el paciente 1

-- SI LLEGAMOS AQUÍ, LA TRANSACCIÓN FUE EXITOSA (sin deadlock)
SELECT 'SESION 1: Transacción exitosa, haciendo COMMIT' AS resultado;
-- Este comando confirma todos los cambios de la transaccion en la base de datos.
COMMIT; 

SELECT 'SESION 1: Transacción completada' AS finalizado;
-- Mensaje final

-- ==========================================
-- SESIÓN 2 - TERMINAL 2  
-- ==========================================

USE GestionPacientes;

-- INICIAR TRANSACCIÓN EN SESIÓN 2
START TRANSACTION;

SELECT 'SESION 2: Iniciando transacción...' AS estado;

-- BLOQUEO 2: Actualizar paciente 2 (obtiene lock exclusivo)
UPDATE gruposanguineo 
SET factor_rh = '+' 
WHERE id = 1;
-- Aqui actualiza el paciente 2 con un numero aleatorio y obtiene un LOCK
-- EXCLUSIVO en el paciente 2, por lo tanto ahora tenemos en la Sesion
-- 1 al paciente 1 con lock y en la Sesion 2 tenemos con lock al paciente
-- 2.

SELECT 'SESION 2: Paciente 2 bloqueado, intentando acceder a paciente 1...' AS estado;

-- ESTO CAUSARÁ DEADLOCK: Intentar acceder a paciente 1 (bloqueado por Sesión 1)
UPDATE historiaclinica 
SET antecedentes = 'Actualizado por Sesión 2' 
WHERE id = 1;
-- Aca intentamos actualizar el paciente 1, pero esta bloqueado por 
-- Sesion 1.
-- SITUACION DE DEADLOCK:
--  - Sesion 1 esperando por paciente 2 (bloqueado por Sesion 2)
--  - Sesion 2 esperando por paciente 1 (bloqueado por Sesion 1)
-- Lo que pasa a continuacion es que MySQL detecta esto y aborta una 
-- de las transacciones.

-- SI LLEGAMOS AQUÍ, LA TRANSACCIÓN FUE EXITOSA (sin deadlock)
SELECT 'SESION 2: Transacción exitosa, haciendo COMMIT' AS resultado;
COMMIT;

SELECT 'SESION 2: Transacción completada' AS finalizado;
-- Mensaje final

-- ==========================================
-- VERIFICACIÓN - Ejecutar en cualquier sesión después del deadlock
-- ==========================================
-- Verificar transacciones activas
SELECT 'TRANSACCIONES ACTIVAS' AS info;
SHOW PROCESSLIST;
-- Con este comando de SHOW PROCCESLIST mostramos todas las
-- conexiones activas a las bases de datos y verificamos que
-- no queden transacciones colgadas.

-- Verificar información de deadlocks en MySQL
SELECT 'INFORMACIÓN DE DEADLOCKS' AS info;

-- Para MySQL 5.6+ puedes usar:
SHOW ENGINE INNODB STATUS\G
-- Este es un comando especial para poder mostrar el estado 
-- interno de InnoDB, donde incluye informacion sobre el ultimo
-- deadlock detectado.
-- \G formatea la salida verticalmente para mejorar la lectura.

-- ¿QUE SUCEDE DURANTE EL DEADLOCK?
-- Secuencia temporal:
-- Tiempo 0: Sesion 1 bloquea Paciente 1 
-- Tiempo 1: Sesion 1 entra en SLEEP(15) 
-- Tiempo 2: Sesion 2 bloquea Paciente 2 
-- Tiempo 3: Sesion 2 intenta acceder a Paciente 1 (bloqueado)
-- Tiempo 4: Sesion 1 despierta e intenta acceder a Paciente 2 (bloqueado)
-- Tiempo 5: MySQL detecta los BLOQUEOS:

-- Sesion 1 → espera → Paciente 2 → bloqueado por → Sesion 2
-- Sesion 2 → espera → Paciente 1 → bloqueado por → Sesion 1

-- Tiempo 6: MySQL elige una victima (generalmente la transaccion mas nueva)
-- Tiempo 7: Transaccion victima recibe ERROR 1213
-- Tiempo 8: Transaccion ganadora hace COMMIT exitoso

-- Resultado final:
-- Una sesion: Error "Deadlock found" 
-- Otra sesion: COMMIT exitoso 
-- Base de datos: Mantiene consistencia sin datos corruptos

-- =====================================================================
-- 2. IMPLEMENTACION DE RETRY ANTE DEADLOCK EN SQL
-- =====================================================================
-- La implementación de una transacción con manejo de deadlock y reintentos (retry) 
-- se puede realizar tanto a nivel de base de datos (procedimiento almacenado en SQL, 
-- idealmente MySQL dado el archivo adjunto) como a nivel de aplicación (Java/JDBC).

-- A continuación, se presentan ambos ejemplos.

-- 1. Implementación en SQL (Procedimiento Almacenado - MySQL) 💾
-- Este ejemplo utiliza un procedimiento almacenado de MySQL para encapsular la lógica 
-- transaccional, el manejo de errores y los reintentos. Asumiremos que usted tiene una 
-- tabla paciente y se añade una tabla log_errores para el registro.

-- A. Estructura de Tablas Requeridas
-- DDL para la tabla de log (si aún no existe)
CREATE TABLE IF NOT EXISTS log_errores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    procedimiento VARCHAR(100) NOT NULL,
    codigo_error INT,
    mensaje_error TEXT,
    timestamp_log DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Suponiendo que la tabla 'paciente' tiene al menos 'id' y 'nombre'.
-- La DDL para paciente está implícita en su archivo 'gestionpacientes.sql'.

-- B. Procedimiento Almacenado con Retry y Deadlock Handler
-- El procedimiento intenta actualizar el nombre de un paciente. 
-- Utiliza un DECLARE CONTINUE HANDLER para capturar el error de 
-- deadlock (1213 o SQLSTATE '40001') y un bucle WHILE para los reintentos.

DELIMITER $$

CREATE PROCEDURE Transaccion_ActualizarPaciente_ConRetry(
    IN p_paciente_id INT,
    IN p_nuevo_nombre VARCHAR(100)
)
BEGIN
    DECLARE v_intentos INT DEFAULT 0;
    DECLARE v_max_intentos INT DEFAULT 3; -- Máx. 3 intentos (Inicial + 2 reintentos)
    DECLARE v_terminado BOOLEAN DEFAULT FALSE;
    DECLARE v_deadlock_ocurrido BOOLEAN DEFAULT FALSE;

    -- *******************************************************
    -- DECLARE HANDLER para Deadlock (Error 1213 o 40001)
    -- *******************************************************
    DECLARE CONTINUE HANDLER FOR SQLSTATE '40001', 1213
    BEGIN
        SET v_deadlock_ocurrido = TRUE;
        -- 1. Loguear el error de Deadlock
        INSERT INTO log_errores (procedimiento, codigo_error, mensaje_error)
        VALUES ('Transaccion_ActualizarPaciente_ConRetry', 1213, CONCAT('Deadlock detectado en intento ', v_intentos));

        -- 2. ROLLBACK: La transacción es abortada por el motor, 
        -- pero un ROLLBACK explícito es buena práctica para liberar 
        -- cualquier lock residual antes de reintentar.
        ROLLBACK;
    END;

    -- *******************************************************
    -- Lógica de Retry
    -- *******************************************************
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED; -- Nivel de aislamiento recomendado
    SET autocommit = 0;

    WHILE v_intentos < v_max_intentos AND v_terminado = FALSE DO
        SET v_intentos = v_intentos + 1;
        SET v_deadlock_ocurrido = FALSE;

        START TRANSACTION;

        -- 1. Lógica de Negocio: Actualización
        UPDATE paciente
        SET nombre = p_nuevo_nombre
        WHERE id = p_paciente_id;

        -- 2. Opcional: Otra operación que podría causar un lock (ejemplo)
        -- INSERT INTO otra_tabla (paciente_id, ...) VALUES (p_paciente_id, ...);

        -- Verificar si ocurrió el deadlock en el handler
        IF v_deadlock_ocurrido THEN
            -- Deadlock ocurrió: ROLLBACK ya ejecutado en el HANDLER
            
            IF v_intentos < v_max_intentos THEN
                -- 3. Aplicar Backoff Breve (pausa, e.g., 0.5 segundos)
                DO SLEEP(0.5);
            END IF;
            -- Continúa al siguiente intento del bucle
        ELSE
            -- No hubo deadlock (u otro error no manejado que abortaría el SP): hacer COMMIT
            COMMIT;
            SET v_terminado = TRUE; -- Éxito, salir del bucle
        END IF;

    END WHILE;

    SET autocommit = 1; -- Restaurar autocommit

    -- *******************************************************
    -- Logging Final si la transacción falló
    -- *******************************************************
    IF v_terminado = FALSE THEN
        INSERT INTO log_errores (procedimiento, codigo_error, mensaje_error)
        VALUES ('Transaccion_ActualizarPaciente_ConRetry', 9999, 'FALLO PERMANENTE: No se pudo completar la transacción después de los reintentos.');
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'ERROR: Fallo permanente en la transacción (Deadlock).';
    END IF;

END$$

DELIMITER ;

-- =====================================================================
-- 3. COMPARACION DE NIVELES DE AISLAMIENTO
-- =====================================================================
-- Claro. La diferencia práctica más clara entre los niveles de aislamiento
-- READ COMMITTED y REPEATABLE READ se observa a través del fenómeno de 
-- Lecturas No Repetibles (Non-Repeatable Reads).

-- A continuación, un ejemplo simple en un escenario concurrente con una tabla 
-- producto que tiene un stock inicial de 100.
-- Este ejemplo requiere de dos sesiones de base de datos (Sesión A y Sesión B) 
-- ejecutando transacciones simultáneamente.

-- 1. Nivel de Aislamiento: READ COMMITTED
-- En este nivel, una transacción solo ve los cambios que ya han sido confirmados
-- (COMMIT) por otras transacciones.
-- ==========================================
-- SESIÓN A   
-- ==========================================
-- Objetivo: Demostrar que la Sesión A leerá un valor diferente la segunda vez.

SET TRANSACTION ISOLATION -- 1
LEVEL READ COMMITTED;	

START TRANSACTION; -- 3

SELECT observaciones FROM 
historiaclinica WHERE id = 1; -- 5

SELECT observaciones FROM 
historiaclinica WHERE id = 1; --8

COMMIT; -- 9
-- ==========================================
-- SESIÓN B   
-- ==========================================

SET autocommit = 0; -- 2

START TRANSACTION; -- 4

UPDATE historiaclinica 
SET observaciones = 'REQUIERE INTERNACION URGENTE' 
WHERE id = 1; -- 6
COMMIT; -- 7

-- Conclusión READ COMMITTED: Con READ COMMITTED, una transacción leerá los 
-- datos más recientes que hayan sido confirmados (COMMIT) por otras transacciones.

-- 2.Nivel de Aislamiento: REPEATABLE READ (Evita Lecturas No Repetibles) 
-- Con REPEATABLE READ, una vez que una transacción lee un registro, se crea una 
-- instantánea (snapshot) de ese registro. Cualquier re-lectura posterior dentro de 
-- la misma transacción devolverá el valor de esa instantánea original, ignorando los 
-- commits de otras transacciones.

-- Objetivo: Demostrar que la Sesión A leerá el mismo valor ambas veces.
-- ==========================================
-- SESIÓN A   
-- ==========================================
-- Objetivo: Demostrar que la Sesión A leerá un valor diferente la segunda vez.

SET TRANSACTION ISOLATION -- 1
LEVEL REPEATABLE READ;	

START TRANSACTION; -- 3

SELECT observaciones FROM 
historiaclinica WHERE id = 1; -- 5

SELECT observaciones FROM 
historiaclinica WHERE id = 1; --8

COMMIT; -- 9
-- ==========================================
-- SESIÓN B   
-- ==========================================

SET autocommit = 0; -- 2

START TRANSACTION; -- 4

UPDATE historiaclinica 
SET observaciones = 'REQUIERE INTERNACION URGENTE' 
WHERE id = 1; -- 6
COMMIT; -- 7

-- Resumen de la Diferencia
-- La diferencia clave es que, bajo REPEATABLE READ, la transacción se comporta 
-- como si la base de datos no hubiera cambiado desde que comenzó. Bajo READ COMMITTED, 
-- la transacción permite que lecturas sucesivas vean cambios confirmados por otras transacciones.

-- =====================================================================
-- 4. BREVE INFORME ACERCA DE CONCURRENCIA Y TRANSACCIONES.
-- =====================================================================
-- Observaciones sobre Concurrencia y Transacciones
-- La gestión de la concurrencia es crucial en sistemas transaccionales como la base de 
-- datos gestionpacientes.

-- Transacciones y Atomicidad: El uso de START TRANSACTION, COMMIT, y ROLLBACK asegura la 
-- atomicidad (propiedad A de ACID), garantizando que las operaciones se completen por 
-- completo o no se apliquen en absoluto, preservando la integridad de los datos.

-- Manejo de Deadlocks: El mecanismo de retry con backoff es esencial para la robustez, ya 
-- que mitiga los fallos por deadlock (código 1213), permitiendo que las transacciones en 
-- conflicto se completen tras un breve retraso.

-- Niveles de Aislamiento: La elección del nivel de aislamiento impacta la concurrencia:

-- READ COMMITTED es más permisivo, permitiendo Lecturas No Repetibles, lo que puede causar 
-- inconsistencias lógicas en transacciones largas (ej. un cálculo basado en dos lecturas).

-- REPEATABLE READ ofrece mayor consistencia al garantizar que las lecturas se repitan dentro
-- de la misma transacción (instantánea de los datos), previniendo las Lecturas No Repetibles, 
-- pero potencialmente reduciendo la concurrencia.