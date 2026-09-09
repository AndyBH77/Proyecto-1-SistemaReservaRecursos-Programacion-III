package cr.ac.una.eif206.modelo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una reserva de recursos hecha por un funcionario.
 *
 * Nota de diseño: para simplificar el manejo con JAXB, la fecha y las horas
 * se guardan como texto (String) en formato ISO ("yyyy-MM-dd" y "HH:mm").
 * Se agregan metodos getFechaComoLocalDate()/getHoraInicioComoLocalTime()
 * etc. para trabajar comodamente con java.time en el resto del codigo.
 */
@XmlRootElement(name = "reserva")
@XmlAccessorType(XmlAccessType.FIELD)
public class Reserva {

    private String id;                 // Ej: RES-000001
    private String idFuncionario;      // id del funcionario que hizo la reserva
    private String actividad;
    private String fecha;              // yyyy-MM-dd
    private String horaInicio;         // HH:mm
    private String horaFin;            // HH:mm
    private EstadoReserva estado;

    @XmlElement(name = "idCategoria")
    private List<String> idsCategoriasSolicitadas = new ArrayList<>();

    @XmlElement(name = "idRecurso")
    private List<String> idsRecursosAsignados = new ArrayList<>();

    public Reserva() {
    }

    public Reserva(String id, String idFuncionario, String actividad, String fecha,
                   String horaInicio, String horaFin, List<String> idsCategoriasSolicitadas) {
        this.id = id;
        this.idFuncionario = idFuncionario;
        this.actividad = actividad;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.idsCategoriasSolicitadas = idsCategoriasSolicitadas;
        this.estado = EstadoReserva.ACTIVA;
    }

    // ---- Getters / Setters ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdFuncionario() {
        return idFuncionario;
    }

    public void setIdFuncionario(String idFuncionario) {
        this.idFuncionario = idFuncionario;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public List<String> getIdsCategoriasSolicitadas() {
        return idsCategoriasSolicitadas;
    }

    public void setIdsCategoriasSolicitadas(List<String> idsCategoriasSolicitadas) {
        this.idsCategoriasSolicitadas = idsCategoriasSolicitadas;
    }

    public List<String> getIdsRecursosAsignados() {
        return idsRecursosAsignados;
    }

    public void setIdsRecursosAsignados(List<String> idsRecursosAsignados) {
        this.idsRecursosAsignados = idsRecursosAsignados;
    }

    // ---- Utilidades con java.time ----

    public LocalDate getFechaComoLocalDate() {
        return LocalDate.parse(fecha);
    }

    public LocalTime getHoraInicioComoLocalTime() {
        return LocalTime.parse(horaInicio);
    }

    public LocalTime getHoraFinComoLocalTime() {
        return LocalTime.parse(horaFin);
    }

    /**
     * Indica si esta reserva se solapa en fecha/hora con el rango dado.
     * Se usa para calcular disponibilidad de recursos (evitar dobles reservas).
     */
    public boolean seSolapaCon(LocalDate otraFecha, LocalTime otroInicio, LocalTime otroFin) {
        if (!this.getFechaComoLocalDate().isEqual(otraFecha)) {
            return false;
        }
        LocalTime inicio = getHoraInicioComoLocalTime();
        LocalTime fin = getHoraFinComoLocalTime();
        // Dos rangos [inicio,fin) se solapan si inicio1 < fin2 y inicio2 < fin1
        return inicio.isBefore(otroFin) && otroInicio.isBefore(fin);
    }
}
