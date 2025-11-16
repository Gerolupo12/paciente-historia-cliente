package views.gui;

import java.awt.Dimension;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane; // Importante: Dependencia del otro handler
import javax.swing.JScrollPane;
import javax.swing.JTextArea; // Necesario para el JScrollPane
import javax.swing.JTextField;

import exceptions.DuplicateEntityException;
import exceptions.ServiceException;
import exceptions.ValidationException;
import models.HistoriaClinica;
import models.Paciente;
import service.PacienteService;

/**
 * Sub-Controlador y Vista de GUI para todas las operaciones de Paciente.
 * <p>
 * Esta clase combina los roles de <code>PacienteMenu</code> y
 * <code>PacienteView</code> (de la versión de consola) en una sola
 * clase, ya que {@link JOptionPane} actúa como vista y controlador.
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li>Orquestar el flujo de la GUI para CRUD de Pacientes.</li>
 * <li>Usar <code>JOptionPane</code> para mostrar y solicitar datos.</li>
 * <li>Llamar a {@link PacienteService} para la lógica de negocio.</li>
 * <li>Capturar excepciones ({@link ValidationException},
 * {@link ServiceException}, etc.) y mostrarlas al usuario.</li>
 * <li>Coordinar con {@link HistoriaGUI} para reutilizar la lógica de
 * creación de HC (HU-001, HU-003).</li>
 * </ul>
 *
 * @author alpha team
 * @see MainGUI
 * @see PacienteService
 * @see HistoriaGUI
 */
public class PacienteGUI {

    private final PacienteService pacienteService;
    private final HistoriaGUI historiaGUI; // Dependencia para HU-001 y HU-003
    private final JFrame parentFrame;

    /**
     * Constructor que inyecta las dependencias necesarias.
     *
     * @param pacienteService El servicio de negocio para Pacientes.
     * @param historiaGUI     El controlador de GUI de Historias,
     *                        necesario para la lógica de
     *                        "agregar HC" al crear/actualizar un paciente.
     * @param parentFrame     El JFrame invisible que será el "dueño"
     *                        de todos los diálogos.
     */
    public PacienteGUI(PacienteService pacienteService, HistoriaGUI historiaGUI, JFrame parentFrame) {
        if (pacienteService == null) {
            throw new IllegalArgumentException("PacienteService no puede ser nulo.");
        }
        if (historiaGUI == null) {
            throw new IllegalArgumentException("HistoriaGUI no puede ser nulo.");
        }
        if (parentFrame == null) {
            throw new IllegalArgumentException("JFrame padre no puede ser nulo.");
        }
        this.pacienteService = pacienteService;
        this.historiaGUI = historiaGUI;
        this.parentFrame = parentFrame;
    }

    // ======================================================
    // ******* MÉTODO NUEVO PARA PROBAR ROLLBACK *******
    // ======================================================
    /**
     * Prueba GUI de operación transaccional con rollback forzado.
     * Inserta un paciente válido pero con una Historia Clínica inválida,
     * para que el servicio falle y ejecute rollback correctamente.
     */
    public void handleTestRollback() {

        try {
            // 1) Crear HC inválida (fuerza error → rollback)
            HistoriaClinica hcMala = new HistoriaClinica(
                    "HC-123", // solo 3 dígitos → DEBE fallar
                    models.GrupoSanguineo.O_MINUS,
                    null,
                    null,
                    null);

            // 2) Crear paciente válido
            Paciente p = new Paciente(
                    "PruebaRollback",
                    "Transaccion",
                    "49999118",
                    LocalDate.of(1990, 1, 1));

            // Asociar HC inválida
            p.setHistoriaClinica(hcMala);

            // 3) Probar inserción (Debe fallar y hacer rollback)
            pacienteService.insert(p);

            mostrarError("❌ ERROR: ¡No debería haberse insertado! (rollback falló)");

        } catch (ValidationException | ServiceException e) {
            mostrarExito(
                    "Rollback ejecutado correctamente.\n" +
                            "La transacción falló y NO se insertó nada.\n\n" +
                            "Mensaje técnico:\n" + e.getMessage());
        } catch (Exception e) {
            mostrarError("Error inesperado durante la prueba transaccional: " + e.getMessage());
        }
    }
    // ======================================================
    // *********** FIN DEL MÉTODO DE ROLLBACK ***************
    // ======================================================

    // ============ MÉTODOS HANDLER (Llamados por MainGUI) ============

    /**
     * Orquesta la creación de un nuevo Paciente (Opción 2).
     * <p>
     * <b>Flujo (HU-001):</b>
     * <ol>
     * <li>Llama a <code>solicitarDatosPacienteGUI()</code>.</li>
     * <li>Si el usuario cancela, la operación termina.</li>
     * <li>Llama a <code>solicitarConfirmacionGUI()</code> para "Agregar HC".</li>
     * <li>Si confirma, delega a <code>historiaGUI.handleCrearHistoria()</code>
     * para crear la HC y la asocia al paciente.</li>
     * <li>Llama a <code>pacienteService.insert()</code>.</li>
     * <li>Muestra éxito o error.</li>
     * </ol>
     * </p>
     */
    public void handleCrearPaciente() {
        try {
            // 1. Vista: Obtener datos "crudos" del Paciente
            Paciente nuevoPaciente = this.solicitarDatosPacienteGUI();
            if (nuevoPaciente == null) {
                mostrarError("Creación de paciente cancelada.");
                return;
            }

            // 2. Vista: Confirmar si se agrega HC
            if (this.solicitarConfirmacionGUI("¿Desea agregar una Historia Clínica ahora?")) {
                // 3. Delegar creación de HC al sub-controlador de Historias
                HistoriaClinica nuevaHc = historiaGUI.handleCrearHistoria();
                if (nuevaHc != null) {
                    nuevoPaciente.setHistoriaClinica(nuevaHc);
                    mostrarExito("Historia Clínica (ID: " + nuevaHc.getId() + ") creada y lista para asociar.");
                } else {
                    mostrarError("Creación de HC cancelada. Se creará el paciente sin HC.");
                }
            }

            // 4. Servicio: Validar y persistir el Paciente
            pacienteService.insert(nuevoPaciente);

            // 5. Vista: Mostrar resultado
            mostrarExito("Paciente creado exitosamente con ID: " + nuevoPaciente.getId());

        } catch (ValidationException | DuplicateEntityException e) {
            // Error de negocio (ej: DNI duplicado, campos vacíos)
            mostrarError(e.getMessage());
        } catch (DateTimeParseException e) {
            mostrarError("Formato de fecha inválido. Use AAAA-MM-DD.");
        } catch (Exception e) {
            // Error de sistema (ej: SQLException)
            mostrarError("Error del sistema al crear el paciente: " + e.getMessage());
        }
    }

    /**
     * Orquesta el listado y búsqueda de Pacientes (Opción 1).
     * <p>
     * <b>Flujo (HU-002):</b> Muestra un submenú de opciones y
     * delega al servicio correspondiente.
     * </p>
     */
    public void handleListarPacientes() {
        try {
            Object[] options = { "Listar Todos (Activos)", "Buscar por DNI", "Buscar por Nombre/Apellido", "Cancelar" };
            int choice = JOptionPane.showOptionDialog(
                    this.parentFrame, // JFrame padre
                    "Seleccione un método de listado:",
                    "Listar Pacientes",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            List<Paciente> pacientes;

            switch (choice) {
                case 0: // Listar Todos
                    pacientes = pacienteService.selectAll(false); // false = NO eliminados
                    mostrarPacientesGUI(pacientes, "Listado de Pacientes Activos");
                    break;
                case 1: // Buscar por DNI
                    String dni = this.solicitarDniGUI();
                    if (dni == null)
                        return; // Cancelado
                    Paciente p = pacienteService.selectByDni(dni);
                    mostrarPacientesGUI(p != null ? List.of(p) : List.of(), "Resultado de Búsqueda por DNI");
                    break;
                case 2: // Buscar por Filtro
                    String filtro = this.solicitarFiltroBusquedaGUI();
                    if (filtro == null)
                        return; // Cancelado
                    pacientes = pacienteService.searchByFilter(filtro);
                    mostrarPacientesGUI(pacientes, "Resultado de Búsqueda por Filtro");
                    break;
                case 3: // Cancelar
                default:
                    break; // No hacer nada
            }
        } catch (Exception e) {
            mostrarError("Error al listar pacientes: " + e.getMessage());
        }
    }

    /**
     * Orquesta la actualización de un Paciente (Opción 3).
     * <p>
     * <b>Flujo (HU-003):</b>
     * <ol>
     * <li>Pide el ID del paciente a actualizar.</li>
     * <li>Obtiene el paciente (<code>pacienteService.selectById</code>).</li>
     * <li>Muestra los datos actuales y pide los nuevos
     * (<code>solicitarDatosActualizacionGUI</code>).</li>
     * <li>Si el paciente no tiene HC, pregunta si se desea agregar una
     * (reutilizando <code>historiaGUI.handleCrearHistoria()</code>).</li>
     * <li>Llama a <code>pacienteService.update()</code>.</li>
     * </ol>
     * </p>
     */
    public void handleActualizarPaciente() {
        try {
            // 1. Vista: Pedir ID
            Integer id = this.solicitarIdPacienteGUI("actualizar");
            if (id == null)
                return; // Cancelado

            // 2. Servicio: Obtener paciente ACTIVO
            Paciente paciente = pacienteService.selectById(id, false);
            if (paciente == null) {
                mostrarError("No se encontró un paciente activo con ID: " + id);
                return;
            }

            // 3. Vista: Mostrar datos actuales y pedir nuevos
            Paciente pacienteActualizado = this.solicitarDatosActualizacionGUI(paciente);
            if (pacienteActualizado == null) {
                mostrarError("Actualización cancelada.");
                return;
            }

            // 4. Lógica de HU-003: Agregar HC si no tiene
            if (pacienteActualizado.getHistoriaClinica() == null) {
                if (this.solicitarConfirmacionGUI("Este paciente no tiene HC. ¿Desea agregar una ahora?")) {
                    HistoriaClinica nuevaHc = historiaGUI.handleCrearHistoria();
                    if (nuevaHc != null) {
                        pacienteActualizado.setHistoriaClinica(nuevaHc);
                        mostrarExito("Nueva Historia Clínica (ID: " + nuevaHc.getId() + ") asignada.");
                    }
                }
            }

            // 5. Servicio: Validar (RN) y persistir
            pacienteService.update(pacienteActualizado);

            // 6. Vista: Mostrar resultado
            mostrarExito("Paciente actualizado exitosamente.");

        } catch (ValidationException | DuplicateEntityException e) {
            mostrarError(e.getMessage());
        } catch (Exception e) {
            mostrarError("Error del sistema al actualizar el paciente: " + e.getMessage());
        }
    }

    /**
     * Orquesta la eliminación (lógica) de un Paciente (Opción 4).
     * <p>
     * <b>Flujo (HU-004 / RN-013):</b>
     * Llama a <code>pacienteService.delete(id)</code>, que se encarga
     * de la lógica de cascada (eliminar Paciente Y su HC asociada).
     * </p>
     */
    public void handleEliminarPaciente() {
        try {
            // 1. Vista: Pedir ID
            Integer id = this.solicitarIdPacienteGUI("eliminar (baja lógica)");
            if (id == null)
                return; // Cancelado

            // 2. Vista: Pedir confirmación
            String msg = "¿Está seguro que desea eliminar al paciente ID " + id
                    + "?\n(Esto también eliminará su Historia Clínica asociada - RN-013)";
            if (this.solicitarConfirmacionGUI(msg)) {
                // 3. Servicio: Ejecutar lógica de negocio
                pacienteService.delete(id);
                // 4. Vista: Mostrar resultado
                mostrarExito("Paciente ID: " + id + " y su HC asociada han sido eliminados (baja lógica).");
            } else {
                mostrarError("Eliminación cancelada.");
            }
        } catch (Exception e) {
            mostrarError("Error al eliminar el paciente: " + e.getMessage());
        }
    }

    /**
     * Orquesta el listado de Pacientes Eliminados (Opción 11.1).
     */
    public void handleListarPacientesEliminados() {
        try {
            List<Paciente> pacientes = pacienteService.selectAll(true); // true = SÍ eliminados
            mostrarPacientesGUI(pacientes, "Listado de Pacientes Eliminados");
        } catch (Exception e) {
            mostrarError("Error al listar pacientes eliminados: " + e.getMessage());
        }
    }

    /**
     * Orquesta la recuperación (lógica) de un Paciente (Opción 11.2).
     * <p>
     * <b>Flujo (HU-010 / RN-031):</b>
     * Llama a <code>pacienteService.recover(id)</code>, que se encarga
     * de la lógica de cascada (recuperar Paciente Y su HC asociada).
     * </p>
     */
    public void handleRecuperarPaciente() {
        try {
            // 1. Vista: Pedir ID
            Integer id = this.solicitarIdPacienteGUI("recuperar");
            if (id == null)
                return; // Cancelado

            // 2. Servicio: Ejecutar lógica de negocio
            pacienteService.recover(id);

            // 3. Vista: Mostrar resultado
            mostrarExito("Paciente ID: " + id + " y su HC asociada han sido recuperados.");
        } catch (Exception e) {
            mostrarError("Error al recuperar el paciente: " + e.getMessage());
        }
    }

    // ============ MÉTODOS HELPER (Vistas de GUI) ============

    /**
     * Muestra una lista de pacientes en un diálogo con scroll.
     *
     * @param pacientes La lista de pacientes a mostrar.
     * @param titulo    El título de la ventana.
     */
    private void mostrarPacientesGUI(List<Paciente> pacientes, String titulo) {
        if (pacientes == null || pacientes.isEmpty()) {
            mostrarExito("No se encontraron pacientes.");
            return;
        }

        // Construir un String largo con todos los datos
        StringBuilder sb = new StringBuilder();
        for (Paciente p : pacientes) {
            sb.append(String.format("ID: %d | DNI: %s\n", p.getId(), p.getDni()));
            sb.append(String.format("Nombre: %s, %s\n", p.getApellido(), p.getNombre()));

            if (p.getFechaNacimiento() != null) {
                int edad = Period.between(p.getFechaNacimiento(), LocalDate.now()).getYears();
                sb.append(String.format("Fecha Nac: %s (%d años)\n", p.getFechaNacimiento(), edad));
            }

            HistoriaClinica hc = p.getHistoriaClinica();
            if (hc != null) {
                sb.append(String.format("  HC Nro: %s (ID: %d)\n", hc.getNumeroHistoria(), hc.getId()));
                sb.append(String.format("  Grupo: %s\n", hc.getGrupoSanguineo()));
            } else {
                sb.append("  HC: Sin Historia Clínica\n");
            }
            sb.append("--------------------------------------------------\n");
        }

        // Crear un JTextArea dentro de un JScrollPane
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        // Mostrar el JScrollPane dentro de un JOptionPane
        JOptionPane.showMessageDialog(
                this.parentFrame, // JFrame padre
                scrollPane,
                titulo,
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra un diálogo para solicitar los datos de un nuevo Paciente.
     *
     * @return Un objeto Paciente (con id=0) o <code>null</code> si el
     *         usuario cancela.
     * @throws DateTimeParseException Si la fecha es inválida.
     */
    private Paciente solicitarDatosPacienteGUI() throws DateTimeParseException {
        // Usamos JPanels para pedir múltiples campos a la vez
        JTextField nombreField = new JTextField();
        JTextField apellidoField = new JTextField();
        JTextField dniField = new JTextField();
        JTextField fechaNacField = new JTextField("AAAA-MM-DD");

        Object[] message = {
                "Nombre:", nombreField,
                "Apellido:", apellidoField,
                "DNI (solo números):", dniField,
                "Fecha de Nacimiento (AAAA-MM-DD):", fechaNacField
        };

        int option = JOptionPane.showConfirmDialog(
                this.parentFrame, // JFrame padre
                message,
                "Crear Nuevo Paciente",
                JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String nombre = nombreField.getText().trim();
            String apellido = apellidoField.getText().trim();
            String dni = dniField.getText().trim();
            LocalDate fechaNac = LocalDate.parse(fechaNacField.getText().trim());

            return new Paciente(nombre, apellido, dni, fechaNac);
        }
        return null; // Usuario presionó Cancelar
    }

    /**
     * Muestra un diálogo para solicitar datos de actualización de un Paciente.
     *
     * @param paciente El Paciente con los datos actuales a pre-rellenar.
     * @return El Paciente con los campos actualizados, o <code>null</code>
     *         si el usuario cancela.
     * @throws DateTimeParseException Si la fecha es inválida.
     */
    private Paciente solicitarDatosActualizacionGUI(Paciente paciente) throws DateTimeParseException {
        JTextField nombreField = new JTextField(paciente.getNombre());
        JTextField apellidoField = new JTextField(paciente.getApellido());
        JTextField dniField = new JTextField(paciente.getDni());
        JTextField fechaNacField = new JTextField(paciente.getFechaNacimiento().toString());

        Object[] message = {
                "Nombre:", nombreField,
                "Apellido:", apellidoField,
                "DNI (solo números):", dniField,
                "Fecha de Nacimiento (AAAA-MM-DD):", fechaNacField
        };

        int option = JOptionPane.showConfirmDialog(
                this.parentFrame, // JFrame padre
                message,
                "Actualizar Paciente ID: " + paciente.getId(),
                JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            paciente.setNombre(nombreField.getText().trim());
            paciente.setApellido(apellidoField.getText().trim());
            paciente.setDni(dniField.getText().trim());
            paciente.setFechaNacimiento(LocalDate.parse(fechaNacField.getText().trim()));
            return paciente;
        }
        return null; // Usuario presionó Cancelar
    }

    /**
     * Muestra un diálogo para solicitar un ID de Paciente.
     *
     * @param accion El verbo (ej: "actualizar", "eliminar").
     * @return El ID (<code>Integer</code>), o <code>null</code> si el
     *         usuario cancela.
     * @throws NumberFormatException Si la entrada no es un número.
     */
    private Integer solicitarIdPacienteGUI(String accion) throws NumberFormatException {
        String idStr = JOptionPane.showInputDialog(
                this.parentFrame, // JFrame padre
                "Ingrese el ID del Paciente que desea " + accion + ":",
                "Solicitar ID",
                JOptionPane.QUESTION_MESSAGE);

        if (idStr == null) {
            return null; // Usuario presionó Cancelar
        }
        return Integer.parseInt(idStr.trim());
    }

    /**
     * Mestra un diálogo para solicitar un filtro de búsqueda (DNI).
     *
     * @return El DNI (<code>String</code>), o <code>null</code> si el
     *         usuario cancela.
     */
    private String solicitarDniGUI() {
        String dni = JOptionPane.showInputDialog(
                this.parentFrame, // JFrame padre
                "Ingrese el DNI a buscar:",
                "Buscar por DNI",
                JOptionPane.QUESTION_MESSAGE);
        return (dni != null) ? dni.trim() : null;
    }

    /**
     * Mestra un diálogo para solicitar un filtro de búsqueda (Nombre/Apellido).
     *
     * @return El filtro (<code>String</code>), o <code>null</code> si el
     *         usuario cancela.
     */
    private String solicitarFiltroBusquedaGUI() {
        String filtro = JOptionPane.showInputDialog(
                this.parentFrame, // JFrame padre
                "Ingrese el texto a buscar (por nombre o apellido):",
                "Buscar por Filtro",
                JOptionPane.QUESTION_MESSAGE);
        return (filtro != null) ? filtro.trim() : null;
    }

    /**
     * Muestra un diálogo de confirmación (Sí/No).
     *
     * @param mensaje La pregunta a confirmar.
     * @return <code>true</code> si se presionó "Sí", <code>false</code> en
     *         caso contrario.
     */
    private boolean solicitarConfirmacionGUI(String mensaje) {
        int result = JOptionPane.showConfirmDialog(
                this.parentFrame, // JFrame padre
                mensaje,
                "Confirmación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Muestra un diálogo de Error.
     *
     * @param mensaje El mensaje de error.
     */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                this.parentFrame, // JFrame padre
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Muestra un diálogo de Éxito/Información.
     *
     * @param mensaje El mensaje de éxito.
     */
    private void mostrarExito(String mensaje) {
        JOptionPane.showMessageDialog(
                this.parentFrame, // JFrame padre
                mensaje,
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
    }
}