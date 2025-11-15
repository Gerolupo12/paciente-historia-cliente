package dao;

import java.sql.Connection;
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

/**
 * Data Access Object (DAO) para la entidad HistoriaClinica (Entidad "B").
 * <p>
 * Gestiona todas las operaciones de persistencia (CRUD) para Historias
 * Clínicas.
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li>Implementar la interfaz {@link GenericDAO} para
 * {@link HistoriaClinica}.</li>
 * <li>Usar <b>PreparedStatement</b> en TODAS las consultas para prevenir SQL
 * Injection.</li>
 * <li>Gestionar el mapeo entre el <code>Enum</code> {@link GrupoSanguineo} y su
 * ID en la tabla <code>GrupoSanguineo</code>.</li>
 * <li>Implementar <b>Baja Lógica (Soft Delete)</b>
 * (<code>eliminado = TRUE</code>).</li>
 * <li>Proveer métodos para operaciones <b>Transaccionales</b> (ej:
 * <code>insertTx</code>) que aceptan una {@link Connection} externa.</li>
 * </ul>
 *
 * <h3>Diferencias con PacienteDAO:</h3>
 * <p>
 * Este DAO es más simple que {@link PacienteDAO} porque
 * <code>HistoriaClinica</code> no tiene relaciones 1-a-1 salientes, por lo que
 * <b>no necesita <code>LEFT JOIN</code></b> en sus consultas
 * <code>SELECT</code>.
 * </p>
 *
 * @author alpha team
 * @see GenericDAO
 * @see main.java.models.HistoriaClinica
 * @see main.java.config.TransactionManager
 */
public class HistoriaClinicaDAO implements GenericDAO<HistoriaClinica> {

    // ============ CONSTANTES SQL ============
    /**
     * Query para insertar una nueva HistoriaClinica.
     * Define los 5 campos a insertar (id es AUTO_INCREMENT).
     */
    private static final String INSERT_SQL = """
                INSERT INTO HistoriaClinica
                    (nro_historia, grupo_sanguineo_id, antecedentes,
                    medicacion_actual, observaciones)
                VALUES (?, ?, ?, ?, ?)
            """;

    /**
     * Query para actualizar una HistoriaClinica existente.
     * Actualiza los 5 campos basado en el ID (parámetro 6).
     */
    private static final String UPDATE_SQL = """
                UPDATE HistoriaClinica
                SET nro_historia = ?, grupo_sanguineo_id = ?, antecedentes = ?,
                    medicacion_actual = ?, observaciones = ?
                WHERE id = ?
            """;

    /**
     * Query para la Baja Lógica (Soft Delete).
     * Actualiza el flag 'eliminado' a TRUE.
     */
    private static final String DELETE_SQL = """
                UPDATE HistoriaClinica
                SET eliminado = TRUE
                WHERE id = ?
            """;

    /**
     * Query para la Recuperación Lógica (Soft Undelete).
     * Actualiza el flag 'eliminado' a FALSE.
     */
    private static final String RECOVER_SQL = """
                UPDATE HistoriaClinica
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
     * <li>Selecciona todos los campos necesarios de HistoriaClinica (hc) y
     * GrupoSanguineo (gs).</li>
     * </ul>
     */
    private static final String SELECT_SQL = """
                SELECT
                    hc.*,
                    gs.nombre_enum
                FROM HistoriaClinica hc
                LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
            """;

    /**
     * Query para obtener una HistoriaClinica por ID y estado de eliminación.
     * Concatena la consulta base <code>SELECT_SQL</code>.
     */
    private static final String SELECT_BY_ID_SQL = SELECT_SQL + """
                WHERE hc.id = ? AND hc.eliminado = ?
            """;

    /**
     * Query para obtener todas las HistoriasClinicas por estado de eliminación.
     * Concatena la consulta base <code>SELECT_SQL</code>.
     */
    private static final String SELECT_ALL_SQL = SELECT_SQL + """
                WHERE hc.eliminado = ?
                ORDER BY hc.id
            """;

    /**
     * Query para buscar HistoriasClinicas (activas) por filtro de texto.
     * Concatena la consulta base <code>SELECT_SQL</code>.
     * Busca con LIKE en nro_historia, campos de texto y el nombre del enum
     * de GrupoSanguineo.
     */
    private static final String SEARCH_BY_FILTER_SQL = SELECT_SQL + """
                WHERE hc.eliminado = FALSE
                    AND (
                        LOWER(hc.nro_historia) LIKE LOWER(?)
                        OR LOWER(hc.antecedentes) LIKE LOWER(?)
                        OR LOWER(hc.medicacion_actual) LIKE LOWER(?)
                        OR LOWER(hc.observaciones) LIKE LOWER(?)
                        OR LOWER(gs.nombre_enum) LIKE LOWER(?)
                    )
                ORDER BY hc.nro_historia
            """;

    /**
     * Query para buscar una HistoriaClinica (activa) por nroHistoria (exacto).
     * Concatena la consulta base <code>SELECT_SQL</code>.
     * Usado para la validación de unicidad (RN-015) en el Service.
     */
    private static final String SELECT_BY_NRO_HISTORIA_SQL = SELECT_SQL + """
                WHERE hc.nro_historia = ? AND hc.eliminado = FALSE
            """;

    /**
     * Query para obtener el ID de un GrupoSanguineo por su nombre de enum.
     * Usado para mapear el Enum de Java a la FK de la BD.
     */
    private static final String SELECT_BY_NOMBRE_ENUM_SQL = """
                SELECT id FROM GrupoSanguineo WHERE nombre_enum = ?
            """;

    // ============ MÉTODOS CRUD (Escritura) ============
    /**
     * {@inheritDoc}
     * <p>
     * Esta versión <b>NO</b> es transaccional. Abre y cierra su propia conexión.
     * </p>
     *
     * @param hc La HistoriaClinica a insertar (con <code>id=0</code>).
     * @throws SQLException Si falla la inserción, la obtención del ID de
     *                      <code>GrupoSanguineo</code>, o la obtención de claves
     *                      generadas.
     */
    @Override
    public void insert(HistoriaClinica hc) throws SQLException {

        // try-with-resources asegura que conn y stmt se cierren
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            // Mapea el objeto HC a los parámetros "?"
            setEntityParameters(stmt, hc);
            stmt.executeUpdate();

            // Sincroniza el ID de la BD con el objeto Java
            setGeneratedId(stmt, hc);

        } catch (SQLException e) {
            throw new SQLException("Error al insertar historia clínica: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta versión <b>SÍ</b> es transaccional. Utiliza la {@link Connection}
     * proveída por la capa de Servicio.
     * </p>
     *
     * @param hc   La entidad a guardar.
     * @param conn La conexión transaccional (con autoCommit=false).
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    @Override
    public void insertTx(HistoriaClinica hc, Connection conn) throws SQLException {

        // try-with-resources solo para PreparedStatement.
        // NO se cierra la 'conn', es gestionada por el TransactionManager.
        try (PreparedStatement stmt = conn.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            // NOTA: setEntityParameters llamará a getGrupoSanguineoId,
            // que abre su PROPIA conexión no transaccional.
            // Ver Javadoc de getGrupoSanguineoId para más detalles.
            setEntityParameters(stmt, hc);
            stmt.executeUpdate();
            setGeneratedId(stmt, hc);
        }
        // La SQLException se propaga hacia el Service, que hará rollback.
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta versión <b>NO</b> es transaccional. Abre y cierra su propia conexión.
     * </p>
     *
     * @param hc La HistoriaClinica a actualizar (con <code>id > 0</code>).
     * @throws SQLException Si el ID no se encuentra (rowsAffected=0) o
     *                      si falla la actualización.
     */
    @Override
    public void update(HistoriaClinica hc) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            setEntityParameters(stmt, hc);
            stmt.setInt(6, hc.getId()); // Parámetro 6 para "WHERE id = ?"

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                // Lanza error si el ID no existía o ya estaba eliminado
                throw new SQLException("Error al actualizar: No se encontró HistoriaClinica con ID: " + hc.getId());
            }
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar historia clínica: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta versión <b>SÍ</b> es transaccional. Utiliza la {@link Connection}
     * proveída por la capa de Servicio.
     * </p>
     *
     * @param hc   La entidad con los datos actualizados.
     * @param conn La conexión transaccional (con autoCommit=false).
     * @throws SQLException Si el ID no se encuentra (rowsAffected=0) o
     *                      si falla la actualización.
     */
    @Override
    public void updateTx(HistoriaClinica hc, Connection conn) throws SQLException {

        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            setEntityParameters(stmt, hc);
            stmt.setInt(6, hc.getId()); // Parámetro 6 para "WHERE id = ?"

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                // Lanza error si el ID no existía o ya estaba eliminado
                throw new SQLException("Error en updateTx: No se encontró HistoriaClinica con ID: " + hc.getId());
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
                throw new SQLException("Error al eliminar: No se encontró HistoriaClinica con ID: " + id);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar historia clínica: " + e.getMessage(), e);
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
                throw new SQLException("Error en deleteTx: No se encontró HistoriaClinica con ID: " + id);
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
     * @param id ID de la historia clínica a recuperar.
     * @throws SQLException Si ocurre un error durante la operación.
     */
    @Override
    public void recover(int id) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(RECOVER_SQL)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                // Lanza error si el ID no existía o ya estaba activo
                throw new SQLException("Error al recuperar: No se encontró HistoriaClinica eliminada con ID: " + id);
            }

        } catch (SQLException e) {
            throw new SQLException("Error al recuperar historia clínica: " + e.getMessage(), e);
        }
    }

    // ============ MÉTODOS SELECT (Lectura) ============
    /**
     * {@inheritDoc}
     * Centraliza la lógica de búsqueda por ID.
     * <p>
     * Reutiliza la consulta <code>SELECT_BY_ID_SQL</code> para buscar tanto
     * historias clínicas activas (<code>deleted = false</code>) como eliminadas
     * (<code>deleted = true</code>).
     * </p>
     *
     * @param id      ID de la historia clínica a seleccionar.
     * @param deleted <code>false</code> para buscar activas.
     *                <code>true</code> para buscar eliminadas.
     * @return El {@link HistoriaClinica} encontrada, o <code>null</code> si no
     *         existe en ese estado.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    @Override
    public HistoriaClinica selectByIdWithStatus(int id, boolean deleted) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id); // Parámetro 1: hc.id = ?
            stmt.setBoolean(2, deleted); // Parámetro2: hc.eliminado = ?
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapEntity(rs);
                }
            }

        } catch (SQLException e) {
            throw new SQLException("Error al obtener historia clínica por ID: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Selecciona todas las historias clínicas de la base de datos.
     * </p>
     * 
     * @param deleted <code>false</code> para activas.
     *                <code>true</code> para eliminadas.
     * @return Lista de todas las historias clínicas.
     * @throws SQLException Si ocurre un error durante la operación.
     */
    @Override
    public List<HistoriaClinica> selectAllWithStatus(boolean deleted) throws SQLException {

        List<HistoriaClinica> historias = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL)) {

            stmt.setBoolean(1, deleted); // Parámetro para hc.eliminado = ?

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    historias.add(mapEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener todas las historias clínicas: " + e.getMessage(), e);
        }
        return historias;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Busca en <code>nro_historia</code>, <code>antecedentes</code>,
     * <code>medicacion_actual</code>, <code>observaciones</code> y
     * <code>gs.nombre_enum</code>. Solo busca en historias clínicas <b>activas</b>.
     * </p>
     * <p>
     * Incluye lógica para traducir la entrada del usuario (ej: "A+") al formato del
     * enum en la BD (ej: "A_PLUS") antes de ejecutar la consulta <code>LIKE</code>.
     * </p>
     * 
     * @param filter Filtro de búsqueda.
     * @return Lista de historias clínicas que coinciden con el filtro.
     * @throws SQLException Si ocurre un error durante la operación.
     */
    @Override
    public List<HistoriaClinica> searchByFilter(String filter) throws SQLException {

        List<HistoriaClinica> historias = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SEARCH_BY_FILTER_SQL)) {

            // Lógica de traducción de UI a BD (ej: "A+" -> "A_PLUS")
            String cleanFilter = filter.trim().toUpperCase();

            if (cleanFilter.endsWith("+")) {
                cleanFilter = cleanFilter.substring(0, cleanFilter.length() - 1) + "_PLUS";

            } else if (cleanFilter.endsWith("-")) {
                cleanFilter = cleanFilter.substring(0, cleanFilter.length() - 1) + "_MINUS";
            }

            String wildcard = "%" + cleanFilter + "%";
            for (int i = 1; i <= 5; i++) {
                stmt.setString(i, wildcard);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    historias.add(mapEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al buscar historias clínicas: " + e.getMessage(), e);
        }
        return historias;
    }

    /**
     * Busca una HistoriaClinica <b>activa</b> por <code>nroHistoria</code>
     * (coincidencia exacta).
     * <p>
     * Método crucial para la capa de Servicio, que lo utiliza para validar la regla
     * de negocio de <code>nroHistoria</code> único (RN-015).
     * </p>
     *
     * @param nroHistoria El número de historia exacto a buscar.
     * @return La {@link HistoriaClinica} encontrada, o <code>null</code> si no
     *         existe o está eliminada.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public HistoriaClinica selectByNroHistoria(String nroHistoria) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_NRO_HISTORIA_SQL)) {

            stmt.setString(1, nroHistoria);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al seleccionar historia clínica por número de historia: " + e.getMessage(),
                    e);
        }
        return null;
    }

    // ============ MÉTODOS HELPER (Mapeo y Parámetros) ============
    /**
     * {@inheritDoc}
     * <p>
     * Mapea <code>ResultSet</code> a <code>HistoriaClinica</code>, manejando
     * el <b>LEFT JOIN</b> con <code>GrupoSanguineo</code>.
     * </p>
     * <p>
     * <b>Lógica de Mapeo (Enum):</b>
     * 1. Lee <code>gs.nombre_enum</code> (ej: "A_PLUS") del <code>ResultSet</code>.
     * 2. Si no es <code>null</code>, usa <code>GrupoSanguineo.valueOf()</code>
     * para convertir el <code>String</code> en el <code>Enum</code>
     * correspondiente.
     * 3. Si es <code>null</code> (o inválido), el <code>grupoSanguineo</code>
     * del objeto permanece <code>null</code>.
     * </p>
     *
     * @param rs El ResultSet posicionado en la fila a mapear.
     * @return El objeto <code>HistoriaClinica</code> mapeado.
     * @throws SQLException Si una columna esperada no se encuentra.
     */
    @Override
    public HistoriaClinica mapEntity(ResultSet rs) throws SQLException {

        // 1. Mapear GrupoSanguineo
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

        // 2. Construye el objeto HC con el Enum (o null)
        return new HistoriaClinica(
                rs.getInt("id"),
                rs.getString("nro_historia"),
                grupo,
                rs.getString("antecedentes"),
                rs.getString("medicacion_actual"),
                rs.getString("observaciones"));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Establece los 5 parámetros (<code>?</code>) del <code>INSERT_SQL</code> y
     * <code>UPDATE_SQL</code>.
     * </p>
     * <p>
     * <b>Parámetros:</b>
     * 1: nro_historia (String)
     * 2: grupo_sanguineo_id (Integer | NULL)
     * 3: antecedentes (String | NULL)
     * 4: medicacion_actual (String | NULL)
     * 5: observaciones (String | NULL)
     * </p>
     * <p>
     * Llama al helper <code>getGrupoSanguineoId()</code> para convertir el
     * <code>Enum</code> en su <code>ID</code> de la tabla
     * <code>GrupoSanguineo</code>.
     * </p>
     *
     * @param stmt     El PreparedStatement (<code>INSERT</code> o
     *                 <code>UPDATE</code>).
     * @param historia La entidad de donde se obtienen los datos.
     * @throws SQLException Si ocurre un error al establecer los parámetros.
     */
    @Override
    public void setEntityParameters(PreparedStatement stmt, HistoriaClinica historia) throws SQLException {

        stmt.setString(1, historia.getNumeroHistoria());

        // Obtiene el ID (Integer) del Enum (GrupoSanguineo)
        stmt.setObject(2, historia.getGrupoSanguineo() != null
                ? getGrupoSanguineoId(historia.getGrupoSanguineo())
                : null, Types.INTEGER);

        stmt.setString(3, historia.getAntecedentes());
        stmt.setString(4, historia.getMedicacionActual());
        stmt.setString(5, historia.getObservaciones());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sincroniza el ID del objeto Java con el ID generado por la BD.
     * </p>
     *
     * @param stmt     El PreparedStatement que ejecutó el <code>INSERT</code>.
     * @param historia La entidad que recibirá el nuevo ID.
     * @throws SQLException Si <code>RETURN_GENERATED_KEYS</code> no devolvió un ID.
     */
    @Override
    public void setGeneratedId(PreparedStatement stmt, HistoriaClinica historia) throws SQLException {

        // try-with-resources para el ResultSet de claves generadas
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {

                // Asigna el ID de la BD al objeto Java
                historia.setId(generatedKeys.getInt(1));

            } else {
                throw new SQLException("La inserción de la historia falló! No se pudo obtener el ID generado");
            }
        }
    }

    /**
     * Obtiene el ID (PK) de la tabla <code>GrupoSanguineo</code> basado en el
     * nombre del <code>Enum</code>.
     * <p>
     * Ejecuta: <code>SELECT id FROM GrupoSanguineo WHERE nombre_enum = ?</code>
     * </p>
     *
     * <p>
     * <b>ADVERTENCIA DE DISEÑO (Performance y Transacciones):</b>
     * Este método abre y cierra su <b>propia conexión</b> a la base de datos
     * (<code>DatabaseConnection.getConnection()</code>).
     * </p>
     * <p>
     * Esto significa que:
     * 1. Es una consulta extra a la BD por cada <code>INSERT</code> o
     * <code>UPDATE</code>.
     * 2. Se ejecuta en una <b>conexión separada, no transaccional</b>, incluso si
     * el método <code>setEntityParameters</code> fue llamado por
     * <code>insertTx</code> o <code>updateTx</code>.
     * </p>
     * <p>
     * <b>Solución Ideal (Refactorización Futura):</b> Este método debería aceptar
     * una <code>Connection</code> como parámetro, la cual sería pasada desde
     * <code>setEntityParameters</code>, que a su vez la recibiría de
     * <code>insertTx/updateTx</code>.
     * </p>
     * <p>
     * <b>Decisión (TPI):</b> Para el alcance de este proyecto, donde la
     * concurrencia no es alta y la tabla <code>GrupoSanguineo</code> es pequeña y
     * estática, esta implementación es <b>aceptable</b>.
     * </p>
     *
     * @param grupo El <code>Enum</code> (ej: <code>GrupoSanguineo.A_PLUS</code>).
     * @return El <code>Integer</code> ID (PK) de la tabla, o <code>null</code>
     *         si no se encuentra.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    private Integer getGrupoSanguineoId(GrupoSanguineo grupo) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        SELECT_BY_NOMBRE_ENUM_SQL)) {

            stmt.setString(1, grupo.name()); // ej: "A_PLUS"

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        // Si no se encuentra (no debería pasar si la BD está sincronizada
        // con el Enum), retorna null.
        return null;
    }

}
