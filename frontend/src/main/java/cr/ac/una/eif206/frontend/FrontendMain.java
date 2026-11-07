package cr.ac.una.eif206.frontend;

import cr.ac.una.eif206.frontend.controlador.LoginController;
import cr.ac.una.eif206.frontend.proxy.ServiceProxy;
import cr.ac.una.eif206.frontend.vista.LoginView;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada del FRONTEND.
 * Primero debe estar corriendo el backend (ServidorMain).
 * Se pueden abrir varios frontends a la vez, todos conectados al mismo backend.
 */
public class FrontendMain {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.out.println("No se pudo cargar la apariencia del sistema.");
        }

        SwingUtilities.invokeLater(() -> {
            ServiceProxy proxy = ServiceProxy.getInstancia();

            // Segun el enunciado: al iniciar, el frontend establece la conexion con el backend
            try {
                proxy.conectar();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo conectar con el servidor en " + proxy.getHost() + ":" + proxy.getPuerto()
                                + ".\nVerifique que el backend (ServidorMain) este corriendo.",
                        "Sin conexion", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }

            LoginView loginView = new LoginView();
            new LoginController(loginView);
            loginView.setVisible(true);
        });
    }
}