package views.gui;

import exceptions.DuplicateEntityException;
import exceptions.ServiceException;
import exceptions.ValidationException;
import models.HistoriaClinica;
import models.Paciente;
import service.PacienteService;
import views.gui.HistoriaGUI; // Importante: Dependencia del otro handler

import javax.swing.*;
import java.awt.Dimension; // Necesario para el JScrollPane
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.List;

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

    /**
     * Constructor que inyecta las dependencias necesarias.
     *
     * @param pacienteService El servicio de negocio para Pacientes.
     * @param historiaGUI     El controlador de GUI de Historias,
     * necesario para la lógica de
     * "agregar HC" al crear/actualizar un paciente.
     */
    public PacienteGUI(PacienteService pacienteService, HistoriaGUI historiaGUI) {
        if (pacienteService == null) {
            throw new IllegalArgumentException("PacienteService no puede ser nulo.");
        }
        if (historiaGUI == null) {
            throw new IllegalArgumentException("HistoriaGUI no puede ser nulo.");
        }
        this.pacienteService = pacienteService;
        this.historiaGUI = historiaGUI;
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
                     "HC-123",  // // solo 3 dígitos → DEBE fallar
                    models.GrupoSanguineo.O_MINUS,
                    null,
                    null,
                    null
            );

            // 2) Crear paciente válido
            Paciente p = new Paciente(
                    "PruebaRollback",
                    "Transaccion",
                    "49999118",
                    LocalDate.of(1990, 1, 1)
            );

            // Asociar HC inválida
            p.setHistoriaClinica(hcMala);

            // 3) Probar inserción (Debe fallar y hacer rollback)
            pacienteService.insert(p);

            mostrarError("❌ ERROR: ¡No debería haberse insertado! (rollback falló)");

        } catch (ValidationException | ServiceException e) {
            mostrarExito(
                    "Rollback ejecutado correctamente.\n" +
                    "La transacción falló y NO se insertó nada.\n\n" +
                    "Mensaje técnico:\n" + e.getMessage()
            );
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
     * ...
     *  
     */
    public void handleCrearPaciente() {
        try {
            Paciente nuevoPaciente = this.solicitarDatosPacienteGUI();
            if (nuevoPaciente == null) {
                mostrarError("Creación de paciente cancelada.");
                return;
            }

            if (this.solicitarConfirmacionGUI("¿Desea agregar una Historia Clínica ahora?")) {
                HistoriaClinica nuevaHc = historiaGUI.handleCrearHistoria();
                if (nuevaHc != null) {
                    nuevoPaciente.setHistoriaClinica(nuevaHc);
                    mostrarExito("Historia Clínica (ID: " + nuevaHc.getId() + ") creada y lista para asociar.");
                } else {
                    mostrarError("Creación de HC cancelada. Se creará el paciente sin HC.");
                }
            }

            pacienteService.insert(nuevoPaciente);

            mostrarExito("Paciente creado exitosamente con ID: " + nuevoPaciente.getId());

        } catch (ValidationException | DuplicateEntityException e) {
            mostrarError(e.getMessage());
        } catch (DateTimeParseException e) {
            mostrarError("Formato de fecha inválido. Use AAAA-MM-DD.");
        } catch (Exception e) {
            mostrarError("Error del sistema al crear el paciente: " + e.getMessage());
        }
    }

    public void handleListarPacientes() {
        try {
            Object[] options = {"Listar Todos (Activos)", "Buscar por DNI", "Buscar por Nombre/Apellido", "Cancelar"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Seleccione un método de listado:",
                    "Listar Pacientes",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]
            );

            List<Paciente> pacientes;

            switch (choice) {
                case 0:
                    pacientes = pacienteService.selectAll(false);
                    mostrarPacientesGUI(pacientes, "Listado de Pacientes Activos");
                    break;
                case 1:
                    String dni = this.solicitarDniGUI();
                    if (dni == null) return;
                    Paciente p = pacienteService.selectByDni(dni);
                    mostrarPacientesGUI(p != null ? List.of(p) : List.of(), "Resultado de Búsqueda por DNI");
                    break;
                case 2:
                    String filtro = this.solicitarFiltroBusquedaGUI();
                    if (filtro == null) return;
                    pacientes = pacienteService.searchByFilter(filtro);
                    mostrarPacientesGUI(pacientes, "Resultado de Búsqueda por Filtro");
                    break;
                case 3:
                default:
                    break;
            }
        } catch (Exception e) {
            mostrarError("Error al listar pacientes: " + e.getMessage());
        }
    }

    public void handleActualizarPaciente() {
        try {
            Integer id = this.solicitarIdPacienteGUI("actualizar");
            if (id == null) return;

            Paciente paciente = pacienteService.selectById(id, false);
            if (paciente == null) {
                mostrarError("No se encontró un paciente activo con ID: " + id);
                return;
            }

            Paciente pacienteActualizado = this.solicitarDatosActualizacionGUI(paciente);
            if (pacienteActualizado == null) {
                mostrarError("Actualización cancelada.");
                return;
            }

            if (pacienteActualizado.getHistoriaClinica() == null) {
                if (this.solicitarConfirmacionGUI("Este paciente no tiene HC. ¿Desea agregar una ahora?")) {
                    HistoriaClinica nuevaHc = historiaGUI.handleCrearHistoria();
                    if (nuevaHc != null) {
                        pacienteActualizado.setHistoriaClinica(nuevaHc);
                        mostrarExito("Nueva Historia Clínica (ID: " + nuevaHc.getId() + ") asignada.");
                    }
                }
            }

            pacienteService.update(pacienteActualizado);

            mostrarExito("Paciente actualizado exitosamente.");

        } catch (ValidationException | DuplicateEntityException e) {
            mostrarError(e.getMessage());
        } catch (Exception e) {
            mostrarError("Error del sistema al actualizar el paciente: " + e.getMessage());
        }
    }

    public void handleEliminarPaciente() {
        try {
            Integer id = this.solicitarIdPacienteGUI("eliminar (baja lógica)");
            if (id == null) return;

            String msg = "¿Está seguro que desea eliminar al paciente ID " + id +
                    "?\n(Esto también eliminará su Historia Clínica asociada - RN-013)";
            if (this.solicitarConfirmacionGUI(msg)) {
                pacienteService.delete(id);
                mostrarExito("Paciente ID: " + id + " y su HC asociada han sido eliminados (baja lógica).");
            } else {
                mostrarError("Eliminación cancelada.");
            }
        } catch (Exception e) {
            mostrarError("Error al eliminar el paciente: " + e.getMessage());
        }
    }

    public void handleListarPacientesEliminados() {
        try {
            List<Paciente> pacientes = pacienteService.selectAll(true);
            mostrarPacientesGUI(pacientes, "Listado de Pacientes Eliminados");
        } catch (Exception e) {
            mostrarError("Error al listar pacientes eliminados: " + e.getMessage());
        }
    }

    public void handleRecuperarPaciente() {
        try {
            Integer id = this.solicitarIdPacienteGUI("recuperar");
            if (id == null) return;

            pacienteService.recover(id);

            mostrarExito("Paciente ID: " + id + " y su HC asociada han sido recuperados.");
        } catch (Exception e) {
            mostrarError("Error al recuperar el paciente: " + e.getMessage());
        }
    }

    // ------------ MÉTODOS AUXILIARES ORIGINALES (sin cambios) ------------

    private void mostrarPacientesGUI(List<Paciente> pacientes, String titulo) {
        if (pacientes == null || pacientes.isEmpty()) {
            mostrarExito("No se encontraron pacientes.");
            return;
        }

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

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        JOptionPane.showMessageDialog(null, scrollPane, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    private Paciente solicitarDatosPacienteGUI() throws DateTimeParseException {
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
                null,
                message,
                "Crear Nuevo Paciente",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            String nombre = nombreField.getText().trim();
            String apellido = apellidoField.getText().trim();
            String dni = dniField.getText().trim();
            LocalDate fechaNac = LocalDate.parse(fechaNacField.getText().trim());

            return new Paciente(nombre, apellido, dni, fechaNac);
        }
        return null;
    }

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
                null,
                message,
                "Actualizar Paciente ID: " + paciente.getId(),
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            paciente.setNombre(nombreField.getText().trim());
            paciente.setApellido(apellidoField.getText().trim());
            paciente.setDni(dniField.getText().trim());
            paciente.setFechaNacimiento(LocalDate.parse(fechaNacField.getText().trim()));
            return paciente;
        }
        return null;
    }

    private Integer solicitarIdPacienteGUI(String accion) throws NumberFormatException {
        String idStr = JOptionPane.showInputDialog(
                null,
                "Ingrese el ID del Paciente que desea " + accion + ":",
                "Solicitar ID",
                JOptionPane.QUESTION_MESSAGE
        );

        if (idStr == null) {
            return null;
        }
        return Integer.parseInt(idStr.trim());
    }

    private String solicitarDniGUI() {
        String dni = JOptionPane.showInputDialog(
                null,
                "Ingrese el DNI a buscar:",
                "Buscar por DNI",
                JOptionPane.QUESTION_MESSAGE
        );
        return (dni != null) ? dni.trim() : null;
    }

    private String solicitarFiltroBusquedaGUI() {
        String filtro = JOptionPane.showInputDialog(
                null,
                "Ingrese el texto a buscar (por nombre o apellido):",
                "Buscar por Filtro",
                JOptionPane.QUESTION_MESSAGE
        );
        return (filtro != null) ? filtro.trim() : null;
    }

    private boolean solicitarConfirmacionGUI(String mensaje) {
        int result = JOptionPane.showConfirmDialog(
                null,
                mensaje,
                "Confirmación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                null,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void mostrarExito(String mensaje) {
        JOptionPane.showMessageDialog(
                null,
                mensaje,
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}

     