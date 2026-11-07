package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.backend.datos.CategoriaRecursoDAO;
import cr.ac.una.eif206.backend.datos.RecursoDAO;
import cr.ac.una.eif206.backend.datos.ReservaDAO;
import cr.ac.una.eif206.common.modelo.Recurso;
import cr.ac.una.eif206.common.modelo.Reserva;
import cr.ac.una.eif206.common.modelo.TablaConsulta;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Arma las matrices de Calendarizacion y Actividades (JORDI).
 *
 * Cada pantalla hace UNA sola consulta a MySQL (no una por celda) y despues
 * llena la matriz en memoria. Los metodos "construir..." son estaticos y no
 * tocan la BD, para poder probarlos con pruebas unitarias.
 *
 * Solo se consideran reservas ACTIVAS (ReservaDAO.listarActivasEntre).
 */
public class ConsultaService {

    /** Las filas de la matriz van, como minimo, de las 06:00 a las 22:00. */
    public static final int HORA_INICIAL = 6;
    public static final int HORA_FINAL = 22;

    private static final String[] DIAS = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final RecursoDAO recursoDAO = new RecursoDAO();
    private final CategoriaRecursoDAO categoriaDAO = new CategoriaRecursoDAO();

    // ==================================================================
    // Con base de datos
    // ==================================================================

    /** Matriz horas x recursos de una categoria, para una fecha. */
    public TablaConsulta calendarizacion(LocalDate fecha, int idCategoria) throws Exception {
        if (fecha == null) {
            throw new Exception("Debe indicar la fecha.");
        }
        if (categoriaDAO.buscarPorId(idCategoria).isEmpty()) {
            throw new Exception("La categoria seleccionada no existe.");
        }
        List<Recurso> recursos = recursoDAO.listarPorCategoria(idCategoria);
        List<Reserva> reservas = reservaDAO.listarActivasEntre(fecha, fecha);   // UNA consulta
        return construirCalendarizacion(fecha, recursos, reservas);
    }

    /** Matriz horas x dias (lunes a domingo) de la semana que contiene la fecha. */
    public TablaConsulta programacionSemanal(LocalDate cualquierDia) throws Exception {
        if (cualquierDia == null) {
            throw new Exception("Debe indicar la fecha.");
        }
        LocalDate lunes = inicioSemana(cualquierDia);
        List<Reserva> reservas = reservaDAO.listarActivasEntre(lunes, lunes.plusDays(6));   // UNA consulta
        return construirProgramacionSemanal(lunes, reservas);
    }

    // ==================================================================
    // Sin base de datos (se prueban con pruebas unitarias)
    // ==================================================================

    public static LocalDate inicioSemana(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Fila = hora, columna = recurso. Cada celda dice "Disponible" o
     * "Actividad - Funcionario" (si hubiera mas de una reserva en la misma
     * hora, por ejemplo 8:00-8:30 y 8:30-9:00, salen una debajo de la otra).
     * Una categoria sin recursos devuelve solo la columna "Hora" y 0 filas.
     */
    public static TablaConsulta construirCalendarizacion(LocalDate fecha, List<Recurso> recursos,
                                                         List<Reserva> reservas) {
        if (recursos.isEmpty()) {
            return new TablaConsulta(new String[]{"Hora"}, new String[0][1]);
        }
        List<Reserva> delDia = reservas.stream().filter(r -> fecha.equals(r.getFecha())).toList();
        int[] rango = rangoDeHoras(delDia);

        String[] encabezados = new String[recursos.size() + 1];
        encabezados[0] = "Hora";
        for (int i = 0; i < recursos.size(); i++) {
            encabezados[i + 1] = recursos.get(i).getDescripcion();
        }

        String[][] filas = new String[rango[1] - rango[0] + 1][recursos.size() + 1];
        for (int hora = rango[0]; hora <= rango[1]; hora++) {
            String[] fila = filas[hora - rango[0]];
            fila[0] = String.format("%02d:00", hora);
            for (int c = 0; c < recursos.size(); c++) {
                String idRecurso = recursos.get(c).getId();
                List<String> textos = new ArrayList<>();
                for (Reserva reserva : delDia) {
                    boolean usaElRecurso = reserva.getRecursos().stream()
                            .anyMatch(r -> r.getId().equals(idRecurso));
                    if (usaElRecurso && seCruzaConLaHora(reserva, hora)) {
                        textos.add(textoDe(reserva));
                    }
                }
                fila[c + 1] = textos.isEmpty() ? "Disponible" : String.join("\n", textos);
            }
        }
        return new TablaConsulta(encabezados, filas);
    }

    /**
     * Fila = hora, columna = dia de la semana (lunes a domingo). Cada celda
     * lleva todas las actividades de ese dia que se cruzan con esa hora, como
     * "Actividad - Funcionario", una por linea; vacia si no hay ninguna.
     */
    public static TablaConsulta construirProgramacionSemanal(LocalDate lunes, List<Reserva> reservas) {
        List<Reserva> delaSemana = reservas.stream()
                .filter(r -> r.getFecha() != null && !r.getFecha().isBefore(lunes)
                        && !r.getFecha().isAfter(lunes.plusDays(6)))
                .toList();
        int[] rango = rangoDeHoras(delaSemana);

        String[] encabezados = new String[8];
        encabezados[0] = "Hora";
        for (int d = 0; d < 7; d++) {
            LocalDate dia = lunes.plusDays(d);
            encabezados[d + 1] = String.format("%s %02d/%02d", DIAS[d], dia.getDayOfMonth(), dia.getMonthValue());
        }

        String[][] filas = new String[rango[1] - rango[0] + 1][8];
        for (int hora = rango[0]; hora <= rango[1]; hora++) {
            String[] fila = filas[hora - rango[0]];
            fila[0] = String.format("%02d:00", hora);
            for (int d = 0; d < 7; d++) {
                LocalDate dia = lunes.plusDays(d);
                List<String> textos = new ArrayList<>();
                for (Reserva reserva : delaSemana) {
                    if (dia.equals(reserva.getFecha()) && seCruzaConLaHora(reserva, hora)) {
                        textos.add(textoDe(reserva));
                    }
                }
                fila[d + 1] = String.join("\n", textos);
            }
        }
        return new TablaConsulta(encabezados, filas);
    }

    /**
     * La celda de la hora H cubre [H:00, H+1:00). La reserva se muestra ahi si
     * los rangos se cruzan (igual regla que Reserva.seSolapaCon): una reserva
     * de 8:00 a 10:00 aparece en las filas 8 y 9, pero no en la 10.
     */
    static boolean seCruzaConLaHora(Reserva reserva, int hora) {
        // Se compara en segundos del dia (y no con LocalTime.plusHours) porque
        // a las 23:00 + 1 hora, LocalTime da la vuelta al dia y queda 00:00.
        int inicioCelda = hora * 3600;
        int finCelda = (hora + 1) * 3600;
        return reserva.getHoraInicio().toSecondOfDay() < finCelda
                && inicioCelda < reserva.getHoraFin().toSecondOfDay();
    }

    /**
     * Rango de horas de la matriz: 6 a 22 como minimo, pero se amplia si hay
     * reservas fuera de ese horario (el sistema permite reservar a cualquier
     * hora y asi nunca queda una reserva "escondida" sin fila).
     */
    static int[] rangoDeHoras(List<Reserva> reservas) {
        int desde = HORA_INICIAL;
        int hasta = HORA_FINAL;
        for (Reserva r : reservas) {
            desde = Math.min(desde, r.getHoraInicio().getHour());
            // una reserva que termina a las 23:00 en punto llena hasta la fila de las 22
            hasta = Math.max(hasta, r.getHoraFin().minusNanos(1).getHour());
        }
        return new int[]{desde, hasta};
    }

    private static String textoDe(Reserva reserva) {
        return reserva.getActividad() + " - " + reserva.getNombreFuncionario();
    }
}
