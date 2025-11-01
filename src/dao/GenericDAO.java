package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Interfaz genérica para operaciones CRUD en la base de datos.
 * Proporciona métodos para guardar, buscar, actualizar y eliminar entidades.
 * Las implementaciones específicas deben manejar los detalles de la conexión y
 * las consultas SQL.
 * 
 * @param <T> tipo de entidad.
 * @author alpha team
 */
public interface GenericDAO<T> {

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
     * @param filter filtro de búsqueda.
     * @return iterable de entidades que coinciden con el filtro.
     * @throws Exception
     */
    Iterable<T> searchByFilter(String filter) throws Exception;

    /**
     * Mapea un `ResultSet` a un objeto de entidad.
     * 
     * @param rs ResultSet de la consulta.
     * @return objeto de entidad mapeado.
     * @throws Exception
     */
    T mapEntity(ResultSet rs) throws Exception;

    /**
     * Establece los parámetros de una entidad en un `PreparedStatement`.
     * 
     * @param stmt   PreparedStatement donde se establecerán los parámetros.
     * @param entity entidad cuyos datos se usarán.
     * @throws Exception
     */
    void setEntityParameters(PreparedStatement stmt, T entity) throws Exception;

    /**
     * Establece el ID generado automáticamente en la entidad después de una
     * inserción.
     * 
     * @param stmt   PreparedStatement usado para la inserción.
     * @param entity entidad donde se establecerá el ID generado.
     * @throws Exception
     */
    void setGeneratedId(PreparedStatement stmt, T entity) throws Exception;

}
