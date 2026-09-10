package cr.ac.una.eif206.modelo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

/**
 * Clase base para cualquier usuario del sistema (Administrador o Funcionario).
 * Contiene los datos comunes: id, clave, rol y correo electronico.
 *
 * Se usa XmlAccessorType(FIELD) para que JAXB (la libreria que convierte
 * objetos Java a XML y viceversa) lea/escriba directamente los atributos.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Usuario {

    protected String id;
    protected String clave;
    protected RolUsuario rol;
    protected String email; // Correo real, usado para el envio de claves generadas

    // Constructor vacio requerido por JAXB
    public Usuario() {
    }

    public Usuario(String id, String clave, RolUsuario rol, String email) {
        this.id = id;
        this.clave = clave;
        this.rol = rol;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
