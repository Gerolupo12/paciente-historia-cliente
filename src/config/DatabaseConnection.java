package config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase utilitaria para gestionar la conexión a la base de datos MySQL.
 * <p>
 * Esta clase implementa un patrón de <strong>Fábrica Estática (Static
 * Factory)</strong> para la obtención de conexiones JDBC.
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li><strong>Carga de Configuración:</strong> Lee los parámetros de conexión
 * (driver, URL, usuario, contraseña) desde un archivo
 * <code>db.properties</code> ubicado en el classpath.</li>
 * <li><strong>Carga del Driver (Fail-Fast):</strong> Utiliza un bloque de
 * inicialización estático (<code>static {}</code>) para cargar el driver JDBC
 * una sola vez cuando la clase es cargada por la JVM. Si el driver
 * (ej: el .jar de MySQL) o el <code>db.properties</code> no se encuentran, la
 * aplicación fallará inmediatamente con un <code>RuntimeException</code>,
 * evitando errores posteriores.</li>
 * <li><strong>Provisión de Conexiones:</strong> Ofrece un método estático
 * {@link #getConnection()} que actúa como una fábrica para crear nuevas
 * conexiones a la base de datos.</li>
 * </ul>
 *
 * <h3>Uso:</h3>
 * 
 * <pre>
 * try (Connection conn = DatabaseConnection.getConnection()) {
 *     // ... usar la conexión (ej: PreparedStatement) ...
 * } catch (SQLException e) {
 *     // ... manejar error de conexión ...
 * }
 * // La conexión se cierra automáticamente por el try-with-resources
 * </pre>
 *
 * @author alpha team
 * @see java.sql.DriverManager
 * @see java.util.Properties
 */
public class DatabaseConnection {

    /**
     * Objeto Properties para cargar la configuración de la base de datos desde
     * "db.properties".
     */
    private static final Properties PROPS = new Properties();

    /**
     * Flag para activar/desactivar los mensajes de depuración.
     */
    private static boolean debug = false;

    static {
        /**
         * Bloque de inicialización estático (se ejecuta una sola vez).
         * Propósito: Cargar la configuración y el driver JDBC al inicio.
         *
         * Flujo (Fail-Fast):
         * 1. Busca "db.properties" en el classpath.
         * 2. Si no lo encuentra, lanza RuntimeException (la app no puede funcionar).
         * 3. Carga las propiedades (URL, user, pass, driver).
         * 4. Carga la clase del driver (Class.forName).
         * 5. Si no encuentra el driver (falta el JAR), lanza RuntimeException.
         */
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                System.err.println("❌ ERROR: archivo db.properties no encontrado en la ruta de clase.");
                // Forzar salida (Fail-Fast) para evitar el uso de credenciales en blanco
                throw new RuntimeException("¡Archivo de configuración de la BD no encontrado!");
            }

            // Cargar las propiedades
            PROPS.load(input);

            // Paso 1: Cargar la clase del Controlador JDBC de MySQL.
            Class.forName(PROPS.getProperty("db.driverClass"));
            log("✅ El Controlador JDBC de MySQL fué registrado correctamente!");

        } catch (IOException | ClassNotFoundException e) {

            // Este error significa que el archivo JAR no está en la carpeta Libraries
            // o que 'db.driverClass' está mal escrito en db.properties.
            error("No se encontró el controlador JDBC de MySQL.", e);
            throw new RuntimeException("¡No se encuentra el controlador en la ruta de clases!", e);
        }
    }

    /**
     * Constructor privado para prevenir la instanciación.
     * Esta es una clase utilitaria que solo debe usarse de forma estática.
     */
    private DatabaseConnection() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no debe ser instanciada");
    }

    /**
     * Obtiene una nueva conexión a la base de datos.
     * <p>
     * Este método actúa como una fábrica. Cada llamada retorna una
     * <strong>nueva instancia</strong> de {@link Connection}.
     * El llamador (caller) es responsable de cerrar esta conexión,
     * preferiblemente usando un bloque <code>try-with-resources</code>.
     * </p>
     *
     * <h3>Validaciones:</h3>
     * <p>
     * Antes de intentar conectar, este método valida que las propiedades
     * 'db.url' y 'db.user' cargadas desde <code>db.properties</code> no sean
     * nulas o vacías. También valida que 'db.password' no sea nulo (aunque
     * sí puede ser vacío).
     * </p>
     *
     * @return Una {@link Connection} activa y lista para ser usada.
     * @throws SQLException Si las credenciales (URL, usuario, contraseña) son
     *                      inválidas, o si no se puede establecer comunicación con
     *                      la base de datos.
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

        // Se permite contraseña vacía (PASSWORD.isEmpty()), pero no nula.
        if (PASSWORD == null) {
            throw new SQLException("La contraseña de la base de datos no puede ser nula.");
        }

        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        log("✅ Conexión a la base de datos establecida correctamente!");

        return connection;
    }

    // ============ MÉTODOS AUXILIARES PARA DEPURACIÓN ============
    /**
     * Habilita o deshabilita el modo de depuración (debug).
     * <p>
     * Cuando está habilitado (true), los métodos {@link #log(String)} y
     * {@link #error(String, Exception)} imprimirán información detallada en la
     * consola, incluyendo stack traces de excepciones.
     * </p>
     * <p>
     * <strong>Valor por defecto:</strong> <code>false</code> (deshabilitado).
     * </p>
     *
     * @param enable true para habilitar logs detallados, false para modo
     *               silencioso.
     */
    public static void setDebug(boolean enable) {
        debug = enable;
    }

    /**
     * Escribe un mensaje de log en <code>System.out</code>, <strong>solo si el modo
     * debug está habilitado</strong>.
     *
     * @param message El mensaje de depuración a mostrar.
     */
    private static void log(String message) {
        if (debug)
            System.out.println(message);
    }

    /**
     * Escribe un mensaje de error en <code>System.err</code>.
     * <p>
     * El mensaje de error (<code>message</code>) se muestra siempre.
     * El stack trace de la excepción (<code>e</code>) solo se imprime
     * si el modo debug está habilitado.
     * </p>
     *
     * @param message El mensaje de error descriptivo.
     * @param e       La excepción que causó el error (puede ser null).
     */
    private static void error(String message, Exception e) {
        System.err.println("❌ ERROR: " + message);
        if (debug && e != null)
            e.printStackTrace(System.err);
    }

}
