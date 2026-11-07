package cr.ac.una.eif206.backend.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Carga la configuracion del archivo backend/src/main/resources/config.properties.
 * Ahi se guardan datos que cambian segun quien ejecute el programa
 * (conexion a MySQL, puertos, Gmail, Gemini), para NO dejarlos en el codigo.
 *
 * Ese archivo NO se sube a GitHub; la plantilla es config.example.properties.
 */
public final class ConfigManager {

    private static final Properties propiedades = new Properties();
    private static boolean cargado = false;

    private ConfigManager() {
    }

    /**
     * Lee el archivo solo la primera vez que se necesita.
     * "synchronized" porque el servidor tiene varios hilos y podrian
     * intentar cargarlo al mismo tiempo.
     */
    private static synchronized void cargarSiHaceFalta() {
        if (cargado) {
            return;
        }
        try (InputStream in = ConfigManager.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                propiedades.load(in);
            } else {
                System.err.println("ADVERTENCIA: no se encontro config.properties en backend/src/main/resources. "
                        + "Copie config.example.properties como config.properties y llenelo.");
            }
        } catch (IOException e) {
            System.err.println("Error cargando config.properties: " + e.getMessage());
        }
        cargado = true;
    }

    /** Devuelve el valor de la llave, o "" si no existe. */
    public static String obtener(String llave) {
        cargarSiHaceFalta();
        return propiedades.getProperty(llave, "").trim();
    }

    /** Devuelve el valor de la llave, o el valor por defecto si no existe. */
    public static String obtener(String llave, String valorPorDefecto) {
        cargarSiHaceFalta();
        return propiedades.getProperty(llave, valorPorDefecto).trim();
    }

    /** Para valores numericos, como los puertos del servidor. */
    public static int obtenerEntero(String llave, int valorPorDefecto) {
        String valor = obtener(llave);
        try {
            return valor.isEmpty() ? valorPorDefecto : Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            System.err.println("Valor invalido para " + llave + ": " + valor + ". Se usa " + valorPorDefecto);
            return valorPorDefecto;
        }
    }
}