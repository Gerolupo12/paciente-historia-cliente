package main;

import static config.DatabaseConnection.getConnection;
import java.sql.*;

/**
 * Clase para probar la conexión a la base de datos y realizar una consulta de
 * ejemplo.
 * 
 * @author alpha team
 */
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
                System.out.println("✅ Conexión establecida con éxito.\n");

                // Crear una consulta
                String sql = """
                            SELECT
                                p.id AS paciente_id,
                                p.nombre,
                                p.apellido,
                                p.dni,
                                p.fecha_nacimiento,
                                hc.id AS hc_id,
                                hc.nro_historia,
                                gs.nombre_enum,
                                hc.antecedentes,
                                hc.medicacion_actual,
                                hc.observaciones
                            FROM Paciente p
                            LEFT JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id
                            LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
                            WHERE p.eliminado = FALSE
                            ORDER BY p.dni
                            LIMIT 10
                        """;

                // Ejecutar consulta SQL con PreparedStatement
                try (PreparedStatement pstmt = conn.prepareStatement(sql);
                        ResultSet rs = pstmt.executeQuery()) {
                    // Obtener información de la conexión
                    DatabaseMetaData metaData = conn.getMetaData();
                    System.out.println("Usuario conectado: " + metaData.getUserName());
                    System.out.println("Base de datos: " + conn.getCatalog());
                    System.out.println("URL: " + metaData.getURL());
                    System.out.println("Driver: " + metaData.getDriverName() + " v" + metaData.getDriverVersion());

                    // Mostrar resultados de la consulta
                    System.out.println("\nConsulta de Prueba - Listado de Pacientes:\n");
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
