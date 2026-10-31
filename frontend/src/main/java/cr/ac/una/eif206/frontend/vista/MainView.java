package cr.ac.una.eif206.frontend.vista;

import cr.ac.una.eif206.common.modelo.Funcionario;
import cr.ac.una.eif206.common.modelo.RolUsuario;
import cr.ac.una.eif206.common.modelo.Usuario;
import cr.ac.una.eif206.frontend.util.Constantes;
import cr.ac.una.eif206.frontend.util.Estilos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Ventana principal del Proyecto 2:
 *  - Arriba: barra con el usuario y los botones de sesion.
 *  - Centro: pestanas segun el rol del usuario.
 *  - Derecha: AREA DE ALERTAS, independiente de las pestanas y siempre visible.
 *
 * Las pestanas empiezan con un PanelEnConstruccion; cada integrante pone su
 * vista real con reemplazarPestana(...) desde MainController.
 */
public class MainView extends JFrame {

    // Titulos de las pestanas (se usan para reemplazarlas despues)
    public static final String TAB_RESERVAS = "Reservas";
    public static final String TAB_CALENDARIZACION = "Calendarización";
    public static final String TAB_ACTIVIDADES = "Actividades";
    public static final String TAB_ESTADISTICAS = "Estadísticas";
    public static final String TAB_FUNCIONARIOS = "Funcionarios";
    public static final String TAB_CATEGORIAS = "Categorías";
    public static final String TAB_RECURSOS = "Recursos";

    private final JTabbedPane pestanas = new JTabbedPane();
    private final JPanel areaAlertas = new JPanel(new BorderLayout());
    private final JButton btnCambiarClave = new JButton("Cambiar clave");
    private final JButton btnCerrarSesion = new JButton("Cerrar sesión");

    public MainView(Usuario usuario) {
        super("SISTEMA DE RESERVAS - " + usuario.getId() + " (" + usuario.getRol() + ")");

        // El cierre con la X lo maneja MainController (para desconectarse del servidor)
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(Constantes.TAMANO_VENTANA_PRINCIPAL);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        crearPestanas(usuario);
        crearAreaAlertas();
        JPanel barraSuperior = crearBarraSuperior(usuario);

        add(barraSuperior, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
        add(areaAlertas, BorderLayout.EAST);

        Estilos.aplicarEstiloGeneral(this, pestanas);
        restaurarColoresBarra(barraSuperior);
    }

    /** Pestanas segun el rol, como en el Proyecto 1. */
    private void crearPestanas(Usuario usuario) {
        if (usuario.getRol() == RolUsuario.FUNCIONARIO) {
            pestanas.addTab(TAB_RESERVAS, new PanelEnConstruccion(TAB_RESERVAS));            // ANDY
        }
        pestanas.addTab(TAB_CALENDARIZACION, new PanelEnConstruccion(TAB_CALENDARIZACION));  // JORDI
        pestanas.addTab(TAB_ACTIVIDADES, new PanelEnConstruccion(TAB_ACTIVIDADES));          // JORDI
        pestanas.addTab(TAB_ESTADISTICAS, new PanelEnConstruccion(TAB_ESTADISTICAS));        // JORDI

        if (usuario.getRol() == RolUsuario.ADMINISTRADOR) {
            pestanas.addTab(TAB_FUNCIONARIOS, new PanelEnConstruccion(TAB_FUNCIONARIOS));    // BENJA
            pestanas.addTab(TAB_CATEGORIAS, new PanelEnConstruccion(TAB_CATEGORIAS));        // BENJA
            pestanas.addTab(TAB_RECURSOS, new PanelEnConstruccion(TAB_RECURSOS));            // JORDI
        }
    }

    /** Area de alertas: Benja la reemplaza por su AlertasPanel (Etapa 11). */
    private void crearAreaAlertas() {
        areaAlertas.setPreferredSize(new Dimension(Constantes.ANCHO_AREA_ALERTAS, 0));
        areaAlertas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 0, 8, 8),
                BorderFactory.createTitledBorder("Alertas")));
        areaAlertas.add(new JLabel("(sin alertas)", SwingConstants.CENTER), BorderLayout.CENTER);
    }

    private JPanel crearBarraSuperior(Usuario usuario) {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Como en la pantalla del enunciado: "Funcionario - Maria Mata"
        String quien = (usuario instanceof Funcionario f)
                ? "Funcionario - " + f.getNombre()
                : "Administrador - " + usuario.getId();
        JLabel lblTitulo = new JLabel("SISTEMA DE RESERVAS   |   " + quien);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel panelSesion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelSesion.add(btnCambiarClave);
        panelSesion.add(btnCerrarSesion);

        barra.add(lblTitulo, BorderLayout.WEST);
        barra.add(panelSesion, BorderLayout.EAST);
        return barra;
    }

    /**
     * Estilos.aplicarEstiloGeneral pinta todos los paneles; aqui se vuelven
     * a poner los colores de la barra superior (igual que en el Proyecto 1).
     */
    private void restaurarColoresBarra(JPanel barra) {
        barra.setBackground(Estilos.VERDE_OSCURO);
        for (java.awt.Component c : barra.getComponents()) {
            if (c instanceof JLabel etiqueta) {
                etiqueta.setForeground(Color.WHITE);
            } else if (c instanceof JPanel panel) {
                panel.setOpaque(false);
            }
        }
        btnCambiarClave.setBackground(Color.WHITE);
        btnCambiarClave.setForeground(Estilos.VERDE_OSCURO);
        btnCerrarSesion.setBackground(Color.WHITE);
        btnCerrarSesion.setForeground(Estilos.VERDE_OSCURO);
    }

    /** Cambia el contenido de una pestana (si existe para este rol). */
    public void reemplazarPestana(String titulo, JComponent vistaReal) {
        int indice = pestanas.indexOfTab(titulo);
        if (indice >= 0) {
            pestanas.setComponentAt(indice, vistaReal);
            Estilos.aplicarEstiloGeneral(vistaReal, pestanas);
        }
    }

    /** Cambia el contenido del area de alertas (lo usara Benja). */
    public void ponerEnAreaAlertas(JComponent contenido) {
        areaAlertas.removeAll();
        areaAlertas.add(contenido, BorderLayout.CENTER);
        areaAlertas.revalidate();
        areaAlertas.repaint();
    }

    public boolean tienePestana(String titulo) {
        return pestanas.indexOfTab(titulo) >= 0;
    }

    public JButton getBtnCambiarClave() {
        return btnCambiarClave;
    }

    public JButton getBtnCerrarSesion() {
        return btnCerrarSesion;
    }
}