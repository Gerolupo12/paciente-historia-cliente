package views;

import java.util.Scanner;

import views.historias.HistoriaMenu;
import views.pacientes.PacienteMenu;

/**
 * Controlador Principal de la Interfaz de Usuario (UI).
 * <p>
 * Esta clase actúa como un <b>"Router"</b> o "Controlador de Frontal" (Front
 * Controller) para la capa de vistas.
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li>Recibir la opción numérica seleccionada por el usuario desde
 * {@link AppMenu}.</li>
 * <li><b>Delegar</b> la solicitud al <b>Sub-Controlador</b> (o "Sub-Menú")
 * apropiado (ej: {@link PacienteMenu}, {@link HistoriaMenu}).</li>
 * <li>Manejar la opción de "Salir" (0), devolviendo <code>false</code> para
 * terminar el bucle principal de {@link AppMenu}.</li>
 * <li>Orquestar menús que requieren múltiples controladores (como el "Submenú
 * de recuperación").</li>
 * </ul>
 *
 * <h3>Arquitectura:</h3>
 * <p>
 * Esta clase <b>no</b> interactúa directamente con los Servicios ni con el
 * Scanner. Solo coordina a otros controladores de la capa de vistas.
 * </p>
 * 
 * <pre>
 * AppMenu (Bucle)
 * ↓
 * MenuHandler (Router)
 * ├─> PacienteMenu (Controlador de Pacientes)
 * └─> HistoriaMenu (Controlador de Historias)
 * </pre>
 *
 * @author alpha team
 * @see AppMenu
 * @see PacienteMenu
 * @see HistoriaMenu
 */
public class MenuHandler {

    // --- Sub-Controladores ---
    private final PacienteMenu pacienteMenu;
    private final HistoriaMenu historiaMenu;

    /**
     * Constructor que inyecta los sub-controladores (Sub-Menús).
     * <p>
     * Implementa la <b>Inyección de Dependencias</b> (DI) para que este controlador
     * principal pueda delegar las tareas.
     * </p>
     *
     * @param pacienteMenu El controlador para todas las
     *                     opciones de Pacientes (1-4).
     * @param historiaMenu El controlador para todas las
     *                     opciones de Historias Clínicas (5-10).
     */
    public MenuHandler(PacienteMenu pacienteMenu, HistoriaMenu historiaMenu) {

        if (pacienteMenu == null) {
            throw new IllegalArgumentException("PacienteMenu no puede ser nulo.");
        }

        if (historiaMenu == null) {
            throw new IllegalArgumentException("HistoriaMenu no puede ser nulo.");
        }

        this.pacienteMenu = pacienteMenu;
        this.historiaMenu = historiaMenu;
    }

    /**
     * Procesa la opción del menú principal seleccionada por el usuario.
     * <p>
     * Este método es el corazón del "Router". Utiliza un <code>switch</code>
     * para delegar la acción al sub-controlador correspondiente.
     * </p>
     *
     * @param opcion El número (<code>int</code>) de la opción seleccionada.
     * @return <code>true</code> si la aplicación debe continuar ejecutándose,
     *         <code>false</code> si el usuario seleccionó "Salir" (Opción 0).
     */
    public boolean processOption(int opcion) {
        switch (opcion) {
            case 1 -> pacienteMenu.handleListarPacientes();
            case 2 -> pacienteMenu.handleCrearPaciente();
            case 3 -> pacienteMenu.handleActualizarPaciente();
            case 4 -> pacienteMenu.handleEliminarPaciente();
            case 5 -> historiaMenu.handleListarHistorias();
            case 6 -> historiaMenu.handleCrearHistoriaIndependiente();
            case 7 -> historiaMenu.handleActualizarHistoriaPorId();
            case 8 -> historiaMenu.handleEliminarHistoriaPorId();
            case 9 -> historiaMenu.handleGestionarHistoriaPorPaciente();
            case 10 -> historiaMenu.handleEliminarHistoriaPorPaciente();
            // Este submenú se maneja aquí porque requiere ambos sub-controladores (Paciente
            // y Historia)
            case 11 -> this.handleSubmenuRecuperacion();
            // El usuario eligió "Salir". Devolvemos 'false' para indicarle a AppMenu que
            // detenga el bucle 'while(running)'.
            case 0 -> {
                return false;
            }
            default -> // Opción no válida
                System.err.println("Opción no válida. Por favor, intente de nuevo.");
        }
        return true;
    }

    /**
     * Maneja el Submenú de Recuperación de datos (Baja Lógica).
     * <p>
     * Esta lógica reside en el <code>MenuHandler</code> principal porque
     * necesita coordinar acciones de <b>ambos</b> sub-controladores
     * (<code>PacienteMenu</code> y <code>HistoriaMenu</code>).
     * </p>
     * <p>
     * Utiliza los métodos de los sub-controladores para realizar las acciones de
     * listar y recuperar.
     * </p>
     */
    private void handleSubmenuRecuperacion() {
        // Obtenemos una referencia al Scanner (que vive en las Vistas)
        // para este submenú específico.
        Scanner scanner = pacienteMenu.getViewScanner();

        boolean runningSubmenu = true;

        while (runningSubmenu) {
            try {
                System.out.println("\n========= SUBMENÚ DE RECUPERACIÓN =========");
                System.out.println("1. Listar Pacientes Eliminados");
                System.out.println("2. Recuperar Paciente por ID");
                System.out.println("3. Listar Historias Clínicas Eliminadas");
                System.out.println("4. Recuperar Historia Clínica por ID");
                System.out.println("0. Volver al menú principal");
                System.out.print("\nIngrese una opción -> ");

                int subopcion = Integer.parseInt(scanner.nextLine());

                switch (subopcion) {
                    case 1 -> pacienteMenu.handleListarPacientesEliminados();
                    case 2 -> pacienteMenu.handleRecuperarPaciente();
                    case 3 -> historiaMenu.handleListarHistoriasEliminadas();
                    case 4 -> historiaMenu.handleRecuperarHistoria();
                    case 0 -> runningSubmenu = false;
                    default -> System.err.println("Opción no válida. Intente de nuevo.");
                }

                // Pausa de UX dentro del submenú
                if (runningSubmenu) {
                    System.out.print("\nPresione Enter para volver al submenú de recuperación...");
                    scanner.nextLine();
                }

            } catch (NumberFormatException e) {
                System.err.println("Entrada inválida. Por favor, ingrese un número.");
                System.out.print("\nPresione Enter para volver al submenú...");

                scanner.nextLine();
            }
        }
    }
}