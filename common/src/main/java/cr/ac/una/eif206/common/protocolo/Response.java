package cr.ac.una.eif206.common.protocolo;

import java.io.Serializable;

/**
 * Respuesta que el backend devuelve al frontend.
 * - Si todo salio bien: exito = true y en "datos" va el resultado.
 * - Si hubo un error (validacion, BD, etc.): exito = false y en "mensaje"
 *   va el texto que el frontend le muestra al usuario.
 *
 * Se crea con los metodos estaticos:
 *   Response.ok(listaDeReservas)
 *   Response.error("La hora de inicio debe ser antes que la de fin")
 */
public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean exito;
    private final String mensaje;
    private final Object datos;

    private Response(boolean exito, String mensaje, Object datos) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    public static Response ok(Object datos) {
        return new Response(true, null, datos);
    }

    public static Response ok() {
        return new Response(true, null, null);
    }

    public static Response error(String mensaje) {
        return new Response(false, mensaje, null);
    }

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    /** Devuelve los datos ya convertidos al tipo esperado. */
    @SuppressWarnings("unchecked")
    public <T> T getDatos() {
        return (T) datos;
    }
}