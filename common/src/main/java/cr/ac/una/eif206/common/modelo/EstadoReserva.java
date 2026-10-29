package cr.ac.una.eif206.common.modelo;

/**
 * Estados posibles de una reserva.
 * Debe coincidir con el ENUM de la columna reserva.estado en MySQL.
 */
public enum EstadoReserva {
    ACTIVA,
    CANCELADA
}