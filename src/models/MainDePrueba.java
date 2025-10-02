package models;

import java.time.LocalDate;

/**
 * Main para prueba de Métodos
 * 
 * @author alpha team
 */
public class MainDePrueba {

    public static void main(String[] args) {
        Paciente p1 = new Paciente(1, "Juan Carlos", "Pérez", "12345678", LocalDate.now());
        Paciente p2 = new Paciente(2, "María Elena", "Gómez", "87654321", LocalDate.now());
        Paciente p3 = new Paciente(3, "Carlos Alberto", "López", "11223344", LocalDate.now());

        HistoriaClinica h1 = new HistoriaClinica(1, "HC-001", GrupoSanguineo.A_PLUS, "Ninguno", "Ninguna",
                "Paciente sano");
        HistoriaClinica h2 = new HistoriaClinica(2, "HC-002", GrupoSanguineo.O_PLUS, "Hipertensión arterial",
                "Losartan 50mg", "Control mensual");
        HistoriaClinica h3 = new HistoriaClinica(3, "HC-003", GrupoSanguineo.B_MINUS, "Diabetes tipo 2",
                "Metformina 850mg", "Control trimestral");

        p1.setHistoriaClinica(h1);
        p2.setHistoriaClinica(h2);
        p3.setHistoriaClinica(h3);

        System.out.println(p1.getHistoriaClinica());
        System.out.println(p2);
        System.out.println(GrupoSanguineo.AB_MINUS.puedeDonarA(GrupoSanguineo.B_PLUS));
        System.out.println(GrupoSanguineo.O_PLUS);
    }

}
