package cr.ac.una.eif206.presentacion.controlador;

import cr.ac.una.eif206.modelo.Usuario;
import cr.ac.una.eif206.negocio.AutenticacionService;
import cr.ac.una.eif206.presentacion.vista.CambiarClaveView;

import javax.swing.JOptionPane;
import java.util.Arrays;

public class CambiarClaveController {

    private final CambiarClaveView vista;
    private final Usuario usuarioActual;
    private final AutenticacionService autenticacionService = new AutenticacionService();

    public CambiarClaveController(CambiarClaveView vista, Usuario usuarioActual) {
        this.vista = vista;
        this.usuarioActual = usuarioActual;
        registrarEventos();
    }

    private void registrarEventos() {
        vista.getBtnGuardar().addActionListener(e -> guardar());
        vista.getBtnCancelar().addActionListener(e -> vista.dispose());
    }

    private void guardar() {
        char[] actual = vista.getTxtClaveActual().getPassword();
        char[] nueva = vista.getTxtClaveNueva().getPassword();
        char[] confirmar = vista.getTxtConfirmarClave().getPassword();

        if (!Arrays.equals(nueva, confirmar)) {
            JOptionPane.showMessageDialog(vista, "La clave nueva y la confirmación no coinciden.",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            autenticacionService.cambiarClave(usuarioActual, new String(actual), new String(nueva));
            JOptionPane.showMessageDialog(vista, "Clave actualizada correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            vista.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, ex.getMessage(),
                    "No se pudo cambiar la clave", JOptionPane.ERROR_MESSAGE);
        }
    }
}
