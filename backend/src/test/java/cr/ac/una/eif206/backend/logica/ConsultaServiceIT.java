package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import cr.ac.una.eif206.common.modelo.TablaConsulta;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Prueba de INTEGRACION de Calendarizacion y Actividades contra MySQL (datos de schema.sql). */
class ConsultaServiceIT {

    private final ConsultaService service = new ConsultaService();

    private static String celda(TablaConsulta t, String hora, int columna) {
        for (String[] fila : t.getFilas()) {
            if (fila[0].equals(hora)) {
                return fila[columna];
            }
        }
        throw new AssertionError("No hay fila para " + hora);
    }

    private int idCategoria(String descripcion) throws Exception {
        return new CategoriaService().listarTodas().stream()
                .filter(c -> c.getDescripcion().equals(descripcion))
                .map(CategoriaRecurso::getId).findFirst()
                .orElseThrow(() -> new AssertionError("Ejecute schema.sql: falta " + descripcion));
    }

    @Test
    void calendarizacionDeSalasElDia30DeOctubre() throws Exception {
        // reserva 3: "Reunion clientes" de Maria Mata, 08:00-10:00, usa la Sala 1 (34343)
        TablaConsulta t = service.calendarizacion(LocalDate.of(2026, 10, 30), idCategoria("Sala para 10 personas"));

        assertArrayEquals(new String[]{"Hora", "Sala 1 primer piso", "Sala 2 segundo piso"}, t.getEncabezados());
        assertEquals("Reunion clientes - Maria Mata", celda(t, "08:00", 1));
        assertEquals("Reunion clientes - Maria Mata", celda(t, "09:00", 1));
        assertEquals("Disponible", celda(t, "10:00", 1));
        assertEquals("Disponible", celda(t, "08:00", 2));        // la Sala 2 esta libre
    }

    @Test
    void calendarizacionNoMuestraReservasCanceladas() throws Exception {
        // reserva 2 (cancelada) era Sala de Juntas el 21 de octubre, 08:00-09:00
        TablaConsulta t = service.calendarizacion(LocalDate.of(2026, 10, 21), idCategoria("Sala de Juntas"));

        assertEquals("Disponible", celda(t, "08:00", 1));
    }

    @Test
    void calendarizacionConCategoriaInexistenteLanzaExcepcion() {
        assertThrows(Exception.class, () -> service.calendarizacion(LocalDate.of(2026, 11, 2), 99999));
    }

    @Test
    void programacionSemanalDeLaSemanaDel2DeNoviembre() throws Exception {
        // pedimos con un miercoles: debe devolver la semana lun 02/11 a dom 08/11
        TablaConsulta t = service.programacionSemanal(LocalDate.of(2026, 11, 4));

        assertEquals("Lun 02/11", t.getEncabezados()[1]);
        assertEquals("Dom 08/11", t.getEncabezados()[7]);
        assertEquals("Capacitacion - Juan Mora", celda(t, "13:00", 1));          // lunes 13-16
        assertEquals("Capacitacion - Juan Mora", celda(t, "15:00", 1));
        assertEquals("", celda(t, "16:00", 1));
        assertEquals("Cafe de cumpleanos - Pedro Arias", celda(t, "15:00", 2));   // martes 15-16
        assertEquals("Coordinacion viaje - Ana Lopez", celda(t, "09:00", 3));     // miercoles 09-11
        assertEquals("Coordinacion viaje - Ana Lopez", celda(t, "10:00", 3));
    }
}
