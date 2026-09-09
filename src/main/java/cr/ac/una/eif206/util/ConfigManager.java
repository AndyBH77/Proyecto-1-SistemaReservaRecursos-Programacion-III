package cr.ac.una.eif206.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Carga la configuracion del archivo src/main/resources/config.properties.
 * Ahi se guardan datos "sensibles" o que cambian segun quien ejecute el
 * programa (usuario de Gmail, clave de aplicacion, api key de la IA), para
 * NO dejarlos escritos directamente en el codigo fuente.
 */
public class ConfigManager {

    private static final Properties propiedades = new Properties();
    private static boolean cargado = false;

    private ConfigManager() {
    }

    private static void cargarSiHaceFalta() {
        if (cargado) {
            return;
        }
        try (InputStream in = ConfigManager.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                propiedades.load(in);
            } else {
                System.err.println("ADVERTENCIA: no se encontro config.properties en resources.");
            }
        } catch (IOException e) {
            System.err.println("Error cargando config.properties: " + e.getMessage());
        }
        cargado = true;
    }

    public static String obtener(String llave) {
        cargarSiHaceFalta();
        return propiedades.getProperty(llave, "");
    }

    public static String obtener(String llave, String valorPorDefecto) {
        cargarSiHaceFalta();
        return propiedades.getProperty(llave, valorPorDefecto);
    }
}
