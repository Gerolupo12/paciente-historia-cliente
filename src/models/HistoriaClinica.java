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

    private String numeroHistoria;
    private GrupoSanguineo grupoSanguineo;
    private String antecedentes;
    private String medicacionActual;
    private String observaciones;
    private Profesional profesional;

    /**
     * Constructor completo para crear una historia clínica.
     *
     * @param id               Identificador único
     * @param numeroHistoria   Número único de la historia
     * @param grupoSanguineo   Grupo sanguíneo del paciente
     * @param antecedentes     Antecedentes médicos
     * @param medicacionActual Medicación actual
     * @param observaciones    Observaciones adicionales
     * @param profesional      Profesional asignado a la historia
     */
    public HistoriaClinica(int id, String numeroHistoria, GrupoSanguineo grupoSanguineo,
            String antecedentes, String medicacionActual, String observaciones,
            Profesional profesional) {
        super(id);
        this.numeroHistoria = numeroHistoria;
        this.grupoSanguineo = grupoSanguineo;
        this.antecedentes = antecedentes;
        this.medicacionActual = medicacionActual;
        this.observaciones = observaciones;
        this.profesional = profesional;
    }

    /**
     * Constructor sin id para crear una historia clínica.
     *
     * @param numeroHistoria   Número único de la historia
     * @param grupoSanguineo   Grupo sanguíneo del paciente
     * @param antecedentes     Antecedentes médicos
     * @param medicacionActual Medicación actual
     * @param observaciones    Observaciones adicionales
     * @param profesional      Profesional asignado a la historia
     */
    public HistoriaClinica(String numeroHistoria, GrupoSanguineo grupoSanguineo,
            String antecedentes, String medicacionActual, String observaciones,
            Profesional profesional) {
        super();
        this.numeroHistoria = numeroHistoria;
        this.grupoSanguineo = grupoSanguineo;
        this.antecedentes = antecedentes;
        this.medicacionActual = medicacionActual;
        this.observaciones = observaciones;
        this.profesional = profesional;
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

    public Profesional getProfesional() {
        return profesional;
    }

    public void setProfesional(Profesional profesional) {
        this.profesional = profesional;
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
        String profStr = (profesional != null)
                ? profesional.getApellido() + ", " + profesional.getNombre()
                : "Sin profesional asignado";
        return "HistoriaClinica{"
                + "id=" + getId()
                + ", numeroHistoria=" + numeroHistoria
                + ", grupoSanguineo=" + grupoSanguineo
                + ", profesional=" + profStr
                + '}';
    }

}
