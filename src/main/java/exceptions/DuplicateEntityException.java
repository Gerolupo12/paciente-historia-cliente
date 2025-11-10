package exceptions;

/**
 * Excepción para representar violaciones de unicidad de entidad.
 * <p>
 * Se lanza cuando se intenta crear o actualizar un registro que
 * entra en conflicto con una restricción única del sistema
 * (por ejemplo, un DNI o número de historia clínica ya existente).
 * </p>
 *
 * <h3>Ejemplos:</h3>
 * <ul>
 * <li>Paciente con un DNI ya registrado.</li>
 * <li>Historia clínica con un número duplicado.</li>
 * </ul>
 *
 * @author alpha team
 * @see service.PacienteService
 * @see service.HistoriaClinicaService
 */
public class DuplicateEntityException extends Exception {
    public DuplicateEntityException(String message) {
        super(message);
    }
}
