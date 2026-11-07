package cr.ac.una.eif206.backend.red;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Servidor que escucha en un puerto y crea un hilo (ManejadorCliente)
 * por cada frontend que se conecta.
 *
 * El puerto se abre en el constructor, asi que apenas se crea el objeto
 * ya se pueden conectar clientes (util para las pruebas).
 */
public class Servidor {

    private final ServerSocket serverSocket;
    private volatile boolean activo = true;

    /** @param puerto puerto a escuchar (0 = cualquier puerto libre, para pruebas) */
    public Servidor(int puerto) throws IOException {
        this.serverSocket = new ServerSocket(puerto);
    }

    public int getPuerto() {
        return serverSocket.getLocalPort();
    }

    /** Ciclo principal: espera clientes para siempre (hasta llamar detener()). */
    public void iniciar() {
        System.out.println("[Servidor] Escuchando en el puerto " + getPuerto() + "...");

        while (activo) {
            try {
                Socket cliente = serverSocket.accept();   // se bloquea hasta que alguien se conecte

                Thread hilo = new Thread(new ManejadorCliente(cliente));
                hilo.setName("cliente-" + cliente.getPort());
                hilo.start();

            } catch (IOException e) {
                if (activo) {
                    System.err.println("[Servidor] Error aceptando conexion: " + e.getMessage());
                }
            }
        }
        System.out.println("[Servidor] Detenido.");
    }

    /** Cierra el puerto; el ciclo de iniciar() termina. */
    public void detener() {
        activo = false;
        try {
            serverSocket.close();
        } catch (IOException ignored) {
            // ya estaba cerrado
        }
    }
}