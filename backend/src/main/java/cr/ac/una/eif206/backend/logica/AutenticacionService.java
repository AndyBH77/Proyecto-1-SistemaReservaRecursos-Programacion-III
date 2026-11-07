package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.backend.datos.UsuarioDAO;
import cr.ac.una.eif206.backend.util.EmailService;
import cr.ac.una.eif206.backend.util.PasswordGenerator;
import cr.ac.una.eif206.common.modelo.Funcionario;
import cr.ac.una.eif206.common.modelo.Usuario;
import jakarta.mail.MessagingException;

/**
 * Logica de ingreso al sistema y manejo de claves.
 * Todas las validaciones lanzan Exception con un mensaje claro;
 * el servidor lo convierte en Response.error(...) para el frontend.
 */
public class AutenticacionService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EmailService emailService = new EmailService();

    /**
     * Valida id y clave. Si son correctos devuelve el usuario SIN la clave
     * (nunca se envia la clave al frontend por la red).
     */
    public Usuario login(String id, String clave) throws Exception {
        if (id == null || id.isBlank() || clave == null || clave.isEmpty()) {
            throw new Exception("Debe indicar el id y la clave.");
        }

        Usuario usuario = usuarioDAO.buscarPorId(id.trim()).orElse(null);

        // Mismo mensaje si no existe o si la clave esta mal: asi no se le
        // revela a un intruso cuales ids existen.
        if (usuario == null || !usuario.getClave().equals(clave)) {
            throw new Exception("El id o la clave no son correctos.");
        }

        usuario.setClave(null);
        return usuario;
    }

    /** Cambia la clave del usuario que tiene la sesion iniciada. */
    public void cambiarClave(String idUsuario, String claveActual, String claveNueva) throws Exception {
        Usuario usuario = usuarioDAO.buscarPorId(idUsuario)
                .orElseThrow(() -> new Exception("El usuario ya no existe."));

        if (claveActual == null || !usuario.getClave().equals(claveActual)) {
            throw new Exception("La clave actual no es correcta.");
        }
        if (claveNueva == null || claveNueva.isBlank()) {
            throw new Exception("La nueva clave no puede estar vacia.");
        }
        if (claveNueva.equals(claveActual)) {
            throw new Exception("La nueva clave debe ser diferente a la actual.");
        }

        usuarioDAO.actualizarClave(idUsuario, claveNueva);
    }

    /**
     * Genera una clave nueva, la envia al correo del usuario y la guarda.
     * Se guarda SOLO si el correo se envio bien; si no, el usuario
     * conserva su clave anterior.
     */
    public void recuperarClave(String id) throws Exception {
        if (id == null || id.isBlank()) {
            throw new Exception("Debe indicar el id.");
        }

        Usuario usuario = usuarioDAO.buscarPorId(id.trim())
                .orElseThrow(() -> new Exception("No existe ningun usuario con el id \"" + id.trim() + "\"."));

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new Exception("El usuario no tiene un correo registrado.");
        }

        String claveNueva = PasswordGenerator.generar(10);
        String nombre = (usuario instanceof Funcionario f) ? f.getNombre() : usuario.getId();

        try {
            emailService.enviarClaveGenerada(usuario.getEmail(), nombre, claveNueva);
        } catch (MessagingException e) {
            throw new Exception("No se pudo enviar el correo con la nueva clave. "
                    + "Revise gmail.usuario/gmail.password en config.properties. Detalle: " + e.getMessage());
        }

        usuarioDAO.actualizarClave(usuario.getId(), claveNueva);
    }
}