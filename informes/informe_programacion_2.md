![Header UTN](../anexos/programacion_2/capturas/header_utn.png)

# Trabajo Final Integrador: Sistema de Gestión de Pacientes e Historias Clínicas

![NetBeans](https://img.shields.io/badge/NetBeans-1B6AC6?logo=apache-netbeans-ide&logoColor=white) ![Java](https://img.shields.io/badge/Java-LTS-red.svg) ![JDBC](https://img.shields.io/badge/JDBC-API-orange) ![MySQL](https://img.shields.io/badge/MySQL-LTS-blue?logo=mysql) ![XAMPP](https://img.shields.io/badge/XAMPP-MySQL-green.svg?logo=xampp&logoColor=orange) ![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white) ![Git](https://img.shields.io/badge/Git-F05032?logo=git&logoColor=white) ![License](https://img.shields.io/badge/license-MIT-green.svg) [![Ver en GitHub](https://img.shields.io/badge/Repositorio-GitHub-black?logo=github)](https://github.com/Gerolupo12/paciente-historia-cliente)

## Datos del Proyecto

- **Asignatura**: Programación II
- **Dominio**: Paciente → HistoriaClínica (Relación 1→1 unidireccional)

## Integrantes

Este proyecto fue desarrollado de manera colaborativa por el siguiente equipo:

- **Lahoz, Cristian** - [GitHub](https://github.com/m415x)
- **Maldonado, Ariana** - [GitHub](https://github.com/AriMaldo19)
- **Ramallo, Gerónimo** - [GitHub](https://github.com/Gerolupo12)

---

## 1. Integrantes y Roles

| Integrante            | Capa(s) a Cargo                           | Rol Principal                              | Responsabilidades                                                                                                                                                             |
| --------------------- | ----------------------------------------- | ------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Lahoz, Cristian**   | `views`, `main`                           | Desarrollo de Interfaz (UI / Consola)      | Implementación del menú principal, navegación de opciones y comunicación entre la vista y la capa de servicio. Integración funcional del flujo de usuario.                    |
| **Maldonado, Ariana** | `service` (incluye `exceptions` y `test`) | Validaciones y Reglas de Negocio           | Desarrollo de la lógica de negocio, validaciones de entrada y manejo de errores mediante excepciones personalizadas. Implementación de pruebas funcionales y transaccionales. |
| **Ramallo, Gerónimo** | `config`, `dao`, `models`                 | Arquitectura, Persistencia y Base de Datos | Diseño de la arquitectura multicapa, modelado de entidades y relaciones, desarrollo de DAOs y configuración del acceso a MySQL con manejo transaccional.                      |

## 2. Elección del Dominio y Justificación

El sistema fue desarrollado en el **dominio sanitario**, centrado en la **gestión de pacientes** y sus **historias clínicas** médicas.

La elección de este dominio se justifica porque permite aplicar una relación **1→1** entre entidades dependientes (**Paciente ↔ HistoriaClínica**), representando un caso real y de alta aplicabilidad dentro de entornos médicos y administrativos.

### Objetivos del dominio

- Facilitar el **registro, consulta y actualización** de datos de pacientes y su historia clínica correspondiente.
- Garantizar la **integridad y unicidad** de la información médica (DNI y número de historia clínica).
- Implementar un sistema **transaccional y confiable**, que mantenga la coherencia entre entidades relacionadas.
- Representar un **modelo escalable y extensible**, capaz de integrarse con futuras interfaces gráficas o módulos adicionales.
- Permitir la **trazabilidad** de los cambios mediante validaciones y manejo de excepciones específicas.

## 3. Diseño: Decisiones Clave (1→1, FK única vs PK compartida) + UML

### 3.1 Relación 1→1: Paciente ↔ Historia Clínica

Se implementó una **relación 1→1 unidireccional** entre `Paciente` y `HistoriaClinica`.  
Cada paciente posee una única historia clínica asociada.

#### Decisión técnica

Se utilizó **clave foránea (FK) única** en lugar de **clave primaria (PK) compartida**.

#### Justificación

- La FK única (`historia_clinica_id`) dentro de `Paciente` simplifica la inserción y actualización.
- Mantiene independencia entre tablas y permite rollback transaccional ante errores.
- Evita dependencia circular entre claves primarias.
- Mejora el mantenimiento y la legibilidad del esquema.

#### Esquema lógico simplificado

El modelo físico de datos incluye tres tablas:

- **paciente** → contiene los datos personales y FK `historia_clinica_id`.
- **historia_clinica** → almacena información médica y referencia a `grupo_sanguineo`.
- **grupo_sanguineo** → catálogo de tipos válidos de sangre.

_(El detalle completo del modelo SQL se presenta en el punto 5.1 Persistencia.)_

### 3.2 Diagrama UML

![UML](../anexos/programacion_2/capturas/uml.png)

#### Relaciones

- Paciente → HistoriaClinica (1→1 unidireccional, FK única).
- HistoriaClinica → GrupoSanguineo (muchos→uno).

## 4. Arquitectura por Capas y Responsabilidades de Cada Paquete

### 4.1 Estructura General

```plaintext
src
├── main
│   └── java
│       ├── config
│       │   ├── DatabaseConnection.java
│       │   └── TransactionManager.java
│       ├── dao
│       │   ├── GenericDAO.java
│       │   ├── HistoriaClinicaDAO.java
│       │   └── PacienteDAO.java
│       ├── exceptions
│       │   ├── DuplicateEntityException.java
│       │   ├── ServiceException.java
│       │   └── ValidationException.java
│       ├── main
│       │   ├── Main.java
│       │   └── TestConnection.java
│       ├── models
│       │   ├── Base.java
│       │   ├── GrupoSanguineo.java
│       │   ├── HistoriaClinica.java
│       │   └── Paciente.java
│       ├── service
│       │   ├── GenericService.java
│       │   ├── HistoriaClinicaService.java
│       │   └── PacienteService.java
│       └── views
│           ├── historias
│           │   ├── HistoriaMenu.java
│           │   └── HistoriaView.java
│           ├── pacientes
│           │   ├── PacienteMenu.java
│           │   └── PacienteView.java
│           ├── AppMenu.java
│           ├── DisplayMenu.java
│           └── MenuHandler.java
└── test
    └── ServiceTest.java
```

### 4.2 Capas Principales

**main**

- Contiene el punto de entrada del sistema (`Main.java`).
- Inicializa los servicios, DAO y controladores.
- Incluye `TestConnection.java` para verificar la conexión con la base de datos.
- Representa el **inicio del flujo de ejecución** del programa y el nexo entre la capa de presentación y la lógica de negocio.

**config**

- Gestiona la configuración de base de datos y las transacciones JDBC.
- `DatabaseConnection`: establece la conexión con MySQL, carga el driver y valida credenciales.
- `TransactionManager`: controla commits y rollbacks de manera centralizada, garantizando la atomicidad de las operaciones.

**dao**

- Encapsula el acceso a datos mediante JDBC puro.
- `GenericDAO`: interfaz genérica con operaciones CRUD.
- `PacienteDAO` y `HistoriaClinicaDAO`: implementaciones concretas con consultas SQL parametrizadas y control de FK únicas.

**service**

- Contiene la **lógica de negocio**, las **validaciones de reglas**, y la **coordinación transaccional**.
- `PacienteService`: orquesta la creación, actualización y eliminación en cascada (Paciente + Historia Clínica).
- `HistoriaClinicaService`: valida unicidad del número de historia, formato correcto y manejo de cascadas.
- `GenericService`: interfaz que define las operaciones base para los servicios.

**models**

- Define las entidades del dominio: `Paciente`, `HistoriaClinica`, `GrupoSanguineo` y `Base`.
- Representa la estructura de datos y sus relaciones (1→1 y N→1).
- Es la capa que **mapea la base de datos al modelo lógico del sistema**.

**views**

- Implementa la **interfaz de usuario por consola** (UI).
- Contiene los menús, controladores y flujos de interacción (`AppMenu`, `DisplayMenu`, `MenuHandler`).
- Permite al usuario ejecutar las operaciones de gestión de pacientes e historias clínicas.

### 4.3 Capas Auxiliares

**exceptions**

- Diseñada por _Ariana Maldonado_ para centralizar la gestión de errores y excepciones controladas.
- Incluye:
  - `ValidationException`: errores de datos inválidos (por ejemplo, formato de DNI o nombre).
  - `ServiceException`: errores técnicos o transaccionales.
  - `DuplicateEntityException`: violaciones de unicidad (DNI o número de historia).
- Mejora la trazabilidad y robustez del sistema.

**test**

- Contiene pruebas funcionales y de validación (`ServiceTest.java`).
- Verifica:
  - Inserciones válidas y rollback transaccional.
  - Detección de DNI duplicado.
  - Validaciones de formato, unicidad y rango de fechas.
- Garantiza la estabilidad y coherencia de las reglas de negocio.

## 5. Persistencia: Estructura de la Base, Orden de Operaciones y Transacciones

### 5.1 Estructura de la Base de Datos

El sistema utiliza una base de datos **MySQL 8.0.43** con tres tablas principales:

1. **paciente**

   - Contiene los datos personales (nombre, apellido, DNI, fecha de nacimiento).
   - Posee una clave foránea única `historia_clinica_id`.

2. **historia_clinica**

   - Almacena información médica, grupo sanguíneo y observaciones.
   - Está relacionada de manera 1→1 con `paciente`.

3. **grupo_sanguineo**
   - Tabla de catálogo para los tipos válidos de sangre.
   - Relación _muchos→uno_ con `historia_clinica`.

```sql
CREATE TABLE grupo_sanguineo (
  id INT AUTO_INCREMENT PRIMARY KEY,
  tipo ENUM('O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-') NOT NULL
);

CREATE TABLE historia_clinica (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nro_historia VARCHAR(10) UNIQUE NOT NULL,
  grupo_sanguineo_id INT NOT NULL,
  antecedentes TEXT,
  medicacion_actual TEXT,
  observaciones TEXT,
  eliminado BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (grupo_sanguineo_id) REFERENCES grupo_sanguineo(id)
);

CREATE TABLE paciente (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL,
  apellido VARCHAR(50) NOT NULL,
  dni VARCHAR(15) UNIQUE NOT NULL,
  fecha_nacimiento DATE NOT NULL,
  historia_clinica_id INT UNIQUE,
  eliminado BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (historia_clinica_id) REFERENCES historia_clinica(id)
);
```

### 5.2 Orden de Operaciones

- **INSERT:** primero se inserta la historia clínica, luego el paciente.
- **UPDATE:** ambas entidades se actualizan en una transacción compartida.
- **DELETE:** se aplica baja lógica (campo eliminado en TRUE).
- **RECOVER:** reactiva registros sin crear duplicados.

```java
// Ejemplo simplificado de inserción transaccional
try (Connection conn = DatabaseConnection.getConnection()) {
    conn.setAutoCommit(false);
    historiaClinicaDAO.insert(historia);
    pacienteDAO.insertTx(paciente, conn);
    conn.commit();
} catch (Exception e) {
    conn.rollback();
}
```

### 5.3 Transacciones y Rollback

- El commit se ejecuta en la capa service una vez que ambas inserciones finalizan correctamente.
- Si ocurre una excepción (ValidationException, ServiceException o DuplicateEntityException), se ejecuta rollback para revertir los cambios.
- La atomicidad garantiza que Paciente e HistoriaClínica siempre se mantengan sincronizados.

## 6. Validaciones y Reglas de Negocio

Las reglas se implementan en las clases del paquete service, y las excepciones personalizadas están definidas en exceptions.

### 6.1 Validaciones Principales

RN-001: El nombre y apellido solo pueden contener letras y espacios.
RN-002: El DNI debe ser único y tener entre 7 y 15 dígitos.
RN-003: La fecha de nacimiento debe estar entre 1900 y la fecha actual.
RN-015: El número de historia clínica debe tener formato HC-####.
RN-016: La historia clínica requiere grupo sanguíneo válido.
RN-017: Las inserciones inválidas generan rollback total.

```java
if (!dni.matches("^[0-9]{7,15}$")) {
    throw new ValidationException("El DNI debe tener solo números (7–15 dígitos).");
}
if (!nroHistoria.matches("^HC-[0-9]{4,17}$")) {
    throw new ValidationException("Formato inválido: debe ser 'HC-0001'.");
}
```

### 6.2 Excepciones Definidas

- ValidationException: errores de entrada del usuario.
- ServiceException: errores técnicos o transaccionales.
- DuplicateEntityException: violación de unicidad (DNI o Nro. HC).

## 7. Pruebas Realizadas

### 7.1 Pruebas de Lógica de Negocio

- Ejecutadas desde la clase ServiceTest, validando:
- Inserción válida de Paciente + HistoriaClínica.
- Detección de DNI duplicado.
- Formato incorrecto de DNI o número de historia.
- Fechas inválidas o anteriores a 1900.
- Rollback completo en caso de error en cualquiera de las entidades.

```console
=== TEST 1: Inserción VÁLIDA ===
✅ Paciente insertado correctamente con ID: 5

=== TEST 2: DNI DUPLICADO ===
✅ Detectó correctamente DNI duplicado → Ya existe un paciente registrado con ese DNI.

=== TEST 3: ROLLBACK TRANSACCIONAL ===
✅ Se produjo rollback correctamente → Error al insertar historia clínica.
```

### 7.2 Pruebas de Interfaz

Menú por consola funcional:

```console
========== MENÚ PRINCIPAL ==========
--- Gestión de Pacientes ---
1. Listar pacientes
2. Crear paciente
3. Actualizar paciente
4. Eliminar paciente
--- Gestión de Historias Clínicas ---
5. Listar Historias Clínicas
6. Crear Historia Clínica
7. Actualizar Historia Clínica
8. Eliminar Historia Clínica por ID
9. Eliminar Historia Clínica por ID de paciente (Seguro)
--- Recuperación de datos borrados ---
10. Submenú de recuperación
0. Salir
```

## 8. Conclusiones y Mejoras Futuras

### 8.1 Conclusiones

El proyecto permitió aplicar conceptos avanzados de arquitectura en capas, validaciones, persistencia con JDBC y manejo transaccional completo.
Cada integrante cumplió un rol esencial, logrando un sistema modular, seguro y escalable.

### 8.2 Posibles Mejoras

- Incorporar interfaz gráfica (Swing o JavaFX).
- Añadir capa REST API con Spring Boot.
- Implementar logs persistentes y auditoría.
- Integrar autenticación de usuarios.

## 9. Fuentes y Herramientas Utilizadas

### Herramientas principales

- IDE: Apache NetBeans 21.0.
- Base de Datos: MySQL 8.0.43.
- Lenguaje: Java 21.
- Gradle: 9.2.0
- Control de versiones: Git + GitHub.
- Testing: JUnit y pruebas manuales por consola.
- IA Asistida: ChatGPT (uso en redacción técnica y documentación).

---

## Enlaces

- [Link al Repositorio GitHub](https://github.com/Gerolupo12/paciente-historia-cliente)

- [Link a la Presentación en Video]()
