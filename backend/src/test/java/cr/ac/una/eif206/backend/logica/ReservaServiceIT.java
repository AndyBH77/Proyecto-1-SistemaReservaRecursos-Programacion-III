package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.backend.datos.ReservaDAO;
import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import cr.ac.una.eif206.common.modelo.ResultadoReserva;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de INTEGRACION de reservas contra MySQL.
 * schema.sql tiene 2 recursos de "Sala para 10 personas" (34343 y 34344).
 */
class ReservaServiceIT {

    private static final LocalDate DIA = LocalDate.of(2030, 1, 15);

    private final ReservaService service = new ReservaService();
    private final ReservaDAO dao = new ReservaDAO();
    private final List<Integer> creadas = new ArrayList<>();
    private int idSalas;

    @BeforeEach
    void buscarCategoriaSalas() throws Exception {
        idSalas = new CategoriaService().listarTodas().stream()
                .filter(c -> c.getDescripcion().equals("Sala para 10 personas"))
                .map(CategoriaRecurso::getId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Ejecute schema.sql: falta 'Sala para 10 personas'"));
    }

    @AfterEach
    void borrarLoCreado() throws Exception {
        for (int id : creadas) {
            dao.eliminar(id);
        }
        creadas.clear();
    }

    private ResultadoReserva reservarSala(String idFuncionario) throws Exception {
        ResultadoReserva r = service.crear(idFuncionario, "Prueba automatica", DIA,
                LocalTime.of(9, 0), LocalTime.of(10, 0), List.of(idSalas));
        if (r.isExito()) {
            creadas.add(r.getReserva().getId());
        }
        return r;
    }

    @Test
    void crearReservaAsignaUnRecursoYTraeElNombre() throws Exception {
        ResultadoReserva r = reservarSala("111");

        assertTrue(r.isExito());
        assertEquals(1, r.getReserva().getRecursos().size());
        assertEquals("Juan Mora", r.getReserva().getNombreFuncionario());
    }

    @Test
    void sinDisponibilidadCuandoSeAcabanLasSalas() throws Exception {
        assertTrue(reservarSala("111").isExito());     // usa la sala 1
        assertTrue(reservarSala("222").isExito());     // usa la sala 2

        ResultadoReserva tercera = reservarSala("333"); // ya no hay

        assertFalse(tercera.isExito());
        assertTrue(tercera.getCategoriasNoDisponibles().contains("Sala para 10 personas"));
    }

    @Test
    void cancelarLiberaElRecurso() throws Exception {
        ResultadoReserva primera = reservarSala("111");
        reservarSala("222");

        service.cancelar(primera.getReserva().getId(), "111");

        assertTrue(reservarSala("333").isExito(), "Al cancelar, la sala deberia quedar libre");
    }

    @Test
    void horaInicioDespuesDeFinLanzaError() {
        assertThrows(Exception.class, () -> service.crear("111", "X", DIA,
                LocalTime.of(10, 0), LocalTime.of(9, 0), List.of(idSalas)));
    }

    @Test
    void noSePuedeCancelarReservaAjena() throws Exception {
        ResultadoReserva r = reservarSala("111");
        assertThrows(Exception.class, () -> service.cancelar(r.getReserva().getId(), "222"));
    }
}