package service;

import dao.GenericDAO;
import models.Persona;

public class PersonaService implements GenericService<Persona> {

    private final GenericDAO<Persona> personaDAO;

    public PersonaService(GenericDAO<Persona> personaDAO) {
        this.personaDAO = personaDAO;
    }

    @Override
    public void insert(Persona persona) throws RuntimeException {

        validarEntidad(persona);

        try {
            System.out.println("Insertando persona: " + persona.getApellido() + ", " + persona.getNombre());
            personaDAO.insert(persona);
        } catch (Exception e) {
            throw new RuntimeException("Error al insertar la persona: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Persona persona) throws RuntimeException {

        validarEntidad(persona);

        try {
            System.out.println("Actualizando persona: " + persona.getApellido() + ", " + persona.getNombre());
            personaDAO.update(persona);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar la persona: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws RuntimeException {

        if (id <= 0) {
            throw new IllegalArgumentException("El ID de la persona debe ser mayor a cero.");
        }

        try {
            System.out.println("Eliminando persona con ID: " + id);
            personaDAO.delete(id);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar la persona: " + e.getMessage(), e);
        }
    }

    @Override
    public Persona selectById(int id) throws RuntimeException {

        try {
            return personaDAO.selectById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener la persona por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Iterable<Persona> selectAll() throws RuntimeException {

        try {
            return personaDAO.selectAll();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener todas las personas: " + e.getMessage(), e);
        }
    }

    @Override
    public void recover(int id) throws RuntimeException {

        try {
            personaDAO.recover(id);
        } catch (Exception e) {
            throw new RuntimeException("Error al recuperar la persona: " + e.getMessage(), e);
        }
    }

    @Override
    public Iterable<Persona> buscarPorFiltro(String filter) throws RuntimeException {

        try {
            return personaDAO.buscarPorFiltro(filter);
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar personas por nombre, apellido o DNI: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validarEntidad(Persona persona) throws RuntimeException {
        if (persona == null) {
            throw new IllegalArgumentException("La persona no puede ser nula.");
        }

        if (persona.getNombre() == null || persona.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la persona no puede estar vacío.");
        }

        if (persona.getApellido() == null || persona.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido de la persona no puede estar vacío.");
        }

        if (persona.getDni() == null || persona.getDni().trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI de la persona no puede estar vacío");
        }

        if (persona.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula.");
        }
        return true;
    }

}
