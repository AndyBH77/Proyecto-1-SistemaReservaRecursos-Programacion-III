package cr.ac.una.eif206.backend.datos;

import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de INTEGRACION (Failsafe): necesita MySQL encendido y el
 * schema.sql ejecutado, porque se conecta a la base real.
 * Se ejecuta con: Maven -> Lifecycle -> verify
 */
class CategoriaRecursoDAOIT {

    private final CategoriaRecursoDAO dao = new CategoriaRecursoDAO();

    @Test
    void seConectaAMySQL() throws Exception {
        try (Connection con = ConexionBD.obtenerConexion()) {
            assertTrue(con.isValid(2), "La conexion a MySQL no es valida");
        }
    }

    @Test
    void listarTodasDevuelveLasCategoriasDelScript() throws Exception {
        List<CategoriaRecurso> categorias = dao.listarTodas();
        assertFalse(categorias.isEmpty(), "No hay categorias: ejecute database/schema.sql");
    }

    @Test
    void buscarPorIdEncuentraUnaCategoriaExistente() throws Exception {
        CategoriaRecurso primera = dao.listarTodas().get(0);

        Optional<CategoriaRecurso> encontrada = dao.buscarPorId(primera.getId());

        assertTrue(encontrada.isPresent());
        assertEquals(primera.getDescripcion(), encontrada.get().getDescripcion());
    }

    @Test
    void buscarPorIdDevuelveVacioSiNoExiste() throws Exception {
        assertTrue(dao.buscarPorId(-1).isEmpty());
    }
}