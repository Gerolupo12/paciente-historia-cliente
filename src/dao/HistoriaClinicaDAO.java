package dao;

import config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.GrupoSanguineo;
import models.HistoriaClinica;
import models.Profesional;

/**
 * DAO específico para la entidad HistoriaClinica.
 * Implementa las operaciones CRUD utilizando la interfaz GenericDAO.
 * 
 * @author alpha team
 */
public class HistoriaClinicaDAO implements GenericDAO<HistoriaClinica> {

    @Override
    public void insert(HistoriaClinica historia) throws Exception {

        String sql = "INSERT INTO HistoriaClinica"
                + " (numero_historia, grupo_sanguineo, antecedentes,"
                + " medicacion_actual, observaciones, profesional_id)"
                + " VALUES (?, ?, ?, ?, ?, ?);";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, historia.getNumeroHistoria());

            GrupoSanguineo gs = historia.getGrupoSanguineo();
            if (gs != null && gs.getDbId() > 0) {
                // Guarda el ID numérico asociado al enum
                stmt.setInt(2, gs.getDbId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            stmt.setString(3, historia.getAntecedentes());
            stmt.setString(4, historia.getMedicacionActual());
            stmt.setString(5, historia.getObservaciones());

            Profesional prof = historia.getProfesional();
            if (prof != null && prof.getId() > 0) {
                // Guarda el ID numérico asociado al profesional
                stmt.setInt(6, prof.getId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    historia.setId(generatedKeys.getInt(1));
                    System.out.println("Historia Clínica insertada con ID: " + historia.getId());
                } else {
                    throw new SQLException(
                            "La inserción de la Historia Clínica falló! No se pudo obtener el ID generado");
                }
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    @Override
    public void update(HistoriaClinica historia) throws Exception {

        String sql = "UPDATE HistoriaClinica"
                + " SET numero_historia = ?, grupo_sanguineo = ?,"
                + " antecedentes = ?, medicacion_actual = ?, observaciones = ?,"
                + " profesional_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, historia.getNumeroHistoria());

            GrupoSanguineo gs = historia.getGrupoSanguineo();
            if (gs != null && gs.getDbId() > 0) {
                stmt.setInt(2, gs.getDbId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            stmt.setString(3, historia.getAntecedentes());
            stmt.setString(4, historia.getMedicacionActual());
            stmt.setString(5, historia.getObservaciones());

            Profesional prof = historia.getProfesional();
            if (prof != null && prof.getId() > 0) {
                stmt.setInt(6, prof.getId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setInt(7, historia.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al actualizar la Historia Clínica: " + e.getMessage());
            throw new SQLException("Error al actualizar la historia clínica en la base de datos.", e);
        } catch (Exception e) {
            System.err.println("Error inesperado al actualizar la Historia Clínica: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) throws Exception {

        String sql = "UPDATE HistoriaClinica SET eliminado = TRUE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("No se pudo marcar como eliminada la historia clínica con ID " + id);
        }
    }

    @Override
    public HistoriaClinica selectById(int id) throws Exception {

        String sql = "SELECT * FROM HistoriaClinica WHERE id = ? AND eliminado = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    HistoriaClinica historia = new HistoriaClinica();
                    historia.setId(rs.getInt("id"));
                    historia.setNumeroHistoria(rs.getString("numero_historia"));

                    // Obtener GrupoSanguineo
                    int grupoSanguineoId = rs.getInt("grupo_sanguineo");
                    if (!rs.wasNull()) {
                        for (GrupoSanguineo gs : GrupoSanguineo.values()) {
                            if (gs.getDbId() == grupoSanguineoId) {
                                historia.setGrupoSanguineo(gs);
                                break;
                            }
                        }
                    }

                    historia.setAntecedentes(rs.getString("antecedentes"));
                    historia.setMedicacionActual(rs.getString("medicacion_actual"));
                    historia.setObservaciones(rs.getString("observaciones"));

                    // Obtener Profesional
                    int profesionalId = rs.getInt("profesional_id");
                    if (!rs.wasNull()) {
                        ProfesionalDAO profesionalDAO = new ProfesionalDAO();
                        Profesional profesional = profesionalDAO.selectById(profesionalId);
                        historia.setProfesional(profesional);
                    }
                    return historia;
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al seleccionar historia clínica por ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Iterable<HistoriaClinica> selectAll() throws Exception {

        String sql = "SELECT * FROM HistoriaClinica WHERE eliminado = FALSE";
        List<HistoriaClinica> historias = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                HistoriaClinica historia = new HistoriaClinica();
                historia.setId(rs.getInt("id"));
                historia.setNumeroHistoria(rs.getString("numero_historia"));

                // Obtener GrupoSanguineo
                int grupoSanguineoId = rs.getInt("grupo_sanguineo");
                if (!rs.wasNull()) {
                    for (GrupoSanguineo gs : GrupoSanguineo.values()) {
                        if (gs.getDbId() == grupoSanguineoId) {
                            historia.setGrupoSanguineo(gs);
                            break;
                        }
                    }
                }

                historia.setAntecedentes(rs.getString("antecedentes"));
                historia.setMedicacionActual(rs.getString("medicacion_actual"));
                historia.setObservaciones(rs.getString("observaciones"));

                // Obtener Profesional
                int profesionalId = rs.getInt("profesional_id");
                if (!rs.wasNull()) {
                    ProfesionalDAO profesionalDAO = new ProfesionalDAO();
                    Profesional profesional = profesionalDAO.selectById(profesionalId);
                    historia.setProfesional(profesional);
                }
                historias.add(historia);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al seleccionar todas las historias clínicas: " + e.getMessage(), e);
        }
        return historias;
    }

    @Override
    public void recover(int id) throws Exception {

        String sql = "UPDATE HistoriaClinica SET eliminado = FALSE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("No se pudo recuperar la historia clínica con ID " + id);
        }
    }

}
