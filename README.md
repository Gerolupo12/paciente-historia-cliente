# Sistema de Gestión de Pacientes e Historias Clínicas

![NetBeans](https://img.shields.io/badge/NetBeans-1B6AC6?logo=apache-netbeans-ide&logoColor=white) ![Java](https://img.shields.io/badge/Java-LTS-red.svg) ![JDBC](https://img.shields.io/badge/JDBC-API-orange) ![MySQL](https://img.shields.io/badge/MySQL-LTS-blue?logo=mysql) ![MySQL Workbench](https://img.shields.io/badge/MySQL%20Workbench-4479A1.svg?logo=mysql&logoColor=white) ![DBeaver](https://img.shields.io/badge/DBeaver-38698C?logo=dbeaver&logoColor=white) ![XAMPP](https://img.shields.io/badge/XAMPP-MySQL-green.svg?logo=xampp&logoColor=orange) ![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white) ![Git](https://img.shields.io/badge/Git-F05032?logo=git&logoColor=white) ![License](https://img.shields.io/badge/license-MIT-green.svg) [![Ver en GitHub](https://img.shields.io/badge/Repositorio-GitHub-black?logo=github)](https://github.com/Gerolupo12/paciente-historia-cliente)

## Trabajo Práctico Integrador - Programación 2

### Descripción del Proyecto

Sistema desarrollado en Java para el Trabajo Práctico Integrador (TPI) de Programación II, cuyo objetivo es demostrar el dominio de los conceptos de **Programación Orientada a Objetos**, **Persistencia de Datos con JDBC**, y **Arquitectura en Capas**. El sistema gestiona la relación unidireccional 1→1 entre **Pacientes** y sus **Historias Clínicas**, aplicando buenas prácticas de diseño, manejo de excepciones, validaciones y transacciones seguras.

### Objetivos Académicos

- Aplicar **arquitectura en capas** (Presentación / Servicio / DAO / Modelo).
- Implementar **principios SOLID** y separación de responsabilidades.
- Desarrollar persistencia con **JDBC** + **MySQL** usando **DAO Pattern**.
- Incorporar **validaciones en distintos niveles**.
- Implementar baja lógica (**Soft Delete**) y manejo robusto de excepciones.
- Utilizar **interfaces genéricas** (GenericDAO, GenericService).
- Diseñar y documentar un sistema con **estilo profesional** y **diagramas UML/ER**.

### Dominio Elegido: Paciente → HistoriaClínica

- **Paciente**: Representa a una persona bajo atención médica. Contiene información personal y datos de identificación.
- **HistoriaClínica**: Almacena la información médica y antecedentes del paciente.
- **Relación 1→1 unidireccional**: un Paciente tiene exactamente una Historia Clínica (opcional hasta ser creada y asignada).

### Arquitectura del Proyecto

```plaintext
paciente-historia-cliente
├── anexos                                  # archivos adicionales para el proyecto
│   ├── db_1
│   └── programacion_2
├── informes
│   ├── informe_db_1.md                     ← informe detallado del proyecto de DB1
│   └── informe_programacion_2.md           ← informe detallado del proyecto de P2
├── sql                                     # scripts SQL
│   ├── db_1
│   └── programacion_2
│       ├── 01_esquema.sql                  ← script para crear el esquema de la BD
│       ├── 02_catalogos.sql                ← script para poblar tablas de catálogo
│       └── 03_carga_masiva.sql             ← scripts para poblar la BD con datos de prueba
├── src                                     # código fuente del proyecto
│   ├── config                              # configuración de conexión y transacciones
│   │   ├── DatabaseConnection.java         ← utilitario para conexión a BD
│   │   └── TransactionManager.java         ← utilitario para manejo de transacciones
│   ├── dao                                 # acceso a datos (JDBC)
│   │   ├── GenericDAO.java                 ← DAO genérico para entidades
│   │   ├── HistoriaClinicaDAO.java         ← DAO específico para historias clínicas
│   │   └── PacienteDAO.java                ← DAO específico para pacientes
│   ├── exceptions                          # excepciones personalizadas
│   │   ├── DuplicateEntityException.java   ← excepción para entidades duplicadas
│   │   ├── ServiceException.java           ← excepción genérica para servicios
│   │   └── ValidationException.java        ← excepción para validaciones de datos
│   ├── main                                # punto de entrada de la aplicación
│   │   ├── resources
│   │   │   └── db.properties               ← archivo de configuración y propiedades para conexión a BD
│   │   ├── Main.java                       ← clase principal para ejecutar la aplicación
│   │   └── TestConnection.java             ← clase para probar conexión a BD
│   ├── models                              # entidades del dominio
│   │   ├── Base.java                       ← clase base para entidades con id y eliminado
│   │   ├── GrupoSanguineo.java             ← modelo de dominio GrupoSanguineo
│   │   ├── HistoriaClinica.java            ← modelo de dominio HistoriaClínica
│   │   └── Paciente.java                   ← modelo de dominio Paciente
│   ├── service                             # lógica de negocio y validaciones
│   │   ├── GenericService.java             ← servicio genérico para entidades
│   │   ├── HistoriaClinicaService.java     ← servicio específico para historias clínicas
│   │   └── PacienteService.java            ← servicio específico para pacientes
│   ├── test                                # pruebas unitarias para entidades
│   │   └── ServiceTest.java                ← pruebas unitarias para servicios
│   └── views                               # capa de presentación (menús por entidad)
│       ├── historias                       # submenús de Historias Clínicas
│       │   ├── HistoriaMenu.java           ← submenú específico para historias clínicas
│       │   └── HistoriaView.java           ← muestra o captura datos de historia clínica
│       ├── pacientes                       # submenús de Pacientes
│       │   ├── PacienteMenu.java           ← submenú específico para pacientes
│       │   └── PacienteView.java           ← muestra o captura datos de paciente
│       ├── AppMenu.java                    ← menú principal con opciones
│       ├── DisplayMenu.java                ← utilitario para imprimir opciones
│       └── MenuHandler.java                ← controlador de menú
├── HISTORIAS_DE_USUARIO.md                 ← historias de usuario del proyecto
└── README.md                               ← archivo de lectura inicial (este archivo)
```

### Diagrama UML (Modelo de Dominio)

![UML](./anexos/programacion_2/capturas/uml.png)

## Diagrama Entidad Relación (DER)

Este DER representa el esquema implementado en la base de datos MySQL, simplificado para los requisitos de Programación II.

![DER](./anexos/programacion_2/capturas/der.png)

### Tecnologías y Herramientas

| Componente                    | Descripción                  |
| ----------------------------- | ---------------------------- |
| **Java JDK 21+**              | Lenguaje principal           |
| **MySQL 8.4+**                | Base de datos relacional     |
| **JDBC (Driver Connector/J)** | Persistencia y consultas SQL |
| **Gradle 8.12**               | Herramienta de build         |
| **NetBeans / IntelliJ IDEA**  | Entorno de desarrollo        |
| **Git & GitHub**              | Control de versiones         |

### Funcionalidades Principales

- CRUD completo para Pacientes y Historias Clínicas
- Relación 1→1 unidireccional con integridad referencial
- Soft delete en todas las entidades
- Búsquedas flexibles por nombre, apellido o DNI
- Validaciones multi-capa
- Transacciones con commit/rollback
- Manejo centralizado de excepciones
- Enum GrupoSanguineo con validación lógica
- Submenús por entidad y menú principal desacoplado

### Requisitos e Instalación

> **Nota:**
> Este proyecto cuenta con dos versiones:
>
> - **Rama `main`:** versión original (compilación con **Ant**, usada por defecto).
> - **Rama `gradle`:** versión alternativa, completamente migrada a **Gradle**.
>
> Si deseas probar la versión con Gradle, cambia a esa rama después de clonar el repositorio.

#### Requisitos Previos

Antes de comenzar, asegúrate de tener instalado lo siguiente:

| Componente                    | Versión Requerida                  | Descripción                                           |
| ----------------------------- | ---------------------------------- | ----------------------------------------------------- |
| **Java JDK**                  | 21 o superior                      | Requerido para compilar y ejecutar la aplicación      |
| **MySQL Server**              | 8.0 o superior                     | Base de datos relacional utilizada por el sistema     |
| **Gradle Wrapper (Opcional)** | Incluido en la rama `gradle`       | No es necesario instalar Gradle manualmente           |
| **IDE**                       | NetBeans / IntelliJ IDEA / VS Code | Opcional, para ejecución en entorno gráfico           |
| **Driver JDBC**               | `mysql-connector-j-8.4.0.jar`      | Descargado automáticamente por Gradle (rama `gradle`) |

#### 1. Clonar el Repositorio

Por defecto, esto descarga la versión **Ant** (rama principal) con todo el código fuente, scripts SQL y configuración del proyecto:

```bash
git clone https://github.com/Gerolupo12/paciente-historia-cliente.git
cd paciente-historia-cliente
```

Si deseas trabajar con la versión **Gradle**, cambia a la rama correspondiente:

```bash
git checkout gradle
```

Esto cargará toda la estructura adaptada a Gradle (`build.gradle`, `gradlew`, `gradlew.bat`, etc.).

#### 2. Configurar la Base de Datos

Ejecuta los siguientes scripts SQL en tu servidor MySQL.
Los archivos están ubicados en [`/sql/programacion_2`](./sql/programacion_2).

Ejecuta en este orden:

1. `01_esquema.sql` → crea la base de datos y las tablas (`Paciente`, `HistoriaClinica`, `GrupoSanguineo`).
2. `02_catalogos.sql` → inserta los datos estáticos (8 grupos sanguíneos).
3. `03_carga_masiva.sql` → (opcional) agrega registros de ejemplo.

```bash
mysql -u root -p < sql/programacion_2/01_esquema.sql
mysql -u root -p < sql/programacion_2/02_catalogos.sql
mysql -u root -p < sql/programacion_2/03_carga_masiva.sql
```

#### 3. Configurar la Conexión (`db.properties`)

a configuración de conexión a la base de datos se gestiona mediante un archivo de propiedades ubicado en `src/main/resources/db.properties`. Dentro de esa carpeta encontrarás el archivo `db.properties.example`.

Este archivo actúa como **plantilla de referencia**.
Antes de ejecutar la aplicación, debes **crear tu propio archivo `db.properties`** en el mismo directorio, copiando el contenido del ejemplo y adaptándolo a tu entorno local:

```bash
# Desde la raíz del proyecto
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

Luego, edita `db.properties` con tus credenciales reales de MySQL:

```properties
# Configuración de la Base de Datos
db.driverClass=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/GestionPacientes
db.user=tu_usuario_de_mysql (por ejemplo, root)
db.password=tu_contraseña_de_mysql
```

**Importante:**

- El archivo `db.properties` **no se sube al repositorio**, ya que está incluido en `.gitignore` para proteger datos sensibles.
- Si trabajas en equipo, cada desarrollador debe mantener su propio `db.properties` local.
- El archivo `db.properties.example` se mantiene versionado como plantilla estándar del proyecto.

> **Nota técnica:**
> Si el archivo `db.properties` no existe o no puede ser leído, la clase
> [`DatabaseConnection`](./src/config/DatabaseConnection.java) lanzará un error controlado indicando:
>
> ```console
> ❌ ERROR: archivo db.properties no encontrado en la ruta de clase.
> ```
>
> Esto evita que la aplicación intente conectarse con credenciales vacías o incorrectas.

#### 4. Instalar y Configurar el Driver JDBC (para proyectos con Ant / NetBeans)

Si estás trabajando en la **rama principal (`main`)** del proyecto —que utiliza **Java con Ant**— deberás **añadir manualmente** el conector JDBC de MySQL.

##### Paso 1. Descargar el Driver JDBC

1. Accede al sitio oficial de MySQL Connector/J:
   [https://dev.mysql.com/downloads/connector/j/](https://dev.mysql.com/downloads/connector/j/)
2. Descarga la versión **"Platform Independent"** (archivo `.zip` o `.tar.gz`).
3. Extrae el archivo descargado y localiza el **JAR** principal, por ejemplo:

   ```plaintext
   mysql-connector-j-8.4.0.jar
   ```

##### Paso 2. Agregar el JAR al Proyecto en NetBeans

1. Abre NetBeans y localiza tu proyecto (`paciente-historia-cliente`).
2. En el panel **Projects**, haz clic derecho sobre el nombre del proyecto →
   **Properties** → **Libraries** → **Compile** → **Add JAR/Folder...**
3. Busca el archivo `mysql-connector-j-8.4.0.jar` que descargaste y selecciónalo.
4. Presiona **OK** para guardar los cambios.

> Consejo:
> Puedes crear una carpeta `/Libraries` dentro del proyecto y colocar allí el `.jar`.
> Luego, agrégalo desde esa ubicación para mantener el entorno más ordenado.

**Nota:**

> En la rama `gradle`, **no necesitas realizar esta configuración manual**.
> Gradle descarga automáticamente el conector al compilar la aplicación.

#### 5. Compilar el Proyecto

##### Si usas la versión **Ant** (rama `main`)

Compila y ejecuta desde tu IDE (NetBeans) como un proyecto Java estándar.
El archivo `build.xml` gestiona las tareas de compilación.

##### Si usas la versión **Gradle** (rama `gradle`)

```bash
# Windows
gradlew.bat build

# Linux / macOS
./gradlew build
```

Gradle descargará el conector MySQL y otras dependencias automáticamente.

#### 6. Ejecutar la Aplicación

##### Opción 1: Ejecutar desde un IDE (Recomendado)

1. Abrer el proyecto en un IDE (NetBeans, IntelliJ, etc.).
2. Verifica que `db.properties` esté configurado correctamente.
3. Localiza y ejecuta el método `main` en la clase: `main/Main.java`

##### Opción 2: Desde la Terminal (solo rama `gradle`)

```bash
# Ejecutar el proyecto en Windows
gradlew.bat run

# Ejecutar el proyecto en Linux / macOS
./gradlew run
```

Gradle descargará automáticamente el conector de MySQL (`mysql-connector-j`) y ejecutará la aplicación desde la clase principal definida en `build.gradle`.

#### 7. Verificar la Conexión (Opcional)

Ejecuta la clase `main/TestConnection.java` para probar la conexión a la base de datos:

```bash
java -cp "build/classes/java/main:<ruta-mysql-jar>" main.TestConnection
```

Salida esperada:

```console
✅ Conexión establecida con éxito.

Usuario conectado: root@localhost
Base de datos: GestionPacientes
URL: jdbc:mysql://localhost:3306/GestionPacientes
Driver: MySQL Connector/J vmysql-connector-j-8.4.0
```

#### 8. Limpieza y Reconstrucción (solo Gradle)

Si deseas limpiar y recompilar completamente el proyecto:

```bash
# Windows
gradlew.bat clean build

# Linux/macOS
./gradlew clean build
```

#### Compatibilidad entre versiones

Ambas ramas —`main` (Ant) y `gradle` (Gradle)—
comparten el **mismo código fuente**, las **mismas clases**, y la **misma lógica de negocio**.
La única diferencia está en el sistema de compilación y ejecución:

| Rama     | Sistema de Build                   | Ideal para                                                |
| -------- | ---------------------------------- | --------------------------------------------------------- |
| `main`   | **Apache Ant (NetBeans)**          | Entornos educativos o integrados en NetBeans              |
| `gradle` | **Gradle Wrapper multiplataforma** | Entornos modernos o automatizados (CI/CD, terminal, etc.) |

### Uso del Sistema

#### Menú Principal

La aplicación se controla mediante un menú en la consola.

```console
========= MENU PRINCIPAL =========
--- Gestión de Pacientes ---
1. Listar pacientes
2. Crear paciente
3. Actualizar paciente
4. Eliminar paciente
--- Gestión de Historias Clínicas ---
5. Listar Historia Clínicas
6. Crear Historia Clínica
7. Actualizar Historia Clínica por ID
8. Eliminar Historia Clínica por ID (Peligroso)
9. Gestionar Historia Clínica por ID de paciente
10. Eliminar Historia Clínica por ID de paciente (Seguro)
--- Recuperación de datos borrados ---
11. Submenú de recuperación

0. Salir
```

#### Gestión de Pacientes

##### 1. Listar Pacientes

Permite listar todos, buscar por DNI, o buscar por filtro de nombre/apellido (HU-002).

**Ejemplo:**

```console
ID: 466 | DNI: 10003261
Nombre: Pérez F., Carmen Y.
Fecha Nac: 1952-08-31 (73 años)
Eliminado: No
  HC Nro: HC-000466 (ID: 466)
  Grupo Sang.: A-
  Antecedentes: Alergia a penicilina
```

##### 2. Crear Paciente

Registra un nuevo paciente. Pregunta opcionalmente si se desea crear y asociar una Historia Clínica en el mismo paso (HU-001).

**Resultado:**

```console
--- Ingrese los datos del nuevo Paciente ---
Nombre -> Pedro
Apellido -> Fulano
DNI (solo números) -> 12345679
Fecha de Nacimiento (AAAA-MM-DD) -> 2000-01-01
¿Desea agregar una Historia Clínica ahora? (s/n) -> S

--- Ingrese los datos de la Historia Clínica ---
Número de Historia (ej: HC-123456) -> HC-123457
Grupo Sanguíneo (A, B, AB, O) -> a
Factor RH (+ o -) -> +
Grupo seleccionado: A+
Antecedentes (opcional) -> Diabetes II
Medicación Actual (opcional) ->
Observaciones (opcional) ->

✅ Nueva Historia Clínica (ID: 1026) creada.

✅ Paciente creado exitosamente con ID: 2050

ID: 2050 | DNI: 12345679
Nombre: Fulano, Pedro
Fecha Nac: 2000-01-01 (25 años)
Eliminado: No
  HC Nro: HC-123457 (ID: 1026)
  Grupo Sang.: A+
  Antecedentes: Diabetes II
```

##### 3. Actualizar Paciente

Modifica los datos de un paciente. Si el paciente no tiene HC, ofrece la posibilidad de crear una (HU-003).

**Ejemplo:**

```console
Ingrese el ID del Paciente que desea actualizar -> 2050

Obteniendo paciente con ID: 2050
ID: 2050 | DNI: 12345679
Nombre: Fulano, Pedro
Fecha Nac: 2000-01-01 (25 años)
Eliminado: No
  HC Nro: HC-123457 (ID: 1026)
  Grupo Sang.: A+
  Antecedentes: Diabetes II

--- Actualizar Paciente (Presione Enter para mantener el valor actual) ---
Nombre [Pedro] -> Pedro Gabriel
Apellido [Fulano] ->
DNI [12345679] ->
Fecha Nacimiento [2000-01-01] ->

✅ Paciente actualizado exitosamente.
ID: 2050 | DNI: 12345679
Nombre: Fulano, Pedro Gabriel
Fecha Nac: 2000-01-01 (25 años)
Eliminado: No
  HC Nro: HC-123457 (ID: 1026)
  Grupo Sang.: A+
  Antecedentes: Diabetes II
```

##### 4. Eliminar Paciente

Realiza una baja lógica (Soft Delete) del paciente y de su Historia Clínica asociada (Cascada Lógica, RN-013).

**Ejemplo:**

```console
Ingrese el ID del Paciente que desea eliminar (baja lógica) -> 2050
¿Está seguro que desea eliminar al paciente ID 2050? (Esto también eliminará su HC) (s/n) -> s
Eliminando historia clínica asociada con ID: 1026
Eliminando paciente con ID: 2050
✅ Paciente ID: 2050 y su HC asociada han sido eliminados (baja lógica).
```

#### Gestión de Historias Clínicas

##### 5. Listar Historias Clínicas

Permite listar todas, buscar por ID, o buscar por filtro de texto (nro, antecedentes, etc.) (HU-006).

**Ejemplo:**

```console
ID: 884 | Nro. Historia: HC-000884
Grupo Sanguíneo: B+
Antecedentes: Antecedentes familiares de diabetes
Medicación: Atorvastatina 200mg nocturnos
Observaciones: Paciente estable, control en 6 meses
Eliminado: No
```

##### 6. Crear HC independiente

Crea una Historia Clínica sin asociarla a ningún paciente (HU-005).

**Ejemplo:**

```console
--- Ingrese los datos de la Historia Clínica ---
Número de Historia (ej: HC-123456) -> HC-012345
Grupo Sanguíneo (A, B, AB, O) -> B
Factor RH (+ o -) -> -
Grupo seleccionado: B-
Antecedentes (opcional) ->
Medicación Actual (opcional) ->
Observaciones (opcional) ->

✅ Historia Clínica independiente creada con ID: 1027
```

##### 7. Actualizar HC por ID

Actualiza los datos de una HC buscándola por su ID.

**Ejemplo:**

```console
Ingrese el ID de la Historia Clínica que desea actualizar -> 1027

Obteniendo historia clinica con ID: 1027
ID: 1027 | Nro. Historia: HC-012345
Grupo Sanguíneo: B-
Antecedentes:
Medicación:
Observaciones:
Eliminado: No

--- Actualizar Historia Clínica (Presione Enter para mantener) ---
Número de Historia [HC-012345] ->
Grupo Sanguíneo actual: [B-]. ¿Desea cambiarlo? (s/n) -> n
Antecedentes [] -> Hipertensión
Medicación [] ->
Observaciones [] ->

✅ Historia Clínica actualizada exitosamente.
```

##### 8. Eliminar HC por ID (Peligroso)

Elimina lógicamente una HC por su ID.

**ADVERTENCIA**: Esto puede dejar una "referencia huérfana" si un paciente la está usando (HU-007).

**Ejemplo:**

```console
Ingrese el ID de la Historia Clínica que desea eliminar (baja lógica) -> 1027
⚠️ ADVERTENCIA: Esta opción es peligrosa.
Si esta HC está asignada a un Paciente, se creará una referencia huérfana.
Use la 'Opción 10: Eliminar HC por Paciente' para una eliminación segura.

Desea eliminar esta HC de todas formas? (s/n) -> s

✅ Historia Clínica ID: 1027 ha sido eliminada (baja lógica).
```

##### 9. Gestionar HC de un Paciente

La operación de gestión 1-a-1 más completa (HU-009).

- Si el paciente tiene HC, permite actualizarla.
- Si el paciente no tiene HC, permite crear una nueva o asignar una existente.

**Ejemplo:**

```console
Ingrese el ID del Paciente cuya HC desea gestionar -> 1027

El paciente no tiene una Historia Clínica asociada.

1. Crear y Asignar una Nueva Historia Clínica
2. Asignar una Historia Clínica Existente
0. Volver
```

##### 10. Eliminar HC de un Paciente (Seguro)

Realiza la "eliminación segura". Primero desasocia la HC del paciente (historia_clinica_id = NULL) y luego la elimina lógicamente. Esto previene referencias huérfanas (HU-008).

**Ejemplo:**

```console
Ingrese el ID del Paciente cuya HC desea eliminar (de forma segura) -> 256

Desea eliminar la HC (ID: 256) de forma segura? (s/n) -> s

✅ Historia Clínica (ID: 256) desasociada y eliminada exitosamente.
```

##### 11. Submenú de recuperación

Permite listar y recuperar (eliminado = FALSE) pacientes e historias clínicas que hayan sido eliminados lógicamente (HU-010).

**Ejemplo:**

```console
========= SUBMENÚ DE RECUPERACIÓN =========
1. Listar Pacientes Eliminados
2. Recuperar Paciente por ID
3. Listar Historias Clínicas Eliminadas
4. Recuperar Historia Clínica por ID
0. Volver al menú principal
```

### Arquitectura en Capas

El proyecto está organizado en una arquitectura de 4 capas, siguiendo las mejores prácticas y los requisitos del TPI. La refactorización ha movido toda la lógica de UI al paquete `views/`.

![Capas](./anexos/programacion_2/capturas/arquitectura-capas.png)

### Patrones y Buenas Prácticas

- **Seguridad (PreparedStatements):** Todas las consultas SQL utilizan `PreparedStatement` para parametrizar las entradas, previniendo vulnerabilidades de Inyección SQL.
- **Baja Lógica (Soft Delete):** Ningún registro se borra físicamente (`DELETE`). Todas las eliminaciones son actualizaciones (`UPDATE ... SET eliminado = TRUE`), preservando el historial y la integridad referencial.
- **Gestión de Recursos (Try-with-Resources):** Todas las conexiones JDBC (`Connection`, `PreparedStatement`, `ResultSet`) se gestionan automáticamente con bloques `try-with-resources` para prevenir la fuga de recursos.
- **Validación Multi-Capa:**
  - **Vista (`views/`):** Valida el formato de entrada (ej: que un ID sea numérico).
  - **Servicio (`service/`):** Valida las reglas de negocio (ej: que un DNI sea único (RN-002) o que una fecha esté en un rango válido).
  - **Base de Datos (`sql/`):** Garantiza la integridad final con `UNIQUE`, `NOT NULL` y `CHECK constraints`.

### Decisiones de Diseño Clave

El diseño de la base de datos y de la arquitectura de la aplicación se guió por principios de normalización, integridad, escalabilidad y consistencia entre el modelo relacional y el modelo de objetos en Java. A continuación, se detallan las decisiones más importantes tomadas durante el proyecto.

#### 1. Implementación de la Relación 1→1 Unidireccional (Requisito TPI)

Para cumplir con el requisito central de P2 de una relación 1→1 unidireccional `Paciente → HistoriaClinica`, se tomó una decisión específica a nivel de base de datos.

- **Implementación:** La relación se implementó en la tabla `Paciente` mediante una clave foránea (`historia_clinica_id`) que apunta a `HistoriaClinica` y, crucialmente, tiene una restricción `UNIQUE`.
- **Justificación:**
  - El `FOREIGN KEY` establece el enlace entre las dos entidades.
  - El `UNIQUE` garantiza que un registro de `HistoriaClinica` solo pueda ser asociado a un único `Paciente`, cumpliendo así la cardinalidad 1 a 1.
  - Se permitió que esta clave foránea sea `NULL`, aportando flexibilidad para poder registrar un paciente antes de que su historia clínica sea creada (HU-001).

#### 2. `GrupoSanguineo`: Tabla en la Base de Datos vs. `Enum` en Java

Se tomó una decisión consciente de modelar el grupo sanguíneo de dos maneras distintas en cada capa, optimizando para el contexto de cada una.

- **En la Base de Datos (Tabla):** Se creó una tabla maestra `GrupoSanguineo`. Esto responde a los principios de normalización (BD1), creando una única fuente de verdad (`A_PLUS`, `A_MINUS`, etc.) y usando un `INT` como FK en `HistoriaClinica`.
- **En Java (`Enum`):** En el código Java, se optó por un `Enum` (`GrupoSanguineo.java`). Esta decisión prioriza la **seguridad de tipos** y la legibilidad del código (P2). El `Enum` asegura que solo se puedan usar valores válidos en tiempo de compilación. El DAO (`HistoriaClinicaDAO`) es responsable de traducir entre el ID de la tabla y el `Enum`.

### 3. Simplificación del Dominio (P2 vs. BD1)

Aunque el proyecto de la materia Bases de Datos I incluía entidades adicionales como `Persona` y `Profesional`, para este TPI de Programación II se decidió simplificar el modelo a las dos entidades centrales: `Paciente` y `HistoriaClinica`.

- **Justificación:** Esta simplificación fue intencional para centrar el esfuerzo en los objetivos de P2: la **arquitectura de software**. El foco del proyecto no es la complejidad del DER, sino la correcta implementación de las capas `views`, `service` y `dao`, la gestión de la persistencia con JDBC, y el manejo de transacciones y reglas de negocio.

### 4. Arquitectura de Vistas (Refactorización)

El proyecto fue refactorizado de un `MenuHandler` monolítico a un paquete `views/` desacoplado, siguiendo el patrón del proyecto modelo `Persona-Domicilio`.

- **`AppMenu`:** Orquestador que inyecta las dependencias.
- **`MenuHandler`:** Controlador "Router" que delega al sub-menú correspondiente.
- **`PacienteMenu` / `HistoriaMenu`:** Sub-Controladores que orquestan la lógica de la UI (llaman al Servicio y a la Vista).
- **`PacienteView` / `HistoriaView`:** Vistas "Tontas" que solo imprimen en consola y leen del `Scanner`.

#### Informes

- [x] [Base de Datos I](./informes/informe_db_1.md)
- [x] [Programación II](./informes/informe_programacion_2.md)

## Video de Demostración

<!-- **[Enlace al video]** -->
