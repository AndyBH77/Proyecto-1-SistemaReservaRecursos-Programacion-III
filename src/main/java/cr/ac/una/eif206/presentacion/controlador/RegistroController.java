package cr.ac.una.eif206.presentacion.controlador;

import cr.ac.una.eif206.negocio.AutenticacionService;
import cr.ac.una.eif206.presentacion.vista.RegistroView;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class RegistroController {

    private final RegistroView vista;
    private final AutenticacionService autenticacionService;

    public RegistroController(RegistroView vista, AutenticacionService autenticacionService) {
        this.vista = vista;
        this.autenticacionService = autenticacionService;
        registrarEventos();
    }

    private void registrarEventos() {
        vista.getBtnRegistrar().addActionListener(e -> registrar());
        vista.getBtnCancelar().addActionListener(e -> vista.dispose());
    }

    private void registrar() {
        String id = vista.getTxtId().getText().trim();
        String nombre = vista.getTxtNombre().getText().trim();
        String telefono = vista.getTxtTelefono().getText().trim();
        String correo = vista.getTxtCorreo().getText().trim();

        // Se deshabilita el boton mientras se envia el correo para evitar doble clic
        vista.getBtnRegistrar().setEnabled(false);
        vista.getBtnRegistrar().setText("Enviando...");

        // El envio de correo puede tardar unos segundos; se hace en un hilo
        // aparte (SwingWorker) para que la ventana no se quede "congelada".
        SwingWorker<Void, Void> tarea = new SwingWorker<>() {
            private Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    autenticacionService.registrarNuevoFuncionario(id, nombre, telefono, correo);
                } catch (Exception e) {
                    error = e;
                }
                return null;
            }

            @Override
            protected void done() {
                vista.getBtnRegistrar().setEnabled(true);
                vista.getBtnRegistrar().setText("Registrar y enviar clave");

                if (error != null) {
                    JOptionPane.showMessageDialog(vista, error.getMessage(),
                            "No se pudo registrar", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(vista,
                            "Usuario registrado correctamente.\nSe envió la clave al correo indicado.",
                            "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
                    vista.dispose();
                }
            }
        };

        tarea.execute();
    }
}
