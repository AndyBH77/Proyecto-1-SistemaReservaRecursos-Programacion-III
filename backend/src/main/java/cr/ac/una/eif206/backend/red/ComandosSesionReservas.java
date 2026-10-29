package cr.ac.una.eif206.backend.red;

import cr.ac.una.eif206.backend.logica.AutenticacionService;
import cr.ac.una.eif206.backend.logica.CategoriaService;
import cr.ac.una.eif206.common.modelo.Usuario;
import cr.ac.una.eif206.common.protocolo.Request;
import cr.ac.una.eif206.common.protocolo.Response;

import java.util.ArrayList;

/**
 * Comandos de ANDY: sesion (login, claves), reservas y alertas.
 *
 * Parametros que espera cada comando (los manda el ServiceProxy):
 *   LOGIN            -> "id", "clave"
 *   CAMBIAR_CLAVE    -> "claveActual", "claveNueva"   (requiere sesion)
 *   RECUPERAR_CLAVE  -> "id"
 *   CERRAR_SESION    -> (ninguno)
 *   LISTAR_CATEGORIAS-> (ninguno)
 *
 * Los comandos de reservas y alertas se agregan en las etapas 10 y 11.
 */
public class ComandosSesionReservas implements ManejadorComandos {

    private final AutenticacionService autenticacionService = new AutenticacionService();
    private final CategoriaService categoriaService = new CategoriaService();

    @Override
    public Response atender(Request request, SesionCliente sesion) throws Exception {
        return switch (request.getComando()) {

            case LOGIN -> {
                Usuario usuario = autenticacionService.login(request.get("id"), request.get("clave"));
                sesion.setUsuario(usuario);      // la conexion queda "con sesion iniciada"
                yield Response.ok(usuario);
            }

            case CAMBIAR_CLAVE -> {
                sesion.exigirSesion();
                autenticacionService.cambiarClave(sesion.getUsuario().getId(),
                        request.get("claveActual"), request.get("claveNueva"));
                yield Response.ok();
            }

            case RECUPERAR_CLAVE -> {
                autenticacionService.recuperarClave(request.get("id"));
                yield Response.ok();
            }

            case CERRAR_SESION -> {
                sesion.setUsuario(null);         // la conexion sigue abierta, pero sin usuario
                yield Response.ok();
            }

            case LISTAR_CATEGORIAS ->
                    Response.ok(new ArrayList<>(categoriaService.listarTodas()));

            default -> null;   // no es un comando de este bloque
        };
    }
}