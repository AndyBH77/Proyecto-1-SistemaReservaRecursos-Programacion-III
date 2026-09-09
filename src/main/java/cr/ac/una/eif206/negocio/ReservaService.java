package cr.ac.una.eif206.negocio;

import cr.ac.una.eif206.modelo.CategoriaRecurso;
import cr.ac.una.eif206.modelo.EstadoReserva;
import cr.ac.una.eif206.modelo.Recurso;
import cr.ac.una.eif206.modelo.Reserva;
import cr.ac.una.eif206.persistencia.CategoriaRecursoDAO;
import cr.ac.una.eif206.persistencia.RecursoDAO;
import cr.ac.una.eif206.persistencia.ReservaDAO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Logica de negocio de Reservas: crear, modificar, cancelar y consultar
 * reservas, incluyendo la validacion de disponibilidad de recursos.
 *
 * Criterio de "minimos y maximos" de inventario que se aplica aqui:
 *  - MINIMO: se necesita al menos 1 recurso disponible por cada categoria
 *    solicitada para que la reserva se pueda hacer (asi lo pide el enunciado).
 *  - MAXIMO: nunca se puede asignar mas recursos de los que fisicamente
 *    existen en esa categoria; el maximo posible a asignar es el total de
 *    recursos de esa categoria menos los que ya esten ocupados en ese horario.
 */
public class ReservaService {

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final CategoriaRecursoDAO categoriaDAO = new CategoriaRecursoDAO();
    private final RecursoDAO recursoDAO = new RecursoDAO();

    public List<Reserva> obtenerReservasDe(String idFuncionario) {
        return reservaDAO.listarPorFuncionario(idFuncionario);
    }

    public List<CategoriaRecurso> obtenerCategoriasDisponibles() {
        return categoriaDAO.listarTodas();
    }

    /**
     * Devuelve TODAS las reservas del sistema (de cualquier funcionario).
     * La usa el Administrador, que debe poder ver el listado completo,
     * a diferencia de un Funcionario normal que solo ve las suyas
     * (ver obtenerReservasDe).
     */
    public List<Reserva> obtenerTodasLasReservas() {
        return reservaDAO.listarTodas();
    }

    /**
     * Busca una reserva puntual por su id, sin importar de que funcionario sea.
     */
    public Optional<Reserva> buscarPorId(String idReserva) {
        return reservaDAO.buscarPorId(idReserva);
    }

    /**
     * Resultado de intentar crear/modificar una reserva: indica si tuvo
     * exito y, si no, cuales categorias no tenian disponibilidad.
     *
     * Se dejan los campos publicos y finales a proposito, para mantener
     * esta clase simple (es solo un "paquete" de resultado, no una entidad).
     */
    public static class ResultadoReserva {
        public final boolean exito;
        public final List<String> categoriasNoDisponibles;
        public final Reserva reserva;

        public ResultadoReserva(boolean exito, List<String> categoriasNoDisponibles, Reserva reserva) {
            this.exito = exito;
            this.categoriasNoDisponibles = categoriasNoDisponibles;
            this.reserva = reserva;
        }
    }

    /**
     * Crea una nueva reserva. Por cada categoria solicitada busca el primer
     * recurso de esa categoria que no tenga ya otra reserva activa que se
     * solape con la fecha/hora indicada.
     */
    public ResultadoReserva crearReserva(String idFuncionario, String actividad, LocalDate fecha,
                                         LocalTime horaInicio, LocalTime horaFin, List<String> idsCategorias) throws Exception {

        validarDatosBasicos(actividad, fecha, horaInicio, horaFin, idsCategorias);
        return asignarRecursosYGuardar(null, idFuncionario, actividad, fecha, horaInicio, horaFin, idsCategorias);
    }

    /**
     * Modifica una reserva existente (funcionalidad de "actualizar" del CRUD).
     * Se vuelve a calcular la disponibilidad con los nuevos datos, sin contar
     * la reserva que se esta modificando (para que no "choque contra si misma").
     */
    public ResultadoReserva modificarReserva(String idReserva, String actividad, LocalDate fecha,
                                             LocalTime horaInicio, LocalTime horaFin, List<String> idsCategorias) throws Exception {

        Optional<Reserva> existente = reservaDAO.buscarPorId(idReserva);
        if (existente.isEmpty()) {
            throw new Exception("La reserva " + idReserva + " ya no existe.");
        }

        validarDatosBasicos(actividad, fecha, horaInicio, horaFin, idsCategorias);

        return asignarRecursosYGuardar(existente.get(), existente.get().getIdFuncionario(),
                actividad, fecha, horaInicio, horaFin, idsCategorias);
    }

    /**
     * Cancela una reserva futura, liberando los recursos que tenia asignados.
     * Segun el enunciado, solo se pueden cancelar reservas futuras.
     */
    public void cancelarReserva(String idReserva) throws Exception {
        Reserva reserva = reservaDAO.buscarPorId(idReserva)
                .orElseThrow(() -> new Exception("La reserva no existe."));

        if (reserva.getFechaComoLocalDate().isBefore(LocalDate.now())) {
            throw new Exception("No se puede cancelar una reserva que ya paso.");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reservaDAO.guardar(reserva);
    }

    /**
     * Elimina definitivamente una reserva del sistema (borrado fisico, para
     * cumplir el requisito de CRUD completo). En el uso normal del sistema
     * lo esperable es "cancelar" (borrado logico); este metodo queda
     * disponible para mantenimiento y para las pruebas automatizadas.
     */
    public void eliminarReserva(String idReserva) {
        reservaDAO.eliminar(idReserva);
    }

    // ------------------------------------------------------------------
    // Metodos privados de apoyo
    // ------------------------------------------------------------------

    private void validarDatosBasicos(String actividad, LocalDate fecha, LocalTime horaInicio,
                                     LocalTime horaFin, List<String> idsCategorias) throws Exception {
        if (actividad == null || actividad.isBlank()) {
            throw new Exception("Debe indicar el nombre de la actividad.");
        }
        if (fecha == null) {
            throw new Exception("Debe indicar la fecha de la actividad.");
        }
        if (horaInicio == null || horaFin == null) {
            throw new Exception("Debe indicar la hora de inicio y de finalizacion.");
        }
        if (!horaInicio.isBefore(horaFin)) {
            throw new Exception("La hora de inicio debe ser anterior a la hora de finalizacion.");
        }
        if (idsCategorias == null || idsCategorias.isEmpty()) {
            throw new Exception("Debe seleccionar al menos una categoria de recurso.");
        }
    }

    private ResultadoReserva asignarRecursosYGuardar(Reserva reservaExistente, String idFuncionario,
                                                     String actividad, LocalDate fecha, LocalTime horaInicio,
                                                     LocalTime horaFin, List<String> idsCategorias) {

        List<Reserva> todasLasReservas = reservaDAO.listarTodas();

        List<String> categoriasNoDisponibles = new ArrayList<>();
        List<String> recursosAsignados = new ArrayList<>();

        for (String idCategoria : idsCategorias) {
            Optional<Recurso> recursoLibre = buscarPrimerRecursoLibre(
                    idCategoria, fecha, horaInicio, horaFin, todasLasReservas, reservaExistente, recursosAsignados);

            if (recursoLibre.isPresent()) {
                recursosAsignados.add(recursoLibre.get().getId());
            } else {
                Optional<CategoriaRecurso> categoria = categoriaDAO.buscarPorId(idCategoria);
                categoriasNoDisponibles.add(categoria.map(CategoriaRecurso::getDescripcion).orElse(idCategoria));
            }
        }

        if (!categoriasNoDisponibles.isEmpty()) {
            // No hubo exito: no se guarda nada, se informa que categorias fallaron
            return new ResultadoReserva(false, categoriasNoDisponibles, null);
        }

        Reserva reserva = (reservaExistente != null) ? reservaExistente : new Reserva();
        if (reserva.getId() == null) {
            reserva.setId(reservaDAO.generarSiguienteId());
        }
        reserva.setIdFuncionario(idFuncionario);
        reserva.setActividad(actividad);
        reserva.setFecha(fecha.toString());
        reserva.setHoraInicio(horaInicio.toString());
        reserva.setHoraFin(horaFin.toString());
        reserva.setIdsCategoriasSolicitadas(idsCategorias);
        reserva.setIdsRecursosAsignados(recursosAsignados);
        reserva.setEstado(EstadoReserva.ACTIVA);

        reservaDAO.guardar(reserva);

        return new ResultadoReserva(true, categoriasNoDisponibles, reserva);
    }

    /**
     * Busca el primer recurso de una categoria que no este ocupado por
     * ninguna otra reserva activa que se solape con la fecha/hora solicitada.
     * Se necesita encontrar "al menos una unidad" (el minimo requerido) para
     * que la categoria cuente como disponible; nunca se puede pasar del
     * maximo fisico de recursos que existen en esa categoria.
     */
    private Optional<Recurso> buscarPrimerRecursoLibre(String idCategoria, LocalDate fecha, LocalTime horaInicio,
                                                       LocalTime horaFin, List<Reserva> todasLasReservas,
                                                       Reserva reservaQueSeExcluye, List<String> yaAsignadosEnEstaOperacion) {

        List<Recurso> recursosDeLaCategoria = recursoDAO.listarPorCategoria(idCategoria);

        for (Recurso recurso : recursosDeLaCategoria) {

            // No se puede asignar dos veces el mismo recurso dentro de la misma reserva
            if (yaAsignadosEnEstaOperacion.contains(recurso.getId())) {
                continue;
            }

            boolean ocupado = false;
            for (Reserva otra : todasLasReservas) {
                if (otra.getEstado() == EstadoReserva.CANCELADA) {
                    continue;
                }
                if (reservaQueSeExcluye != null && otra.getId().equals(reservaQueSeExcluye.getId())) {
                    continue; // no comparar la reserva contra si misma al modificarla
                }
                if (otra.getIdsRecursosAsignados().contains(recurso.getId())
                        && otra.seSolapaCon(fecha, horaInicio, horaFin)) {
                    ocupado = true;
                    break;
                }
            }

            if (!ocupado) {
                return Optional.of(recurso);
            }
        }

        return Optional.empty();
    }
}