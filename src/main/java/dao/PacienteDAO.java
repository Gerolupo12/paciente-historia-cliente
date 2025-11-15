package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import config.DatabaseConnection;
import models.GrupoSanguineo;
import models.HistoriaClinica;
import models.Paciente;

/**
 * Data Access Object (DAO) para la entidad Paciente (Entidad "A").
 * <p>
 * Gestiona todas las operaciones de persistencia (CRUD) para Pacientes en la
 * base de datos.
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li>Implementar la interfaz {@link GenericDAO} para {@link Paciente}.</li>
 * <li>Usar <b>PreparedStatement</b> en TODAS las consultas para prevenir SQL
 * Injection.</li>
 * <li>Gestionar la relación 1-a-1 con {@link HistoriaClinica} (HC) mediante
 * <b>LEFT JOIN</b> en las consultas SELECT.</li>
 * <li>Implementar <b>Baja Lógica (Soft Delete)</b>
 * (<code>eliminado = TRUE</code>, no <code>DELETE</code> físico).</li>
 * <li>Proveer métodos para operaciones <b>Transaccionales</b> (ej:
 * <code>insertTx</code>) que aceptan una {@link Connection} externa.</li>
 * <li>Mapear <code>ResultSet</code> a objetos <code>Paciente</code> (incluyendo
 * la HC anidada).</li>
 * </ul>
 *
 * <h3>Patrón de Diseño:</h3>
 * DAO con <b>Inyección de Dependencias</b> (recibe
 * <code>HistoriaClinicaDAO</code>
 * en el constructor) y uso de <b>try-with-resources</b> para manejo automático
 * de recursos JDBC.
 *
 * @author alpha team
 * @see GenericDAO
 * @see main.java.models.Paciente
 * @see main.java.models.HistoriaClinica
 * @see main.java.config.TransactionManager
 */
public class PacienteDAO implements GenericDAO<Paciente> {

    // ============ CONSTANTES SQL ============
    /**
     * Query para insertar un nuevo Paciente.
     * Define los 5 campos a insertar (id es AUTO_INCREMENT).
     */
    private static final String INSERT_SQL = """
                INSERT INTO Paciente
                (nombre, apellido, dni, fecha_nacimiento, historia_clinica_id)
                VALUES (?, ?, ?, ?, ?)
            """;

    /**
     * Query para actualizar un Paciente existente.
     * Actualiza los 5 campos basado en el ID (parámetro 6).
     */
    private static final String UPDATE_SQL = """
                UPDATE Paciente
                SET nombre = ?, apellido = ?, dni = ?, fecha_nacimiento = ?,
                historia_clinica_id = ?
                WHERE id = ?
            """;

    /**
     * Query para la Baja Lógica (Soft Delete).
     * Actualiza el flag 'eliminado' a TRUE.
     */
    private static final String DELETE_SQL = """
                UPDATE Paciente
                SET eliminado = TRUE
                WHERE id = ?
            """;

    /**
     * Query para la Recuperación Lógica (Soft Undelete).
     * Actualiza el flag 'eliminado' a FALSE.
     */
    private static final String RECOVER_SQL = """
                UPDATE Paciente
                SET eliminado = FALSE
                WHERE id = ?
            """;

    /**
     * <b>Consulta Base (Patrón DRY)</b>
     * <p>
     * Esta es la consulta <code>SELECT</code> y <code>JOIN</code> base que es
     * reutilizada por todas las demás operaciones de lectura.
     * </p>
     * <ul>
     * <li>Selecciona todos los campos necesarios de Paciente (p) e HistoriaClinica
     * (hc).</li>
     * <li>Usa <b>LEFT JOIN</b> para asegurar que los Pacientes <b>sin</b> historia
     * clínica (<code>historia_clinica_id = NULL</code>) sean devueltos.</li>
     * </ul>
     */
    private static final String SELECT_SQL = """
                SELECT
                    p.id AS paciente_id,
                    p.nombre,
                    p.apellido,
                    p.dni,
                    p.fecha_nacimiento,
                    p.historia_clinica_id,
                    hc.id AS hc_id,
                    hc.nro_historia,
                    gs.nombre_enum,
                    hc.antecedentes,
                    hc.medicacion_actual,
                    hc.observaciones
                FROM Paciente p
                LEFT JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id
                LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
            """;

    /**
     * Query para obtener un Paciente por ID y estado de eliminación.
     * Concatena la consulta base <code>SELECT_SQL</code>.
     */
    private static final String SELECT_BY_ID_SQL = SELECT_SQL + """
                WHERE p.id = ? AND p.eliminado = ?
            """;

    /**
     * Query para obtener todos los Pacientes por estado de eliminación.
     * Concatena la consulta base <code>SELECT_SQL</code>.
     */
    private static final String SELECT_ALL_SQL = SELECT_SQL + """
                WHERE p.eliminado = ?
                ORDER BY p.apellido, p.nombre
            """;

    /**
     * Query para buscar Pacientes (activos) por filtro de texto.
     * Concatena la consulta base <code>SELECT_SQL</code>.
     * Busca con <code>LIKE</code> en nombre y apellido.
     */
    private static final String SEARCH_BY_FILTER_SQL = SELECT_SQL + """
                WHERE p.eliminado = FALSE
                AND (
                    LOWER(p.nombre) LIKE LOWER(?)
                    OR LOWER(p.apellido) LIKE LOWER(?)
                )
                ORDER BY p.apellido, p.nombre
            """;

    /**
     * Query para buscar un Paciente (activo) por DNI exacto.
     * Concatena la consulta base <code>SELECT_SQL</code>.
     * Usado para la validación de unicidad (RN-002) en el Service.
     */
    private static final String SELECT_BY_DNI_SQL = SELECT_SQL + """
                WHERE p.dni = ? AND p.eliminado = FALSE
            """;

    /**
     * Dependencia inyectada, aunque no se use directamente en este DAO,
     * es una práctica común en el patrón de inyección de dependencias.
     */
    private final HistoriaClinicaDAO historiaClinicaDAO;

    /**
     * Constructor con Inyección de Dependencias.
     *
     * @param historiaClinicaDAO Una instancia de {@link HistoriaClinicaDAO}
     *                           (aunque no se use activamente en esta clase, se
     *                           inyecta para seguir el patrón).
     */
    public PacienteDAO(HistoriaClinicaDAO historiaClinicaDAO) {
        this.historiaClinicaDAO = historiaClinicaDAO;
    }

    // ============ MÉTODOS CRUD (Escritura) ============
    /**
     * {@inheritDoc}
     * <p>
     * Esta versión <b>NO</b> es transaccional. Abre y cierra su propia conexión.
     * </p>
     *
     * @param paciente El Paciente a insertar (con <code>id=0</code>).
     * @throws SQLException Si falla la inserción o la obtención de claves
     *                      generadas.
     */
    @Override
    public void insert(Paciente paciente) throws SQLException {

        // try-with-resources asegura que conn y stmt se cierren
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            // Mapea el objeto Paciente a los parámetros "?"
            setEntityParameters(stmt, paciente);

            stmt.executeUpdate();

            // Sincroniza el ID de la BD con el objeto Java
            setGeneratedId(stmt, paciente);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta versión <b>SÍ</b> es transaccional. Utiliza la {@link Connection}
     * proveída por la capa de Servicio.
     * </p>
     *
     * @param paciente La entidad a guardar.
     * @param conn     La conexión transaccional (con autoCommit=false).
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    @Override
    public void insertTx(Paciente paciente, Connection conn) throws SQLException {

        // try-with-resources solo para PreparedStatement.
        // NO se cierra la 'conn', es gestionada por el TransactionManager.
        try (PreparedStatement stmt = conn.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            setEntityParameters(stmt, paciente);
            stmt.executeUpdate();
            setGeneratedId(stmt, paciente);
        }
        // La SQLException se propaga hacia el Service, que hará rollback.
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta versión <b>NO</b> es transaccional. Abre y cierra su propia conexión.
     * </p>
     *
     * @param paciente El Paciente a actualizar (con <code>id > 0</code>).
     * @throws SQLException Si el ID no se encuentra (rowsAffected=0) o
     *                      si falla la actualización.
     */
    @Override
    public void update(Paciente paciente) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            setEntityParameters(stmt, paciente);
            stmt.setInt(6, paciente.getId()); // Parámetro 6 para "WHERE id = ?"

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                // Lanza error si el ID no existía o ya estaba eliminado
                throw new SQLException("Error al actualizar: No se encontró Paciente con ID: " + paciente.getId());
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta versión <b>SÍ</b> es transaccional. Utiliza la {@link Connection}
     * proveída por la capa de Servicio.
     * </p>
     *
     * @param paciente La entidad con los datos actualizados.
     * @param conn     La conexión transaccional (con autoCommit=false).
     * @throws SQLException Si el ID no se encuentra (rowsAffected=0) o
     *                      si falla la actualización.
     */
    @Override
    public void updateTx(Paciente paciente, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            setEntityParameters(stmt, paciente);
            stmt.setInt(6, paciente.getId()); // Parámetro 6 para "WHERE id = ?"

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                // Lanza error si el ID no existía o ya estaba eliminado
                throw new SQLException("Error en updateTx: No se encontró Paciente con ID: " + paciente.getId());
            }
        }
        // La SQLException se propaga hacia el Service, que hará rollback.
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta versión <b>NO</b> es transaccional. Abre y cierra su propia conexión.
     * </p>
     *
     * @param id El ID de la entidad a marcar como eliminada.
     * @throws SQLException Si el ID no se encuentra (rowsAffected=0) o
     *                      si falla la actualización.
     */
    @Override
    public void delete(int id) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                // Lanza error si el ID no existía o ya estaba eliminado
                throw new SQLException("Error al eliminar: No se encontró Paciente con ID: " + id);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta versión <b>SÍ</b> es transaccional. Utiliza la {@link Connection}
     * proveída por la capa de Servicio.
     * </p>
     *
     * @param id   El ID de la entidad a marcar como eliminada.
     * @param conn La conexión transaccional (con autoCommit=false).
     * @throws SQLException Si el ID no se encuentra (rowsAffected=0) o
     *                      si falla la actualización.
     */
    @Override
    public void deleteTx(int id, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                // Lanza error si el ID no existía o ya estaba eliminado
                throw new SQLException("Error en deleteTx: No se encontró Paciente con ID: " + id);
            }
        }
        // La SQLException se propaga hacia el Service, que hará rollback.
    }

    /**
     * {@inheritDoc}
     * <p>
     * Ejecuta un <code>UPDATE</code> para setear <code>eliminado = FALSE</code>.
     * </p>
     *
     * @param id ID del paciente a recuperar.
     * @throws SQLException Si falla la recuperación.
     */
    @Override
    public void recover(int id) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(RECOVER_SQL)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                // Lanza error si el ID no existía o ya estaba activo
                throw new SQLException("Error al recuperar: No se encontró Paciente eliminado con ID: " + id);
            }
        }
    }

    // ============ MÉTODOS SELECT (Lectura) ============
    /**
     * {@inheritDoc}
     * Centraliza la lógica de búsqueda por ID.
     * <p>
     * Reutiliza la consulta <code>SELECT_BY_ID_SQL</code> para buscar tanto
     * pacientes activos (<code>deleted = false</code>) como eliminados
     * (<code>deleted = true</code>).
     * </p>
     *
     * @param id      El ID del paciente a buscar.
     * @param deleted <code>false</code> para buscar activos.
     *                <code>true</code> para buscar eliminados.
     * @return El {@link Paciente} encontrado, o <code>null</code> si no existe en
     *         ese estado.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    @Override
    public Paciente selectByIdWithStatus(int id, boolean deleted) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id); // Parámetro 1: p.id = ?
            stmt.setBoolean(2, deleted); // Parámetro2: p.eliminado = ?

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al seleccionar paciente por ID: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Selecciona todos los pacientes de la base de datos.
     * </p>
     * 
     * @param deleted <code>false</code> para activos.
     *                <code>true</code> para eliminados.
     * @return Lista de todos los pacientes.
     * @throws SQLException Si ocurre un error durante la operación.
     */
    @Override
    public List<Paciente> selectAllWithStatus(boolean deleted) throws SQLException {

        List<Paciente> pacientes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL)) {

            stmt.setBoolean(1, deleted); // Parámetro para p.eliminado = ?

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    // Mapea la fila actual a un objeto Paciente
                    pacientes.add(mapEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al seleccionar todos los pacientes: " + e.getMessage(), e);
        }
        return pacientes;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Busca en las columnas <code>nombre</code> y <code>apellido</code> usando
     * <code>LIKE '%filtro%'</code>. Solo busca en pacientes <b>activos</b>.
     * </p>
     * 
     * @param filter filtro de búsqueda.
     * @return Lista de pacientes que coinciden con el filtro.
     * @throws SQLException Si ocurre un error durante la operación.
     */
    @Override
    public List<Paciente> searchByFilter(String filter) throws SQLException {

        List<Paciente> pacientes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SEARCH_BY_FILTER_SQL)) {

            String wildcard = "%" + filter.trim() + "%";

            stmt.setString(1, wildcard); // Para 'nombre'
            stmt.setString(2, wildcard); // Para 'apellido'

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    pacientes.add(mapEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al buscar pacientes por nombre o apellido: " + e.getMessage(), e);
        }
        return pacientes;
    }

    /**
     * Busca un paciente <b>activo</b> por DNI (coincidencia exacta).
     * <p>
     * Método crucial para la capa de Servicio, que lo utiliza para validar la regla
     * de negocio de DNI único (RN-002).
     * </p>
     *
     * @param dni El DNI exacto a buscar.
     * @return El {@link Paciente} encontrado, o <code>null</code> si no
     *         existe o está eliminado.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public Paciente selectByDni(String dni) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_DNI_SQL)) {

            stmt.setString(1, dni);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al seleccionar paciente por DNI: " + e.getMessage(), e);
        }
        return null;
    }

    // ============ MÉTODOS HELPER (Mapeo y Parámetros) ============
    /**
     * {@inheritDoc}
     * <p>
     * Mapea <code>ResultSet</code> a <code>Paciente</code>, manejando el <b>LEFT
     * JOIN</b> con <code>HistoriaClinica</code>.
     * </p>
     * <p>
     * <b>Lógica de Mapeo 1-a-1 (LEFT JOIN):</b>
     * 1. Se leen los campos del Paciente (<code>paciente_id</code>,
     * <code>nombre</code>, etc.).
     * 2. Se lee el campo <code>hc_id</code> (la FK).
     * 3. Se comprueba si <code>rs.getObject("hc_id")</code> es <code>null</code>.
     * 4. <b>Si NO es null:</b> Se leen los campos de la HC
     * (<code>nro_historia</code>, etc.) y se crea un nuevo objeto
     * <code>HistoriaClinica</code>.
     * 5. <b>Si ES null:</b> El objeto <code>historiaClinica</code> permanece
     * <code>null</code>.
     * 6. Se crea el <code>Paciente</code> final, asociándole la
     * <code>historiaClinica</code> (que puede ser <code>null</code>).
     * </p>
     *
     * @param rs El ResultSet posicionado en la fila a mapear.
     * @return El objeto <code>Paciente</code> mapeado.
     * @throws SQLException Si una columna esperada no se encuentra.
     */
    @Override
    public Paciente mapEntity(ResultSet rs) throws SQLException {

        // 1. Mapear GrupoSanguineo (el nivel más profundo)
        GrupoSanguineo grupo = null;
        String nombreEnum = rs.getString("nombre_enum");

        if (nombreEnum != null) {
            try {
                // Convierte el String de la BD (ej: "A_PLUS") al Enum
                grupo = GrupoSanguineo.valueOf(nombreEnum);

            } catch (IllegalArgumentException e) {
                // Log de advertencia si el valor de la BD no es un Enum válido
                System.err.println("Advertencia: GrupoSanguineo inválido en BD: " + nombreEnum);
                grupo = null;
            }
        }

        // 2. Mapear HistoriaClinica (depende de GrupoSanguineo)
        // Se usa rs.getObject() para comprobar de forma segura si la FK fue NULL
        HistoriaClinica historiaClinica = null;

        if (rs.getObject("hc_id") != null) {
            historiaClinica = new HistoriaClinica(
                    rs.getInt("hc_id"),
                    rs.getString("nro_historia"),
                    grupo,
                    rs.getString("antecedentes"),
                    rs.getString("medicacion_actual"),
                    rs.getString("observaciones"));
        }

        // 3. Mapear Paciente (depende de HistoriaClinica)
        return new Paciente(
                rs.getInt("paciente_id"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("dni"),
                // Convertir java.sql.Date a java.time.LocalDate
                rs.getDate("fecha_nacimiento") != null
                        ? rs.getDate("fecha_nacimiento").toLocalDate()
                        : null,
                historiaClinica); // Asocia la HC (o null)
    }

    /**
     * {@inheritDoc}
     * <p>
     * Establece los 5 parámetros (<code>?</code>) del <code>INSERT_SQL</code> y
     * <code>UPDATE_SQL</code>.
     * </p>
     * <p>
     * <b>Parámetros:</b>
     * 1: nombre (String)
     * 2: apellido (String)
     * 3: dni (String)
     * 4: fecha_nacimiento (java.sql.Date | NULL)
     * 5: historia_clinica_id (Integer | NULL)
     * </p>
     *
     * @param stmt     El PreparedStatement (<code>INSERT</code> o
     *                 <code>UPDATE</code>).
     * @param paciente La entidad de donde se obtienen los datos.
     * @throws SQLException Si ocurre un error al establecer los parámetros.
     */
    @Override
    public void setEntityParameters(PreparedStatement stmt, Paciente paciente) throws SQLException {

        stmt.setString(1, paciente.getNombre());
        stmt.setString(2, paciente.getApellido());
        stmt.setString(3, paciente.getDni());

        // Convertir LocalDate a java.sql.Date (manejando NULL)
        if (paciente.getFechaNacimiento() != null) {
            stmt.setDate(4, Date.valueOf(paciente.getFechaNacimiento()));
        } else {
            stmt.setNull(4, Types.DATE);
        }

        // Usar el helper para setear la FK (manejando NULL)
        setHistoriaClinicaId(stmt, 5, paciente.getHistoriaClinica());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sincroniza el ID del objeto Java con el ID generado por la BD.
     * </p>
     *
     * @param stmt     El PreparedStatement que ejecutó el <code>INSERT</code>.
     * @param paciente La entidad que recibirá el nuevo ID.
     * @throws SQLException Si <code>RETURN_GENERATED_KEYS</code> no devolvió un ID.
     */
    @Override
    public void setGeneratedId(PreparedStatement stmt, Paciente paciente) throws SQLException {

        // try-with-resources para el ResultSet de claves generadas
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {

            if (generatedKeys.next()) {
                // Asigna el ID de la BD al objeto Java
                paciente.setId(generatedKeys.getInt(1));

            } else {
                throw new SQLException("La inserción del paciente falló! No se pudo obtener el ID generado");
            }
        }
    }

    /**
     * Helper privado para establecer la FK <code>historia_clinica_id</code>.
     * <p>
     * Maneja la lógica de la relación 1-a-1:
     * - Si el Paciente tiene una HC con ID > 0, setea el <code>Integer</code>.
     * - Si el Paciente <b>no</b> tiene HC (<code>historiaClinica == null</code>)
     * o la HC es nueva (<code>id=0</code>), setea <code>NULL</code> en la base de
     * datos.
     * </p>
     *
     * @param stmt           El PreparedStatement.
     * @param parameterIndex El índice del parámetro (<code>?</code>) (ej: 5 en
     *                       <code>INSERT_SQL</code>).
     * @param historia       El objeto HistoriaClinica (puede ser
     *                       <code>null</code>).
     * @throws SQLException Si ocurre un error al establecer el parámetro.
     */
    private void setHistoriaClinicaId(PreparedStatement stmt, int parameterIndex, HistoriaClinica historia)
            throws SQLException {

        if (historia != null && historia.getId() > 0) {
            // Asocia el ID de la HC existente
            stmt.setInt(parameterIndex, historia.getId());
        } else {
            // Desasocia (o no asocia) la HC
            stmt.setNull(parameterIndex, Types.INTEGER);
        }
    }

}
