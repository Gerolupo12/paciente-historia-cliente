package dao;

import config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.GrupoSanguineo;
import models.HistoriaClinica;

/**
 * Implementación DAO para la entidad HistoriaClinica.
 * Gestiona operaciones CRUD sobre la tabla HistoriaClinica.
 * 
 * @author alpha team
 */
public class HistoriaClinicaDAO implements GenericDAO<HistoriaClinica> {

    // Sentencias SQL
    private static final String INSERT_SQL = """
                INSERT INTO HistoriaClinica
                    (nro_historia, grupo_sanguineo_id, antecedentes,
                    medicacion_actual, observaciones)
                VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
                UPDATE HistoriaClinica
                SET nro_historia = ?, grupo_sanguineo_id = ?, antecedentes = ?,
                    medicacion_actual = ?, observaciones = ?
                WHERE id = ?
            """;

    private static final String DELETE_SQL = """
                UPDATE HistoriaClinica
                SET eliminado = TRUE
                WHERE id = ?
            """;

    private static final String SELECT_BY_ID_SQL = """
                SELECT
                    hc.*,
                    gs.nombre_enum
                FROM HistoriaClinica hc
                LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
                WHERE hc.id = ? AND hc.eliminado = FALSE
            """;

    private static final String SELECT_ALL_SQL = """
                SELECT
                    hc.*,
                    gs.nombre_enum
                FROM HistoriaClinica hc
                LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
                WHERE hc.eliminado = ?
                ORDER BY hc.id
            """;

    private static final String RECOVER_SQL = """
                UPDATE HistoriaClinica
                SET eliminado = FALSE
                WHERE id = ?
            """;

    private static final String SEARCH_BY_FILTER_SQL = """
                SELECT hc.*, gs.nombre_enum
                FROM HistoriaClinica hc
                LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
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

    private static final String SELECT_BY_NRO_HISTORIA_SQL = """
                SELECT
                    hc.*,
                    gs.nombre_enum
                FROM HistoriaClinica hc
                LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
                WHERE hc.nro_historia = ? AND hc.eliminado = FALSE
            """;

    /**
     * Inserta una nueva historia clínica en la base de datos.
     * 
     * @param hc HistoriaClinica a insertar.
     * @throws SQLException Si ocurre un error durante la operación.
     */
    @Override
    public void insert(HistoriaClinica hc) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            setEntityParameters(stmt, hc);
            stmt.executeUpdate();
            setGeneratedId(stmt, hc);

        } catch (SQLException e) {
            throw new SQLException("Error al insertar historia clínica: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza una historia clínica existente en la base de datos.
     * 
     * @param hc HistoriaClinica a actualizar.
     * @throws SQLException Si ocurre un error durante la operación.
     */
    @Override
    public void update(HistoriaClinica hc) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            setEntityParameters(stmt, hc);
            stmt.setInt(6, hc.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error al actualizar historia clínica: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina (marca como eliminado) una historia clínica de la base de datos.
     * 
     * @param id ID de la historia clínica a eliminar.
     * @throws SQLException Si ocurre un error durante la operación.
     */
    @Override
    public void delete(int id) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error al eliminar historia clínica: " + e.getMessage(), e);
        }
    }

    /**
     * Selecciona una historia clínica por su ID.
     * 
     * @param id ID de la historia clínica a seleccionar.
     * @return La historia clínica encontrada o null si no existe.
     * @throws SQLException Si ocurre un error durante la operación.
     */
    @Override
    public HistoriaClinica selectById(int id) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id);
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
     * Selecciona todas las historias clínicas de la base de datos.
     * 
     * @return Lista de todas las historias clínicas.
     * @throws SQLException Si ocurre un error durante la operación.
     */
    @Override
    public List<HistoriaClinica> selectAll(boolean deleted) throws SQLException {

        List<HistoriaClinica> historias = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL)) {

            stmt.setBoolean(1, deleted);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    historias.add(mapEntity(rs));
                }
            } catch (SQLException e) {
                throw new SQLException("Error al mapear historias clínicas: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new SQLException("Error al obtener todas las historias clínicas: " + e.getMessage(), e);
        }
        return historias;
    }

    /**
     * Recupera una historia clínica eliminada lógicamente por su ID.
     * 
     * @param id ID de la historia clínica a recuperar.
     * @throws SQLException Si ocurre un error durante la operación.
     */
    @Override
    public void recover(int id) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(RECOVER_SQL)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error al recuperar historia clínica: " + e.getMessage(), e);
        }
    }

    /**
     * Busca historias clínicas que coincidan con un filtro en sus atributos.
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
     * Busca una historia clínica por su número de historia.
     * 
     * @param nroHistoria Número de historia clínica a buscar.
     * @return La historia clínica encontrada o null si no existe.
     * @throws SQLException Si ocurre un error durante la operación.
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

    /**
     * Convierte un ResultSet en un objeto HistoriaClinica.
     * 
     * @param rs ResultSet con los datos de la historia clínica.
     * @return Objeto HistoriaClinica mapeado.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    @Override
    public HistoriaClinica mapEntity(ResultSet rs) throws SQLException {

        GrupoSanguineo grupo = null;
        String nombreEnum = rs.getString("nombre_enum");
        if (nombreEnum != null) {
            try {
                grupo = GrupoSanguineo.valueOf(nombreEnum);
            } catch (IllegalArgumentException e) {
                System.err.println("Grupo sanguíneo inválido: " + nombreEnum);
            }
        }

        return new HistoriaClinica(
                rs.getInt("id"),
                rs.getString("nro_historia"),
                grupo,
                rs.getString("antecedentes"),
                rs.getString("medicacion_actual"),
                rs.getString("observaciones"));
    }

    /**
     * Obtiene el ID del grupo sanguíneo en base a su nombre_enum.
     * 
     * @param grupo Grupo sanguíneo
     * @return ID del grupo sanguíneo o null si no se encuentra
     */
    private Integer getGrupoSanguineoId(GrupoSanguineo grupo) throws SQLException {

        String sql = "SELECT id FROM GrupoSanguineo WHERE nombre_enum = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, grupo.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return null;
    }

    /**
     * Establece los parámetros de una historia clínica en un PreparedStatement.
     * 
     * @param stmt     PreparedStatement donde se establecerán los parámetros.
     * @param historia entidad cuyos datos se usarán.
     * @throws SQLException Si ocurre un error al establecer los parámetros.
     */
    @Override
    public void setEntityParameters(PreparedStatement stmt, HistoriaClinica historia) throws SQLException {

        HistoriaClinica hc = (HistoriaClinica) historia;
        stmt.setString(1, hc.getNumeroHistoria());
        stmt.setObject(2, hc.getGrupoSanguineo() != null
                ? getGrupoSanguineoId(hc.getGrupoSanguineo())
                : null, Types.INTEGER);
        stmt.setString(3, hc.getAntecedentes());
        stmt.setString(4, hc.getMedicacionActual());
        stmt.setString(5, hc.getObservaciones());
    }

    /**
     * Establece el ID generado automáticamente después de una inserción.
     * 
     * @param stmt     PreparedStatement utilizado para la inserción.
     * @param historia HistoriaClinica a la que se le asignará el ID generado.
     * @throws SQLException Si ocurre un error al obtener el ID generado.
     */
    @Override
    public void setGeneratedId(PreparedStatement stmt, HistoriaClinica historia) throws SQLException {

        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                historia.setId(generatedKeys.getInt(1));
                System.out.println("Historia insertada con ID: " + historia.getId());
            } else {
                throw new SQLException("La inserción de la historia falló! No se pudo obtener el ID generado");
            }
        } catch (SQLException e) {
            throw e;
        }
    }

}
