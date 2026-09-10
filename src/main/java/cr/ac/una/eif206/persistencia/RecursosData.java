package cr.ac.una.eif206.persistencia;

import cr.ac.una.eif206.modelo.Recurso;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "recursos")
@XmlAccessorType(XmlAccessType.FIELD)
public class RecursosData {

    @XmlElement(name = "recurso")
    private List<Recurso> recursos = new ArrayList<>();

    public List<Recurso> getRecursos() {
        return recursos;
    }

    public void setRecursos(List<Recurso> recursos) {
        this.recursos = recursos;
    }
}
