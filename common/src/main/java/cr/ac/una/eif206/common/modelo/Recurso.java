package cr.ac.una.eif206.common.modelo;

import java.io.Serializable;
import java.util.Objects;

/**
 * Recurso fisico (ej: "Laptop #238715").
 * El id es el numero de activo (texto que escribe el administrador).
 *
 * descripcionCategoria no es una columna de la tabla recurso: el DAO la llena
 * con un JOIN, para poder mostrarla en tablas sin hacer otra consulta.
 */
public class Recurso implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private int idCategoria;
    private String descripcionCategoria;
    private String descripcion;

    public Recurso() {
    }

    public Recurso(String id, int idCategoria, String descripcion) {
        this.id = id;
        this.idCategoria = idCategoria;
        this.descripcion = descripcion;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getDescripcionCategoria() { return descripcionCategoria; }
    public void setDescripcionCategoria(String descripcionCategoria) { this.descripcionCategoria = descripcionCategoria; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return descripcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recurso)) return false;
        return Objects.equals(id, ((Recurso) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}