package cr.ac.una.eif206.common.modelo;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Una matriz lista para mostrar en una JTable: encabezados + filas de texto.
 * La usan Calendarizacion (horas x recursos) y Actividades (horas x dias).
 *
 * Se usa String y no Object a proposito: asi todo lo que viaja por el socket
 * es seguro de serializar.
 */
public class TablaConsulta implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String[] encabezados;
    private final String[][] filas;

    public TablaConsulta(String[] encabezados, String[][] filas) {
        this.encabezados = encabezados;
        this.filas = filas;
    }

    public String[] getEncabezados() {
        return encabezados;
    }

    public String[][] getFilas() {
        return filas;
    }

    /** Cantidad de columnas (incluye la de la hora). */
    public int getCantidadColumnas() {
        return encabezados.length;
    }

    /** Cantidad de filas de datos. */
    public int getCantidadFilas() {
        return filas.length;
    }

    @Override
    public String toString() {
        return "TablaConsulta" + Arrays.toString(encabezados) + " (" + filas.length + " filas)";
    }
}
