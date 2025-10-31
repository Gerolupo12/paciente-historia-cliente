package dao;

import config.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.GrupoSanguineo;
import models.HistoriaClinica;
import models.Paciente;

/**
 * DAO específico para la entidad Paciente.
 * Implementa las operaciones CRUD utilizando la interfaz GenericDAO.
 * 
 * @author alpha team
 */
public class PacienteDAO implements GenericDAO<Paciente> {

    private static final String INSERT_SQL = """
                INSERT INTO Paciente
                (nombre, apellido, dni, fecha_nacimiento, historia_clinica_id)
                VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
                UPDATE Paciente
                SET nombre = ?, apellido = ?, dni = ?, fecha_nacimiento = ?,
                historia_clinica_id = ?
                WHERE id = ?
            """;

    private static final String DELETE_SQL = "UPDATE Paciente SET eliminado = TRUE WHERE id = ?";

    private static final String SELECT_BY_ID_SQL = """
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
                WHERE p.id = ? AND p.eliminado = FALSE
            """;

    private static final String SELECT_ALL_SQL = """
                SELECT
                    p.id AS paciente_id,
                    p.nombre,
                    p.apellido,
                    p.dni,
                    p.fecha_nacimiento,
                    hc.id AS hc_id,
                    hc.nro_historia,
                    gs.nombre_enum,
                    hc.antecedentes,
                    hc.medicacion_actual,
                    hc.observaciones
                FROM Paciente p
                LEFT JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id
                LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
                WHERE p.eliminado = FALSE
                ORDER BY p.apellido, p.nombre
            """;

    private static final String RECOVER_SQL = "UPDATE Paciente SET eliminado = FALSE WHERE id = ?";

    private static final String SEARCH_BY_FILTER_SQL = """
                SELECT
                    p.id AS paciente_id,
                    p.nombre,
                    p.apellido,
                    p.dni,
                    p.fecha_nacimiento,
                    hc.id AS hc_id,
                    hc.nro_historia,
                    gs.nombre_enum,
                    hc.antecedentes,
                    hc.medicacion_actual,
                    hc.observaciones
                FROM Paciente p
                LEFT JOIN HistoriaClinica hc ON p.historia_clinica_id = hc.id
                LEFT JOIN GrupoSanguineo gs ON hc.grupo_sanguineo_id = gs.id
                WHERE p.eliminado = FALSE
                AND (
                    LOWER(p.nombre) LIKE LOWER(?)
                    OR LOWER(p.apellido) LIKE LOWER(?)
                    OR p.dni LIKE ?
                )
                ORDER BY p.apellido, p.nombre
            """;

    @Override
    public void insert(Paciente paciente) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        INSERT_SQL, Statement.RETURN_GENERATED_KEYS);) {

            setEntityParameters(stmt, paciente);
            stmt.executeUpdate();
            setGeneratedId(stmt, paciente);

        } catch (SQLException e) {
            throw e;
        }
    }

    @Override
    public void update(Paciente paciente) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            setEntityParameters(stmt, paciente);
            stmt.executeUpdate();
            setGeneratedId(stmt, paciente);

        } catch (SQLException e) {
            System.err.println("Error al actualizar el paciente: " + e.getMessage());
            throw new SQLException("Error al actualizar el paciente en la base de datos.", e);
        } catch (Exception e) {
            // Captura otras posibles excepciones generales
            System.err.println("Error inesperado al actualizar el paciente: " + e.getMessage());
            throw e; // Relanza la excepción original
        }
    }

    @Override
    public void delete(int id) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("No se pudo marcar como eliminado al paciente con ID " + id);
        }
    }

    @Override
    public Paciente selectById(int id) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id);

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

    @Override
    public Iterable<Paciente> selectAll() throws SQLException {

        List<Paciente> pacientes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
                ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                pacientes.add(mapEntity(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error al seleccionar todos los pacientes: " + e.getMessage(), e);
        }
        return pacientes;
    }

    @Override
    public void recover(int id) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(RECOVER_SQL)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("No se pudo recuperar el paciente con ID " + id);
        }
    }

    @Override
    public Iterable<Paciente> searchByFilter(String filter) throws Exception {

        List<Paciente> pacientes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(SEARCH_BY_FILTER_SQL)) {
            String wildcard = "%" + filter.trim() + "%";
            stmt.setString(1, wildcard);
            stmt.setString(2, wildcard);
            stmt.setString(3, wildcard);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pacientes.add(mapEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al buscar pacientes por nombre, apellido o DNI: " + e.getMessage(), e);
        }
        return pacientes;
    }

    /**
     * Convierte un ResultSet en un objeto Paciente.
     * 
     * @param rs ResultSet con los datos del paciente.
     * @return Objeto Paciente mapeado.
     * @throws SQLException Si ocurre un error al acceder a los datos del ResultSet.
     */
    @Override
    public Paciente mapEntity(ResultSet rs) throws SQLException {

        GrupoSanguineo grupo = null;
        String nombreEnum = rs.getString("nombre_enum");
        if (nombreEnum != null) {
            try {
                grupo = GrupoSanguineo.valueOf(nombreEnum);
            } catch (IllegalArgumentException e) {
                grupo = null;
            }
        }

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

        return new Paciente(
                rs.getInt("paciente_id"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("dni"),
                rs.getDate("fecha_nacimiento") != null
                        ? rs.getDate("fecha_nacimiento").toLocalDate()
                        : null,
                historiaClinica);
    }

    @Override
    public void setEntityParameters(PreparedStatement stmt, Paciente paciente) throws SQLException {

        stmt.setString(1, paciente.getNombre());
        stmt.setString(2, paciente.getApellido());
        stmt.setString(3, paciente.getDni());

        try {
            // Obtiene el LocalDate del objeto paciente
            LocalDate fechaNacimiento = paciente.getFechaNacimiento();
            // Verifica si la fecha es nula antes de convertir
            if (fechaNacimiento != null) {
                // Convierte LocalDate a java.sql.Date
                Date sqlFechaNacimiento = Date.valueOf(fechaNacimiento);
                // Asigna el valor al PreparedStatement en el índice 4
                stmt.setDate(4, sqlFechaNacimiento);
            } else {
                // Si la fecha es nula en el objeto Java, envía NULL a la base de datos
                stmt.setNull(4, Types.DATE);
            }
        } catch (SQLException e) {
            throw e;
        }

        setHistoriaClinicaId(stmt, 5, paciente.getHistoriaClinica());
    }

    @Override
    public void setGeneratedId(PreparedStatement stmt, Paciente paciente) throws SQLException {

        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                paciente.setId(generatedKeys.getInt(1));
                System.out.println("Paciente insertado con ID: " + paciente.getId());
            } else {
                throw new SQLException("La inserción del paciente falló! No se pudo obtener el ID generado");
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    private void setHistoriaClinicaId(PreparedStatement stmt, int parameterIndex, HistoriaClinica historia)
            throws SQLException {

        if (historia != null && historia.getId() > 0) {
            stmt.setInt(parameterIndex, historia.getId());
        } else {
            stmt.setNull(parameterIndex, Types.INTEGER);
        }
    }

}
