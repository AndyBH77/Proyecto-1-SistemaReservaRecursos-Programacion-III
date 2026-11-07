package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.common.modelo.Recurso;
import cr.ac.una.eif206.common.modelo.Reserva;
import cr.ac.una.eif206.common.modelo.TablaConsulta;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de UNIDAD (sin MySQL) de las matrices de Calendarizacion y Actividades.
 * Se prueban los metodos estaticos "construir...", que reciben las listas ya armadas.
 */
class ConsultaServiceTest {

    // lunes 2 de noviembre de 2026
    private static final LocalDate LUNES = LocalDate.of(2026, 11, 2);

    private static Recurso recurso(String id, String descripcion) {
        return new Recurso(id, 1, descripcion);
    }

    private static Reserva reserva(String actividad, String funcionario, LocalDate fecha,
                                   int horaIni, int minIni, int horaFin, int minFin, Recurso... recursos) {
        Reserva r = new Reserva("111", actividad, fecha, LocalTime.of(horaIni, minIni), LocalTime.of(horaFin, minFin));
        r.setNombreFuncionario(funcionario);
        r.setRecursos(List.of(recursos));
        return r;
    }

    /** Busca la fila de una hora (ej. "08:00") y devuelve el texto de la columna indicada. */
    private static String celda(TablaConsulta t, String hora, int columna) {
        for (String[] fila : t.getFilas()) {
            if (fila[0].equals(hora)) {
                return fila[columna];
            }
        }
        throw new AssertionError("No hay fila para la hora " + hora);
    }

    // ---------------- Calendarizacion ----------------

    @Test
    void calendarizacionMarcaReservadoYDisponible() {
        Recurso sala1 = recurso("A", "Sala 1");
        Recurso sala2 = recurso("B", "Sala 2");
        Reserva r = reserva("Reunion clientes", "Maria Mata", LUNES, 8, 0, 10, 0, sala1);

        TablaConsulta t = ConsultaService.construirCalendarizacion(LUNES, List.of(sala1, sala2), List.of(r));

        assertArrayEquals(new String[]{"Hora", "Sala 1", "Sala 2"}, t.getEncabezados());
        assertEquals("Reunion clientes - Maria Mata", celda(t, "08:00", 1));
        assertEquals("Reunion clientes - Maria Mata", celda(t, "09:00", 1));
        assertEquals("Disponible", celda(t, "08:00", 2));     // la otra sala sigue libre
    }

    @Test
    void reservaQueTerminaALaHoraEnPuntoNoOcupaEsaFila() {
        Recurso sala = recurso("A", "Sala 1");
        Reserva r = reserva("Reunion", "Maria", LUNES, 8, 0, 10, 0, sala);

        TablaConsulta t = ConsultaService.construirCalendarizacion(LUNES, List.of(sala), List.of(r));

        assertEquals("Disponible", celda(t, "10:00", 1));     // 8-10 y 10-11 no se cruzan
        assertEquals("Disponible", celda(t, "07:00", 1));
    }

    @Test
    void reservaDeMediaHoraOcupaLaFilaCompleta() {
        Recurso sala = recurso("A", "Sala 1");
        Reserva r = reserva("Llamada", "Pedro", LUNES, 10, 0, 10, 30, sala);

        TablaConsulta t = ConsultaService.construirCalendarizacion(LUNES, List.of(sala), List.of(r));

        assertEquals("Llamada - Pedro", celda(t, "10:00", 1));
    }

    @Test
    void ignoraReservasDeOtraFecha() {
        Recurso sala = recurso("A", "Sala 1");
        Reserva otroDia = reserva("Otra", "Ana", LUNES.plusDays(1), 8, 0, 9, 0, sala);

        TablaConsulta t = ConsultaService.construirCalendarizacion(LUNES, List.of(sala), List.of(otroDia));

        assertEquals("Disponible", celda(t, "08:00", 1));
    }

    @Test
    void dosReservasEnLaMismaHoraSalenUnaDebajoDeLaOtra() {
        Recurso sala = recurso("A", "Sala 1");
        Reserva a = reserva("Primera", "Ana", LUNES, 8, 0, 8, 30, sala);
        Reserva b = reserva("Segunda", "Beto", LUNES, 8, 30, 9, 0, sala);

        TablaConsulta t = ConsultaService.construirCalendarizacion(LUNES, List.of(sala), List.of(a, b));

        assertEquals("Primera - Ana\nSegunda - Beto", celda(t, "08:00", 1));
    }

    @Test
    void categoriaSinRecursosDevuelveSoloLaColumnaHora() {
        TablaConsulta t = ConsultaService.construirCalendarizacion(LUNES, List.of(), List.of());

        assertEquals(1, t.getCantidadColumnas());
        assertEquals(0, t.getCantidadFilas());
    }

    @Test
    void lasFilasVanDe6a22YSeAmplianSiHayReservasFueraDeHorario() {
        Recurso sala = recurso("A", "Sala 1");

        TablaConsulta normal = ConsultaService.construirCalendarizacion(LUNES, List.of(sala), List.of());
        assertEquals("06:00", normal.getFilas()[0][0]);
        assertEquals("22:00", normal.getFilas()[normal.getCantidadFilas() - 1][0]);

        Reserva temprano = reserva("Madrugada", "Ana", LUNES, 5, 0, 6, 30, sala);
        Reserva tarde = reserva("Noche", "Beto", LUNES, 23, 0, 23, 45, sala);
        TablaConsulta ampliada = ConsultaService.construirCalendarizacion(LUNES, List.of(sala), List.of(temprano, tarde));
        assertEquals("05:00", ampliada.getFilas()[0][0]);
        assertEquals("23:00", ampliada.getFilas()[ampliada.getCantidadFilas() - 1][0]);
        assertEquals("Madrugada - Ana", celda(ampliada, "05:00", 1));
        assertEquals("Noche - Beto", celda(ampliada, "23:00", 1));
    }

    // ---------------- Programacion semanal ----------------

    @Test
    void semanaTiene7DiasConEncabezadosYTodasLasHoras() {
        TablaConsulta t = ConsultaService.construirProgramacionSemanal(LUNES, List.of());

        assertArrayEquals(new String[]{"Hora", "Lun 02/11", "Mar 03/11", "Mié 04/11", "Jue 05/11",
                "Vie 06/11", "Sáb 07/11", "Dom 08/11"}, t.getEncabezados());
        assertEquals(17, t.getCantidadFilas());               // 06:00 a 22:00
    }

    @Test
    void semanaPoneLaActividadEnSuDiaYSuHora() {
        Reserva martes = reserva("Cafe de cumpleanos", "Pedro Arias", LUNES.plusDays(1), 15, 0, 16, 0);

        TablaConsulta t = ConsultaService.construirProgramacionSemanal(LUNES, List.of(martes));

        assertEquals("Cafe de cumpleanos - Pedro Arias", celda(t, "15:00", 2));   // columna 2 = martes
        assertEquals("", celda(t, "15:00", 1));                                  // lunes vacio
        assertEquals("", celda(t, "16:00", 2));                                  // ya termino
    }

    @Test
    void semanaJuntaLasActividadesQueCoincidenEnLaMismaCelda() {
        Reserva a = reserva("Capacitacion", "Juan Mora", LUNES, 13, 0, 16, 0);
        Reserva b = reserva("Revision", "Maria Mata", LUNES, 14, 0, 15, 0);

        TablaConsulta t = ConsultaService.construirProgramacionSemanal(LUNES, List.of(a, b));

        assertEquals("Capacitacion - Juan Mora", celda(t, "13:00", 1));
        assertEquals("Capacitacion - Juan Mora\nRevision - Maria Mata", celda(t, "14:00", 1));
    }

    @Test
    void semanaIgnoraReservasDeOtraSemana() {
        Reserva siguiente = reserva("Futura", "Ana", LUNES.plusDays(7), 9, 0, 10, 0);

        TablaConsulta t = ConsultaService.construirProgramacionSemanal(LUNES, List.of(siguiente));

        for (String[] fila : t.getFilas()) {
            for (int c = 1; c < fila.length; c++) {
                assertTrue(fila[c].isEmpty());
            }
        }
    }

    @Test
    void inicioSemanaSiempreEsLunes() {
        assertEquals(LUNES, ConsultaService.inicioSemana(LUNES));
        assertEquals(LUNES, ConsultaService.inicioSemana(LocalDate.of(2026, 11, 8)));    // domingo
        assertEquals(LUNES, ConsultaService.inicioSemana(LocalDate.of(2026, 11, 4)));    // miercoles
    }
}
