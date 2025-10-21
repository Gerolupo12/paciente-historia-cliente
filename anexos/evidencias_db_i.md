# Anexo - Evidencias de Ejecución e Interacción con IA

Este documento recopila las evidencias visuales (capturas de pantalla) y los registros de interacción con herramientas de Inteligencia Artificial (IA) correspondientes a cada etapa del Trabajo Final Integrador de Bases de Datos I, como se referencia en el informe principal (`informe_db_i.md`).

---

## Etapa 1: Modelado y Definición

### 1.1 Resultados de Ejecución (`validacion_constraints.sql`)

Resultados de la validación de las `constraints` del esquema normalizado.

- **Inserciones Correctas:**

  - `[Captura de pantalla mostrando la ejecución exitosa de inserciones válidas]`

- **Inserciones Erróneas (Validación de Constraints):**
  - **Error 1 (UNIQUE DNI):** `[Captura de pantalla del error UNIQUE DNI]`
  - **Error 2 (CHECK Fecha Nacimiento):** `[Captura de pantalla del error CHECK Fecha]`
  - **Error 3 (CHECK Longitud DNI):** `[Captura de pantalla del error CHECK Longitud DNI]`
  - **Error 4 (UNIQUE Matrícula):** `[Captura de pantalla del error UNIQUE Matrícula]`
  - **Error 5 (FOREIGN KEY Persona ID):** `[Captura de pantalla del error FK Persona ID]`
  - **Error 6 (UNIQUE Paciente Persona ID):** `[Captura de pantalla del error UNIQUE Paciente Persona ID]`
  - **Error 7 (UNIQUE Paciente Historia ID):** `[Captura de pantalla del error UNIQUE Paciente Historia ID]`
  - **Error 8 (CHECK Formato Nro Historia):** `[Captura de pantalla del error CHECK Nro Historia]`

### 1.2 Interacción con IA (Modelado y Normalización)

Registro del diálogo con la IA para validar el proceso de normalización.

- **Prompt Utilizado:**

```plaintext
Actúa como un tutor de la asignatura Bases de Datos I (base de datos relacionales). Estamos con mi grupo diseñando el esquema para un sistema de gestión de pacientes [...]. Nuestra idea es crear una tabla Persona para solucionar esto. ¿Puedes guiarnos con preguntas para validar si esta es la mejor estrategia? Ayúdanos a pensar en las ventajas de escalabilidad [...].
```

- **Fragmento Relevante de la Respuesta de la IA:**

  - `[Pega aquí el fragmento del chat donde la IA hace las preguntas guía]`

- **Reflexión del Grupo / Respuesta a la IA:**
  - `[Pega aquí la respuesta que elaboraron para la IA]`

---

## Etapa 2: Carga Masiva de Datos

### 2.1 Resultados de Ejecución (`carga_masiva_datos.sql`)

Confirmación de la carga y conteo final de registros.

- **Confirmación de Carga y Conteo:**
  - `[Captura de pantalla del final del script mostrando "CARGA COMPLETADA" y la tabla de conteos]`

### 2.2 Resultados de Pruebas de Rendimiento (`pruebas_rendimiento_indices_profiling.sql`)

Tabla comparativa generada por el script de profiling.

- **Tabla Comparativa de Tiempos (Profiling):**
  - `[Captura de pantalla o output de la tabla final del script de profiling]`

### 2.3 Interacción con IA (Carga Masiva y Optimización)

Registro del diálogo con la IA sobre técnicas de generación de datos.

- **Prompt (Distribución Ponderada):**

```plaintext
[...] Noté que la distribución de los grupos sanguíneos es uniforme [...]. Quiero implementar una distribución ponderada [...]. ¿Puedes explicarme la lógica detrás de usar `RAND()` con `CASE` [...]? No me des el código completo [...].
```

- **Respuesta IA (Distribución Ponderada):**

  - `[Pega aquí el fragmento del chat sobre distribución ponderada]`

- **Prompt (CTE Recursivo):**

```plaintext
Mi compañera usó un método con `CROSS JOIN` para generar secuencias [...], pero es muy verboso. ¿Existe una forma más moderna o limpia [...] en MySQL 8.0?
```

- **Respuesta IA (CTE Recursivo):**
  - `[Pega aquí el fragmento del chat sobre CTEs recursivos]`

---

## Etapa 3: Consultas Complejas y Útiles

### 3.1 Resultados de Ejecución (`consultas_complejas.sql`)

Ejemplos de los resultados obtenidos al ejecutar las consultas complejas.

- **Consulta 1 (Ficha Pacientes):** `[Captura de pantalla con algunos resultados de la Consulta 1]`
- **Consulta 2 (Pacientes por Especialidad):** `[Captura de pantalla con resultados de la Consulta 2]`
- **Consulta 3 (Grupos Minoritarios):** `[Captura de pantalla con resultados de la Consulta 3]`
- **Consulta 4 (Profesionales Sobrecargados):** `[Captura de pantalla con resultados de la Consulta 4]`
- **Uso de Vista (`vw_pacientes_activos`):** `[Captura de pantalla usando la vista vw_pacientes_activos]`

### 3.2 Interacción con IA (Diseño y Optimización de Consultas)

Registro del diálogo sobre la optimización de consultas.

- **Prompt Utilizado:**

```plaintext
Actúa como un analista de datos. [...] Hemos pensado en usar una subconsulta correlacionada [...], pero nos preocupa el rendimiento [...]. ¿Podrías explicarnos las ventajas y desventajas [...] y mencionarnos qué alternativa con JOIN podríamos considerar? [...]
```

- **Fragmento Relevante de la Respuesta de la IA:**
  - `[Pega aquí el fragmento del chat comparando subconsultas vs JOIN]`

---

## Etapa 4: Seguridad e Integridad

### 4.1 Resultados de Ejecución (`seguridad_integridad.sql`)

Evidencias de la creación del usuario restringido y el uso de las vistas.

- **Prueba de Usuario con Mínimos Privilegios:**

  - `[Captura de pantalla mostrando el error al intentar `DROP TABLE` con app_user]`

- **Resultados de Consultas a las Vistas:**
  - **`vw_pacientes_activos`:** `[Captura de pantalla consultando vw_pacientes_activos]`
  - **`vw_profesionales_con_datos`:** `[Captura de pantalla consultando vw_profesionales_con_datos]`

_(Nota: Las pruebas de integridad ya están documentadas en la Etapa 1)._

### 4.2 Interacción con IA (Seguridad)

Registro del diálogo sobre privilegios mínimos.

- **Prompt Utilizado:**

```plaintext
[...] La consigna me pide crear un usuario con privilegios mínimos [...]. No estoy seguro de qué permisos exactos debería concederle. ¿Puedes darme una pista sobre los 4 permisos esenciales [...] y explícame por qué no debería darle `ALTER` o `DROP`?
```

- **Fragmento Relevante de la Respuesta de la IA:**
  - `[Pega aquí el fragmento del chat sobre permisos CRUD y riesgos DDL]`

---

## Etapa 5: Concurrencia y Transacciones

### 5.1 Resultados de Simulación (`concurrencia_transacciones.sql`)

Evidencias de las pruebas de deadlock y niveles de aislamiento.

- **Simulación de Deadlock:**

  - `[Captura de pantalla mostrando el mensaje de error de deadlock en una de las sesiones]`

- **Comparación de Niveles de Aislamiento:**
  - **READ COMMITTED:** `[Captura(s) de pantalla mostrando la lectura no repetible]`
  - **REPEATABLE READ:** `[Captura(s) de pantalla mostrando la lectura consistente (sin lectura no repetible)]`

### 5.2 Interacción con IA (Concurrencia)

Registro del diálogo sobre la simulación de deadlocks.

- **Prompt Utilizado:**

```plaintext
[...] necesito simular un `deadlock` en MySQL [...]. ¿Puedes darme un guion genérico paso a paso (sin código SQL específico) de cómo [...] provocar un `deadlock`? Quiero entender la secuencia de eventos.
```

- **Fragmento Relevante de la Respuesta de la IA:**
  - `[Pega aquí el fragmento del chat explicando la secuencia lógica del deadlock]`

---

_Fin del Anexo de Evidencias._
