package service;

import dao.HistoriaClinicaDAO;
import java.sql.SQLException;
import java.util.List;
import models.HistoriaClinica;

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
 * @author alpha team
 * @see GenericService
 * @see models.HistoriaClinica
 * @see dao.HistoriaClinicaDAO
 * @see PacienteService
 */
public class HistoriaClinicaService implements GenericService<HistoriaClinica> {

    /**
     * DAO para acceso a datos de Historias Clínicas.
     * <p>
     * <b>Nota:</b> Se usa la clase concreta {@link HistoriaClinicaDAO} en lugar de
     * la interfaz <code>GenericDAO</code>. Esto es <b>necesario</b> para poder
     * llamar a métodos específicos como <code>selectByNroHistoria()</code>, que es
     * vital para la validación de unicidad (RN-015).
     * </p>
     */
    private final HistoriaClinicaDAO historiaClinicaDAO;

    /**
     * Constructor que inyecta la dependencia del DAO.
     * <p>
     * Implementa la <b>Inyección de Dependencias</b> (DI) manual.
     * Valida que el DAO no sea nulo (principio Fail-Fast).
     * </p>
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

    // ============ MÉTODOS CRUD (Escritura) ============
    /**
     * {@inheritDoc}
     * <p>
     * <b>Flujo de Validación:</b>
     * <br>
     * 1. Llama a {@link #validateEntity(HistoriaClinica)} (RN-016, RN-017).
     * <br>
     * 2. Llama a {@link #validateNroHistoriaUnique(String, Integer)} (RN-015).
     * <br>
     * 3. Delega la inserción a <code>historiaClinicaDAO.insert()</code>.
     * </p>
     * 
     * @param historia La entidad HistoriaClinica a insertar.
     * @throws Exception Si falla alguna validación o la operación de
     */
    @Override
    public void insert(HistoriaClinica historia) throws Exception {

        // 1. Validaciones de Reglas de Negocio
        validateEntity(historia);
        validateNroHistoriaUnique(historia.getNumeroHistoria(), null); // null para INSERT

        try {
            System.out.println("Insertando historia clinica ID: " + historia.getId());

            // 2. Persistencia
            historiaClinicaDAO.insert(historia);

        } catch (SQLException e) {
            // Re-lanzar como una excepción genérica de servicio
            throw new SQLException("Error al insertar la historia clínica: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Flujo de Validación:</b>
     * <br>
     * 1. Valida que <code>historia.getId() > 0</code>.
     * <br>
     * 2. Llama a {@link #validateEntity(HistoriaClinica)} (RN-016, RN-017).
     * <br>
     * 3. Llama a {@link #validateNroHistoriaUnique(String, Integer)} (RN-015),
     * permitiendo que el <code>nroHistoria</code> pertenezca al ID de la historia
     * actual.
     * <br>
     * 4. Delega la actualización a <code>historiaClinicaDAO.update()</code>.
     * </p>
     * 
     * @param historia La entidad HistoriaClinica a actualizar.
     * @throws Exception Si falla alguna validación o la operación de
     */
    @Override
    public void update(HistoriaClinica historia) throws Exception {

        // 1. Validaciones de Reglas de Negocio
        if (historia.getId() <= 0) {
            throw new IllegalArgumentException("El ID de la historia clínica debe ser mayor a cero.");
        }

        validateEntity(historia);
        validateNroHistoriaUnique(historia.getNumeroHistoria(), historia.getId()); // ID para UPDATE

        try {
            System.out.println("Actualizando historia clinica ID: " + historia.getId());

            // 2. Persistencia
            historiaClinicaDAO.update(historia);

        } catch (SQLException e) {
            // Re-lanzar como una excepción genérica de servicio
            throw new SQLException("Error al actualizar la historia clínica: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Nota de Diseño (HU-007):</b> Este método elimina una HC independientemente
     * de si un <code>Paciente</code> la está referenciando.
     * Esto puede crear una <b>referencia huérfana</b> (un <code>Paciente</code>
     * apuntando a una HC eliminada).
     * <br>
     * La lógica de "Eliminación Segura" (HU-008) está implementada en
     * {@link PacienteService#deleteHistoriaClinica(int, int)}.
     * </p>
     * 
     * @param id El ID de la historia clínica a eliminar.
     * @throws Exception Si ocurre un error durante la operación de eliminación.
     */
    @Override
    public void delete(int id) throws Exception {

        if (id <= 0) {
            throw new IllegalArgumentException("El ID de la historia clínica debe ser mayor a cero.");
        }

        try {
            System.out.println("Eliminando historia clinica con ID: " + id);

            historiaClinicaDAO.delete(id);

        } catch (SQLException e) {
            throw new SQLException("Error al eliminar la historia clínica: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Nota de Diseño (RN-031):</b> Este método solo recupera la HC.
     * La lógica de "Recuperación en Cascada" (recuperar Paciente Y su HC) está
     * implementada en {@link PacienteService#recover(int)}.
     * </p>
     * 
     * @param id El ID de la historia clínica a recuperar.
     * @throws Exception Si ocurre un error durante la operación de recuperación.
     */
    @Override
    public void recover(int id) throws Exception {

        if (id <= 0) {
            throw new IllegalArgumentException("El ID de la historia clínica debe ser mayor a cero.");
        }

        try {
            System.out.println("Recuperando historia clinica con ID: " + id);

            historiaClinicaDAO.recover(id);

        } catch (SQLException e) {
            throw new SQLException("Error al recuperar la historia clínica: " + e.getMessage(), e);
        }
    }

    // ============ MÉTODOS SELECT (Lectura) ============
    /**
     * {@inheritDoc}
     * <p>
     * Busca una historia clínica por su ID, considerando su estado (deleted o
     * activa).
     * </p>
     *
     * @param id      El ID de la historia clínica a buscar.
     * @param deleted <code>false</code> para activa.
     *                <code>true</code> para eliminada.
     * @return HistoriaClinica encontrada o <code>null</code>.
     * @throws Exception Si ID <= 0 o si falla la consulta
     *                   (<code>SQLException</code>).
     */
    @Override
    public HistoriaClinica selectById(int id, boolean deleted) throws Exception {

        if (id <= 0) {
            throw new IllegalArgumentException("El ID de la historia clínica debe ser mayor a cero.");
        }

        try {
            System.out.println("\nObteniendo historia clinica con ID: " + id);
            return historiaClinicaDAO.selectByIdWithStatus(id, deleted);

        } catch (SQLException e) {
            throw new SQLException("Error al obtener la historia clínica por ID: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Obtiene todas las historias clínicas de la base de datos.
     * </p>
     *
     * @param eliminado <code>false</code> para activas.
     *                  <code>true</code> para eliminadas.
     * @return Lista de historias clínicas (puede estar vacía).
     * @throws Exception Si falla la consulta (<code>SQLException</code>).
     */
    @Override
    public List<HistoriaClinica> selectAll(boolean deleted) throws Exception {

        try {
            List<HistoriaClinica> historiasClinicas = historiaClinicaDAO.selectAllWithStatus(deleted);

            String tipo = deleted ? "eliminadas" : "activas";

            System.out.println("\n=========================================");
            System.out.println(historiasClinicas.size() + " Historia Clínicas " + tipo + " encontradas.");
            System.out.println("=========================================");

            return historiasClinicas;

        } catch (SQLException e) {
            throw new SQLException("Error al obtener todas las historias clínicas: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Busca historias clínicas que coincidan con el filtro en los campos:
     * <code>numeroHistoria</code>, <code>grupoSanguineo</code>,
     * <code>antecedentesMedicos</code>, <code>medicacionActual</code>,
     * <code>observaciones</code>.
     * </p>
     *
     * @param filter Cadena de texto para filtrar.
     * @return Lista de historias clínicas (puede estar vacía).
     * @throws Exception Si el filtro es nulo/vacío
     *                   (<code>IllegalArgumentException</code>)
     *                   o si falla la consulta (<code>SQLException</code>).
     */
    @Override
    public List<HistoriaClinica> searchByFilter(String filter) throws Exception {

        if (filter == null || filter.trim().isEmpty()) {
            throw new IllegalArgumentException("El filtro de búsqueda no puede estar vacío");
        }

        try {
            List<HistoriaClinica> historiasClinicas = historiaClinicaDAO.searchByFilter(filter);

            System.out.println("\n=========================================");
            System.out.println(historiasClinicas.size() + " Historia Clínicas encontrados.");
            System.out.println("=========================================");

            return historiasClinicas;

        } catch (SQLException e) {
            throw new SQLException(
                    "Error al buscar historias clínicas por Grupo Sanguíneo, Antecedentes, Medicación Actual u Observaciones: "
                            + e.getMessage(),
                    e);
        }
    }

    /**
     * Busca una HistoriaClinica <b>activa</b> por su <code>nroHistoria</code>
     * (coincidencia exacta).
     * <p>
     * Este es un método de conveniencia para la UI
     * ({@link views.historias.HistoriaMenu}),
     * que envuelve la llamada al DAO con validación.
     * </p>
     *
     * @param nroHistoria El número de historia exacto a buscar.
     * @return HistoriaClinica encontrada o <code>null</code> si no existe.
     * @throws Exception Si el <code>nroHistoria</code> es nulo/vacío
     *                   (<code>IllegalArgumentException</code>) o si falla la
     *                   consulta
     *                   (<code>SQLException</code>).
     */
    public HistoriaClinica selectByNroHistoria(String nroHistoria) throws Exception {

        if (nroHistoria == null || nroHistoria.trim().isEmpty()) {
            throw new IllegalArgumentException("El Nro. de Historia no puede ser nulo o vacío.");
        }

        try {
            System.out.println("\n=========================================");
            System.out.println("Obteniendo HC con Nro: " + nroHistoria);
            System.out.println("=========================================");

            // Llama al método específico del DAO
            return historiaClinicaDAO.selectByNroHistoria(nroHistoria);

        } catch (SQLException e) {
            throw new SQLException("Error al obtener la HC por Nro. de Historia: " + e.getMessage(), e);
        }
    }

    // ============ MÉTODOS DE VALIDACIÓN (RN) ============
    /**
     * {@inheritDoc}
     * <p>
     * Esta es la implementación de las <b>Reglas de Negocio (RN)</b> para una
     * <code>HistoriaClinica</code>.
     * </p>
     *
     * <h3>Validaciones Ejecutadas:</h3>
     * <ul>
     * <li><b>RN-016.1:</b> HistoriaClinica no puede ser <code>null</code>.</li>
     * <li><b>RN-016.2:</b> <code>numeroHistoria</code> no puede ser
     * <code>null</code> o vacío (<code>isBlank</code>).</li>
     * <li><b>RN-016.3:</b> <code>grupoSanguineo</code> no puede ser
     * <code>null</code>.</li>
     * <li><b>RN-017 (Formato):</b> <code>numeroHistoria</code> debe cumplir con el
     * formato regex <code>^HC-[0-9]{4,17}$</code>.</li>
     * </ul>
     * <p>
     * <b>Nota:</b> La validación de unicidad (RN-015) se realiza por separado en
     * {@link #validateNroHistoriaUnique(String, Integer)}.
     * </p>
     *
     * @param historia La entidad HistoriaClinica a validar.
     * @throws IllegalArgumentException Si alguna regla de negocio es violada.
     */
    @Override
    public void validateEntity(HistoriaClinica historia) throws Exception {

        if (historia == null) {
            throw new IllegalArgumentException("La historia clínica no puede ser nula.");
        }

        // RN-016.2: Validación de nroHistoria (Existencia)
        if (historia.getNumeroHistoria() == null || historia.getNumeroHistoria().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de historia no puede estar vacío.");
        }

        // RN-017: Validación de nroHistoria (Formato)
        String regex = "^HC-[0-9]{4,17}$";
        if (!historia.getNumeroHistoria().matches(regex)) {
            throw new IllegalArgumentException("El número de historia no es válido (ej: 'HC-123456').");
        }

        // RN-016.3: Validación de grupoSanguineo (Existencia)
        if (historia.getGrupoSanguineo() == null) {
            throw new IllegalArgumentException("El grupo sanguíneo no puede estar vacío.");
        }

        // Los campos antecedentes, medicacionActual y observaciones son opcionales
        // (pueden ser nulos o vacíos), por lo que no se validan aquí.
    }

    /**
     * Valida que un <code>nroHistoria</code> sea único en el sistema (<b>Regla de
     * Negocio RN-015</b>).
     * <p>
     * Este método es crucial para prevenir la duplicidad de datos y es llamado por
     * <code>insert()</code> y <code>update()</code>.
     * </p>
     *
     * <h3>Lógica de Validación:</h3>
     * <ol>
     * <li>Busca en el DAO si ya existe una HC con ese <code>nroHistoria</code>
     * (<code>historiaClinicaDAO.selectByNroHistoria()</code>).</li>
     * <li><b>Si NO existe (<code>existente == null</code>):</b> Es único, la
     * validación pasa.</li>
     * <li><b>Si existe (<code>existente != null</code>):</b>
     * <ul>
     * <li><b>Caso INSERT:</b> <code>historiaId</code> es <code>null</code>. Es un
     * duplicado. Lanza <code>IllegalArgumentException</code>.</li>
     * <li><b>Caso UPDATE:</b> <code>historiaId</code> <b>no</b> es
     * <code>null</code>.
     * <ul>
     * <li>Si el ID encontrado (<code>existente.getId()</code>) es <b>diferente</b>
     * al ID que estamos actualizando (<code>historiaId</code>), el
     * <code>nroHistoria</code> pertenece a <b>otra</b> HC. Lanza
     * <code>IllegalArgumentException</code>.</li>
     * <li>Si el ID encontrado es <b>igual</b>, la HC está conservando su propio
     * <code>nroHistoria</code>. La validación pasa.</li>
     * </ul>
     * </li>
     * </ul>
     * </ol>
     *
     * @param nroHistoria El número de historia (String) a validar.
     * @param historiaId  El ID de la HC que se está <code>UPDATE</code>ando
     *                    (<b>importante:</b> debe ser <code>null</code> si es
     *                    un <code>INSERT</code>).
     * @throws IllegalArgumentException Si el <code>nroHistoria</code> ya
     *                                  está en uso por <i>otra</i> HC.
     * @throws Exception                Si ocurre un error de
     *                                  <code>SQLException</code> durante la
     *                                  consulta.
     */
    private void validateNroHistoriaUnique(String nroHistoria, Integer historiaId) throws Exception {

        HistoriaClinica existente = historiaClinicaDAO.selectByNroHistoria(nroHistoria);

        if (existente != null) {
            // Existe una HC con ese nroHistoria.
            // Ahora debemos verificar si es "esta" HC o "otra" HC.

            if (historiaId == null || existente.getId() != historiaId) {
                throw new IllegalArgumentException("Ya existe una Historia Clínica con el número: " + nroHistoria);
            }
        }
        // Si 'existente' es null, el nroHistoria está disponible. Validación OK.
    }

}