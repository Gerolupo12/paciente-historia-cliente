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
    private String antecedentes;
    private String medicacionActual;
    private String observaciones;
    private GrupoSanguineo grupoSanguineo;

    /**
     * Constructor completo para crear una historia clínica con todos los datos.
     * 
     * @param id               Identificador único de la historia clínica
     * @param numeroHistoria   Número único de identificación de la historia
     * @param grupoSanguineo   Grupo sanguíneo del paciente
     * @param antecedentes     Antecedentes médicos del paciente
     * @param medicacionActual Medicación que toma actualmente el paciente
     * @param observaciones    Observaciones médicas adicionales
     */
    public HistoriaClinica(int id, String numeroHistoria,
            GrupoSanguineo grupoSanguineo, String antecedentes,
            String medicacionActual, String observaciones) {
        super(id);
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
     * Constructor por defecto.
     */
    public HistoriaClinica() {
        super(); // Llama al constructor por defecto de Base
    }

    // ============ GETTERS Y SETTERS ============

    /**
     * Obtiene el número único de identificación de la historia clínica.
     * 
     * @return Número de historia clínica
     */
    public String getNumeroHistoria() {
        return numeroHistoria;
    }

    /**
     * Establece el número único de identificación de la historia clínica.
     * 
     * @param numeroHistoria Nuevo número de historia clínica
     */
    public void setNumeroHistoria(String numeroHistoria) {
        this.numeroHistoria = numeroHistoria;
    }

    /**
     * Obtiene el grupo sanguíneo del paciente.
     * 
     * @return Grupo sanguíneo del paciente
     */
    public GrupoSanguineo getGrupoSanguineo() {
        return grupoSanguineo;
    }

    /**
     * Establece el grupo sanguíneo del paciente.
     * 
     * @param grupoSanguineo Nuevo grupo sanguíneo
     */
    public void setGrupoSanguineo(GrupoSanguineo grupoSanguineo) {
        if (grupoSanguineo != null) {
            this.grupoSanguineo = grupoSanguineo;
        }
    }

    /**
     * Obtiene los antecedentes médicos del paciente.
     * 
     * @return Antecedentes médicos (texto largo)
     */
    public String getAntecedentes() {
        return antecedentes;
    }

    /**
     * Establece los antecedentes médicos del paciente.
     * 
     * @param antecedentes Nuevos antecedentes médicos
     */
    public void setAntecedentes(String antecedentes) {
        this.antecedentes = antecedentes;
    }

    /**
     * Obtiene la medicación actual que toma el paciente.
     * 
     * @return Medicación actual del paciente (texto largo)
     */
    public String getMedicacionActual() {
        return medicacionActual;
    }

    /**
     * Establece la medicación actual que toma el paciente.
     * 
     * @param medicacionActual Nueva medicación actual
     */
    public void setMedicacionActual(String medicacionActual) {
        this.medicacionActual = medicacionActual;
    }

    /**
     * Obtiene las observaciones médicas adicionales.
     * 
     * @return Observaciones médicas (texto largo)
     */
    public String getObservaciones() {
        return observaciones;
    }

    /**
     * Establece las observaciones médicas adicionales.
     * 
     * @param observaciones Nuevas observaciones médicas
     */
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    /**
     * Representación en String del objeto HistoriaClinica.
     * Muestra un resumen de la información médica.
     * 
     * @return String representando la historia clínica
     */
    @Override
    public String toString() {
        return "HistoriaClinica{"
                + "id=" + getId()
                + ", numeroHistoria=" + numeroHistoria
                + ", grupoSanguineo=" + grupoSanguineo
                + ", antecedentes=" + antecedentes
                + ", medicacionActual=" + medicacionActual
                + ", observaciones=" + observaciones
                + ", eliminado=" + getEliminado() + '}';
    }

}
