package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.backend.datos.CategoriaRecursoDAO;
import cr.ac.una.eif206.backend.datos.ReservaDAO;
import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import cr.ac.una.eif206.common.modelo.EstadoReserva;
import cr.ac.una.eif206.common.modelo.Recurso;
import cr.ac.una.eif206.common.modelo.Reserva;
import cr.ac.una.eif206.common.modelo.ResultadoReserva;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

/**
 * Logica de negocio de Reservas (solo para usuarios FUNCIONARIO).
 *
 * Regla del enunciado: la reserva tiene exito si hay al menos un recurso
 * libre de CADA categoria pedida; se asigna el primer recurso disponible de
 * cada una. Si no, se informa que categorias no tenian disponibilidad.
 */
public class ReservaService {

    /**
     * CANDADO compartido por todos los hilos del servidor.
     * Si dos funcionarios reservan al mismo tiempo, se atienden uno por uno;
     * asi nunca se asigna el mismo recurso dos veces en el mismo horario.
     * (Es static porque cada cliente conectado tiene su propio ReservaService.)
     */
    private static final Object CANDADO = new Object();

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final CategoriaRecursoDAO categoriaDAO = new CategoriaRecursoDAO();

    public List<Reserva> listarDe(String idFuncionario) throws SQLException {
        return reservaDAO.listarPorFuncionario(idFuncionario);
    }

    public ResultadoReserva crear(String idFuncionario, String actividad, LocalDate fecha,
                                  LocalTime horaInicio, LocalTime horaFin, List<Integer> idsCategorias) throws Exception {
        validar(actividad, fecha, horaInicio, horaFin, idsCategorias);

        synchronized (CANDADO) {
            Reserva reserva = new Reserva(idFuncionario, actividad.trim(), fecha, horaInicio, horaFin);
            return asignarRecursosYGuardar(reserva, idsCategorias, true);
        }
    }

    public ResultadoReserva modificar(int idReserva, String idFuncionario, String actividad, LocalDate fecha,
                                      LocalTime horaInicio, LocalTime horaFin, List<Integer> idsCategorias) throws Exception {
        validar(actividad, fecha, horaInicio, horaFin, idsCategorias);

        synchronized (CANDADO) {
            Reserva reserva = buscarPropia(idReserva, idFuncionario);
            if (reserva.getEstado() == EstadoReserva.CANCELADA) {
                throw new Exception("No se puede modificar una reserva cancelada.");
            }

            reserva.setActividad(actividad.trim());
            reserva.setFecha(fecha);
            reserva.setHoraInicio(horaInicio);
            reserva.setHoraFin(horaFin);
            return asignarRecursosYGuardar(reserva, idsCategorias, false);
        }
    }

    /** Cancela una reserva FUTURA, liberando sus recursos. */
    public void cancelar(int idReserva, String idFuncionario) throws Exception {
        synchronized (CANDADO) {
            Reserva reserva = buscarPropia(idReserva, idFuncionario);

            if (reserva.getEstado() == EstadoReserva.CANCELADA) {
                throw new Exception("La reserva ya estaba cancelada.");
            }
            if (reserva.getFecha().isBefore(LocalDate.now())) {
                throw new Exception("No se puede cancelar una reserva que ya paso.");
            }
            reservaDAO.actualizarEstado(idReserva, EstadoReserva.CANCELADA);
        }
    }

    // ------------------------------------------------------------------

    private ResultadoReserva asignarRecursosYGuardar(Reserva reserva, List<Integer> idsCategorias,
                                                     boolean esNueva) throws SQLException {
        List<Recurso> asignados = new ArrayList<>();
        List<String> noDisponibles = new ArrayList<>();
        int idExcluido = esNueva ? 0 : reserva.getId();

        // LinkedHashSet quita categorias repetidas y conserva el orden
        for (int idCategoria : new LinkedHashSet<>(idsCategorias)) {
            Optional<Recurso> libre = reservaDAO.buscarRecursoLibre(idCategoria, reserva.getFecha(),
                    reserva.getHoraInicio(), reserva.getHoraFin(), idExcluido);

            if (libre.isPresent()) {
                asignados.add(libre.get());
            } else {
                noDisponibles.add(categoriaDAO.buscarPorId(idCategoria)
                        .map(CategoriaRecurso::getDescripcion)
                        .orElse("Categoria " + idCategoria));
            }
        }

        if (!noDisponibles.isEmpty()) {
            return ResultadoReserva.sinDisponibilidad(noDisponibles);   // no se guarda nada
        }

        reserva.setRecursos(asignados);
        reserva.setEstado(EstadoReserva.ACTIVA);
        if (esNueva) {
            reservaDAO.insertar(reserva);
        } else {
            reservaDAO.actualizar(reserva);
        }

        // Se vuelve a leer de la BD para devolverla completa (con nombre del funcionario)
        return ResultadoReserva.exitosa(reservaDAO.buscarPorId(reserva.getId()).orElse(reserva));
    }

    private Reserva buscarPropia(int idReserva, String idFuncionario) throws Exception {
        Reserva reserva = reservaDAO.buscarPorId(idReserva)
                .orElseThrow(() -> new Exception("La reserva ya no existe."));
        if (!reserva.getIdFuncionario().equals(idFuncionario)) {
            throw new Exception("Solo puede modificar o cancelar sus propias reservas.");
        }
        return reserva;
    }

    private void validar(String actividad, LocalDate fecha, LocalTime horaInicio,
                         LocalTime horaFin, List<Integer> idsCategorias) throws Exception {
        if (actividad == null || actividad.isBlank()) {
            throw new Exception("Debe indicar el nombre de la actividad.");
        }
        if (actividad.trim().length() > 150) {
            throw new Exception("El nombre de la actividad no puede pasar de 150 caracteres.");
        }
        if (fecha == null) {
            throw new Exception("Debe indicar la fecha de la actividad.");
        }
        if (fecha.isBefore(LocalDate.now())) {
            throw new Exception("No se puede reservar en una fecha que ya paso.");
        }
        if (horaInicio == null || horaFin == null) {
            throw new Exception("Debe indicar la hora de inicio y la de finalizacion.");
        }
        if (!horaInicio.isBefore(horaFin)) {
            throw new Exception("La hora de inicio debe ser anterior a la hora de finalizacion.");
        }
        if (idsCategorias == null || idsCategorias.isEmpty()) {
            throw new Exception("Debe seleccionar al menos una categoria de recurso.");
        }
    }
}