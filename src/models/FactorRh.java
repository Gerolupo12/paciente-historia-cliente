package models;

/**
 * Enumeración que representa el factor Rh (Rhesus) de los grupos sanguíneos.
 * Determina si el grupo sanguíneo es positivo o negativo.
 * 
 * @author alpha team
 * @see GrupoSanguineo
 */
public enum FactorRh {
    /** Factor Rh positivo */
    POSITIVO,

    /** Factor Rh negativo */
    NEGATIVO;

    /**
     * Convierte el factor Rh a símbolo para mostrar.
     * 
     * @return "+" para POSITIVO, "-" para NEGATIVO
     */
    public String toSymbol() {
        return this == POSITIVO ? "+" : "-";
    }

    /**
     * Representación en String del enum FactorRh.
     * 
     * @return String representando al factor Rh
     */
    @Override
    public String toString() {
        return "FactorRh=" + name();
    }

}
