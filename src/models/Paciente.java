package models;

import java.time.LocalDate;

/**
 * Representa un paciente en el sistema de gestión médica. Hereda los datos
 * personales de la clase Persona y añade la referencia a su historia clínica.
 * Implementa relación unidireccional 1→1 con HistoriaClinica.
 *
 * @author alpha team
 * @see Persona
 * @see HistoriaClinica
 */
public class Paciente extends Persona {

    private HistoriaClinica historiaClinica;

    /**
     * Constructor completo para crear un paciente con todos sus datos.
     *
     * @param id Identificador único del paciente
     * @param nombre Nombre del paciente
     * @param apellido Apellido del paciente
     * @param dni Documento Nacional de Identidad (único)
     * @param fechaNacimiento Fecha de nacimiento del paciente
     * @param historiaClinica Historia clínica asociada
     */
    public Paciente(int id, String nombre, String apellido, String dni,
            LocalDate fechaNacimiento, HistoriaClinica historiaClinica) {
        super(id, nombre, apellido, dni, fechaNacimiento);
        this.historiaClinica = historiaClinica;
    }

    /**
     * Constructor para crear un paciente sin una historia clínica asignada.
     *
     * @param id Identificador único del paciente
     * @param nombre Nombre del paciente
     * @param apellido Apellido del paciente
     * @param dni Documento Nacional de Identidad (único)
     * @param fechaNacimiento Fecha de nacimiento del paciente
     */
    public Paciente(int id, String nombre, String apellido, String dni,
            LocalDate fechaNacimiento) {
        super(id, nombre, apellido, dni, fechaNacimiento);
    }

    /**
     * Constructor por defecto.
     */
    public Paciente() {
        super();
    }

    // ============ GETTERS Y SETTERS ESPECÍFICOS DE PACIENTE ============
     public HistoriaClinica getHistoriaClinica() {
        return historiaClinica;
    }

    public void setHistoriaClinica(HistoriaClinica historiaClinica) {
        this.historiaClinica = historiaClinica;
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
                + ", " + (historiaClinica != null ? historiaClinica.toString() : "Sin Historia Clínica")
                + '}';
    }

}
