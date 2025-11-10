package models;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad Paciente (representa la Entidad "A" en la relación 1-a-1).
 * <p>
 * Modela un paciente en el sistema de gestión médica. Hereda de la clase
 * {@link Base} para obtener los campos <code>id</code> y
 * <code>eliminado</code>.
 * </p>
 *
 * <h3>Relación con HistoriaClinica (Entidad "B"):</h3>
 * <ul>
 * <li>Implementa la relación <b>unidireccional 1-a-1</b> (Paciente →
 * HistoriaClinica) requerida por la consigna del TPI.</li>
 * <li>Un Paciente puede tener 0 o 1 HistoriaClinica.</li>
 * <li>La FK <code>historia_clinica_id</code> se almacena en la tabla
 * <code>Paciente</code>.</li>
 * </ul>
 *
 * <h3>Reglas de Negocio Clave:</h3>
 * <ul>
 * <li><b>RN-001 (Validación):</b> Nombre, Apellido, DNI y Fecha de Nacimiento
 * son obligatorios. Esta validación es responsabilidad de
 * {@link service.PacienteService}.</li>
 * <li><b>RN-002 (Unicidad):</b> El DNI debe ser único en el sistema.
 * Esta validación se implementa en
 * {@link service.PacienteService#validateDniUnique}
 * y con un <code>UNIQUE constraint</code> en la base de datos.</li>
 * </ul>
 *
 * @author alpha team
 * @see Base
 * @see HistoriaClinica
 * @see service.PacienteService
 * @see dao.PacienteDAO
 */
public class Paciente extends Base {

    // ============ ATRIBUTOS ============
    /**
     * Nombre del paciente.
     * Requerido (no nulo, no vacío). La validación es responsabilidad de
     * {@link service.PacienteService}.
     */
    private String nombre;

    /**
     * Apellido del paciente.
     * Requerido (no nulo, no vacío). La validación es responsabilidad de
     * {@link service.PacienteService}.
     */
    private String apellido;

    /**
     * Documento Nacional de Identidad (DNI) del paciente.
     * Requerido (no nulo, no vacío) y debe ser único (RN-002).
     * La validación es responsabilidad de {@link service.PacienteService}.
     * <p>
     * Se utiliza como clave de negocio para implementar {@link #equals(Object)}
     * y {@link #hashCode()}.
     * </p>
     */
    private String dni;

    /**
     * Fecha de nacimiento del paciente.
     * Requerida. La validación (ej: no nula, no futura) es responsabilidad de
     * {@link service.PacienteService}.
     */
    private LocalDate fechaNacimiento;

    /**
     * Referencia a la Historia Clínica asociada (Entidad "B").
     * Esta es la implementación de la <b>relación 1-a-1 unidireccional</b>.
     * <p>
     * Puede ser <code>null</code> si el paciente (A) no tiene una Historia Clínica
     * (B) asignada.
     * </p>
     * <p>
     * En la capa DAO ({@link dao.PacienteDAO}), este objeto se carga usando un
     * <code>LEFT JOIN</code>.
     * </p>
     */
    private HistoriaClinica historiaClinica;

    // ============ CONSTRUCTORES ============
    /**
     * Constructor completo.
     * Usado para reconstruir un objeto Paciente con todos sus datos,
     * (ej: por el DAO desde la base de datos).
     *
     * @param id              Identificador único (PK)
     * @param nombre          Nombre del paciente
     * @param apellido        Apellido del paciente
     * @param dni             Documento de identidad (único)
     * @param fechaNacimiento Fecha de nacimiento
     * @param historiaClinica La Historia Clínica asociada (puede ser null)
     */
    public Paciente(int id, String nombre, String apellido, String dni,
            LocalDate fechaNacimiento, HistoriaClinica historiaClinica) {

        super(id);
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.historiaClinica = historiaClinica;
    }

    /**
     * Constructor para una nueva instancia (sin ID) con Historia Clínica.
     * Usado antes de persistir un paciente nuevo que ya tiene una HC.
     *
     * @param nombre          Nombre del paciente
     * @param apellido        Apellido del paciente
     * @param dni             Documento de identidad (único)
     * @param fechaNacimiento Fecha de nacimiento
     * @param historiaClinica La Historia Clínica asociada
     */
    public Paciente(String nombre, String apellido, String dni,
            LocalDate fechaNacimiento, HistoriaClinica historiaClinica) {

        // Llama al constructor de Base() (id=0, eliminado=false)
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.historiaClinica = historiaClinica;
    }

    /**
     * Constructor para reconstruir un Paciente (con ID) sin Historia Clínica.
     *
     * @param id              Identificador único (PK)
     * @param nombre          Nombre del paciente
     * @param apellido        Apellido del paciente
     * @param dni             Documento de identidad (único)
     * @param fechaNacimiento Fecha de nacimiento
     */
    public Paciente(int id, String nombre, String apellido, String dni,
            LocalDate fechaNacimiento) {

        super(id);
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        // historiaClinica queda null por defecto
    }

    /**
     * Constructor para una nueva instancia (sin ID) sin Historia Clínica.
     * Usado comúnmente desde la UI para crear un nuevo paciente.
     *
     * @param nombre          Nombre del paciente
     * @param apellido        Apellido del paciente
     * @param dni             Documento de identidad (único)
     * @param fechaNacimiento Fecha de nacimiento
     */
    public Paciente(String nombre, String apellido, String dni,
            LocalDate fechaNacimiento) {

        // Llama al constructor de Base() (id=0, eliminado=false)
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        // historiaClinica queda null por defecto
    }

    /**
     * Constructor por defecto.
     * Necesario para algunas librerías de mapeo o frameworks.
     */
    public Paciente() {
        super();
    }

    // ============ GETTERS Y SETTERS ESPECÍFICOS DE PACIENTE ============
    // Los setters son simples y no contienen lógica de negocio
    // La validación es responsabilidad de la Capa de Servicio.

    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del paciente.
     * La validación (ej: no nulo/vacío) es manejada por
     * {@link service.PacienteService} antes de persistir.
     *
     * @param nombre El nombre del paciente.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    /**
     * Establece el apellido del paciente.
     * La validación (ej: no nulo/vacío) es manejada por
     * {@link service.PacienteService} antes de persistir.
     *
     * @param apellido El apellido del paciente.
     */
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    /**
     * Establece el DNI del paciente.
     * La validación (ej: no nulo/vacío, formato, unicidad RN-002)
     * es manejada por {@link service.PacienteService} antes de persistir.
     *
     * @param dni El DNI del paciente.
     */
    public void setDni(String dni) {
        this.dni = dni;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    /**
     * Establece la fecha de nacimiento del paciente.
     * La validación (ej: no nula, no futura) es manejada por
     * {@link service.PacienteService} antes de persistir.
     *
     * @param fechaNacimiento La fecha de nacimiento.
     */
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public HistoriaClinica getHistoriaClinica() {
        return historiaClinica;
    }

    /**
     * Asocia o desasocia una historia clínica a este paciente.
     *
     * @param historiaClinica La {@link HistoriaClinica} a asociar, o
     *                        <code>null</code> para desasociar (esto resultará en
     *                        <code>historia_clinica_id = NULL</code> en la base de
     *                        datos).
     */
    public void setHistoriaClinica(HistoriaClinica historiaClinica) {
        this.historiaClinica = historiaClinica;
    }

    // ============ OTROS MÉTODOS ============
    /**
     * Devuelve una representación en String del objeto Paciente.
     * Útil para logging y depuración.
     *
     * @return String representando al paciente y su historia clínica (si existe).
     */
    @Override
    public String toString() {

        return "Paciente{"
                + "id=" + getId()
                + ", eliminado=" + isEliminado() // Heredado de Base
                + ", nombre='" + getNombre()
                + ", apellido='" + getApellido()
                + ", dni=" + getDni()
                + ", fechaNacimiento=" + getFechaNacimiento()
                + ", " + (historiaClinica != null
                        ? historiaClinica.toString()
                        : "Sin Historia Clínica")
                + '}';
    }

    /**
     * Compara si dos objetos Paciente son iguales basándose en su DNI.
     * <p>
     * Esta es la implementación de la "igualdad de negocio" (Regla RN-002).
     * Dos pacientes son considerados el mismo si tienen el mismo DNI,
     * ya que el DNI es la clave natural única del paciente.
     * </p>
     *
     * @param o El objeto a comparar.
     * @return <code>true</code> si los DNI son iguales, <code>false</code> en
     *         caso contrario.
     */
    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Paciente paciente = (Paciente) o;

        // La comparación se basa únicamente en la clave de negocio (DNI)
        return Objects.equals(dni, paciente.dni);
    }

    /**
     * Genera un código hash basado en el DNI del paciente.
     * <p>
     * Es consistente con {@link #equals(Object)}: si dos pacientes
     * son <code>equals()</code> (mismo DNI), tendrán el mismo
     * <code>hashCode()</code>.
     * </p>
     * <p>
     * Fundamental para el correcto funcionamiento en colecciones
     * como <code>HashMap</code> o <code>HashSet</code>.
     * </p>
     *
     * @return El código hash del paciente (basado en el DNI).
     */
    @Override
    public int hashCode() {
        // El hash code se basa únicamente en los campos usados en equals()
        return Objects.hash(dni);
    }

}
