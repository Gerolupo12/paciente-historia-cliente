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
     * @param id              Identificador único del profesional
     * @param nombre          Nombre del profesional
     * @param apellido        Apellido del profesional
     * @param dni             Documento Nacional de Identidad (único)
     * @param fechaNacimiento Fecha de nacimiento
     * @param matricula       Matrícula profesional (única)
     * @param especialidad    Especialidad médica
     */
    public Profesional(int id, String nombre, String apellido, String dni,
            LocalDate fechaNacimiento, String matricula, String especialidad) {
        super(id, nombre, apellido, dni, fechaNacimiento);
        this.matricula = matricula;
        this.especialidad = especialidad;
    }

    /**
     * Constructor sin id para crear un Profesional.
     *
     * @param nombre          Nombre del profesional
     * @param apellido        Apellido del profesional
     * @param dni             Documento Nacional de Identidad (único)
     * @param fechaNacimiento Fecha de nacimiento
     * @param matricula       Matrícula profesional (única)
     * @param especialidad    Especialidad médica
     */
    public Profesional(String nombre, String apellido, String dni,
            LocalDate fechaNacimiento, String matricula, String especialidad) {
        super(nombre, apellido, dni, fechaNacimiento);
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
        if (matricula == null || matricula.isBlank() || !validarMatricula(matricula)) {
            throw new IllegalArgumentException("La matrícula no es válida.");
        }
        this.matricula = matricula;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        if (especialidad == null || especialidad.isBlank()) {
            throw new IllegalArgumentException("La especialidad no puede estar vacía.");
        }
        this.especialidad = especialidad;
    }

    // ============ OTROS MÉTODOS ============
    /**
     * Valida que la matrícula cumpla con la expresión regular.
     * 
     * @param matricula
     * @return boolean
     */
    private boolean validarMatricula(String matricula) {
        String regex = "^(MP|MN|MI)-[0-9]{5,17}$";
        return (matricula.matches(regex));
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
