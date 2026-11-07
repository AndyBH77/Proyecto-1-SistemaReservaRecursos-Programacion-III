package cr.ac.una.eif206.common.modelo;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de UNIDAD (Surefire) del metodo seSolapaCon de Reserva.
 */
class ReservaTest {

    private final LocalDate dia = LocalDate.of(2026, 11, 2);

    private Reserva reservaDe8a10() {
        return new Reserva("111", "Reunion", dia, LocalTime.of(8, 0), LocalTime.of(10, 0));
    }

    @Test
    void seSolapaCuandoLosHorariosSeCruzan() {
        assertTrue(reservaDe8a10().seSolapaCon(dia, LocalTime.of(9, 0), LocalTime.of(11, 0)));
    }

    @Test
    void noSeSolapaSiUnaTerminaCuandoEmpiezaLaOtra() {
        assertFalse(reservaDe8a10().seSolapaCon(dia, LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    @Test
    void noSeSolapaEnOtroDia() {
        assertFalse(reservaDe8a10().seSolapaCon(dia.plusDays(1), LocalTime.of(8, 0), LocalTime.of(10, 0)));
    }
}