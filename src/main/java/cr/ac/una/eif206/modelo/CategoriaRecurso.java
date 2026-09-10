package cr.ac.una.eif206.modelo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Categoria de recurso (ej: "Sala de Juntas", "Laptop windows").
 *
 * NOTA PARA EL EQUIPO: el CRUD completo de esta clase (pantalla de
 * busqueda/alta/baja/modificacion) le corresponde a la funcionalidad
 * "4-Lista de categorias de recursos". Aqui solo se define el modelo y un
 * DAO de lectura basico para que la pantalla de Reservas pueda listar las
 * categorias disponibles.
 */
@XmlRootElement(name = "categoriaRecurso")
@XmlAccessorType(XmlAccessType.FIELD)
public class CategoriaRecurso {

    private String id;
    private String descripcion;

    public CategoriaRecurso() {
    }

    public CategoriaRecurso(String id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        // Se usa cuando esta clase se muestra en un JList o JComboBox
        return descripcion;
    }
}
