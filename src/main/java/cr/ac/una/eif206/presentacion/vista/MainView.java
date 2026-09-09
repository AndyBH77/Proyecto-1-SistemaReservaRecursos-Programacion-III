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
 * funcionalidad (similar al ejemplo de pantallas del enunciado). El
 * contenido real de "Reservas" lo arma esta persona; el resto de pestañas
 * son placeholders que reemplazaran los demas integrantes del equipo.
 */
public class MainView extends JFrame {

    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final ReservaView reservaView;
    private final JButton btnCambiarClave = new JButton("Cambiar clave");
    private final JButton btnCerrarSesion = new JButton("Cerrar sesión");

    public MainView(Usuario usuarioActual) {
        super("SISTEMA DE RESERVAS - " + usuarioActual.getId() + " (" + usuarioActual.getRol() + ")");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Constantes.TAMANO_VENTANA_PRINCIPAL);
        setResizable(false); // Requerimiento: tamaño fijo
        setLocationRelativeTo(null);

        reservaView = new ReservaView();

        // Pestañas visibles para cualquier rol
        tabbedPane.addTab("Reservas", reservaView);
        tabbedPane.addTab("Calendarización", new PanelEnConstruccion("Calendarización de recursos"));
        tabbedPane.addTab("Actividades", new PanelEnConstruccion("Programación de actividades"));
        tabbedPane.addTab("Estadísticas", new PanelEnConstruccion("Estadísticas"));

        // Pestañas exclusivas del administrador
        if (usuarioActual.getRol() == RolUsuario.ADMINISTRADOR) {
            tabbedPane.addTab("Funcionarios", new PanelEnConstruccion("Lista de funcionarios"));
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

    public JButton getBtnCambiarClave() {
        return btnCambiarClave;
    }

    public JButton getBtnCerrarSesion() {
        return btnCerrarSesion;
    }
}
