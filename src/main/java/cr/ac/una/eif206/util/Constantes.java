package cr.ac.una.eif206.util;

import java.awt.Dimension;

/**
 * Constantes generales de la aplicacion (tamanos de ventana, formatos, etc.)
 */
public class Constantes {

    private Constantes() {
    }

    // Tamanos fijos para las ventanas (requerimiento: no se pueden redimensionar).
    public static final Dimension TAMANO_VENTANA_PRINCIPAL = new Dimension(950, 650);
    public static final Dimension TAMANO_VENTANA_LOGIN = new Dimension(380, 260);
    public static final Dimension TAMANO_VENTANA_REGISTRO = new Dimension(420, 380);
    public static final Dimension TAMANO_VENTANA_CAMBIAR_CLAVE = new Dimension(350, 250);

    public static final String FORMATO_FECHA = "dd/MM/yyyy";
    public static final String FORMATO_HORA = "HH:mm";
}
