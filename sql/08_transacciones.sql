-- =====================================================================
-- CONSULTA SEGURA EN JAVA
-- =====================================================================
-- A continuación, se muestra un ejemplo de cómo se ejecutan consultas
-- seguras desde el código Java usando PreparedStatement.
-- Esta técnica evita inyección SQL al separar la consulta de los valores
-- ingresados por el usuario.
-- =====================================================================
/* 
public boolean existeUsuarioPorDni(String dni, Connection connection) {
String sql = "SELECT * FROM usuarios WHERE dni = ?";

// Preparar la declaración
PreparedStatement stmt = connection.prepareStatement(sql);

// Establecer parámetros DNI
stmt.setString(1, dni);  // Primer parámetro (?)

// Ejecutar
ResultSet rs = stmt.executeQuery();

return rs.next();
}
 */
-- Vectores de Ataque Probados:
-- 1. `40123456' OR '1'='1` - Inyección para obtener todos los registros
-- 2. `40123456' --` - Intento de comentar condiciones adicionales  
-- 3. `' OR dni LIKE '40%` - Inyección con operador LIKE
-- 4. `'; DROP TABLE usuarios; --` - Ataque de eliminación de datos
-- Comentario:
-- PreparedStatement valida que el DNI sea un string
-- Los caracteres especiales como ' se convierten en \'
-- Con este método el valor del DNI se pasa como parámetro, evitando que
-- alguien inserte código malicioso dentro de la consulta.
-- Es una buena práctica de seguridad que siempre debe aplicarse
-- al trabajar con datos ingresados por usuarios.
-- =====================================================================