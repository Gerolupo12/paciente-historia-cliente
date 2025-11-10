package exceptions;

/**
 * Excepción específica para errores de validación de datos.
 * <p>
 * Se lanza cuando una entidad no cumple con las
 * Reglas de Negocio (RN) definidas en la capa de servicio.
 * </p>
 *
 * <h3>Ejemplos:</h3>
 * <ul>
 * <li>Nombre vacío o con caracteres no válidos.</li>
 * <li>DNI con formato incorrecto o longitud fuera de rango.</li>
 * <li>Fecha de nacimiento fuera del rango permitido.</li>
 * </ul>
 *
 * <p>
 * Esta excepción es <b>verificada</b> (checked exception), lo que obliga
 * a capturarla o declararla con <code>throws</code>.
 * </p>
 *
 * @author alpha team
 * @see service.GenericService
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
