package views;

import dao.HistoriaClinicaDAO;
import dao.PacienteDAO;
import java.util.Scanner;
import service.HistoriaClinicaService;
import service.PacienteService;
import views.historias.HistoriaMenu;
import views.historias.HistoriaView;
import views.pacientes.PacienteMenu;
import views.pacientes.PacienteView;

/**
 * Orquestador principal de la aplicación y su ciclo de vida (el "Main Loop").
 * <p>
 * Esta clase representa el nivel más alto de la capa de Vistas (UI).
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li><b>Ensamblaje (Inyección de Dependencias):</b> Es responsable de <b>crear
 * e inyectar</b> todas las dependencias de la aplicación, siguiendo la
 * arquitectura de 4 capas (DAO → Service → View/Controller).</li>
 * <li><b>Crear el Scanner:</b> Gestiona la <b>única</b> instancia de
 * {@link Scanner} que se compartirá con todas las subclases de la vista.</li>
 * <li><b>Ciclo de Vida:</b> Ejecuta el bucle principal
 * (<code>while(running)</code>) que mantiene la aplicación viva.</li>
 * <li><b>Delegación:</b> Captura la opción del menú principal y la delega al
 * {@link MenuHandler} para su procesamiento.</li>
 * <li><b>Gestión de Recursos:</b> Asegura que el <code>Scanner</code> se cierre
 * correctamente al salir de la aplicación usando
 * <code>try-with-resources</code>.</li>
 * </ul>
 *
 * <h3>Flujo de Inyección (Bottom-Up):</h3>
 * <ol>
 * <li>Crea DAOs (Capa de Datos).</li>
 * <li>Crea Services (Capa de Negocio), inyectando los DAOs.</li>
 * <li>Crea Views (Capa de Vista - "tonta"), inyectando el Scanner.</li>
 * <li>Crea Menus/Sub-Controladores (Capa de Vista - "inteligente"), inyectando
 * los Services y las Views.</li>
 * <li>Crea el MenuHandler principal, inyectando los Sub-Controladores.</li>
 * </ol>
 *
 * @author alpha team
 * @see main.Main
 * @see views.MenuHandler
 * @see service.PacienteService
 * @see service.HistoriaClinicaService
 */
public class AppMenu {

    /**
     * Scanner único compartido por toda la capa de Vistas.
     * Se pasa a las clases View (ej: PacienteView) para capturar datos.
     */
    private final Scanner scanner;

    /**
     * El controlador principal que gestiona la navegación del menú.
     */
    private final MenuHandler menuHandler;

    /**
     * Flag que controla el bucle principal del menú.
     * Se vuelve <code>false</code> cuando el usuario elige la opción "Salir".
     */
    private boolean running;

    /**
     * Constructor de AppMenu.
     * <p>
     * Aquí se realiza la <b>Inyección de Dependencias (DI)</b> manual de toda la
     * aplicación. Se instancian todas las capas (DAO, Service, View, Controllers) y
     * se "cablean" entre sí.
     * </p>
     */
    public AppMenu() {

        // 1. Crear Recurso Compartido (Scanner)
        this.scanner = new Scanner(System.in);
        this.running = true;

        // 2. Crear Capa DAO (Acceso a Datos)
        HistoriaClinicaDAO historiaClinicaDAO = new HistoriaClinicaDAO();
        // PacienteDAO depende de HistoriaClinicaDAO
        PacienteDAO pacienteDAO = new PacienteDAO(historiaClinicaDAO);

        // 3. Crear Capa Service (Lógica de Negocio)
        // HistoriaClinicaService depende de HistoriaClinicaDAO
        HistoriaClinicaService historiaClinicaService = new HistoriaClinicaService(historiaClinicaDAO);
        // PacienteService depende de PacienteDAO y HistoriaClinicaService
        PacienteService pacienteService = new PacienteService(pacienteDAO, historiaClinicaService);

        // 4. Crear Capa de Vistas (Clases "Tontas" - I/O)
        // Las vistas solo saben cómo imprimir y escanear
        PacienteView pacienteView = new PacienteView(this.scanner);
        HistoriaView historiaView = new HistoriaView(this.scanner);

        // 5. Crear Sub-Controladores de Menú (Clases "Inteligentes" - Lógica UI)
        // HistoriaMenu necesita ambos servicios (para gestionar HC y asignarlas a
        // Pacientes) y su Vista
        HistoriaMenu historiaMenu = new HistoriaMenu(historiaView, historiaClinicaService, pacienteService);
        // PacienteMenu necesita su Servicio, su Vista y el sub-menú de Historias
        PacienteMenu pacienteMenu = new PacienteMenu(pacienteService, pacienteView, historiaMenu);

        // 6. Crear Controlador Principal
        // El MenuHandler solo necesita delegar a los sub-controladores
        this.menuHandler = new MenuHandler(pacienteMenu, historiaMenu);
    }

    /**
     * Inicia y ejecuta el ciclo de vida principal de la aplicación (Main Loop).
     * <p>
     * <b>Flujo:</b>
     * <ol>
     * <li>Usa <code>try-with-resources</code> para garantizar que el
     * <code>Scanner</code> se cierre al final.</li>
     * <li>Mientras <code>this.running</code> sea <code>true</code>:</li>
     * <li>a. Muestra el menú principal ({@link DisplayMenu}).</li>
     * <li>b. Captura la opción del usuario.</li>
     * <li>c. Llama a <code>menuHandler.processOption()</code> para delegar la
     * acción.</li>
     * <li>d. <code>processOption()</code> devuelve <code>false</code> si el usuario
     * eligió "Salir".</li>
     * <li>e. Pide "Presione Enter para continuar" (pausa de UI).</li>
     * <li>Maneja <code>NumberFormatException</code> si la entrada no es un
     * entero.</li>
     * </ol>
     * </p>
     */
    public void run() {

        // try-with-resources asegura que this.scanner se cierre al salir del
        // bloque, incluso si hay una excepción.
        try (Scanner scannerResource = this.scanner) {
            while (running) {
                try {

                    // 1. Mostrar el menú de opciones
                    DisplayMenu.showMainMenu();

                    // 2. Capturar la opción
                    int opcion = Integer.parseInt(scannerResource.nextLine());

                    // 3. Delegar la opción al controlador principal
                    // El controlador devuelve 'false' si la opción es 'Salir'
                    this.running = menuHandler.processOption(opcion);

                    // 4. Lógica de Pausa (UX)
                    if (this.running) {
                        System.out.print("\nPresione Enter para volver al menú...");
                        scannerResource.nextLine();
                    }
                } catch (NumberFormatException e) {
                    // Captura entradas inválidas (ej: "hola")
                    System.out.println("Entrada invalida. Por favor, ingrese un numero.");

                    System.out.print("\nPresione Enter para volver al menú...");
                    scannerResource.nextLine();
                }
            }
        }
        System.out.println("\nSaliendo del sistema... ¡Hasta pronto!");
    }

}
