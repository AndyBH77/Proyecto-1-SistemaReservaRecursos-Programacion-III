package cr.ac.una.eif206.common.modelo;

/**
 * Usuario con rol ADMINISTRADOR. No tiene datos extra.
 */
public class Administrador extends Usuario {

    private static final long serialVersionUID = 1L;

    public Administrador() {
        this.rol = RolUsuario.ADMINISTRADOR;
    }

    public Administrador(String id, String clave, String email) {
        super(id, clave, RolUsuario.ADMINISTRADOR, email);
    }
}