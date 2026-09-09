package cr.ac.una.eif206.negocio;

import cr.ac.una.eif206.modelo.Administrador;
import cr.ac.una.eif206.modelo.Funcionario;
import cr.ac.una.eif206.modelo.Usuario;
import cr.ac.una.eif206.persistencia.AdministradorDAO;
import cr.ac.una.eif206.persistencia.FuncionarioDAO;

import jakarta.mail.MessagingException;

import java.util.List;
import java.util.Optional;

public class AutenticacionService {

    private final AdministradorDAO administradorDAO = new AdministradorDAO();
    private final FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
    private final EmailService emailService = new EmailService();

    public Optional<Usuario> login(String id, String clave) {
        Optional<Administrador> admin = administradorDAO.buscarPorId(id);
        if (admin.isPresent() && admin.get().getClave().equals(clave)) {
            return Optional.of(admin.get());
        }

        Optional<Funcionario> funcionario = funcionarioDAO.buscarPorId(id);
        if (funcionario.isPresent() && funcionario.get().getClave().equals(clave)) {
            return Optional.of(funcionario.get());
        }

        return Optional.empty();
    }

    public void cambiarClave(Usuario usuario, String claveActual, String claveNueva) throws Exception {
        if (!usuario.getClave().equals(claveActual)) {
            throw new Exception("La clave actual no es correcta.");
        }
        if (claveNueva == null || claveNueva.isBlank()) {
            throw new Exception("La nueva clave no puede estar vacia.");
        }

        usuario.setClave(claveNueva);

        if (usuario instanceof Administrador administrador) {
            administradorDAO.guardarTodos(reemplazarEnLista(administradorDAO.listarTodos(), administrador));
        } else if (usuario instanceof Funcionario funcionario) {
            funcionarioDAO.guardar(funcionario);
        }
    }

    /**
     * Registra un nuevo funcionario en el sistema: genera una clave aleatoria,
     * la envia al correo indicado y guarda el nuevo usuario.
     * Solo el Administrador tiene acceso a la pantalla que llama este metodo.
     */
    public String registrarNuevoFuncionario(String id, String nombre, String telefono, String correo) throws Exception {
        validarDatosFuncionario(nombre, telefono, correo);

        if (id == null || id.isBlank()) {
            throw new Exception("El id es obligatorio.");
        }
        if (funcionarioDAO.existeId(id) || administradorDAO.buscarPorId(id).isPresent()) {
            throw new Exception("Ya existe un usuario registrado con el id \"" + id + "\".");
        }

        String claveGenerada = PasswordGenerator.generar(10);
        Funcionario nuevoFuncionario = new Funcionario(id, claveGenerada, correo, nombre, telefono);

        try {
            emailService.enviarClaveGenerada(correo, nombre, claveGenerada);
        } catch (MessagingException e) {
            throw new Exception("No se pudo enviar el correo con la clave. Verifique la configuracion "
                    + "de gmail.usuario/gmail.password o su conexion a internet. Detalle: " + e.getMessage());
        }

        funcionarioDAO.guardar(nuevoFuncionario);
        return claveGenerada;
    }

    /**
     * Modifica los datos (nombre, telefono, correo) de un funcionario ya
     * existente. No cambia su id ni su clave (para eso esta cambiarClave).
     */
    public void modificarDatosFuncionario(String id, String nombre, String telefono, String correo) throws Exception {
        Funcionario funcionario = funcionarioDAO.buscarPorId(id)
                .orElseThrow(() -> new Exception("El funcionario \"" + id + "\" no existe."));

        validarDatosFuncionario(nombre, telefono, correo);

        funcionario.setNombre(nombre);
        funcionario.setTelefono(telefono);
        funcionario.setEmail(correo);
        funcionarioDAO.guardar(funcionario);
    }

    public void recuperarClave(String id) throws Exception {
        Optional<Funcionario> funcionarioOpt = funcionarioDAO.buscarPorId(id);
        Optional<Administrador> adminOpt = administradorDAO.buscarPorId(id);

        if (funcionarioOpt.isEmpty() && adminOpt.isEmpty()) {
            throw new Exception("No existe ningun usuario con el id \"" + id + "\".");
        }

        String claveNueva = PasswordGenerator.generar(10);

        if (funcionarioOpt.isPresent()) {
            Funcionario funcionario = funcionarioOpt.get();
            emailService.enviarClaveGenerada(funcionario.getEmail(), funcionario.getNombre(), claveNueva);
            funcionario.setClave(claveNueva);
            funcionarioDAO.guardar(funcionario);
        } else {
            Administrador admin = adminOpt.get();
            emailService.enviarClaveGenerada(admin.getEmail(), admin.getId(), claveNueva);
            admin.setClave(claveNueva);
            administradorDAO.guardarTodos(reemplazarEnLista(administradorDAO.listarTodos(), admin));
        }
    }

    private void validarDatosFuncionario(String nombre, String telefono, String correo) throws Exception {
        if (nombre == null || nombre.isBlank()) {
            throw new Exception("El nombre es obligatorio.");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new Exception("El telefono es obligatorio.");
        }
        if (correo == null || !correo.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            throw new Exception("Debe indicar un correo electronico valido.");
        }
    }

    private List<Administrador> reemplazarEnLista(List<Administrador> lista, Administrador admin) {
        lista.removeIf(a -> a.getId().equalsIgnoreCase(admin.getId()));
        lista.add(admin);
        return lista;
    }
}