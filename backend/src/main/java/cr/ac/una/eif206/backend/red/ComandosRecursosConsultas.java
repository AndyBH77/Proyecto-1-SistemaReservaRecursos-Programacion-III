package cr.ac.una.eif206.backend.red;

import cr.ac.una.eif206.backend.logica.ConsultaService;
import cr.ac.una.eif206.backend.logica.EstadisticaService;
import cr.ac.una.eif206.backend.logica.RecursoService;
import cr.ac.una.eif206.common.modelo.RolUsuario;
import cr.ac.una.eif206.common.protocolo.Request;
import cr.ac.una.eif206.common.protocolo.Response;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Comandos de JORDI: recursos, calendarizacion, actividades y estadisticas.
 *
 * Parametros que espera cada comando (los manda el ServiceProxy):
 *   LISTAR_RECURSOS               -> (ninguno)                          sesion       -> List<Recurso>
 *   LISTAR_RECURSOS_POR_CATEGORIA -> "idCategoria"                      sesion       -> List<Recurso>
 *   AGREGAR_RECURSO               -> "id", "idCategoria", "descripcion" ADMINISTRADOR
 *   MODIFICAR_RECURSO             -> "id", "idCategoria", "descripcion" ADMINISTRADOR
 *   ELIMINAR_RECURSO              -> "id"                               ADMINISTRADOR
 *   CALENDARIZACION               -> "fecha", "idCategoria"             sesion       -> TablaConsulta
 *   PROGRAMACION_SEMANAL          -> "fecha"                            sesion       -> TablaConsulta
 *   ESTADISTICAS_RECURSOS         -> "desde", "hasta"                   sesion       -> List<EstadisticaItem>
 *   ESTADISTICAS_ACTIVIDADES      -> "desde", "hasta"                   sesion       -> List<EstadisticaItem>
 */
public class ComandosRecursosConsultas implements ManejadorComandos {

    private final RecursoService recursoService = new RecursoService();
    private final ConsultaService consultaService = new ConsultaService();
    private final EstadisticaService estadisticaService = new EstadisticaService();

    @Override
    public Response atender(Request request, SesionCliente sesion) throws Exception {
        return switch (request.getComando()) {

            // ---------------- Recursos ----------------
            case LISTAR_RECURSOS -> {
                sesion.exigirSesion();
                yield Response.ok(new ArrayList<>(recursoService.listarTodos()));
            }

            case LISTAR_RECURSOS_POR_CATEGORIA -> {
                sesion.exigirSesion();
                yield Response.ok(new ArrayList<>(
                        recursoService.listarPorCategoria(entero(request, "idCategoria"))));
            }

            case AGREGAR_RECURSO -> {
                sesion.exigirRol(RolUsuario.ADMINISTRADOR);
                recursoService.agregar(request.get("id"), entero(request, "idCategoria"),
                        request.get("descripcion"));
                yield Response.ok();
            }

            case MODIFICAR_RECURSO -> {
                sesion.exigirRol(RolUsuario.ADMINISTRADOR);
                recursoService.modificar(request.get("id"), entero(request, "idCategoria"),
                        request.get("descripcion"));
                yield Response.ok();
            }

            case ELIMINAR_RECURSO -> {
                sesion.exigirRol(RolUsuario.ADMINISTRADOR);
                recursoService.eliminar(request.get("id"));
                yield Response.ok();
            }

            // ---------------- Calendarizacion y Actividades ----------------
            case CALENDARIZACION -> {
                sesion.exigirSesion();
                yield Response.ok(consultaService.calendarizacion(
                        request.<LocalDate>get("fecha"), entero(request, "idCategoria")));
            }

            case PROGRAMACION_SEMANAL -> {
                sesion.exigirSesion();
                yield Response.ok(consultaService.programacionSemanal(request.<LocalDate>get("fecha")));
            }

            // ---------------- Estadisticas ----------------
            case ESTADISTICAS_RECURSOS -> {
                sesion.exigirSesion();
                yield Response.ok(new ArrayList<>(estadisticaService.estadisticasRecursos(
                        request.<LocalDate>get("desde"), request.<LocalDate>get("hasta"))));
            }

            case ESTADISTICAS_ACTIVIDADES -> {
                sesion.exigirSesion();
                yield Response.ok(new ArrayList<>(estadisticaService.estadisticasActividades(
                        request.<LocalDate>get("desde"), request.<LocalDate>get("hasta"))));
            }

            default -> null;
        };
    }

    /** Lee un parametro entero; si falta, da un mensaje claro en vez de un NullPointerException. */
    private int entero(Request request, String nombre) throws Exception {
        Integer valor = request.get(nombre);
        if (valor == null) {
            throw new Exception("Falta el parametro \"" + nombre + "\" en la peticion.");
        }
        return valor;
    }
}
