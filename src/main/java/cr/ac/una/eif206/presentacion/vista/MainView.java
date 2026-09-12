package cr.ac.una.eif206.presentacion.vista;

import cr.ac.una.eif206.modelo.RolUsuario;
import cr.ac.una.eif206.modelo.Usuario;
import cr.ac.una.eif206.util.Constantes;
import cr.ac.una.eif206.util.Estilos;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Font;
import java.awt.FlowLayout;

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
        super("SISTEMA DE RESERVAS" );

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

        JPanel barraSuperior = new JPanel(new BorderLayout()); //Barra superior personalizada.
        barraSuperior.setBackground(Estilos.VERDE_OSCURO);
        barraSuperior.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel lblTitulo = new JLabel("SISTEMA DE RESERVAS - " + usuarioActual.getId() + " (" + usuarioActual.getRol() + ")"); //Título colocado a la izquierda.
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel panelSesion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Panel para los botones de sesión.
        panelSesion.setOpaque(false);
        panelSesion.add(btnCambiarClave);
        panelSesion.add(btnCerrarSesion);
        barraSuperior.add(lblTitulo, BorderLayout.WEST);
        barraSuperior.add(panelSesion, BorderLayout.EAST);
        add(barraSuperior, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        Estilos.aplicarEstiloGeneral(this, tabbedPane); //Se aplica el estilo después de crear y agregar todos los componentes.
        barraSuperior.setBackground(Estilos.VERDE_OSCURO); //Se vuelven a colocar los colores de la barra porque la personalización recursiva modifica paneles.
        panelSesion.setOpaque(false);
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