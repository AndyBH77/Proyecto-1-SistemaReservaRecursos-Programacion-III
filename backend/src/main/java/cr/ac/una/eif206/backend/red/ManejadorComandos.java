package cr.ac.una.eif206.backend.red;

import cr.ac.una.eif206.common.protocolo.Request;
import cr.ac.una.eif206.common.protocolo.Response;

/**
 * Lo que debe saber hacer cada clase de comandos (una por integrante).
 *
 * atender(...) devuelve:
 *  - una Response, si este manejador atiende ese comando;
 *  - null, si el comando no es suyo (entonces se le pregunta al siguiente).
 *
 * Si algo sale mal (validacion, BD...), se lanza una Exception con un
 * mensaje claro y el ManejadorCliente lo convierte en Response.error(...).
 */
public interface ManejadorComandos {

    Response atender(Request request, SesionCliente sesion) throws Exception;
}