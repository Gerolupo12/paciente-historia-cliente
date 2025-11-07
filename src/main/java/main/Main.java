package main;

import main.java.config.DatabaseConnection;
import main.java.views.AppMenu;

public class Main {

    public static void main(String[] args) {

        DatabaseConnection.setDebug(false); // modo producción

        AppMenu app = new AppMenu();
        app.run();
    }

}
