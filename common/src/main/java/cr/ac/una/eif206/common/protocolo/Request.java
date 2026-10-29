package cr.ac.una.eif206.common.protocolo;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Peticion que el frontend envia al backend por el socket.
 * Lleva un Comando y los parametros que ese comando necesita, guardados por nombre.
 *
 * Ejemplo de uso (en el ServiceProxy del frontend):
 *   Request req = new Request(Comando.LOGIN)
 *           .agregar("id", "111")
 *           .agregar("clave", "111");
 *
 * Y en el backend:
 *   String id = req.get("id");
 *
 * OJO: todo lo que se agregue como parametro debe ser Serializable
 * (String, Integer, LocalDate, List, clases del modelo...).
 */
public class Request implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Comando comando;
    private final HashMap<String, Object> parametros = new HashMap<>();

    public Request(Comando comando) {
        this.comando = comando;
    }

    /** Agrega un parametro y devuelve el mismo Request (para encadenar). */
    public Request agregar(String nombre, Object valor) {
        parametros.put(nombre, valor);
        return this;
    }

    /** Obtiene un parametro ya convertido al tipo que se espera. */
    @SuppressWarnings("unchecked")
    public <T> T get(String nombre) {
        return (T) parametros.get(nombre);
    }

    public Comando getComando() {
        return comando;
    }

    @Override
    public String toString() {
        return "Request{" + comando + ", parametros=" + parametros.keySet() + "}";
    }
}