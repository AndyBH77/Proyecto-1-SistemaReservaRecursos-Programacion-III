package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.backend.datos.EstadisticaDAO;
import cr.ac.una.eif206.common.modelo.EstadisticaItem;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Estadisticas por periodo (JORDI). Los GROUP BY los hace MySQL
 * (EstadisticaDAO); aqui solo se valida el rango y se completan las semanas
 * sin actividades con 0.
 */
public class EstadisticaService {

    /** Para que nadie pida un periodo absurdo (millones de semanas por el socket). */
    static final long MAX_DIAS = 3660;   // ~10 anios

    private final EstadisticaDAO dao = new EstadisticaDAO();

    /** Categorias reservadas en el periodo y la cantidad de cada una. */
    public List<EstadisticaItem> estadisticasRecursos(LocalDate desde, LocalDate hasta)
            throws SQLException {
        validarRango(desde, hasta);
        return dao.recursosPorCategoria(desde, hasta);
    }

    /**
     * Una fila por CADA semana del periodo (lunes a domingo), incluidas las
     * semanas sin actividades, que salen con 0. La etiqueta es el lunes (yyyy-MM-dd).
     */
    public List<EstadisticaItem> estadisticasActividades(LocalDate desde, LocalDate hasta)
            throws SQLException {
        validarRango(desde, hasta);
        return completarSemanas(desde, hasta, dao.actividadesPorSemana(desde, hasta));
    }

    /**
     * Recorre desde el lunes de "desde" hasta el lunes de "hasta", semana por
     * semana, y toma la cantidad del resultado de MySQL o 0 si no hubo.
     * Es estatico y sin BD para poder probarlo con una prueba unitaria.
     */
    static List<EstadisticaItem> completarSemanas(LocalDate desde, LocalDate hasta,
                                                  List<EstadisticaItem> deMySql) {
        Map<String, Integer> cantidades = new HashMap<>();
        for (EstadisticaItem item : deMySql) {
            cantidades.put(item.getEtiqueta(), item.getCantidad());
        }

        List<EstadisticaItem> resultado = new ArrayList<>();
        LocalDate semana = desde.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate ultima = hasta.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        while (!semana.isAfter(ultima)) {
            String etiqueta = semana.toString();
            resultado.add(new EstadisticaItem(etiqueta, cantidades.getOrDefault(etiqueta, 0)));
            semana = semana.plusWeeks(1);
        }
        return resultado;
    }

    static void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debe indicar las fechas desde y hasta.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) > MAX_DIAS) {
            throw new IllegalArgumentException("El periodo no puede ser mayor a 10 anios.");
        }
    }
}
