package cr.ac.una.eif206.frontend.controlador;

import cr.ac.una.eif206.common.modelo.Usuario;
import cr.ac.una.eif206.frontend.proxy.ServiceProxy;
import cr.ac.una.eif206.frontend.vista.ActividadesView;
import cr.ac.una.eif206.frontend.vista.CalendarizacionView;
import cr.ac.una.eif206.frontend.vista.CambiarClaveView;
import cr.ac.una.eif206.frontend.vista.CategoriaRecursoView;
import cr.ac.una.eif206.frontend.vista.EstadisticasView;
import cr.ac.una.eif206.frontend.vista.FuncionarioView;
import cr.ac.una.eif206.frontend.vista.LoginView;
import cr.ac.una.eif206.frontend.vista.MainView;
import cr.ac.una.eif206.frontend.vista.RecursoView;
import cr.ac.una.eif206.frontend.vista.ReservaView;
import cr.ac.una.eif206.frontend.vista.alertas.AlertasPanel;

import javax.swing.JOptionPane;
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

        // ---------- Funcionarios y Categorias (solo ADMINISTRADOR) ----------
        if (vista.tienePestana(MainView.TAB_FUNCIONARIOS)) {
            FuncionarioView funcionarioView = new FuncionarioView();
            new FuncionarioController(funcionarioView);
            vista.reemplazarPestana(MainView.TAB_FUNCIONARIOS, funcionarioView);
        }
        if (vista.tienePestana(MainView.TAB_CATEGORIAS)) {
            CategoriaRecursoView categoriaView = new CategoriaRecursoView();
            new CategoriaRecursoController(categoriaView);
            vista.reemplazarPestana(MainView.TAB_CATEGORIAS, categoriaView);
        }

        // ---------- JORDI: Recursos, Calendarizacion, Actividades, Estadisticas ----------
        // Recursos: solo existe para ADMINISTRADOR
        if (vista.tienePestana(MainView.TAB_RECURSOS)) {
            RecursoView recursoView = new RecursoView();
            new RecursoController(recursoView);
            vista.reemplazarPestana(MainView.TAB_RECURSOS, recursoView);
        }
        // Calendarizacion, Actividades y Estadisticas: existen para los dos roles
        if (vista.tienePestana(MainView.TAB_CALENDARIZACION)) {
            CalendarizacionView calendarizacionView = new CalendarizacionView();
            new CalendarizacionController(calendarizacionView);
            vista.reemplazarPestana(MainView.TAB_CALENDARIZACION, calendarizacionView);
        }
        if (vista.tienePestana(MainView.TAB_ACTIVIDADES)) {
            ActividadesView actividadesView = new ActividadesView();
            new ActividadesController(actividadesView);
            vista.reemplazarPestana(MainView.TAB_ACTIVIDADES, actividadesView);
        }
        if (vista.tienePestana(MainView.TAB_ESTADISTICAS)) {
            EstadisticasView estadisticasView = new EstadisticasView();
            new EstadisticasController(estadisticasView);
            vista.reemplazarPestana(MainView.TAB_ESTADISTICAS, estadisticasView);
        }

    }

    /**
     * Abre el canal de alertas (segunda conexion) y muestra las alertas en el
     * area de la derecha (AlertasPanel): Actividad | Ver | Desc.
     * Cada alerta que llega la recibe AlertasController (implementa AlertaListener).
     */
    private void iniciarAlertas() {
        AlertasPanel panelAlertas = new AlertasPanel();
        AlertasController alertasController = new AlertasController(panelAlertas);
        vista.ponerEnAreaAlertas(panelAlertas);
        proxy.iniciarAlertas(usuario.getId(), alertasController);
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