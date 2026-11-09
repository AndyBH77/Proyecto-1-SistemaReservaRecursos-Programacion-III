package cr.ac.una.eif206.frontend.vista.alertas;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Iconos del area de alertas, DIBUJADOS con codigo (no necesitan archivos .png):
 *  - VER: una carpeta (parte de atras roja y frente azul), como en el enunciado.
 *  - DESCARTAR: un circulo rojo con una raya blanca.
 */
public final class IconosAlertas {

    public static final Icon VER = new IconoCarpeta();
    public static final Icon DESCARTAR = new IconoDescartar();

    private IconosAlertas() {
    }

    /** Activa el suavizado de bordes para que los dibujos no se vean "pixelados". */
    private static Graphics2D preparar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g2;
    }

    /** Carpeta abierta: fondo rojo (con pestana) y frente azul inclinado. */
    private static class IconoCarpeta implements Icon {
        private static final int ANCHO = 22;
        private static final int ALTO = 18;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = preparar(g);
            g2.translate(x, y);

            // Parte de atras (roja) con su pestana
            Path2D atras = new Path2D.Double();
            atras.moveTo(1, 3);
            atras.lineTo(8, 3);
            atras.lineTo(10, 5);
            atras.lineTo(19, 5);
            atras.lineTo(19, 16);
            atras.lineTo(1, 16);
            atras.closePath();
            g2.setColor(new Color(214, 64, 69));
            g2.fill(atras);
            g2.setColor(new Color(120, 30, 35));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(atras);

            // Frente (azul), un poco inclinado como carpeta abierta
            Path2D frente = new Path2D.Double();
            frente.moveTo(4, 8);
            frente.lineTo(21, 8);
            frente.lineTo(18, 16);
            frente.lineTo(1, 16);
            frente.closePath();
            g2.setColor(new Color(110, 180, 235));
            g2.fill(frente);
            g2.setColor(new Color(30, 70, 120));
            g2.draw(frente);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return ANCHO;
        }

        @Override
        public int getIconHeight() {
            return ALTO;
        }
    }

    /** Circulo rojo con una raya blanca horizontal (simbolo de "quitar"). */
    private static class IconoDescartar implements Icon {
        private static final int TAMANO = 18;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = preparar(g);
            g2.translate(x, y);

            g2.setColor(new Color(235, 64, 52));
            g2.fillOval(0, 0, TAMANO - 1, TAMANO - 1);

            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(4, TAMANO / 2.0 - 2, TAMANO - 9, 3, 2, 2));

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return TAMANO;
        }

        @Override
        public int getIconHeight() {
            return TAMANO;
        }
    }
}