package views.pacientes;

import java.util.List;
import java.util.Scanner;
import models.HistoriaClinica;
import models.Paciente; // Se necesita para la lógica de "agregar HC"
import service.PacienteService;
import views.historias.HistoriaMenu;

/**
 * Sub-Controlador (o Sub-Menú) para todas las operaciones
 * relacionadas con la entidad Paciente.
 * <p>
 * Esta clase implementa la lógica de orquestación de la UI para
 * Pacientes. Es llamada por el {@link main.java.views.MenuHandler} principal
 * y coordina las interacciones entre el {@link PacienteService}
 * (lógica de negocio) y la {@link PacienteView} (lógica de I/O).
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li>Orquestar el flujo para crear, listar, actualizar y eliminar
 * pacientes.</li>
 * <li>Manejar las <b>excepciones</b> provenientes de la capa de Servicio
 * (<code>IllegalArgumentException</code>, <code>SQLException</code>, etc.)
 * y traducirlas en mensajes amigables para el usuario usando
 * <code>pacienteView.mostrarError()</code>.</li>
 * <li>Coordinar con otros sub-controladores (como {@link HistoriaMenu})
 * para lógicas complejas (ej: agregar una HC nueva al actualizar un
 * paciente).</li>
 * </ul>
 *
 * @author alpha team
 * @see main.java.views.MenuHandler
 * @see main.java.views.pacientes.PacienteView
 * @see main.java.service.PacienteService
 */
public class PacienteMenu {

    private final PacienteService pacienteService;
    private final PacienteView pacienteView;
    private final HistoriaMenu historiaMenu; // Necesario para HU-003

    /**
     * Constructor que inyecta las dependencias necesarias.
     *
     * @param pacienteService El servicio de negocio para Pacientes.
     * @param pacienteView    La vista "tonta" para I/O de Pacientes.
     * @param historiaMenu    El sub-controlador de Historias, necesario
     *                        para la lógica de "agregar HC" al crear/actualizar
     *                        un paciente.
     */
    public PacienteMenu(PacienteService pacienteService, PacienteView pacienteView, HistoriaMenu historiaMenu) {
        if (pacienteService == null) {
            throw new IllegalArgumentException("PacienteService no puede ser nulo.");
        }
        if (pacienteView == null) {
            throw new IllegalArgumentException("PacienteView no puede ser nulo.");
        }
        if (historiaMenu == null) {
            throw new IllegalArgumentException("HistoriaMenu no puede ser nulo.");
        }
        this.pacienteService = pacienteService;
        this.pacienteView = pacienteView;
        this.historiaMenu = historiaMenu;
    }

    /**
     * Proporciona acceso al Scanner de la vista.
     * Utilizado por {@link main.java.views.MenuHandler} para su submenú.
     *
     * @return La instancia compartida del {@link Scanner}.
     */
    public Scanner getViewScanner() {
        return this.pacienteView.getScanner();
    }

    /**
     * Orquesta la creación de un nuevo Paciente (Opción 2 del menú).
     * <p>
     * <b>Flujo (HU-001):</b>
     * <ol>
     * <li>Llama a <code>pacienteView.solicitarDatosPaciente()</code> para
     * obtener un objeto Paciente "crudo".</li>
     * <li>Pregunta (usando <code>pacienteView</code>) si se desea
     * agregar una HC.</li>
     * <li>Si la respuesta es "s", delega a
     * <code>historiaMenu.handleCrearHistoria()</code> para crear la HC
     * y la asocia al paciente.</li>
     * <li>Llama a <code>pacienteService.insert()</code> con el objeto
     * Paciente (que puede tener o no una HC).</li>
     * <li>Muestra éxito o error usando <code>pacienteView</code>.</li>
     * </ol>
     * </p>
     */
    public void handleCrearPaciente() {
        try {
            // 1. Vista: Obtener datos "crudos" del Paciente
            Paciente nuevoPaciente = pacienteView.solicitarDatosPaciente();

            // 2. Vista: Confirmar si se agrega HC
            if (pacienteView.solicitarConfirmacion("¿Desea agregar una Historia Clínica ahora?")) {
                // 3. Delegar creación de HC al sub-controlador de Historias
                // El método 'handleCrearHistoria' crea la HC y la devuelve
                HistoriaClinica nuevaHc = historiaMenu.handleCrearHistoria();
                if (nuevaHc != null) {
                    nuevoPaciente.setHistoriaClinica(nuevaHc);
                    pacienteView.mostrarExito("\nNueva Historia Clínica (ID: " + nuevaHc.getId() + ") creada.");
                } else {
                    pacienteView.mostrarError("La creación de la Historia Clínica fue cancelada.");
                }
            }

            // 4. Servicio: Validar y persistir el Paciente (con o sin HC)
            pacienteService.insert(nuevoPaciente);

            // 5. Vista: Mostrar resultado
            pacienteView.mostrarExito("Paciente creado exitosamente con ID: " + nuevoPaciente.getId() + "\n");
            pacienteView.mostrarPacienteDetalle(nuevoPaciente);

        } catch (Exception e) {
            // Manejo de errores de Servicio (Validación, BD) o Vista (Formato)
            pacienteView.mostrarError(e.getMessage());
        }
    }

    /**
     * Orquesta el listado y búsqueda de Pacientes (Opción 1 del menú).
     * <p>
     * <b>Flujo (HU-002):</b>
     * <ol>
     * <li>Muestra un submenú (Listar todos, Buscar por DNI, Buscar por
     * Filtro).</li>
     * <li>Según la opción:</li>
     * <li>a. Llama a <code>pacienteService.selectAll(false)</code>.</li>
     * <li>b. Llama a <code>pacienteService.selectByDni()</code>.</li>
     * <li>c. Llama a <code>pacienteService.searchByFilter()</code>.</li>
     * <li>Pasa la lista resultante (o el paciente único) a
     * <code>pacienteView.mostrarPacientes()</code>.</li>
     * <li>Maneja excepciones y muestra errores con
     * <code>pacienteView.mostrarError()</code>.</li>
     * </ol>
     * </p>
     */
    public void handleListarPacientes() {
        try {
            System.out.println("\n--- Listar/Buscar Pacientes ---");
            System.out.println("1. Listar todos los pacientes activos");
            System.out.println("2. Buscar paciente por DNI");
            System.out.println("3. Buscar paciente por Nombre o Apellido");
            System.out.println("\n0. Volver");
            System.out.print("\nIngrese una opción -> ");

            int subopcion = Integer.parseInt(pacienteView.getScanner().nextLine().trim());
            List<Paciente> pacientes;

            switch (subopcion) {
                case 1 -> {
                    // Listar todos
                    pacientes = pacienteService.selectAll(false); // false = NO eliminados
                    pacienteView.mostrarPacientes(pacientes);
                }
                case 2 -> {
                    // Buscar por DNI
                    System.out.print("\nIngrese el DNI a buscar -> ");
                    String dni = pacienteView.getScanner().nextLine().trim();
                    Paciente p = pacienteService.selectByDni(dni);
                    pacienteView.mostrarPacientes(p != null ? List.of(p) : List.of());
                }
                case 3 -> {
                    // Buscar por Filtro
                    String filtro = pacienteView.solicitarFiltroBusqueda();
                    pacientes = pacienteService.searchByFilter(filtro);
                    pacienteView.mostrarPacientes(pacientes);
                }
                case 0 -> {
                }
                default -> pacienteView.mostrarError("Opción no válida.");
            }
            // Volver
        } catch (Exception e) {
            pacienteView.mostrarError(e.getMessage());
        }
    }

    /**
     * Orquesta la actualización de un Paciente (Opción 3 del menú).
     * <p>
     * <b>Flujo (HU-003):</b>
     * <ol>
     * <li>Llama a <code>pacienteView.solicitarIdPaciente("actualizar")</code>.</li>
     * <li>Llama a <code>pacienteService.selectById(id, false)</code>
     * para obtener el paciente <b>activo</b>.</li>
     * <li>Si no existe, muestra error.</li>
     * <li>Llama a <code>pacienteView.solicitarDatosActualizacion(paciente)</code>
     * (patrón "Enter para mantener").</li>
     * <li>Si el paciente <b>no</b> tiene HC, pregunta si desea agregar una
     * (lógica de HU-003) y delega a
     * <code>historiaMenu.handleCrearHistoria()</code>.</li>
     * <li>Llama a <code>pacienteService.update()</code>.</li>
     * <li>Muestra éxito o error.</li>
     * </ol>
     * </p>
     */
    public void handleActualizarPaciente() {
        try {
            // 1. Vista: Pedir ID
            int id = pacienteView.solicitarIdPaciente("actualizar");

            // 2. Servicio: Obtener paciente ACTIVO
            Paciente paciente = pacienteService.selectById(id, false);
            if (paciente == null) {
                pacienteView.mostrarError("No se encontró un paciente activo con ID: " + id);
                return;
            }

            // 3. Vista: Mostrar datos actuales y pedir nuevos
            pacienteView.mostrarPacienteDetalle(paciente);
            paciente = pacienteView.solicitarDatosActualizacion(paciente);

            // 4. Lógica de HU-003: Agregar HC si no tiene
            if (paciente.getHistoriaClinica() == null) {
                if (pacienteView.solicitarConfirmacion("Este paciente no tiene HC. ¿Desea agregar una ahora?")) {
                    // Reutilizar el sub-controlador de Historias
                    HistoriaClinica nuevaHc = historiaMenu.handleCrearHistoria();
                    if (nuevaHc != null) {
                        paciente.setHistoriaClinica(nuevaHc);
                        pacienteView.mostrarExito("Nueva Historia Clínica (ID: " + nuevaHc.getId() + ") asignada.");
                    } else {
                        pacienteView.mostrarError("Creación de HC cancelada. Paciente no actualizado.");
                        return; // Cancela la actualización del paciente
                    }
                }
            }
            // (La Opción 9 se usa para *actualizar* o *asignar* una HC existente)

            // 5. Servicio: Validar (RN) y persistir
            pacienteService.update(paciente);

            // 6. Vista: Mostrar resultado
            pacienteView.mostrarExito("\nPaciente actualizado exitosamente.");
            pacienteView.mostrarPacienteDetalle(paciente);

        } catch (Exception e) {
            pacienteView.mostrarError(e.getMessage());
        }
    }

    /**
     * Orquesta la eliminación (lógica) de un Paciente (Opción 4 del menú).
     * <p>
     * <b>Flujo (HU-004 / RN-013):</b>
     * <ol>
     * <li>Llama a <code>pacienteView.solicitarIdPaciente("eliminar")</code>.</li>
     * <li>Llama a <code>pacienteService.delete(id)</code>.</li>
     * <li>El servicio se encarga de la lógica de cascada (eliminar
     * Paciente Y su HC asociada).</li>
     * <li>Muestra éxito o error.</li>
     * </ol>
     * </p>
     */
    public void handleEliminarPaciente() {
        try {
            // 1. Vista: Pedir ID
            int id = pacienteView.solicitarIdPaciente("eliminar (baja lógica)");

            // 2. Vista: Pedir confirmación (¡Buena práctica!)
            if (pacienteView.solicitarConfirmacion(
                    "¿Está seguro que desea eliminar al paciente ID " + id + "? (Esto también eliminará su HC)")) {
                // 3. Servicio: Ejecutar lógica de negocio
                pacienteService.delete(id);
                // 4. Vista: Mostrar resultado
                pacienteView
                        .mostrarExito("Paciente ID: " + id + " y su HC asociada han sido eliminados (baja lógica).");
            } else {
                pacienteView.mostrarError("Eliminación cancelada.");
            }

        } catch (Exception e) {
            pacienteView.mostrarError(e.getMessage());
        }
    }

    /**
     * Orquesta el listado de Pacientes Eliminados (Opción 11.1).
     *
     * @see views.MenuHandler#handleSubmenuRecuperacion()
     */
    public void handleListarPacientesEliminados() {
        try {
            // 1. Servicio: Llamar a selectAll(true)
            List<Paciente> pacientes = pacienteService.selectAll(true); // true = SÍ eliminados
            // 2. Vista: Mostrar la lista
            pacienteView.mostrarPacientes(pacientes);
        } catch (Exception e) {
            pacienteView.mostrarError(e.getMessage());
        }
    }

    /**
     * Orquesta la recuperación (lógica) de un Paciente (Opción 11.2).
     * <p>
     * <b>Flujo (HU-010 / RN-031):</b>
     * <ol>
     * <li>Llama a <code>pacienteView.solicitarIdPaciente("recuperar")</code>.</li>
     * <li>Llama a <code>pacienteService.recover(id)</code>.</li>
     * <li>El servicio se encarga de la lógica de cascada (recuperar
     * Paciente Y su HC asociada).</li>
     * <li>Muestra éxito o error.</li>
     * </ol>
     * </p>
     *
     * @see views.MenuHandler#handleSubmenuRecuperacion()
     */
    public void handleRecuperarPaciente() {
        try {
            // 1. Vista: Pedir ID
            int id = pacienteView.solicitarIdPaciente("recuperar");
            // 2. Servicio: Ejecutar lógica de negocio
            pacienteService.recover(id);
            // 3. Vista: Mostrar resultado
            pacienteView.mostrarExito("Paciente ID: " + id + " y su HC asociada han sido recuperados.");
        } catch (Exception e) {
            pacienteView.mostrarError(e.getMessage());
        }
    }
}