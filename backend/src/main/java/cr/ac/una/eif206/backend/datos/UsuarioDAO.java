package cr.ac.una.eif206.backend.datos;

import cr.ac.una.eif206.common.modelo.Administrador;
import cr.ac.una.eif206.common.modelo.Funcionario;
import cr.ac.una.eif206.common.modelo.RolUsuario;
import cr.ac.una.eif206.common.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Acceso a la tabla "usuario" (y su parte de "funcionario") para el LOGIN
 * y el manejo de claves. Sirve para administradores y funcionarios.
 *
 * El CRUD completo de funcionarios (registrar, modificar, eliminar, buscar)
 * lo hace Benja en su propio FuncionarioDAO.
 */
public class UsuarioDAO {

    /**
     * LEFT JOIN: trae TODOS los usuarios; si el usuario tambien esta en la
     * tabla funcionario, agrega su nombre y telefono (si no, vienen en null).
     */
    private static final String SELECT_BASE =
            "SELECT u.id, u.clave, u.rol, u.email, f.nombre, f.telefono "
                    + "FROM usuario u LEFT JOIN funcionario f ON f.id = u.id ";

    /** Busca un usuario por su id. Optional.empty() si no existe. */
    public Optional<Usuario> buscarPorId(String id) throws SQLException {
        String sql = SELECT_BASE + "WHERE u.id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    /** Cambia la clave de un usuario (sirve para cualquier rol). */
    public void actualizarClave(String id, String claveNueva) throws SQLException {
        String sql = "UPDATE usuario SET clave = ? WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, claveNueva);
            ps.setString(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Convierte la fila en el objeto correcto segun el rol:
     * ADMINISTRADOR -> Administrador, FUNCIONARIO -> Funcionario.
     */
    private Usuario mapear(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String clave = rs.getString("clave");
        String email = rs.getString("email");
        RolUsuario rol = RolUsuario.valueOf(rs.getString("rol"));

        if (rol == RolUsuario.ADMINISTRADOR) {
            return new Administrador(id, clave, email);
        }
        return new Funcionario(id, clave, email, rs.getString("nombre"), rs.getString("telefono"));
    }
}