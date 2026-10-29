package cr.ac.una.eif206.backend.red;

import cr.ac.una.eif206.common.protocolo.Request;
import cr.ac.una.eif206.common.protocolo.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.List;

/**
 * Atiende a UN frontend conectado. Cada instancia corre en su propio hilo.
 *
 * Ciclo de vida:
 *  1. Recibe el socket ya conectado (lo acepto el Servidor).
 *  2. Lee un Request, lo procesa y responde con un Response. Repite.
 *  3. Cuando el frontend se cierra, readObject lanza EOFException/SocketException
 *     y el hilo termina.
 */
public class ManejadorCliente implements Runnable {

    private final Socket socket;
    private final SesionCliente sesion = new SesionCliente();

    /** Las clases de comandos de cada integrante, en el orden en que se consultan. */
    private final List<ManejadorComandos> manejadores = List.of(
            new ComandosSesionReservas(),          // Andy
            new ComandosFuncionariosCategorias(),  // Benja
            new ComandosRecursosConsultas()        // Jordi
    );

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String cliente = socket.getRemoteSocketAddress().toString();
        System.out.println("[Servidor] Cliente conectado: " + cliente);

        try (socket) {
            // IMPORTANTE: crear PRIMERO el ObjectOutputStream y hacer flush().
            // Si ambos lados crean primero el ObjectInputStream, se quedan
            // esperandose mutuamente para siempre (deadlock).
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Object recibido = entrada.readObject();   // espera la siguiente peticion

                Response respuesta;
                if (recibido instanceof Request request) {
                    System.out.println("[Servidor] " + cliente + " -> " + request.getComando());
                    respuesta = procesar(request);
                } else {
                    respuesta = Response.error("Peticion no valida.");
                }

                salida.writeObject(respuesta);
                salida.flush();
                // reset() evita que Java reenvie una copia "vieja" de un objeto
                // que ya se habia enviado antes por este mismo stream.
                salida.reset();
            }

        } catch (EOFException | SocketException e) {
            // Normal: el frontend cerro la conexion.
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[Servidor] Error con " + cliente + ": " + e.getMessage());
        }

        System.out.println("[Servidor] Cliente desconectado: " + cliente);
    }

    /**
     * Le pregunta a cada clase de comandos si atiende este comando.
     * Cualquier error se convierte en Response.error con un mensaje para el usuario.
     */
    private Response procesar(Request request) {
        try {
            for (ManejadorComandos manejador : manejadores) {
                Response respuesta = manejador.atender(request, sesion);
                if (respuesta != null) {
                    return respuesta;
                }
            }
            return Response.error("El comando " + request.getComando()
                    + " todavia no esta implementado en el servidor.");

        } catch (SQLException e) {
            e.printStackTrace();   // detalle tecnico en la consola del servidor
            return Response.error("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            return Response.error(e.getMessage());   // errores de validacion
        }
    }
}