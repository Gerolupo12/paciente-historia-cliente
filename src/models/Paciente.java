package models;

import java.time.LocalDate;

/**
 * Representa un paciente en el sistema de gestión médica.
 * Contiene información personal del paciente y referencia a su historia
 * clínica.
 * Implementa relación unidireccional 1→1 con HistoriaClinica.
 * 
 * @author alpha team
 * @see HistoriaClinica
 * @see Base
 */
public class Paciente extends Base {

    private String nombre;
    private String apellido;
    private String dni;
    private LocalDate fechaNacimiento;
    private HistoriaClinica historiaClinica;

    /**
     * Constructor completo para crear un paciente con todos sus datos básicos.
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
     * Constructor por defecto.
     */
    public Paciente() {
        super(); // Llama al constructor por defecto de Base
    }

    // ============ GETTERS Y SETTERS ============

    /**
     * Obtiene el nombre del paciente.
     * 
     * @return Nombre del paciente
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del paciente.
     * 
     * @param nombre Nuevo nombre del paciente
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el apellido del paciente.
     * 
     * @return Apellido del paciente
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * Establece el apellido del paciente.
     * 
     * @param apellido Nuevo apellido del paciente
     */
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    /**
     * Obtiene el Documento Nacional de Identidad del paciente.
     * Este campo es único en el sistema.
     * 
     * @return DNI del paciente
     */
    public String getDni() {
        return dni;
    }

    /**
     * Establece el Documento Nacional de Identidad del paciente.
     * Debe ser único en el sistema.
     * 
     * @param dni Nuevo DNI del paciente
     */
    public void setDni(String dni) {
        this.dni = dni;
    }

    /**
     * Obtiene la fecha de nacimiento del paciente.
     * 
     * @return Fecha de nacimiento
     */
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    /**
     * Establece la fecha de nacimiento del paciente validando que no sea mayor
     * que la fecha actual.
     * 
     * @param fechaNacimiento Nueva fecha de nacimiento
     */
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento.isBefore(LocalDate.now())
                || fechaNacimiento.isEqual(LocalDate.now())) {
            this.fechaNacimiento = fechaNacimiento;
        }
    }

    /**
     * Obtiene la historia clínica asociada al paciente.
     * Relación unidireccional 1→1.
     * 
     * @return Historia clínica del paciente, o null si no tiene
     */
    public HistoriaClinica getHistoriaClinica() {
        return historiaClinica;
    }

    /**
     * Establece la historia clínica asociada al paciente.
     * Relación unidireccional 1→1.
     * 
     * @param historiaClinica Nueva historia clínica del paciente
     */
    public void setHistoriaClinica(HistoriaClinica historiaClinica) {
        this.historiaClinica = historiaClinica;
    }

    /**
     * Representación en String del objeto Paciente.
     * Incluye todos los datos básicos y referencia a historia clínica.
     * 
     * @return String representando al paciente
     */
    @Override
    public String toString() {
        return "Paciente{"
                + "id=" + getId()
                + ", nombre=" + nombre
                + ", apellido=" + apellido
                + ", dni=" + dni
                + ", fechaNacimiento=" + fechaNacimiento
                + ", " + getHistoriaClinica()
                + ", eliminado=" + getEliminado() + '}';
    }

}
