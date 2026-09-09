package cr.ac.una.eif206.presentacion.controlador;

import cr.ac.una.eif206.modelo.Usuario;
import cr.ac.una.eif206.presentacion.vista.CambiarClaveView;
import cr.ac.una.eif206.presentacion.vista.LoginView;
import cr.ac.una.eif206.presentacion.vista.MainView;

/**
 * Controlador general de la ventana principal: maneja el boton de cambiar
 * clave y el de cerrar sesion. La pestaña de Reservas tiene su propio
 * controlador (ReservaController), que se activa automaticamente aqui.
 */
public class MainController {

    private final MainView vista;
    private final Usuario usuarioActual;

    public MainController(MainView vista, Usuario usuarioActual) {
        this.vista = vista;
        this.usuarioActual = usuarioActual;
        registrarEventos();

        // Se activa el controlador de la pestaña de Reservas
        new ReservaController(vista.getReservaView(), usuarioActual);
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
