package cr.ac.una.eif206.frontend.proxy;

import cr.ac.una.eif206.common.protocolo.Comando;
import cr.ac.una.eif206.common.protocolo.Notificacion;
import cr.ac.una.eif206.common.protocolo.Request;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * HILO que mantiene la SEGUNDA conexion con el backend (canal de alertas).
 *
 * 1. Se conecta y envia SUSCRIBIR_ALERTAS con el id del usuario.
 * 2. Se queda esperando: cada vez que el backend envia una Notificacion,
 *    se la pasa al AlertaListener (en el hilo de Swing, con invokeLater).
 *
 * Es un hilo "daemon": se cierra solo cuando se cierra el programa.
 */
public class EscuchaAlertas extends Thread {

    private final String host;
    private final int puerto;
    private final String idUsuario;
    private final AlertaListener listener;

    private volatile boolean activo = true;
    private volatile Socket socket;

    public EscuchaAlertas(String host, int puerto, String idUsuario, AlertaListener listener) {
        this.host = host;
        this.puerto = puerto;
        this.idUsuario = idUsuario;
        this.listener = listener;
        setName("escucha-alertas");
        setDaemon(true);
    }

    @Override
    public void run() {
        try (Socket s = new Socket(host, puerto)) {
            socket = s;
            ObjectOutputStream salida = new ObjectOutputStream(s.getOutputStream());
            salida.flush();
            ObjectInputStream entrada = new ObjectInputStream(s.getInputStream());

            // Pedir al backend que use esta conexion para enviarnos alertas
            salida.writeObject(new Request(Comando.SUSCRIBIR_ALERTAS).agregar("idUsuario", idUsuario));
            salida.flush();
            entrada.readObject();   // confirmacion (Response.ok)
            System.out.println("[Alertas] Escuchando alertas como usuario " + idUsuario);

            while (activo) {
                Object recibido = entrada.readObject();   // espera hasta que llegue algo
                if (recibido instanceof Notificacion notificacion) {
                    // La pantalla solo se puede tocar desde el hilo de Swing
                    SwingUtilities.invokeLater(() -> listener.alRecibir(notificacion));
                }
            }
        } catch (Exception e) {
            if (activo) {
                System.err.println("[Alertas] Se perdio el canal de alertas: " + e.getMessage());
            }
        }
    }

    /** Cierra el canal (al cerrar sesion o salir). */
    public void detener() {
        activo = false;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
            // ya estaba cerrado
        }
    }
}