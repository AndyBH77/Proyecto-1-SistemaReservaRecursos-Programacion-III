package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.backend.datos.FuncionarioDAO;
import cr.ac.una.eif206.backend.datos.UsuarioDAO;
import cr.ac.una.eif206.backend.util.EmailService;
import cr.ac.una.eif206.backend.util.PasswordGenerator;
import cr.ac.una.eif206.common.modelo.Funcionario;
import jakarta.mail.MessagingException;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

/**
 * Logica de negocio de Funcionarios (CRUD solo para ADMINISTRADOR).
 *
 * Al REGISTRAR un funcionario se genera una clave aleatoria y se le envia por
 * correo. Primero se envia el correo y SOLO si salio bien se guarda: asi nunca
 * queda un funcionario que no conoce su clave. (Los usuarios de prueba de
 * schema.sql tienen clave = id.)
 */
public class FuncionarioService {

    private static final String REGEX_CORREO = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[a-zA-Z]{2,}$";

    private final FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EmailService emailService = new EmailService();

    public List<Funcionario> listarTodos() throws SQLException {
        return funcionarioDAO.listarTodos();
    }

    /** Busca por id o nombre; si el texto esta vacio devuelve todos. */
    public List<Funcionario> buscar(String texto) throws SQLException {
        if (texto == null || texto.isBlank()) {
            return funcionarioDAO.listarTodos();
        }
        return funcionarioDAO.buscar(texto.trim());
    }

    /**
     * Registra un funcionario nuevo y le envia su clave por correo.
     * @return el correo al que se envio la clave
     */
    public String registrar(String id, String nombre, String telefono, String email) throws Exception {
        String idLimpio = validarId(id);
        validarDatos(nombre, telefono, email);

        if (usuarioDAO.buscarPorId(idLimpio).isPresent()) {
            throw new Exception("Ya existe un usuario con el id \"" + idLimpio + "\".");
        }

        String clave = PasswordGenerator.generar(10);
        String correo = email.trim();

        try {
            emailService.enviarClaveGenerada(correo, nombre.trim(), clave);
        } catch (MessagingException e) {
            throw new Exception("No se pudo enviar el correo con la clave, el funcionario NO se registró. "
                    + "Revise el correo o la configuración de Gmail. Detalle: " + e.getMessage());
        }

        try {
            funcionarioDAO.insertar(new Funcionario(idLimpio, clave, correo, nombre.trim(), telefono.trim()));
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new Exception("Ya existe un usuario con el id \"" + idLimpio + "\".");
        }
        return correo;
    }

    /** Modifica nombre, telefono y correo. El id y la clave no cambian. */
    public void modificar(String id, String nombre, String telefono, String email) throws Exception {
        String idLimpio = validarId(id);
        validarDatos(nombre, telefono, email);

        int filas = funcionarioDAO.actualizar(
                new Funcionario(idLimpio, null, email.trim(), nombre.trim(), telefono.trim()));
        if (filas == 0) {
            throw new Exception("El funcionario \"" + idLimpio + "\" ya no existe.");
        }
    }

    public void eliminar(String id) throws Exception {
        String idLimpio = validarId(id);
        try {
            if (funcionarioDAO.eliminar(idLimpio) == 0) {
                throw new Exception("El funcionario \"" + idLimpio + "\" ya no existe.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new Exception("No se puede eliminar: el funcionario tiene reservas registradas.");
        }
    }

    // ------------------------------------------------------------------
    // Validaciones (se hacen ANTES de tocar la base de datos)
    // ------------------------------------------------------------------

    private String validarId(String id) throws Exception {
        if (id == null || id.isBlank()) {
            throw new Exception("El id es obligatorio.");
        }
        String limpio = id.trim();
        if (limpio.contains(" ")) {
            throw new Exception("El id no puede tener espacios.");
        }
        if (limpio.length() > 20) {
            throw new Exception("El id no puede pasar de 20 caracteres.");
        }
        return limpio;
    }

    /** Publico y estatico para poder probarlo sin base de datos (prueba unitaria). */
    public static void validarDatos(String nombre, String telefono, String email) throws Exception {
        if (nombre == null || nombre.isBlank()) {
            throw new Exception("El nombre es obligatorio.");
        }
        if (nombre.trim().length() > 100) {
            throw new Exception("El nombre no puede pasar de 100 caracteres.");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new Exception("El teléfono es obligatorio.");
        }
        if (!telefono.trim().matches("^[0-9+()\\- ]{8,20}$")) {
            throw new Exception("El teléfono solo puede tener números, espacios, +, - y paréntesis (8 a 20 caracteres).");
        }
        if (email == null || !email.trim().matches(REGEX_CORREO)) {
            throw new Exception("Debe indicar un correo electrónico válido.");
        }
    }
}