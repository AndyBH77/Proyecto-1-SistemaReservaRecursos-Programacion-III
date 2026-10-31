package cr.ac.una.eif206.frontend.util;

import java.awt.Dimension;

/**
 * Constantes de la interfaz grafica (tamanos fijos de ventanas, formatos).
 * Las ventanas NO se pueden redimensionar (setResizable(false)).
 */
public final class Constantes {

    private Constantes() {
    }

    // La principal es mas ancha que en el Proyecto 1 para que quepa el area de alertas.
    public static final Dimension TAMANO_VENTANA_PRINCIPAL = new Dimension(1250, 700);
    public static final Dimension TAMANO_VENTANA_LOGIN = new Dimension(460, 380);
    public static final Dimension TAMANO_VENTANA_REGISTRO = new Dimension(420, 380);
    public static final Dimension TAMANO_VENTANA_CAMBIAR_CLAVE = new Dimension(350, 250);

    /** Ancho del area de alertas (lado derecho de la ventana principal). */
    public static final int ANCHO_AREA_ALERTAS = 300;

    public static final String FORMATO_FECHA = "dd/MM/yyyy";
    public static final String FORMATO_HORA = "HH:mm";
}