package models;

public class HistoriaClinica extends Base {

    private String numeroHistoria;
    private String antecedentes;
    private String medicacionActual;
    private String observaciones;
    private GrupoSanguineo grupoSanguineo;

    public HistoriaClinica(int id, String numeroHistoria, String antecedentes, 
            String medicacionActual, String observaciones) {
        super(id);
        this.numeroHistoria = numeroHistoria;
        this.antecedentes = antecedentes;
        this.medicacionActual = medicacionActual;
        this.observaciones = observaciones;
    }
    
    public HistoriaClinica() {
    }

    public String getNumeroHistoria() {
        return numeroHistoria;
    }

    public void setNumeroHistoria(String numeroHistoria) {
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
