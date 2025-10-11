package models;

import java.time.LocalDate;

/**
 * Representa a un profesional médico en el sistema. Hereda los datos personales
 * de la clase Persona y añade atributos específicos como la matrícula y la
 * especialidad.
 *
 * @author alpha team
 * @see Persona
 * @see HistoriaClinica
 */
public class Profesional extends Persona {

    private String matricula;
    private String especialidad;

    /**
     * Constructor completo para crear un Profesional.
     *
     * @param id Identificador único del profesional
     * @param nombre Nombre del profesional
     * @param apellido Apellido del profesional
     * @param dni Documento Nacional de Identidad (único)
     * @param fechaNacimiento Fecha de nacimiento
     * @param matricula Matrícula profesional (única)
     * @param especialidad Especialidad médica
     */
    public Profesional(int id, String nombre, String apellido, String dni,
            LocalDate fechaNacimiento, String matricula, String especialidad) {
        super(id, nombre, apellido, dni, fechaNacimiento);
        this.matricula = matricula;
        this.especialidad = especialidad;
    }

    /**
     * Constructor por defecto.
     */
    public Profesional() {
        super();
    }

    // ============ GETTERS Y SETTERS ESPECÍFICOS DE PROFESIONAL ============
    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    /**
     * Representación en String del objeto Profesional.
     *
     * @return String representando al profesional.
     */
    @Override
    public String toString() {
        return "Profesional{"
                + "id=" + getId()
                + ", nombre=" + getApellido() + ", " + getNombre()
                + ", dni=" + getDni()
                + ", matricula=" + matricula
                + ", especialidad=" + especialidad
                + '}';
    }

}
