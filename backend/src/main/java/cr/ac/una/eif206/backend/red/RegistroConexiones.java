package cr.ac.una.eif206.backend.red;

import cr.ac.una.eif206.common.protocolo.Notificacion;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lista de frontends suscritos al canal de alertas (su "segunda conexion").
 *
 * Cuando alguien registra una reserva, difundir(...) le envia la Notificacion
 * a todos los suscritos MENOS al usuario que la hizo.
 *
 * Es "static" porque hay UNA sola lista para todo el servidor, compartida por
 * todos los hilos. CopyOnWriteArrayList permite recorrerla y modificarla desde
 * varios hilos al mismo tiempo sin errores.
 */
public final class RegistroConexiones {

    /** Un frontend suscrito: de que usuario es y por donde se le escribe. */
    private record Suscriptor(String idUsuario, ObjectOutputStream salida) {
    }

    private static final List<Suscriptor> suscriptores = new CopyOnWriteArrayList<>();

    private RegistroConexiones() {
    }

    public static void registrar(String idUsuario, ObjectOutputStream salida) {
        suscriptores.add(new Suscriptor(idUsuario, salida));
        System.out.println("[Alertas] Suscrito el usuario " + idUsuario
                + " (frontends escuchando: " + suscriptores.size() + ")");
    }

    /** Se llama cuando se cierra la conexion de alertas de un frontend. */
    public static void quitar(ObjectOutputStream salida) {
        suscriptores.removeIf(s -> s.salida() == salida);
    }

    /**
     * Envia la notificacion a todos los suscritos, excepto al usuario indicado.
     * @param idUsuarioExcluido el que hizo la reserva (null = enviar a todos, se usa en pruebas)
     */
    public static void difundir(Notificacion notificacion, String idUsuarioExcluido) {
        int enviadas = 0;

        for (Suscriptor s : suscriptores) {
            if (idUsuarioExcluido != null && idUsuarioExcluido.equals(s.idUsuario())) {
                continue;   // al que hizo la reserva no se le avisa
            }
            try {
                // synchronized: que nadie mas escriba en ese mismo stream a la vez
                synchronized (s.salida()) {
                    s.salida().writeObject(notificacion);
                    s.salida().flush();
                    s.salida().reset();
                }
                enviadas++;
            } catch (IOException e) {
                suscriptores.remove(s);   // ese frontend ya se cerro
            }
        }
        System.out.println("[Alertas] \"" + notificacion.getTitulo() + "\" enviada a " + enviadas + " frontend(s).");
    }

    public static int cantidadSuscritos() {
        return suscriptores.size();
    }
}