package cr.ac.una.eif206.backend.red;

import cr.ac.una.eif206.common.modelo.RolUsuario;
import cr.ac.una.eif206.common.modelo.Usuario;

/**
 * Datos de la sesion de UNA conexion (un frontend).
 * Cada ManejadorCliente tiene su propia SesionCliente.
 *
 * Al hacer LOGIN se guarda el usuario aqui; asi los comandos pueden
 * revisar si hay sesion iniciada y si el usuario tiene el rol correcto.
 */
public class SesionCliente {

    private Usuario usuario;

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public boolean estaAutenticado() {
        return usuario != null;
    }

    /** Lanza error si nadie ha iniciado sesion en esta conexion. */
    public void exigirSesion() throws Exception {
        if (!estaAutenticado()) {
            throw new Exception("Debe iniciar sesion primero.");
        }
    }

    /** Lanza error si el usuario no tiene el rol indicado. */
    public void exigirRol(RolUsuario rol) throws Exception {
        exigirSesion();
        if (usuario.getRol() != rol) {
            throw new Exception("Esta opcion solo esta disponible para usuarios tipo " + rol + ".");
        }
    }
}