package cr.ac.una.eif206.frontend.controlador;

import cr.ac.una.eif206.common.modelo.Usuario;
import cr.ac.una.eif206.frontend.proxy.ServiceProxy;
import cr.ac.una.eif206.frontend.vista.CambiarClaveView;
import cr.ac.una.eif206.frontend.vista.LoginView;
import cr.ac.una.eif206.frontend.vista.MainView;
import cr.ac.una.eif206.frontend.vista.ReservaView;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Controlador de la ventana principal: botones de sesion, cierre de la
 * ventana, canal de alertas, y "conecta" la vista + controlador de cada pestana.
 */
public class MainController {

    private final MainView vista;
    private final Usuario usuario;
    private final ServiceProxy proxy = ServiceProxy.getInstancia();

    public MainController(MainView vista, Usuario usuario) {
        this.vista = vista;
        this.usuario = usuario;
        registrarEventos();
        conectarPestanas();
        iniciarAlertas();
    }

    /**
     * Cada integrante crea aqui SU vista y SU controlador y los pone en su
     * pestana. Ejemplo (Jordi):
     *
     *   if (vista.tienePestana(MainView.TAB_RECURSOS)) {
     *       RecursoView recursoView = new RecursoView();
     *       new RecursoController(recursoView);
     *       vista.reemplazarPestana(MainView.TAB_RECURSOS, recursoView);
     *   }
     */
    private void conectarPestanas() {
        // ---------- ANDY: Reservas (Etapa 10) ----------
        // Solo existe para FUNCIONARIO (el administrador no tiene esta pestana)
        if (vista.tienePestana(MainView.TAB_RESERVAS)) {
            ReservaView reservaView = new ReservaView();
            new ReservaController(reservaView, usuario);
            vista.reemplazarPestana(MainView.TAB_RESERVAS, reservaView);
        }

        // ---------- BENJA: Funcionarios, Categorias ----------

        // ---------- JORDI: Recursos, Calendarizacion, Actividades, Estadisticas ----------

    }

    /**
     * Abre el canal de alertas (segunda conexion) y muestra cada alerta que llega.
     *
     * Por ahora es una LISTA SENCILLA TEMPORAL (solo el titulo), para probar
     * que las alertas llegan.
     *
     * BENJA: reemplaza las 3 lineas de abajo por algo asi:
     *     AlertasPanel panel = new AlertasPanel();
     *     AlertasController alertas = new AlertasController(panel);   // implementa AlertaListener
     *     vista.ponerEnAreaAlertas(panel);
     *     proxy.iniciarAlertas(usuario.getId(), alertas);
     */
    private void iniciarAlertas() {
        DefaultListModel<String> alertasTemporales = new DefaultListModel<>();
        vista.ponerEnAreaAlertas(new JScrollPane(new JList<>(alertasTemporales)));
        proxy.iniciarAlertas(usuario.getId(), notificacion -> alertasTemporales.addElement(notificacion.getTitulo()));
    }

    private void registrarEventos() {
        vista.getBtnCambiarClave().addActionListener(e -> {
            CambiarClaveView dialogo = new CambiarClaveView(vista);
            new CambiarClaveController(dialogo);
            dialogo.setVisible(true);
        });

        vista.getBtnCerrarSesion().addActionListener(e -> cerrarSesion());

        // La X de la ventana: confirmar, desconectarse y salir
        vista.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int opcion = JOptionPane.showConfirmDialog(vista, "¿Desea salir del sistema?",
                        "Salir", JOptionPane.YES_NO_OPTION);
                if (opcion == JOptionPane.YES_OPTION) {
                    proxy.desconectar();   // cierra tambien el canal de alertas
                    System.exit(0);
                }
            }
        });
    }

    /** Cierra la sesion en el servidor (la conexion sigue abierta) y vuelve al Login. */
    private void cerrarSesion() {
        proxy.detenerAlertas();   // ya no debe recibir alertas de esta sesion
        try {
            proxy.cerrarSesion();
        } catch (Exception ignored) {
            // si el servidor no responde, igual se vuelve al login
        }
        vista.dispose();
        LoginView loginView = new LoginView();
        new LoginController(loginView);
        loginView.setVisible(true);
    }

    public Usuario getUsuario() {
        return usuario;
    }
}