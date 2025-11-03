package service;

import dao.PacienteDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import models.Paciente;

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
 * <b>Nota:</b> Esta implementación <b>no</b> utiliza un
 * {@link config.TransactionManager} explícito, por lo que cada operación de DAO
 * (ej: `historiaClinicaService.insert` y `pacienteDAO.insert`) se ejecuta en su
 * propia transacción (auto-commit).
 * </p>
 * <p>
 * Para un TPI que requiera transacciones atómicas (ej: si falla
 * `pacienteDAO.insert`, que se revierta `historiaClinicaService.insert`), estos
 * métodos deberían ser refactorizados para usar <code>TransactionManager</code>
 * y llamar a los métodos <code>...Tx()</code> del DAO.
 * </p>
 *
 * @author alpha team
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
     * <b>IMPORTANTE:</b> Este servicio (Paciente) necesita coordinar con el
     * servicio de HC para las siguientes lógicas de negocio:
     * </p>
     * <ul>
     * <li><b>Insert/Update:</b> Crear/actualizar la HC (Entidad B) <b>antes</b> de
     * crear/actualizar el Paciente (Entidad A).</li>
     * <li><b>Delete (RN-013):</b> Eliminar la HC asociada <b>antes</b> de eliminar
     * el Paciente.</li>
     * <li><b>Recover (RN-031):</b> Recuperar la HC asociada <b>antes</b> de
     * recuperar el Paciente.</li>
     * <li><b>Safe Delete (HU-008):</b> Desasociar la HC
     * (<code>UPDATE Paciente</code>) <b>antes</b> de eliminar la HC.</li>
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

    // ============ MÉTODOS CRUD (Escritura) ============
    /**
     * {@inheritDoc}
     * <p>
     * <b>Flujo de Coordinación (RN-004):</b>
     * <br>
     * 1. Llama a {@link #validateEntity(Paciente)} (RN-001).
     * <br>
     * 2. Llama a {@link #validateDniUnique(String, Integer)} (RN-002).
     * <br>
     * 3. Si <code>paciente.getHistoriaClinica()</code> existe y es nueva
     * (<code>id=0</code>), llama a <code>historiaClinicaService.insert()</code>
     * <b>primero</b> para obtener el nuevo ID de la HC.
     * <br>
     * 4. Llama a <code>pacienteDAO.insert()</code> para persistir al paciente con
     * la FK <code>historia_clinica_id</code> correcta.
     * </p>
     *
     * @param paciente El objeto Paciente a insertar (debe tener ID=0).
     * @throws Exception Si la validación falla, el DNI está duplicado, o hay error
     *                   de BD.
     */
    @Override
    public void insert(Paciente paciente) throws Exception {

        // 1. Validaciones de Reglas de Negocio
        validateEntity(paciente);
        validateDniUnique(paciente.getDni(), null); // null para INSERT

        try {
            System.out.println("Insertando paciente: " + paciente.getApellido() + ", " + paciente.getNombre());

            // 2. Orquestación de Entidades (Insertar B antes que A)
            if (paciente.getHistoriaClinica() != null) {
                if (paciente.getHistoriaClinica().getId() == 0) {
                    // Nueva historia clínica: insertar primero para obtener ID autogenerado
                    historiaClinicaService.insert(paciente.getHistoriaClinica());

                } else {
                    // (Caso raro) HC ya existe: se actualiza
                    historiaClinicaService.update(paciente.getHistoriaClinica());
                }
            }

            // 3. Persistir Paciente (A)
            pacienteDAO.insert(paciente);

        } catch (Exception e) {
            // Re-lanzar como una excepción genérica de servicio
            throw new Exception("Error al insertar el paciente: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Flujo de Coordinación:</b>
     * <br>
     * 1. Valida que <code>paciente.getId() > 0</code>.
     * <br>
     * 2. Llama a {@link #validateEntity(Paciente)} (RN-001).
     * <br>
     * 3. Llama a {@link #validateDniUnique(String, Integer)} (RN-002), permitiendo
     * que el DNI pertenezca al ID del paciente actual.
     * <br>
     * 4. Orquesta la inserción/actualización de la <code>HistoriaClinica</code>
     * (igual que en <code>insert</code>).
     * <br>
     * 5. Llama a <code>pacienteDAO.update()</code>.
     * </p>
     *
     * @param paciente El objeto Paciente a actualizar (debe tener ID > 0).
     * @throws Exception Si la validación falla, el DNI está duplicado, o hay error
     */
    @Override
    public void update(Paciente paciente) throws Exception {

        // 1. Validaciones de Reglas de Negocio
        if (paciente.getId() <= 0) {
            throw new IllegalArgumentException("El ID del paciente debe ser mayor a 0 para actualizar");
        }

        validateEntity(paciente);
        validateDniUnique(paciente.getDni(), paciente.getId()); // ID para UPDATE

        try {
            System.out.println("Actualizando paciente: " + paciente.getApellido() + ", " + paciente.getNombre());

            // 2. Orquestación de Entidades
            if (paciente.getHistoriaClinica() != null) {
                if (paciente.getHistoriaClinica().getId() == 0) {
                    // Nueva historia clínica: insertar primero para obtener ID autogenerado
                    historiaClinicaService.insert(paciente.getHistoriaClinica());

                } else {
                    // La historia clínica ya existe, se actualiza
                    historiaClinicaService.update(paciente.getHistoriaClinica());
                }
            }

            // 3. Persistir Paciente (A)
            pacienteDAO.update(paciente);

        } catch (Exception e) {
            // Re-lanzar como una excepción genérica de servicio
            throw new Exception("Error al actualizar la paciente: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Lógica de Cascada (RN-013):</b>
     * <br>
     * 1. Valida <code>id > 0</code>.
     * <br>
     * 2. Busca al paciente <b>activo</b> (<code>eliminado=false</code>) usando
     * <code>pacienteDAO.selectByIdWithStatus(id, false)</code>.
     * <br>
     * 3. Si el paciente tiene una <code>HistoriaClinica</code> (HC) asociada:
     * <br>
     * Llama a <code>historiaClinicaService.delete(id)</code>
     * para eliminar la HC (Entidad B) <b>primero</b>.
     * <br>
     * 4. Llama a <code>pacienteDAO.delete(id)</code> para eliminar el Paciente
     * (Entidad A).
     * </p>
     *
     * @param id ID del paciente a eliminar.
     * @throws Exception Si el ID es inválido o hay error de BD.
     */
    @Override
    public void delete(int id) throws Exception {

        if (id <= 0) {
            throw new IllegalArgumentException("El ID del paciente debe ser mayor a cero.");
        }

        try {
            // 1. Buscar al paciente ACTIVO que se desea eliminar
            Paciente paciente = pacienteDAO.selectByIdWithStatus(id, false);

            if (paciente == null) {
                System.out.println("Paciente con ID " + id + " no encontrado (o ya estaba eliminado).");
                return;
            }

            // 2. Eliminar la HC asociada (RN-013)
            if (paciente.getHistoriaClinica() != null) {
                // Obtener el ID de la historia clínica
                int historiaId = paciente.getHistoriaClinica().getId();

                System.out.println("Eliminando historia clínica asociada con ID: " + historiaId);

                // Eliminar la historia clínica
                historiaClinicaService.delete(historiaId);
            }

            System.out.println("Eliminando paciente con ID: " + id);

            // 3. Eliminar el paciente
            pacienteDAO.delete(id);

        } catch (Exception e) {
            throw new Exception("Error al eliminar el paciente: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Lógica de Cascada (RN-031):</b>
     * <br>
     * 1. Valida <code>id > 0</code>.
     * <br>
     * 2. Busca al paciente <b>eliminado</b> (<code>eliminado=true</code>) usando
     * <code>pacienteDAO.selectByIdWithStatus(id, true)</code>.
     * <br>
     * 3. Si el paciente tiene una <code>HistoriaClinica</code> (HC) asociada:
     * <br>
     * Llama a <code>historiaClinicaService.recover()</code> para recuperar la HC
     * (Entidad B) <b>primero</b>.
     * <br>
     * 4. Llama a <code>pacienteDAO.recover()</code> para recuperar el Paciente
     * (Entidad A).
     * </p>
     * 
     * @param id ID del paciente a recuperar.
     * @throws Exception Si el ID es inválido o hay error de BD.
     */
    @Override
    public void recover(int id) throws Exception {

        if (id <= 0) {
            throw new IllegalArgumentException("El ID del paciente debe ser mayor a cero.");
        }

        try {
            // 1. Buscar al paciente ELIMINADO que se desea recuperar
            Paciente paciente = pacienteDAO.selectByIdWithStatus(id, true);

            if (paciente == null) {
                System.out.println("No se encontró un paciente eliminado con ID: " + id);
                return;
            }

            // 2. Si tiene historia, recuperarla también (RN-031)
            if (paciente.getHistoriaClinica() != null) {

                // Obtener el ID de la historia clínica
                int historiaId = paciente.getHistoriaClinica().getId();

                System.out.println("Recuperando historia clínica asociada con ID: " + historiaId);

                // Asumimos que la HC también fue eliminada, la recuperamos
                historiaClinicaService.recover(historiaId);
            }

            // 3. Recuperar al paciente
            pacienteDAO.recover(id);

            System.out.println("Paciente con ID: " + id + " recuperado exitosamente!");

        } catch (Exception e) {
            throw new Exception("Error al recuperar el paciente: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina una Historia Clínica (HC) de forma segura, previniendo referencias
     * huérfanas (HU-008).
     * <p>
     * Esta es la implementación de la <b>"Eliminación Segura"</b>.
     * </p>
     *
     * <h3>Flujo (pseudo-transaccional):</h3>
     * <ol>
     * <li>Busca al Paciente <b>activo</b>.</li>
     * <li>Valida que el Paciente sea el dueño de la HC que se quiere borrar.</li>
     * <li><b>Paso 1 (UPDATE):</b> Pone
     * <code>paciente.setHistoriaClinica(null)</code> y llama a
     * <code>pacienteDAO.update()</code>. Esto setea la FK
     * <code>historia_clinica_id = NULL</code> en la BD.</li>
     * <li><b>Paso 2 (DELETE):</b> Llama a
     * <code>historiaClinicaService.delete()</code>. Esto setea
     * <code>eliminado = TRUE</code> en la HC.</li>
     * </ol>
     * <p>
     * <b>Nota:</b> Como esta lógica no usa <code>TransactionManager</code>, si el
     * Paso 2 falla, el Paso 1 <b>no</b> se revierte.
     * El paciente quedaría desasociado pero la HC seguiría activa.
     * </p>
     *
     * @param pacienteId        ID del paciente.
     * @param historiaClinicaId ID de la historia clínica a eliminar.
     * @throws Exception Si los IDs son inválidos, el paciente no se encuentra,
     *                   o la HC no pertenece al paciente.
     */
    public void deleteHistoriaClinica(int pacienteId, int historiaClinicaId) throws Exception {

        if (pacienteId <= 0 || historiaClinicaId <= 0) {
            throw new IllegalArgumentException("Los IDs deben ser mayores a 0");
        }

        // Buscar paciente ACTIVO
        Paciente paciente = pacienteDAO.selectByIdWithStatus(pacienteId, false);

        if (paciente == null) {
            throw new IllegalArgumentException("Paciente no encontrado con ID: " + pacienteId);
        }

        // Validar "propiedad" de la HC
        if (paciente.getHistoriaClinica() == null || paciente.getHistoriaClinica().getId() != historiaClinicaId) {
            throw new IllegalArgumentException("La Historia Clínica no pertenece a este paciente");
        }

        // Flujo de eliminación segura (HU-008):
        // 1. Desasociar la FK en Paciente
        paciente.setHistoriaClinica(null);
        pacienteDAO.update(paciente); // UPDATE Paciente SET historia_clinica_id = NULL

        // 2. Eliminar lógicamente la HC
        historiaClinicaService.delete(historiaClinicaId); // UPDATE HistoriaClinica SET eliminado = TRUE
    }

    // ============ MÉTODOS SELECT (Lectura) ============
    /**
     * {@inheritDoc}
     * <p>
     * Busca un paciente por su ID.
     * </p>
     * 
     * @param id      El ID del paciente a buscar.
     * @param deleted <code>false</code> para activo.
     *                <code>true</code> para eliminado.
     * @return Paciente encontrado o <code>null</code>.
     * @throws Exception Si ID <= 0 o si falla la consulta
     *                   (<code>SQLException</code>).
     */
    @Override
    public Paciente selectById(int id, boolean deleted) throws Exception {

        if (id <= 0) {
            throw new IllegalArgumentException("El ID del paciente debe ser mayor a cero.");
        }

        try {
            System.out.println("\nObteniendo paciente con ID: " + id);

            return pacienteDAO.selectByIdWithStatus(id, deleted);

        } catch (SQLException e) {
            throw new SQLException("Error al obtener el paciente por ID: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Obtiene todos los pacientes de la base de datos.
     * </p>
     * 
     * @param deleted <code>false</code> para activos.
     *                <code>true</code> para eliminados.
     * @return Lista de pacientes (puede estar vacía).
     * @throws Exception Si falla la consulta (<code>SQLException</code>).
     */
    @Override
    public List<Paciente> selectAll(boolean deleted) throws Exception {

        try {
            List<Paciente> pacientes = pacienteDAO.selectAllWithStatus(deleted);
            String tipo = deleted ? "eliminados" : "activos";

            System.out.println("\n=========================================");
            System.out.println(pacientes.size() + " pacientes " + tipo + " encontrados.");
            System.out.println("=========================================");

            return pacientes;

        } catch (SQLException e) {
            throw new SQLException("Error al obtener todos los pacientes: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Busca pacientes que coincidan con un filtro en sus atributos (nombre,
     * apellido o DNI).
     * </p>
     *
     * @param filter Cadena de texto para filtrar.
     * @return Lista de pacientes (puede estar vacía).
     * @throws Exception Si el filtro es nulo/vacío
     *                   (<code>IllegalArgumentException</code>)
     *                   o si falla la consulta (<code>SQLException</code>).
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

        } catch (SQLException e) {
            throw new SQLException("Error al buscar pacientes por nombre o apellido: " + e.getMessage(), e);
        }
    }

    /**
     * Busca un paciente <b>activo</b> por su DNI (coincidencia exacta).
     * <p>
     * Este es un método de conveniencia para la UI ({@link main.MenuHandler}), que
     * envuelve la llamada al DAO con validación.
     * </p>
     *
     * @param dni DNI del paciente.
     * @return Paciente encontrado o <code>null</code> si no existe.
     * @throws Exception Si el DNI es nulo/vacío
     *                   (<code>IllegalArgumentException</code>)
     *                   o si falla la consulta (<code>SQLException</code>).
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

        } catch (SQLException e) {
            throw new SQLException("Error al obtener el paciente por DNI: " + e.getMessage(), e);
        }
    }

    // ============ MÉTODOS DE VALIDACIÓN (RN) ============
    /**
     * {@inheritDoc}
     * <p>
     * Esta es la implementación de la <b>Regla de Negocio (RN-001)</b> y las
     * validaciones de formato que estaban en el Modelo.
     * </p>
     *
     * <h3>Validaciones Ejecutadas:</h3>
     * <ul>
     * <li><b>RN-001.1:</b> Paciente no puede ser <code>null</code>.</li>
     * <li><b>RN-001.2:</b> Nombre no puede ser <code>null</code> o vacío
     * (<code>isBlank</code>).</li>
     * <li><b>RN-001.3:</b> Apellido no puede ser <code>null</code> o vacío
     * (<code>isBlank</code>).</li>
     * <li><b>RN-001.4:</b> DNI no puede ser <code>null</code> o vacío
     * (<code>isBlank</code>).</li>
     * <li><b>RN-001.5:</b> Fecha de Nacimiento no puede ser <code>null</code>.</li>
     * <li><b>RN-001.6 (Formato DNI):</b> DNI debe ser numérico (7-15 dígitos).</li>
     * <li><b>RN-001.7 (Rango Fecha):</b> Fecha de Nacimiento debe ser entre
     * 1900-01-01 y la fecha actual.</li>
     * </ul>
     * 
     * @param paciente El objeto Paciente a validar.
     * @throws IllegalArgumentException Si alguna regla de negocio es violada.
     */
    @Override
    public void validateEntity(Paciente paciente) throws Exception {

        if (paciente == null) {
            throw new IllegalArgumentException("El objeto Paciente no puede ser nulo.");
        }

        // RN-001.2: Validación de Nombre
        if (paciente.getNombre() == null || paciente.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del paciente no puede estar vacío.");
        }

        // RN-001.3: Validación de Apellido
        if (paciente.getApellido() == null || paciente.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido del paciente no puede estar vacío.");
        }

        // RN-001.4: Validación de DNI (Existencia)
        if (paciente.getDni() == null || paciente.getDni().trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI del paciente no puede estar vacío.");
        }

        // RN-001.6: Validación de DNI (Formato)
        String dniRegex = "^[0-9]{7,15}$";
        if (!paciente.getDni().matches(dniRegex)) {
            throw new IllegalArgumentException("El DNI no es válido (debe ser numérico, 7-15 dígitos).");
        }

        // RN-001.5: Validación de Fecha de Nacimiento (Existencia)
        if (paciente.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula.");
        }

        // RN-001.7: Validación de Fecha de Nacimiento (Rango)
        LocalDate fechaNac = paciente.getFechaNacimiento();
        LocalDate fechaMin = LocalDate.of(1900, Month.JANUARY, 1);
        LocalDate fechaMax = LocalDate.now();

        if (fechaNac.isAfter(fechaMax) || fechaNac.isBefore(fechaMin)) {
            throw new IllegalArgumentException("La fecha de nacimiento no es válida (debe ser entre 1900 y hoy).");
        }
    }

    /**
     * Valida que un DNI sea único en el sistema (<b>Regla de Negocio RN-002</b>).
     * <p>
     * Este método es crucial para prevenir la duplicidad de datos y es
     * llamado por <code>insert()</code> y <code>update()</code>.
     * </p>
     *
     * <h3>Lógica de Validación:</h3>
     * <ol>
     * <li>Busca en el DAO si ya existe un paciente con ese DNI
     * (<code>pacienteDAO.selectByDni(dni)</code>).</li>
     * <li><b>Si NO existe (<code>existente == null</code>):</b> El DNI es
     * único y la validación pasa.</li>
     * <li><b>Si SÍ existe (<code>existente != null</code>):</b>
     * <ul>
     * <li><b>Caso INSERT:</b> <code>pacienteId</code> es <code>null</code>.
     * Como <code>existente</code> existe, es un duplicado. Lanza
     * <code>IllegalArgumentException</code>.</li>
     * <li><b>Caso UPDATE:</b> <code>pacienteId</code> <b>no</b> es
     * <code>null</code>.
     * <ul>
     * <li>Si el ID encontrado (<code>existente.getId()</code>) es
     * <b>diferente</b> al ID que estamos actualizando
     * (<code>pacienteId</code>), significa que el DNI pertenece a
     * <b>otro</b> paciente. Lanza
     * <code>IllegalArgumentException</code>.</li>
     * <li>Si el ID encontrado es <b>igual</b> al ID que estamos
     * actualizando, significa que el paciente está conservando su
     * propio DNI. La validación pasa.</li>
     * </ul>
     * </li>
     * </ul>
     * </ol>
     *
     * @param dni        El DNI (String) a validar.
     * @param pacienteId El ID del paciente que se está
     *                   <code>UPDATE</code>ando
     *                   (<b>importante:</b> debe ser <code>null</code> si es
     *                   un <code>INSERT</code>).
     * @throws IllegalArgumentException Si el DNI ya está en uso por
     *                                  <i>otro</i> paciente.
     * @throws Exception                Si ocurre un error de
     *                                  <code>SQLException</code> durante la
     *                                  consulta.
     */
    private void validateDniUnique(String dni, Integer pacienteId) throws Exception {

        Paciente existente = pacienteDAO.selectByDni(dni);

        if (existente != null) {

            // Existe un paciente con ese DNI.
            // Ahora debemos verificar si es "este" paciente u "otro" paciente.
            if (pacienteId == null || existente.getId() != pacienteId) {
                throw new IllegalArgumentException("Ya existe un paciente con el DNI: " + dni);
            }
        }
        // Si 'existente' es null, el DNI está disponible. Validación OK.
    }

}
