package service;

import models.Paciente;

public class PacienteService implements GenericService<Paciente> {

    @Override
    public void insert(Paciente persona) throws Exception {

    }

    @Override
    public void update(Paciente persona) throws Exception {

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
    public Iterable<Paciente> buscarPorFiltro(String filter) throws Exception {
        return null;
    }

    @Override
    public boolean validarEntidad(Paciente persona) throws Exception {
        return false;
    }

}
