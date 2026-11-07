package cr.ac.una.eif206.backend.red;

import cr.ac.una.eif206.common.protocolo.Request;
import cr.ac.una.eif206.common.protocolo.Response;

/**
 * Comandos de BENJA: funcionarios y categorias (solo administrador).
 *
 * BENJA: por cada comando de tu bloque en Comando.java agrega un "case"
 * que llame a tu servicio. Ejemplo:
 *
 *   case LISTAR_FUNCIONARIOS -> {
 *       sesion.exigirRol(RolUsuario.ADMINISTRADOR);
 *       yield Response.ok(new ArrayList<>(funcionarioService.listarTodos()));
 *   }
 *
 * Comandos: LISTAR_FUNCIONARIOS, BUSCAR_FUNCIONARIOS, REGISTRAR_FUNCIONARIO,
 * MODIFICAR_FUNCIONARIO, ELIMINAR_FUNCIONARIO, BUSCAR_CATEGORIAS,
 * AGREGAR_CATEGORIA, MODIFICAR_CATEGORIA, ELIMINAR_CATEGORIA.
 */
public class ComandosFuncionariosCategorias implements ManejadorComandos {

    @Override
    public Response atender(Request request, SesionCliente sesion) throws Exception {
        return switch (request.getComando()) {

            // BENJA: tus "case" van aqui

            default -> null;
        };
    }
}