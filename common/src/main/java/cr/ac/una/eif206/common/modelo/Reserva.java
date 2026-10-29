package cr.ac.una.eif206.common.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Reserva de recursos para una actividad.
 *
 * - id: lo genera MySQL (AUTO_INCREMENT). 0 = todavia no guardada.
 * - nombreFuncionario: no es columna de la tabla reserva; el DAO la llena con
 *   un JOIN. Se usa en calendarizacion, actividades y alertas.
 * - recursos: los recursos asignados (tabla reserva_recurso).
 */
public class Reserva implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String idFuncionario;
    private String nombreFuncionario;
    private String actividad;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoReserva estado = EstadoReserva.ACTIVA;
    private List<Recurso> recursos = new ArrayList<>();

    public Reserva() {
    }

    public Reserva(String idFuncionario, String actividad, LocalDate fecha,
                   LocalTime horaInicio, LocalTime horaFin) {
        this.idFuncionario = idFuncionario;
        this.actividad = actividad;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    /**
     * Indica si esta reserva choca en horario con otra fecha/hora.
     * Dos rangos [inicio, fin) se solapan si inicio1 < fin2 y inicio2 < fin1.
     * Ej: 8:00-10:00 y 10:00-11:00 NO se solapan (una termina cuando empieza la otra).
     */
    public boolean seSolapaCon(LocalDate otraFecha, LocalTime otroInicio, LocalTime otroFin) {
        if (!fecha.isEqual(otraFecha)) {
            return false;
        }
        return horaInicio.isBefore(otroFin) && otroInicio.isBefore(horaFin);
    }

    /** Descripciones de los recursos, para mostrarlas en tablas o en la alerta. */
    public List<String> getDescripcionesRecursos() {
        List<String> descripciones = new ArrayList<>();
        for (Recurso r : recursos) {
            descripciones.add(r.getDescripcion());
        }
        return descripciones;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIdFuncionario() { return idFuncionario; }
    public void setIdFuncionario(String idFuncionario) { this.idFuncionario = idFuncionario; }

    public String getNombreFuncionario() { return nombreFuncionario; }
    public void setNombreFuncionario(String nombreFuncionario) { this.nombreFuncionario = nombreFuncionario; }

    public String getActividad() { return actividad; }
    public void setActividad(String actividad) { this.actividad = actividad; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }

    public List<Recurso> getRecursos() { return recursos; }
    public void setRecursos(List<Recurso> recursos) { this.recursos = recursos; }
}