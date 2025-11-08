package exceptions;
/**
 * Excepción general para errores en la capa de servicio.
 * <p>
 * Se utiliza para encapsular errores no controlados o
 * excepciones técnicas que ocurren durante la ejecución
 * de una operación de negocio (por ejemplo, errores de base de datos).
 * </p>
 *
 * <p>
 * A diferencia de {@link ValidationException}, esta clase representa
 * errores internos o transaccionales, no imputables al usuario.
 * </p>
 *
 * <h3>Ejemplos:</h3>
 * <ul>
 *   <li>Fallo en la conexión a la base de datos.</li>
 *   <li>Error durante un commit o rollback transaccional.</li>
 * </ul>
 *
 * @author alpha
 * @see service.PacienteService
 * @see service.HistoriaClinicaService
 */
public class ServiceException extends Exception {
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
