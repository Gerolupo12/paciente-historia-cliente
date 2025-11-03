package views;

/**
 * Clase utilitaria (Utility Class) para mostrar los menús de la aplicación.
 * <p>
 * Esta clase es parte de la capa de Vistas, pero es una vista "tonta".
 * Su <b>única responsabilidad</b> es imprimir texto formateado en la consola .
 * </p>
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 * <li>Mostrar el menú principal con todas las opciones disponibles.</li>
 * <li>No tiene estado (<code>static</code>) y no se puede instanciar
 * (constructor privado).</li>
 * <li><b>No</b> lee la entrada del usuario (esa es la responsabilidad de
 * {@link AppMenu} y las clases <code>...View</code>).</li>
 * </ul>
 *
 * @author alpha team
 * @see AppMenu
 * @see MenuHandler
 */
public final class DisplayMenu { // 'final' porque es una clase utilitaria

    /**
     * Constructor privado para prevenir la instanciación.
     * Esta es una clase utilitaria con solo métodos estáticos.
     */
    private DisplayMenu() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no debe ser instanciada");
    }

    /**
     * Muestra el menú principal con todas las opciones CRUD y de gestión.
     * <p>
     * Las opciones están numeradas para corresponder con el <code>switch</code>
     * en {@link MenuHandler#processOption(int)}.
     * </p>
     *
     * <h3>Opciones de Pacientes (1-4):</h3>
     * <ul>
     * <li>1: Listar Pacientes (todos, por filtro de texto, por DNI).</li>
     * <li>2: Crear Paciente (con HC opcional).</li>
     * <li>3: Actualizar Paciente (datos personales y asociación de HC).</li>
     * <li>4: Eliminar Paciente (Baja lógica en cascada a HC asociada)
     * (RN-013).</li>
     * </ul>
     *
     * <h3>Opciones de Historias Clínicas (5-10):</h3>
     * <ul>
     * <li>5: Listar HCs (todas, por filtro de texto, por ID).</li>
     * <li>6: Crear HC independiente (sin asociar a paciente).</li>
     * <li>7: Actualizar HC por ID (para HCs independientes).</li>
     * <li>8: Eliminar HC por ID (<b>PELIGROSO:</b> puede dejar referencias
     * huérfanas) (HU-007).</li>
     * <li>9: Gestionar HC por Paciente (Crear, Asignar, Actualizar) (HU-009).</li>
     * <li>10: Eliminar HC por Paciente (<b>SEGURO:</b> desasocia y elimina)
     * (HU-008).</li>
     * </ul>
     *
     * <h3>Opciones del Sistema (11, 0):</h3>
     * <ul>
     * <li>11: Submenú de recuperación (Baja Lógica).</li>
     * <li>0: Salir del sistema.</li>
     * </ul>
     */
    public static void showMainMenu() {
        System.out.println("\n========= MENU =========");
        System.out.println("--- Gestión de Pacientes ---");
        System.out.println("1. Listar pacientes");
        System.out.println("2. Crear paciente");
        System.out.println("3. Actualizar paciente");
        System.out.println("4. Eliminar paciente");
        System.out.println("--- Gestión de Historias Clínicas ---");
        System.out.println("5. Listar Historia Clínicas");
        System.out.println("6. Crear Historia Clínica");
        System.out.println("7. Actualizar Historia Clínica por ID");
        System.out.println("8. Eliminar Historia Clínica por ID (Peligroso)");
        System.out.println("9. Gestionar Historia Clínica por ID de paciente");
        System.out.println("10. Eliminar Historia Clínica por ID de paciente (Seguro)");
        System.out.println("--- Sistema ---");
        System.out.println("11. Submenú de recuperación de datos borrados");
        System.out.println("0. Salir");
        System.out.print("\nIngrese una opcion -> ");
    }

}
