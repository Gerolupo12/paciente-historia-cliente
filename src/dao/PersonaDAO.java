package dao;

import config.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.Persona;

/**
 * DAO específico para la entidad Persona.
 * Implementa las operaciones CRUD utilizando la interfaz GenericDAO.
 * 
 * @author alpha team
 */
public class PersonaDAO implements GenericDAO<Persona> {

    @Override
    public void insert(Persona persona) throws SQLException {

        String sql = "INSERT INTO Persona (nombre, apellido, dni, fecha_nacimiento)"
                + " VALUES (?, ?, ?, ?);";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS);) {

            stmt.setString(1, persona.getNombre());
            stmt.setString(2, persona.getApellido());
            stmt.setString(3, persona.getDni());

            try {
                // Convierte el objeto LocalDate a java.sql.Date
                LocalDate fechaNacimiento = persona.getFechaNacimiento();
                Date sqlFechaNacimiento = Date.valueOf(fechaNacimiento);
                // Asigna el valor al PreparedStatement
                stmt.setDate(4, sqlFechaNacimiento);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    persona.setId(generatedKeys.getInt(1));
                    System.out.println("Persona insertada con ID: " + persona.getId());
                } else {
                    throw new SQLException("La inserción de la persona falló! No se pudo obtener el ID generado");
                }
            } catch (SQLException e) {
                throw e;
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    @Override
    public void update(Persona persona) throws SQLException {
        String sql = "UPDATE Persona"
                + " SET nombre = ?, apellido = ?, dni = ?, fecha_nacimiento = ?"
                + " WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, persona.getNombre());
            stmt.setString(2, persona.getApellido());
            stmt.setString(3, persona.getDni());

            try {
                // Obtiene el LocalDate del objeto Persona
                LocalDate fechaNacimiento = persona.getFechaNacimiento();
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

            stmt.setInt(5, persona.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al actualizar la Persona: " + e.getMessage());
            throw new SQLException("Error al actualizar la persona en la base de datos.", e);
        } catch (Exception e) {
            // Captura otras posibles excepciones generales
            System.err.println("Error inesperado al actualizar la Persona: " + e.getMessage());
            throw e; // Relanza la excepción original
        }
    }

    @Override
    public void delete(int id) throws SQLException {

        String sql = "UPDATE Persona SET eliminado = TRUE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("No se pudo marcar como eliminada la persona con ID " + id);
        }
    }

    @Override
    public Persona selectById(int id) throws SQLException {

        String sql = "SELECT * FROM Persona WHERE id = ? AND eliminado = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Persona(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("dni"),
                            rs.getDate("fecha_nacimiento").toLocalDate());
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al seleccionar persona por ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Iterable<Persona> selectAll() throws SQLException {

        String sql = "SELECT * FROM Persona WHERE eliminado = FALSE";
        List<Persona> personas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                personas.add(new Persona(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni"),
                        rs.getDate("fecha_nacimiento") != null
                                ? rs.getDate("fecha_nacimiento").toLocalDate()
                                : null));
            }
        } catch (SQLException e) {
            throw new SQLException("Error al seleccionar todas las personas: " + e.getMessage(), e);
        }
        return personas;
    }

    @Override
    public void recover(int id) throws SQLException {

        String sql = "UPDATE Persona SET eliminado = FALSE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("No se pudo recuperar la persona con ID " + id);
        }
    }

    public List<Persona> buscarPorNombreApellido(String filtro) throws SQLException {

        String sql = "SELECT id, nombre, apellido, dni, fecha_nacimiento"
                + " FROM Persona"
                + " WHERE eliminado = FALSE AND (nombre LIKE ? OR apellido LIKE ?)";
        List<Persona> personas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + filtro + "%");
            stmt.setString(2, "%" + filtro + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Persona persona = new Persona();
                persona.setId(rs.getInt("id"));
                persona.setNombre(rs.getString("nombre"));
                persona.setApellido(rs.getString("apellido"));
                persona.setDni(rs.getString("dni"));
                persona.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
                personas.add(persona);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al buscar personas por nombre o apellido: " + e.getMessage(), e);
        }
        return personas;
    }

}
