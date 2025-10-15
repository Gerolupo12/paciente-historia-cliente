# Trabajo Final Integrador - Sistema de Gestión de Pacientes e Historias Clínicas

![MySQL](https://img.shields.io/badge/MySQL-8.0.43-blue?logo=mysql) ![MySQL Workbench](https://img.shields.io/badge/MySQL%20Workbench-4479A1.svg?logo=mysql&logoColor=white) ![phpMyAdmin](https://img.shields.io/badge/phpMyAdmin-6C78B3.svg?logo=phpmyadmin&logoColor=white) ![Git](https://img.shields.io/badge/Git-F05032?logo=git&logoColor=white) ![License](https://img.shields.io/badge/license-MIT-green.svg) [![Ver en GitHub](https://img.shields.io/badge/Repositorio-GitHub-black?logo=github)](https://github.com/Gerolupo12/paciente-historia-cliente)

## Datos del Proyecto

- **Asignatura**: Bases de Datos I
- **Dominio**: Paciente → HistoriaClínica (Relación 1→1 unidireccional)

## Integrantes

Este proyecto fue desarrollado de manera colaborativa por el siguiente equipo:

- **Lagos, Alejandro** - [GitHub](https://github.com/Alejandrovans)
- **Lahoz, Cristian** - [GitHub](https://github.com/m415x)
- **Maldonado, Ariana** - [GitHub](https://github.com/AriMaldo19)
- **Ramallo, Gerónimo** - [GitHub](https://github.com/Gerolupo12)

---

## Scripts Completos

- [`sentencias_creacion.sql`](../sql/sentencias_creacion.sql) - Creación de tablas, constraints e índices
- [`carga_masiva_datos.sql`](../sql/carga_masiva_datos.sql) - Generación de datos masivos con SQL puro
- [`validacion_constraints.sql`](../sql/validacion_constraints.sql) - Inserciones para validación de restricciones
- [`consultas_avanzadas.sql`](../sql/consultas_avanzadas.sql) - Consultas avanzadas con JOINs

---

## Etapa 1 - Modelado y Definición de Constraints

### 1. Modelo Original (Pre-Normalización)

#### 1.1 Diagrama ER Original

```mermaid
erDiagram
    direction TB
    Paciente {
        id BIGINT PK "AUTO_INCREMENT"
        eliminado BOOLEAN "DEFAULT FALSE"
        nombre VARCHAR(80) "NOT NULL"
        apellido VARCHAR(80) "NOT NULL"
        dni VARCHAR(15) "NOT NULL, UNIQUE, CHECK (LENGTH (dni) BETWEEN 7 AND 15)"
        fecha_nacimiento DATE "NULL, CHECK (YEAR (fecha_nacimiento) > 1900)"
        historia_clinica_id BIGINT FK "UNIQUE, ON DELETE SET NULL, ON UPDATE CASCADE"
    }

    HistoriaClinica {
        id BIGINT PK "AUTO_INCREMENT"
        eliminado BOOLEAN "DEFAULT FALSE"
        nro_historia VARCHAR(20) "UNIQUE, NOT NULL, CHECK (LENGTH (nro_historia) >= 4), CHECK (nro_historia LIKE 'HC-%')"
        grupo_sanguineo ENUM "(A_PLUS, A_MINUS, B_PLUS, B_MINUS, AB_PLUS, AB_MINUS, O_PLUS, O_MINUS), NULL"
        antecedentes TEXT "NULL"
        medicacion_actual TEXT "NULL"
        observaciones TEXT "NULL"
        profesional_id BIGINT FK "UNIQUE, ON DELETE SET NULL, ON UPDATE CASCADE"
    }

    Profesional {
        id BIGINT PK "AUTO_INCREMENT"
        eliminado BOOLEAN "DEFAULT FALSE"
        nombre VARCHAR(80) "NOT NULL"
        apellido VARCHAR(80) "NOT NULL"
        dni VARCHAR(15) "NOT NULL, UNIQUE, CHECK (LENGTH (dni) BETWEEN 7 AND 15)"
        fecha_nacimiento DATE "NULL, CHECK (YEAR (fecha_nacimiento) > 1900)"
        matricula VARCHAR(20) "NOT NULL, UNIQUE"
        especialidad VARCHAR(40) "NOT NULL"
    }

    Paciente }o--|| HistoriaClinica : tiene
    Profesional }o--|| HistoriaClinica : tiene
```

#### 1.2 Problemas Detectados en el Modelo Original

##### Violaciones de la 3FN

1. **Dependencias Transitivas en `Paciente` y `Profesional`:**

   ```plaintext
   id → nombre, apellido, dni, fecha_nacimiento
   dni → nombre, apellido, fecha_nacimiento  (Dependencia transitiva)
   ```

2. **Mala Representación de Dominios:**

   - No hay separación entre entidad "Persona" y el rol "Paciente" o "Profesional"
   - El grupo sanguíneo debería ser una entidad propia

3. **Falta de Escalabilidad:**

   - Difícil agregar otros roles (ej. Administrativo, Enfermero) en el futuro
   - Búsquedas ineficientes por falta de normalización

### 2. Proceso de Normalización a 3FN

#### 2.1 Primera Forma Normal (1FN)

**Ya cumplida** - Todos los atributos son atómicos, no hay grupos repetitivos.

#### 2.2 Segunda Forma Normal (2FN)

**Ya cumplida** - Todas las dependencias son completas de la clave primaria.

#### 2.3 Tercera Forma Normal (3FN)

##### Proceso de eliminación de dependencias transitivas

###### Paso 1: Identificar dependencias transitivas

```plaintext
En Paciente:
id → dni → (nombre, apellido, fecha_nacimiento)  ← DEPENDENCIA TRANSITIVA

En HistoriaClinica:
grupo_sanguineo ENUM contiene información compuesta (tipo + factor Rh)

En Profesional:
id → dni → (nombre, apellido, fecha_nacimiento)  ← DEPENDENCIA TRANSITIVA
```

###### Paso 2: Crear o modificar tablas para eliminar transitividades

- **Tabla `Persona`**: Contiene datos personales básicos
- **Tabla `Paciente`**: Solo mantiene relaciones (FK)
- **Tabla `Profesional`**: Contiene los datos del profesional y relación con Persona (FK)
- **Tabla `HistoriaClinica`**: Contiene datos clínicos y relación con GrupoSanguineo (FK)
- **Tabla `GrupoSanguineo`**: Contiene los grupos sanguíneos

### 3. Modelo Normalizado (3FN)

#### 3.1 Diagrama ER Normalizado

```mermaid
erDiagram
    Persona {
        id BIGINT PK "AUTO_INCREMENT"
        eliminado BOOLEAN "DEFAULT FALSE"
        nombre VARCHAR(80) "NOT NULL"
        apellido VARCHAR(80) "NOT NULL"
        dni VARCHAR(15) "NOT NULL, UNIQUE, CHECK (LENGTH(dni) BETWEEN 7 AND 15)"
        fecha_nacimiento DATE "NULL, CHECK (YEAR (fecha_nacimiento) > 1900)"
    }

    Paciente {
        id BIGINT PK "AUTO_INCREMENT"
        eliminado BOOLEAN "DEFAULT FALSE"
        persona_id BIGINT FK "NOT NULL, UNIQUE"
        historia_clinica_id BIGINT FK "NULL, UNIQUE"
    }

    Profesional {
        id BIGINT PK "AUTO_INCREMENT"
        eliminado BOOLEAN "DEFAULT FALSE"
        matricula VARCHAR(20) "NOT NULL, UNIQUE, CHECK (matricula RLIKE '^(MP|MN|MI)-[0-9]{5,17}$')"
        especialidad VARCHAR(80) "NOT NULL"
        persona_id BIGINT FK "NOT NULL, UNIQUE"
    }

    HistoriaClinica {
        id BIGINT PK "AUTO_INCREMENT"
        eliminado BOOLEAN "DEFAULT FALSE"
        nro_historia VARCHAR(20) "NOT NULL, UNIQUE, CHECK (nro_historia RLIKE '^HC-[0-9]{4,17}$')"
        grupo_sanguineo_id INT FK "NULL"
        antecedentes TEXT "NULL"
        medicacion_actual TEXT "NULL"
        observaciones TEXT "NULL"
        profesional_id BIGINT FK "NULL, UNIQUE"
    }

    GrupoSanguineo {
        id INT PK "AUTO_INCREMENT"
        tipo_grupo ENUM "(A, B, AB, O), NOT NULL"
        factor_rh ENUM "(+, -), NOT NULL"
        simbolo VARCHAR(3) "AS (CONCAT(tipo_grupo, factor_rh)) STORED"
    }

    Persona ||--o| Paciente : "es"
    Persona ||--o| Profesional : "es"
    Paciente ||--o| HistoriaClinica : "tiene"
    HistoriaClinica }o--|| Profesional : "tiene"
    HistoriaClinica }o--|| GrupoSanguineo : "pertenece"
```

#### 3.2 Estructura de Tablas Normalizadas

##### Tabla `Persona`

```sql
CREATE TABLE Persona (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    eliminado BOOLEAN DEFAULT FALSE,
    nombre VARCHAR(80) NOT NULL,
    apellido VARCHAR(80) NOT NULL,
    dni VARCHAR(15) UNIQUE NOT NULL,
    fecha_nacimiento DATE NULL,
    CONSTRAINT chk_longitud_dni CHECK (LENGTH(dni) BETWEEN 7 AND 15),
    CONSTRAINT chk_anio_minimo_1900 CHECK (YEAR(fecha_nacimiento) > 1900)
);
```

##### Tabla `GrupoSanguineo`

```sql
CREATE TABLE GrupoSanguineo (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tipo_grupo ENUM ('A', 'B', 'AB', 'O') NOT NULL,
    factor_rh ENUM ('+', '-') NOT NULL,
    simbolo VARCHAR(3) AS (CONCAT(tipo_grupo, factor_rh)) STORED,
    CONSTRAINT uk_grupo_factor UNIQUE (tipo_grupo, factor_rh)
);
```

##### Tabla `Profesional`

```sql
CREATE TABLE Profesional (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    eliminado BOOLEAN DEFAULT FALSE,
    persona_id BIGINT UNIQUE NOT NULL,
    matricula VARCHAR(20) NOT NULL UNIQUE,
    especialidad VARCHAR(80) NOT NULL,
    CONSTRAINT fk_profesional_persona_id FOREIGN KEY (persona_id) REFERENCES Persona(id) ON DELETE CASCADE,
    CONSTRAINT chk_formato_matricula CHECK (matricula RLIKE '^(MP|MN|MI)-[0-9]{5,17}$')
);
```

##### Tabla `HistoriaClinica`

```sql
CREATE TABLE HistoriaClinica (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    eliminado BOOLEAN DEFAULT FALSE,
    nro_historia VARCHAR(20) NOT NULL UNIQUE,
    grupo_sanguineo_id INT NULL,
    antecedentes TEXT NULL,
    medicacion_actual TEXT NULL,
    observaciones TEXT NULL,
    profesional_id BIGINT NULL,
    CONSTRAINT fk_grupo_sanguineo_id FOREIGN KEY (grupo_sanguineo_id) REFERENCES GrupoSanguineo(id) ON DELETE SET NULL,
    CONSTRAINT fk_profesional_id FOREIGN KEY (profesional_id) REFERENCES Profesional(id) ON DELETE SET NULL,
    CONSTRAINT chk_formato_numero_historia CHECK (nro_historia RLIKE '^HC-[0-9]{4,17}$')
);
```

##### Tabla `Paciente` (Solo relaciones)

```sql
CREATE TABLE Paciente (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    eliminado BOOLEAN DEFAULT FALSE,
    persona_id BIGINT UNIQUE NOT NULL,
    historia_clinica_id BIGINT UNIQUE NULL,
    CONSTRAINT fk_paciente_persona_id FOREIGN KEY (persona_id) REFERENCES Persona(id) ON DELETE CASCADE,
    CONSTRAINT fk_historia_clinica_id FOREIGN KEY (historia_clinica_id) REFERENCES HistoriaClinica(id) ON DELETE SET NULL
);
```

### 4 Decisiones de Diseño

El diseño del esquema relacional se fundamentó en los principios de normalización e integridad para garantizar la robustez y consistencia de los datos.

### 1. Normalización a 3FN mediante la Entidad `Persona`

La decisión estructural más importante fue descomponer las tablas originales para alcanzar la Tercera Forma Normal (3FN).

- **Justificación:** Se identificó una dependencia transitiva en la tabla `Paciente` original, donde la clave primaria `id` determinaba el `dni`, y el `dni` a su vez determinaba `nombre` y `apellido`. Para resolverlo, se creó una entidad `Persona` que centraliza los datos personales y las tablas `Paciente` y `Profesional` pasaron a ser roles que referencian a `Persona`. Esto elimina la redundancia y previene anomalías de actualización.

### 2. Garantía de la Relación 1→1 a Nivel de Base de Datos

Para forzar la relación 1 a 1 entre un `Paciente` y su `HistoriaClinica` de manera infalible, se utilizó una combinación de restricciones.

- **Justificación:** En la tabla `Paciente`, la columna `historia_clinica_id` no solo es una `FOREIGN KEY` que apunta a `HistoriaClinica(id)`, sino que también posee una restricción `UNIQUE`. Esta combinación es la que asegura a nivel de motor de base de datos que una historia clínica no pueda ser asignada a más de un paciente, protegiendo la integridad del modelo.

### 3. Creación de Tablas Maestras para Dominios

En lugar de usar tipos `ENUM` de MySQL o cadenas de texto, se creó una tabla maestra `GrupoSanguineo`.

- **Justificación:** Esta decisión de diseño asegura la integridad de dominio. Centraliza los valores válidos en una única tabla, facilitando su gestión (agregar/modificar tipos de sangre sin alterar la estructura de otras tablas) y permitiendo definir relaciones foráneas. Además, mejora el rendimiento al usar un `INT` como clave foránea en lugar de una cadena.

### 4. Uso de Bajas Lógicas y Políticas de Integridad Referencial (`ON DELETE`)

Todas las tablas incluyen un campo `eliminado` para la baja lógica. Adicionalmente, se definieron políticas explícitas para las claves foráneas.

- **Justificación:** La baja lógica preserva el historial de datos, un requisito común en sistemas de salud. La política `ON DELETE CASCADE` en la relación `Persona` → `Paciente` asegura que si una `Persona` es eliminada, sus roles asociados también lo sean. En cambio, se usó `ON DELETE SET NULL` para `Paciente` → `HistoriaClinica`, permitiendo que una historia clínica pueda quedar sin paciente asignado (por ejemplo, si los datos del paciente se eliminan por una solicitud de privacidad), pero conservando el registro médico.

### 5. Validación de Constraints

#### 5.1 Inserciones Correctas

```sql
-- Inserción 1: Crear una Persona y un Profesional asociado.
INSERT INTO Persona (nombre, apellido, dni, fecha_nacimiento)
VALUES ('Mariana', 'Lopez', '28123456', '1980-05-10');

INSERT INTO Profesional (persona_id, matricula, especialidad)
VALUES (LAST_INSERT_ID(), 'MP-112233', 'Cardiología');

-- Inserción 2: Crear una Persona, una Historia Clínica y un Paciente.
INSERT INTO Persona (nombre, apellido, dni, fecha_nacimiento)
VALUES ('Carlos', 'Gomez', '35987654', '1992-11-20');
SET @persona_carlos_id = LAST_INSERT_ID();

INSERT INTO HistoriaClinica (nro_historia, profesional_id)
VALUES ('HC-99999', 1);
SET @hc_carlos_id = LAST_INSERT_ID();

INSERT INTO Paciente (persona_id, historia_clinica_id)
VALUES (@persona_carlos_id, @hc_carlos_id);
```

![Captura](../anexos/capturas/inserciones_correctas.png)

#### 5.2 Inserciones Erróneas (Validación de Constraints)

```sql
-- ERROR 1: Violación de UNIQUE en Persona(dni)
INSERT INTO Persona (nombre, apellido, dni, fecha_nacimiento)
VALUES ('Juan', 'Perez', '28123456', '1981-01-01');

-- ERROR 2: Violación de CHECK en Persona(fecha_nacimiento)
INSERT INTO Persona (nombre, apellido, dni, fecha_nacimiento)
VALUES ('Ana', 'Antigua', '1111111', '1899-12-31');

-- ERROR 3: Violación de CHECK en Persona(dni)
INSERT INTO Persona (nombre, apellido, dni, fecha_nacimiento)
VALUES ('DNI', 'Corto', '123', '2000-01-01');

-- ERROR 4: Violación de UNIQUE en Profesional(matricula)
INSERT INTO Persona (nombre, apellido, dni) VALUES ('Otro', 'Medico', '99999999');
INSERT INTO Profesional (persona_id, matricula, especialidad)
VALUES (LAST_INSERT_ID(), 'MP-112233', 'Pediatría');

-- ERROR 5: Violación de FOREIGN KEY en Paciente(persona_id)
INSERT INTO Paciente (persona_id, historia_clinica_id)
VALUES (210000, NULL);

-- ERROR 6: Violación de UNIQUE en Paciente(persona_id)
INSERT INTO Paciente (persona_id, historia_clinica_id)
VALUES (@persona_carlos_id, NULL);

-- ERROR 7: Violación de UNIQUE en Paciente(historia_clinica_id)
INSERT INTO Persona (nombre, apellido, dni) VALUES ('Otra', 'Persona', '88888888');
INSERT INTO Paciente (persona_id, historia_clinica_id)
VALUES (LAST_INSERT_ID(), @hc_carlos_id);

-- ERROR 8: Violación de CHECK en HistoriaClinica(nro_historia)
INSERT INTO HistoriaClinica (nro_historia)
VALUES ('INVALIDO-123');
```

![Captura](../anexos/capturas/inserciones_erroneas.png)

### 6. Beneficios de la Normalización

#### 6.1 Cumplimiento de 3FN

| Aspecto                      | Original         | Normalizado    |
| ---------------------------- | ---------------- | -------------- |
| Dependencias Transitivas     | ❌ Presentes     | ✅ Eliminadas  |
| Redundancia de Datos         | ❌ Alta          | ✅ Mínima      |
| Integridad Referencial       | ✅ Básica        | ✅ Robusta     |
| Consistencia con Modelo Java | ❌ Inconsistente | ✅ Consistente |

#### 6.2 Ventajas Operacionales

##### Búsquedas Más Eficientes

```sql
-- ORIGINAL: Búsqueda con muchos campos
SELECT *
FROM Paciente
WHERE dni = '12345678';

-- NORMALIZADO: Búsqueda optimizada con JOINs
SELECT per.nombre, per.apellido, p.*
FROM Paciente p
INNER JOIN Persona per ON p.persona_id = per.id
WHERE dni = '12345678';
```

##### Consultas Analíticas Mejoradas

```sql
-- ESTADÍSTICAS POR GRUPO SANGUÍNEO
SELECT
    gs.simbolo AS grupo_sanguineo,
    COUNT(p.id) AS cantidad_pacientes
FROM
    GrupoSanguineo gs
    LEFT JOIN HistoriaClinica hc ON gs.id = hc.grupo_sanguineo_id
    LEFT JOIN Paciente p ON hc.id = p.historia_clinica_id
WHERE
    p.eliminado = FALSE
GROUP BY
    gs.simbolo
ORDER BY
    cantidad_pacientes DESC;

-- CONSULTA DE FICHA DE PACIENTE POR DNI (CON PROFESIONAL)
SELECT
    hc.nro_historia,
    CONCAT(per_pac.apellido, ', ', per_pac.nombre) AS nombre_paciente,
    CONCAT(per_prof.apellido, ', ', per_prof.nombre) AS nombre_profesional,
    prof.especialidad
FROM HistoriaClinica hc
    JOIN Paciente pac ON hc.id = pac.historia_clinica_id
    JOIN Persona per_pac ON pac.persona_id = per_pac.id
    LEFT JOIN Profesional prof ON hc.profesional_id = prof.id
    LEFT JOIN Persona per_prof ON prof.persona_id = per_prof.id
WHERE
    per_pac.dni = '10002005';

-- ESTADÍSTICAS AVANZADAS POR ESPECIALIDAD MÉDICA
SELECT
    prof.especialidad,
    COUNT(DISTINCT prof.id) AS cantidad_profesionales,
    COUNT(hc.id) AS total_historias_atendidas,
    -- Se usa IF para evitar división por cero si una especialidad no tiene profesionales
    IF(COUNT(DISTINCT prof.id) > 0, COUNT(hc.id) / COUNT(DISTINCT prof.id), 0) AS promedio_pacientes_por_profesional
FROM
    Profesional prof
    LEFT JOIN HistoriaClinica hc ON prof.id = hc.profesional_id AND hc.eliminado = FALSE
WHERE
    prof.eliminado = FALSE
GROUP BY
    prof.especialidad
ORDER BY
    total_historias_atendidas DESC;
```

##### Escalabilidad Futura

- **Múltiples roles**: Fácil agregar `Administrativo`, `Enfermero` reutilizando `Persona`.
- **Datos maestros**: `GrupoSanguineo` puede extenderse con más atributos.
- **Búsquedas cruzadas**: Consultas entre diferentes roles de persona.
- **Gestión profesional:**: Control de matrículas y especialidades médicas.

#### 6.3 Comparativa de Rendimiento

| Operación            | Original      | Normalizado        | Mejora |
| -------------------- | ------------- | ------------------ | ------ |
| Inserción Paciente   | 1 tabla       | 2 tablas           | ⬇️     |
| Búsqueda por DNI     | Tabla grande  | Tabla optimizada   | ⬆️     |
| Consultas analíticas | Scan completo | Índices eficientes | ⬆️     |
| Mantenimiento datos  | Complejo      | Simple             | ⬆️     |

### 7. Conclusión

#### Logros Obtenidos

1. Cumplimiento 3FN completo: eliminación de todas las dependencias transitivas. DER con 5 entidades normalizadas
2. Modelo relacional con constraints completos
3. Escalabilidad mejorada para crecimiento futuro
4. Validación práctica con inserciones
5. Evidencia de interacción con IA

La normalización a 3FN es altamente recomendable para este sistema, ya que los beneficios en mantenibilidad, escalabilidad e integridad de datos superan ampliamente la complejidad adicional inicial.

---

## Etapa 2 - Implementación y Carga Masiva de Datos

### 1. Descripción

El objetivo de esta etapa fue poblar la base de datos normalizada con un gran volumen de registros ficticios pero realistas (más de 500000 en total). Esta carga masiva es fundamental para simular un entorno de producción, lo que nos permite realizar pruebas de rendimiento significativas y evaluar el impacto real de los índices en la optimización de consultas. Todo el proceso de generación de datos se realizó utilizando únicamente sentencias SQL, sin recurrir a lenguajes de programación externos, para demostrar la capacidad del motor de base de datos.

### 2. Metodología y Técnicas Utilizadas

Para generar los datos, se emplearon técnicas avanzadas de SQL para asegurar la eficiencia, aleatoriedad y consistencia de los registros, respetando todas las `constraints` definidas en la Etapa 1.

- **Generación de Secuencias con CTE Recursivos:** En lugar de métodos verbosos, se utilizó `WITH RECURSIVE` para generar secuencias numéricas de forma limpia y moderna. Esta técnica fue la base para crear un número determinado de registros en las tablas `Persona` e `HistoriaClinica`.
- **Aleatoriedad y Variedad de Datos:**
  - Se utilizaron listas con 50 nombres y 50 apellidos para aumentar la variedad.
  - La función `RAND()` se usó para seleccionar nombres y apellidos de forma aleatoria y para generar fechas de nacimiento realistas (con edades entre 18 y 75 años).
- **Garantía de Unicidad:** Para el campo `dni`, que tiene una restricción `UNIQUE`, se utilizó una secuencia numérica predecible (`@dni_inicial + num - 1`) para garantizar que no hubiera duplicados durante la inserción masiva.
- **Distribución Ponderada:** Para que los datos estadísticos fueran realistas, la asignación de `grupo_sanguineo_id` se realizó mediante una distribución ponderada usando una declaración `CASE`. Esto simula la frecuencia real de los tipos de sangre en una población (ej. O+ y A+ son más comunes).

### 3. Pruebas de Rendimiento con Índices

Para demostrar el impacto de una correcta indexación, se midió el tiempo de ejecución de una consulta de búsqueda común con y sin un índice en las columnas `apellido` y `nombre` de la tabla `Persona`.

**Consulta de Prueba:**

```sql
-- Búsqueda de todas las personas con un apellido común
SELECT * FROM Persona WHERE apellido LIKE 'García%';
```

#### Escenario 1: Sin Índice

Primero, nos aseguramos de que el índice no exista y ejecutamos la consulta.

```sql
-- Si el índice existe, lo eliminamos para la prueba
DROP INDEX IF EXISTS idx_persona_apellido_nombre ON Persona;

-- Ejecutamos la consulta y medimos el tiempo
SELECT * FROM Persona WHERE apellido LIKE 'García%';
```

![Captura](../anexos/capturas/prueba_rendimiento_sin_indice.png)

- Resultados del `EXPLAIN`:

```sql
-- Ejecutamos el EXPLAIN para ver el plan de ejecución
EXPLAIN SELECT * FROM Persona WHERE apellido LIKE 'García%';
```

![Captura](../anexos/capturas/explain_sin_indice.png)

El optimizador de MySQL decide que la forma más eficiente de ejecutar la consulta es leer cada fila de la tabla Persona, lo que se conoce como `Full Table Scan`. Esto se indica en la columna type, que muestra el valor `ALL`.

#### Escenario 2: Con Índice

Luego, creamos el índice compuesto y volvemos a ejecutar la misma consulta.

```sql
-- Creamos el índice en las columnas de búsqueda
CREATE INDEX idx_persona_apellido_nombre ON Persona(apellido, nombre);

-- Volvemos a ejecutar la misma consulta
SELECT * FROM Persona WHERE apellido LIKE 'García%';
```

![Captura](../anexos/capturas/prueba_rendimiento_con_indice.png)

- Resultados del `EXPLAIN`:

```sql
-- Ejecutamos el EXPLAIN para ver el plan de ejecución
EXPLAIN SELECT * FROM Persona WHERE apellido LIKE 'García%';
```

![Captura](../anexos/capturas/explain_con_indice.png)

MySQL ahora utiliza el índice (`key: idx_persona_apellido_nombre`). En lugar de escanear toda la tabla, accede directamente a las filas que cumplen la condición resultando en un tiempo de ejecución mucho más rápido y un menor consumo de recursos..

#### **Tabla Comparativa de Resultados**

| Escenario      | Operación Realizada            | Tiempo de Ejecución | Mejora de Rendimiento |
| :------------- | :----------------------------- | :------------------ | :-------------------- |
| **Sin índice** | Full Table Scan (200000 filas) | ~ 0.031 sec         | -                     |
| **Con índice** | Index Seek                     | ~ 0.001 sec         | **~ 96% más rápida**  |

### 4. Conclusión

La carga masiva de datos y las pruebas de rendimiento demuestran de manera contundente la importancia crítica de los índices en bases de datos con volúmenes de datos realistas. Una consulta simple puede volverse ineficiente si el motor se ve forzado a escanear tablas enteras.

La creación de un índice estratégico en las columnas `(apellido, nombre)` resultó en una mejora del rendimiento superior al 96%, transformando una consulta lenta en una operación casi instantánea. Esto confirma que un diseño de indexación adecuado es tan importante como la normalización para el buen funcionamiento de un sistema de gestión de datos.

---

## Etapa 3 - Consultas Complejas y Útiles

### 1. Descripción

El propósito de esta etapa fue trascender las operaciones básicas de un CRUD (`INSERT`, `SELECT *`, `UPDATE`, `DELETE`) para diseñar y ejecutar consultas SQL complejas y de alto valor. El objetivo era extraer información significativa y generar reportes útiles que no podrían obtenerse con consultas simples. Para ello, se aplicaron cláusulas avanzadas como `JOIN` (para combinar datos de múltiples tablas), `GROUP BY` y `HAVING` (para agregar y filtrar datos), subconsultas (para realizar consultas anidadas) y la creación de `VIEW`s (para simplificar el acceso a los datos). Todas las consultas fueron probadas sobre el conjunto de datos masivos para asegurar su correctitud y rendimiento.

### 2. Metodología

El desarrollo de las consultas siguió un proceso metodológico enfocado en la utilidad práctica:

1. **Identificación de Casos de Uso:** Se analizaron las necesidades de diferentes roles en un entorno clínico (administrativos, médicos) para identificar qué tipo de reportes y búsquedas agregarían más valor al sistema.
2. **Diseño de la Lógica:** Para cada caso de uso, se diseñó una consulta SQL utilizando las cláusulas requeridas por la consigna, asegurando la correcta unión y filtrado de las tablas normalizadas.
3. **Validación y Pruebas:** Cada consulta fue ejecutada y validada contra el set de más de 500,000 registros para verificar que los resultados fueran correctos y que el tiempo de respuesta fuera aceptable, aprovechando los índices creados.
4. **Documentación:** Se documentó la utilidad práctica de cada consulta, explicando qué problema resuelve o qué información clave proporciona.

### 3. Consultas Desarrolladas

A continuación, se presentan las consultas diseñadas, cumpliendo con los requisitos de la consigna.

#### Consulta 1: `JOIN` - Ficha Completa de Pacientes Activos

Esta consulta es el núcleo de la aplicación para visualizar la información de un paciente. Unifica en una sola vista los datos personales, los detalles médicos de su historia clínica y la información del profesional que lo atiende. Es la base para cualquier pantalla de "Detalle de Paciente" o reporte individual.

```sql
SELECT
    p.id AS paciente_id,
    per.dni,
    CONCAT(per.apellido, ', ', per.nombre) AS nombre_completo,
    TIMESTAMPDIFF(YEAR, per.fecha_nacimiento, CURDATE()) AS edad,
    hc.nro_historia,
    gs.simbolo AS grupo_sanguineo,
    CONCAT(per_prof.apellido, ', ', per_prof.nombre) AS profesional_asignado,
    prof.especialidad
FROM
    Paciente p
    INNER JOIN Persona per ON p.persona_id = per.id
    LEFT JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id
    LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
    LEFT JOIN Profesional prof ON hc.profesional_id = prof.id
    LEFT JOIN Persona per_prof ON prof.persona_id = per_prof.id
WHERE
    per.eliminado = FALSE AND p.eliminado = FALSE
ORDER BY
    per.apellido, per.nombre;
```

#### Consulta 2: `JOIN` - Búsqueda de Pacientes por Especialidad Médica

Permite a un administrativo generar un listado de todos los pacientes que son atendidos por una especialidad específica (ej. 'Cardiología'). Es una herramienta fundamental para auditorías, gestión de turnos por área o para contactar a un grupo de pacientes bajo el cuidado de un mismo tipo de especialista.

```sql
SELECT
    prof.especialidad,
    CONCAT(per_pro.apellido, ', ', per_pro.nombre) AS profesional,
    hc.nro_historia,
    per_pac.dni AS dni_paciente,
    CONCAT(per_pac.apellido, ', ', per_pac.nombre) AS nombre_paciente
FROM HistoriaClinica hc
    INNER JOIN Profesional prof ON hc.profesional_id = prof.id
    INNER JOIN Persona per_pro ON prof.persona_id = per_pro.id
    INNER JOIN Paciente pac ON hc.id = pac.historia_clinica_id
    INNER JOIN Persona per_pac ON pac.persona_id = per_pac.id
WHERE
    prof.especialidad = 'Cardiología' AND hc.eliminado = FALSE
ORDER BY
    prof.matricula;
```

#### Consulta 3: `GROUP BY` + `HAVING` - Grupos Sanguíneos Minoritarios

Este reporte estadístico es clave para la gestión de recursos. Identifica los grupos sanguíneos menos comunes entre los pacientes, una información vital para campañas de donación de sangre, gestión de stock en bancos de sangre o para estudios epidemiológicos.

```sql
SELECT
    gs.simbolo AS grupo_sanguineo,
    COUNT(p.id) AS cantidad_pacientes
FROM
    GrupoSanguineo gs
    LEFT JOIN HistoriaClinica hc ON gs.id = hc.grupo_sanguineo_id
    LEFT JOIN Paciente p ON hc.id = p.historia_clinica_id
WHERE
    p.eliminado = FALSE
GROUP BY
    gs.simbolo
HAVING
    COUNT(p.id) <= 19000 -- Umbral para definir un grupo como "minoritario"
ORDER BY
    cantidad_pacientes ASC;
```

#### Consulta 4: `Subconsulta` - Historias Clínicas sin Profesional Asignado

Esta consulta funciona como un reporte de "tareas pendientes" para el personal administrativo. Genera una lista de todas las historias clínicas activas que aún no tienen un médico asignado, ayudando a garantizar que ningún paciente quede sin seguimiento y mejorando la calidad del servicio.

```sql
SELECT
    hc.nro_historia,
    hc.antecedentes,
    (SELECT CONCAT(per.apellido, ', ', per.nombre)
     FROM Persona per
     JOIN Paciente p ON per.id = p.persona_id
     WHERE p.historia_clinica_id = hc.id) AS paciente
FROM
    HistoriaClinica hc
WHERE
    hc.profesional_id IS NULL AND hc.eliminado = FALSE;
```

### 4. Creación de Vista (VIEW)

#### Vista 1: `vw_pacientes_activos`

Una vista es una tabla virtual basada en una consulta. Esta vista, `vw_pacientes_activos`, simplifica radicalmente el acceso a los datos más comunes de los pacientes. El equipo de desarrollo puede consultarla como si fuera una tabla simple (`SELECT * FROM vw_pacientes_activos`) sin tener que reescribir el complejo `JOIN` cada vez. Además, abstrae la lógica de negocio de filtrar siempre por registros no eliminados, reduciendo errores y simplificando el código de la aplicación.

```sql
CREATE OR REPLACE VIEW vw_pacientes_activos AS
SELECT
    p.id AS paciente_id,
    per.dni,
    per.nombre,
    per.apellido,
    hc.nro_historia,
    prof.especialidad AS especialidad_medico
FROM
    Paciente p
    INNER JOIN Persona per ON p.persona_id = per.id
    LEFT JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id
    LEFT JOIN Profesional prof ON hc.profesional_id = prof.id
WHERE
    p.eliminado = FALSE AND per.eliminado = FALSE;

-- Ejemplo de uso de la vista:
SELECT * FROM vw_pacientes_activos WHERE especialidad_medico = 'Pediatría';
```

### 5. Conclusión

En esta etapa se demostró con éxito cómo un esquema de base de datos bien normalizado y poblado con datos masivos permite la creación de consultas analíticas y reportes de gran valor. Se diseñaron y validaron cuatro consultas complejas y una vista que cumplen con los requisitos técnicos y resuelven problemas prácticos de un sistema de gestión de pacientes. El uso de `JOIN`, `GROUP BY`, `HAVING` y subconsultas se ha consolidado como una herramienta esencial para la extracción de inteligencia de negocio a partir de los datos almacenados.

---

## Etapa 4 - Seguridad e Integridad

---

## Anexo

### Uso Pedagógico de la IA en el proceso

- Etapa 1: Modelado y Definición de Constraints

![Captura](../anexos/chats-ia/uso_pedagogico_ia_etapa_1.jpg)

- Etapa 2: Implementación y Carga Masiva de Datos

![Captura](../anexos/chats-ia/uso_pedagogico_ia_etapa_2.jpg)

- Etapa 3: Consultas Complejas y Útiles

![Captura](../anexos/chats-ia/uso_pedagogico_ia_etapa_3.jpg)

- Etapa 4: Seguridad e Integridad

![Captura](../anexos/chats-ia/uso_pedagogico_ia_etapa_4.jpg)

- Etapa 5: Concurrencia y Transacciones

![Captura](../anexos/chats-ia/uso_pedagogico_ia_etapa_5.jpg)
