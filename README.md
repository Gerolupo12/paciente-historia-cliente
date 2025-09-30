# Sistema de Gestión de Pacientes e Historias Clínicas

![NetBeans](https://img.shields.io/badge/NetBeans-1B6AC6?logo=apache-netbeans-ide&logoColor=white) ![Java](https://img.shields.io/badge/Java-21.0.8.LTS-red.svg) ![MySQL](https://img.shields.io/badge/MySQL-8.0.43-blue?logo=mysql) ![JDBC](https://img.shields.io/badge/JDBC-API-orange) ![Git](https://img.shields.io/badge/Git-F05032?logo=git&logoColor=white) ![License](https://img.shields.io/badge/license-MIT-green.svg) [![Ver en GitHub](https://img.shields.io/badge/Repositorio-GitHub-black?logo=github)](https://github.com/Gerolupo12/paciente-historia-cliente)

## Integrantes del Grupo

- **Ariana Maldonado** - [GitHub](https://github.com/AriMaldo19)
- **Gerónimo Ramallo** - [GitHub](https://github.com/Gerolupo12)
- **Alejandro Lagos** - [GitHub](https://github.com/Alejandrovans)
- **Cristian Lahoz** - [GitHub](https://github.com/m415x)

## Descripción del Proyecto

Sistema desarrollado en Java que gestiona la relación unidireccional 1-->1 entre **Pacientes** y sus **Historias Clínicas**. Implementa el patrón DAO, transacciones con commit/rollback, y un menú de consola para operaciones CRUD completas.

### Dominio Elegido: Paciente --> HistoriaClínica

- **Paciente**: Información personal y datos de identificación
- **HistoriaClínica**: Datos médicos y antecedentes del paciente

## Estructura del Proyecto

```plaintext
    paciente-historia-cliente/
    ├── sql
    │   └── schema.sql
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
    │   │   ├── NewMain.java
    │   │   └── TestConnection.java
    │   ├── models
    │   │   ├── Base.java
    │   │   ├── GrupoSanguineo.java
    │   │   ├── HistoriaClinica.java
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
        direction LR
            class Base {
                - id: int
                - eliminado: boolean
                + Base(int)
                + Base()
                + getId() int
                + setId(int) void
                + getEliminado() boolean
                + setEliminado(boolean) void
            }

            class Paciente {
                - nombre: String
                - apellido: String
                - dni: String
                - fechaNacimiento: LocalDate
                - historiaClinica: HistoriaClinica
                + Paciente(int, String, String, String, LocalDate)
                + Paciente()
                + getNombre() String
                + setNombre(String) void
                + getApellido() String
                + setApellido(String) void
                + getDni() String
                + setDni(String) void
                + getFechaNacimiento() LocalDate
                + setFechaNacimiento(LocalDate) void
                + getHistoriaClinica() HistoriaClinica
                + setHistoriaClinica(HistoriaClinica) void
                + toString() String
            }

            class HistoriaClinica {
                - numeroHistoria: String
                - antecedentes: String
                - medicacionActual: String
                - observaciones: String
                - grupoSanguineo: GrupoSanguineo
                + HistoriaClinica(id: int, String, String, String, String, GrupoSanguineo)
                + HistoriaClinica(id: int, numeroHistoria: String)
                + HistoriaClinica()
                + getNumeroHistoria() String
                + setNumeroHistoria(String) void
                + getGrupoSanguineo() GrupoSanguineo
                + setGrupoSanguineo(GrupoSanguineo) void
                + getAntecedentes() String
                + setAntecedentes(String) void
                + getMedicacionActual() String
                + setMedicacionActual(String) void
                + getObservaciones() String
                + setObservaciones(String) void
                + toString() String
            }

            class GrupoSanguineo {
                A_PLUS
                A_MINUS
                B_PLUS
                B_MINUS
                AB_PLUS
                AB_MINUS
                O_PLUS
                O_MINUS
                + puedeDonarA(receptor: GrupoSanguineo) boolean
            }

            <<abstract>> Base
            <<enum>> GrupoSanguineo

            Base <|-- Paciente : implementa
            Base <|-- HistoriaClinica : implementa
            Paciente --> "1" HistoriaClinica : -historiaClinica
            HistoriaClinica --> "1" GrupoSanguineo : -grupoSanguineo
```

<!-- ## Requisitos del Sistema -->

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

### Tabla: `Paciente`

| Campo            | Tipo MySQL  | Restricciones               |
| ---------------- | ----------- | --------------------------- |
| id               | INT         | PRIMARY KEY, AUTO_INCREMENT |
| eliminado        | BOOLEAN     | DEFAULT FALSE               |
| nombre           | VARCHAR(80) | NOT NULL                    |
| apellido         | VARCHAR(80) | NOT NULL                    |
| dni              | VARCHAR(8)  | NOT NULL, UNIQUE            |
| fecha_nacimiento | DATE        | NULLABLE                    |
| historia_clinica | INT         | FOREIGN KEY, UNIQUE         |

### Tabla: `HistoriaClinica`

| Campo             | Tipo MySQL                                                                                | Restricciones               |
| ----------------- | ----------------------------------------------------------------------------------------- | --------------------------- |
| id                | INT                                                                                       | PRIMARY KEY, AUTO_INCREMENT |
| eliminado         | BOOLEAN                                                                                   | DEFAULT FALSE               |
| nro_historia      | VARCHAR(20)                                                                               | UNIQUE                      |
| grupo_sanguineo   | ENUM('A_PLUS','A_MINUS', 'B_PLUS', 'B_MINUS', 'AB_PLUS', 'AB_MINUS', 'O_PLUS', 'O_MINUS') | NULLABLE                    |
| antecedentes      | TEXT                                                                                      | NULLABLE                    |
| medicacion_actual | TEXT                                                                                      | NULLABLE                    |
| observaciones     | TEXT                                                                                      | NULLABLE                    |

## Diagrama ER

```mermaid
    erDiagram
    direction LR
        Paciente {
            id INT PK "AUTO_INCREMENT"
            eliminado BOOLEAN "DEFAULT FALSE"
            nombre VARCHAR(80) "NOT NULL"
            apellido VARCHAR(80) "NOT NULL"
            dni VARCHAR(8) "NOT NULL, UNIQUE"
            fecha_nacimiento DATE "NULLABLE"
            historia_clinica INT FK "UNIQUE"
        }

        HistoriaClinica {
            id INT PK "AUTO_INCREMENT"
            eliminado BOOLEAN "DEFAULT FALSE"
            nro_historia VARCHAR(20) "UNIQUE"
            grupo_sanguineo ENUM "A_PLUS, A_MINUS, B_PLUS, B_MINUS, AB_PLUS, AB_MINUS, O_PLUS, O_MINUS"
            antecedentes TEXT "NULLABLE"
            medicacion_actual TEXT "NULLABLE"
            observaciones TEXT "NULLABLE"
        }

        Paciente 1--1 HistoriaClinica : tiene
```

## Video Demostración

<!-- [Ver video de demostración](#) (10-15 minutos) -->

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo [`LICENSE`](LICENSE) para más detalles.
