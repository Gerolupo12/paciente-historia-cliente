package models;

/**
 * Clase abstracta base que proporciona propiedades comunes para todas las
 * entidades del sistema. Implementa la lógica de identificación única y baja
 * lógica para todas las entidades derivadas.
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
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEliminado() {
        return (eliminado ? "Si" : "No");
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
    }

}
