package cr.ac.una.eif206.backend.red;

import cr.ac.una.eif206.common.protocolo.Comando;
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
 * Atiende a UNA conexion de un frontend. Cada instancia corre en su propio hilo.
 *
 * Hay dos tipos de conexion:
 *  - Normal: recibe Requests y responde Responses (ciclo de siempre).
 *  - Canal de alertas: el primer Request es SUSCRIBIR_ALERTAS; desde ahi el
 *    servidor la usa para ENVIARLE notificaciones (RegistroConexiones).
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

        ObjectOutputStream salida = null;

        try (socket) {
            // IMPORTANTE: primero el ObjectOutputStream + flush(), luego el ObjectInputStream
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Object recibido = entrada.readObject();   // espera la siguiente peticion

                Response respuesta;
                if (recibido instanceof Request request) {

                    if (request.getComando() == Comando.SUSCRIBIR_ALERTAS) {
                        // Esta conexion pasa a ser un canal de alertas
                        RegistroConexiones.registrar(request.get("idUsuario"), salida);
                        respuesta = Response.ok();
                    } else {
                        System.out.println("[Servidor] " + cliente + " -> " + request.getComando());
                        respuesta = procesar(request);
                    }

                } else {
                    respuesta = Response.error("Peticion no valida.");
                }

                // synchronized: en un canal de alertas, difundir() tambien escribe en este stream
                synchronized (salida) {
                    salida.writeObject(respuesta);
                    salida.flush();
                    salida.reset();
                }
            }

        } catch (EOFException | SocketException e) {
            // Normal: el frontend cerro la conexion.
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[Servidor] Error con " + cliente + ": " + e.getMessage());
        } finally {
            if (salida != null) {
                RegistroConexiones.quitar(salida);   // si era canal de alertas, deja de recibir
            }
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
            e.printStackTrace();
            return Response.error("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }
}