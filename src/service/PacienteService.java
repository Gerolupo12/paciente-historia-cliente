package service;

import dao.PacienteDAO;
import java.util.List;
import models.Paciente;

/**
 * Servicio para gestionar operaciones relacionadas con pacientes.
 * Implementa la interfaz GenericService para operaciones CRUD y validaciones
 * específicas.
 * 
 * @author alpha team
 */
public class PacienteService implements GenericService<Paciente> {

    // Dependencias
    private final PacienteDAO pacienteDAO;
    private final HistoriaClinicaService historiaClinicaService;

    /**
     * Constructor que inyecta las dependencias necesarias.
     * 
     * @param pacienteDAO
     * @param historiaClinicaService
     */
    public PacienteService(PacienteDAO pacienteDAO, HistoriaClinicaService historiaClinicaService) {

        if (pacienteDAO == null) {
            throw new IllegalArgumentException("El DAO de Paciente no puede ser nulo.");
        }
        if (historiaClinicaService == null) {
            throw new IllegalArgumentException("El servicio de Historia Clínica no puede ser nulo.");
        }
        this.pacienteDAO = pacienteDAO;
        this.historiaClinicaService = historiaClinicaService;
    }

    /**
     * Inserta un nuevo paciente en la base de datos.
     * Realiza validaciones de la entidad y asegura la unicidad del DNI.
     * Gestiona la inserción o actualización de la historia clínica asociada.
     * 
     * @param paciente El objeto Paciente a insertar.
     * @throws Exception Si ocurre un error durante la inserción o validación.
     */
    @Override
    public void insert(Paciente paciente) throws Exception {

        validateEntity(paciente);
        validateDniUnique(paciente.getDni(), null);

        try {
            System.out.println("Insertando paciente: " + paciente.getApellido() + ", " + paciente.getNombre());

            if (paciente.getHistoriaClinica() != null) {
                if (paciente.getHistoriaClinica().getId() == 0) {
                    historiaClinicaService.insert(paciente.getHistoriaClinica());
                } else {
                    historiaClinicaService.update(paciente.getHistoriaClinica());
                }
            }
            pacienteDAO.insert(paciente);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error al insertar la paciente: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza un paciente existente en la base de datos.
     * Realiza validaciones de la entidad y asegura la unicidad del DNI.
     * Gestiona la inserción o actualización de la historia clínica asociada.
     * 
     * @param paciente El objeto Paciente a actualizar.
     * @throws Exception Si ocurre un error durante la actualización o validación.
     */
    @Override
    public void update(Paciente paciente) throws Exception {

        validateEntity(paciente);

        if (paciente.getId() <= 0) {
            throw new IllegalArgumentException("El ID del paciente debe ser mayor a 0 para actualizar");
        }

        validateDniUnique(paciente.getDni(), paciente.getId());

        try {
            System.out.println("Actualizando paciente: " + paciente.getApellido() + ", " + paciente.getNombre());

            if (paciente.getHistoriaClinica() != null) {
                if (paciente.getHistoriaClinica().getId() == 0) {
                    historiaClinicaService.insert(paciente.getHistoriaClinica());
                } else {
                    historiaClinicaService.update(paciente.getHistoriaClinica());
                }
            }
            pacienteDAO.update(paciente);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error al actualizar la paciente: " + e.getMessage(), e);
        }

    }

    /**
     * Elimina un paciente de la base de datos por su ID (borrado lógico).
     * Si el paciente tiene una historia clínica asociada, también la elimina.
     * 
     * @param id ID del paciente a eliminar.
     * @throws Exception Si ocurre un error durante la eliminación.
     */
    @Override
    public void delete(int id) throws Exception {

        if (id <= 0) {
            throw new IllegalArgumentException("El ID del paciente debe ser mayor a cero.");
        }

        try {
            Paciente paciente = pacienteDAO.selectById(id);

            if (paciente == null) {
                System.out.println("Paciente con ID " + id + " no encontrado (o ya estaba eliminado).");
                return;
            }

            if (paciente.getHistoriaClinica() != null) {
                int historiaId = paciente.getHistoriaClinica().getId();
                System.out.println("Eliminando historia clínica asociada con ID: " + historiaId);
                historiaClinicaService.delete(historiaId);
            }

            System.out.println("Eliminando paciente con ID: " + id);
            pacienteDAO.delete(id);

        } catch (RuntimeException e) {
            throw new RuntimeException("Error al eliminar el paciente: " + e.getMessage(), e);
        }
    }

    /**
     * Busca un paciente por su ID.
     *
     * @param id ID del paciente.
     * @return Paciente encontrado o null si no existe.
     * @throws Exception Si ocurre un error durante la búsqueda.
     */
    @Override
    public Paciente selectById(int id) throws Exception {

        if (id <= 0) {
            throw new IllegalArgumentException("El ID del paciente debe ser mayor a cero.");
        }

        try {
            System.out.println("\nObteniendo paciente con ID: " + id);
            return pacienteDAO.selectById(id);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error al obtener el paciente por ID: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los pacientes de la base de datos.
     *
     * @return Lista de todos los pacientes.
     * @throws Exception Si ocurre un error durante la obtención de los pacientes.
     */
    @Override
    public List<Paciente> selectAll(boolean deleted) throws Exception {

        try {
            List<Paciente> pacientes = pacienteDAO.selectAll(deleted);
            String tipo = deleted ? "eliminados" : "activos";

            System.out.println("\n=========================================");
            System.out.println(pacientes.size() + " pacientes " + tipo + " encontrados.");
            System.out.println("=========================================");

            return pacientes;

        } catch (RuntimeException e) {
            throw new RuntimeException("Error al obtener todos los pacientes: " + e.getMessage(), e);
        }
    }

    /**
     * Recupera un paciente eliminado lógicamente por su ID.
     *
     * @param id ID del paciente a recuperar.
     * @throws Exception Si ocurre un error durante la recuperación.
     */
    @Override
    public void recover(int id) throws Exception {

        if (id <= 0) {
            throw new IllegalArgumentException("El ID del paciente debe ser mayor a cero.");
        }

        try {
            System.out.println("Recuperando paciente con ID: " + id);
            pacienteDAO.recover(id);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error al recuperar el paciente: " + e.getMessage(), e);
        }
    }

    /**
     * Busca pacientes que coincidan con un filtro en sus atributos (nombre,
     * apellido o DNI).
     *
     * @param filter Cadena de texto para filtrar.
     * @return Lista de pacientes que coinciden con el filtro.
     * @throws Exception Si ocurre un error durante la búsqueda.
     */
    @Override
    public List<Paciente> searchByFilter(String filter) throws Exception {

        if (filter == null || filter.trim().isEmpty()) {
            throw new IllegalArgumentException("El filtro de búsqueda no puede estar vacío");
        }

        try {
            List<Paciente> pacientes = pacienteDAO.searchByFilter(filter);

            System.out.println("\n=========================================");
            System.out.println(pacientes.size() + " pacientes encontrados por filtro: " + filter);
            System.out.println("=========================================");

            return pacientes;

        } catch (RuntimeException e) {
            throw new RuntimeException("Error al buscar pacientes por nombre o apellido: " + e.getMessage(), e);
        }
    }

    /**
     * Busca un paciente por su DNI.
     * 
     * @param dni DNI del paciente.
     * @return Paciente encontrado o null si no existe.
     * @throws Exception
     */
    public Paciente selectByDni(String dni) throws Exception {

        if (dni == null || dni.trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI no puede ser nulo o vacío.");
        }

        try {

            System.out.println("\n=========================================");
            System.out.println("Obteniendo paciente con DNI: " + dni);
            System.out.println("=========================================");

            return pacienteDAO.selectByDni(dni);

        } catch (RuntimeException e) {
            throw new RuntimeException("Error al obtener el paciente por DNI: " + e.getMessage(), e);
        }
    }

    /**
     * Valida los datos de un paciente.
     *
     * @param paciente El objeto Paciente a validar.
     * @return true si el paciente es válido.
     * @throws IllegalArgumentException Si algún dato del paciente no es válido.
     */
    @Override
    public void validateEntity(Paciente paciente) throws Exception {

        if (paciente == null) {
            throw new IllegalArgumentException("El paciente no puede ser nulo.");
        }

        if (paciente.getNombre() == null || paciente.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del paciente no puede estar vacío.");
        }

        if (paciente.getApellido() == null || paciente.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido del paciente no puede estar vacío.");
        }

        if (paciente.getDni() == null || paciente.getDni().trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI del paciente no puede estar vacío");
        }

        if (paciente.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula.");
        }
    }

    /**
     * Valida que el DNI de un paciente sea único en la base de datos.
     *
     * @param dni        El DNI a validar.
     * @param pacienteId El ID del paciente (para exclusión en actualizaciones).
     * @throws Exception Si el DNI ya existe para otro paciente.
     */
    private void validateDniUnique(String dni, Integer pacienteId) throws Exception {

        Paciente existente = pacienteDAO.selectByDni(dni);

        if (existente != null) {
            // Existe un paciente con ese DNI
            if (pacienteId == null || existente.getId() != pacienteId) {
                throw new IllegalArgumentException("Ya existe un paciente con el DNI: " + dni);
            }
        }
    }

    /**
     * Elimina la historia clínica asociada a un paciente.
     * Actualiza la referencia en el paciente antes de eliminar la historia clínica.
     * 
     * @param pacienteId        ID del paciente.
     * @param historiaClinicaId ID de la historia clínica a eliminar.
     * @throws Exception Si ocurre un error durante la eliminación.
     */
    public void deleteHistoriaClinica(int pacienteId, int historiaClinicaId) throws Exception {

        if (pacienteId <= 0 || historiaClinicaId <= 0) {
            throw new IllegalArgumentException("Los IDs deben ser mayores a 0");
        }

        Paciente paciente = pacienteDAO.selectById(pacienteId);
        if (paciente == null) {
            throw new IllegalArgumentException("Paciente no encontrado con ID: " + pacienteId);
        }

        if (paciente.getHistoriaClinica() == null || paciente.getHistoriaClinica().getId() != historiaClinicaId) {
            throw new IllegalArgumentException("La Historia Clínica no pertenece a este paciente");
        }

        // Secuencia transaccional: actualizar FK → eliminar historiaClinica
        paciente.setHistoriaClinica(null);
        pacienteDAO.update(paciente);
        historiaClinicaService.delete(historiaClinicaId);
    }
}
