package main.java.views.historias;

import java.util.List;
import java.util.Scanner;
import main.java.models.GrupoSanguineo;
import main.java.models.HistoriaClinica;

/**
 * Clase de Vista (View) específica para la entidad HistoriaClinica.
 * <p>
 * Esta clase es una vista "tonta" (Dumb View). Su <b>única responsabilidad</b>
 * es interactuar con la consola (<code>System.in</code> y
 * <code>System.out</code>) para operaciones de Historia Clínica.
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li><b>Mostrar Datos:</b> Imprimir los detalles de una o
 * más <code>HistoriaClinica</code>s.</li>
 * <li><b>Capturar Datos:</b> Solicitar al usuario entradas (como
 * <code>nroHistoria</code>, <code>grupoSanguineo</code>, etc.).</li>
 * <li><b>Manejo de Enum:</b> Contiene la lógica para pedir al usuario
 * el grupo y factor (ej: "A", "+") y convertirlo al formato
 * del Enum (ej: "A_PLUS").</li>
 * <li><b>Mostrar Mensajes:</b> Imprimir mensajes de éxito o error.</li>
 * </ul>
 *
 * <h3>Arquitectura:</h3>
 * <p>
 * Esta clase es llamada <b>exclusivamente</b> por su controlador,
 * {@link HistoriaMenu}. <b>Nunca</b> llama a la capa de Servicio
 * ({@link service.HistoriaClinicaService}) directamente.
 * </p>
 *
 * @author alpha team
 * @see HistoriaMenu
 * @see main.java.models.HistoriaClinica
 */
public class HistoriaView {

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
    public HistoriaView(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("El Scanner no puede ser nulo.");
        }
        this.scanner = scanner;
    }

    /**
     * Proporciona acceso al Scanner inyectado.
     *
     * @return La instancia compartida del {@link Scanner}.
     */
    public Scanner getScanner() {
        return this.scanner;
    }

    /**
     * Imprime una lista de historias clínicas en la consola.
     * <p>
     * Si la lista está vacía, muestra un mensaje "No se encontraron...".
     * Si no, itera sobre la lista y llama a
     * {@link #mostrarHistoriaDetalle(HistoriaClinica)} para cada una.
     * </p>
     *
     * @param historias La {@link List} de {@link HistoriaClinica}s a mostrar.
     */
    public void mostrarHistorias(List<HistoriaClinica> historias) {
        if (historias == null || historias.isEmpty()) {
            System.out.println("No se encontraron historias clínicas que coincidan con los criterios.");
            return;
        }

        System.out.println("\n--- Listado de Historias Clínicas ---");
        for (HistoriaClinica hc : historias) {
            mostrarHistoriaDetalle(hc);
            System.out.println("------------------------------------");
        }
    }

    /**
     * Imprime los detalles de un único objeto HistoriaClinica.
     *
     * @param hc La {@link HistoriaClinica} a mostrar.
     */
    public void mostrarHistoriaDetalle(HistoriaClinica hc) {
        if (hc == null) {
            System.err.println("Error: La historia clínica a mostrar es nula.");
            return;
        }

        System.out.printf("ID: %d | Nro. Historia: %s\n", hc.getId(), hc.getNumeroHistoria());
        System.out.printf("Grupo Sanguíneo: %s\n", hc.getGrupoSanguineo());
        System.out.printf("Antecedentes: %s\n", hc.getAntecedentes() != null ? hc.getAntecedentes() : "N/A");
        System.out.printf("Medicación: %s\n", hc.getMedicacionActual() != null ? hc.getMedicacionActual() : "N/A");
        System.out.printf("Observaciones: %s\n", hc.getObservaciones() != null ? hc.getObservaciones() : "N/A");
        System.out.printf("Eliminado: %s\n", hc.isEliminado() ? "Si" : "No");
    }

    /**
     * Solicita al usuario los datos para crear una nueva HistoriaClinica.
     * <p>
     * <b>Importante:</b> Esta función <b>no</b> realiza validaciones de
     * Reglas de Negocio (como formato o unicidad de <code>nroHistoria</code>).
     * Su responsabilidad es capturar los <code>Strings</code> y
     * crear un objeto "crudo".
     * </p>
     * <p>
     * La validación (RN) es responsabilidad de
     * {@link service.HistoriaClinicaService}.
     * </p>
     *
     * @return Un nuevo objeto {@link HistoriaClinica} (con <code>id=0</code>)
     *         poblado con los datos ingresados.
     * @throws IllegalArgumentException Si el usuario ingresa un grupo
     *                                  sanguíneo inválido (falla
     *                                  <code>GrupoSanguineo.valueOf()</code>).
     */
    public HistoriaClinica solicitarDatosHistoria() throws IllegalArgumentException {
        System.out.println("\n--- Ingrese los datos de la Historia Clínica ---");

        // 1. Número de Historia (Validación de formato en el Service)
        System.out.print("Número de Historia (ej: HC-123456) -> ");
        String nroHistoria = scanner.nextLine().trim();

        // 2. Grupo Sanguíneo (Validación de formato aquí en la Vista)
        GrupoSanguineo grupoSanguineo = null;
        while (grupoSanguineo == null) {
            try {
                System.out.print("Grupo Sanguíneo (A, B, AB, O) -> ");
                String grupoInput = scanner.nextLine().trim().toUpperCase();
                System.out.print("Factor RH (+ o -) -> ");
                String factorInput = scanner.nextLine().trim();

                // Convertir (ej: "A", "+") a "A_PLUS"
                String enumName = grupoInput + "_" + (factorInput.equals("+") ? "PLUS" : "MINUS");

                // Validar que el Enum exista
                grupoSanguineo = GrupoSanguineo.valueOf(enumName);
            } catch (IllegalArgumentException e) {
                System.err.println("Error: Grupo sanguíneo o factor inválido. Intente de nuevo.");
            }
        }

        System.out.println("Grupo seleccionado: " + grupoSanguineo.toString());

        // 3. Campos de texto opcionales
        System.out.print("Antecedentes (opcional) -> ");
        String antecedentes = scanner.nextLine().trim();

        System.out.print("Medicación Actual (opcional) -> ");
        String medicacion = scanner.nextLine().trim();

        System.out.print("Observaciones (opcional) -> ");
        String observaciones = scanner.nextLine().trim();

        // Devuelve un objeto "crudo" (sin ID).
        // La capa de Servicio lo validará (formato nroHistoria, unicidad, etc.)
        return new HistoriaClinica(nroHistoria, grupoSanguineo, antecedentes, medicacion, observaciones);
    }

    /**
     * Solicita al usuario los datos para actualizar una HistoriaClinica.
     * <p>
     * Implementa el patrón "Enter para mantener":
     * Si el usuario presiona Enter (la entrada está vacía), el método
     * <b>no</b> actualiza ese campo en el objeto <code>hc</code>.
     * </p>
     *
     * @param hc El objeto {@link HistoriaClinica} a actualizar (con sus
     *           datos actuales).
     * @return El mismo objeto <code>HistoriaClinica</code>, modificado
     *         (mutado) con los nuevos datos.
     * @throws IllegalArgumentException Si el usuario ingresa un grupo
     *                                  sanguíneo inválido.
     */
    public HistoriaClinica solicitarDatosActualizacion(HistoriaClinica hc) throws IllegalArgumentException {
        System.out.println("\n--- Actualizar Historia Clínica (Presione Enter para mantener) ---");

        System.out.printf("Número de Historia [%s] -> ", hc.getNumeroHistoria());
        String nroHistoria = scanner.nextLine().trim();
        if (!nroHistoria.isEmpty()) {
            hc.setNumeroHistoria(nroHistoria);
        }

        // Grupo Sanguíneo (es más simple pedirlo de nuevo que mantenerlo)
        System.out.printf("Grupo Sanguíneo actual: [%s]. ¿Desea cambiarlo? (s/n) -> ", hc.getGrupoSanguineo());
        if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
            GrupoSanguineo grupoSanguineo = null;
            while (grupoSanguineo == null) {
                try {
                    System.out.print("Nuevo Grupo (A, B, AB, O) -> ");
                    String grupoInput = scanner.nextLine().trim().toUpperCase();
                    System.out.print("Nuevo Factor RH (+ o -) -> ");
                    String factorInput = scanner.nextLine().trim();

                    String enumName = grupoInput + "_" + (factorInput.equals("+") ? "PLUS" : "MINUS");
                    grupoSanguineo = GrupoSanguineo.valueOf(enumName);
                    hc.setGrupoSanguineo(grupoSanguineo); // Actualizar
                } catch (IllegalArgumentException e) {
                    System.err.println("Error: Grupo o factor inválido. Intente de nuevo.");
                }
            }
        }

        System.out.printf("Antecedentes [%s] -> ", hc.getAntecedentes());
        String antecedentes = scanner.nextLine().trim();
        if (!antecedentes.isEmpty()) {
            hc.setAntecedentes(antecedentes);
        }

        System.out.printf("Medicación [%s] -> ", hc.getMedicacionActual());
        String medicacion = scanner.nextLine().trim();
        if (!medicacion.isEmpty()) {
            hc.setMedicacionActual(medicacion);
        }

        System.out.printf("Observaciones [%s] -> ", hc.getObservaciones());
        String observaciones = scanner.nextLine().trim();
        if (!observaciones.isEmpty()) {
            hc.setObservaciones(observaciones);
        }

        return hc;
    }

    /**
     * Solicita al usuario un ID de Historia Clínica para una acción específica.
     *
     * @param accion El verbo de la acción (ej: "actualizar", "eliminar").
     * @return El <code>int</code> ID ingresado por el usuario.
     * @throws NumberFormatException Si la entrada no es un número entero.
     */
    public int solicitarIdHistoria(String accion) throws NumberFormatException {
        System.out.printf("\nIngrese el ID de la Historia Clínica que desea %s -> ", accion);
        return Integer.parseInt(scanner.nextLine().trim());
    }

    /**
     * Solicita al usuario un filtro de texto para la búsqueda de HC.
     *
     * @return El <code>String</code> del filtro (ya "trimeado").
     */
    public String solicitarFiltroBusqueda() {
        System.out.print("\nIngrese el texto a buscar (por Nro. Historia, antecedentes, grupo, etc.) -> ");
        return scanner.nextLine().trim();
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