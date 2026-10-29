package cr.ac.una.eif206.backend.datos;

import cr.ac.una.eif206.backend.util.ConfigManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Punto unico para obtener una conexion a MySQL.
 * Los datos (url, usuario, clave) salen de config.properties.
 *
 * Uso en un DAO (la conexion se cierra sola al terminar el try):
 *   try (Connection con = ConexionBD.obtenerConexion()) { ... }
 *
 * Se abre una conexion nueva por cada operacion: asi cada hilo del
 * servidor usa la suya y no hay problemas de concurrencia.
 */
public final class ConexionBD {

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        String url = ConfigManager.obtener("db.url");
        String usuario = ConfigManager.obtener("db.usuario");
        String clave = ConfigManager.obtener("db.clave");

        if (url.isEmpty()) {
            throw new SQLException("Falta 'db.url' en backend/src/main/resources/config.properties");
        }
        // El driver de MySQL (mysql-connector-j) se registra solo al estar en el pom.
        return DriverManager.getConnection(url, usuario, clave);
    }
}