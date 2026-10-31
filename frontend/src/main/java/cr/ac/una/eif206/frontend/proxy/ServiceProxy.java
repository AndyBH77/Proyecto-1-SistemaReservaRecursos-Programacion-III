package cr.ac.una.eif206.frontend.proxy;

import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import cr.ac.una.eif206.common.modelo.DatosReservaExtraidos;
import cr.ac.una.eif206.common.modelo.Reserva;
import cr.ac.una.eif206.common.modelo.ResultadoReserva;
import cr.ac.una.eif206.common.modelo.Usuario;
import cr.ac.una.eif206.common.protocolo.Comando;
import cr.ac.una.eif206.common.protocolo.Request;
import cr.ac.una.eif206.common.protocolo.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * PATRON PROXY: representa localmente al "Service" del backend.
 *
 * Los controladores lo usan como si fuera un servicio normal:
 *     Usuario u = ServiceProxy.getInstancia().login("111", "111");
 * pero por dentro este metodo arma un Request, lo envia por el socket,
 * espera el Response y devuelve los datos (o lanza Exception con el
 * mensaje de error que mando el servidor).
 *
 * Es un SINGLETON: hay una sola instancia (una sola conexion) por frontend.
 *
 * Cada integrante agrega sus metodos SOLO en su bloque (abajo).
 */
public final class ServiceProxy {

    private static ServiceProxy instancia;

    private final String host;
    private final int puerto;

    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;

    private ServiceProxy() {
        Properties config = new Properties();
        try (InputStream in = ServiceProxy.class.getClassLoader().getResourceAsStream("cliente.properties")) {
            if (in != null) {
                config.load(in);
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer cliente.properties: " + e.getMessage());
        }
        host = config.getProperty("servidor.host", "localhost").trim();
        puerto = Integer.parseInt(config.getProperty("servidor.puerto", "5000").trim());
    }

    /** Devuelve la unica instancia del proxy (la crea la primera vez). */
    public static synchronized ServiceProxy getInstancia() {
        if (instancia == null) {
            instancia = new ServiceProxy();
        }
        return instancia;
    }

    // ==================================================================
    // Conexion
    // ==================================================================

    /** Abre la conexion con el backend (si no estaba abierta). */
    public synchronized void conectar() throws IOException {
        if (estaConectado()) {
            return;
        }
        socket = new Socket(host, puerto);
        // Igual que en el servidor: primero el ObjectOutputStream + flush()
        salida = new ObjectOutputStream(socket.getOutputStream());
        salida.flush();
        entrada = new ObjectInputStream(socket.getInputStream());
    }

    public synchronized boolean estaConectado() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized void desconectar() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
            // ya estaba cerrado
        }
        socket = null;
        salida = null;
        entrada = null;
    }

    public String getHost() {
        return host;
    }

    public int getPuerto() {
        return puerto;
    }

    /**
     * Envia un Request y espera el Response.
     * "synchronized": si dos pantallas piden algo al mismo tiempo, se
     * atienden una por una (por la misma conexion no pueden ir dos a la vez).
     */
    private synchronized Response enviar(Request request) throws Exception {
        try {
            conectar();
            salida.writeObject(request);
            salida.flush();
            salida.reset();
            return (Response) entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            desconectar();
            throw new Exception("No hay conexion con el servidor (" + host + ":" + puerto + "). "
                    + "Verifique que el backend este corriendo.");
        }
    }

    /** Envia el Request; si el servidor respondio error, lo lanza como Exception. */
    private <T> T ejecutar(Request request) throws Exception {
        Response respuesta = enviar(request);
        if (!respuesta.isExito()) {
            throw new Exception(respuesta.getMensaje());
        }
        return respuesta.getDatos();
    }

    // ==================================================================
    // ANDY: sesion, reservas y alertas
    // ==================================================================

    // ---------------- Sesion ----------------

    public Usuario login(String id, String clave) throws Exception {
        return ejecutar(new Request(Comando.LOGIN)
                .agregar("id", id)
                .agregar("clave", clave));
    }

    public void cambiarClave(String claveActual, String claveNueva) throws Exception {
        ejecutar(new Request(Comando.CAMBIAR_CLAVE)
                .agregar("claveActual", claveActual)
                .agregar("claveNueva", claveNueva));
    }

    public void recuperarClave(String id) throws Exception {
        ejecutar(new Request(Comando.RECUPERAR_CLAVE).agregar("id", id));
    }

    public void cerrarSesion() throws Exception {
        ejecutar(new Request(Comando.CERRAR_SESION));
    }

    public List<CategoriaRecurso> listarCategorias() throws Exception {
        return ejecutar(new Request(Comando.LISTAR_CATEGORIAS));
    }

    // ---------------- Reservas ----------------

    public List<Reserva> listarMisReservas() throws Exception {
        return ejecutar(new Request(Comando.LISTAR_MIS_RESERVAS));
    }

    public ResultadoReserva crearReserva(String actividad, LocalDate fecha, LocalTime horaInicio,
                                         LocalTime horaFin, List<Integer> idsCategorias) throws Exception {
        return ejecutar(new Request(Comando.CREAR_RESERVA)
                .agregar("actividad", actividad)
                .agregar("fecha", fecha)
                .agregar("horaInicio", horaInicio)
                .agregar("horaFin", horaFin)
                .agregar("idsCategorias", new ArrayList<>(idsCategorias)));
    }

    public ResultadoReserva modificarReserva(int idReserva, String actividad, LocalDate fecha,
                                             LocalTime horaInicio, LocalTime horaFin,
                                             List<Integer> idsCategorias) throws Exception {
        return ejecutar(new Request(Comando.MODIFICAR_RESERVA)
                .agregar("idReserva", idReserva)
                .agregar("actividad", actividad)
                .agregar("fecha", fecha)
                .agregar("horaInicio", horaInicio)
                .agregar("horaFin", horaFin)
                .agregar("idsCategorias", new ArrayList<>(idsCategorias)));
    }

    public void cancelarReserva(int idReserva) throws Exception {
        ejecutar(new Request(Comando.CANCELAR_RESERVA).agregar("idReserva", idReserva));
    }

    public DatosReservaExtraidos extraerReservaIA(String frase) throws Exception {
        return ejecutar(new Request(Comando.EXTRAER_RESERVA_IA).agregar("frase", frase));
    }

    // ---------------- Alertas (Etapa 11) ----------------


    // ==================================================================
    // BENJA: funcionarios y categorias
    // Ejemplo:
    //   public List<Funcionario> listarFuncionarios() throws Exception {
    //       return ejecutar(new Request(Comando.LISTAR_FUNCIONARIOS));
    //   }
    // ==================================================================


    // ==================================================================
    // JORDI: recursos, calendarizacion, actividades y estadisticas
    // Ejemplo:
    //   public List<Recurso> listarRecursosPorCategoria(int idCategoria) throws Exception {
    //       return ejecutar(new Request(Comando.LISTAR_RECURSOS_POR_CATEGORIA)
    //               .agregar("idCategoria", idCategoria));
    //   }
    // ==================================================================

}