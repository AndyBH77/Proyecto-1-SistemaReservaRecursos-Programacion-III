package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.backend.datos.RecursoDAO;
import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import cr.ac.una.eif206.common.modelo.Recurso;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de INTEGRACION de Recursos contra MySQL.
 * Depende de los datos de schema.sql (si cambiaron a mano, volver a ejecutarlo).
 */
class RecursoServiceIT {

    private static final String ID_PRUEBA = "IT-REC-001";

    private final RecursoService service = new RecursoService();
    private final RecursoDAO dao = new RecursoDAO();
    private int idLaptops;
    private int idProyectores;

    @BeforeEach
    void buscarCategorias() throws Exception {
        List<CategoriaRecurso> categorias = new CategoriaService().listarTodas();
        idLaptops = categorias.stream().filter(c -> c.getDescripcion().equals("Laptop windows 11"))
                .map(CategoriaRecurso::getId).findFirst()
                .orElseThrow(() -> new AssertionError("Ejecute schema.sql: falta 'Laptop windows 11'"));
        idProyectores = categorias.stream().filter(c -> c.getDescripcion().equals("Proyector"))
                .map(CategoriaRecurso::getId).findFirst()
                .orElseThrow(() -> new AssertionError("Ejecute schema.sql: falta 'Proyector'"));
    }

    @AfterEach
    void limpiar() throws Exception {
        dao.eliminar(ID_PRUEBA);
    }

    @Test
    void agregarConsultarModificarYEliminar() throws Exception {
        service.agregar(ID_PRUEBA, idLaptops, "Laptop de prueba IT");

        Recurso guardado = dao.buscarPorId(ID_PRUEBA).orElseThrow();
        assertEquals("Laptop de prueba IT", guardado.getDescripcion());
        assertEquals("Laptop windows 11", guardado.getDescripcionCategoria());   // viene del JOIN
        assertTrue(service.listarPorCategoria(idLaptops).stream().anyMatch(r -> r.getId().equals(ID_PRUEBA)));
        assertFalse(service.listarPorCategoria(idProyectores).stream().anyMatch(r -> r.getId().equals(ID_PRUEBA)));

        service.modificar(ID_PRUEBA, idProyectores, "Ahora es un proyector");
        Recurso modificado = dao.buscarPorId(ID_PRUEBA).orElseThrow();
        assertEquals(idProyectores, modificado.getIdCategoria());
        assertEquals("Ahora es un proyector", modificado.getDescripcion());

        service.eliminar(ID_PRUEBA);
        assertTrue(dao.buscarPorId(ID_PRUEBA).isEmpty());
    }

    @Test
    void agregarUnNumeroDeActivoRepetidoLanzaExcepcion() throws Exception {
        service.agregar(ID_PRUEBA, idLaptops, "Primera");

        Exception e = assertThrows(Exception.class, () -> service.agregar(ID_PRUEBA, idLaptops, "Repetida"));
        assertEquals("Ya existe un recurso con ese numero de activo.", e.getMessage());
    }

    @Test
    void agregarConCategoriaInexistenteLanzaExcepcion() {
        Exception e = assertThrows(Exception.class, () -> service.agregar(ID_PRUEBA, 99999, "Sin categoria"));
        assertEquals("La categoria seleccionada ya no existe.", e.getMessage());
    }

    @Test
    void eliminarUnRecursoQueEstaEnReservasLanzaExcepcion() {
        // 34343 (Sala 1 primer piso) esta en las reservas 1 y 3 de schema.sql
        Exception e = assertThrows(Exception.class, () -> service.eliminar("34343"));
        assertEquals("No se puede borrar: el recurso esta en reservas.", e.getMessage());
    }

    @Test
    void eliminarOModificarUnRecursoQueNoExisteLanzaExcepcion() {
        assertThrows(Exception.class, () -> service.eliminar("NO-EXISTE-XYZ"));
        assertThrows(Exception.class, () -> service.modificar("NO-EXISTE-XYZ", idLaptops, "Nada"));
    }

    @Test
    void listarTodosTrae7RecursosDeSchemaConSuCategoria() throws Exception {
        List<Recurso> todos = service.listarTodos();

        assertTrue(todos.size() >= 7);
        assertTrue(todos.stream().allMatch(r -> r.getDescripcionCategoria() != null));
    }
}
