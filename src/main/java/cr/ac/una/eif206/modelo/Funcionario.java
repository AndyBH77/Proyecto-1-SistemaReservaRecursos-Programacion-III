package cr.ac.una.eif206.modelo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Representa a un funcionario: es un Usuario que ademas tiene nombre y telefono.
 *
 * NOTA PARA EL EQUIPO: esta clase la usa principalmente el modulo de
 * "3-Lista de Funcionarios" (Persona B), pero se define aqui porque el
 * Login/Registro (Persona A) necesita crear y consultar funcionarios para
 * poder iniciar sesion y registrar nuevos usuarios. Coordinen si alguno de
 * los dos necesita agregarle mas campos.
 */
@XmlRootElement(name = "funcionario")
@XmlAccessorType(XmlAccessType.FIELD)
public class Funcionario extends Usuario {

    private String nombre;
    private String telefono;

    public Funcionario() {
        super();
    }

    public Funcionario(String id, String clave, String email, String nombre, String telefono) {
        super(id, clave, RolUsuario.FUNCIONARIO, email);
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
