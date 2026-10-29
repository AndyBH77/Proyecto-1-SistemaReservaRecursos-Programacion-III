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
 * Este DAO es el EJEMPLO a seguir para los demas DAO:
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

            while (rs.next()) {          // avanza fila por fila
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Busca una categoria por su id. Optional.empty() si no existe. */
    public Optional<CategoriaRecurso> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, descripcion FROM categoria WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);            // llena el primer ?

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
     *
     * Si la descripcion ya existe, MySQL lanza un error por el UNIQUE
     * (SQLIntegrityConstraintViolationException); el servicio debe avisarlo.
     */
    public CategoriaRecurso insertar(CategoriaRecurso categoria) throws SQLException {
        String sql = "INSERT INTO categoria (descripcion) VALUES (?)";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, categoria.getDescripcion());
            ps.executeUpdate();          // INSERT, UPDATE y DELETE usan executeUpdate

            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    categoria.setId(llaves.getInt(1));
                }
            }
        }
        return categoria;
    }

    // ------------------------------------------------------------------
    // BENJA: agregar aqui buscarPorDescripcion(String texto),
    //        actualizar(CategoriaRecurso c) y eliminar(int id),
    //        siguiendo el mismo patron.
    // ------------------------------------------------------------------

    /** Convierte la fila actual del ResultSet en un objeto CategoriaRecurso. */
    private CategoriaRecurso mapear(ResultSet rs) throws SQLException {
        return new CategoriaRecurso(rs.getInt("id"), rs.getString("descripcion"));
    }
}