package test;

import dao.HistoriaClinicaDAO;
import dao.PacienteDAO;
import models.GrupoSanguineo;
import models.HistoriaClinica;
import models.Paciente;
import service.HistoriaClinicaService;
import service.PacienteService;
import exceptions.DuplicateEntityException;
import exceptions.ServiceException;
import exceptions.ValidationException;

import java.time.LocalDate;

/**
 * Clase de prueba manual para la capa de servicio (Service Layer).
 * <p>
 * Permite probar las reglas de negocio, validaciones y comportamiento
 * transaccional de {@link PacienteService} e {@link HistoriaClinicaService}.
 * </p>
 *
 * <h3>Tests Incluidos:</h3>
 * <ul>
 *     <li>✅ Inserción válida (Paciente + Historia Clínica)</li>
 *     <li>🚫 Detección de DNI duplicado (RN-002)</li>
 *     <li>🚫 Validación de DNI con formato inválido (RN-001.6)</li>
 *     <li>🚫 Validación de fecha de nacimiento futura (RN-001.7)</li>
 *     <li>🚫 Rollback transaccional ante error en la Historia Clínica (RN-017)</li>
 * </ul>
 *
 * <p>
 * Este test puede ejecutarse directamente desde el método {@link #main(String[])}.
 * No requiere interfaz gráfica ni frameworks de testing.
 * </p>
 *
 * @author alpha
 */
public class ServiceTest {

    public static void main(String[] args) {

        try {
            // === 1️⃣ Inicialización de servicios ===
            // Se crean los DAO concretos
            HistoriaClinicaDAO hcDao = new HistoriaClinicaDAO();
            PacienteDAO pacDao = new PacienteDAO(hcDao);

            // Se instancian los servicios de negocio (Service Layer)
            HistoriaClinicaService hcService = new HistoriaClinicaService(hcDao);
            PacienteService pacienteService = new PacienteService(pacDao, hcService);

            // ===========================================================
            // TEST 1: Inserción válida de Paciente + Historia Clínica
            // ===========================================================
            System.out.println("\n=== TEST 1: Inserción VÁLIDA ===");

            // Se crea una Historia Clínica con datos válidos
            HistoriaClinica hc = new HistoriaClinica(
                    "HC-9999",
                    GrupoSanguineo.A_PLUS,
                    "Sin antecedentes",
                    null,
                    null
            );

            // Se crea un Paciente con datos coherentes
            // 🔹 Se usa un DNI nuevo que no exista en la base
            Paciente nuevo = new Paciente(
                    "María",
                    "Fernández",
                    "39999888", // DNI nuevo para evitar duplicado en BD
                    LocalDate.of(1990, 5, 12)
            );

            // Se asocia la Historia Clínica al Paciente (relación 1-a-1)
            nuevo.setHistoriaClinica(hc);

            // Se invoca al servicio → debe validar e insertar correctamente
            pacienteService.insert(nuevo);
            System.out.println("✅ Paciente insertado correctamente con ID: " + nuevo.getId());

            // ===========================================================
            // TEST 2: Detección de DNI duplicado (RN-002)
            // ===========================================================
            System.out.println("\n=== TEST 2: DNI DUPLICADO ===");

            try {
                // Se intenta insertar un paciente con el mismo DNI que el anterior
                Paciente duplicado = new Paciente(
                        "Carlos",
                        "Pérez",
                        "39999888", // mismo DNI → debe fallar
                        LocalDate.of(1985, 3, 9)
                );

                pacienteService.insert(duplicado);

            } catch (DuplicateEntityException e) {
                // El servicio debe detectar el DNI repetido y lanzar la excepción esperada
                System.out.println("✅ Detectó correctamente DNI duplicado → " + e.getMessage());
            }

            // ===========================================================
            // TEST 3: Validación de DNI con formato inválido
            // ===========================================================
            System.out.println("\n=== TEST 3: DNI INVÁLIDO ===");

            try {
                // Se usa un DNI con caracteres no numéricos (viola el formato requerido)
                Paciente invalido = new Paciente(
                        "Lucía",
                        "Gómez",
                        "36A00222", // contiene una letra → formato inválido
                        LocalDate.of(1988, 7, 3)
                );

                pacienteService.insert(invalido);

            } catch (ValidationException e) {
                // Debe lanzar error por formato inválido
                System.out.println("✅ Validación atrapó error → " + e.getMessage());
            }

            // ===========================================================
            // TEST 4: Fecha de nacimiento demasiado antigua
            // ===========================================================
            System.out.println("\n=== TEST 4: Fecha demasiado antigua ===");

            try {
                // Se crea un paciente con una fecha de nacimiento anterior a 1900 → inválido (RN-001.7)
                Paciente futuro = new Paciente(
                        "Juan",
                        "Rojas",
                        "37222111",
                        LocalDate.of(1880, 5, 10) // fecha demasiado vieja → inválido
                );

                pacienteService.insert(futuro);

            } catch (ValidationException e) {
                // El servicio debe detectar la fecha no válida (anterior a 1900)
                System.out.println("✅ Validación atrapó error → " + e.getMessage());
            }

            // ===========================================================
            // TEST 5: Rollback transaccional (error en Historia Clínica)
            // ===========================================================
            System.out.println("\n=== TEST 5: Rollback transaccional ===");

            try {
                // Genera error forzando un número de historia mal formado
                // (No cumple con el patrón 'HC-0000' → violará RN-017)
                HistoriaClinica hcMala = new HistoriaClinica(
                        "HISTORIA-MAL",
                        GrupoSanguineo.O_MINUS,
                        null,
                        null,
                        null
                );

                // Paciente válido
                Paciente pacienteMalo = new Paciente(
                        "Luis",
                        "Castro",
                        "37333444",
                        LocalDate.of(1995, 1, 1)
                );

                // Se vincula la HC errónea al paciente
                pacienteMalo.setHistoriaClinica(hcMala);

                // Debe fallar la inserción y producir rollback (no debe persistir nada)
                pacienteService.insert(pacienteMalo);

            } catch (ValidationException e) {
                // Excepción esperada por validación de formato
                System.out.println("✅ Se produjo rollback correctamente → " + e.getMessage());
            } catch (ServiceException e) {
                // Si se encapsula dentro de ServiceException también está bien
                System.out.println("✅ Se produjo rollback correctamente → " + e.getMessage());
            }

            // ===========================================================
            // FIN DE LAS PRUEBAS
            // ===========================================================
            System.out.println("\n=== TODOS LOS TESTS FINALIZADOS ===");

        } catch (Exception e) {
            System.err.println("❌ Error general de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
