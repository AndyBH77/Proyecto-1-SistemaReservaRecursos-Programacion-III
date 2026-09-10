package cr.ac.una.eif206.presentacion.controlador;

import cr.ac.una.eif206.modelo.Usuario;
import cr.ac.una.eif206.presentacion.vista.CambiarClaveView;
import cr.ac.una.eif206.presentacion.vista.LoginView;
import cr.ac.una.eif206.presentacion.vista.MainView;

/**
 * Controlador general de la ventana principal: maneja el boton de cambiar
 * clave y el de cerrar sesion. Ademas activa el controlador de la pestaña
 * de Reservas y, si el usuario es Administrador, el de la pestaña de
 * Funcionarios.
 */
public class MainController {

    private final MainView vista;
    private final Usuario usuarioActual;

    public MainController(MainView vista, Usuario usuarioActual) {
        this.vista = vista;
        this.usuarioActual = usuarioActual;
        registrarEventos();

        new ReservaController(vista.getReservaView(), usuarioActual);

        if (vista.getFuncionarioView() != null) {
            new FuncionarioController(vista.getFuncionarioView());
        }
    }

    private void registrarEventos() {
        vista.getBtnCambiarClave().addActionListener(e -> {
            CambiarClaveView cambiarClaveView = new CambiarClaveView(vista);
            new CambiarClaveController(cambiarClaveView, usuarioActual);
            cambiarClaveView.setVisible(true);
        });

        vista.getBtnCerrarSesion().addActionListener(e -> {
            vista.dispose();
            LoginView loginView = new LoginView();
            new LoginController(loginView);
            loginView.setVisible(true);
        });
    }
}