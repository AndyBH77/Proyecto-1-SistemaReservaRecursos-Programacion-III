package cr.ac.una.eif206.common.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de intentar crear o modificar una reserva.
 *  - exito = true  -> "reserva" trae la reserva guardada (con sus recursos asignados).
 *  - exito = false -> "categoriasNoDisponibles" dice que categorias no tenian
 *                     ningun recurso libre en ese horario (como pide el enunciado).
 */
public class ResultadoReserva implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean exito;
    private final List<String> categoriasNoDisponibles;
    private final Reserva reserva;

    private ResultadoReserva(boolean exito, List<String> categoriasNoDisponibles, Reserva reserva) {
        this.exito = exito;
        this.categoriasNoDisponibles = categoriasNoDisponibles;
        this.reserva = reserva;
    }

    public static ResultadoReserva exitosa(Reserva reserva) {
        return new ResultadoReserva(true, new ArrayList<>(), reserva);
    }

    public static ResultadoReserva sinDisponibilidad(List<String> categoriasNoDisponibles) {
        return new ResultadoReserva(false, new ArrayList<>(categoriasNoDisponibles), null);
    }

    public boolean isExito() {
        return exito;
    }

    public List<String> getCategoriasNoDisponibles() {
        return categoriasNoDisponibles;
    }

    public Reserva getReserva() {
        return reserva;
    }
}