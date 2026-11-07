package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.common.modelo.EstadisticaItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de INTEGRACION de Estadisticas contra MySQL (GROUP BY reales).
 * Con schema.sql recien ejecutado y el periodo 2026-10-01 a 2026-11-30.
 */
class EstadisticaServiceIT {

    private static final LocalDate DESDE = LocalDate.of(2026, 10, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 11, 30);

    private final EstadisticaService service = new EstadisticaService();

    private static int cantidadDe(List<EstadisticaItem> items, String etiqueta) {
        return items.stream().filter(i -> i.getEtiqueta().equals(etiqueta))
                .mapToInt(EstadisticaItem::getCantidad).findFirst().orElse(0);
    }

    @Test
    void recursosPorCategoriaCuentaSoloReservasActivas() throws Exception {
        List<EstadisticaItem> r = service.estadisticasRecursos(DESDE, HASTA);

        // Salas: reservas 1, 3 y 5. La reserva 2 esta cancelada y es de otra categoria.
        assertEquals(3, cantidadDe(r, "Sala para 10 personas"));
        assertEquals(2, cantidadDe(r, "Laptop windows 11"));     // reservas 3 y 6
        assertEquals(1, cantidadDe(r, "Sala de Juntas"));        // solo la 4 (la 2 esta cancelada)
        assertEquals(1, cantidadDe(r, "Proyector"));
        assertEquals(1, cantidadDe(r, "Microfono inalambrico"));
        assertEquals("Sala para 10 personas", r.get(0).getEtiqueta());   // la mayor primero
    }

    @Test
    void recursosPorCategoriaRespetaElPeriodo() throws Exception {
        // solo el 3 de noviembre: la reserva 5 (Sala 2)
        List<EstadisticaItem> r = service.estadisticasRecursos(LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 3));

        assertEquals(1, r.size());
        assertEquals("Sala para 10 personas", r.get(0).getEtiqueta());
        assertEquals(1, r.get(0).getCantidad());
    }

    @Test
    void periodoSinReservasDevuelveListaVacia() throws Exception {
        assertTrue(service.estadisticasRecursos(LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 31)).isEmpty());
    }

    @Test
    void actividadesPorSemanaIncluyeLasSemanasEnCero() throws Exception {
        List<EstadisticaItem> r = service.estadisticasActividades(DESDE, HASTA);

        assertEquals(10, r.size());                              // 28 sep ... 30 nov
        assertEquals(0, cantidadDe(r, "2026-09-28"));
        assertEquals(1, cantidadDe(r, "2026-10-19"));            // reserva 1 (la 2 esta cancelada)
        assertEquals(1, cantidadDe(r, "2026-10-26"));            // reserva 3
        assertEquals(3, cantidadDe(r, "2026-11-02"));            // reservas 4, 5 y 6
        assertEquals(0, cantidadDe(r, "2026-11-30"));
        assertEquals(5, r.stream().mapToInt(EstadisticaItem::getCantidad).sum());   // 5 reservas activas
    }
}
