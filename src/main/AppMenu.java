package main;

import dao.HistoriaClinicaDAO;
import dao.PacienteDAO;
import java.util.Scanner;
import service.HistoriaClinicaService;
import service.PacienteService;

public class AppMenu {

    private final Scanner scanner;
    private final MenuHandler menuHandler;
    private boolean running;

    public AppMenu() {

        this.scanner = new Scanner(System.in);

        // 1. Crear los DAOs
        HistoriaClinicaDAO historiaClinicaDAO = new HistoriaClinicaDAO();
        PacienteDAO pacienteDAO = new PacienteDAO(historiaClinicaDAO);

        // 2. Crear los Servicios
        HistoriaClinicaService historiaClinicaService = new HistoriaClinicaService(historiaClinicaDAO);
        PacienteService pacienteService = new PacienteService(pacienteDAO, historiaClinicaService);

        // 3. Inyectar AMBOS servicios en MenuHandler
        this.menuHandler = new MenuHandler(scanner, pacienteService, historiaClinicaService);

        this.running = true;
    }

    public void run() {

        try (Scanner scannerResource = this.scanner) {
            while (running) {
                try {
                    DisplayMenu.showMainMenu();
                    int opcion = Integer.parseInt(scannerResource.nextLine());
                    processOption(opcion);

                    if (opcion != 0) {
                        System.out.print("\nPresione Enter para volver al menú...");
                        scannerResource.nextLine();
                    }

                } catch (NumberFormatException e) {
                    System.out.println("Entrada invalida. Por favor, ingrese un numero.");

                    System.out.print("\nPresione Enter para volver al menú...");
                    scannerResource.nextLine();
                }
            }
        }
    }

    private void processOption(int opcion) {

        switch (opcion) {
            case 1 -> menuHandler.readPaciente();
            case 2 -> menuHandler.createPaciente();
            case 3 -> menuHandler.updatePaciente();
            case 4 -> menuHandler.deletePaciente();
            case 5 -> menuHandler.readHistoriaClinica();
            case 6 -> menuHandler.createHistoriaClinica();
            case 7 -> menuHandler.updateHistoriaClinica();
            case 8 -> menuHandler.deleteHistoriaClinica();
            case 9 -> menuHandler.updateHistoriaClinicaDePaciente();
            case 10 -> menuHandler.deleteHistoriaClinicaDePaciente();
            case 11 -> menuHandler.recover();
            case 0 -> {
                System.out.println("Saliendo...");
                running = false;
            }
            default -> System.out.println("Opcion no valida.");
        }
    }

}
