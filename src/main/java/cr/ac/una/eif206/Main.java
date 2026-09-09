package cr.ac.una.eif206;

import cr.ac.una.eif206.presentacion.controlador.LoginController;
import cr.ac.una.eif206.presentacion.vista.LoginView;

import javax.swing.SwingUtilities;


 //Punto de entrada del programa.

public class Main {

    public static void main(String[] args) {
        // Se usa invokeLater para que la interfaz grafica se cree en el
        // "Event Dispatch Thread" de Swing.
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            new LoginController(loginView);
            loginView.setVisible(true);
        });
    }
}
