package cr.ac.una.eif206.modelo;

/**
 * Estado de una reserva: ACTIVA mientras este vigente, CANCELADA cuando
 * el funcionario la cancela (esto libera los recursos que tenia asignados).
 */
public enum EstadoReserva {
    ACTIVA,
    CANCELADA
}
