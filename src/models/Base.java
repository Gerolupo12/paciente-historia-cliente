package models;

/**
 * Clase abstracta base que proporciona propiedades comunes para todas las
 * entidades del sistema.
 * Implementa la lógica de identificación única y baja lógica para todas las
 * entidades derivadas.
 * 
 * @author alpha team
 */
public abstract class Base {

    private int id;
    private Boolean eliminado;

    /**
     * Constructor parametrizado que inicializa una entidad con ID específico.
     * Por defecto, marca la entidad como no eliminada.
     * 
     * @param id Identificador único de la entidad
     */
    public Base(int id) {
        this.id = id;
        this.eliminado = false; // Por defecto, no está eliminado
    }

    /**
     * Constructor por defecto. Inicializa con valores por defecto.
     */
    public Base() {
    }

    // ============ GETTERS Y SETTERS ============

    /**
     * Obtiene el identificador único de la entidad.
     * 
     * @return ID único de la entidad
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador único de la entidad.
     * 
     * @param id Nuevo ID único para la entidad
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Verifica si la entidad ha sido marcada como eliminada (baja lógica).
     * 
     * @return true si la entidad está eliminada, false en caso contrario
     */
    public Boolean getEliminado() {
        return eliminado;
    }

    /**
     * Establece el estado de eliminación de la entidad (baja lógica).
     * 
     * @param eliminado true para marcar como eliminada, false para reactivar
     */
    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
    }

}
