package cr.ac.una.eif206.frontend.controlador;

import cr.ac.una.eif206.frontend.proxy.ServiceProxy;
import cr.ac.una.eif206.frontend.util.Dialogos;
import cr.ac.una.eif206.frontend.vista.CambiarClaveView;

import java.util.Arrays;

/**
 * Controlador del dialogo "Cambiar clave".
 * El servidor ya sabe quien es el usuario (por la sesion de la conexion),
 * asi que solo se envian la clave actual y la nueva.
 */
public class CambiarClaveController {

    private final CambiarClaveView vista;
    private final ServiceProxy proxy = ServiceProxy.getInstancia();

    public CambiarClaveController(CambiarClaveView vista) {
        this.vista = vista;
        vista.getBtnGuardar().addActionListener(e -> guardar());
        vista.getBtnCancelar().addActionListener(e -> vista.dispose());
    }

    private void guardar() {
        char[] actual = vista.getTxtClaveActual().getPassword();
        char[] nueva = vista.getTxtClaveNueva().getPassword();
        char[] confirmar = vista.getTxtConfirmarClave().getPassword();

        if (actual.length == 0 || nueva.length == 0) {
            Dialogos.advertencia(vista, "Datos incompletos", "Debe llenar todos los campos.");
            return;
        }
        if (!Arrays.equals(nueva, confirmar)) {
            Dialogos.advertencia(vista, "Datos invalidos", "La clave nueva y la confirmacion no coinciden.");
            return;
        }

        try {
            proxy.cambiarClave(new String(actual), new String(nueva));
            Dialogos.info(vista, "Exito", "Clave actualizada correctamente.");
            vista.dispose();
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo cambiar la clave", ex);
        }
    }
}