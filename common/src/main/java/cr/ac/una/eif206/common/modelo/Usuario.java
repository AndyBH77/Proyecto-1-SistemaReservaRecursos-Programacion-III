package cr.ac.una.eif206.common.modelo;

import java.io.Serializable;

/**
 * Datos comunes de cualquier usuario (administrador o funcionario).
 *
 * Implementa Serializable para poder enviarse por el socket.
 *
 * IMPORTANTE: el backend nunca debe devolverle la clave al frontend.
 * Despues del login, el backend pone clave = null antes de enviar el usuario.
 */
public abstract class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String id;
    protected String clave;
    protected RolUsuario rol;
    protected String email;

    public Usuario() {
    }

    public Usuario(String id, String clave, RolUsuario rol, String email) {
        this.id = id;
        this.clave = clave;
        this.rol = rol;
        this.email = email;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}