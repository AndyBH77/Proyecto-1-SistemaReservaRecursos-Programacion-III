package cr.ac.una.eif206.presentacion.controlador;

import cr.ac.una.eif206.modelo.Usuario;
import cr.ac.una.eif206.negocio.AutenticacionService;
import cr.ac.una.eif206.presentacion.vista.LoginView;
import cr.ac.una.eif206.presentacion.vista.MainView;

import javax.swing.JOptionPane;
import java.util.Optional;

/**
 * Controlador de la ventana de Login. Conecta los eventos de LoginView
 * con la logica de negocio de AutenticacionService.
 *
 * NOTA: ya no existe un registro publico de usuarios desde aqui. Ahora solo
 * el Administrador puede crear funcionarios, desde la pestaña "Funcionarios"
 * de la ventana principal (ver FuncionarioController).
 */
public class LoginController {

    private final LoginView vista;
    private final AutenticacionService autenticacionService = new AutenticacionService();

    public LoginController(LoginView vista) {
        this.vista = vista;
        registrarEventos();
    }

    private void registrarEventos() {
        vista.getBtnIngresar().addActionListener(e -> intentarIngresar());
        vista.getBtnSalir().addActionListener(e -> System.exit(0));
        vista.getBtnOlvideClave().addActionListener(e -> recuperarClave());
    }

    private void intentarIngresar() {
        String id = vista.getTxtId().getText().trim();
        String clave = new String(vista.getTxtClave().getPassword());

        if (id.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Debe indicar el id y la clave.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Optional<Usuario> usuario = autenticacionService.login(id, clave);

        if (usuario.isPresent()) {
            MainView mainView = new MainView(usuario.get());
            new MainController(mainView, usuario.get());
            mainView.setVisible(true);
            vista.dispose();
        } else {
            JOptionPane.showMessageDialog(vista, "El id o la clave no son correctos.",
                    "Error de ingreso", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recuperarClave() {
        String id = JOptionPane.showInputDialog(vista,
                "Indique su id de usuario. Se enviará una nueva clave a su correo registrado:",
                "Recuperar clave", JOptionPane.QUESTION_MESSAGE);

        if (id == null || id.isBlank()) {
            return;
        }

        try {
            autenticacionService.recuperarClave(id.trim());
            JOptionPane.showMessageDialog(vista, "Se envió una nueva clave a su correo registrado.",
                    "Clave enviada", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, ex.getMessage(),
                    "No se pudo recuperar la clave", JOptionPane.ERROR_MESSAGE);
        }
    }
}