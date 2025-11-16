package views.gui;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import dao.HistoriaClinicaDAO;
import dao.PacienteDAO;
import service.HistoriaClinicaService; // Necesario para el JOptionPane
import service.PacienteService; // Necesario para el JScrollPane

/**
 * Orquestador principal de la Interfaz Gráfica (GUI) y punto de entrada.
 * <p>
 * Esta clase reemplaza la lógica de {@link views.AppMenu} y
 * {@link views.MenuHandler} para un entorno gráfico basado en JOptionPane.
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li><b>Ensamblaje (DI):</b> Crea e inyecta todas las dependencias
 * (DAOs, Servicios, y los Handlers de GUI).</li>
 * <li><b>Ciclo de Vida (Main Loop):</b> Ejecuta el bucle
 * <code>while(running)</code>
 * que muestra el menú principal de la GUI.</li>
 * <li><b>Enrutamiento (Router):</b> Delega la acción seleccionada
 * al sub-controlador de GUI correspondiente ({@link PacienteGUI} o
 * {@link HistoriaGUI}).</li>
 * </ul>
 *
 * @author alpha team
 * @see main.Main
 */
public class MainGUI {

    // --- Sub-Controladores de GUI ---
    private final JFrame parentFrame;
    private final PacienteGUI pacienteGUI;
    private final HistoriaGUI historiaGUI;

    /**
     * Constructor de MainGUI.
     * <p>
     * Aquí se realiza la <b>Inyección de Dependencias (DI)</b> manual
     * de toda la aplicación para el modo GUI.
     * </p>
     * <p>
     * <b>Flujo de Inyección (Bottom-Up):</b>
     * <ol>
     * <li>Crea DAOs (Capa de Datos).</li>
     * <li>Crea Services (Capa de Negocio), inyectando los DAOs.</li>
     * <li>Crea los GUI Handlers (Capa de Vista/Controlador),
     * inyectando los Services.</li>
     * </ol>
     * </p>
     * 
     * @param parentFrame El JFrame invisible que será el "dueño"
     *                    de todos los diálogos.
     */
    public MainGUI(JFrame parentFrame) {
        this.parentFrame = parentFrame;

        // 1. Crear Capa DAO
        HistoriaClinicaDAO historiaClinicaDAO = new HistoriaClinicaDAO();
        PacienteDAO pacienteDAO = new PacienteDAO(historiaClinicaDAO);

        // 2. Crear Capa Service
        HistoriaClinicaService historiaClinicaService = new HistoriaClinicaService(historiaClinicaDAO);
        // PacienteService necesita ambos servicios para la coordinación
        PacienteService pacienteService = new PacienteService(pacienteDAO, historiaClinicaService);

        // 3. Crear Handlers de GUI
        // Inyectar servicios
        this.historiaGUI = new HistoriaGUI(historiaClinicaService, pacienteService, this.parentFrame);
        // Inyectar servicio Y el otro handler
        this.pacienteGUI = new PacienteGUI(pacienteService, this.historiaGUI, this.parentFrame);
    }

    /**
     * Inicia y ejecuta el ciclo de vida principal de la GUI (Main Loop).
     * <p>
     * Muestra el menú principal jerárquico (usando
     * <code>JOptionPane.showOptionDialog</code>) en un bucle
     * hasta que el usuario selecciona "Salir".
     * </p>
     */
    public void run() {
        boolean running = true;

        // Opciones del Menú Principal (Jerárquico)
        Object[] options = {
                "Gestión de Pacientes",
                "Gestión de Historias Clínicas",
                "Submenú de Recuperación",
                "Salir"
        };

        while (running) {
            int opcion = JOptionPane.showOptionDialog(
                    this.parentFrame, // JFrame padre
                    "Seleccione un módulo:", // Mensaje más claro
                    "🏥 Sistema de Gestión de Pacientes (GUI)",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            // Desactivar el "siempre al frente" después de que la primera ventana haya
            // robado el foco.
            this.parentFrame.setAlwaysOnTop(false);

            // 'opcion' es el índice del array 'options' (0-3)
            switch (opcion) {
                case 0: // Gestión de Pacientes
                    this.showPacienteMenu();
                    break;
                case 1: // Gestión de Historias Clínicas
                    this.showHistoriaMenu();
                    break;
                case 2: // Submenú de Recuperación
                    this.showRecuperacionMenu();
                    break;
                case 3: // Salir
                case -1: // Cerrar ventana (X)
                default:
                    running = false; // Termina el bucle
                    // System.exit(0);
                    break;
            }
        }
    }

    /**
     * Muestra el Submenú de Gestión de Pacientes (Opciones 1-4).
     * Este método es llamado por run() cuando el usuario selecciona
     * "Gestión de Pacientes".
     */
    private void showPacienteMenu() {
        Object[] options = {
                "1. Listar Pacientes",
                "2. Crear Paciente",
                "3. Actualizar Paciente",
                "4. Eliminar Paciente",
                "5. Probar Transacción (Rollback)",
                "Volver"
        };

        int opcion = JOptionPane.showOptionDialog(
                this.parentFrame, // JFrame padre
                "Seleccione una operación para Pacientes:",
                "Gestión de Pacientes",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        switch (opcion) {
            case 0: // 1. Listar Pacientes
                pacienteGUI.handleListarPacientes();
                break;
            case 1: // 2. Crear Paciente
                pacienteGUI.handleCrearPaciente();
                break;
            case 2: // 3. Actualizar Paciente
                pacienteGUI.handleActualizarPaciente();
                break;
            case 3: // 4. Eliminar Paciente
                pacienteGUI.handleEliminarPaciente();
                break;
            case 4: // 5. Probar Transacción (Rollback)
                pacienteGUI.handleTestRollback();
                break;
            case 5: // Volver
            default:
                break;
        }
    }

    /**
     * Muestra el Submenú de Gestión de Historias Clínicas (Opciones 5-10).
     * Este método es llamado por run() cuando el usuario selecciona
     * "Gestión de Historias Clínicas".
     */
    private void showHistoriaMenu() {
        Object[] options = {
                "5. Listar HC",
                "6. Crear HC",
                "7. Actualizar HC (por ID)",
                "8. Eliminar HC",
                "9. HC por Paciente",
                "10. Eliminar HC(Seguro)",
                "Volver"
        };

        int opcion = JOptionPane.showOptionDialog(
                this.parentFrame, // JFrame padre
                "Seleccione una operación para Historias Clínicas:",
                "Gestión de Historias Clínicas",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        switch (opcion) {
            case 0: // 5. Listar HCs
                historiaGUI.handleListarHistorias();
                break;
            case 1: // 6. Crear HC (Independiente)
                historiaGUI.handleCrearHistoriaIndependiente();
                break;
            case 2: // 7. Actualizar HC (por ID)
                historiaGUI.handleActualizarHistoriaPorId();
                break;
            case 3: // 8. Eliminar HC (Peligroso)
                historiaGUI.handleEliminarHistoriaPorId();
                break;
            case 4: // 9. Gestionar HC de Paciente
                historiaGUI.handleGestionarHistoriaPorPaciente();
                break;
            case 5: // 10. Eliminar HC de Paciente (Seguro)
                historiaGUI.handleEliminarHistoriaPorPaciente();
                break;
            case 6: // Volver
            default:
                break; // Vuelve al loop principal
        }
    }

    /**
     * Muestra el Submenú de Recuperación de datos (Opción 11).
     * (Este es tu método handleSubmenuRecuperacionGUI() renombrado).
     */
    private void showRecuperacionMenu() {
        Object[] options = {
                "Listar Pacientes Eliminados",
                "Recuperar Paciente por ID",
                "Listar HCs Eliminadas",
                "Recuperar HC por ID",
                "Volver al Menú Principal"
        };

        boolean runningSubmenu = true;

        while (runningSubmenu) {
            int opcion = JOptionPane.showOptionDialog(
                    this.parentFrame, // JFrame padre
                    "Seleccione una opción de recuperación:",
                    "Submenú de Recuperación",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            switch (opcion) {
                case 0: // Listar Pacientes Eliminados
                    pacienteGUI.handleListarPacientesEliminados();
                    break;
                case 1: // Recuperar Paciente
                    pacienteGUI.handleRecuperarPaciente();
                    break;
                case 2: // Listar HCs Eliminadas
                    historiaGUI.handleListarHistoriasEliminadas();
                    break;
                case 3: // Recuperar HC
                    historiaGUI.handleRecuperarHistoria();
                    break;
                case 4: // Volver
                case -1: // Cerrar ventana
                default:
                    runningSubmenu = false; // Sale del bucle del submenú
                    break;
            }
        }
    }

    /**
     * Punto de entrada estático llamado por {@link main.Main}.
     * <p>
     * Es responsable de crear la instancia de <code>MainGUI</code>
     * y ejecutar su ciclo de vida <code>run()</code>.
     * </p>
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {

        // --- INICIO DE LA MODIFICACIÓN ---
        // 1. Crear un frame padre invisible
        JFrame parentFrame = new JFrame();
        parentFrame.setUndecorated(true); // Sin bordes

        // Forzarlo a estar al frente para el primer diálogo
        parentFrame.setAlwaysOnTop(true);
        parentFrame.setLocationRelativeTo(null); // Centrar en la pantalla
        parentFrame.setVisible(true); // Hacerlo visible (aunque sea invisible)
        parentFrame.toFront(); // Traerlo al frente
        // --- FIN DE LA MODIFICACIÓN ---

        // 2. Crea la instancia que contiene toda la lógica de la GUI e inyectar el
        // frame padre
        MainGUI gui = new MainGUI(parentFrame);

        // 3. Ejecuta el bucle principal del menú
        gui.run();

        // 3. Cuando run() termina, cerrar el frame invisible
        // Esto también ayuda a que el hilo de Swing se cierre
        // correctamente y Gradle no se quede "colgado".
        parentFrame.dispose();
    }
}
