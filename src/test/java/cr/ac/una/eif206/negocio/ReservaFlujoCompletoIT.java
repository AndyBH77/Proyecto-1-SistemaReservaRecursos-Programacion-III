package cr.ac.una.eif206.negocio;

import cr.ac.una.eif206.negocio.ReservaService.ResultadoReserva;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de INTEGRACION (la ejecuta Failsafe con "mvn verify"): usa
 * ReservaService completo junto con los DAO reales, que leen y escriben
 * archivos XML de verdad en la carpeta "data/", a diferencia de una prueba
 * de unidad que probaria una sola pieza aislada.
 */
class ReservaFlujoCompletoIT {

    @Test
    void seCreaUnaReservaConCategoriaDeEjemplo() throws Exception {
        ReservaService servicio = new ReservaService();

        ResultadoReserva resultado = servicio.crearReserva(
                "999", // id de funcionario de prueba
                "Actividad de prueba (IT)",
                LocalDate.now().plusDays(10),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                List.of("CAT-000002") // Laptop windows (categoria semilla)
        );

        assertTrue(resultado.exito, "La reserva deberia poder crearse con la categoria de ejemplo");
        assertNotNull(resultado.reserva);

        // limpieza: se elimina la reserva de prueba para no dejar basura
        servicio.eliminarReserva(resultado.reserva.getId());
    }
}
