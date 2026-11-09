package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.backend.datos.CategoriaRecursoDAO;
import cr.ac.una.eif206.common.modelo.CategoriaRecurso;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

/**
 * Logica de negocio de las categorias de recursos (CRUD solo para ADMINISTRADOR).
 * Aqui van las validaciones; el DAO solo habla con MySQL.
 *
 * Los errores de MySQL por llaves (descripcion repetida, categoria con
 * recursos) llegan como SQLIntegrityConstraintViolationException y aqui se
 * traducen a un mensaje entendible para el usuario.
 */
public class CategoriaService {

    private static final int LARGO_MAXIMO = 100;   // igual que VARCHAR(100) en schema.sql

    private final CategoriaRecursoDAO dao = new CategoriaRecursoDAO();

    public List<CategoriaRecurso> listarTodas() throws SQLException {
        return dao.listarTodas();
    }

    /** Busca por descripcion; si el texto esta vacio devuelve todas. */
    public List<CategoriaRecurso> buscar(String texto) throws SQLException {
        if (texto == null || texto.isBlank()) {
            return dao.listarTodas();
        }
        return dao.buscarPorDescripcion(texto.trim());
    }

    public CategoriaRecurso agregar(String descripcion) throws Exception {
        String limpia = validarDescripcion(descripcion);
        try {
            return dao.insertar(new CategoriaRecurso(0, limpia));
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new Exception("Ya existe una categoría con la descripción \"" + limpia + "\".");
        }
    }

    public void modificar(int id, String descripcion) throws Exception {
        String limpia = validarDescripcion(descripcion);
        try {
            if (dao.actualizar(new CategoriaRecurso(id, limpia)) == 0) {
                throw new Exception("La categoría ya no existe.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new Exception("Ya existe otra categoría con la descripción \"" + limpia + "\".");
        }
    }

    public void eliminar(int id) throws Exception {
        try {
            if (dao.eliminar(id) == 0) {
                throw new Exception("La categoría ya no existe.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new Exception("No se puede eliminar: la categoría tiene recursos asociados. "
                    + "Elimine o cambie de categoría esos recursos primero.");
        }
    }

    /** Revisa la descripcion y la devuelve sin espacios al inicio/fin. */
    private String validarDescripcion(String descripcion) throws Exception {
        if (descripcion == null || descripcion.isBlank()) {
            throw new Exception("Debe indicar la descripción de la categoría.");
        }
        String limpia = descripcion.trim();
        if (limpia.length() > LARGO_MAXIMO) {
            throw new Exception("La descripción no puede pasar de " + LARGO_MAXIMO + " caracteres.");
        }
        return limpia;
    }
}