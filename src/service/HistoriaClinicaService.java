package service;

import dao.GenericDAO;
import java.util.List;
import models.HistoriaClinica;

public class HistoriaClinicaService implements GenericService<HistoriaClinica> {

    private final GenericDAO<HistoriaClinica> historiaClinicaDAO;

    public HistoriaClinicaService(GenericDAO<HistoriaClinica> historiaClinicaDAO) {

        if (historiaClinicaDAO == null) {
            throw new IllegalArgumentException("El DAO de Historia Clínica no puede ser nulo.");
        }
        this.historiaClinicaDAO = historiaClinicaDAO;
    }

    @Override
    public void insert(HistoriaClinica historia) throws Exception {

        validateEntity(historia);

        try {
            System.out.println("Insertando historia clinica ID: " + historia.getId());
            historiaClinicaDAO.insert(historia);
        } catch (Exception e) {
            throw new RuntimeException("Error al insertar la historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(HistoriaClinica historia) throws Exception {

        validateEntity(historia);

        if (historia.getId() <= 0) {
            throw new IllegalArgumentException("El ID de la historia clínica debe ser mayor a cero.");
        }

        try {
            System.out.println("Actualizando historia clinica ID: " + historia.getId());
            historiaClinicaDAO.update(historia);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar la historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws Exception {

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
    public HistoriaClinica selectById(int id) throws Exception {

        if (id <= 0) {
            throw new IllegalArgumentException("El ID de la historia clínica debe ser mayor a cero.");
        }

        try {
            System.out.println("\nObteniendo historia clinica con ID: " + id);
            return historiaClinicaDAO.selectById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener la historia clínica por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<HistoriaClinica> selectAll(boolean deleted) throws Exception {

        try {
            List<HistoriaClinica> historiasClinicas = historiaClinicaDAO.selectAll(deleted);

            String tipo = deleted ? "eliminadas" : "activas";

            System.out.println("\n=========================================");
            System.out.println(historiasClinicas.size() + " Historia Clínicas " + tipo + " encontradas.");
            System.out.println("=========================================");

            return historiasClinicas;
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener todas las historias clínicas: " + e.getMessage(), e);
        }
    }

    @Override
    public void recover(int id) throws Exception {

        if (id <= 0) {
            throw new IllegalArgumentException("El ID de la historia clínica debe ser mayor a cero.");
        }

        try {
            System.out.println("Recuperando historia clinica con ID: " + id);
            historiaClinicaDAO.recover(id);
        } catch (Exception e) {
            throw new RuntimeException("Error al recuperar la historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public List<HistoriaClinica> searchByFilter(String filter) throws Exception {

        if (filter == null || filter.trim().isEmpty()) {
            throw new IllegalArgumentException("El filtro de búsqueda no puede estar vacío");
        }

        try {
            List<HistoriaClinica> historiasClinicas = historiaClinicaDAO.searchByFilter(filter);

            System.out.println("\n=========================================");
            System.out.println(historiasClinicas.size() + " Historia Clínicas encontrados.");
            System.out.println("=========================================");

            return historiasClinicas;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al buscar historias clínicas por Grupo Sanguíneo, Antecedentes, Medicación Actual u Observaciones: "
                            + e.getMessage(),
                    e);
        }
    }

    @Override
    public void validateEntity(HistoriaClinica historia) throws Exception {

        if (historia == null) {
            throw new IllegalArgumentException("La historia clínica no puede ser nula.");
        }

        if (historia.getNumeroHistoria() == null || historia.getNumeroHistoria().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de historia no puede estar vacío.");
        }

        if (historia.getGrupoSanguineo() == null) {
            throw new IllegalArgumentException("El grupo sanguíneo no puede estar vacío.");
        }
    }
}