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

    @Override
    public void insert(HistoriaClinica hc) throws SQLException {

        String sql = """
                    INSERT INTO HistoriaClinica
                        (nro_historia, grupo_sanguineo_id, antecedentes,
                        medicacion_actual, observaciones)
                    VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hc.getNumeroHistoria());
            stmt.setObject(2, hc.getGrupoSanguineo() != null
                    ? getGrupoSanguineoId(hc.getGrupoSanguineo())
                    : null, Types.INTEGER);
            stmt.setString(3, hc.getAntecedentes());
            stmt.setString(4, hc.getMedicacionActual());
            stmt.setString(5, hc.getObservaciones());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error al insertar historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(HistoriaClinica hc) throws SQLException {

        String sql = """
                    UPDATE HistoriaClinica
                    SET nro_historia = ?, grupo_sanguineo_id = ?, antecedentes = ?,
                        medicacion_actual = ?, observaciones = ?
                    WHERE id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hc.getNumeroHistoria());
            stmt.setObject(2, hc.getGrupoSanguineo() != null
                    ? getGrupoSanguineoId(hc.getGrupoSanguineo())
                    : null, Types.INTEGER);
            stmt.setString(3, hc.getAntecedentes());
            stmt.setString(4, hc.getMedicacionActual());
            stmt.setString(5, hc.getObservaciones());
            stmt.setInt(6, hc.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error al actualizar historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws SQLException {

        String sql = "UPDATE HistoriaClinica SET eliminado = TRUE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error al eliminar historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public void recover(int id) throws SQLException {

        String sql = "UPDATE HistoriaClinica SET eliminado = FALSE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error al recuperar historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public HistoriaClinica selectById(int id) throws SQLException {

        String sql = """
                    SELECT
                        hc.*,
                        gs.nombre_enum
                    FROM HistoriaClinica hc
                    LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
                    WHERE hc.id = ? AND hc.eliminado = FALSE
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapHistoriaClinica(rs);
                }
            }

        } catch (SQLException e) {
            throw new SQLException("Error al obtener historia clínica por ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Iterable<HistoriaClinica> selectAll() throws SQLException {

        String sql = """
                    SELECT
                        hc.*,
                        gs.nombre_enum
                    FROM HistoriaClinica hc
                    LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
                    WHERE hc.eliminado = FALSE
                    ORDER BY hc.id
                """;

        List<HistoriaClinica> historias = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                historias.add(mapHistoriaClinica(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error al obtener todas las historias clínicas: " + e.getMessage(), e);
        }
        return historias;
    }

    @Override
    public Iterable<HistoriaClinica> buscarPorFiltro(String filter) throws SQLException {

        String sql = """
                    SELECT hc.*, gs.nombre_enum
                    FROM HistoriaClinica hc
                    LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
                    WHERE hc.eliminado = FALSE
                        AND (
                            LOWER(hc.nro_historia) LIKE LOWER(?)
                            OR LOWER(hc.antecedentes) LIKE LOWER(?)
                            OR LOWER(hc.medicacion_actual) LIKE LOWER(?)
                            OR LOWER(hc.observaciones) LIKE LOWER(?)
                        )
                    ORDER BY hc.nro_historia
                """;

        List<HistoriaClinica> historias = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            String wildcard = "%" + filter.trim() + "%";
            for (int i = 1; i <= 4; i++) {
                stmt.setString(i, wildcard);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    historias.add(mapHistoriaClinica(rs));
                }
            }

        } catch (SQLException e) {
            throw new SQLException("Error al buscar historias clínicas: " + e.getMessage(), e);
        }
        return historias;
    }

    /**
     * Convierte un ResultSet en un objeto HistoriaClinica.
     * 
     * @param rs ResultSet con los datos de la historia clínica.
     * @return Objeto HistoriaClinica mapeado.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    private HistoriaClinica mapHistoriaClinica(ResultSet rs) throws SQLException {

        GrupoSanguineo grupo = null;
        String nombreEnum = rs.getString("nombre_enum");
        if (nombreEnum != null) {
            try {
                grupo = GrupoSanguineo.valueOf(nombreEnum);
            } catch (IllegalArgumentException e) {
                System.err.println("⚠️ Grupo sanguíneo inválido: " + nombreEnum);
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

}
