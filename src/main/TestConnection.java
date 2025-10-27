package main;

import static config.DatabaseConnection.getConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestConnection {
    // *** Prueba de conexión ***
    public static void main(String[] args) {
        /*
         * Prueba simple para comprobar si la conexión funciona:
         * Se usa un bloque try-with-resources para asegurar que la conexión
         * se cierre automáticamente al salir del bloque.
         * No es necesario llamar explícitamente a conn.close().
         */
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("✅ Conexión establecida con éxito.");

                // Crear una consulta
                String sql = "SELECT * FROM persona LIMIT 10";

                // Ejecutar consulta SQL con PreparedStatement
                try (PreparedStatement pstmt = conn.prepareStatement(sql);
                        ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("Listado de personas:");
                    while (rs.next()) {
                        String dni = rs.getString("dni");
                        String nombre = rs.getString("nombre");
                        String apellido = rs.getString("apellido");
                        System.out.println("DNI: " + dni + " - " + apellido + ", " + nombre);
                    }
                }
            } else {
                System.out.println("❌ No se pudo establecer la conexión.");
            }
        } catch (SQLException e) {
            // Manejo de errores en la conexión a la base de datos
            System.err.println("⚠️ Error al conectar a la base de datos: " + e.getMessage());
            e.printStackTrace(); // Imprime el stack trace completo para depuración
        }
    }
}
