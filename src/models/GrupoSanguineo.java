package models;

/**
 * Enumeración que representa los posibles grupos sanguíneos en el sistema.
 * Incluye todos los tipos de grupos sanguíneos con sus factores Rh positivos y
 * negativos.
 * 
 * @author alpha team
 * @see HistoriaClinica
 */
public enum GrupoSanguineo {

    /** Grupo A con factor Rh positivo */
    A_PLUS,

    /** Grupo A con factor Rh negativo */
    A_MINUS,

    /** Grupo B con factor Rh positivo */
    B_PLUS,

    /** Grupo B con factor Rh negativo */
    B_MINUS,

    /** Grupo AB con factor Rh positivo */
    AB_PLUS,

    /** Grupo AB con factor Rh negativo */
    AB_MINUS,

    /** Grupo O con factor Rh positivo */
    O_PLUS,

    /** Grupo O con factor Rh negativo */
    O_MINUS;

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
}
