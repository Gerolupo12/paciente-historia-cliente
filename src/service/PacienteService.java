package service;

import dao.PacienteDAO;
import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import models.Paciente;
import models.HistoriaClinica;
import exceptions.ServiceException;
import exceptions.ValidationException;
import exceptions.DuplicateEntityException;

/**
 * Implementación del servicio de negocio para la entidad Paciente (Entidad
 * "A").
 * <p>
 * Esta clase es la <b>Capa de Lógica de Negocio</b> (Service Layer) para
 * <code>Paciente</code>. Se sitúa entre la capa de UI
 * ({@link main.MenuHandler}) y la capa de Datos ({@link dao.PacienteDAO}).
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li><b>Validación de Reglas de Negocio (RN):</b> Llama a
 * {@link #validateEntity(Paciente)} antes de cualquier <code>insert</code> o
 * <code>update</code> para asegurar la integridad de los datos (campos
 * obligatorios, formatos, rangos).</li>
 * <li><b>Garantizar Unicidad:</b> Llama a {@link #validateDniUnique} para
 * asegurar que no existan DNI duplicados (RN-002).</li>
 * <li><b>Orquestación de Entidades:</b> Coordina operaciones que involucran a
 * más de una entidad. Por ejemplo, en <code>insert</code>, se asegura de que
 * <code>HistoriaClinica</code> se inserte <b>antes</b> que
 * <code>Paciente</code> para obtener la FK.</li>
 * <li><b>Lógica de Cascada (Soft Delete):</b> Implementa la lógica de negocio
 * de que al eliminar (<code>delete</code>) o recuperar (<code>recover</code>)
 * un <code>Paciente</code>, su <code>HistoriaClinica</code> asociada también se
 * elimine o recupere (RN-013, RN-031).</li>
 * <li><b>Lógica de "Eliminación Segura":</b> Provee
 * {@link #deleteHistoriaClinica(int, int)} como la forma segura de desasociar
 * una HC, previniendo referencias huérfanas (HU-008).</li>
 * </ul>
 *
 * <h3>Transacciones:</h3>
 * <p>
 * Ahora implementa manejo de transacciones reales:
 * <ul>
 * <li>Ambas entidades (Paciente y su HistoriaClinica) se insertan/actualizan en
 * una misma conexión con <b>commit/rollback</b>.</li>
 * <li>Si alguna operación falla, se revierte todo (atomicidad garantizada).</li>
 * </ul>
 * </p>
 *
 * @author alpha
 * @see GenericService
 * @see models.Paciente
 * @see dao.PacienteDAO
 * @see HistoriaClinicaService
 */
public class PacienteService implements GenericService<Paciente> {

    /**
     * DAO para acceso a datos de pacientes.
     * Inyectado en el constructor (Dependency Injection).
     */
    private final PacienteDAO pacienteDAO;

    /**
     * Servicio de negocio para la entidad Historia Clínica.
     * <p>
     * Este servicio (Paciente) necesita coordinar con el
     * servicio de HC para las siguientes lógicas de negocio:
     * </p>
     * <ul>
     * <li>Insert/Update: Crear/actualizar la HC (Entidad B)
     * <b>antes</b> de crear/actualizar el Paciente (Entidad A).</li>
     * <li>Delete (RN-013): Eliminar la HC asociada <b>antes</b> de eliminar el Paciente.</li>
     * <li>Recover (RN-031): Recuperar la HC asociada <b>antes</b> de recuperar el Paciente.</li>
     * </ul>
     */
    private final HistoriaClinicaService historiaClinicaService;

    /**
     * Constructor que inyecta las dependencias necesarias.
     * <p>
     * Implementa la <b>Inyección de Dependencias</b> (DI) manual.
     * Valida que ambas dependencias no sean nulas (principio Fail-Fast).
     * </p>
     *
     * @param pacienteDAO            El objeto DAO de Paciente para acceder a la
     *                               tabla <code>Paciente</code>.
     * @param historiaClinicaService El servicio de Historia Clínica para
     *                               operaciones coordinadas.
     * @throws IllegalArgumentException Si alguna dependencia es nula.
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

    // =============================================================
    // MÉTODOS CRUD (Escritura)
    // =============================================================

    /**
     * {@inheritDoc}
     * <p>
     * Inserta un nuevo paciente, asegurando validaciones,
     * unicidad de DNI y atomicidad con su historia clínica.
     * </p>
     */
    @Override
    public void insert(Paciente paciente)
            throws ServiceException, ValidationException, DuplicateEntityException {
        validateEntity(paciente);
        validateDniUnique(paciente.getDni(), null);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (paciente.getHistoriaClinica() != null) {
                    if (paciente.getHistoriaClinica().getId() == 0) {
                        historiaClinicaService.insert(paciente.getHistoriaClinica());
                    } else {
                        historiaClinicaService.update(paciente.getHistoriaClinica());
                    }
                }
                pacienteDAO.insertTx(paciente, conn);
                conn.commit();

            } catch (Exception ex) {
                conn.rollback();
                throw new ServiceException("Error transaccional al insertar Paciente+HC: " + ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al insertar el paciente: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Actualiza un paciente existente con validaciones, unicidad y transacción.
     * </p>
     */
    @Override
    public void update(Paciente paciente)
            throws ServiceException, ValidationException, DuplicateEntityException {
        if (paciente.getId() <= 0) {
            throw new ValidationException("El ID del paciente debe ser mayor a 0 para actualizar.");
        }

        validateEntity(paciente);
        validateDniUnique(paciente.getDni(), paciente.getId());

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (paciente.getHistoriaClinica() != null) {
                    if (paciente.getHistoriaClinica().getId() == 0) {
                        historiaClinicaService.insert(paciente.getHistoriaClinica());
                    } else {
                        historiaClinicaService.update(paciente.getHistoriaClinica());
                    }
                }
                pacienteDAO.updateTx(paciente, conn);
                conn.commit();

            } catch (Exception ex) {
                conn.rollback();
                throw new ServiceException("Error transaccional al actualizar Paciente+HC: " + ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al actualizar el paciente: " + e.getMessage(), e);
        }
    }

    // =============================================================
    // BAJA Y RECUPERACIÓN LÓGICA (CASCADA)
    // =============================================================

    /**
     * {@inheritDoc}
     * <p>
     * Lógica de Cascada (RN-013): elimina paciente y su HC asociada.
     * </p>
     */
    @Override
    public void delete(int id) throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("El ID del paciente debe ser mayor a cero.");
        }
        try {
            Paciente paciente = pacienteDAO.selectByIdWithStatus(id, false);
            if (paciente == null) return;

            if (paciente.getHistoriaClinica() != null) {
                historiaClinicaService.delete(paciente.getHistoriaClinica().getId());
            }
            pacienteDAO.delete(id);
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar el paciente: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Lógica de Cascada (RN-031): recupera paciente y su HC asociada.
     * </p>
     */
    @Override
    public void recover(int id) throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("El ID del paciente debe ser mayor a cero.");
        }
        try {
            Paciente paciente = pacienteDAO.selectByIdWithStatus(id, true);
            if (paciente == null) return;

            if (paciente.getHistoriaClinica() != null) {
                historiaClinicaService.recover(paciente.getHistoriaClinica().getId());
            }
            pacienteDAO.recover(id);
        } catch (SQLException e) {
            throw new ServiceException("Error al recuperar el paciente: " + e.getMessage(), e);
        }
    }

    // =============================================================
    // MÉTODOS SELECT (Lectura)
    // =============================================================

    @Override
    public Paciente selectById(int id, boolean deleted)
            throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("El ID del paciente debe ser mayor a cero.");
        }
        try {
            return pacienteDAO.selectByIdWithStatus(id, deleted);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener paciente por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Paciente> selectAll(boolean deleted) throws ServiceException {
        try {
            return pacienteDAO.selectAllWithStatus(deleted);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener todos los pacientes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Paciente> searchByFilter(String filter)
            throws ServiceException, ValidationException {
        if (filter == null || filter.trim().isEmpty()) {
            throw new ValidationException("El filtro de búsqueda no puede estar vacío.");
        }
        try {
            return pacienteDAO.searchByFilter(filter);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar pacientes: " + e.getMessage(), e);
        }
    }

    public Paciente selectByDni(String dni)
            throws ServiceException, ValidationException {
        if (dni == null || dni.trim().isEmpty()) {
            throw new ValidationException("El DNI no puede ser nulo o vacío.");
        }
        try {
            return pacienteDAO.selectByDni(dni);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener paciente por DNI: " + e.getMessage(), e);
        }
    }

    // =============================================================
    // VALIDACIONES DE NEGOCIO
    // =============================================================

    @Override
    public void validateEntity(Paciente paciente) throws ValidationException {
        if (paciente == null) {
            throw new ValidationException("El objeto Paciente no puede ser nulo.");
        }

        // --- Nombre ---
        if (paciente.getNombre() == null || paciente.getNombre().trim().isEmpty()) {
            throw new ValidationException("El nombre del paciente no puede estar vacío.");
        }
        if (!paciente.getNombre().matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            throw new ValidationException("El nombre solo puede contener letras y espacios.");
        }

        // --- Apellido ---
        if (paciente.getApellido() == null || paciente.getApellido().trim().isEmpty()) {
            throw new ValidationException("El apellido del paciente no puede estar vacío.");
        }
        if (!paciente.getApellido().matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            throw new ValidationException("El apellido solo puede contener letras y espacios.");
        }

        // --- DNI ---
        String dni = paciente.getDni();
        if (dni == null || dni.trim().isEmpty()) {
            throw new ValidationException("El DNI no puede estar vacío.");
        }
        dni = dni.replaceAll("[ .-]", "");
        if (!dni.matches("^[0-9]{7,15}$")) {
            throw new ValidationException(
                    "El DNI debe tener solo números (7–15 dígitos), sin puntos ni guiones (ej: 36200193).");
        }
        paciente.setDni(dni); // normaliza el valor

        // --- Fecha de nacimiento ---
        LocalDate fechaNac = paciente.getFechaNacimiento();
        if (fechaNac == null) {
            throw new ValidationException("La fecha de nacimiento no puede ser nula.");
        }

        LocalDate fechaMin = LocalDate.of(1900, Month.JANUARY, 1);
        LocalDate fechaMax = LocalDate.now();

        if (fechaNac.isAfter(fechaMax) || fechaNac.isBefore(fechaMin)) {
            throw new ValidationException("La fecha de nacimiento no es válida (debe ser entre 1900 y hoy).");
        }
    }

    private void validateDniUnique(String dni, Integer pacienteId)
            throws DuplicateEntityException, ServiceException {
        try {
            Paciente existente = pacienteDAO.selectByDni(dni);
            if (existente != null) {
                if (pacienteId == null || existente.getId() != pacienteId) {
                    throw new DuplicateEntityException("Ya existe un paciente registrado con el DNI " + dni + ".");
                }
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al validar unicidad de DNI: " + e.getMessage(), e);
        }
    }

    // =============================================================
    // ELIMINACIÓN SEGURA (HU-008)
    // =============================================================

    public void deleteHistoriaClinica(int pacienteId, int historiaClinicaId)
            throws ServiceException, ValidationException {
        if (pacienteId <= 0 || historiaClinicaId <= 0) {
            throw new ValidationException("Los IDs deben ser mayores a 0.");
        }

        try {
            Paciente paciente = pacienteDAO.selectByIdWithStatus(pacienteId, false);
            if (paciente == null) {
                throw new ValidationException("Paciente no encontrado con ID: " + pacienteId);
            }
            if (paciente.getHistoriaClinica() == null
                    || paciente.getHistoriaClinica().getId() != historiaClinicaId) {
                throw new ValidationException("La Historia Clínica no pertenece a este paciente.");
            }

            paciente.setHistoriaClinica(null);
            pacienteDAO.update(paciente);
            historiaClinicaService.delete(historiaClinicaId);

        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar historia clínica de forma segura: " + e.getMessage(), e);
        }
    }
}
