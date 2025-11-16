package views.gui;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import exceptions.DuplicateEntityException;
import exceptions.ServiceException;
import exceptions.ValidationException;
import models.GrupoSanguineo;
import models.HistoriaClinica;
import models.Paciente;
import service.HistoriaClinicaService;
import service.PacienteService;

/**
 * Sub-Controlador y Vista de GUI para todas las operaciones de HistoriaClinica.
 * <p>
 * Esta clase combina los roles de <code>HistoriaMenu</code> y
 * <code>HistoriaView</code> (de la versión de consola) en una sola
 * clase, ya que {@link JOptionPane} actúa como vista y controlador.
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li>Orquestar el flujo de la GUI para CRUD de Historias Clínicas.</li>
 * <li>Manejar las lógicas <b>complejas de la relación 1-a-1</b>
 * (Opciones 9 y 10).</li>
 * <li>Usar <code>JOptionPane</code> para mostrar y solicitar datos.</li>
 * <li>Llamar a {@link HistoriaClinicaService} y {@link PacienteService}
 * para la lógica de negocio.</li>
 * <li>Capturar excepciones ({@link ValidationException},
 * {@link ServiceException}, etc.) y mostrarlas al usuario.</li>
 * </ul>
 * 
 * @author alpha team
 * @see MainGUI
 * @see HistoriaClinicaService
 * @see PacienteService
 */
public class HistoriaGUI {

    private final HistoriaClinicaService historiaClinicaService;
    private final PacienteService pacienteService;
    private final JFrame parentFrame;

    /**
     * Constructor que inyecta las dependencias de servicio necesarias.
     * <p>
     * <b>Nota:</b> Este controlador necesita <b>ambos</b> servicios.
     * <code>HistoriaClinicaService</code> para crear/actualizar HCs.
     * <code>PacienteService</code> para buscar pacientes y
     * actualizar su FK <code>historia_clinica_id</code> (Opciones 9 y 10).
     * </p>
     *
     * @param historiaClinicaService El servicio de negocio para HCs.
     * @param pacienteService        El servicio de negocio para Pacientes.
     * @param parentFrame            El JFrame invisible que será el "dueño"
     *                               de todos los diálogos.
     */
    public HistoriaGUI(HistoriaClinicaService historiaClinicaService, PacienteService pacienteService,
            JFrame parentFrame) {
        if (historiaClinicaService == null) {
            throw new IllegalArgumentException("HistoriaClinicaService no puede ser nulo.");
        }
        if (pacienteService == null) {
            throw new IllegalArgumentException("PacienteService no puede ser nulo.");
        }
        if (parentFrame == null) {
            throw new IllegalArgumentException("ParentFrame no puede ser nulo.");
        }
        this.historiaClinicaService = historiaClinicaService;
        this.pacienteService = pacienteService;
        this.parentFrame = parentFrame;
    }

    // ============ MÉTODOS HANDLER (Llamados por MainGUI) ============

    /**
     * Orquesta el listado y búsqueda de HCs (Opción 5).
     * <p>
     * <b>Flujo (HU-006):</b> Muestra un submenú de opciones y
     * delega al servicio correspondiente.
     * </p>
     */
    public void handleListarHistorias() {
        try {
            Object[] options = { "Listar Todas (Activas)", "Buscar por ID", "Buscar por Filtro (texto)",
                    "Buscar por Nro. Historia (Exacto)", "Cancelar" };
            int choice = JOptionPane.showOptionDialog(
                    this.parentFrame, // JFrame padre
                    "Seleccione un método de listado:",
                    "Listar Historias Clínicas",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            List<HistoriaClinica> historias;

            switch (choice) {
                case 0: // Listar Todas
                    historias = historiaClinicaService.selectAll(false); // false = NO eliminadas
                    mostrarHistoriasGUI(historias, "Listado de Historias Clínicas Activas");
                    break;
                case 1: // Buscar por ID
                    Integer id = this.solicitarIdHistoriaGUI("buscar");
                    if (id == null)
                        return;
                    HistoriaClinica hc = historiaClinicaService.selectById(id, false);
                    mostrarHistoriasGUI(hc != null ? List.of(hc) : List.of(), "Resultado de Búsqueda por ID");
                    break;
                case 2: // Buscar por Filtro
                    String filtro = this.solicitarFiltroBusquedaGUI();
                    if (filtro == null)
                        return;
                    historias = historiaClinicaService.searchByFilter(filtro);
                    mostrarHistoriasGUI(historias, "Resultado de Búsqueda por Filtro");
                    break;
                case 3: // Buscar por Nro. Historia
                    String nro = JOptionPane.showInputDialog(
                            this.parentFrame, // JFrame padre
                            "Ingrese el Nro. de Historia exacto:",
                            "Buscar por Nro. Historia",
                            JOptionPane.QUESTION_MESSAGE);

                    if (nro == null || nro.trim().isEmpty())
                        return;
                    HistoriaClinica hcNro = historiaClinicaService.selectByNroHistoria(nro.trim());
                    mostrarHistoriasGUI(hcNro != null ? List.of(hcNro) : List.of(),
                            "Resultado de Búsqueda por Nro. Historia");
                    break;
                case 4: // Cancelar
                default:
                    break; // No hacer nada
            }
        } catch (Exception e) {
            mostrarError("Error al listar historias clínicas: " + e.getMessage());
        }
    }

    /**
     * Orquesta la creación de una HC independiente (Opción 6).
     * <p>
     * <b>Flujo (HU-005):</b> Llama al método reutilizable
     * {@link #handleCrearHistoria()} y muestra un mensaje de éxito.
     * </p>
     */
    public void handleCrearHistoriaIndependiente() {
        try {
            // Llama al handler reutilizable
            HistoriaClinica nuevaHc = this.handleCrearHistoria();

            if (nuevaHc != null) {
                mostrarExito("Historia Clínica independiente creada exitosamente con ID: " + nuevaHc.getId());
            } else {
                mostrarError("Creación de Historia Clínica cancelada.");
            }
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    /**
     * Orquesta la actualización de una HC por ID (Opción 7).
     * <p>
     * <b>Flujo:</b>
     * <ol>
     * <li>Pide el ID de la HC.</li>
     * <li>Obtiene la HC (<code>historiaClinicaService.selectById</code>).</li>
     * <li>Pide los nuevos datos (<code>solicitarDatosActualizacionGUI</code>).</li>
     * <li>Llama a <code>historiaClinicaService.update()</code>.</li>
     * </ol>
     * </p>
     */
    public void handleActualizarHistoriaPorId() {
        try {
            // 1. Vista: Pedir ID
            Integer id = this.solicitarIdHistoriaGUI("actualizar");
            if (id == null)
                return; // Cancelado

            // 2. Servicio: Obtener HC activa
            HistoriaClinica hc = historiaClinicaService.selectById(id, false);
            if (hc == null) {
                mostrarError("No se encontró una Historia Clínica activa con ID: " + id);
                return;
            }

            // 3. Vista: Mostrar datos actuales y pedir nuevos
            HistoriaClinica hcActualizada = this.solicitarDatosActualizacionGUI(hc);
            if (hcActualizada == null) {
                mostrarError("Actualización cancelada.");
                return;
            }

            // 4. Servicio: Validar (RN) y persistir
            historiaClinicaService.update(hcActualizada);

            // 5. Vista: Mostrar resultado
            mostrarExito("Historia Clínica actualizada exitosamente.");

        } catch (ValidationException | DuplicateEntityException e) {
            mostrarError(e.getMessage());
        } catch (Exception e) {
            mostrarError("Error del sistema al actualizar la HC: " + e.getMessage());
        }
    }

    /**
     * Orquesta la eliminación (lógica) de una HC por ID (Opción 8).
     * <p>
     * <b>Flujo (HU-007 - PELIGROSO):</b>
     * Muestra una advertencia y, si el usuario confirma, elimina la HC,
     * pudiendo dejar referencias huérfanas.
     * </p>
     */
    public void handleEliminarHistoriaPorId() {
        try {
            // 1. Vista: Pedir ID
            Integer id = this.solicitarIdHistoriaGUI("eliminar (baja lógica)");
            if (id == null)
                return; // Cancelado

            // 2. Vista: Advertir y pedir confirmación
            String msg = "<html><b>ADVERTENCIA: Esta opción es peligrosa.</b><br>" +
                    "Si esta HC está asignada a un Paciente, se creará una referencia huérfana.<br>" +
                    "Use la 'Opción 10' para una eliminación segura.<br><br>" +
                    "<b>¿Desea continuar con la eliminación de la HC ID: " + id + "?</b></html>";

            if (this.solicitarConfirmacionGUI(msg)) {
                // 3. Servicio: Ejecutar lógica de negocio
                historiaClinicaService.delete(id);
                // 4. Vista: Mostrar resultado
                mostrarExito("Historia Clínica ID: " + id + " ha sido eliminada (baja lógica).");
            } else {
                mostrarError("Eliminación cancelada.");
            }

        } catch (Exception e) {
            mostrarError("Error al eliminar la HC: " + e.getMessage());
        }
    }

    /**
     * Orquesta la gestión de la HC de un Paciente (Opción 9).
     * <p>
     * <b>Flujo (HU-009):</b>
     * <ol>
     * <li>Pide el ID del Paciente.</li>
     * <li>Busca al Paciente.</li>
     * <li><b>Caso 1 (Paciente tiene HC):</b> Llama a
     * <code>solicitarDatosActualizacionGUI</code> y
     * <code>historiaClinicaService.update()</code>.</li>
     * <li><b>Caso 2 (Paciente NO tiene HC):</b> Muestra submenú
     * ("Crear", "Asignar") y llama a <code>pacienteService.update()</code>
     * para guardar la nueva FK.</li>
     * </ol>
     * </p>
     */
    public void handleGestionarHistoriaPorPaciente() {
        try {
            // 1. Pedir ID Paciente
            Integer pacienteId = this.solicitarIdPacienteGUI("gestionar");
            if (pacienteId == null)
                return; // Cancelado

            // 2. Buscar Paciente
            Paciente paciente = pacienteService.selectById(pacienteId, false);
            if (paciente == null) {
                mostrarError("No se encontró un Paciente activo con ID: " + pacienteId);
                return;
            }

            // 3. Flujo Lógico (HU-009)
            if (paciente.getHistoriaClinica() != null) {
                // --- CASO 1: Paciente YA TIENE HC (Solo se puede actualizar) ---
                HistoriaClinica hcExistente = paciente.getHistoriaClinica();
                mostrarExito("El paciente ya tiene la HC ID: " + hcExistente.getId() + ". Puede actualizarla.");

                HistoriaClinica hcActualizada = this.solicitarDatosActualizacionGUI(hcExistente);
                if (hcActualizada == null) {
                    mostrarError("Actualización cancelada.");
                    return;
                }

                historiaClinicaService.update(hcActualizada);
                mostrarExito("Historia Clínica del paciente actualizada.");

            } else {
                // --- CASO 2: Paciente NO TIENE HC (Crear o Asignar) ---
                Object[] options = { "Crear y Asignar Nueva HC", "Asignar HC Existente", "Cancelar" };
                int choice = JOptionPane.showOptionDialog(
                        this.parentFrame, // JFrame padre
                        "El paciente no tiene una Historia Clínica asociada. ¿Qué desea hacer?",
                        "Gestionar HC de Paciente",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);

                HistoriaClinica hcParaAsignar = null;

                switch (choice) {
                    case 0: // Crear Nueva
                        hcParaAsignar = this.handleCrearHistoria();
                        break;
                    case 1: // Asignar Existente
                        Integer hcId = this.solicitarIdHistoriaGUI("asignar");
                        if (hcId == null)
                            break;
                        hcParaAsignar = historiaClinicaService.selectById(hcId, false);
                        if (hcParaAsignar == null) {
                            mostrarError("No se encontró una HC activa con ID: " + hcId);
                        }
                        break;
                    default: // Cancelar
                        return;
                }

                // 4. Asignar y Guardar
                if (hcParaAsignar != null) {
                    paciente.setHistoriaClinica(hcParaAsignar);
                    pacienteService.update(paciente); // Guarda la FK en Paciente
                    mostrarExito("Historia Clínica (ID: " + hcParaAsignar.getId() + ") asignada al Paciente (ID: "
                            + paciente.getId() + ").");
                }
            }
        } catch (Exception e) {
            mostrarError("Error al gestionar la HC del paciente: " + e.getMessage());
        }
    }

    /**
     * Orquesta la eliminación (lógica) de una HC por Paciente (Opción 10).
     * <p>
     * <b>Flujo (HU-008 - SEGURO):</b>
     * Llama a <code>pacienteService.deleteHistoriaClinica()</code>
     * para desasociar la FK y luego eliminar la HC.
     * </p>
     */
    public void handleEliminarHistoriaPorPaciente() {
        try {
            // 1. Vista: Pedir ID Paciente
            Integer pacienteId = this.solicitarIdPacienteGUI("cuya HC desea eliminar (de forma segura)");
            if (pacienteId == null)
                return; // Cancelado

            // 2. Servicio: Obtener Paciente (para saber ID de HC)
            Paciente paciente = pacienteService.selectById(pacienteId, false);
            if (paciente == null || paciente.getHistoriaClinica() == null) {
                mostrarError("No se encontró un paciente con HC asociada para el ID: " + pacienteId);
                return;
            }

            int hcId = paciente.getHistoriaClinica().getId();

            // 3. Vista: Pedir confirmación
            String msg = "¿Está seguro que desea desasociar y eliminar la HC (ID: " + hcId + ") del Paciente (ID: "
                    + pacienteId + ")?";
            if (this.solicitarConfirmacionGUI(msg)) {
                // 4. Servicio: Ejecutar lógica de eliminación segura (HU-008)
                pacienteService.deleteHistoriaClinica(pacienteId, hcId);
                // 5. Vista: Mostrar resultado
                mostrarExito("Historia Clínica (ID: " + hcId + ") desasociada y eliminada exitosamente.");
            } else {
                mostrarError("Eliminación cancelada.");
            }
        } catch (Exception e) {
            mostrarError("Error al eliminar la HC del paciente: " + e.getMessage());
        }
    }

    /**
     * Orquesta el listado de HCs Eliminadas (Opción 11.3).
     */
    public void handleListarHistoriasEliminadas() {
        try {
            List<HistoriaClinica> historias = historiaClinicaService.selectAll(true); // true = SÍ eliminadas
            mostrarHistoriasGUI(historias, "Listado de Historias Clínicas Eliminadas");
        } catch (Exception e) {
            mostrarError("Error al listar HCs eliminadas: " + e.getMessage());
        }
    }

    /**
     * Orquesta la recuperación (lógica) de una HC (Opción 11.4).
     */
    public void handleRecuperarHistoria() {
        try {
            // 1. Vista: Pedir ID
            Integer id = this.solicitarIdHistoriaGUI("recuperar");
            if (id == null)
                return; // Cancelado
            // 2. Servicio: Ejecutar lógica de negocio
            historiaClinicaService.recover(id);
            // 3. Vista: Mostrar resultado
            mostrarExito("Historia Clínica ID: " + id + " ha sido recuperada.");
        } catch (Exception e) {
            mostrarError("Error al recuperar la HC: " + e.getMessage());
        }
    }

    // ============ MÉTODOS HELPER (Reutilizables) ============

    /**
     * Método helper reutilizable para crear una Historia Clínica.
     * <p>
     * Es llamado internamente (Opción 6, 9) y externamente por
     * {@link PacienteGUI} (Opción 2, 3).
     * </p>
     * <p>
     * <b>Flujo:</b>
     * <ol>
     * <li>Llama a <code>solicitarDatosHistoriaGUI()</code>.</li>
     * <li>Si el usuario <b>cancela</b>, devuelve <b><code>null</code></b>.</li>
     * <li>Llama a <code>historiaClinicaService.insert()</code>.</li>
     * <li><b>Devuelve</b> el objeto <code>HistoriaClinica</code>
     * (con su nuevo ID).</li>
     * </ol>
     * </p>
     *
     * @return La {@link HistoriaClinica} recién creada y persistida,
     *         o <code>null</code> si el usuario canceló la operación.
     * @throws ValidationException      Si la HC no cumple las RN.
     * @throws DuplicateEntityException Si el <code>nroHistoria</code> ya existe.
     * @throws ServiceException         Si falla la inserción en la BD.
     */
    public HistoriaClinica handleCrearHistoria()
            throws ValidationException, DuplicateEntityException, ServiceException {

        // 1. Vista: Obtener datos "crudos"
        HistoriaClinica nuevaHc = this.solicitarDatosHistoriaGUI();

        if (nuevaHc == null) {
            return null; // Usuario canceló
        }

        // 2. Servicio: Validar (RN) y persistir
        historiaClinicaService.insert(nuevaHc);

        // 3. Devolver la HC con su nuevo ID
        return nuevaHc;
    }

    // ============ MÉTODOS HELPER (Vistas de GUI) ============

    /**
     * Muestra una lista de HCs en un diálogo con scroll.
     *
     * @param historias La lista de HCs a mostrar.
     * @param titulo    El título de la ventana.
     */
    private void mostrarHistoriasGUI(List<HistoriaClinica> historias, String titulo) {
        if (historias == null || historias.isEmpty()) {
            mostrarExito("No se encontraron historias clínicas.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (HistoriaClinica hc : historias) {
            sb.append(String.format("ID: %d | Nro. Historia: %s\n", hc.getId(), hc.getNumeroHistoria()));
            sb.append(String.format("Grupo Sang.: %s\n", hc.getGrupoSanguineo()));
            sb.append(String.format("Antecedentes: %s\n", hc.getAntecedentes() != null ? hc.getAntecedentes() : "N/A"));
            sb.append(String.format("Medicación: %s\n",
                    hc.getMedicacionActual() != null ? hc.getMedicacionActual() : "N/A"));
            sb.append("--------------------------------------------------\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        JOptionPane.showMessageDialog(
                this.parentFrame, // JFrame padre
                scrollPane,
                titulo,
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra un diálogo para solicitar los datos de una nueva HC.
     *
     * @return Un objeto HC (con id=0) o <code>null</code> si el usuario cancela.
     * @throws IllegalArgumentException Si el grupo sanguíneo es inválido.
     */
    private HistoriaClinica solicitarDatosHistoriaGUI() throws IllegalArgumentException {
        JTextField nroHistoriaField = new JTextField("HC-");
        JTextField grupoField = new JTextField("A");
        JTextField factorField = new JTextField("+");
        JTextField antecedentesField = new JTextField();
        JTextField medicacionField = new JTextField();
        JTextField observacionesField = new JTextField();

        Object[] message = {
                "Número de Historia (ej: HC-123456):", nroHistoriaField,
                "Grupo Sanguíneo (A, B, AB, O):", grupoField,
                "Factor RH (+ o -):", factorField,
                "Antecedentes (opcional):", antecedentesField,
                "Medicación Actual (opcional):", medicacionField,
                "Observaciones (opcional):", observacionesField
        };

        int option = JOptionPane.showConfirmDialog(
                this.parentFrame, // JFrame padre
                message,
                "Crear Nueva Historia Clínica",
                JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String nroHistoria = nroHistoriaField.getText().trim();
            String grupoInput = grupoField.getText().trim().toUpperCase();
            String factorInput = factorField.getText().trim();

            // Convertir (ej: "A", "+") a "A_PLUS"
            String enumName = grupoInput + "_" + (factorInput.equals("+") ? "PLUS" : "MINUS");
            GrupoSanguineo grupoSanguineo = GrupoSanguineo.valueOf(enumName); // Puede lanzar IllegalArgumentException

            String antecedentes = antecedentesField.getText().trim();
            String medicacion = medicacionField.getText().trim();
            String observaciones = observacionesField.getText().trim();

            return new HistoriaClinica(nroHistoria, grupoSanguineo, antecedentes, medicacion, observaciones);
        }
        return null; // Usuario presionó Cancelar
    }

    /**
     * Muestra un diálogo para solicitar datos de actualización de una HC.
     *
     * @param hc La HC con los datos actuales a pre-rellenar.
     * @return La HC con los campos actualizados, o <code>null</code>
     *         si el usuario cancela.
     * @throws IllegalArgumentException Si el grupo sanguíneo es inválido.
     */
    private HistoriaClinica solicitarDatosActualizacionGUI(HistoriaClinica hc) throws IllegalArgumentException {
        JTextField nroHistoriaField = new JTextField(hc.getNumeroHistoria());
        // Lógica para pre-rellenar Grupo y Factor
        String[] grupoFactor = hc.getGrupoSanguineo().toString().split("(?=[-+])"); // Divide "A+" en "A" y "+"
        JTextField grupoField = new JTextField(grupoFactor[0]);
        JTextField factorField = new JTextField(grupoFactor[1]);

        JTextField antecedentesField = new JTextField(hc.getAntecedentes());
        JTextField medicacionField = new JTextField(hc.getMedicacionActual());
        JTextField observacionesField = new JTextField(hc.getObservaciones());

        Object[] message = {
                "Número de Historia (ej: HC-123456):", nroHistoriaField,
                "Grupo Sanguíneo (A, B, AB, O):", grupoField,
                "Factor RH (+ o -):", factorField,
                "Antecedentes (opcional):", antecedentesField,
                "Medicación Actual (opcional):", medicacionField,
                "Observaciones (opcional):", observacionesField
        };

        int option = JOptionPane.showConfirmDialog(
                this.parentFrame, // JFrame padre
                message,
                "Actualizar Historia Clínica ID: " + hc.getId(),
                JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            hc.setNumeroHistoria(nroHistoriaField.getText().trim());

            String grupoInput = grupoField.getText().trim().toUpperCase();
            String factorInput = factorField.getText().trim();
            String enumName = grupoInput + "_" + (factorInput.equals("+") ? "PLUS" : "MINUS");
            hc.setGrupoSanguineo(GrupoSanguineo.valueOf(enumName)); // Actualizar

            hc.setAntecedentes(antecedentesField.getText().trim());
            hc.setMedicacionActual(medicacionField.getText().trim());
            hc.setObservaciones(observacionesField.getText().trim());

            return hc;
        }
        return null; // Usuario presionó Cancelar
    }

    /**
     * Muestra un diálogo para solicitar un ID de Historia Clínica.
     *
     * @param accion El verbo (ej: "actualizar", "eliminar", "asignar").
     * @return El ID (<code>Integer</code>), o <code>null</code> si el
     *         usuario cancela.
     * @throws NumberFormatException Si la entrada no es un número.
     */
    private Integer solicitarIdHistoriaGUI(String accion) throws NumberFormatException {
        String idStr = JOptionPane.showInputDialog(
                this.parentFrame, // JFrame padre
                "Ingrese el ID de la Historia Clínica que desea " + accion + ":",
                "Solicitar ID de HC",
                JOptionPane.QUESTION_MESSAGE);

        if (idStr == null) {
            return null; // Usuario presionó Cancelar
        }
        return Integer.parseInt(idStr.trim());
    }

    /**
     * Muestra un diálogo para solicitar un ID de Paciente.
     *
     * @param accion El verbo (ej: "gestionar", "eliminar").
     * @return El ID (<code>Integer</code>), o <code>null</code> si el
     *         usuario cancela.
     * @throws NumberFormatException Si la entrada no es un número.
     */
    private Integer solicitarIdPacienteGUI(String accion) throws NumberFormatException {
        String idStr = JOptionPane.showInputDialog(
                this.parentFrame, // JFrame padre
                "Ingrese el ID del Paciente para " + accion + " su HC:",
                "Solicitar ID de Paciente",
                JOptionPane.QUESTION_MESSAGE);

        if (idStr == null) {
            return null; // Usuario presionó Cancelar
        }
        return Integer.parseInt(idStr.trim());
    }

    /**
     * Mestra un diálogo para solicitar un filtro de búsqueda (para HC).
     *
     * @return El filtro (<code>String</code>), o <code>null</code> si el
     *         usuario cancela.
     */
    private String solicitarFiltroBusquedaGUI() {
        String filtro = JOptionPane.showInputDialog(
                this.parentFrame, // JFrame padre
                "Ingrese el texto a buscar (por Nro. Historia, antecedentes, grupo, etc.):",
                "Buscar HC por Filtro",
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