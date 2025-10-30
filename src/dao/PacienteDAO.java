package dao;

import java.sql.*;
import models.Paciente;

/**
 * DAO específico para la entidad Paciente.
 * Implementa las operaciones CRUD utilizando la interfaz GenericDAO.
 * 
 * @author alpha team
 */
public class PacienteDAO implements GenericDAO<Paciente> {

    @Override
    public void insert(Paciente paciente) throws SQLException {

    }

    @Override
    public void update(Paciente paciente) throws SQLException {

    }

    @Override
    public void delete(int id) throws SQLException {

    }

    @Override
    public Paciente selectById(int id) throws SQLException {
        return null;
    }

    @Override
    public Iterable<Paciente> selectAll() throws SQLException {
        return null;
    }

    @Override
    public void recover(int id) throws SQLException {

    }

}
