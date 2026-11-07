package cr.ac.una.eif206.common.protocolo;

import cr.ac.una.eif206.common.modelo.Reserva;

import java.io.Serializable;

/**
 * Alerta asincronica que el backend envia a los DEMAS frontends cuando
 * alguien registra una reserva nueva.
 *
 * Lleva la reserva completa (con nombre del funcionario y recursos),
 * que tiene todo lo que pide la ventana "Ver alerta":
 * Encargado, Fecha, Desde, Hasta y Recursos.
 */
public class Notificacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Reserva reserva;

    public Notificacion(Reserva reserva) {
        this.reserva = reserva;
    }

    public Reserva getReserva() {
        return reserva;
    }

    /** Texto de la fila en el area de alertas: "Funcionario - Actividad". */
    public String getTitulo() {
        return reserva.getNombreFuncionario() + " - " + reserva.getActividad();
    }
}