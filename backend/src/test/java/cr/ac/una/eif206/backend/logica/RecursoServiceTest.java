package cr.ac.una.eif206.backend.logica;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Prueba de UNIDAD (sin MySQL): las validaciones fallan ANTES de tocar la base de datos.
 */
class RecursoServiceTest {

    private final RecursoService service = new RecursoService();

    @Test
    void numeroDeActivoVacioLanzaExcepcion() {
        Exception e = assertThrows(Exception.class, () -> service.agregar("  ", 1, "Laptop"));
        assertEquals("Debe indicar el numero de activo del recurso.", e.getMessage());
    }

    @Test
    void descripcionVaciaLanzaExcepcion() {
        Exception e = assertThrows(Exception.class, () -> service.agregar("LAP-1", 1, "   "));
        assertEquals("Debe indicar la descripcion del recurso.", e.getMessage());
    }

    @Test
    void sinCategoriaLanzaExcepcion() {
        Exception e = assertThrows(Exception.class, () -> service.modificar("LAP-1", 0, "Laptop"));
        assertEquals("Debe seleccionar una categoria.", e.getMessage());
    }

    @Test
    void textosDemasiadoLargosLanzanExcepcion() {
        String largo = "x".repeat(101);
        assertThrows(Exception.class, () -> service.agregar("LAP-1", 1, largo));
        assertThrows(Exception.class, () -> service.agregar("y".repeat(31), 1, "Laptop"));
    }

    @Test
    void eliminarSinIdLanzaExcepcion() {
        assertThrows(Exception.class, () -> service.eliminar(null));
        assertThrows(Exception.class, () -> service.eliminar(" "));
    }
}
