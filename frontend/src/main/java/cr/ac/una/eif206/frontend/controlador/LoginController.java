package cr.ac.una.eif206.frontend.controlador;

import cr.ac.una.eif206.common.modelo.Usuario;
import cr.ac.una.eif206.frontend.proxy.ServiceProxy;
import cr.ac.una.eif206.frontend.util.Dialogos;
import cr.ac.una.eif206.frontend.vista.LoginView;
import cr.ac.una.eif206.frontend.vista.MainView;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Controlador de la ventana de Login.
 * Ya no usa AutenticacionService directamente: todo va por el ServiceProxy.
 */
public class LoginController {

    private final LoginView vista;
    private final ServiceProxy proxy = ServiceProxy.getInstancia();

    public LoginController(LoginView vista) {
        this.vista = vista;
        registrarEventos();
    }

    private void registrarEventos() {
        vista.getBtnIngresar().addActionListener(e -> intentarIngresar());
        vista.getTxtClave().addActionListener(e -> intentarIngresar());   // Enter en la clave
        vista.getBtnOlvideClave().addActionListener(e -> recuperarClave());
        vista.getBtnSalir().addActionListener(e -> {
            proxy.desconectar();
            System.exit(0);
        });
    }

    private void intentarIngresar() {
        String id = vista.getTxtId().getText().trim();
        String clave = new String(vista.getTxtClave().getPassword());

        if (id.isEmpty() || clave.isEmpty()) {
            Dialogos.advertencia(vista, "Datos incompletos", "Debe indicar el id y la clave.");
            return;
        }

        vista.getBtnIngresar().setEnabled(false);   // evita doble clic mientras responde el servidor

        // SwingWorker: la peticion se hace en otro hilo para no congelar la ventana
        new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() throws Exception {
                return proxy.login(id, clave);
            }

            @Override
            protected void done() {   // esto ya corre en el hilo de Swing
                vista.getBtnIngresar().setEnabled(true);
                try {
                    abrirVentanaPrincipal(get());
                } catch (Exception ex) {
                    Dialogos.error(vista, "Error de ingreso", ex);
                }
            }
        }.execute();
    }

    private void abrirVentanaPrincipal(Usuario usuario) {
        MainView mainView = new MainView(usuario);
        new MainController(mainView, usuario);
        mainView.setVisible(true);
        vista.dispose();
    }

    private void recuperarClave() {
        String id = JOptionPane.showInputDialog(vista,
                "Indique su id de usuario. Se enviara una nueva clave a su correo registrado:",
                "Recuperar clave", JOptionPane.QUESTION_MESSAGE);

        if (id == null || id.isBlank()) {
            return;
        }

        vista.getBtnOlvideClave().setEnabled(false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                proxy.recuperarClave(id.trim());   // enviar el correo tarda unos segundos
                return null;
            }

            @Override
            protected void done() {
                vista.getBtnOlvideClave().setEnabled(true);
                try {
                    get();
                    Dialogos.info(vista, "Clave enviada", "Se envio una nueva clave a su correo registrado.");
                } catch (Exception ex) {
                    Dialogos.error(vista, "No se pudo recuperar la clave", ex);
                }
            }
        }.execute();
    }
}