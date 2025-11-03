package models;

/**
 * Entidad HistoriaClinica (representa la Entidad "B" en la relación 1-a-1).
 * <p>
 * Contiene la información médica relevante de un {@link Paciente}.
 * Hereda de {@link Base} para obtener los campos <code>id</code> y
 * <code>eliminado</code>.
 * </p>
 *
 * <h3>Relación con Paciente (Entidad "A"):</h3>
 * <ul>
 * <li>Es el "lado pasivo" de la relación <b>unidireccional 1-a-1</b>
 * (Paciente → HistoriaClinica).</li>
 * <li>Esta clase <b>no</b> tiene una referencia de vuelta al Paciente,
 * cumpliendo con la consigna.</li>
 * <li>La FK <code>historia_clinica_id</code> se almacena en la tabla
 * <code>Paciente</code>.</li>
 * </ul>
 *
 * <h3>Reglas de Negocio Clave:</h3>
 * <ul>
 * <li><b>RN-015 (Unicidad):</b> El <code>numeroHistoria</code> debe ser único
 * en
 * el sistema.</li>
 * <li><b>RN-016 (Validación):</b> <code>numeroHistoria</code> y
 * <code>grupoSanguineo</code> son obligatorios.</li>
 * <li><b>RN-017 (Formato):</b> <code>numeroHistoria</code> debe cumplir con un
 * formato específico (ej: "HC-XXXX").</li>
 * </ul>
 * <p>
 * <b>Nota:</b> Todas las validaciones de reglas de negocio (RN) son
 * responsabilidad de la capa de servicio
 * ({@link service.HistoriaClinicaService}), no de esta clase .
 * </p>
 *
 * @author alpha team
 * @see Base
 * @see Paciente
 * @see GrupoSanguineo
 * @see service.HistoriaClinicaService
 * @see dao.HistoriaClinicaDAO
 */
public class HistoriaClinica extends Base {

    // ============ ATRIBUTOS ============
    /**
     * Número de historia clínica (Clave de Negocio).
     * Requerido, debe ser único (RN-015) y tener un formato válido (RN-017).
     * La validación es responsabilidad de {@link service.HistoriaClinicaService}.
     * <p>
     * Se utiliza como clave de negocio para implementar {@link #equals(Object)}
     * y {@link #hashCode()}.
     * </p>
     */
    private String numeroHistoria;

    /**
     * Grupo sanguíneo del paciente (Enum).
     * Requerido (RN-016). La validación es responsabilidad de
     * {@link service.HistoriaClinicaService}.
     */
    private GrupoSanguineo grupoSanguineo;

    /**
     * Antecedentes médicos relevantes del paciente (opcional).
     */
    private String antecedentes;

    /**
     * Medicación actual que el paciente está tomando (opcional).
     */
    private String medicacionActual;

    /**
     * Observaciones generales del profesional (opcional).
     */
    private String observaciones;

    // ============ CONSTRUCTORES ============
    /**
     * Constructor completo.
     * Usado para reconstruir un objeto HistoriaClinica con todos sus datos,
     * (ej: por el DAO desde la base de datos).
     *
     * @param id               Identificador único (PK)
     * @param numeroHistoria   Número único de la historia (Clave de Negocio)
     * @param grupoSanguineo   Grupo sanguíneo del paciente (Enum)
     * @param antecedentes     Antecedentes médicos
     * @param medicacionActual Medicación actual
     * @param observaciones    Observaciones adicionales
     */
    public HistoriaClinica(int id, String numeroHistoria, GrupoSanguineo grupoSanguineo,
            String antecedentes, String medicacionActual, String observaciones) {

        super(id);
        this.numeroHistoria = numeroHistoria;
        this.grupoSanguineo = grupoSanguineo;
        this.antecedentes = antecedentes;
        this.medicacionActual = medicacionActual;
        this.observaciones = observaciones;
    }

    /**
     * Constructor para una nueva instancia (sin ID).
     * Usado antes de persistir una nueva historia clínica.
     *
     * @param numeroHistoria   Número único de la historia
     * @param grupoSanguineo   Grupo sanguíneo del paciente
     * @param antecedentes     Antecedentes médicos
     * @param medicacionActual Medicación actual
     * @param observaciones    Observaciones adicionales
     */
    public HistoriaClinica(String numeroHistoria, GrupoSanguineo grupoSanguineo,
            String antecedentes, String medicacionActual, String observaciones) {

        super(); // Llama a Base() (id=0, eliminado=false)
        this.numeroHistoria = numeroHistoria;
        this.grupoSanguineo = grupoSanguineo;
        this.antecedentes = antecedentes;
        this.medicacionActual = medicacionActual;
        this.observaciones = observaciones;
    }

    /**
     * Constructor simplificado (con ID).
     * Útil para reconstruir la HC desde el PacienteDAO
     * (que solo necesita ID y numeroHistoria para el 'toString').
     *
     * @param id             Identificador único (PK)
     * @param numeroHistoria Número único de la historia
     */
    public HistoriaClinica(int id, String numeroHistoria) {

        super(id);
        this.numeroHistoria = numeroHistoria;
    }

    /**
     * Constructor simplificado (sin ID).
     *
     * @param numeroHistoria Número único de la historia
     */
    public HistoriaClinica(String numeroHistoria) {

        super(); // Llama a Base() (id=0, eliminado=false)
        this.numeroHistoria = numeroHistoria;
    }

    /**
     * Constructor por defecto.
     * Necesario para algunas librerías de mapeo o frameworks.
     */
    public HistoriaClinica() {
        super(); // Llama al constructor por defecto de Base
    }

    // ============ GETTERS Y SETTERS ============
    // Los setters son simples y no contienen lógica de negocio.
    // La validación es responsabilidad de la Capa de Servicio.

    public String getNumeroHistoria() {
        return numeroHistoria;
    }

    /**
     * Establece el número de historia clínica.
     * La validación (ej: no nulo/vacío, formato RN-017, unicidad RN-015)
     * es manejada por {@link service.HistoriaClinicaService} antes de persistir.
     *
     * @param numeroHistoria El número de historia.
     */
    public void setNumeroHistoria(String numeroHistoria) {
        this.numeroHistoria = numeroHistoria;
    }

    public GrupoSanguineo getGrupoSanguineo() {
        return grupoSanguineo;
    }

    /**
     * Establece el grupo sanguíneo.
     * La validación (ej: no nulo, RN-016) es manejada por
     * {@link service.HistoriaClinicaService} antes de persistir.
     *
     * @param grupoSanguineo El {@link GrupoSanguineo} del paciente.
     */
    public void setGrupoSanguineo(GrupoSanguineo grupoSanguineo) {
        this.grupoSanguineo = grupoSanguineo;
    }

    public String getAntecedentes() {
        return antecedentes;
    }

    /**
     * Establece los antecedentes médicos (opcional).
     *
     * @param antecedentes Texto de antecedentes.
     */
    public void setAntecedentes(String antecedentes) {
        this.antecedentes = antecedentes;
    }

    public String getMedicacionActual() {
        return medicacionActual;
    }

    /**
     * Establece la medicación actual (opcional).
     *
     * @param medicacionActual Texto de medicación.
     */
    public void setMedicacionActual(String medicacionActual) {
        this.medicacionActual = medicacionActual;
    }

    public String getObservaciones() {
        return observaciones;
    }

    /**
     * Establece las observaciones generales (opcional).
     *
     * @param observaciones Texto de observaciones.
     */
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    // ============ OTROS MÉTODOS ============
    /**
     * Devuelve una representación en String del objeto HistoriaClinica.
     * Se enfoca en los campos clave para identificación.
     *
     * @return String representando la historia clínica.
     */
    @Override
    public String toString() {

        return "HistoriaClinica{"
                + "id=" + getId()
                + ", eliminado=" + isEliminado() // Heredado de Base
                + ", numeroHistoria=" + getNumeroHistoria()
                + ", grupoSanguineo=" + getGrupoSanguineo()
                + '}';
    }

    /**
     * Compara si dos objetos HistoriaClinica son iguales basándose en su
     * <code>numeroHistoria</code>.
     * <p>
     * Esta es la implementación de la "igualdad de negocio" (Regla RN-015).
     * Dos historias clínicas son consideradas la misma si tienen el mismo
     * <code>numeroHistoria</code>, ya que es la clave natural única de la
     * historia.
     * </p>
     *
     * @param o El objeto a comparar.
     * @return <code>true</code> si los <code>numeroHistoria</code> son iguales,
     *         <code>false</code> en caso contrario.
     */
    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        HistoriaClinica that = (HistoriaClinica) o;

        // La comparación se basa únicamente en la clave de negocio (numeroHistoria)
        return numeroHistoria.equals(that.numeroHistoria);
    }

    /**
     * Genera un código hash basado en el <code>numeroHistoria</code>.
     * <p>
     * Es consistente con {@link #equals(Object)}: si dos historias son
     * <code>equals()</code> (mismo <code>numeroHistoria</code>), tendrán el mismo
     * <code>hashCode()</code>.
     * </p>
     * <p>
     * Fundamental para el correcto funcionamiento en colecciones como
     * <code>HashMap</code> o <code>HashSet</code>.
     * </p>
     *
     * @return El código hash de la historia (basado en el
     *         <code>numeroHistoria</code>).
     */
    @Override
    public int hashCode() {
        // El hash code se basa únicamente en los campos usados en equals()
        return numeroHistoria.hashCode();
    }

}
