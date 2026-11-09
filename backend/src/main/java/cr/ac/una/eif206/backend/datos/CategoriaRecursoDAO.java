package cr.ac.una.eif206.backend.datos;

import cr.ac.una.eif206.common.modelo.CategoriaRecurso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a la tabla "categoria" de MySQL.
 *
 * Patron que siguen todos los DAO:
 *  1. Escribir el SQL con ? en lugar de los datos.
 *  2. Abrir conexion y PreparedStatement dentro de un try (...) para que se cierren solos.
 *  3. Llenar los ? con setString / setInt (numerados desde 1).
 *  4. Recorrer el ResultSet y convertir cada fila en un objeto del modelo (metodo mapear).
 */
public class CategoriaRecursoDAO {

    /** Todas las categorias, ordenadas por descripcion. */
    public List<CategoriaRecurso> listarTodas() throws SQLException {
        String sql = "SELECT id, descripcion FROM categoria ORDER BY descripcion";
        List<CategoriaRecurso> lista = new ArrayList<>();

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Categorias cuya descripcion contiene el texto (sin importar mayusculas). */
    public List<CategoriaRecurso> buscarPorDescripcion(String texto) throws SQLException {
        String sql = "SELECT id, descripcion FROM categoria WHERE descripcion LIKE ? ORDER BY descripcion";
        List<CategoriaRecurso> lista = new ArrayList<>();

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + texto + "%");   // % = "cualquier texto antes/despues"

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Busca una categoria por su id. Optional.empty() si no existe. */
    public Optional<CategoriaRecurso> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, descripcion FROM categoria WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Inserta una categoria nueva. MySQL genera el id (AUTO_INCREMENT)
     * y aqui se recupera con getGeneratedKeys() para devolverlo en el objeto.
     * Si la descripcion ya existe, MySQL lanza SQLIntegrityConstraintViolationException (UNIQUE).
     */
    public CategoriaRecurso insertar(CategoriaRecurso categoria) throws SQLException {
        String sql = "INSERT INTO categoria (descripcion) VALUES (?)";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, categoria.getDescripcion());
            ps.executeUpdate();

            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    categoria.setId(llaves.getInt(1));
                }
            }
        }
        return categoria;
    }

    /** Cambia la descripcion. Devuelve cuantas filas cambio (0 = no existia). */
    public int actualizar(CategoriaRecurso categoria) throws SQLException {
        String sql = "UPDATE categoria SET descripcion = ? WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoria.getDescripcion());
            ps.setInt(2, categoria.getId());
            return ps.executeUpdate();
        }
    }

    /**
     * Borra la categoria. Devuelve cuantas filas borro (0 = no existia).
     * Si tiene recursos, MySQL lo impide (ON DELETE RESTRICT) y lanza
     * SQLIntegrityConstraintViolationException.
     */
    public int eliminar(int id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    /** Convierte la fila actual del ResultSet en un objeto CategoriaRecurso. */
    private CategoriaRecurso mapear(ResultSet rs) throws SQLException {
        return new CategoriaRecurso(rs.getInt("id"), rs.getString("descripcion"));
    }
}