package cr.ac.una.eif206;

import cr.ac.una.eif206.presentacion.controlador.LoginController;
import cr.ac.una.eif206.presentacion.vista.LoginView;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {

        // Intenta utilizar la apariencia visual del sistema operativo.

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.out.println("No se pudo cargar la apariencia del sistema.");
        }
        //SwingUtilities.invokeLater hace que toda la interfaz gráfica se ejecute en el hilo recomendado por Java Swing.

        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            new LoginController(loginView);
            loginView.setVisible(true);
        });
    }
}