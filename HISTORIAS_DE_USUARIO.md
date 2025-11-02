# Historias de Usuario - Sistema de Gestión de Pacientes e Historias Clínicas

Especificaciones funcionales del TPI de Programación 2 para el dominio Paciente → Historia Clínica.

## Tabla de Contenidos

<!--
- [Épica 1: Gestión de Pacientes (Entidad A)](#épica-1-gestión-de-pacientes)
- [Épica 2: Gestión de Historias Clínicas (Entidad B)](#épica-2-gestión-de-historias-clínicas)
- [Épica 3: Operaciones Asociadas (Gestión de la Relación)](#épica-3-operaciones-asociadas)
- [Épica 4: Recuperación de Datos (Baja Lógica)](#épica-4-recuperación-de-datos)
- [Reglas de Negocio (Resumen)](#reglas-de-negocio)
- [Modelo de Datos](#modelo-de-datos)
 -->

---

## Épica 1: Gestión de Pacientes

### HU-001: Crear Paciente

**Como** usuario del sistema
**Quiero** crear un registro de paciente con sus datos básicos
**Para** almacenar la información de nuevos pacientes en la base de datos

#### Criterios de Aceptación

```gherkin
Escenario: Crear paciente sin historia clínica
  Dado que el usuario selecciona "Crear paciente"
  Cuando ingresa nombre "Juan", apellido "Perez", DNI "12345678" y fecha "1991-02-02"
  Y responde "n" a agregar historia clínica
  Entonces el sistema crea al paciente con ID autogenerado
  Y la columna 'historia_clinica_id' es NULL
  Y muestra "Paciente creado exitosamente con ID: X"

Escenario: Crear paciente con historia clínica nueva
  Dado que el usuario selecciona "Crear paciente"
  Cuando ingresa los datos del paciente
  Y responde "s" a agregar historia clínica
  Y cuando ingresa nroHistoria "HC-0009999", grupo sanguíneo "A+" y antecedentes
  Entonces el sistema crea la historia clínica primero (B)
  Y luego crea al paciente (A) con la FK a la historia recién creada
  Y muestra "Paciente creado exitosamente con ID: X"

Escenario: Intento de crear paciente con DNI duplicado
  Dado que existe un paciente con DNI "12345678"
  Cuando el usuario intenta crear un paciente con el mismo DNI
  Entonces el sistema muestra "Ya existe un paciente con el DNI: 12345678"
  Y no crea el registro

Escenario: Intento de crear paciente con campos vacíos
  Dado que el usuario selecciona "Crear paciente"
  Cuando deja el DNI vacío (solo espacios o enter)
  Entonces el sistema muestra "El DNI del paciente no puede estar vacío"
  Y no crea el registro
```

#### Reglas de Negocio Aplicables

- **RN-001**: Nombre, apellido, DNI y fecha de nacimiento son obligatorios.
- **RN-002**: El DNI debe ser único en el sistema.
- **RN-003**: La historia clínica es opcional durante la creación.
- **RN-004**: Si se crea con HC, la HC (B) se inserta antes que el Paciente (A).

#### Implementación Técnica

- **Menú**: Opción 2 (`MenuHandler.createPaciente()`)
- **Servicio**: `PacienteService.insert()`
- **Validación**: `validateDniUnique()`

---

### HU-002: Listar y Buscar Pacientes

**Como** usuario del sistema
**Quiero** ver un listado de todos los pacientes o buscarlos por filtros
**Para** consultar la información almacenada

#### Criterios de Aceptación

```gherkin
Escenario: Listar todos los pacientes
  Dado que existen pacientes en el sistema
  Cuando el usuario selecciona "Listar pacientes" y elige "Listar todos" (1)
  Entonces el sistema muestra todos los pacientes no eliminados
  Y para cada paciente con HC, muestra sus datos (ej: "N° Historia: HC-0009999")
  Y para pacientes sin HC, muestra "Sin historia clínica"

Escenario: Buscar pacientes por nombre/apellido
  Dado que existen pacientes "Juan Perez" y "Miguel Torres"
  Cuando el usuario busca por "perez" (subopción 2)
  Entonces el sistema muestra solo a "Juan Perez"

Escenario: Buscar paciente por DNI
  Dado que existe un paciente con DNI "12345678"
  Cuando el usuario busca por "12345678" (subopción 3)
  Entonces el sistema muestra solo a ese paciente

Escenario: No hay pacientes en el sistema
  Dado que no existen pacientes activos
  Cuando el usuario lista todos los pacientes
  Entonces el sistema muestra "No se encontraron pacientes."
```

#### Reglas de Negocio Aplicables

- **RN-005**: Solo se listan pacientes con `eliminado = FALSE`.
- **RN-006**: La HC se obtiene mediante `LEFT JOIN`.
- **RN-007**: Búsqueda por nombre/apellido usa `LIKE %filtro%` (case-insensitive).
- **RN-008**: Búsqueda por DNI es una coincidencia exacta.

#### Implementación Técnica

- **Menú**: Opción 1 (`MenuHandler.readPaciente()`)
- **Servicio**: `PacienteService.selectAll(false)`, `searchByFilter()`, `selectByDni()`
- **DAO**: `PacienteDAO.SELECT_ALL_SQL`, `SEARCH_BY_FILTER_SQL`, `SELECT_BY_DNI_SQL`

---

### HU-003: Actualizar Paciente

**Como** usuario del sistema
**Quiero** modificar los datos de un paciente existente
**Para** mantener la información actualizada

#### Criterios de Aceptación

```gherkin
Escenario: Actualizar solo apellido
  Dado que existe el paciente ID 2048, apellido "Perez"
  Cuando el usuario actualiza al paciente ID 2048
  Y presiona Enter en nombre y DNI
  Y escribe "Gonzalez" en apellido
  Entonces el sistema actualiza solo el apellido

Escenario: Actualizar con DNI duplicado
  Dado que existen pacientes con DNI "111" y "222"
  Cuando el usuario intenta cambiar DNI del paciente "222" a "111"
  Entonces el sistema muestra "Ya existe un paciente con el DNI: 111"
  Y no actualiza el registro

Escenario: Actualizar con mismo DNI
  Dado que existe el paciente ID 2048 con DNI "12345678"
  Cuando el usuario actualiza otros campos de ID 2048 (ej: nombre)
  Y deja el DNI como "12345678" (o presiona Enter)
  Entonces el sistema permite la actualización (validación ignora el ID propio)

Escenario: Agregar historia clínica a paciente sin HC
  Dado que el paciente ID 2048 no tiene HC
  Cuando el usuario actualiza al paciente ID 2048
  Y responde "s" a "agregar una nueva historia clínica"
  Entonces el sistema crea la HC y la asocia al paciente
```

#### Reglas de Negocio Aplicables

- **RN-009**: Se valida DNI único (RN-002) excepto para el mismo ID de paciente.
- **RN-010**: Campos vacíos (Enter) mantienen el valor original.
- **RN-011**: Se puede crear y asociar una HC si el paciente no tenía una.

#### Implementación Técnica

- **Menú**: Opción 3 (`MenuHandler.updatePaciente()`)
- **Servicio**: `PacienteService.update()`
- **Validación**: `validateDniUnique(dni, pacienteId)`

---

### HU-004: Eliminar Paciente (y su Historia Clínica asociada)

**Como** usuario del sistema
**Quiero** eliminar un paciente del sistema
**Para** mantener solo registros activos

#### Criterios de Aceptación

```gherkin
Escenario: Eliminar paciente con historia clínica (Eliminación lógica en cascada)
  Dado que existe el paciente ID 2048 asociado a la HC ID 1024
  Cuando el usuario elimina al paciente ID 2048
  Entonces el sistema primero marca la HC ID 1024 como 'eliminado = TRUE'
  Y luego marca al paciente ID 2048 como 'eliminado = TRUE'
  Y muestra "Paciente ID: 2048 eliminado exitosamente!"

Escenario: Paciente eliminado no aparece en listados
  Dado que se eliminó al paciente ID 2048
  Cuando el usuario lista todos los pacientes
  Entonces el paciente ID 2048 no aparece en los resultados
```

#### Reglas de Negocio Aplicables

- **RN-012**: Eliminación es lógica (`eliminado = TRUE`), no física (`DELETE`).
- **RN-013**: Al eliminar un paciente (A), su historia clínica (B) asociada también debe ser marcada como eliminada.
- **RN-014**: La eliminación de la HC (B) debe ocurrir _antes_ de la del Paciente (A) para mantener la integridad si la FK tuviera `ON DELETE RESTRICT`.

#### Implementación Técnica

- **Menú**: Opción 4 (`MenuHandler.deletePaciente()`)
- **Servicio**: `PacienteService.delete()` (que internamente llama a `historiaClinicaService.delete()` si existe)

---

## Épica 2: Gestión de Historias Clínicas

### HU-005: Crear Historia Clínica Independiente

**Como** usuario del sistema
**Quiero** crear una historia clínica sin asociarla a ningún paciente
**Para** tener historias disponibles para asignación posterior

#### Criterios de Aceptación

```gherkin
Escenario: Crear historia clínica válida
  Dado que el usuario selecciona "Crear Historia Clínica"
  Cuando ingresa nroHistoria "HC-1234567", grupo "O-", etc.
  Entonces el sistema crea la HC con ID autogenerado
  Y muestra "Historia Clínica creada con ID: X"

Escenario: Intento de crear HC con nroHistoria duplicado
  Dado que existe una HC con nroHistoria "HC-1234567"
  Cuando el usuario intenta crear otra HC con el mismo número
  Entonces el sistema muestra un error de violación de unicidad
```

#### Reglas de Negocio Aplicables

- **RN-015**: El `nroHistoria` debe ser único en el sistema.
- **RN-016**: `grupoSanguineo` es un `Enum`.

#### Implementación Técnica

- **Menú**: Opción 6 (`MenuHandler.createHistoriaClinica()`)
- **Servicio**: `HistoriaClinicaService.insert()`

---

### HU-006: Listar y Buscar Historias Clínicas

**Como** usuario del sistema
**Quiero** ver todas las historias clínicas o buscarlas por filtro
**Para** consultar información médica

#### Criterios de Aceptación

```gherkin
Escenario: Listar todas las historias
  Dado que existen historias clínicas
  Cuando el usuario selecciona "Listar Historia Clínicas" y elige "Listar todas" (1)
  Entonces el sistema muestra todas las HC no eliminadas

Escenario: Buscar HC por palabra clave (filtro)
  Dado que existe una HC con antecedentes "Alergia Penicilina" y grupo "A+"
  Cuando el usuario busca por "peni" (subopción 3)
  Entonces el sistema encuentra la HC
  Cuando el usuario busca por "a+" (subopción 3)
  Entonces el sistema traduce "a+" a "A_PLUS" y encuentra la HC
```

#### Reglas de Negocio Aplicables

- **RN-017**: Solo se listan HC con `eliminado = FALSE`.
- **RN-018**: Búsqueda por filtro (Opción 3) busca en `nroHistoria`, `antecedentes`, `medicacionActual`, `observaciones` Y `grupoSanguineo`.
- **RN-019**: La búsqueda de grupo sanguíneo traduce el formato (ej: "A+") al formato Enum ("A_PLUS").

#### Implementación Técnica

- **Menú**: Opción 5 (`MenuHandler.readHistoriaClinica()`)
- **Servicio**: `HistoriaClinicaService.selectAll(false)`, `searchByFilter()`

---

### HU-007: Eliminar Historia Clínica por ID (Operación Peligrosa)

**Como** usuario del sistema
**Quiero** eliminar una historia clínica directamente por su ID
**Para** remover direcciones no utilizadas

⚠️ **ADVERTENCIA**: Esta operación puede dejar referencias huérfanas si la HC está asociada a un paciente.

#### Criterios de Aceptación

```gherkin
Escenario: Eliminar HC no asociada
  Dado que existe la HC ID 1025 (independiente)
  Cuando el usuario elimina la HC ID 1025
  Entonces el sistema marca 'eliminado = TRUE' para la HC 1025
  Y muestra "Historia Clínica ID: 1025 eliminada exitosamente!"

Escenario: Eliminar HC asociada (Genera referencia huérfana)
  Dado que el paciente ID 2048 tiene 'historia_clinica_id = 1024'
  Cuando el usuario elimina la HC ID 1024 (usando esta opción)
  Entonces el sistema marca la HC 1024 como 'eliminado = TRUE'
  PERO el paciente ID 2048 mantiene 'historia_clinica_id = 1024'
  Y el paciente queda apuntando a una HC eliminada (referencia huérfana)
```

#### Reglas de Negocio Aplicables

- **RN-020**: Eliminación es lógica (`eliminado = TRUE`).
- **RN-021**: Esta operación NO verifica si la HC está asociada a un paciente.
- **RN-022**: Causa referencias huérfanas. Usar HU-008 como alternativa segura.

#### Implementación Técnica

- **Menú**: Opción 8 (`MenuHandler.deleteHistoriaClinica()`)
- **Servicio**: `HistoriaClinicaService.delete()`

---

## Épica 3: Operaciones Asociadas

### HU-008: Eliminar Historia Clínica por Paciente (Operación Segura)

**Como** usuario del sistema
**Quiero** desasociar y eliminar la historia clínica de un paciente específico
**Para** remover la HC sin dejar referencias huérfanas

✅ **RECOMENDADO**: Esta es la forma segura de eliminar una HC asociada.

#### Criterios de Aceptación

```gherkin
Escenario: Eliminar HC de paciente correctamente
  Dado que el paciente ID 2048 tiene 'historia_clinica_id = 1024'
  Cuando el usuario elimina la HC por paciente ID 2048
  Entonces el sistema primero actualiza 'paciente' (ID 2048) 'historia_clinica_id = NULL'
  Y luego marca la HC ID 1024 como 'eliminado = TRUE'
  Y muestra "Historia Clínica desasociada y eliminada exitosamente!"

Escenario: Intento en paciente sin HC
  Dado que el paciente ID 2048 no tiene HC
  Cuando el usuario intenta eliminar su HC
  Entonces el sistema muestra "El paciente no tiene una historia clínica asociada."
```

#### Reglas de Negocio Aplicables

- **RN-023**: Se actualiza la FK (`historia_clinica_id = NULL`) en Paciente _antes_ de eliminar la HC.
- **RN-024**: Previene referencias huérfanas.
- **RN-025**: Operación en dos pasos: UPDATE Paciente → UPDATE HistoriaClinica.

#### Implementación Técnica

- **Menú**: Opción 10 (`MenuHandler.deleteHistoriaClinicaDePaciente()`)
- **Servicio**: `PacienteService.update()` (para setear a null) y `HistoriaClinicaService.delete()`

---

### HU-009: Gestionar Historia Clínica por Paciente

**Como** usuario del sistema
**Quiero** actualizar la HC de un paciente, o asignarle una si no tiene
**Para** mantener centralizada la gestión de la relación

#### Criterios de Aceptación

```gherkin
Escenario: Actualizar HC de paciente (Paciente ya tiene HC)
  Dado que el paciente ID 2048 tiene la HC ID 1024
  Cuando el usuario selecciona "Actualizar HC por ID de paciente" (Opción 9) e ingresa 2048
  Entonces el sistema muestra los datos de la HC 1024 y pide nuevos valores
  Y actualiza la HC 1024

Escenario: Asignar HC Nueva (Paciente no tiene HC)
  Dado que el paciente ID 2048 no tiene HC
  Cuando el usuario selecciona Opción 9 e ingresa 2048
  Y el sistema pregunta "¿Desea crear una? (s/n)"
  Y el usuario responde "s" y crea la HC 1025
  Entonces el sistema asocia la nueva HC 1025 al paciente ID 2048

Escenario: Asignar HC Existente (Paciente no tiene HC)
  Dado que el paciente ID 2048 no tiene HC y existe la HC 1026
  Cuando el usuario selecciona Opción 9 e ingresa 2048
  Y el sistema muestra el submenú (1. Crear, 2. Asignar)
  Y el usuario elige "2" e ingresa el ID 1026
  Entonces el sistema asocia la HC 1026 al paciente ID 2048
```

#### Reglas de Negocio Aplicables

- **RN-026**: Si el paciente tiene HC, solo se puede actualizar.
- **RN-027**: Si el paciente no tiene HC, se ofrece un submenú para (1) Crear o (2) Asignar existente.

#### Implementación Técnica

- **Menú**: Opción 9 (`MenuHandler.updateHistoriaClinicaDePaciente()`)
- **Lógica**: Contiene el `switch` que implementamos para manejar los 3 escenarios.

---

## Épica 4: Recuperación de Datos

### HU-010: Gestionar Datos Eliminados

**Como** administrador del sistema
**Quiero** listar y recuperar registros eliminados lógicamente
**Para** revertir eliminaciones accidentales

#### Criterios de Aceptación

```gherkin
Escenario: Listar Pacientes Eliminados
  Dado que el paciente ID 2048 fue eliminado (eliminado = TRUE)
  Cuando el usuario selecciona "Submenú de recuperación" (11)
  Y elige "Listar Pacientes Eliminados" (3)
  Entonces el sistema muestra al paciente ID 2048

Escenario: Recuperar Paciente por ID
  Dado que el paciente ID 2048 fue eliminado
  Cuando el usuario elige "Recuperar Paciente por ID" (1) e ingresa 2048
  Entonces el sistema actualiza 'paciente' (ID 2048) 'eliminado = FALSE'
  Y el paciente ID 2048 vuelve a aparecer en el listado principal (Opción 1)
```

#### Reglas de Negocio Aplicables

- **RN-028**: El listado principal (HU-002) solo muestra `eliminado = FALSE`.
- **RN-029**: El listado de recuperación solo muestra `eliminado = TRUE`.
- **RN-030**: Recuperar es un `UPDATE` que setea `eliminado = FALSE`.
- **RN-031**: Recuperar un Paciente NO recupera automáticamente su HC (comportamiento actual).

#### Implementación Técnica

- **Menú**: Opción 11 (`MenuHandler.recover()`)
- **Servicio**: `PacienteService.selectAll(true)`, `HistoriaClinicaService.selectAll(true)`, `PacienteService.recover()`, `HistoriaClinicaService.recover()`
