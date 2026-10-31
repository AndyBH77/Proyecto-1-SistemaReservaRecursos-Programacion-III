package cr.ac.una.eif206.backend.red;

import cr.ac.una.eif206.backend.logica.AutenticacionService;
import cr.ac.una.eif206.backend.logica.CategoriaService;
import cr.ac.una.eif206.backend.logica.ReservaService;
import cr.ac.una.eif206.backend.logica.llm.LlmExtractorService;
import cr.ac.una.eif206.common.modelo.ResultadoReserva;
import cr.ac.una.eif206.common.modelo.RolUsuario;
import cr.ac.una.eif206.common.modelo.Usuario;
import cr.ac.una.eif206.common.protocolo.Request;
import cr.ac.una.eif206.common.protocolo.Response;

import java.util.ArrayList;

/**
 * Comandos de ANDY: sesion (login, claves), reservas y alertas.
 *
 * Parametros que espera cada comando (los manda el ServiceProxy):
 *   LOGIN               -> "id", "clave"
 *   CAMBIAR_CLAVE       -> "claveActual", "claveNueva"          (requiere sesion)
 *   RECUPERAR_CLAVE     -> "id"
 *   CERRAR_SESION       -> (ninguno)
 *   LISTAR_CATEGORIAS   -> (ninguno)
 *   LISTAR_MIS_RESERVAS -> (ninguno)                            (solo FUNCIONARIO)
 *   CREAR_RESERVA       -> "actividad", "fecha", "horaInicio", "horaFin", "idsCategorias"
 *   MODIFICAR_RESERVA   -> "idReserva" + los mismos de CREAR_RESERVA
 *   CANCELAR_RESERVA    -> "idReserva"
 *   EXTRAER_RESERVA_IA  -> "frase"
 */
public class ComandosSesionReservas implements ManejadorComandos {

    private final AutenticacionService autenticacionService = new AutenticacionService();
    private final CategoriaService categoriaService = new CategoriaService();
    private final ReservaService reservaService = new ReservaService();
    private final LlmExtractorService llmExtractorService = new LlmExtractorService();

    @Override
    public Response atender(Request request, SesionCliente sesion) throws Exception {
        return switch (request.getComando()) {

            // ---------------- Sesion ----------------
            case LOGIN -> {
                Usuario usuario = autenticacionService.login(request.get("id"), request.get("clave"));
                sesion.setUsuario(usuario);
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
                sesion.setUsuario(null);
                yield Response.ok();
            }

            case LISTAR_CATEGORIAS ->
                    Response.ok(new ArrayList<>(categoriaService.listarTodas()));

            // ---------------- Reservas (solo funcionario) ----------------
            case LISTAR_MIS_RESERVAS -> {
                sesion.exigirRol(RolUsuario.FUNCIONARIO);
                yield Response.ok(new ArrayList<>(reservaService.listarDe(sesion.getUsuario().getId())));
            }

            case CREAR_RESERVA -> {
                sesion.exigirRol(RolUsuario.FUNCIONARIO);
                ResultadoReserva resultado = reservaService.crear(
                        sesion.getUsuario().getId(),
                        request.get("actividad"),
                        request.get("fecha"),
                        request.get("horaInicio"),
                        request.get("horaFin"),
                        request.get("idsCategorias"));

                // ETAPA 11: aqui se avisara a los DEMAS frontends si resultado.isExito()

                yield Response.ok(resultado);
            }

            case MODIFICAR_RESERVA -> {
                sesion.exigirRol(RolUsuario.FUNCIONARIO);
                int idReserva = request.<Integer>get("idReserva");
                yield Response.ok(reservaService.modificar(
                        idReserva,
                        sesion.getUsuario().getId(),
                        request.get("actividad"),
                        request.get("fecha"),
                        request.get("horaInicio"),
                        request.get("horaFin"),
                        request.get("idsCategorias")));
            }

            case CANCELAR_RESERVA -> {
                sesion.exigirRol(RolUsuario.FUNCIONARIO);
                int idReserva = request.<Integer>get("idReserva");
                reservaService.cancelar(idReserva, sesion.getUsuario().getId());
                yield Response.ok();
            }

            case EXTRAER_RESERVA_IA -> {
                sesion.exigirSesion();
                yield Response.ok(llmExtractorService.extraer(
                        request.get("frase"), categoriaService.listarTodas()));
            }

            default -> null;   // no es un comando de este bloque
        };
    }
}