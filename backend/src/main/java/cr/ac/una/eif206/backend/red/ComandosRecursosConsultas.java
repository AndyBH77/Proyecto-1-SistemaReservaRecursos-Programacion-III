package cr.ac.una.eif206.backend.red;

import cr.ac.una.eif206.common.protocolo.Request;
import cr.ac.una.eif206.common.protocolo.Response;

/**
 * Comandos de JORDI: recursos, calendarizacion, actividades y estadisticas.
 *
 * JORDI: por cada comando de tu bloque en Comando.java agrega un "case"
 * que llame a tu servicio. Ejemplo:
 *
 *   case LISTAR_RECURSOS -> {
 *       sesion.exigirSesion();
 *       yield Response.ok(new ArrayList<>(recursoService.listarTodos()));
 *   }
 *
 * Comandos: LISTAR_RECURSOS, LISTAR_RECURSOS_POR_CATEGORIA, AGREGAR_RECURSO,
 * MODIFICAR_RECURSO, ELIMINAR_RECURSO, CALENDARIZACION, PROGRAMACION_SEMANAL,
 * ESTADISTICAS_RECURSOS, ESTADISTICAS_ACTIVIDADES.
 */
public class ComandosRecursosConsultas implements ManejadorComandos {

    @Override
    public Response atender(Request request, SesionCliente sesion) throws Exception {
        return switch (request.getComando()) {

            // JORDI: tus "case" van aqui

            default -> null;
        };
    }
}
