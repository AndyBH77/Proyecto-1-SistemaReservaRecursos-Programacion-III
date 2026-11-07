package cr.ac.una.eif206.common.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lo que la IA (o el extractor simple de respaldo) logro identificar en la
 * frase que escribio el usuario. El backend lo llena y el frontend lo usa
 * para completar el formulario de reserva.
 *
 * Los campos que no se pudieron identificar quedan en null.
 */
public class DatosReservaExtraidos implements Serializable {

    private static final long serialVersionUID = 1L;

    public String actividad;
    public LocalDate fecha;
    public LocalTime horaInicio;
    public LocalTime horaFin;
    public List<String> nombresCategorias = new ArrayList<>();
}