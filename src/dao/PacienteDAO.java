package dao;

import java.sql.Connection;
import models.Paciente;

/**
 * DAO específico para la entidad Paciente.
 * Implementa las operaciones CRUD utilizando la interfaz GenericDAO.
 * 
 * @author alpha team
 */
public class PacienteDAO implements GenericDAO<Paciente> {

    @Override
    public void insert(Paciente entity) throws Exception {

    }

    @Override
    public void update(Paciente entity) throws Exception {

    }

    @Override
    public void delete(int id) throws Exception {

    }

    @Override
    public Paciente selectById(int id) throws Exception {
        return null;
    }

    @Override
    public Iterable<Paciente> selectAll() throws Exception {
        return null;
    }

    @Override
    public void recover(int id) throws Exception {

    }

    @Override
    public void saveTx(Paciente entity, Connection conn) throws Exception {

    }

}
