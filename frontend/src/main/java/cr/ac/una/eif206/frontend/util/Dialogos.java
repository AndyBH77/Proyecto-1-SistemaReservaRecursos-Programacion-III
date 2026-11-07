package cr.ac.una.eif206.frontend.util;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.util.concurrent.ExecutionException;

/**
 * Atajos para mostrar mensajes al usuario.
 *
 * mensajeDe(...) saca el texto "real" del error. Cuando el error viene de un
 * SwingWorker, Java lo envuelve en ExecutionException; aqui se desenvuelve
 * para mostrar el mensaje que mando el servidor (ej: "La clave actual no es correcta.").
 */
public final class Dialogos {

    private Dialogos() {
    }

    public static String mensajeDe(Throwable error) {
        Throwable causa = (error instanceof ExecutionException && error.getCause() != null)
                ? error.getCause() : error;
        String mensaje = causa.getMessage();
        return (mensaje == null || mensaje.isBlank()) ? "Ocurrio un error inesperado." : mensaje;
    }

    public static void error(Component padre, String titulo, Throwable error) {
        JOptionPane.showMessageDialog(padre, mensajeDe(error), titulo, JOptionPane.ERROR_MESSAGE);
    }

    public static void advertencia(Component padre, String titulo, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, titulo, JOptionPane.WARNING_MESSAGE);
    }

    public static void info(Component padre, String titulo, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }
}