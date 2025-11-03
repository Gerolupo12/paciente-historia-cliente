package main;

import config.DatabaseConnection;
import views.AppMenu;

public class Main {

    public static void main(String[] args) {

        DatabaseConnection.setDebug(false); // modo producción

        AppMenu app = new AppMenu();
        app.run();
    }

}
