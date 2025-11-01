package service;

/**
 * Interfaz genérica para servicios de negocio.
 * Define las operaciones CRUD básicas que las implementaciones concretas deben
 * ofrecer, delegando la persistencia al DAO correspondiente.
 *
 * @param <T> Tipo de entidad gestionada por el servicio.
 * @author alpha team
 */
public interface GenericService<T> {

    /**
     * Guarda una entidad en la base de datos.
     * 
     * @param entity entidad a guardar.
     * @throws Exception
     */
    void insert(T entity) throws Exception;

    /**
     * Actualiza una entidad en la base de datos.
     *
     * @param entity entidad a actualizar.
     * @throws Exception
     */
    void update(T entity) throws Exception;

    /**
     * Elimina una entidad de la base de datos por su ID.
     *
     * @param id ID de la entidad a eliminar.
     * @throws Exception
     */
    void delete(int id) throws Exception;

    /**
     * Busca una entidad por su ID.
     * 
     * @param id ID de la entidad.
     * @return entidad encontrada o null si no existe.
     * @throws Exception
     */
    T selectById(int id) throws Exception;

    /**
     * Busca todas las entidades.
     *
     * @return iterable de entidades.
     * @throws Exception
     */
    Iterable<T> selectAll() throws Exception;

    /**
     * Recupera una entidad eliminada lógicamente por su ID.
     *
     * @param id ID de la entidad a recuperar.
     * @throws Exception
     */
    void recover(int id) throws Exception;

    /**
     * Busca entidades que coincidan con un filtro en sus atributos.
     * 
     * @param filter cadena de texto para filtrar.
     * @return iterable de entidades que coinciden con el filtro.
     * @throws Exception
     */
    Iterable<T> searchByFilter(String filter) throws Exception;

    /**
     * Valida una entidad antes de operaciones CRUD.
     * 
     * @param entity entidad a validar.
     * @throws Exception
     */
    void validateEntity(T entity) throws Exception;

}
