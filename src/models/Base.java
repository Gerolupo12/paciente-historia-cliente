package models;

/**
 * Clase abstracta base que proporciona propiedades comunes para todas las
 * entidades del sistema. Implementa la lógica de identificación única y baja
 * lógica para todas las entidades derivadas.
 *
 * @author alpha team
 */
public abstract class Base {

    // ============ ATRIBUTOS ============
    private int id;
    private Boolean eliminado;

    // ============ CONSTRUCTORES ============
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
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor a cero.");
        }
        this.id = id;
    }

    public boolean getEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public String isEliminado() {
        return (eliminado ? "Si" : "No");
    }

}
