package models;

import java.time.LocalDate;
import java.time.Month;

/**
 * Clase abstracta que representa a una persona en el sistema. Proporciona los
 * atributos y comportamientos comunes para entidades como Paciente y
 * Profesional, evitando la duplicación de código. Hereda de la clase Base para
 * incluir ID y baja lógica.
 *
 * @author alpha team
 * @see Base
 * @see Paciente
 * @see Profesional
 */
public class Persona extends Base {

    private String nombre;
    private String apellido;
    private String dni;
    private LocalDate fechaNacimiento;

    /**
     * Constructor completo para inicializar una Persona.
     *
     * @param id              Identificador único
     * @param nombre          Nombre de la persona
     * @param apellido        Apellido de la persona
     * @param dni             Documento Nacional de Identidad (único)
     * @param fechaNacimiento Fecha de nacimiento
     */
    public Persona(int id, String nombre, String apellido, String dni, LocalDate fechaNacimiento) {
        super(id);
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Constructor sin id para inicializar una Persona.
     *
     * @param nombre          Nombre de la persona
     * @param apellido        Apellido de la persona
     * @param dni             Documento Nacional de Identidad (único)
     * @param fechaNacimiento Fecha de nacimiento
     */
    public Persona(String nombre, String apellido, String dni, LocalDate fechaNacimiento) {
        super();
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Constructor por defecto.
     */
    public Persona() {
        super();
    }

    // ============ GETTERS Y SETTERS ESPECÍFICOS DE PERSONA ============
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
     * Representación en String del objeto Persona.
     *
     * @return String representando a la persona.
     */
    @Override
    public String toString() {
        return "Paciente{"
                + "id=" + getId()
                + ", nombre=" + getApellido() + ", " + getNombre()
                + ", dni=" + getDni()
                + ", fechaNacimiento=" + getFechaNacimiento()
                + '}';
    }

}
