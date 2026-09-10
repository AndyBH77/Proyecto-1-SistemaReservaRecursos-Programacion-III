package cr.ac.una.eif206.persistencia;

import cr.ac.una.eif206.modelo.Administrador;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

/**
 * "Envoltorio" necesario para poder guardar una lista de administradores en
 * un solo archivo XML con JAXB (JAXB no puede serializar una List suelta
 * sin una clase raiz que la contenga).
 */
@XmlRootElement(name = "administradores")
@XmlAccessorType(XmlAccessType.FIELD)
public class AdministradoresData {

    @XmlElement(name = "administrador")
    private List<Administrador> administradores = new ArrayList<>();

    public List<Administrador> getAdministradores() {
        return administradores;
    }

    public void setAdministradores(List<Administrador> administradores) {
        this.administradores = administradores;
    }
}
