package cr.ac.una.eif206.common.modelo;

import java.io.Serializable;

/**
 * Una fila de estadisticas: una etiqueta y su cantidad.
 * Ej: ("Laptop windows 11", 4) o ("2026-10-26", 3) para semanas.
 * Sirve para llenar la tabla y el grafico de barras.
 */
public class EstadisticaItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String etiqueta;
    private int cantidad;

    public EstadisticaItem() {
    }

    public EstadisticaItem(String etiqueta, int cantidad) {
        this.etiqueta = etiqueta;
        this.cantidad = cantidad;
    }

    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}