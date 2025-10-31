package models;

import java.time.LocalDate;
import java.time.Month;

/**
 * Representa un paciente en el sistema de gestión médica. Hereda de la clase
 * Base y añade la referencia a su historia clínica.
 * Implementa relación unidireccional 1→1 con HistoriaClinica.
 *
 * @author alpha team
 * @see Base
 * @see HistoriaClinica
 */
public class Paciente extends Base {

    private String nombre;
    private String apellido;
    private String dni;
    private LocalDate fechaNacimiento;
    private HistoriaClinica historiaClinica;

    /**
     * Constructor completo para crear un paciente con todos sus datos.
     *
     * @param id              Identificador único del paciente
     * @param nombre          Nombre del paciente
     * @param apellido        Apellido del paciente
     * @param dni             Documento Nacional de Identidad (único)
     * @param fechaNacimiento Fecha de nacimiento del paciente
     * @param historiaClinica Historia clínica asociada
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
     * Constructor sin id para crear un paciente con todos sus datos.
     *
     * @param nombre          Nombre del paciente
     * @param apellido        Apellido del paciente
     * @param dni             Documento Nacional de Identidad (único)
     * @param fechaNacimiento Fecha de nacimiento del paciente
     * @param historiaClinica Historia clínica asociada
     */
    public Paciente(String nombre, String apellido, String dni,
            LocalDate fechaNacimiento, HistoriaClinica historiaClinica) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.historiaClinica = historiaClinica;
    }

    /**
     * Constructor para crear un paciente sin una historia clínica asignada.
     *
     * @param id              Identificador único del paciente
     * @param nombre          Nombre del paciente
     * @param apellido        Apellido del paciente
     * @param dni             Documento Nacional de Identidad (único)
     * @param fechaNacimiento Fecha de nacimiento del paciente
     */
    public Paciente(int id, String nombre, String apellido, String dni,
            LocalDate fechaNacimiento) {
        super(id);
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Constructor sin id para crear un paciente sin una historia clínica asignada.
     *
     * @param nombre          Nombre del paciente
     * @param apellido        Apellido del paciente
     * @param dni             Documento Nacional de Identidad (único)
     * @param fechaNacimiento Fecha de nacimiento del paciente
     */
    public Paciente(String nombre, String apellido, String dni,
            LocalDate fechaNacimiento) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Constructor por defecto.
     */
    public Paciente() {
        super();
    }

    // ============ GETTERS Y SETTERS ESPECÍFICOS DE PACIENTE ============
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío");
        }
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("El DNI no puede estar vacío");
        }
        if (!validarDni(dni)) {
            throw new IllegalArgumentException("El DNI no es válido.");
        }
        this.dni = dni;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula");
        }
        if (!validarFechaNacimiento(fechaNacimiento)) {
            throw new IllegalArgumentException("La fecha de nacimiento no es válida.");
        }
        this.fechaNacimiento = fechaNacimiento;
    }

    public HistoriaClinica getHistoriaClinica() {
        return historiaClinica;
    }

    public void setHistoriaClinica(HistoriaClinica historiaClinica) {
        this.historiaClinica = historiaClinica;
    }

    // ============ OTROS MÉTODOS ============
    /**
     * Valida que el DNI sea un número con longitud entre 7 y 15 caracteres.
     * 
     * @param dni
     * @return boolean
     */
    private boolean validarDni(String dni) {
        String regex = "^[0-9]{7,15}$";
        return dni.matches(regex);
    }

    /**
     * Valida que la fecha de nacimiento sea posterior a 1900 y que no supere
     * la fecha actual.
     * 
     * @param fechaNacimiento
     * @return boolean
     */
    private boolean validarFechaNacimiento(LocalDate fechaNacimiento) {
        return (fechaNacimiento.isBefore(LocalDate.now())
                || fechaNacimiento.isEqual(LocalDate.now()))
                && !fechaNacimiento.isAfter(LocalDate.of(1900, Month.JANUARY, 1));
    }

    /**
     * Representación en String del objeto Paciente.
     *
     * @return String representando al paciente y su historia clínica.
     */
    @Override
    public String toString() {
        return "Paciente{"
                + "id=" + getId()
                + ", nombre=" + getApellido() + ", " + getNombre()
                + ", dni=" + getDni()
                + ", fechaNacimiento=" + getFechaNacimiento()
                + ", " + (historiaClinica != null
                        ? historiaClinica.toString()
                        : "Sin Historia Clínica")
                + '}';
    }

}
