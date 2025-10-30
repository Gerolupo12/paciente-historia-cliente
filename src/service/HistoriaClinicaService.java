package service;

import dao.GenericDAO;
import models.HistoriaClinica;

public class HistoriaClinicaService implements GenericService<HistoriaClinica> {

    private final GenericDAO<HistoriaClinica> historiaClinicaDAO;

    public HistoriaClinicaService(GenericDAO<HistoriaClinica> historiaClinicaDAO) {
        this.historiaClinicaDAO = historiaClinicaDAO;
    }

    @Override
    public void insert(HistoriaClinica historia) throws RuntimeException {

        validarEntidad(historia);

        try {
            System.out.println("Insertando historia clinica ID: " + historia.getId());
            historiaClinicaDAO.insert(historia);
        } catch (Exception e) {
            throw new RuntimeException("Error al insertar la historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(HistoriaClinica historia) throws RuntimeException {

        validarEntidad(historia);

        try {
            System.out.println("Actualizando historia clinica ID: " + historia.getId());
            historiaClinicaDAO.update(historia);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar la historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws RuntimeException {

        if (id <= 0) {
            throw new IllegalArgumentException("El ID de la historia clínica debe ser mayor a cero.");
        }

        try {
            System.out.println("Eliminando historia clinica con ID: " + id);
            historiaClinicaDAO.delete(id);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar la historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public HistoriaClinica selectById(int id) throws RuntimeException {

        try {
            return historiaClinicaDAO.selectById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener la historia clínica por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Iterable<HistoriaClinica> selectAll() throws RuntimeException {

        try {
            return historiaClinicaDAO.selectAll();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener todas las historias clínicas: " + e.getMessage(), e);
        }
    }

    @Override
    public void recover(int id) throws RuntimeException {

        try {
            historiaClinicaDAO.recover(id);
        } catch (Exception e) {
            throw new RuntimeException("Error al recuperar la historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public Iterable<HistoriaClinica> buscarPorFiltro(String filter) throws RuntimeException {

        try {
            return historiaClinicaDAO.buscarPorFiltro(filter);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al buscar historias clínicas por Grupo Sanguíneo, Antecedentes Medicación Actual u Observaciones: "
                            + e.getMessage(),
                    e);
        }
    }

    @Override
    public boolean validarEntidad(HistoriaClinica historia) throws RuntimeException {
        if (historia == null) {
            throw new IllegalArgumentException("La historia clínica no puede ser nula.");
        }
        return false;
    }
}