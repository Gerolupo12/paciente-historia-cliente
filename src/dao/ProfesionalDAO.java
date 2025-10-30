package dao;

import models.Profesional;

/**
 * DAO específico para la entidad Profesional.
 * Implementa las operaciones CRUD utilizando la interfaz GenericDAO.
 * 
 * @author alpha team
 */
public class ProfesionalDAO implements GenericDAO<Profesional> {

    @Override
    public void insert(Profesional profesional) throws Exception {

    }

    @Override
    public void update(Profesional profesional) throws Exception {

    }

    @Override
    public void delete(int id) throws Exception {

    }

    @Override
    public Profesional selectById(int id) throws Exception {
        return null;
    }

    @Override
    public Iterable<Profesional> selectAll() throws Exception {
        return null;
    }

    @Override
    public void recover(int id) throws Exception {

    }

}
