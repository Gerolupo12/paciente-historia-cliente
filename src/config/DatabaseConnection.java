package config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Administra la conexión de la base de datos para la aplicación. Utiliza JDBC
 * para conectarse a una base de datos MySQL.
 *
 * @author alpha team
 */
public class DatabaseConnection {

    private static final Properties PROPS = new Properties();

    // *** Bloque estático para cargar el controlador ***
    // Este bloque se ejecuta solo UNA VEZ cuando la clase se carga en la memoria.
    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (input == null) {
                System.err.println("❌ ERROR: archivo db.properties no encontrado en la ruta de clase.");
                // Forzar salida para evitar el uso de credenciales en blanco
                throw new RuntimeException("¡Archivo de configuración de la BD no encontrado!");
            }
            // Cargar las propiedades
            PROPS.load(input);
            // Paso 1: Cargar la clase del Controlador JDBC de MySQL.
            Class.forName(PROPS.getProperty("db.driverClass"));
            System.out.println("✅ El Controlador JDBC de MySQL fué registrado correctamente!");
        } catch (IOException | ClassNotFoundException e) {
            // Este error significa que el archivo JAR no está en la carpeta Libraries.
            System.err.println("❌ Error: No se encontró el controlador JDBC de MySQL.");
            throw new RuntimeException("¡No se encuentra el controlador en la ruta de clases!", e);
        }
    }

    /**
     * Método para obtener una conexión a la base de datos.
     *
     * @return Connection si la conexión es exitosa.
     * @throws SQLException Si se produce un error de acceso a la base de datos.
     */
    public static Connection getConnection() throws SQLException {

        // Se leen las propiedades directamente del objeto PROPS
        String URL = PROPS.getProperty("db.url");
        String USER = PROPS.getProperty("db.user");
        String PASSWORD = PROPS.getProperty("db.password");

        // Paso 2: Establecer la conexión mediante DriverManager.
        // Dado que el controlador se carga en el bloque estático, DriverManager sabe
        // cómo gestionar la URL.
        System.out.println("Intentando conectar con la base de datos...");
        // Validación adicional para asegurarse de que las credenciales no estén vacías
        if (URL == null || URL.isEmpty() || USER == null || USER.isEmpty()
                || PASSWORD == null || PASSWORD.isEmpty()) {
            throw new SQLException("Configuración de la base de datos incompleta o inválida.");
        }
        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("✅ Conexión a la base de datos establecida correctamente!");
        return connection;
    }

    // *** Opcional: Método para cerrar la conexión ***
    /**
     * Cierra la conexión a la base de datos dada de forma segura.
     *
     * @param connection La conexión a cerrar.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✅ Conexión a la base de datos cerrada.");
            } catch (SQLException e) {
                System.err.println("❌ Error al cerrar la conexión a la base de datos: " + e.getMessage());
            }
        }
    }

}
