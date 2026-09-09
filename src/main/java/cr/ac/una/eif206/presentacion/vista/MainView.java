package cr.ac.una.eif206.presentacion.vista;

import cr.ac.una.eif206.modelo.RolUsuario;
import cr.ac.una.eif206.modelo.Usuario;
import cr.ac.una.eif206.util.Constantes;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

/**
 * Ventana principal del sistema, con pestañas de navegacion hacia cada
 * funcionalidad (similar al ejemplo de pantallas del enunciado).
 */
public class MainView extends JFrame {

    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final ReservaView reservaView;

    // Solo se crea si el usuario logueado es Administrador (ver mas abajo).
    // Queda null para un Funcionario normal, por eso el getter avisa que
    // puede devolver null.
    private FuncionarioView funcionarioView;

    private final JButton btnCambiarClave = new JButton("Cambiar clave");
    private final JButton btnCerrarSesion = new JButton("Cerrar sesión");

    public MainView(Usuario usuarioActual) {
        super("SISTEMA DE RESERVAS - " + usuarioActual.getId() + " (" + usuarioActual.getRol() + ")");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Constantes.TAMANO_VENTANA_PRINCIPAL);
        setResizable(false);
        setLocationRelativeTo(null);

        reservaView = new ReservaView();

        tabbedPane.addTab("Reservas", reservaView);
        tabbedPane.addTab("Calendarización", new PanelEnConstruccion("Calendarización de recursos"));
        tabbedPane.addTab("Actividades", new PanelEnConstruccion("Programación de actividades"));
        tabbedPane.addTab("Estadísticas", new PanelEnConstruccion("Estadísticas"));

        if (usuarioActual.getRol() == RolUsuario.ADMINISTRADOR) {
            funcionarioView = new FuncionarioView();
            tabbedPane.addTab("Funcionarios", funcionarioView);
            tabbedPane.addTab("Categorías", new PanelEnConstruccion("Lista de categorías de recurso"));
            tabbedPane.addTab("Recursos", new PanelEnConstruccion("Lista de recursos"));
        }

        JPanel barraSuperior = new JPanel();
        barraSuperior.add(btnCambiarClave);
        barraSuperior.add(btnCerrarSesion);

        add(barraSuperior, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    public ReservaView getReservaView() {
        return reservaView;
    }

    public FuncionarioView getFuncionarioView() {
        return funcionarioView;
    }

    public JButton getBtnCambiarClave() {
        return btnCambiarClave;
    }

    public JButton getBtnCerrarSesion() {
        return btnCerrarSesion;
    }
}