package cr.ac.una.eif206.backend.red;

import cr.ac.una.eif206.backend.logica.CategoriaService;
import cr.ac.una.eif206.backend.logica.FuncionarioService;
import cr.ac.una.eif206.common.modelo.RolUsuario;
import cr.ac.una.eif206.common.protocolo.Request;
import cr.ac.una.eif206.common.protocolo.Response;

import java.util.ArrayList;

/**
 * Comandos de funcionarios y categorias (TODOS solo para ADMINISTRADOR).
 *
 * Parametros que espera cada comando (los manda el ServiceProxy):
 *   LISTAR_FUNCIONARIOS   -> (ninguno)                              -> List<Funcionario>
 *   BUSCAR_FUNCIONARIOS   -> "texto"                                -> List<Funcionario>
 *   REGISTRAR_FUNCIONARIO -> "id", "nombre", "telefono", "email"    -> String (correo al que se envio la clave)
 *   MODIFICAR_FUNCIONARIO -> "id", "nombre", "telefono", "email"
 *   ELIMINAR_FUNCIONARIO  -> "id"
 *   BUSCAR_CATEGORIAS     -> "texto"                                -> List<CategoriaRecurso>
 *   AGREGAR_CATEGORIA     -> "descripcion"
 *   MODIFICAR_CATEGORIA   -> "id" (Integer), "descripcion"
 *   ELIMINAR_CATEGORIA    -> "id" (Integer)
 *
 * (LISTAR_CATEGORIAS esta en ComandosSesionReservas porque lo usan todos.)
 */
public class ComandosFuncionariosCategorias implements ManejadorComandos {

    private final FuncionarioService funcionarioService = new FuncionarioService();
    private final CategoriaService categoriaService = new CategoriaService();

    @Override
    public Response atender(Request request, SesionCliente sesion) throws Exception {
        return switch (request.getComando()) {

            // ---------------- Funcionarios ----------------
            case LISTAR_FUNCIONARIOS -> {
                sesion.exigirRol(RolUsuario.ADMINISTRADOR);
                yield Response.ok(new ArrayList<>(funcionarioService.listarTodos()));
            }

            case BUSCAR_FUNCIONARIOS -> {
                sesion.exigirRol(RolUsuario.ADMINISTRADOR);
                yield Response.ok(new ArrayList<>(funcionarioService.buscar(request.get("texto"))));
            }

            case REGISTRAR_FUNCIONARIO -> {
                sesion.exigirRol(RolUsuario.ADMINISTRADOR);
                String correo = funcionarioService.registrar(request.get("id"), request.get("nombre"),
                        request.get("telefono"), request.get("email"));
                yield Response.ok(correo);
            }

            case MODIFICAR_FUNCIONARIO -> {
                sesion.exigirRol(RolUsuario.ADMINISTRADOR);
                funcionarioService.modificar(request.get("id"), request.get("nombre"),
                        request.get("telefono"), request.get("email"));
                yield Response.ok();
            }

            case ELIMINAR_FUNCIONARIO -> {
                sesion.exigirRol(RolUsuario.ADMINISTRADOR);
                funcionarioService.eliminar(request.get("id"));
                yield Response.ok();
            }

            // ---------------- Categorias ----------------
            case BUSCAR_CATEGORIAS -> {
                sesion.exigirRol(RolUsuario.ADMINISTRADOR);
                yield Response.ok(new ArrayList<>(categoriaService.buscar(request.get("texto"))));
            }

            case AGREGAR_CATEGORIA -> {
                sesion.exigirRol(RolUsuario.ADMINISTRADOR);
                categoriaService.agregar(request.get("descripcion"));
                yield Response.ok();
            }

            case MODIFICAR_CATEGORIA -> {
                sesion.exigirRol(RolUsuario.ADMINISTRADOR);
                categoriaService.modificar(entero(request, "id"), request.get("descripcion"));
                yield Response.ok();
            }

            case ELIMINAR_CATEGORIA -> {
                sesion.exigirRol(RolUsuario.ADMINISTRADOR);
                categoriaService.eliminar(entero(request, "id"));
                yield Response.ok();
            }

            default -> null;   // no es un comando de este bloque
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