package main;

import java.util.Scanner;

import javax.swing.SwingUtilities;

import config.DatabaseConnection;
import views.AppMenu;
import views.gui.MainGUI;

public class Main {

    public static void main(String[] args) {
        DatabaseConnection.setDebug(false); // modo producción

        // --- (Arreglo 2: Caracteres sin tildes) ---
        System.out.println("====================================");
        System.out.println("Sistema de Gestion de Pacientes");
        System.out.println("====================================");
        System.out.println("Seleccione el modo de ejecucion:");
        System.out.println("1. Consola");
        System.out.println("2. Interfaz Grafica (JOptionPane)\n");
        System.out.print("Opcion -> ");

        String opcion;

        // Usamos try-with-resources para que el Scanner
        // solo viva lo necesario para leer la opción.
        try (Scanner scanner = new Scanner(System.in)) {
            opcion = scanner.nextLine();
        } // El scanner se cierra aquí automáticamente.

        if ("2".equals(opcion)) {
            System.out.println("Iniciando interfaz grafica...");
            // Se lanza la GUI. El hilo 'main' ahora está libre
            // para terminar, lo que completará la tarea :run de Gradle.
            SwingUtilities.invokeLater(() -> MainGUI.main(null));
        } else {
            System.out.println("Iniciando modo consola...\n");
            // Se lanza la app de consola, que tiene su propio
            // bucle y su propio scanner.
            AppMenu app = new AppMenu();
            app.run();
        }

        // No hay 'scanner.close()' aquí, el hilo 'main' termina.
    }
}