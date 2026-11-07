package cr.ac.una.eif206.backend.datos;

import cr.ac.una.eif206.common.modelo.Recurso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a la tabla "recurso" (JORDI).
 *
 * Todas las consultas hacen JOIN con categoria para traer tambien la
 * descripcion de la categoria (Recurso.descripcionCategoria) y poder
 * mostrarla en tablas sin otra consulta.
 *
 * Los metodos lanzan SQLException: el servicio la traduce a un mensaje claro.
 */
public class RecursoDAO {

    private static final String SELECT_BASE =
            "SELECT r.id, r.id_categoria, r.descripcion, c.descripcion AS desc_categoria "
                    + "FROM recurso r JOIN categoria c ON c.id = r.id_categoria ";

    /** Todos los recursos, ordenados por categoria y luego por numero de activo. */
    public List<Recurso> listarTodos() throws SQLException {
        String sql = SELECT_BASE + "ORDER BY c.descripcion, r.id";
        List<Recurso> lista = new ArrayList<>();

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Los recursos de una sola categoria (para el filtro y para Calendarizacion). */
    public List<Recurso> listarPorCategoria(int idCategoria) throws SQLException {
        String sql = SELECT_BASE + "WHERE r.id_categoria = ? ORDER BY r.id";
        List<Recurso> lista = new ArrayList<>();

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public Optional<Recurso> buscarPorId(String id) throws SQLException {
        String sql = SELECT_BASE + "WHERE r.id = ?";

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

    /** Si el numero de activo ya existe, MySQL lanza SQLIntegrityConstraintViolationException. */
    public void insertar(Recurso recurso) throws SQLException {
        String sql = "INSERT INTO recurso (id, id_categoria, descripcion) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, recurso.getId());
            ps.setInt(2, recurso.getIdCategoria());
            ps.setString(3, recurso.getDescripcion());
            ps.executeUpdate();
        }
    }

    /** Cambia categoria y descripcion. Devuelve cuantas filas cambio (0 = no existe ese id). */
    public int actualizar(Recurso recurso) throws SQLException {
        String sql = "UPDATE recurso SET id_categoria = ?, descripcion = ? WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, recurso.getIdCategoria());
            ps.setString(2, recurso.getDescripcion());
            ps.setString(3, recurso.getId());
            return ps.executeUpdate();
        }
    }

    /**
     * Borra el recurso. Devuelve cuantas filas borro (0 = no existe ese id).
     * Si esta en alguna reserva, MySQL lo impide (ON DELETE RESTRICT) con
     * SQLIntegrityConstraintViolationException.
     */
    public int eliminar(String id) throws SQLException {
        String sql = "DELETE FROM recurso WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate();
        }
    }

    private Recurso mapear(ResultSet rs) throws SQLException {
        Recurso recurso = new Recurso(rs.getString("id"), rs.getInt("id_categoria"),
                rs.getString("descripcion"));
        recurso.setDescripcionCategoria(rs.getString("desc_categoria"));
        return recurso;
    }
}
