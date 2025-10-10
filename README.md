# Sistema de Gestión de Pacientes e Historias Clínicas

![NetBeans](https://img.shields.io/badge/NetBeans-1B6AC6?logo=apache-netbeans-ide&logoColor=white) ![Java](https://img.shields.io/badge/Java-21.0.8.LTS-red.svg) ![MySQL](https://img.shields.io/badge/MySQL-8.0.43-blue?logo=mysql) ![JDBC](https://img.shields.io/badge/JDBC-API-orange) ![Git](https://img.shields.io/badge/Git-F05032?logo=git&logoColor=white) ![License](https://img.shields.io/badge/license-MIT-green.svg) [![Ver en GitHub](https://img.shields.io/badge/Repositorio-GitHub-black?logo=github)](https://github.com/Gerolupo12/paciente-historia-cliente)

## Integrantes del Grupo


- **Lagos, Alejandro** - [GitHub](https://github.com/Alejandrovans)
- **Lahoz, Cristian** - [GitHub](https://github.com/m415x)
- **Maldonado, Ariana** - [GitHub](https://github.com/AriMaldo19)
- **Ramallo, Gerónimo** - [GitHub](https://github.com/Gerolupo12)

## Descripción del Proyecto

Sistema desarrollado en Java que gestiona la relación unidireccional 1-->1 entre **Pacientes** y sus **Historias Clínicas**. Implementa el patrón DAO, transacciones con commit/rollback, y un menú de consola para operaciones CRUD completas.

### Dominio Elegido: Paciente --> HistoriaClínica

- **Paciente**: Información personal y datos de identificación
- **HistoriaClínica**: Datos médicos y antecedentes del paciente

## Estructura del Proyecto

```plaintext
    paciente-historia-cliente/
    ├── informes
    │   ├── Base_de_Datos_I.md
    │   └── Programacion_II.md
    ├── sql
    │   ├── tablas_3FN
    │   │   ├── init_db.sql
    │   │   ├── init_schema.sql
    │   │   ├── pruebas_constraints.sql
    │   │   └── pruebas_consultas.sql
    │   └── tablas_sin_normalizar
    │       ├── init_db.sql
    │       ├── init_schema.sql
    │       └── pruebas_constraints.sql
    ├── src
    │   ├── config
    │   │   ├── DatabaseConnection.java
    │   │   └── TransactionManager.java
    │   ├── dao
    │   │   ├── GenericDAO.java
    │   │   ├── HistoriaClinicaDAO.java
    │   │   └── PacienteDAO.java
    │   ├── main
    │   │   ├── AppMenu.java
    │   │   ├── Main.java
    │   │   └── TestConnection.java
    │   ├── models
    │   │   ├── Base.java
    │   │   ├── FactorRh.java
    │   │   ├── GrupoSanguineo.java
    │   │   ├── HistoriaClinica.java
    │   │   ├── MainDePrueba.java
    │   │   └── Paciente.java
    │   └── service
    │       ├── GenericService.java
    │       ├── HistoriaClinicaService.java
    │       └── PacienteService.java
    └── test
```

## Diagrama UML

```mermaid
classDiagram
    direction TB
    class Persona {
        - id: int
        - eliminado: boolean
        - nombre: String
        - apellido: String
        - dni: String
        - fechaNacimiento: LocalDate
        + Persona()
        + getters/setters()
        + toString() String
    }

    class Paciente {
        - id: int
        - eliminado: boolean
        - persona: Persona
        - historiaClinica: HistoriaClinica
        + Paciente()
        + getters/setters()
        + toString() String
    }

    class Profesional {
        - id: int
        - eliminado: boolean
        - persona: Persona
        - matricula: String
        - especialidad: String
        + Profesional()
        + getters/setters()
        + toString() String
    }

    class HistoriaClinica {
        - id: int
        - numeroHistoria: String
        - antecedentes: String
        - medicacionActual: String
        - observaciones: String
        - grupoSanguineo: GrupoSanguineo
        - profesional: Profesional
        + HistoriaClinica()
        + getters/setters()
        + toString() String
    }

    class GrupoSanguineo {
        - id: int
        - tipoGrupo: String
        - factorRh: String
        - simbolo: String
        + GrupoSanguineo()
        + getters/setters()
        + toString() String
    }

    Persona "1" -- "1" Paciente : es
    Persona "1" -- "1" Profesional : es
    Paciente "1" -- "1" HistoriaClinica : tiene
    HistoriaClinica "1" -- "1" GrupoSanguineo : tiene
    HistoriaClinica "1" -- "1" Profesional : médico tratante
```

<!-- ## Instalación y Configuración -->

<!-- ## Uso de la Aplicación -->

## Funcionalidades Implementadas

- Relación 1-->1 unidireccional (Paciente --> HistoriaClinica)
- CRUD completo con baja lógica
- Transacciones con commit/rollback
- Validaciones de entrada robustas
- Manejo de excepciones en todas las capas
- Búsquedas por campos clave (DNI, número de historia)
- Arquitectura en capas (DAO/Service/Menu)

## Estructura de la Base de Datos

### Tabla A: `Paciente`

| Campo               | Tipo MySQL  | Restricciones                                                    |
| ------------------- | ----------- | ---------------------------------------------------------------- |
| id                  | BIGINT      | PRIMARY KEY, AUTO_INCREMENT                                      |
| eliminado           | BOOLEAN     | DEFAULT FALSE                                                    |
| nombre              | VARCHAR(80) | NOT NULL                                                         |
| apellido            | VARCHAR(80) | NOT NULL                                                         |
| dni                 | VARCHAR(15) | NOT NULL, UNIQUE, CHECK (LENGTH (dni) BETWEEN 7 AND 15)          |
| fecha_nacimiento    | DATE        | NULL, CHECK (YEAR (fecha_nacimiento) > 1900)                     |
| historia_clinica_id | BIGINT      | FOREIGN KEY, UNIQUE, NULL, ON DELETE SET NULL, ON UPDATE CASCADE |

### Tabla B: `HistoriaClinica`

| Campo             | Tipo MySQL                                                                                | Restricciones                                                                         |
| ----------------- | ----------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------- |
| id                | BIGINT                                                                                    | PRIMARY KEY, AUTO_INCREMENT                                                           |
| eliminado         | BOOLEAN                                                                                   | DEFAULT FALSE                                                                         |
| nro_historia      | VARCHAR(20)                                                                               | UNIQUE, NOT NULL, CHECK (LENGTH(nro_historia) >= 4), CHECK (nro_historia LIKE 'HC-%') |
| grupo_sanguineo   | ENUM('A_PLUS','A_MINUS', 'B_PLUS', 'B_MINUS', 'AB_PLUS', 'AB_MINUS', 'O_PLUS', 'O_MINUS') | NULL                                                                                  |
| antecedentes      | TEXT                                                                                      | NULL                                                                                  |
| medicacion_actual | TEXT                                                                                      | NULL                                                                                  |
| observaciones     | TEXT                                                                                      | NULL                                                                                  |

## Diagrama ER

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
        }

        Paciente }o--|| HistoriaClinica : tiene
```

## Informes

- [x] [Base de Datos I](./informes/Base_de_Datos_I.md)
- [ ] [Programación II](./informes/Programacion_II.md)

## Video Demostración

<!-- [Ver video de demostración](#) (10-15 minutos) -->

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo [`LICENSE`](LICENSE) para más detalles.
