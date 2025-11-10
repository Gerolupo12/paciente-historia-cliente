package models;

/**
 * Clase base abstracta para todas las entidades del sistema (Paciente,
 * HistoriaClinica).
 * <p>
 * Implementa las propiedades y comportamientos comunes a todas las entidades
 * persistentes, incluyendo la identificación única (ID) y la estrategia de
 * <strong>baja lógica (Soft Delete)</strong>.
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li>Proporcionar un campo <b><code>id</code></b> para la clave primaria
 * (PK).</li>
 * <li>Proporcionar un campo <b><code>eliminado</code></b> para implementar la
 * baja lógica, permitiendo "eliminar" registros sin borrarlos físicamente de la
 * base de datos.</li>
 * </ul>
 *
 * <h3>Patrón de Diseño:</h3>
 * <p>
 * Actúa como la raíz de la <b>herencia</b> en el modelo de dominio.
 * Clases como `Paciente` y `HistoriaClinica` extienden `Base` para heredar
 * estos campos comunes.
 * </p>
 *
 * @author alpha team
 * @see Paciente
 * @see HistoriaClinica
 */
public abstract class Base {

    // ============ ATRIBUTOS ============
    /**
     * Identificador único (PK) de la entidad.
     * <p>
     * Se espera que este valor sea gestionado por la base de datos (ej:
     * AUTO_INCREMENT).
     * Un valor de <code>0</code> indica que la entidad es nueva y aún no ha sido
     * persistida.
     * </p>
     */
    private int id;

    /**
     * Flag para la baja lógica (Soft Delete).
     * <ul>
     * <li><code>false</code> (default): La entidad está activa y es visible en
     * consultas estándar.</li>
     * <li><code>true</code>: La entidad está "eliminada" y debe ser excluida de las
     * consultas y operaciones habituales.</li>
     * </ul>
     * <p>
     * Se inicializa en <code>false</code> por defecto.
     * </p>
     */
    private boolean eliminado = false;

    // ============ CONSTRUCTORES ============
    /**
     * Constructor para reconstruir una entidad existente (ej: desde la BD).
     * <p>
     * Típicamente usado por la capa DAO al mapear un <code>ResultSet</code>
     * a un objeto del modelo.
     * </p>
     *
     * @param id El identificador único (PK) de la entidad.
     */
    public Base(int id) {

        this.id = id;
        this.eliminado = false; // Por defecto, no está eliminado
    }

    /**
     * Constructor por defecto para una entidad nueva.
     * <p>
     * El <code>id</code> se inicializa en <code>0</code> (valor por defecto de int)
     * y <code>eliminado</code> en <code>false</code> (valor por defecto del campo).
     * </p>
     */
    public Base() {
        // Valores por defecto (id=0, eliminado=false)
    }

    // ============ GETTERS Y SETTERS ============
    /**
     * Obtiene el identificador único (ID) de la entidad.
     *
     * @return El ID de la entidad (<code>0</code> si es nueva).
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador único (ID) de la entidad.
     * <p>
     * Este método es llamado típicamente por la capa DAO después de que un
     * <code>INSERT</code> en la base de datos ha generado un nuevo ID (usando
     * <code>RETURN_GENERATED_KEYS</code>).
     * </p>
     *
     * @param id El nuevo ID de la entidad, asignado por la base de datos.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Verifica si la entidad está marcada como eliminada lógicamente.
     * <p>
     * (Convención de Java para getters booleanos).
     * </p>
     *
     * @return <code>true</code> si la entidad está marcada como eliminada,
     *         <code>false</code> si está activa.
     */
    public boolean isEliminado() {
        return eliminado;
    }

    /**
     * Establece el estado de eliminación lógica (soft delete) de la entidad.
     *
     * @param eliminado <code>true</code> para marcar como eliminada,
     *                  <code>false</code> para marcar como activa (o "recuperar").
     */
    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

}
