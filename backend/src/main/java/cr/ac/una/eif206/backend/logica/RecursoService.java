package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.backend.datos.CategoriaRecursoDAO;
import cr.ac.una.eif206.backend.datos.RecursoDAO;
import cr.ac.una.eif206.common.modelo.Recurso;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

/**
 * Logica de negocio de los recursos (JORDI).
 * Valida los datos y traduce los errores de MySQL a mensajes entendibles.
 * Todo error que el usuario debe ver sale como Exception con un mensaje claro.
 */
public class RecursoService {

    static final int MAX_ID = 30;               // recurso.id         VARCHAR(30)
    static final int MAX_DESCRIPCION = 100;     // recurso.descripcion VARCHAR(100)

    private final RecursoDAO recursoDAO = new RecursoDAO();
    private final CategoriaRecursoDAO categoriaDAO = new CategoriaRecursoDAO();

    public List<Recurso> listarTodos() throws SQLException {
        return recursoDAO.listarTodos();
    }

    public List<Recurso> listarPorCategoria(int idCategoria) throws SQLException {
        return recursoDAO.listarPorCategoria(idCategoria);
    }

    public void agregar(String id, int idCategoria, String descripcion) throws Exception {
        Recurso recurso = validar(id, idCategoria, descripcion);

        if (recursoDAO.buscarPorId(recurso.getId()).isPresent()) {
            throw new Exception("Ya existe un recurso con ese numero de activo.");
        }
        try {
            recursoDAO.insertar(recurso);
        } catch (SQLIntegrityConstraintViolationException e) {
            // otro cliente lo creo (o borro la categoria) justo en este instante
            throw new Exception(mensajeDeIntegridadAlGuardar(e));
        }
    }

    public void modificar(String id, int idCategoria, String descripcion) throws Exception {
        Recurso recurso = validar(id, idCategoria, descripcion);

        try {
            if (recursoDAO.actualizar(recurso) == 0) {
                throw new Exception("No existe un recurso con ese numero de activo.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new Exception(mensajeDeIntegridadAlGuardar(e));
        }
    }

    public void eliminar(String id) throws Exception {
        if (id == null || id.isBlank()) {
            throw new Exception("Debe indicar el numero de activo del recurso.");
        }
        try {
            if (recursoDAO.eliminar(id.trim()) == 0) {
                throw new Exception("No existe un recurso con ese numero de activo.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new Exception("No se puede borrar: el recurso esta en reservas.");
        }
    }

    // ------------------------------------------------------------------

    /** Valida los campos y la categoria; devuelve el recurso ya limpio (trim). */
    private Recurso validar(String id, int idCategoria, String descripcion) throws Exception {
        String idLimpio = id == null ? "" : id.trim();
        String descripcionLimpia = descripcion == null ? "" : descripcion.trim();

        if (idLimpio.isEmpty()) {
            throw new Exception("Debe indicar el numero de activo del recurso.");
        }
        if (idLimpio.length() > MAX_ID) {
            throw new Exception("El numero de activo no puede tener mas de " + MAX_ID + " caracteres.");
        }
        if (descripcionLimpia.isEmpty()) {
            throw new Exception("Debe indicar la descripcion del recurso.");
        }
        if (descripcionLimpia.length() > MAX_DESCRIPCION) {
            throw new Exception("La descripcion no puede tener mas de " + MAX_DESCRIPCION + " caracteres.");
        }
        if (idCategoria <= 0) {
            throw new Exception("Debe seleccionar una categoria.");
        }
        if (categoriaDAO.buscarPorId(idCategoria).isEmpty()) {
            throw new Exception("La categoria seleccionada ya no existe.");
        }
        return new Recurso(idLimpio, idCategoria, descripcionLimpia);
    }

    /** MySQL: 1062 = valor duplicado, 1452 = la llave foranea (categoria) no existe. */
    private String mensajeDeIntegridadAlGuardar(SQLException e) {
        if (e.getErrorCode() == 1452) {
            return "La categoria seleccionada ya no existe.";
        }
        return "Ya existe un recurso con ese numero de activo.";
    }
}
