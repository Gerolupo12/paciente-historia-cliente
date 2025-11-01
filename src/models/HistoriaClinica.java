package models;

/**
 * Representa la historia clínica de un paciente en el sistema médico.
 * Contiene información médica relevante como antecedentes, medicación y
 * observaciones.
 * Está relacionada en forma unidireccional 1→1 con Paciente.
 * 
 * @author alpha team
 * @see Paciente
 * @see GrupoSanguineo
 * @see Base
 */
public class HistoriaClinica extends Base {

    // ============ ATRIBUTOS ============
    private String numeroHistoria;
    private GrupoSanguineo grupoSanguineo;
    private String antecedentes;
    private String medicacionActual;
    private String observaciones;

    // ============ CONSTRUCTORES ============
    /**
     * Constructor completo para crear una historia clínica.
     *
     * @param id               Identificador único
     * @param numeroHistoria   Número único de la historia
     * @param grupoSanguineo   Grupo sanguíneo del paciente
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
     * Constructor sin id para crear una historia clínica.
     *
     * @param numeroHistoria   Número único de la historia
     * @param grupoSanguineo   Grupo sanguíneo del paciente
     * @param antecedentes     Antecedentes médicos
     * @param medicacionActual Medicación actual
     * @param observaciones    Observaciones adicionales
     */
    public HistoriaClinica(String numeroHistoria, GrupoSanguineo grupoSanguineo,
            String antecedentes, String medicacionActual, String observaciones) {
        super();
        this.numeroHistoria = numeroHistoria;
        this.grupoSanguineo = grupoSanguineo;
        this.antecedentes = antecedentes;
        this.medicacionActual = medicacionActual;
        this.observaciones = observaciones;
    }

    /**
     * Constructor simplificado.
     * 
     * @param id             Identificador único de la historia clínica
     * @param numeroHistoria Número único de identificación de la historia
     */
    public HistoriaClinica(int id, String numeroHistoria) {
        super(id);
        this.numeroHistoria = numeroHistoria;
    }

    /**
     * Constructor simplificado sin id.
     * 
     * @param numeroHistoria Número único de identificación de la historia
     */
    public HistoriaClinica(String numeroHistoria) {
        super();
        this.numeroHistoria = numeroHistoria;
    }

    /**
     * Constructor por defecto.
     */
    public HistoriaClinica() {
        super(); // Llama al constructor por defecto de Base
    }

    // ============ GETTERS Y SETTERS ============
    public String getNumeroHistoria() {
        return numeroHistoria;
    }

    public void setNumeroHistoria(String numeroHistoria) {
        if (numeroHistoria == null || numeroHistoria.isBlank()
                || !validarNumeroHistoria(numeroHistoria)) {
            throw new IllegalArgumentException("El número de historia no es válido.");
        }
        this.numeroHistoria = numeroHistoria;
    }

    public GrupoSanguineo getGrupoSanguineo() {
        return grupoSanguineo;
    }

    public void setGrupoSanguineo(GrupoSanguineo grupoSanguineo) {
        this.grupoSanguineo = grupoSanguineo;
    }

    public String getAntecedentes() {
        return antecedentes;
    }

    public void setAntecedentes(String antecedentes) {
        this.antecedentes = antecedentes;
    }

    public String getMedicacionActual() {
        return medicacionActual;
    }

    public void setMedicacionActual(String medicacionActual) {
        this.medicacionActual = medicacionActual;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    // ============ OTROS MÉTODOS ============
    /**
     * Valida que el número de la historia cumpla con la expresión regular.
     * 
     * @param numeroHistoria
     * @return boolean
     */
    private boolean validarNumeroHistoria(String numeroHistoria) {
        String regex = "^HC-[0-9]{4,17}$";
        return (numeroHistoria.matches(regex));
    }

    /**
     * Representación en String del objeto HistoriaClinica.
     *
     * @return String representando la historia clínica.
     */
    @Override
    public String toString() {
        return "HistoriaClinica{"
                + "id=" + getId()
                + ", numeroHistoria=" + numeroHistoria
                + ", grupoSanguineo=" + grupoSanguineo
                + '}';
    }

    /**
     * Compara si dos objetos HistoriaClinica son iguales basándose en su número de
     * historia.
     * Dos historias clínicas son iguales si tienen el mismo numeroHistoria.
     * 
     * @param o Objeto a comparar
     * @return boolean indicando si son iguales
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HistoriaClinica that = (HistoriaClinica) o;
        return numeroHistoria.equals(that.numeroHistoria);
    }

    /**
     * Genera un código hash basado en el numeroHistoria de la historia clínica.
     * Este método es fundamental para el correcto funcionamiento de colecciones
     * que utilizan hashing, como HashMap o HashSet.
     * 
     * @return int código hash de la historia clínica
     */
    @Override
    public int hashCode() {
        return numeroHistoria.hashCode();
    }

}
