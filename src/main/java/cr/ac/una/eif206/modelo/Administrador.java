package cr.ac.una.eif206.modelo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "administrador")
@XmlAccessorType(XmlAccessType.FIELD)
public class Administrador extends Usuario {

    public Administrador() {
        super();
    }

    public Administrador(String id, String clave, String email) {
        super(id, clave, RolUsuario.ADMINISTRADOR, email);
    }
}
