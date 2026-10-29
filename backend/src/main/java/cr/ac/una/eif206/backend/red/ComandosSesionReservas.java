package cr.ac.una.eif206.backend.red;

import cr.ac.una.eif206.backend.logica.CategoriaService;
import cr.ac.una.eif206.common.protocolo.Request;
import cr.ac.una.eif206.common.protocolo.Response;

import java.util.ArrayList;

/**
 * Comandos de ANDY: sesion (login, claves), reservas y alertas.
 *
 * Cada "case" llama al servicio correspondiente y devuelve la Response.
 * Los demas comandos (LOGIN, CREAR_RESERVA, etc.) se agregan en las etapas 8 y 9.
 */
public class ComandosSesionReservas implements ManejadorComandos {

    private final CategoriaService categoriaService = new CategoriaService();

    @Override
    public Response atender(Request request, SesionCliente sesion) throws Exception {
        return switch (request.getComando()) {

            case LISTAR_CATEGORIAS ->
                // new ArrayList<>(...) asegura que la lista se pueda serializar
                    Response.ok(new ArrayList<>(categoriaService.listarTodas()));

            default -> null;   // no es un comando de este bloque
        };
    }
}