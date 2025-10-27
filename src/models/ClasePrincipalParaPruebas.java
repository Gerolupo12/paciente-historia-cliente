package models;

import java.time.LocalDate;

/**
 * Clase principal para probar la creación de objetos y las relaciones
 * entre las clases del dominio Paciente-HistoriaClinica.
 * 
 * @author alpha team
 */
public class ClasePrincipalParaPruebas {

        public static void main(String[] args) {

                // 1. Crear un Profesional de prueba
                // Este profesional será asignado a las historias clínicas.
                Profesional prof1 = new Profesional(
                                101, // id
                                "Ana", // nombre
                                "Martinez", // apellido
                                "25123456", // dni
                                LocalDate.of(1978, 8, 15), // fechaNacimiento
                                "MP-12345", // matricula
                                "Cardiología" // especialidad
                );

                // 2. Crear las Historias Clínicas, ahora incluyendo al Profesional
                HistoriaClinica h1 = new HistoriaClinica(
                                1, // id
                                "HC-001", // numeroHistoria
                                GrupoSanguineo.A_PLUS, // grupoSanguineo
                                "Alergia a la penicilina", // antecedentes
                                "Ninguna", // medicacionActual
                                "Paciente sano", // observaciones
                                prof1 // profesional asignado
                );

                HistoriaClinica h2 = new HistoriaClinica(
                                2, // id
                                "HC-002", // numeroHistoria
                                GrupoSanguineo.O_PLUS, // grupoSanguineo
                                "Hipertensión arterial", // antecedentes
                                "Losartan 50mg", // medicacionActual
                                "Control mensual", // observaciones
                                prof1 // profesional asignado
                );

                // 3. Crear los Pacientes y asociarles su Historia Clínica
                // Paciente 1: Creado usando el constructor completo que incluye la historia.
                Paciente p1 = new Paciente(
                                1, // id
                                "Juan Carlos", // nombre
                                "Pérez", // apellido
                                "12345678", // dni
                                LocalDate.of(1985, 5, 20), // fechaNacimiento
                                h1 // historiaClinica
                );

                // Paciente 2: Creado primero y luego se le asigna la historia con el setter.
                Paciente p2 = new Paciente(
                                2, // id
                                "María Elena", // nombre
                                "Gómez", // apellido
                                "87654321", // dni
                                LocalDate.of(1990, 2, 10) // fechaNacimiento
                );
                p2.setHistoriaClinica(h2); // Asignación posterior

                // 4. Imprimir resultados para verificar que todo funciona correctamente
                System.out.println("--- PRUEBAS DE OBJETOS Y RELACIONES ---");
                System.out.println("PACIENTE 1 CREADO:");
                System.out.println(p1.toString()); // Debería mostrar los datos del paciente y su historia

                System.out.println("\nPACIENTE 2 CREADO:");
                System.out.println(p2.toString());

                System.out.println("\n--- PRUEBAS DE MÉTODOS ESPECÍFICOS ---");
                System.out.println("Profesional de la Historia Clínica de " + p1.getNombre() + ": "
                                + p1.getHistoriaClinica().getProfesional().getNombre());

                System.out.println("\n--- PRUEBAS DEL ENUM GRUPO SANGUÍNEO ---");
                System.out.println("¿Puede O- donar a AB+? -> "
                                + GrupoSanguineo.O_MINUS.puedeDonarA(GrupoSanguineo.AB_PLUS));
                System.out.println("¿Puede A+ donar a O-? -> "
                                + GrupoSanguineo.A_PLUS.puedeDonarA(GrupoSanguineo.O_MINUS));
                System.out.println("Representación del grupo sanguíneo O+: " + GrupoSanguineo.O_PLUS.toString());
        }
}