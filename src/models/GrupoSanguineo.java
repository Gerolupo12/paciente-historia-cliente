package models;

/**
 * Enumeración que representa los grupos sanguíneos con su factor Rh.
 * Combina el tipo de grupo (A, B, AB, O) con el factor Rh (positivo/negativo).
 * 
 * @author alpha team
 * @see HistoriaClinica
 */
public enum GrupoSanguineo {

    /** Grupo A+ */
    A_PLUS,

    /** Grupo A- */
    A_MINUS,

    /** Grupo B+ */
    B_PLUS,

    /** Grupo B- */
    B_MINUS,

    /** Grupo AB+ */
    AB_PLUS,

    /** Grupo AB- */
    AB_MINUS,

    /** Grupo O+ */
    O_PLUS,

    /** Grupo O- */
    O_MINUS;

    // ============ MÉTODOS ============
    /**
     * Verifica si el grupo sanguíneo es compatible para donación con otro grupo.
     * Reglas básicas de compatibilidad sanguínea.
     * 
     * @param receptor Grupo sanguíneo del receptor
     * @return true si este grupo puede donar al receptor, false en caso contrario
     */
    public String puedeDonarA(GrupoSanguineo receptor) {
        // Reglas específicas por grupo
        boolean compatible = switch (this) {
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
        return (compatible ? "Si" : "No");
    }

    /**
     * Representación en String del enum GrupoSanguineo.
     * 
     * @return String representando el grupo sanguíneo
     */
    @Override
    public String toString() {
        String name = this.name();
        String tipo = name.substring(0, name.indexOf('_'));
        String factorRh = name.endsWith("_PLUS") ? "+" : "-";
        return tipo + factorRh;
    }

}
