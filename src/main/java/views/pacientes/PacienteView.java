package main.java.views.pacientes;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import main.java.models.HistoriaClinica;
import main.java.models.Paciente;

/**
 * Clase de Vista (View) específica para la entidad Paciente.
 * <p>
 * Esta clase es una vista "tonta" (Dumb View). Su <b>única responsabilidad</b>
 * es interactuar con la consola (<code>System.in</code> y
 * <code>System.out</code>).
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li><b>Mostrar Datos:</b> Imprimir los detalles de uno o
 * más <code>Paciente</code>s en un formato legible.</li>
 * <li><b>Capturar Datos:</b> Solicitar al usuario entradas (como DNI,
 * nombre, etc.) usando el {@link Scanner} inyectado.</li>
 * <li><b>Mostrar Mensajes:</b> Imprimir mensajes de éxito
 * (<code>mostrarExito</code>)
 * o error (<code>mostrarError</code>).</li>
 * </ul>
 *
 * <h3>Arquitectura:</h3>
 * <p>
 * Esta clase es llamada <b>exclusivamente</b> por su controlador,
 * {@link PacienteMenu}. <b>Nunca</b> llama a la capa de Servicio
 * ({@link service.PacienteService}) directamente.
 * </p>
 *
 * @author alpha team
 * @see PacienteMenu
 * @see main.java.models.Paciente
 */
public class PacienteView {

    /**
     * El Scanner compartido de la aplicación, inyectado desde
     * {@link main.java.views.AppMenu}.
     */
    private final Scanner scanner;

    /**
     * Constructor que inyecta el Scanner.
     *
     * @param scanner La instancia única de {@link Scanner} que maneja
     *                <code>System.in</code>.
     */
    public PacienteView(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("El Scanner no puede ser nulo.");
        }
        this.scanner = scanner;
    }

    /**
     * Proporciona acceso al Scanner inyectado.
     * <p>
     * Utilizado por {@link main.java.views.MenuHandler} para manejar submenús
     * que residen en el controlador principal (como el menú de recuperación).
     * </p>
     *
     * @return La instancia compartida del {@link Scanner}.
     */
    public Scanner getScanner() {
        return this.scanner;
    }

    /**
     * Imprime una lista de pacientes en la consola.
     * <p>
     * Si la lista está vacía, muestra un mensaje "No se encontraron...".
     * Si no, itera sobre la lista y llama a
     * {@link #mostrarPacienteDetalle(Paciente)}
     * para cada uno.
     * </p>
     *
     * @param pacientes La {@link List} de {@link Paciente}s a mostrar.
     */
    public void mostrarPacientes(List<Paciente> pacientes) {
        if (pacientes == null || pacientes.isEmpty()) {
            System.out.println("No se encontraron pacientes que coincidan con los criterios.");
            return;
        }

        for (Paciente p : pacientes) {
            mostrarPacienteDetalle(p);
            System.out.println("------------------------------");
        }
    }

    /**
     * Imprime los detalles de un único objeto Paciente en un formato legible.
     * <p>
     * Maneja la lógica de visualización de la relación 1-a-1:
     * <ul>
     * <li>Si <code>paciente.getHistoriaClinica()</code> no es <code>null</code>,
     * imprime los detalles de la HC.</li>
     * <li>Si es <code>null</code>, imprime "Sin Historia Clínica".</li>
     * </ul>
     * También calcula y muestra la edad del paciente.
     * </p>
     *
     * @param paciente El {@link Paciente} a mostrar.
     */
    public void mostrarPacienteDetalle(Paciente paciente) {
        if (paciente == null) {
            System.err.println("Error: El paciente a mostrar es nulo.");
            return;
        }

        // Calcular Edad
        int edad = 0;
        if (paciente.getFechaNacimiento() != null) {
            edad = Period.between(paciente.getFechaNacimiento(), LocalDate.now()).getYears();
        }

        // Imprimir datos del Paciente
        System.out.printf("ID: %d | DNI: %s\n", paciente.getId(), paciente.getDni());
        System.out.printf("Nombre: %s, %s\n", paciente.getApellido(), paciente.getNombre());
        System.out.printf("Fecha Nac: %s (%d años)\n", paciente.getFechaNacimiento(), edad);
        System.out.printf("Eliminado: %s\n", paciente.isEliminado() ? "Si" : "No");

        // Imprimir datos de la Historia Clínica (Entidad B)
        HistoriaClinica hc = paciente.getHistoriaClinica();
        if (hc != null) {
            System.out.printf("  HC Nro: %s (ID: %d)\n", hc.getNumeroHistoria(), hc.getId());
            System.out.printf("  Grupo Sang.: %s\n", hc.getGrupoSanguineo());
            System.out.printf("  Antecedentes: %s\n", hc.getAntecedentes() != null ? hc.getAntecedentes() : "N/A");
        } else {
            System.out.println("  HC: Sin Historia Clínica asignada.");
        }
    }

    /**
     * Solicita al usuario los datos para crear un nuevo Paciente.
     * <p>
     * <b>Importante:</b> Esta función <b>no</b> realiza validaciones de
     * Reglas de Negocio (como formato de DNI o unicidad). Su única
     * responsabilidad es capturar la entrada (<code>String</code>) y
     * crear un objeto <code>Paciente</code> "crudo".
     * </p>
     * <p>
     * La validación (RN) es responsabilidad de {@link service.PacienteService}.
     * </p>
     *
     * @return Un nuevo objeto {@link Paciente} (con <code>id=0</code>)
     *         poblado con los datos ingresados.
     * @throws DateTimeParseException Si el usuario ingresa un formato de
     *                                fecha inválido.
     */
    public Paciente solicitarDatosPaciente() throws DateTimeParseException {
        System.out.println("\n--- Ingrese los datos del nuevo Paciente ---");
        System.out.print("Nombre -> ");
        String nombre = scanner.nextLine().trim();

        System.out.print("Apellido -> ");
        String apellido = scanner.nextLine().trim();

        System.out.print("DNI (solo números) -> ");
        String dni = scanner.nextLine().trim();

        System.out.print("Fecha de Nacimiento (AAAA-MM-DD) -> ");
        LocalDate fechaNac = LocalDate.parse(scanner.nextLine().trim());

        // Devuelve un objeto "crudo" (sin ID).
        // La capa de Servicio lo validará.
        return new Paciente(nombre, apellido, dni, fechaNac);
    }

    /**
     * Solicita al usuario los datos para actualizar un Paciente existente.
     * <p>
     * Implementa el patrón "Enter para mantener":
     * Si el usuario presiona Enter (la entrada está vacía), el método
     * <b>no</b> actualiza ese campo en el objeto <code>paciente</code>.
     * </p>
     *
     * @param paciente El objeto {@link Paciente} a actualizar (con sus
     *                 datos actuales).
     * @return El mismo objeto <code>Paciente</code>, modificado
     *         (mutado) con los nuevos datos.
     * @throws DateTimeParseException Si el usuario ingresa un formato de
     *                                fecha inválido.
     */
    public Paciente solicitarDatosActualizacion(Paciente paciente) throws DateTimeParseException {
        System.out.println("\n--- Actualizar Paciente (Presione Enter para mantener el valor actual) ---");

        System.out.printf("Nombre [%s] -> ", paciente.getNombre());
        String nombre = scanner.nextLine().trim();
        if (!nombre.isEmpty()) {
            paciente.setNombre(nombre);
        }

        System.out.printf("Apellido [%s] -> ", paciente.getApellido());
        String apellido = scanner.nextLine().trim();
        if (!apellido.isEmpty()) {
            paciente.setApellido(apellido);
        }

        System.out.printf("DNI [%s] -> ", paciente.getDni());
        String dni = scanner.nextLine().trim();
        if (!dni.isEmpty()) {
            paciente.setDni(dni);
        }

        System.out.printf("Fecha Nacimiento [%s] -> ", paciente.getFechaNacimiento());
        String fechaStr = scanner.nextLine().trim();
        if (!fechaStr.isEmpty()) {
            paciente.setFechaNacimiento(LocalDate.parse(fechaStr));
        }

        return paciente;
    }

    /**
     * Solicita al usuario un ID de Paciente para una acción específica.
     *
     * @param accion El verbo de la acción (ej: "actualizar", "eliminar",
     *               "recuperar").
     * @return El <code>int</code> ID ingresado por el usuario.
     * @throws NumberFormatException Si la entrada no es un número entero.
     */
    public int solicitarIdPaciente(String accion) throws NumberFormatException {
        System.out.printf("\nIngrese el ID del Paciente que desea %s -> ", accion);
        return Integer.parseInt(scanner.nextLine().trim());
    }

    /**
     * Solicita al usuario un filtro de texto para la búsqueda.
     *
     * @return El <code>String</code> del filtro (ya "trimeado").
     */
    public String solicitarFiltroBusqueda() {
        System.out.print("\nIngrese el texto a buscar (por nombre o apellido) -> ");
        return scanner.nextLine().trim();
    }

    /**
     * Muestra un mensaje de confirmación genérico (Si/No).
     *
     * @param mensaje La pregunta a confirmar (ej: "¿Desea agregar una HC?").
     * @return <code>true</code> si el usuario responde "s" o "S",
     *         <code>false</code> en cualquier otro caso.
     */
    public boolean solicitarConfirmacion(String mensaje) {
        System.out.printf("%s (s/n) -> ", mensaje);
        return scanner.nextLine().trim().equalsIgnoreCase("s");
    }

    /**
     * Muestra un mensaje de error al usuario.
     * Imprime en <code>System.err</code>.
     *
     * @param mensaje El mensaje de error a mostrar.
     */
    public void mostrarError(String mensaje) {
        System.err.println("❌ ERROR: " + mensaje);
    }

    /**
     * Muestra un mensaje de éxito al usuario.
     * Imprime en <code>System.out</code>.
     *
     * @param mensaje El mensaje de éxito a mostrar.
     */
    public void mostrarExito(String mensaje) {
        System.out.println("✅ " + mensaje);
    }

}