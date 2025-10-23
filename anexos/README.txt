============================================================
README - Scripts SQL del Trabajo Final Integrador (Bases de Datos I)
============================================================

Este archivo describe el contenido y el orden de ejecución
recomendado para los scripts SQL del proyecto.

Versión de SGBD Utilizada: MySQL 8.0.43

Orden de Ejecución Sugerido:
-----------------------------

1.  01_esquema.sql
    - Crea la estructura de la base de datos (tablas, PKs, FKs, CHECKs).
    - ¡IMPORTANTE! Ejecutar solo una vez o asegurarse de que sea idempotente.

2.  02_catalogos.sql
    - Inserta los datos iniciales en las tablas maestras (GrupoSanguineo).

3.  03_carga_masiva.sql
    - Inserta un gran volumen de datos ficticios (~350k registros) en las tablas principales.
    - ¡ADVERTENCIA! Este script puede tardar varios minutos en ejecutarse. Vacía las tablas antes de insertar.

4.  04_indices.sql
    - Crea los índices adicionales para optimizar el rendimiento de las consultas.
    - ¡IMPORTANTE! Ejecutar solo una vez o asegurarse de que sea idempotente.

5.  06_vistas.sql
    - Crea las vistas utilizadas para simplificar el acceso a los datos.
    - ¡IMPORTANTE! Ejecutar solo una vez o asegurarse de que sea idempotente.

6.  07_seguridad.sql
    - Crea el usuario de la aplicación ('user_gestion') y le asigna los privilegios mínimos necesarios.
    - ¡IMPORTANTE! Ejecutar solo una vez o asegurarse de que sea idempotente.

Scripts Adicionales (Ejecución según necesidad):
-------------------------------------------------

* 05_consultas.sql
    - Contiene ejemplos de consultas complejas y útiles para el sistema. Se pueden ejecutar individualmente.

* 05_explain.sql
    - Script para demostrar el uso de EXPLAIN y el impacto de los índices en el plan de ejecución.

* 07_pruebas_integridad.sql
    - Contiene sentencias INSERT diseñadas para fallar y probar las constraints. Se ejecutan individualmente.

* 08_transacciones.sql
    - Contiene el ejemplo conceptual de manejo de transacciones en Java (no ejecutable directamente en SQL).

* 09_concurrencia_guiada.sql
    - Guion para simular deadlocks y comparar niveles de aislamiento usando dos sesiones de cliente SQL.