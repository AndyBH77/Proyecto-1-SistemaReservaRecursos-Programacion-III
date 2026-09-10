package cr.ac.una.eif206.persistencia;

import cr.ac.una.eif206.modelo.CategoriaRecurso;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "categorias")
@XmlAccessorType(XmlAccessType.FIELD)
public class CategoriasData {

    @XmlElement(name = "categoria")
    private List<CategoriaRecurso> categorias = new ArrayList<>();

    public List<CategoriaRecurso> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaRecurso> categorias) {
        this.categorias = categorias;
    }
}
