package cr.ac.una.eif206.negocio.llm;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO (objeto simple de transporte de datos) con lo que la IA (o el
 * extractor simple de respaldo) logra identificar a partir de la frase
 * escrita por el usuario.
 */
public class DatosReservaExtraidos {

    public String actividad;
    public LocalDate fecha;
    public LocalTime horaInicio;
    public LocalTime horaFin;
    public List<String> nombresCategorias = new ArrayList<>();
}
