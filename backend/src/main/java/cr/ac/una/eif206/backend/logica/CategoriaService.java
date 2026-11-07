package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.backend.datos.CategoriaRecursoDAO;
import cr.ac.una.eif206.common.modelo.CategoriaRecurso;

import java.sql.SQLException;
import java.util.List;

/**
 * Logica de negocio de las categorias de recursos.
 * Aqui van las validaciones; el DAO solo habla con MySQL.
 *
 * Andy deja listarTodas() porque Reservas, Recursos y Calendarizacion
 * necesitan la lista de categorias.
 */
public class CategoriaService {

    private final CategoriaRecursoDAO dao = new CategoriaRecursoDAO();

    public List<CategoriaRecurso> listarTodas() throws SQLException {
        return dao.listarTodas();
    }

    // ------------------------------------------------------------------
    // BENJA: agregar aqui buscar, agregar, modificar y eliminar,
    //        con sus validaciones (descripcion vacia, repetida, etc.).
    // ------------------------------------------------------------------
}