package main.java.models;

/**
 * Enumeración (Enum) que representa los grupos sanguíneos y su factor Rh.
 * <p>
 * Esta clase implementa el patrón <b>Type-Safe Enum</b>, garantizando que
 * solo se puedan asignar valores válidos de grupo sanguíneo a una
 * {@link HistoriaClinica}.
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li>Proporcionar una lista finita y segura de tipos de sangre (A+, A-, B+,
 * etc.).</li>
 * <li>Encapsular la lógica de compatibilidad de donación
 * ({@link #puedeDonarA(GrupoSanguineo)}).</li>
 * <li>Proporcionar una representación amigable para la UI
 * ({@link #toString()}),
 * que convierte <code>A_PLUS</code> en <code>"A+"</code>.</li>
 * </ul>
 *
 * <h3>Persistencia:</h3>
 * <p>
 * En la base de datos, se persiste el <b>nombre</b> del enum (ej: "A_PLUS")
 * en una tabla <code>GrupoSanguineo</code>, y la
 * <code>HistoriaClinicaDAO</code> lo mapea a esta enumeración al leer los
 * datos.
 * </p>
 *
 * @author alpha team
 * @see HistoriaClinica
 * @see service.HistoriaClinicaService
 * @see dao.HistoriaClinicaDAO
 */
public enum GrupoSanguineo {

    /** Grupo A Positivo (A+) */
    A_PLUS,

    /** Grupo A Negativo (A-) */
    A_MINUS,

    /** Grupo B Positivo (B+) */
    B_PLUS,

    /** Grupo B Negativo (B-) */
    B_MINUS,

    /** Grupo AB Positivo (AB+) */
    AB_PLUS,

    /** Grupo AB Negativo (AB-) */
    AB_MINUS,

    /** Grupo O Positivo (O+) */
    O_PLUS,

    /** Grupo O Negativo (O-) (Donante Universal) */
    O_MINUS;

    // ============ MÉTODOS ============
    /**
     * Verifica si este grupo sanguíneo (el donante) es compatible para una donación
     * con el grupo receptor.
     * <p>
     * Implementa las reglas básicas de compatibilidad sanguínea, donde
     * <code>O_MINUS</code> es el donante universal.
     * </p>
     *
     * @param receptor El {@link GrupoSanguineo} del receptor.
     * @return "Si" si este grupo (<code>this</code>) puede donar al
     *         <code>receptor</code>, "No" en caso contrario.
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
     * Devuelve una representación en String amigable para el usuario.
     * <p>
     * Este método es crucial para la capa de UI ({@link main.MenuHandler}),
     * ya que convierte la representación interna (ej: <code>A_PLUS</code>)
     * en el formato que el usuario ve y espera (ej: <code>"A+"</code>).
     * </p>
     * <p>
     * Ejemplo: <code>A_PLUS.toString()</code> devuelve <code>"A+"</code>.
     * </p>
     *
     * @return Una cadena de texto formateada (ej: "A+", "O-").
     */
    @Override
    public String toString() {

        String name = this.name(); // Ej: "A_PLUS"

        // Extrae la parte antes del '_', ej: "A"
        String tipo = name.substring(0, name.indexOf('_'));

        // Determina el factor Rh basándose en el sufijo
        String factorRh = name.endsWith("_PLUS") ? "+" : "-";

        // Concatena para formar la salida deseada, ej: "A" + "+"
        return tipo + factorRh;
    }

}
