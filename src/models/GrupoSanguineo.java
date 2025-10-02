package models;

/**
 * Enumeración que representa los grupos sanguíneos con su factor Rh.
 * Combina el tipo de grupo (A, B, AB, O) con el factor Rh (positivo/negativo).
 * 
 * @author alpha team
 * @see FactorRh
 * @see HistoriaClinica
 */
public enum GrupoSanguineo {

    /** Grupo A con factor Rh positivo */
    A_PLUS(FactorRh.POSITIVO),

    /** Grupo A con factor Rh negativo */
    A_MINUS(FactorRh.NEGATIVO),

    /** Grupo B con factor Rh positivo */
    B_PLUS(FactorRh.POSITIVO),

    /** Grupo B con factor Rh negativo */
    B_MINUS(FactorRh.NEGATIVO),

    /** Grupo AB con factor Rh positivo */
    AB_PLUS(FactorRh.POSITIVO),

    /** Grupo AB con factor Rh negativo */
    AB_MINUS(FactorRh.NEGATIVO),

    /** Grupo O con factor Rh positivo */
    O_PLUS(FactorRh.POSITIVO),

    /** Grupo O con factor Rh negativo */
    O_MINUS(FactorRh.NEGATIVO);

    private final FactorRh factorRh;

    /**
     * Constructor que asocia un GrupoSanguineo con factor Rh específico.
     * 
     * @param factorRh El factor Rh del grupo sanguíneo
     */
    GrupoSanguineo(FactorRh factorRh) {
        this.factorRh = factorRh;
    }

    /**
     * Obtiene el tipo de grupo sanguíneo (sin el factor Rh).
     * 
     * @return El tipo de grupo (A, B, AB, O)
     */
    public String getTipoGrupo() {
        String name = this.name();
        return name.substring(0, name.indexOf('_'));
    }

    /**
     * Verifica si el grupo sanguíneo es compatible para donación con otro grupo.
     * Reglas básicas de compatibilidad sanguínea.
     * 
     * @param receptor Grupo sanguíneo del receptor
     * @return true si este grupo puede donar al receptor, false en caso contrario
     */
    public boolean puedeDonarA(GrupoSanguineo receptor) {
        // Reglas específicas por grupo
        return switch (this) {
            case A_MINUS -> receptor == A_MINUS
                    || receptor == A_PLUS
                    || receptor == AB_MINUS
                    || receptor == AB_PLUS;
            case A_PLUS -> receptor == A_PLUS
                    || receptor == AB_PLUS;
            case B_MINUS -> receptor == B_MINUS
                    || receptor == B_PLUS
                    || receptor == AB_MINUS
                    || receptor == AB_PLUS;
            case B_PLUS -> receptor == B_PLUS
                    || receptor == AB_PLUS;
            case AB_MINUS -> receptor == AB_MINUS
                    || receptor == AB_PLUS;
            case AB_PLUS -> receptor == AB_PLUS;
            case O_MINUS -> true; // O- puede donar a todos
            case O_PLUS -> receptor == O_PLUS
                    || receptor == A_PLUS
                    || receptor == B_PLUS
                    || receptor == AB_PLUS;
            default -> false;
        };
    }

    /**
     * Representación en String del enum GrupoSanguineo.
     * 
     * @return String en formato legible (ej: "A+", "O-")
     */
    @Override
    public String toString() {
        String tipo = getTipoGrupo();
        String simbolo = factorRh.toSymbol();
        return "GrupoSanguineo=" + tipo + simbolo;
    }

}
