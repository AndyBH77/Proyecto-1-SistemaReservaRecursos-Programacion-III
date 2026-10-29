package cr.ac.una.eif206.common.modelo;

/**
 * Usuario con rol FUNCIONARIO. Ademas de los datos de usuario
 * tiene nombre y telefono (tabla funcionario en MySQL).
 */
public class Funcionario extends Usuario {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private String telefono;

    public Funcionario() {
        this.rol = RolUsuario.FUNCIONARIO;
    }

    public Funcionario(String id, String clave, String email, String nombre, String telefono) {
        super(id, clave, RolUsuario.FUNCIONARIO, email);
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /** Se usa cuando el funcionario aparece en un JComboBox o JList. */
    @Override
    public String toString() {
        return nombre;
    }
}