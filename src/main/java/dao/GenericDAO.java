package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz Genérica para el patrón Data Access Object (DAO).
 * <p>
 * Define el <b>contrato</b> estándar para todas las operaciones de persistencia
 * (CRUD) en la base de datos. El uso de una interfaz genérica permite la
 * abstracción de la capa de acceso a datos y promueve la reutilización de
 * código (Principio DRY).
 * </p>
 *
 * <h3>Responsabilidades del Contrato:</h3>
 * <ul>
 * <li><b>CRUD Básico:</b> Definir métodos para Insertar, Actualizar, Eliminar
 * (lógico) y Seleccionar (por ID, todos).</li>
 * <li><b>Soporte Transaccional:</b> Proveer métodos (ej: <code>insertTx</code>)
 * que acepten una {@link Connection} externa. Esto es <b>CRÍTICO</b> para que
 * la capa de Servicio ({@link service.GenericService}) pueda orquestar
 * operaciones atómicas que involucren múltiples DAOs (ej: insertar Paciente y
 * su HistoriaClinica) bajo una misma transacción.</li>
 * <li><b>Baja Lógica:</b> Incluir métodos para <code>delete</code> (marcar como
 * eliminado) y <code>recover</code> (desmarcar).</li>
 * <li><b>Mapeo:</b> Definir métodos estándar para mapear
 * (<code>mapEntity</code>) y des-mapear (<code>setEntityParameters</code>)
 * objetos y consultas.</li>
 * </ul>
 *
 * @param <T> El tipo de la entidad del modelo (ej: Paciente, HistoriaClinica)
 *            que esta interfaz manejará.
 * @author alpha team
 * @see models.Base
 */
public interface GenericDAO<T> {

    // ============ MÉTODOS CRUD (Escritura) ============
    /**
     * Inserta una nueva entidad en la base de datos.
     * <p>
     * <b>IMPORTANTE:</b> Este método gestiona su propia conexión y <b>NO</b>
     * participa en transacciones externas.
     * Útil para inserciones simples que no requieren coordinación.
     * </p>
     *
     * @param entity La entidad a guardar (ej: un Paciente nuevo).
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    void insert(T entity) throws SQLException;

    /**
     * Inserta una nueva entidad usando una <b>transacción existente</b>.
     * <p>
     * Este es el método <b>CRÍTICO</b> para la capa de Servicio.
     * <b>NO</b> abre ni cierra la conexión; simplemente la utiliza para ejecutar el
     * PreparedStatement.
     * </p>
     *
     * @param entity La entidad a guardar.
     * @param conn   La {@link Connection} transaccional (con autoCommit=false)
     *               proveída por la capa de Servicio (ej: desde un
     *               TransactionManager).
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    void insertTx(T entity, Connection conn) throws SQLException;

    /**
     * Actualiza una entidad existente en la base de datos.
     * <p>
     * <b>IMPORTANTE:</b> Este método gestiona su propia conexión y <b>NO</b>
     * participa en transacciones externas.
     * </p>
     *
     * @param entity La entidad con los datos actualizados.
     * @throws SQLException Si la entidad no existe (ej: rowsAffected = 0) o
     *                      si ocurre un error de BD.
     */
    void update(T entity) throws SQLException;

    /**
     * Actualiza una entidad existente usando una <b>transacción existente</b>.
     * <p>
     * Permite que la actualización de esta entidad sea parte de una operación
     * atómica más grande en la capa de Servicio.
     * </p>
     *
     * @param entity La entidad con los datos actualizados.
     * @param conn   La {@link Connection} transaccional (con autoCommit=false).
     * @throws SQLException Si la entidad no existe (rowsAffected = 0) o
     *                      si ocurre un error de BD.
     */
    void updateTx(T entity, Connection conn) throws SQLException;

    /**
     * Realiza una "Baja Lógica" (Soft Delete) de una entidad por su ID.
     * <p>
     * <b>No</b> borra el registro físicamente (<code>DELETE</code>), sino que
     * ejecuta un <code>UPDATE</code> para setear <code>eliminado = TRUE</code>.
     * </p>
     * <p>
     * <b>IMPORTANTE:</b> Este método gestiona su propia conexión y <b>NO</b>
     * participa en transacciones externas.
     * </p>
     *
     * @param id El ID de la entidad a marcar como eliminada.
     * @throws SQLException Si la entidad no existe (rowsAffected = 0) o
     *                      si ocurre un error de BD.
     */
    void delete(int id) throws SQLException;

    /**
     * Realiza una "Baja Lógica" (Soft Delete) usando una <b>transacción
     * existente</b>.
     *
     * @param id   El ID de la entidad a marcar como eliminada.
     * @param conn La {@link Connection} transaccional (con autoCommit=false).
     * @throws SQLException Si la entidad no existe (rowsAffected = 0) o
     *                      si ocurre un error de BD.
     */
    void deleteTx(int id, Connection conn) throws SQLException;

    /**
     * Recupera una entidad que fue eliminada lógicamente (Soft Delete).
     * <p>
     * Ejecuta un <code>UPDATE</code> para setear <code>eliminado = FALSE</code>.
     * </p>
     *
     * @param id El ID de la entidad a recuperar.
     * @throws SQLException Si la entidad no existe (rowsAffected = 0) o
     *                      si ocurre un error de BD.
     */
    void recover(int id) throws SQLException;

    // ============ MÉTODOS SELECT (Lectura) ============
    /**
     * Busca y recupera una entidad por su ID y su estado:
     * <ul>
     * <li>Activos: (<code>eliminado = false</code>)</li>
     * <li>Eliminados (<code>eliminado = true</code>)</li>
     * </ul>
     * 
     * @param id      El ID de la entidad a buscar.
     * @param deleted <code>false</code> para buscar activos.
     *                <code>true</code> para buscar eliminados.
     * @return La entidad encontrada, o <b>null</b> si no existe o fue eliminada
     *         lógicamente.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    T selectByIdWithStatus(int id, boolean deleted) throws SQLException;

    /**
     * Busca y recupera una lista de todas las entidades según su estado de
     * eliminación.
     *
     * @param eliminados <code>false</code> para obtener todas las entidades
     *                   <b>activas</b> (lo más común).
     *                   <code>true</code> para obtener todas las entidades
     *                   <b>eliminadas</b> (usado en el submenú de recuperación).
     * @return Una {@link List} de entidades (puede estar vacía).
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    List<T> selectAllWithStatus(boolean deleted) throws SQLException;

    /**
     * Busca entidades <b>activas</b> que coincidan con un filtro de texto.
     * <p>
     * La implementación (ej: PacienteDAO) definirá en qué columnas se aplica el
     * filtro (ej: nombre, apellido, DNI, etc.) y cómo se aplica (ej:
     * <code>LIKE '%filtro%'</code>).
     * </p>
     *
     * @param filter El texto de búsqueda (ej: "Juan").
     * @return Una {@link List} de entidades que coinciden (puede estar vacía).
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    List<T> searchByFilter(String filter) throws SQLException;

    // ============ MÉTODOS HELPER (Mapeo y Parámetros) ============
    /**
     * Mapea la fila actual de un {@link ResultSet} a un objeto de la entidad (T).
     * <p>
     * Esta es una responsabilidad clave del DAO. Debe leer las columnas del
     * <code>ResultSet</code> (ej: "nombre", "apellido") y usarlas para construir y
     * devolver un nuevo objeto (ej: <code>new Paciente(...)</code>).
     * </p>
     *
     * @param rs El ResultSet posicionado en la fila a mapear.
     * @return El objeto (T) mapeado con los datos de la fila.
     * @throws SQLException Si una columna esperada no se encuentra en el
     *                      ResultSet.
     */
    T mapEntity(ResultSet rs) throws SQLException;

    /**
     * Establece los parámetros de un {@link PreparedStatement} a partir de un
     * objeto de entidad.
     * <p>
     * Este es el reverso de <code>mapEntity</code>. Se usa en <code>insert()</code>
     * y <code>update()</code> para "des-mapear" el objeto Java a parámetros SQL
     * (<code>?</code>).
     * </p>
     * <p>
     * Ejemplo:
     * <code>stmt.setString(1, entity.getNombre());</code>
     * <code>stmt.setString(2, entity.getApellido());</code>
     * </p>
     *
     * @param stmt   El PreparedStatement (ej: <code>INSERT INTO...</code>)
     *               cuyos parámetros (<code>?</code>) se van a establecer.
     * @param entity La entidad de donde se obtienen los datos.
     * @throws SQLException Si ocurre un error al establecer los parámetros.
     */
    void setEntityParameters(PreparedStatement stmt, T entity) throws SQLException;

    /**
     * Obtiene el ID autogenerado (ej: AUTO_INCREMENT) después de un
     * <code>INSERT</code> y lo establece en el objeto entidad.
     * <p>
     * Esto es <b>fundamental</b> para que el objeto en memoria (Java) se sincronice
     * con su estado en la base de datos (que ahora tiene un ID).
     * </p>
     *
     * @param stmt   El PreparedStatement que ejecutó el <code>INSERT</code>
     *               (debe haber sido creado con
     *               <code>RETURN_GENERATED_KEYS</code>).
     * @param entity La entidad (con <code>id=0</code>) que recibirá el
     *               nuevo ID.
     * @throws SQLException Si no se pueden obtener las claves generadas.
     */
    void setGeneratedId(PreparedStatement stmt, T entity) throws SQLException;

}
