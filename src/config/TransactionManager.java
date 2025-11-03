package config;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Gestiona el ciclo de vida de las transacciones JDBC.
 *
 * <p>
 * Esta clase es una utilidad crucial para asegurar la atomicidad (el principio
 * de "todo o nada") en las operaciones de la capa de servicio que involucran
 * múltiples accesos a la base de datos (ej: insertar un Paciente y su
 * HistoriaClinica al mismo tiempo).
 * </p>
 *
 * <p>
 * <b>Patrón de Diseño:</b> Implementa {@link AutoCloseable} para ser utilizada
 * en bloques <code>try-with-resources</code>. Esta es la característica
 * principal, ya que garantiza que el método {@link #close()} se llame
 * siempre, incluso si ocurre una excepción.
 * </p>
 *
 * <p>
 * <b>Flujo de Uso (try-with-resources):</b>
 * </p>
 *
 * <pre>
 * // 1. Obtener la conexión
 * try (Connection conn = DatabaseConnection.getConnection();
 *         // 2. Iniciar el TransactionManager con esa conexión
 *         TransactionManager tm = new TransactionManager(conn)) {
 *
 *     // 3. Iniciar la transacción (pone setAutoCommit(false))
 *     tm.startTransaction();
 *
 *     // 4. Pasar la MISMA conexión a los DAOs
 *     // (Se necesitan métodos DAO que acepten una Connection)
 *     historiaDAO.insertTx(historia, tm.getConnection());
 *     pacienteDAO.insertTx(paciente, tm.getConnection());
 *
 *     // 5. Si todo fue exitoso, hacer commit
 *     tm.commit();
 *
 * } catch (SQLException e) {
 *     // 6. Si algo falló (SQLException, etc.), el 'catch' se ejecuta
 *     // Y el 'close()' del TransactionManager hará rollback() automáticamente.
 *     System.err.println("Error en la transacción, rollback ejecutado: " + e.getMessage());
 * }
 * // 7. Al salir del bloque try, 'tm.close()' se llama automáticamente.
 * // - Si 'commit()' se llamó, 'close()' solo cierra la conexión.
 * // - Si 'commit()' NO se llamó (por error o por olvido),
 * // 'close()' llama a 'rollback()' antes de cerrar la conexión.
 * </pre>
 *
 * @see AutoCloseable
 * @see java.sql.Connection
 */
public class TransactionManager implements AutoCloseable {

    /**
     * La conexión JDBC que esta instancia está gestionando.
     */
    private final Connection conn;

    /**
     * Flag para rastrear si una transacción está actualmente activa (es decir,
     * entre startTransaction() y commit()/rollback()).
     */
    private boolean transactionActive;

    /**
     * Constructor. Recibe una conexión existente.
     *
     * @param conn La conexión JDBC (obtenida de DatabaseConnection) que se va a
     *             gestionar.
     * @throws SQLException Si la conexión es nula o está cerrada.
     */
    public TransactionManager(Connection conn) throws SQLException {

        if (conn == null || conn.isClosed()) {
            throw new SQLException("La conexión no puede ser nula o ya está cerrada.");
        }

        this.conn = conn;
        this.transactionActive = false;
    }

    /**
     * Inicia la transacción.
     * Llama a {@link Connection#setAutoCommit(boolean) setAutoCommit(false)}
     * en la conexión.
     *
     * @throws SQLException Si la conexión no puede iniciar la transacción.
     */
    public void startTransaction() throws SQLException {

        if (conn == null || conn.isClosed()) {
            throw new SQLException("No se puede iniciar la transacción: conexión no disponible.");
        }

        conn.setAutoCommit(false);
        transactionActive = true;
    }

    /**
     * Confirma (persiste) todos los cambios realizados desde que se llamó a
     * {@link #startTransaction()}.
     * Llama a {@link Connection#commit()}.
     *
     * @throws SQLException Si no hay una transacción activa o la conexión falla.
     */
    public void commit() throws SQLException {

        if (conn == null) {
            throw new SQLException("Error al hacer commit: no hay conexión establecida.");
        }

        if (!transactionActive) {
            throw new SQLException("No hay una transacción activa para hacer commit.");
        }

        conn.commit();
        transactionActive = false;
    }

    /**
     * Revierte (deshace) todos los cambios realizados desde que se llamó a
     * {@link #startTransaction()}.
     * Llama a {@link Connection#rollback()}.
     * <p>
     * Nota: Este método se llama automáticamente por {@link #close()} si la
     * transacción sigue activa al salir de un bloque try-with-resources.
     * </p>
     */
    public void rollback() {

        if (conn != null && transactionActive) {

            try {
                // Intenta revertir los cambios
                conn.rollback();
                transactionActive = false;

            } catch (SQLException e) {
                // Imprime el error de rollback pero no lanza una excepción
                // para no "enmascarar" la excepción original que causó el rollback.
                System.err.println("Error crítico durante el rollback: " + e.getMessage());
            }
        }
    }

    /**
     * Obtiene la conexión JDBC subyacente.
     *
     * @return La {@link Connection} gestionada.
     */
    public Connection getConnection() {

        if (conn == null) {
            throw new IllegalStateException("No hay conexión establecida.");
        }

        return conn;
    }

    /**
     * Devuelve si una transacción está actualmente en curso.
     *
     * @return true si {@link #startTransaction()} fue llamado y {@link #commit()} o
     *         {@link #rollback()} aún no han sido llamados.
     */
    public boolean isTransactionActive() {

        return transactionActive;
    }

    /**
     * Implementación del método {@link AutoCloseable#close()}.
     * Este es el método de limpieza que se llama al salir del bloque
     * <code>try-with-resources</code>.
     *
     * <p>
     * <b>Flujo de Limpieza:</b>
     * 1. Verifica si la transacción sigue activa (es decir, `commit()` no se
     * llamó).
     * 2. Si está activa, llama a {@link #rollback()} para deshacer los cambios
     * (esto previene "commits fantasma").
     * 3. Restablece `autoCommit` a `true` (devuelve la conexión a su estado
     * normal).
     * 4. Cierra la conexión física (`conn.close()`).
     * </p>
     */
    @Override
    public void close() {

        if (conn != null) {

            try {
                if (transactionActive) {

                    // ¡CRÍTICO! Si salimos del try-with-resources y la transacción
                    // sigue activa (por una excepción o por olvido), hacemos rollback.
                    rollback();
                }

            } catch (Exception e) {
                // Error al hacer rollback
                System.err.println("Error al intentar rollback en close(): " + e.getMessage());

            } finally {

                // Siempre debemos intentar restablecer autoCommit y cerrar la conexión
                try (conn) {
                    conn.setAutoCommit(true);

                } catch (Exception e) {

                    // Error al limpiar la conexión
                    System.err.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
    }
}
