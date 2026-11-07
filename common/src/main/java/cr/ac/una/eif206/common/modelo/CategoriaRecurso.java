package cr.ac.una.eif206.common.modelo;

import java.io.Serializable;
import java.util.Objects;

/**
 * Categoria de recursos (ej: "Sala para 10 personas").
 * El id ahora es un numero que genera MySQL (AUTO_INCREMENT).
 * Si id = 0, significa que todavia no se ha guardado en la BD.
 */
public class CategoriaRecurso implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String descripcion;

    public CategoriaRecurso() {
    }

    public CategoriaRecurso(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** Lo que se ve en los JComboBox / JList. */
    @Override
    public String toString() {
        return descripcion;
    }

    /**
     * Dos categorias son "iguales" si tienen el mismo id.
     * Sirve para seleccionar la correcta en un combo o lista.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoriaRecurso)) return false;
        return id == ((CategoriaRecurso) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}