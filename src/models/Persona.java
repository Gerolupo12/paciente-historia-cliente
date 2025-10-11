package models;

import java.time.LocalDate;

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
public abstract class Persona extends Base {

    private String nombre;
    private String apellido;
    private String dni;
    private LocalDate fechaNacimiento;

    /**
     * Constructor completo para inicializar una Persona.
     *
     * @param id Identificador único
     * @param nombre Nombre de la persona
     * @param apellido Apellido de la persona
     * @param dni Documento Nacional de Identidad (único)
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
     * Constructor por defecto.
     */
    public Persona() {
        super();
    }

    // ============ GETTERS Y SETTERS ============
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento.isBefore(LocalDate.now())
                || fechaNacimiento.isEqual(LocalDate.now())) {
            this.fechaNacimiento = fechaNacimiento;
        }
    }

}
