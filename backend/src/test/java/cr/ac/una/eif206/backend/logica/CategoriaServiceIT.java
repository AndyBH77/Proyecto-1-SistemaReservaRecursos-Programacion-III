package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.backend.datos.CategoriaRecursoDAO;
import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de INTEGRACION del CRUD de categorias contra MySQL.
 * Lo que crea lo borra al final (@AfterEach).
 */
class CategoriaServiceIT {

    private static final String NOMBRE_PRUEBA = "Categoria de prueba IT";

    private final CategoriaService service = new CategoriaService();
    private final CategoriaRecursoDAO dao = new CategoriaRecursoDAO();

    @AfterEach
    void borrarLoCreado() throws Exception {
        for (CategoriaRecurso c : dao.buscarPorDescripcion("prueba IT")) {
            dao.eliminar(c.getId());
        }
    }

    @Test
    void agregarBuscarModificarYEliminar() throws Exception {
        CategoriaRecurso nueva = service.agregar("  " + NOMBRE_PRUEBA + "  ");
        assertTrue(nueva.getId() > 0, "MySQL debio generar el id");
        assertEquals(NOMBRE_PRUEBA, nueva.getDescripcion(), "Debe guardarse sin espacios de mas");

        List<CategoriaRecurso> encontradas = service.buscar("prueba IT");
        assertEquals(1, encontradas.size());

        service.modificar(nueva.getId(), NOMBRE_PRUEBA + " 2");
        assertEquals(NOMBRE_PRUEBA + " 2", dao.buscarPorId(nueva.getId()).orElseThrow().getDescripcion());

        service.eliminar(nueva.getId());
        assertTrue(dao.buscarPorId(nueva.getId()).isEmpty());
    }

    @Test
    void noPermiteDescripcionRepetida() throws Exception {
        service.agregar(NOMBRE_PRUEBA);
        Exception error = assertThrows(Exception.class, () -> service.agregar(NOMBRE_PRUEBA));
        assertTrue(error.getMessage().contains("Ya existe"));
    }

    @Test
    void noPermiteEliminarCategoriaConRecursos() throws Exception {
        int idSalas = service.buscar("Sala para 10 personas").get(0).getId();
        Exception error = assertThrows(Exception.class, () -> service.eliminar(idSalas));
        assertTrue(error.getMessage().contains("recursos"));
    }

    @Test
    void buscarSinTextoDevuelveTodas() throws Exception {
        assertFalse(service.buscar("").isEmpty());
        assertEquals(service.listarTodas().size(), service.buscar("   ").size());
    }

    @Test
    void descripcionVaciaLanzaError() {
        assertThrows(Exception.class, () -> service.agregar("   "));
    }
}