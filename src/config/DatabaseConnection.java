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
    private static boolean debug = false;

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
            log("✅ El Controlador JDBC de MySQL fué registrado correctamente!");

        } catch (IOException | ClassNotFoundException e) {
            // Este error significa que el archivo JAR no está en la carpeta Libraries.
            error("No se encontró el controlador JDBC de MySQL.", e);
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
        log("Intentando conectar con la base de datos...");
        // Validación adicional para asegurarse de que las credenciales no estén vacías
        if (URL == null || URL.trim().isEmpty()) {
            throw new SQLException("La URL de la base de datos no puede ser nula o vacía.");
        }
        if (USER == null || USER.trim().isEmpty()) {
            throw new SQLException("El usuario de la base de datos no puede ser nulo o vacío.");
        }
        if (PASSWORD == null) {
            throw new SQLException("La contraseña de la base de datos no puede ser nula.");
        }
        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        log("✅ Conexión a la base de datos establecida correctamente!");
        return connection;
    }

    // ============ MÉTODOS AUXILIARES PARA DEPURACIÓN ============
    /**
     * Activa o desactiva los mensajes de depuración.
     * 
     * @param enable true para mostrar logs detallados, false para solo mostrar
     *               errores.
     */
    public static void setDebug(boolean enable) {
        debug = enable;
    }

    /**
     * Muestra un mensaje solo si el modo debug está activo.
     */
    private static void log(String message) {
        if (debug)
            System.out.println(message);
    }

    /**
     * Muestra un mensaje de error (siempre visible).
     */
    private static void error(String message, Exception e) {
        System.err.println("❌ ERROR: " + message);
        if (debug)
            e.printStackTrace(System.err);
    }

}
