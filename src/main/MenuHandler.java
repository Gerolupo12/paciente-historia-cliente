package main;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Scanner;
import models.GrupoSanguineo;
import models.HistoriaClinica;
import models.Paciente;
import service.HistoriaClinicaService;
import service.PacienteService;

public class MenuHandler {

    private final Scanner scanner;
    private final PacienteService pacienteService;
    private final HistoriaClinicaService historiaClinicaService;

    public MenuHandler(Scanner scanner, PacienteService pacienteService,
            HistoriaClinicaService historiaClinicaService) {

        if (scanner == null) {
            throw new IllegalArgumentException("Scanner no puede ser null");
        }

        if (pacienteService == null) {
            throw new IllegalArgumentException("PacienteService no puede ser null");
        }

        if (historiaClinicaService == null) {
            throw new IllegalArgumentException("HistoriaClinicaService no puede ser null");
        }
        this.scanner = scanner;
        this.pacienteService = pacienteService;
        this.historiaClinicaService = historiaClinicaService;
    }

    public void readPaciente() {

        try {
            System.out.print(
                    "\n¿Desea (1) listar todos, (2) buscar por nombre/apellido o (3) buscar por DNI? \nIngrese opcion -> ");
            int subopcion = Integer.parseInt(scanner.nextLine());

            List<Paciente> pacientes = List.of();

            switch (subopcion) {
                case 1 -> pacientes = (List<Paciente>) pacienteService.selectAll(false);
                case 2 -> {
                    System.out.print("\nIngrese texto a buscar -> ");
                    String filtro = scanner.nextLine().trim();
                    pacientes = (List<Paciente>) pacienteService.searchByFilter(filtro);
                }
                case 3 -> {
                    System.out.print("\nIngrese DNI a buscar -> ");
                    String dni = scanner.nextLine().trim();
                    Paciente paciente = pacienteService.selectByDni(dni);
                    pacientes = (paciente != null) ? List.of(paciente) : List.of();
                }
                default -> System.out.println("Opcion invalida.");
            }

            displayPacientes(pacientes);

        } catch (Exception e) {
            System.err.println("Error al listar pacientes: " + e.getMessage());
        }
    }

    public void createPaciente() {

        try {
            System.out.println("\nIngrese los datos del paciente:");

            String nombre = "";
            String apellido = "";
            String dni = "";
            LocalDate fechaNacimiento = null;

            do {
                System.out.print("Nombre -> ");
                nombre = scanner.nextLine().trim();
                System.out.print("Apellido -> ");
                apellido = scanner.nextLine().trim();
                System.out.print("DNI -> ");
                dni = scanner.nextLine().trim();
                System.out.print("Fecha de nacimiento (YYYY-MM-DD) -> ");
                fechaNacimiento = LocalDate.parse(scanner.nextLine().trim());
            } while (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || fechaNacimiento == null);

            HistoriaClinica historiaClinica = null;
            System.out.print("¿Desea agregar una historia clínica? (s/n) -> ");
            if (scanner.nextLine().equalsIgnoreCase("s")) {
                historiaClinica = createHistoriaClinica();
            }

            Paciente paciente = new Paciente(0, nombre, apellido, dni, fechaNacimiento);
            paciente.setHistoriaClinica(historiaClinica);
            pacienteService.insert(paciente);
            System.out.println("Paciente creado exitosamente con ID: " + paciente.getId());
        } catch (Exception e) {
            System.err.println("Error al crear paciente: " + e.getMessage());
        }
    }

    public void updatePaciente() {

        try {
            System.out.print("\nIngrese el ID del paciente a actualizar -> ");
            int id = Integer.parseInt(scanner.nextLine());
            Paciente paciente = pacienteService.selectById(id);

            if (paciente == null) {
                System.out.println("Paciente no encontrado.");
                return;
            }

            System.out.println("Paciente actual: " + paciente.getApellido() + ", " + paciente.getNombre());
            System.out.print("\nNuevo nombre (dejar vacío para mantener " + paciente.getNombre() + ") -> ");
            String nombre = scanner.nextLine().trim();
            if (!nombre.isEmpty()) {
                paciente.setNombre(nombre);
            }

            System.out.print("Nuevo apellido (dejar vacío para mantener " + paciente.getApellido() + ") -> ");
            String apellido = scanner.nextLine().trim();
            if (!apellido.isEmpty()) {
                paciente.setApellido(apellido);
            }

            System.out.print("Nuevo DNI (dejar vacío para mantener " + paciente.getDni() + ") -> ");
            String dni = scanner.nextLine().trim();
            if (!dni.isEmpty()) {
                paciente.setDni(dni);
            }

            System.out.print("Nueva fecha de nacimiento (YYYY-MM-DD) (dejar vacío para mantener "
                    + paciente.getFechaNacimiento() + ") -> ");
            String fechaNacimientoStr = scanner.nextLine().trim();
            if (!fechaNacimientoStr.isEmpty()) {
                paciente.setFechaNacimiento(LocalDate.parse(fechaNacimientoStr));
            }

            // Actualizar o crear historia clínica
            if (paciente.getHistoriaClinica() != null) {
                System.out.print("\n¿Desea actualizar la historia clínica existente? (s/n) -> ");
                if (scanner.nextLine().equalsIgnoreCase("s")) {
                    updateHistoriaClinica(paciente.getHistoriaClinica());
                }
            } else {
                System.out.print("\n¿Desea agregar una nueva historia clínica? (s/n) -> ");
                if (scanner.nextLine().equalsIgnoreCase("s")) {
                    paciente.setHistoriaClinica(createHistoriaClinica());
                }
            }

            pacienteService.update(paciente);
            System.out.println("\nPaciente actualizado exitosamente!");
        } catch (Exception e) {
            System.err.println("Error al actualizar paciente: " + e.getMessage());
        }
    }

    public void deletePaciente() {

        try {
            System.out.print("\nIngrese el ID del paciente a eliminar -> ");
            int id = Integer.parseInt(scanner.nextLine());
            pacienteService.delete(id);
            System.out.println("\nPaciente ID: " + id + " eliminado exitosamente!");
        } catch (Exception e) {
            System.err.println("Error al eliminar paciente: " + e.getMessage());
        }
    }

    public void readHistoriaClinica() {

        try {
            System.out.print(
                    "\n¿Desea (1) listar todas, (2) buscar por ID o (3) buscar por palabra clave? \nIngrese opcion -> ");
            int subopcion = Integer.parseInt(scanner.nextLine());

            List<HistoriaClinica> historias = List.of();

            switch (subopcion) {
                case 1 -> historias = (List<HistoriaClinica>) historiaClinicaService.selectAll(false);
                case 2 -> {
                    System.out.print("\nIngrese ID de historia a buscar -> ");
                    int id = Integer.parseInt(scanner.nextLine().trim());
                    HistoriaClinica historiaClinica = historiaClinicaService.selectById(id);
                    historias = (historiaClinica != null) ? List.of(historiaClinica) : List.of();
                }
                case 3 -> {
                    System.out.print("\nIngrese un filtro a buscar -> ");
                    String filtro = scanner.nextLine().trim();
                    historias = (List<HistoriaClinica>) historiaClinicaService.searchByFilter(filtro);
                }
                default -> System.out.println("Opcion invalida.");
            }

            displayHistorias(historias);

        } catch (Exception e) {
            System.err.println("Error al listar historia clínica: " + e.getMessage());
        }
    }

    public HistoriaClinica createHistoriaClinica() {

        try {
            System.out.println("\nIngrese los datos de la historia clínica:");

            String numeroHistoria = "";
            GrupoSanguineo grupoSanguineo = null;

            do {
                System.out.print("Número de Historia -> ");
                numeroHistoria = scanner.nextLine().trim();
                System.out.print("Grupo Sanguíneo (A, B, AB, O) -> ");
                String grupoInput = scanner.nextLine().trim();
                System.out.print("Factor RH (+/-) -> ");
                String factorRhInput = scanner.nextLine().trim();
                String grupoSanguineoStr = grupoInput.toUpperCase() + "_"
                        + (factorRhInput.equals("+") ? "PLUS" : "MINUS");
                grupoSanguineo = GrupoSanguineo.valueOf(grupoSanguineoStr);
            } while (numeroHistoria.isEmpty() || grupoSanguineo == null);

            System.out.print("Antecedentes -> ");
            String antecedentes = scanner.nextLine().trim();
            System.out.print("Medicación Actual -> ");
            String medicacionActual = scanner.nextLine().trim();
            System.out.print("Observaciones -> ");
            String observaciones = scanner.nextLine().trim();

            HistoriaClinica historiaClinica = new HistoriaClinica(
                    0, numeroHistoria, grupoSanguineo, antecedentes,
                    medicacionActual, observaciones);
            historiaClinicaService.insert(historiaClinica);
            System.out.println("\nHistoria Clínica creada con ID: " + historiaClinica.getId());
            return historiaClinica;
        } catch (Exception e) {
            System.err.println("Error al crear historia clínica: " + e.getMessage());
        }
        return null;
    }

    public void updateHistoriaClinica() {
        try {
            System.out.print("\nIngrese el ID de la historia clínica a actualizar -> ");
            int id = Integer.parseInt(scanner.nextLine());
            HistoriaClinica historiaClinica = historiaClinicaService.selectById(id);

            if (historiaClinica == null) {
                System.out.println("Historia Clínica no encontrada.");
                return;
            }

            updateHistoriaClinica(historiaClinica);
            historiaClinicaService.update(historiaClinica);
            System.out.println("\nHistoria Clínica actualizada exitosamente!");
        } catch (Exception e) {
            System.err.println("Error al actualizar historia clínica: " + e.getMessage());
        }
    }

    private void updateHistoriaClinica(HistoriaClinica historiaClinica) {

        System.out.println("\nHistoria Clínica actual: " + historiaClinica.getNumeroHistoria());
        System.out.print(
                "Nuevo número de historia (dejar vacío para mantener " + historiaClinica.getNumeroHistoria() + ") -> ");
        String numeroHistoria = scanner.nextLine().trim();
        if (!numeroHistoria.isEmpty()) {
            historiaClinica.setNumeroHistoria(numeroHistoria);
        }

        System.out.print("Nuevo grupo sanguíneo (A, B, AB, O) (dejar vacío para mantener "
                + historiaClinica.getGrupoSanguineo().toString() + ") -> ");
        String grupoInput = scanner.nextLine().trim();
        if (!grupoInput.isEmpty()) {
            System.out.print("Nuevo factor RH (+/-) -> ");
            String factorRhInput = scanner.nextLine().trim();
            String grupoSanguineoStr = grupoInput.toUpperCase() + "_"
                    + (factorRhInput.equals("+") ? "PLUS" : "MINUS");
            historiaClinica.setGrupoSanguineo(GrupoSanguineo.valueOf(grupoSanguineoStr));
        }

        System.out
                .print("Nuevos antecedentes (dejar vacío para mantener " + historiaClinica.getAntecedentes() + ") -> ");
        String antecedentes = scanner.nextLine().trim();
        if (!antecedentes.isEmpty()) {
            historiaClinica.setAntecedentes(antecedentes);

        }

        System.out.print("Nueva medicación actual (dejar vacío para mantener " + historiaClinica.getMedicacionActual()
                + ") -> ");
        String medicacionActual = scanner.nextLine().trim();
        if (!medicacionActual.isEmpty()) {
            historiaClinica.setMedicacionActual(medicacionActual);
        }

        System.out.print(
                "Nuevas observaciones (dejar vacío para mantener " + historiaClinica.getObservaciones() + ") -> ");
        String observaciones = scanner.nextLine().trim();
        if (!observaciones.isEmpty()) {
            historiaClinica.setObservaciones(observaciones);
        }
    }

    public void deleteHistoriaClinica() {
        try {
            System.out.print("\nIngrese el ID de la historia clínica a eliminar -> ");
            int id = Integer.parseInt(scanner.nextLine());
            historiaClinicaService.delete(id);
            System.out.println("\nHistoria Clínica ID: " + id + " eliminada exitosamente!");
        } catch (Exception e) {
            System.err.println("Error al eliminar historia clínica: " + e.getMessage());
        }
    }

    public void updateHistoriaClinicaDePaciente() {
        try {
            System.out.print("\nIngrese el ID del paciente cuya historia clínica desea gestionar -> ");
            int idPaciente = Integer.parseInt(scanner.nextLine());
            Paciente paciente = pacienteService.selectById(idPaciente);

            if (paciente == null) {
                System.out.println("\nPaciente no encontrado.");
                return;
            }

            if (paciente.getHistoriaClinica() != null) {
                System.out.println("\nEl paciente ya tiene una historia. Actualizando historia clínica de: "
                        + paciente.getApellido() + ", " + paciente.getNombre());

                updateHistoriaClinica(paciente.getHistoriaClinica());
                historiaClinicaService.update(paciente.getHistoriaClinica());

                System.out.println("\n¡Historia Clínica del paciente actualizada exitosamente!");
                return;
            }

            System.out.println("\nEl paciente" + paciente.getApellido() + ", "
                    + paciente.getNombre() + "no tiene una historia clínica asociada:\n");
            System.out.println("¿Que desea hacer?");
            System.out.println("1. Crear y asignar una nueva historia clínica");
            System.out.println("2. Asignar una historia clínica existente");
            System.out.println("0. Volver al menú");
            System.out.print("\nIngrese una opcion -> ");

            int subopcion = Integer.parseInt(scanner.nextLine());
            HistoriaClinica hcParaAsignar = null;

            switch (subopcion) {
                case 1 -> {
                    hcParaAsignar = createHistoriaClinica();
                    if (hcParaAsignar == null) {
                        System.out.println("Creación de historia clínica cancelada o fallida.");
                        return;
                    }
                    System.out.println("\nAsignando nueva historia clínica al paciente...");
                }

                case 2 -> {
                    System.out.print("\nIngrese el ID de la historia clínica existente a asignar -> ");
                    int idHistoria = Integer.parseInt(scanner.nextLine());
                    hcParaAsignar = historiaClinicaService.selectById(idHistoria);

                    if (hcParaAsignar == null) {
                        System.out.println("\nHistoria Clínica con ID " + idHistoria + " no encontrada.");
                        return;
                    }

                    System.out
                            .println("\nAsignando historia (" + hcParaAsignar.getNumeroHistoria() + ") al paciente...");
                }

                case 0 -> {
                    // Opción 0: Volver
                    System.out.println("Volviendo al menú...");
                    return; // Sale del método
                }

                default -> {
                    System.out.println("Opción no válida.");
                    return; // Sale del método
                }
            }

            paciente.setHistoriaClinica(hcParaAsignar);
            pacienteService.update(paciente);

            System.out.println(
                    "\n¡Historia Clínica asignada al paciente "
                            + paciente.getApellido() + ", " + paciente.getNombre()
                            + " exitosamente!");

        } catch (NumberFormatException e) {
            System.err.println("Entrada invalida. Por favor, ingrese un numero.");
        } catch (Exception e) {
            System.err.println("Error al asignar historia clínica al paciente: " + e.getMessage());
        }
    }

    public void deleteHistoriaClinicaDePaciente() {
        try {
            System.out.print("\nIngrese el ID del paciente cuya historia clínica desea eliminar -> ");
            int idPaciente = Integer.parseInt(scanner.nextLine());
            Paciente paciente = pacienteService.selectById(idPaciente);

            if (paciente == null) {
                System.out.println("Paciente no encontrado.");
                return;
            }

            if (paciente.getHistoriaClinica() == null) {
                System.out.println("El paciente no tiene una historia clínica asociada.");
                return;
            }

            System.out.println("Eliminando historia clínica del paciente: " + paciente.getApellido() + ", "
                    + paciente.getNombre());
            historiaClinicaService.delete(paciente.getHistoriaClinica().getId());
            paciente.setHistoriaClinica(null);
            pacienteService.update(paciente);
            System.out.println("Historia Clínica desasociada y eliminada exitosamente!");
        } catch (Exception e) {
            System.err.println("Error al eliminar historia clínica del paciente: " + e.getMessage());
        }
    }

    public void recover() {

        try {
            System.out.println("\n========= SUBMENU DE RECUPERACION =========");
            System.out.println("1. Recuperar Paciente por ID");
            System.out.println("2. Recuperar Historia Clínica por ID");
            System.out.println("3. Listar Pacientes Eliminados");
            System.out.println("4. Listar Historias Clínicas Eliminadas");
            System.out.println("0. Volver al menú principal");
            System.out.print("\nIngrese una opcion -> ");

            int subopcion = Integer.parseInt(scanner.nextLine());

            switch (subopcion) {
                case 1 -> {
                    System.out.print("\nIngrese el ID del paciente a recuperar -> ");
                    int id = Integer.parseInt(scanner.nextLine());
                    pacienteService.recover(id);
                    System.out.println("\nPaciente ID: " + id + " recuperado exitosamente!");
                }
                case 2 -> {
                    System.out.print("\nIngrese el ID de la historia clínica a recuperar -> ");
                    int id = Integer.parseInt(scanner.nextLine());
                    historiaClinicaService.recover(id);
                    System.out.println("\nHistoria Clínica ID: " + id + " recuperada exitosamente!");
                }
                case 3 -> {
                    List<Paciente> pacientesEliminados = pacienteService.selectAll(true);
                    System.out.println("\nListando pacientes eliminados...");
                    displayPacientes(pacientesEliminados);
                }
                case 4 -> {
                    System.out.println("\nListando historias clínicas eliminadas...");
                    List<HistoriaClinica> historiasEliminadas = historiaClinicaService.selectAll(true);
                    displayHistorias(historiasEliminadas);
                }
                case 0 -> System.out.println("Volviendo al menú principal...");
                default -> System.out.println("Opcion no valida.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Entrada invalida. Por favor, ingrese un numero.");
        } catch (Exception e) {
            System.err.println("Error en el submenú de recuperación: " + e.getMessage());
        }
    }

    private void displayPacientes(List<Paciente> pacientes) {

        if (pacientes.isEmpty()) {
            System.out.println("No se encontraron pacientes.");
            return;
        }

        for (Paciente p : pacientes) {

            LocalDate fechaNacimiento = p.getFechaNacimiento();
            int edad = 0;

            if (fechaNacimiento != null) {
                edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
            }

            System.out.printf(
                    "ID: %d | DNI: %s | Apellido y Nombre: %s, %s | Fecha de Nacimiento: %s | Edad: %d años\n",
                    p.getId(), p.getDni(), p.getApellido(), p.getNombre(), fechaNacimiento, edad);

            if (p.getHistoriaClinica() != null) {
                System.out.printf(
                        "\tID Historia Clínica: %d | N° Historia: %s%n",
                        p.getHistoriaClinica().getId(),
                        p.getHistoriaClinica().getNumeroHistoria());
                System.out.println("\t\tGrupo Sanguineo: " + p.getHistoriaClinica().getGrupoSanguineo());
                System.out.println("\t\tAntecedentes: " + p.getHistoriaClinica().getAntecedentes());
                System.out.println("\t\tMedicación Actual: " + p.getHistoriaClinica().getMedicacionActual());
                System.out.println("\t\tObservaciones: " + p.getHistoriaClinica().getObservaciones());

            } else {
                System.out.println("\tSin historia clínica\t");
            }
            System.out.println();
        }
    }

    private void displayHistorias(List<HistoriaClinica> historias) {

        if (historias.isEmpty()) {
            System.out.println("No se encontraron historias clínicas.");
            return;
        }

        for (HistoriaClinica historiaClinica : historias) {
            System.out.printf(
                    "ID: %d - N° Historia: %s%n",
                    historiaClinica.getId(),
                    historiaClinica.getNumeroHistoria());
            System.out.println("\tGrupo Sanguineo: " + historiaClinica.getGrupoSanguineo());
            System.out.println("\tAntecedentes: " + historiaClinica.getAntecedentes());
            System.out.println("\tMedicación Actual: " + historiaClinica.getMedicacionActual());
            System.out.println("\tObservaciones: " + historiaClinica.getObservaciones());
            System.out.println();
        }
    }

}
