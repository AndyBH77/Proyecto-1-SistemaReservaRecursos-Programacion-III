package cr.ac.una.eif206.backend.datos;

import cr.ac.una.eif206.common.modelo.Funcionario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a las tablas "usuario" y "funcionario" para el CRUD de funcionarios.
 *
 * Un funcionario esta en DOS tablas: sus datos de usuario (id, clave, rol,
 * email) en "usuario" y su nombre/telefono en "funcionario". Por eso insertar
 * y actualizar usan una TRANSACCION (o se guardan las dos o ninguna).
 *
 * La clave NUNCA se lee aqui: no hace falta para mostrar funcionarios y
 * asi no viaja al frontend.
 */
public class FuncionarioDAO {

    private static final String SELECT_BASE =
            "SELECT u.id, u.email, f.nombre, f.telefono "
                    + "FROM funcionario f JOIN usuario u ON u.id = f.id ";

    public List<Funcionario> listarTodos() throws SQLException {
        String sql = SELECT_BASE + "ORDER BY f.nombre";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapearTodos(rs);
        }
    }

    /** Funcionarios cuyo id O nombre contienen el texto. */
    public List<Funcionario> buscar(String texto) throws SQLException {
        String sql = SELECT_BASE + "WHERE f.id LIKE ? OR f.nombre LIKE ? ORDER BY f.nombre";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            ps.setString(2, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return mapearTodos(rs);
            }
        }
    }

    public Optional<Funcionario> buscarPorId(String id) throws SQLException {
        String sql = SELECT_BASE + "WHERE f.id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<Funcionario> lista = mapearTodos(rs);
                return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
            }
        }
    }

    /** Inserta en usuario (con su clave) y en funcionario, en una transaccion. */
    public void insertar(Funcionario funcionario) throws SQLException {
        String sqlUsuario = "INSERT INTO usuario (id, clave, rol, email) VALUES (?, ?, 'FUNCIONARIO', ?)";
        String sqlFuncionario = "INSERT INTO funcionario (id, nombre, telefono) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlUsuario)) {
                    ps.setString(1, funcionario.getId());
                    ps.setString(2, funcionario.getClave());
                    ps.setString(3, funcionario.getEmail());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(sqlFuncionario)) {
                    ps.setString(1, funcionario.getId());
                    ps.setString(2, funcionario.getNombre());
                    ps.setString(3, funcionario.getTelefono());
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Actualiza correo (usuario) y nombre/telefono (funcionario). No cambia id ni clave. */
    public int actualizar(Funcionario funcionario) throws SQLException {
        String sqlUsuario = "UPDATE usuario SET email = ? WHERE id = ? AND rol = 'FUNCIONARIO'";
        String sqlFuncionario = "UPDATE funcionario SET nombre = ?, telefono = ? WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                int filas;
                try (PreparedStatement ps = con.prepareStatement(sqlUsuario)) {
                    ps.setString(1, funcionario.getEmail());
                    ps.setString(2, funcionario.getId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(sqlFuncionario)) {
                    ps.setString(1, funcionario.getNombre());
                    ps.setString(2, funcionario.getTelefono());
                    ps.setString(3, funcionario.getId());
                    filas = ps.executeUpdate();
                }
                con.commit();
                return filas;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Borra el usuario; su fila de "funcionario" se borra sola (ON DELETE CASCADE).
     * Si tiene reservas, MySQL lo impide y lanza SQLIntegrityConstraintViolationException.
     * Devuelve cuantas filas borro (0 = no existia).
     */
    public int eliminar(String id) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id = ? AND rol = 'FUNCIONARIO'";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate();
        }
    }

    private List<Funcionario> mapearTodos(ResultSet rs) throws SQLException {
        List<Funcionario> lista = new ArrayList<>();
        while (rs.next()) {
            // clave = null: nunca se envia al frontend
            lista.add(new Funcionario(rs.getString("id"), null, rs.getString("email"),
                    rs.getString("nombre"), rs.getString("telefono")));
        }
        return lista;
    }
}