package main.java.views.historias;

import java.util.List;
import main.java.models.HistoriaClinica;
import main.java.models.Paciente;
import main.java.service.HistoriaClinicaService;
import main.java.service.PacienteService;

/**
 * Sub-Controlador (o Sub-Menú) para todas las operaciones
 * relacionadas con la entidad HistoriaClinica.
 * <p>
 * Esta clase implementa la lógica de orquestación de la UI para
 * Historias Clínicas. Es llamada por el {@link main.java.views.MenuHandler} principal
 * y coordina las interacciones entre los Servicios ({@link PacienteService},
 * {@link HistoriaClinicaService}) y la {@link HistoriaView} (lógica de I/O).
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li>Orquestar el flujo para crear, listar, actualizar y eliminar HCs.</li>
 * <li>Manejar las <b>lógicas de relación 1-a-1</b> más complejas, como
 * {@link #handleGestionarHistoriaPorPaciente(int)} (HU-009) y
 * {@link #handleEliminarHistoriaPorPaciente()} (HU-008).</li>
 * <li>Manejar las <b>excepciones</b> provenientes de la capa de Servicio
 * y traducirlas en mensajes amigables para el usuario usando
 * <code>historiaView.mostrarError()</code>.</li>
 * </ul>
 *
 * @author alpha team
 * @see main.java.views.MenuHandler
 * @see main.java.views.historias.HistoriaView
 * @see main.java.service.HistoriaClinicaService
 * @see main.java.service.PacienteService
 */
public class HistoriaMenu {

    private final HistoriaView historiaView;
    private final HistoriaClinicaService historiaClinicaService;
    private final PacienteService pacienteService; // Necesario para asignar/gestionar HC de pacientes

    /**
     * Constructor que inyecta las dependencias necesarias.
     * <p>
     * <b>Nota:</b> Este controlador necesita <b>ambos</b> servicios.
     * <code>HistoriaClinicaService</code> para crear/actualizar HCs.
     * <code>PacienteService</code> para buscar pacientes y
     * actualizar su FK <code>historia_clinica_id</code>.
     * </p>
     *
     * @param historiaView           La vista "tonta" para I/O de HCs.
     * @param historiaClinicaService El servicio de negocio para HCs.
     * @param pacienteService        El servicio de negocio para Pacientes.
     */
    public HistoriaMenu(HistoriaView historiaView, HistoriaClinicaService historiaClinicaService,
            PacienteService pacienteService) {
        if (historiaView == null) {
            throw new IllegalArgumentException("HistoriaView no puede ser nulo.");
        }
        if (historiaClinicaService == null) {
            throw new IllegalArgumentException("HistoriaClinicaService no puede ser nulo.");
        }
        if (pacienteService == null) {
            throw new IllegalArgumentException("PacienteService no puede ser nulo.");
        }
        this.historiaView = historiaView;
        this.historiaClinicaService = historiaClinicaService;
        this.pacienteService = pacienteService;
    }

    /**
     * Orquesta el listado y búsqueda de Historias Clínicas (Opción 5 del menú).
     * <p>
     * <b>Flujo (HU-006):</b>
     * <ol>
     * <li>Muestra un submenú (Listar todas, Buscar por ID, Buscar por Filtro,
     * Buscar por Nro. Historia).</li>
     * <li>Según la opción, llama al método de servicio correspondiente.</li>
     * <li>Pasa la lista resultante (o la HC única) a
     * <code>historiaView.mostrarHistorias()</code>.</li>
     * <li>Maneja excepciones y muestra errores.</li>
     * </ol>
     * </p>
     */
    public void handleListarHistorias() {
        try {
            System.out.println("\n--- Listar/Buscar Historias Clínicas ---");
            System.out.println("1. Listar todas las HCs activas");
            System.out.println("2. Buscar HC por ID");
            System.out.println("3. Buscar HC por Filtro (texto)");
            System.out.println("4. Buscar HC por Nro. de Historia (Exacto)");
            System.out.println("0. Volver");
            System.out.print("Ingrese una opción -> ");

            int subopcion = Integer.parseInt(historiaView.getScanner().nextLine().trim());
            List<HistoriaClinica> historias;

            switch (subopcion) {
                case 1 -> {
                    // Listar todas
                    historias = historiaClinicaService.selectAll(false); // false = NO eliminadas
                    historiaView.mostrarHistorias(historias);
                }
                case 2 -> {
                    // Buscar por ID
                    int id = historiaView.solicitarIdHistoria("buscar");
                    HistoriaClinica hc = historiaClinicaService.selectById(id, false);
                    historiaView.mostrarHistorias(hc != null ? List.of(hc) : List.of());
                }
                case 3 -> {
                    // Buscar por Filtro
                    String filtro = historiaView.solicitarFiltroBusqueda();
                    historias = historiaClinicaService.searchByFilter(filtro);
                    historiaView.mostrarHistorias(historias);
                }
                case 4 -> {
                    // Buscar por Nro. Historia
                    System.out.print("Ingrese el Nro. de Historia exacto -> ");
                    String nro = historiaView.getScanner().nextLine().trim();
                    // Asumimos que HCServicio tiene un método selectByNroHistoria
                    // (lo cual es necesario para la validación de unicidad)
                    HistoriaClinica hcNro = historiaClinicaService.selectByNroHistoria(nro);
                    historiaView.mostrarHistorias(hcNro != null ? List.of(hcNro) : List.of());
                }
                case 0 -> {
                }
                default -> System.err.println("Opción no válida.");
            }
            // Volver
        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
        }
    }

    /**
     * Orquesta la creación de una HC independiente (Opción 6 del menú).
     * <p>
     * <b>Flujo (HU-005):</b>
     * <ol>
     * <li>Llama al método reutilizable {@link #handleCrearHistoria()}.</li>
     * <li>Si la creación fue exitosa (no nula), muestra el mensaje de éxito.</li>
     * </ol>
     * </p>
     */
    public void handleCrearHistoriaIndependiente() {
        try {
            HistoriaClinica nuevaHc = this.handleCrearHistoria();
            if (nuevaHc != null) {
                historiaView.mostrarExito("\nHistoria Clínica independiente creada con ID: " + nuevaHc.getId());
            } else {
                historiaView.mostrarError("Creación de Historia Clínica cancelada.");
            }
        } catch (Exception e) {
            historiaView.mostrarError(e.getMessage());
        }
    }

    /**
     * Orquesta la actualización de una HC por ID (Opción 7 del menú).
     * <p>
     * <b>Importante:</b> Esta operación (al igual que la Opción 8) no
     * está vinculada a un paciente. Si la HC está asignada a uno o más
     * pacientes, la actualización será visible para todos ellos.
     * </p>
     *
     * @see views.pacientes.PacienteMenu#handleActualizarPaciente()
     * @see #handleGestionarHistoriaPorPaciente(int)
     */
    public void handleActualizarHistoriaPorId() {
        try {
            // 1. Vista: Pedir ID
            int id = historiaView.solicitarIdHistoria("actualizar");

            // 2. Servicio: Obtener HC activa
            HistoriaClinica hc = historiaClinicaService.selectById(id, false);
            if (hc == null) {
                historiaView.mostrarError("No se encontró una Historia Clínica activa con ID: " + id);
                return;
            }

            // 3. Vista: Mostrar datos actuales y pedir nuevos
            historiaView.mostrarHistoriaDetalle(hc);
            hc = historiaView.solicitarDatosActualizacion(hc);

            // 4. Servicio: Validar (RN) y persistir
            historiaClinicaService.update(hc);

            // 5. Vista: Mostrar resultado
            historiaView.mostrarExito("\nHistoria Clínica actualizada exitosamente.");

        } catch (Exception e) {
            historiaView.mostrarError(e.getMessage());
        }
    }

    /**
     * Orquesta la eliminación (lógica) de una HC por ID (Opción 8 del menú).
     * <p>
     * <b>Flujo (HU-007 - PELIGROSO):</b>
     * <ol>
     * <li>Llama a <code>historiaView.solicitarIdHistoria("eliminar")</code>.</li>
     * <li>Advierte al usuario sobre el riesgo.</li>
     * <li>Si confirma, llama a <code>historiaClinicaService.delete(id)</code>.</li>
     * <li>Esto <b>NO</b> actualiza la FK en el <code>Paciente</code>
     * y puede dejar referencias huérfanas.</li>
     * </ol>
     * </p>
     */
    public void handleEliminarHistoriaPorId() {
        try {
            // 1. Vista: Pedir ID
            int id = historiaView.solicitarIdHistoria("eliminar (baja lógica)");

            // 2. Vista: Advertir y pedir confirmación
            System.err.println("⚠️ ADVERTENCIA: Esta opción es peligrosa.");
            System.err.println("Si esta HC está asignada a un Paciente, se creará una referencia huérfana.");
            System.err.println("Use la 'Opción 10: Eliminar HC por Paciente' para una eliminación segura.");

            System.err.print("\nDesea eliminar esta HC de todas formas? (s/n) -> ");
            if (historiaView.getScanner().nextLine().trim().equalsIgnoreCase("s")) {
                // 3. Servicio: Ejecutar lógica de negocio
                historiaClinicaService.delete(id);
                // 4. Vista: Mostrar resultado
                historiaView.mostrarExito("\nHistoria Clínica ID: " + id + " ha sido eliminada (baja lógica).");
            } else {
                historiaView.mostrarError("Eliminación cancelada.");
            }

        } catch (Exception e) {
            historiaView.mostrarError(e.getMessage());
        }
    }

    /**
     * Orquesta la gestión de la HC de un Paciente (Opción 9 del menú).
     * <p>
     * <b>Flujo (HU-009):</b>
     * <ol>
     * <li>Pide el ID del Paciente (usando <code>pacienteService</code>
     * para obtener la vista, aunque sería mejor tener una vista de Paciente
     * inyectada).</li>
     * <li>Busca al Paciente (<code>pacienteService.selectById</code>).</li>
     * <li><b>Caso 1: Paciente YA TIENE HC.</b>
     * <ul>
     * <li>Llama a <code>historiaView.solicitarDatosActualizacion()</code>
     * y <code>historiaClinicaService.update()</code>.</li>
     * </ul>
     * </li>
     * <li><b>Caso 2: Paciente NO TIENE HC.</b>
     * <ul>
     * <li>Muestra submenú: "1. Crear Nueva" o "2. Asignar Existente".</li>
     * <li><b>Caso 2.1:</b> Llama a <code>handleCrearHistoria()</code>
     * y asigna la nueva HC al paciente.</li>
     * <li><b>Caso 2.2:</b> Pide ID de HC, la busca con
     * <code>historiaClinicaService.selectById()</code> y la asigna.</li>
     * <li>Llama a <code>pacienteService.update(paciente)</code> para
     * guardar la nueva FK.</li>
     * </ul>
     * </li>
     * </ol>
     * </p>
     */
    public void handleGestionarHistoriaPorPaciente() {
        try {
            // 1. Pedir ID Paciente
            System.out.print("\nIngrese el ID del Paciente cuya HC desea gestionar -> ");
            int pacienteId = Integer.parseInt(historiaView.getScanner().nextLine().trim());

            // 2. Buscar Paciente
            Paciente paciente = pacienteService.selectById(pacienteId, false);
            if (paciente == null) {
                historiaView.mostrarError("No se encontró un Paciente activo con ID: " + pacienteId);
                return;
            }

            // 3. Flujo Lógico (HU-009)
            if (paciente.getHistoriaClinica() != null) {
                // --- CASO 1: Paciente YA TIENE HC (Solo se puede actualizar) ---
                HistoriaClinica hcExistente = paciente.getHistoriaClinica();
                System.out.println("El paciente ya tiene una HC. Actualizando HC ID: " + hcExistente.getId());

                historiaView.mostrarHistoriaDetalle(hcExistente);
                hcExistente = historiaView.solicitarDatosActualizacion(hcExistente);

                historiaClinicaService.update(hcExistente);
                historiaView.mostrarExito("Historia Clínica del paciente actualizada.");

            } else {
                // --- CASO 2: Paciente NO TIENE HC (Crear o Asignar) ---
                System.out.println("El paciente no tiene una Historia Clínica asociada.¿Qué desea hacer?\n");
                System.out.println("1. Crear y Asignar una Nueva Historia Clínica");
                System.out.println("2. Asignar una Historia Clínica Existente");
                System.out.println("0. Volver");
                System.out.print("\nIngrese una opción -> ");
                int subopcion = Integer.parseInt(historiaView.getScanner().nextLine().trim());

                HistoriaClinica hcParaAsignar = null;

                if (subopcion == 1) {
                    // 2.1: Crear Nueva
                    hcParaAsignar = this.handleCrearHistoria(); // Llama al método reutilizable
                } else if (subopcion == 2) {
                    // 2.2: Asignar Existente
                    int hcId = historiaView.solicitarIdHistoria("asignar");
                    hcParaAsignar = historiaClinicaService.selectById(hcId, false);
                    if (hcParaAsignar == null) {
                        historiaView.mostrarError("No se encontró una HC activa con ID: " + hcId);
                    }
                } else {
                    return; // Volver
                }

                // 4. Asignar y Guardar
                if (hcParaAsignar != null) {
                    paciente.setHistoriaClinica(hcParaAsignar);
                    pacienteService.update(paciente); // Guarda la FK en Paciente
                    historiaView.mostrarExito("Historia Clínica (ID: " + hcParaAsignar.getId()
                            + ") asignada al Paciente (ID: " + paciente.getId() + ").");
                }
            }
        } catch (Exception e) {
            historiaView.mostrarError(e.getMessage());
        }
    }

    /**
     * Orquesta la eliminación (lógica) de una HC por Paciente (Opción 10).
     * <p>
     * <b>Flujo (HU-008 - SEGURO):</b>
     * <ol>
     * <li>Pide el ID del Paciente.</li>
     * <li>Busca al Paciente y su HC.</li>
     * <li>Si existe, llama a
     * <code>pacienteService.deleteHistoriaClinica(pacienteId, hcId)</code>.</li>
     * <li>El servicio se encarga de la "eliminación segura"
     * (UPDATE Paciente SET FK=NULL, luego DELETE HC).</li>
     * </ol>
     * </p>
     */
    public void handleEliminarHistoriaPorPaciente() {
        try {
            // 1. Vista: Pedir ID Paciente
            System.out.print("\nIngrese el ID del Paciente cuya HC desea eliminar (de forma segura) -> ");
            int pacienteId = Integer.parseInt(historiaView.getScanner().nextLine().trim());

            // 2. Servicio: Obtener Paciente (para saber ID de HC)
            Paciente paciente = pacienteService.selectById(pacienteId, false);
            if (paciente == null || paciente.getHistoriaClinica() == null) {
                historiaView.mostrarError("No se encontró un paciente con HC asociada para el ID: " + pacienteId);
                return;
            }

            int hcId = paciente.getHistoriaClinica().getId();

            // 3. Vista: Pedir confirmación
            System.out.print("\nDesea eliminar la HC (ID: " + hcId + ") de forma segura? (s/n) -> ");
            if (historiaView.getScanner().nextLine().trim().equalsIgnoreCase("s")) {

                // 4. Servicio: Ejecutar lógica de eliminación segura (HU-008)
                pacienteService.deleteHistoriaClinica(pacienteId, hcId);

                // 5. Vista: Mostrar resultado
                historiaView
                        .mostrarExito("\nHistoria Clínica (ID: " + hcId + ") desasociada y eliminada exitosamente.");
            } else {
                historiaView.mostrarError("Eliminación cancelada.");
            }

        } catch (Exception e) {
            historiaView.mostrarError(e.getMessage());
        }
    }

    /**
     * Orquesta el listado de HCs Eliminadas (Opción 11.3).
     *
     * @see views.MenuHandler#handleSubmenuRecuperacion()
     */
    public void handleListarHistoriasEliminadas() {
        try {
            // 1. Servicio: Llamar a selectAll(true)
            List<HistoriaClinica> historias = historiaClinicaService.selectAll(true); // true = SÍ eliminadas
            // 2. Vista: Mostrar la lista
            historiaView.mostrarHistorias(historias);
        } catch (Exception e) {
            historiaView.mostrarError(e.getMessage());
        }
    }

    /**
     * Orquesta la recuperación (lógica) de una HC (Opción 11.4).
     *
     * @see views.MenuHandler#handleSubmenuRecuperacion()
     */
    public void handleRecuperarHistoria() {
        try {
            // 1. Vista: Pedir ID
            int id = historiaView.solicitarIdHistoria("recuperar");
            // 2. Servicio: Ejecutar lógica de negocio
            historiaClinicaService.recover(id);
            // 3. Vista: Mostrar resultado
            historiaView.mostrarExito("Historia Clínica ID: " + id + " ha sido recuperada.");
        } catch (Exception e) {
            historiaView.mostrarError(e.getMessage());
        }
    }

    // ============ MÉTODOS HELPER (Reutilizables) ============

    /**
     * Método helper reutilizable para crear una Historia Clínica.
     * <p>
     * Este método es llamado por:
     * <ul>
     * <li><code>handleCrearHistoriaIndependiente()</code> (Opción 6)</li>
     * <li><code>PacienteMenu.handleCrearPaciente()</code> (Opción 2)</li>
     * <li><code>handleGestionarHistoriaPorPaciente()</code> (Opción 9.1)</li>
     * </ul>
     * </p>
     * <p>
     * <b>Flujo:</b>
     * <ol>
     * <li>Llama a <code>historiaView.solicitarDatosHistoria()</code>
     * para obtener un objeto "crudo".</li>
     * <li>Llama a <code>historiaClinicaService.insert()</code>
     * para validar (RN) y persistir la HC.</li>
     * <li>El servicio (y el DAO) se encargan de asignar el
     * ID auto-generado al objeto.</li>
     * <li><b>Devuelve</b> el objeto <code>HistoriaClinica</code>
     * completo (con su nuevo ID).</li>
     * </ol>
     * </p>
     *
     * @return La {@link HistoriaClinica} recién creada y persistida
     *         (con su ID), o <code>null</code> si la creación falla.
     */
    public HistoriaClinica handleCrearHistoria() {
        try {
            // 1. Vista: Obtener datos "crudos"
            HistoriaClinica nuevaHc = historiaView.solicitarDatosHistoria();

            // 2. Servicio: Validar (RN-015, RN-016, RN-017) y persistir
            historiaClinicaService.insert(nuevaHc);

            // 3. Devolver la HC con su nuevo ID
            return nuevaHc;

        } catch (Exception e) {
            historiaView.mostrarError(e.getMessage());
            return null; // Indicar que la creación falló
        }
    }
}