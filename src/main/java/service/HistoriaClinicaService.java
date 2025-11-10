package service;

import dao.HistoriaClinicaDAO;
import java.sql.SQLException;
import java.util.List;
import models.HistoriaClinica;
import exceptions.ServiceException;
import exceptions.ValidationException;
import exceptions.DuplicateEntityException;

/**
 * Implementación del servicio de negocio para la entidad HistoriaClinica
 * (Entidad "B").
 * <p>
 * Esta clase es la <b>Capa de Lógica de Negocio</b> (Service Layer) para
 * <code>HistoriaClinica</code>. Se sitúa entre la capa de UI
 * ({@link main.MenuHandler}) y la capa de Datos
 * ({@link dao.HistoriaClinicaDAO}).
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li><b>Validación de Reglas de Negocio (RN):</b> Es la <b>responsabilidad
 * principal</b>. Llama a {@link #validateEntity(HistoriaClinica)} (RN-016,
 * RN-017) y {@link #validateNroHistoriaUnique} (RN-015) antes de cualquier
 * operación de escritura.</li>
 * <li><b>Abstracción del DAO:</b> Actúa como intermediario, ocultando los
 * detalles de la persistencia.</li>
 * <li><b>NO coordina</b> con otros servicios (a diferencia de
 * <code>PacienteService</code>), ya que es la entidad "pasiva" en la relación
 * 1-a-1.</li>
 * </ul>
 *
 * @author alpha
 * @see GenericService
 * @see models.HistoriaClinica
 * @see dao.HistoriaClinicaDAO
 * @see PacienteService
 */
public class HistoriaClinicaService implements GenericService<HistoriaClinica> {

    /**
     * DAO para acceso a datos de Historias Clínicas.
     */
    private final HistoriaClinicaDAO historiaClinicaDAO;

    /**
     * Constructor que inyecta la dependencia del DAO.
     *
     * @param historiaClinicaDAO El objeto DAO concreto de HistoriaClinica.
     * @throws IllegalArgumentException Si la dependencia es nula.
     */
    public HistoriaClinicaService(HistoriaClinicaDAO historiaClinicaDAO) {
        if (historiaClinicaDAO == null) {
            throw new IllegalArgumentException("El DAO de Historia Clínica no puede ser nulo.");
        }
        this.historiaClinicaDAO = historiaClinicaDAO;
    }

    // =============================================================
    // MÉTODOS CRUD (Escritura)
    // =============================================================

    @Override
    public void insert(HistoriaClinica historia)
            throws ServiceException, ValidationException, DuplicateEntityException {
        try {
            validateEntity(historia);
            validateNroHistoriaUnique(historia.getNumeroHistoria(), null); // INSERT
            historiaClinicaDAO.insert(historia);
        } catch (DuplicateEntityException e) {
            throw e;
        } catch (ValidationException e) {
            throw e;
        } catch (SQLException e) {
            throw new ServiceException("Error al insertar la historia clínica: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ServiceException("Error inesperado al insertar historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(HistoriaClinica historia)
            throws ServiceException, ValidationException, DuplicateEntityException {
        if (historia.getId() <= 0) {
            throw new ValidationException("El ID de la historia clínica debe ser mayor a cero.");
        }

        try {
            validateEntity(historia);
            validateNroHistoriaUnique(historia.getNumeroHistoria(), historia.getId()); // UPDATE
            historiaClinicaDAO.update(historia);
        } catch (DuplicateEntityException e) {
            throw e;
        } catch (ValidationException e) {
            throw e;
        } catch (SQLException e) {
            throw new ServiceException("Error al actualizar la historia clínica: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ServiceException("Error inesperado al actualizar historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("El ID de la historia clínica debe ser mayor a cero.");
        }
        try {
            historiaClinicaDAO.delete(id);
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar la historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public void recover(int id) throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("El ID de la historia clínica debe ser mayor a cero.");
        }
        try {
            historiaClinicaDAO.recover(id);
        } catch (SQLException e) {
            throw new ServiceException("Error al recuperar la historia clínica: " + e.getMessage(), e);
        }
    }

    // =============================================================
    // MÉTODOS SELECT (Lectura)
    // =============================================================

    @Override
    public HistoriaClinica selectById(int id, boolean deleted)
            throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("El ID de la historia clínica debe ser mayor a cero.");
        }
        try {
            return historiaClinicaDAO.selectByIdWithStatus(id, deleted);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener historia clínica por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<HistoriaClinica> selectAll(boolean deleted) throws ServiceException {
        try {
            return historiaClinicaDAO.selectAllWithStatus(deleted);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener todas las historias clínicas: " + e.getMessage(), e);
        }
    }

    @Override
    public List<HistoriaClinica> searchByFilter(String filter)
            throws ServiceException, ValidationException {
        if (filter == null || filter.trim().isEmpty()) {
            throw new ValidationException("El filtro de búsqueda no puede estar vacío.");
        }
        try {
            return historiaClinicaDAO.searchByFilter(filter);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar historias clínicas: " + e.getMessage(), e);
        }
    }

    public HistoriaClinica selectByNroHistoria(String nroHistoria)
            throws ServiceException, ValidationException {
        if (nroHistoria == null || nroHistoria.trim().isEmpty()) {
            throw new ValidationException("El Nro. de Historia no puede ser nulo o vacío.");
        }
        try {
            return historiaClinicaDAO.selectByNroHistoria(nroHistoria);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener la HC por número: " + e.getMessage(), e);
        }
    }

    // =============================================================
    // VALIDACIONES DE NEGOCIO
    // =============================================================

    @Override
    public void validateEntity(HistoriaClinica historia) throws ValidationException {
        if (historia == null) {
            throw new ValidationException("La historia clínica no puede ser nula.");
        }

        String nro = historia.getNumeroHistoria();
        if (nro == null || nro.trim().isEmpty()) {
            throw new ValidationException("El número de historia no puede estar vacío.");
        }
        if (!nro.matches("^HC-[0-9]{4,17}$")) {
            throw new ValidationException("El número de historia debe tener el formato 'HC-####' (ej: HC-000123).");
        }

        if (historia.getGrupoSanguineo() == null) {
            throw new ValidationException("Debe asignarse un grupo sanguíneo válido.");
        }
    }

    private void validateNroHistoriaUnique(String nroHistoria, Integer historiaId)
            throws DuplicateEntityException, ServiceException {
        try {
            HistoriaClinica existente = historiaClinicaDAO.selectByNroHistoria(nroHistoria);
            if (existente != null) {
                if (historiaId == null || existente.getId() != historiaId) {
                    throw new DuplicateEntityException(
                            "Ya existe una Historia Clínica con el número: " + nroHistoria);
                }
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al validar unicidad de número de historia: " + e.getMessage(), e);
        }
    }
}
