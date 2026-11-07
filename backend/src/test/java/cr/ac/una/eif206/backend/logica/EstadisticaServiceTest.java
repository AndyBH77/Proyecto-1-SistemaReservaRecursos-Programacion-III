package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.common.modelo.EstadisticaItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Prueba de UNIDAD (sin MySQL): validacion del rango y relleno de semanas con 0. */
class EstadisticaServiceTest {

    @Test
    void desdeMayorQueHastaLanzaExcepcion() {
        EstadisticaService service = new EstadisticaService();
        LocalDate desde = LocalDate.of(2026, 11, 30);
        LocalDate hasta = LocalDate.of(2026, 10, 1);

        Exception e1 = assertThrows(Exception.class, () -> service.estadisticasRecursos(desde, hasta));
        assertEquals("La fecha desde no puede ser posterior a la fecha hasta.", e1.getMessage());
        assertThrows(Exception.class, () -> service.estadisticasActividades(desde, hasta));
    }

    @Test
    void fechasNulasLanzanExcepcion() {
        EstadisticaService service = new EstadisticaService();

        assertThrows(Exception.class, () -> service.estadisticasRecursos(null, LocalDate.now()));
        assertThrows(Exception.class, () -> service.estadisticasActividades(LocalDate.now(), null));
    }

    @Test
    void periodoDemasiadoLargoLanzaExcepcion() {
        EstadisticaService service = new EstadisticaService();

        assertThrows(Exception.class, () -> service.estadisticasActividades(
                LocalDate.of(1900, 1, 1), LocalDate.of(2026, 1, 1)));
    }

    @Test
    void completarSemanasPoneCeroEnLasSemanasSinActividades() {
        // 1 oct 2026 (jueves) a 30 nov 2026 (lunes) -> 10 semanas, de la que empieza el 28 sep a la del 30 nov
        List<EstadisticaItem> deMySql = List.of(
                new EstadisticaItem("2026-10-19", 1),
                new EstadisticaItem("2026-11-02", 3));

        List<EstadisticaItem> r = EstadisticaService.completarSemanas(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 30), deMySql);

        assertEquals(10, r.size());
        assertEquals("2026-09-28", r.get(0).getEtiqueta());
        assertEquals(0, r.get(0).getCantidad());
        assertEquals("2026-10-19", r.get(3).getEtiqueta());
        assertEquals(1, r.get(3).getCantidad());
        assertEquals("2026-11-02", r.get(5).getEtiqueta());
        assertEquals(3, r.get(5).getCantidad());
        assertEquals("2026-11-30", r.get(9).getEtiqueta());
        assertEquals(0, r.get(9).getCantidad());
    }

    @Test
    void unPeriodoDentroDeUnaSolaSemanaDevuelveUnaFila() {
        List<EstadisticaItem> r = EstadisticaService.completarSemanas(
                LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 5), List.of());

        assertEquals(1, r.size());
        assertEquals("2026-11-02", r.get(0).getEtiqueta());
        assertEquals(0, r.get(0).getCantidad());
    }
}
