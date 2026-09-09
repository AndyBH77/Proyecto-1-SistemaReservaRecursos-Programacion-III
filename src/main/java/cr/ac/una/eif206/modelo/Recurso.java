package cr.ac.una.eif206.modelo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Un recurso fisico especifico (ej: "Laptop #238715"), que pertenece a una
 * categoria. El CRUD completo de esta clase lo implementa la funcionalidad
 * "5-Lista de recursos"; aqui solo se deja el modelo y un DAO basico.
 */
@XmlRootElement(name = "recurso")
@XmlAccessorType(XmlAccessType.FIELD)
public class Recurso {

    private String id; // numero de activo
    private String idCategoria;
    private String descripcion;

    public Recurso() {
    }

    public Recurso(String id, String idCategoria, String descripcion) {
        this.id = id;
        this.idCategoria = idCategoria;
        this.descripcion = descripcion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(String idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
