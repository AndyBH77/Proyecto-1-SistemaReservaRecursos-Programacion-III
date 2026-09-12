package cr.ac.una.eif206.negocio;
import cr.ac.una.eif206.modelo.CategoriaRecurso;
import cr.ac.una.eif206.modelo.EstadoReserva;
import cr.ac.una.eif206.modelo.Funcionario;
import cr.ac.una.eif206.modelo.Recurso;
import cr.ac.una.eif206.modelo.Reserva;
import cr.ac.una.eif206.persistencia.CategoriaRecursoDAO;
import cr.ac.una.eif206.persistencia.FuncionarioDAO;
import cr.ac.una.eif206.persistencia.RecursoDAO;
import cr.ac.una.eif206.persistencia.ReservaDAO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Logica de consulta compartida por las tres pantallas. **/
public class ConsultaService {

    public static final int HORA_INICIAL = 6;
    public static final int HORA_FINAL = 22;

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final RecursoDAO recursoDAO = new RecursoDAO();
    private final CategoriaRecursoDAO categoriaDAO = new CategoriaRecursoDAO();
    private final FuncionarioDAO funcionarioDAO = new FuncionarioDAO();

    public List<CategoriaRecurso> listarCategorias() {
        return categoriaDAO.listarTodas();
    } // solicita al DAO todas las categorías disponibles para llenar el combo de Calendarización.

    public List<Recurso> listarRecursos(String idCategoria) {
        return recursoDAO.listarPorCategoria(idCategoria);
    }//

    public Object[][] crearCalendarizacion(LocalDate fecha, String idCategoria) {
        List<Recurso> recursos = listarRecursos(idCategoria);
        Object[][] datos = new Object[HORA_FINAL - HORA_INICIAL + 1][recursos.size() + 1];
        for (int hora = HORA_INICIAL; hora <= HORA_FINAL; hora++) {
            int fila = hora - HORA_INICIAL;
            datos[fila][0] = String.format("%02d:00", hora);
            for (int columna = 0; columna < recursos.size(); columna++) {
                datos[fila][columna + 1] = descripcionReserva(fecha, hora, recursos.get(columna).getId());
            }
        }
        return datos;
    }

    public String[] encabezadosCalendarizacion(String idCategoria) {
        List<Recurso> recursos = listarRecursos(idCategoria);
        String[] encabezados = new String[recursos.size() + 1];
        encabezados[0] = "Hora";
        for (int i = 0; i < recursos.size(); i++) {
            encabezados[i + 1] = recursos.get(i).getDescripcion();
        }
        return encabezados;
    }

    public Object[][] crearProgramacionSemanal(LocalDate fechaReferencia) {
        LocalDate lunes = inicioSemana(fechaReferencia);
        Object[][] datos = new Object[HORA_FINAL - HORA_INICIAL + 1][8];
        for (int hora = HORA_INICIAL; hora <= HORA_FINAL; hora++) {
            int fila = hora - HORA_INICIAL;
            datos[fila][0] = String.format("%02d:00", hora);
            for (int dia = 0; dia < 7; dia++) {
                datos[fila][dia + 1] = actividadesDelMomento(lunes.plusDays(dia), hora);
            }
        }
        return datos;
    }

    public String[] encabezadosSemana(LocalDate fechaReferencia) {
        LocalDate lunes = inicioSemana(fechaReferencia);
        String[] nombres = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        String[] encabezados = new String[8];
        encabezados[0] = "Hora";
        for (int i = 0; i < 7; i++) {
            LocalDate fecha = lunes.plusDays(i);
            encabezados[i + 1] = nombres[i] + " " + String.format("%02d/%02d", fecha.getDayOfMonth(), fecha.getMonthValue());
        }
        return encabezados;
    }

    public LocalDate inicioSemana(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public Map<String, Integer> estadisticasRecursos(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        Map<String, Integer> resultado = new LinkedHashMap<>();
        for (Reserva reserva : reservasActivasEnRango(desde, hasta)) {
            for (String idCategoria : reserva.getIdsCategoriasSolicitadas()) {
                String nombre = categoriaDAO.buscarPorId(idCategoria)
                        .map(CategoriaRecurso::getDescripcion).orElse(idCategoria);
                resultado.put(nombre, resultado.getOrDefault(nombre, 0) + 1);
            }
        }
        return resultado;
    }

    public Map<String, Integer> estadisticasActividades(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        Map<String, Integer> resultado = new LinkedHashMap<>();
        LocalDate semana = inicioSemana(desde);
        LocalDate ultimaSemana = inicioSemana(hasta);
        while (!semana.isAfter(ultimaSemana)) {
            resultado.put(semana.toString(), 0);
            semana = semana.plusWeeks(1);
        }
        for (Reserva reserva : reservasActivasEnRango(desde, hasta)) {
            String clave = inicioSemana(reserva.getFechaComoLocalDate()).toString();
            resultado.put(clave, resultado.getOrDefault(clave, 0) + 1);
        }
        return resultado;
    }

    private List<Reserva> reservasActivasEnRango(LocalDate desde, LocalDate hasta) {
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva reserva : reservaDAO.listarTodas()) {
            LocalDate fecha = reserva.getFechaComoLocalDate();
            if (reserva.getEstado() == EstadoReserva.ACTIVA && !fecha.isBefore(desde) && !fecha.isAfter(hasta)) {
                resultado.add(reserva);
            }
        }
        return resultado;
    }

    private String descripcionReserva(LocalDate fecha, int hora, String idRecurso) {
        for (Reserva reserva : reservaDAO.listarTodas()) {
            if (estaActivaEnHora(reserva, fecha, hora) && reserva.getIdsRecursosAsignados().contains(idRecurso)) {
                return reserva.getActividad() + "\n" + nombreFuncionario(reserva.getIdFuncionario());
            }
        }
        return "Disponible";
    }

    private String actividadesDelMomento(LocalDate fecha, int hora) {
        StringBuilder texto = new StringBuilder();
        for (Reserva reserva : reservaDAO.listarTodas()) {
            if (estaActivaEnHora(reserva, fecha, hora)) {
                if (!texto.isEmpty()) texto.append("\n");
                texto.append(reserva.getActividad()).append(" - ").append(nombreFuncionario(reserva.getIdFuncionario()));
            }
        }
        return texto.toString();
    }

    private boolean estaActivaEnHora(Reserva reserva, LocalDate fecha, int hora) {
        if (reserva.getEstado() != EstadoReserva.ACTIVA || !reserva.getFechaComoLocalDate().equals(fecha)) return false;
        LocalTime inicioCelda = LocalTime.of(hora, 0);
        LocalTime finCelda = inicioCelda.plusHours(1);
        return reserva.getHoraInicioComoLocalTime().isBefore(finCelda)
                && inicioCelda.isBefore(reserva.getHoraFinComoLocalTime());
    }

    private String nombreFuncionario(String id) {
        return funcionarioDAO.buscarPorId(id).map(Funcionario::getNombre).orElse(id);
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
    }
}
