package service;

import java.util.List;

/**
 * Interfaz genérica para la capa de Servicio (Lógica de Negocio).
 * <p>
 * Define el <b>contrato</b> estándar para todas las operaciones de negocio
 * relacionadas con una entidad <code>&lt;T&gt;</code>.
 * </p>
 *
 * <h3>Responsabilidades de la Capa de Servicio:</h3>
 * <ul>
 * <li><b>Validación de Reglas de Negocio (RN):</b> Es la <b>responsabilidad
 * principal</b>. Debe llamar a {@link #validateEntity(T)} antes de cualquier
 * operación de escritura (insert, update).</li>
 * <li><b>Orquestación de Transacciones:</b> Debe gestionar la atomicidad (todo
 * o nada) de las operaciones. Por ejemplo, al crear un <code>Paciente</code>,
 * la implementación de <code>insert()</code> debe asegurarse de que tanto el
 * <code>Paciente</code> como su <code>HistoriaClinica</code> se inserten
 * correctamente, usando <code>commit()</code> o <code>rollback()</code> (ej:
 * con {@link config.TransactionManager}).</li>
 * <li><b>Abstracción del DAO:</b> Actúa como intermediario entre la capa de UI
 * ({@link main.MenuHandler}) y la capa de Datos ({@link dao.GenericDAO}),
 * ocultando los detalles de la persistencia.</li>
 * </ul>
 *
 * @param <T> El tipo de la entidad del modelo (ej: Paciente, HistoriaClinica)
 *            que este servicio gestiona.
 * @author alpha team
 * @see dao.GenericDAO
 * @see config.TransactionManager
 */
public interface GenericService<T> {

    // ============ MÉTODOS CRUD (Escritura) ============
    /**
     * Valida e inserta una nueva entidad en la base de datos.
     * <p>
     * <b>Implementación (Debe):</b>
     * 1. Llamar a {@link #validateEntity(T)} para asegurar reglas de negocio
     * (ej: campos obligatorios, unicidad de DNI).
     * 2. Orquestar la transacción (ej: insertar <code>HistoriaClinica</code>
     * antes que <code>Paciente</code>) llamando a los métodos <code>...Tx()</code>
     * del DAO.
     * 3. Realizar <code>commit()</code> o <code>rollback()</code>.
     * </p>
     *
     * @param entity La entidad a guardar (con <code>id=0</code>).
     * @throws Exception Si la validación (<code>IllegalArgumentException</code>)
     *                   o la persistencia (<code>SQLException</code>) fallan.
     */
    void insert(T entity) throws Exception;

    /**
     * Valida y actualiza una entidad existente en la base de datos.
     * <p>
     * <b>Implementación (Debe):</b>
     * 1. Asegurarse que <code>entity.getId() > 0</code>.
     * 2. Llamar a {@link #validateEntity(T)} para asegurar reglas de negocio
     * (ej: unicidad de DNI excluyendo el ID actual).
     * 3. Orquestar la transacción llamando a los métodos <code>...Tx()</code>
     * del DAO.
     * </p>
     *
     * @param entity La entidad con los datos actualizados (debe tener
     *               <code>id > 0</code>).
     * @throws Exception Si la validación (<code>IllegalArgumentException</code>),
     *                   el ID es inválido, o la persistencia
     *                   (<code>SQLException</code>) falla.
     */
    void update(T entity) throws Exception;

    /**
     * Realiza una "Baja Lógica" (Soft Delete) de una entidad por su ID.
     * <p>
     * <b>Implementación (Debe):</b>
     * 1. Asegurarse que <code>id > 0</code>.
     * 2. Orquestar la lógica de eliminación (ej: eliminar <code>Paciente</code>
     * Y su <code>HistoriaClinica</code> asociada).
     * 3. Llamar a <code>dao.delete(id)</code> (o <code>deleteTx</code>).
     * </p>
     *
     * @param id El ID de la entidad a marcar como <code>eliminado = TRUE</code>.
     * @throws Exception Si el ID es inválido o la persistencia
     *                   (<code>SQLException</code>)
     *                   falla.
     */
    void delete(int id) throws Exception;

    /**
     * Recupera una entidad que fue eliminada lógicamente.
     * <p>
     * <b>Implementación (Debe):</b>
     * 1. Asegurarse que <code>id > 0</code>.
     * 2. Orquestar la lógica de recuperación (ej: recuperar <code>Paciente</code>
     * Y su <code>HistoriaClinica</code> asociada).
     * 3. Llamar a <code>dao.recover(id)</code>.
     * </p>
     *
     * @param id El ID de la entidad a recuperar (setear
     *           <code>eliminado = FALSE</code>).
     * @throws Exception Si el ID es inválido o la persistencia
     *                   (<code>SQLException</code>)
     *                   falla.
     */
    void recover(int id) throws Exception;

    // ============ MÉTODOS SELECT (Lectura) ============
    /**
     * Busca y recupera una entidad por su ID y su estado de eliminación.
     * <p>
     * <b>Implementación (Debe):</b>
     * 1. Asegurarse que <code>id > 0</code>.
     * 2. Llamar al método <code>dao.selectByIdWithStatus(id, deleted)</code>.
     * </p>
     *
     * @param id      El ID de la entidad a buscar.
     * @param deleted <code>false</code> para buscar entidades <b>activas</b>
     *                (uso normal).
     *                <code>true</code> para buscar entidades <b>eliminadas</b>
     *                (usado por la lógica de <code>recover</code>).
     * @return La entidad encontrada, o <b>null</b> si no existe en ese estado.
     * @throws Exception Si el ID es inválido o la consulta
     *                   (<code>SQLException</code>)
     *                   falla.
     */
    T selectById(int id, boolean deleted) throws Exception;

    /**
     * Busca y recupera una lista de todas las entidades según su estado de
     * eliminación.
     * <p>
     * <b>Implementación (Debe):</b>
     * 1. Llamar a <code>dao.selectAllWithStatus(deleted)</code>.
     * </p>
     *
     * @param deleted <code>false</code> para obtener todas las entidades
     *                <b>activas</b> (para el listado principal).
     *                <code>true</code> para obtener todas las entidades
     *                <b>eliminadas</b> (para el submenú de recuperación).
     * @return Una {@link List} de entidades (puede estar vacía).
     * @throws Exception Si la consulta (<code>SQLException</code>) falla.
     */
    List<T> selectAll(boolean deleted) throws Exception;

    /**
     * Busca entidades <b>activas</b> que coincidan con un filtro de texto.
     * <p>
     * <b>Implementación (Debe):</b>
     * 1. Validar que el filtro no sea nulo o vacío.
     * 2. Llamar a <code>dao.searchByFilter(filter)</code>.
     * </p>
     *
     * @param filter El texto de búsqueda (ej: "Juan").
     * @return Una {@link List} de entidades que coinciden (puede estar vacía).
     * @throws Exception Si el filtro es inválido
     *                   (<code>IllegalArgumentException</code>)
     *                   o la consulta (<code>SQLException</code>) falla.
     */
    List<T> searchByFilter(String filter) throws Exception;

    // ============ MÉTODOS DE VALIDACIÓN ============
    /**
     * Valida que una entidad cumpla con todas las Reglas de Negocio (RN)
     * antes de ser persistida.
     * <p>
     * Este es el "guardián" de la capa de servicio.
     * </p>
     *
     * <h3>Validaciones Típicas (Debe implementar):</h3>
     * <ul>
     * <li>Que la entidad no sea <code>null</code>.</li>
     * <li>Campos obligatorios (ej: DNI no puede ser <code>null</code> o
     * vacío).</li>
     * <li>Formatos (ej: <code>nroHistoria</code> debe ser "HC-XXXX").</li>
     * <li>Unicidad (ej: DNI debe ser único, llamando a un método
     * <code>validateDniUnique</code>).</li>
     * </ul>
     *
     * @param entity La entidad a validar.
     * @throws IllegalArgumentException Si alguna regla de negocio es violada.
     * @throws Exception                Si la validación requiere una consulta a la
     *                                  BD
     *                                  (ej: unicidad) y esta falla.
     */
    void validateEntity(T entity) throws Exception;

}
