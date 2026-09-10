package cr.ac.una.eif206.presentacion.vista;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * Panel generico de "placeholder" para las pestañas que todavia no
 * desarrolla esta persona (Funcionarios, Categorias, Recursos,
 * Calendarizacion, Actividades, Estadisticas). Cada integrante del equipo
 * debe reemplazar su panel correspondiente por su propia vista real.
 */
public class PanelEnConstruccion extends JPanel {

    public PanelEnConstruccion(String nombreFuncionalidad) {
        setLayout(new BorderLayout());
        JLabel etiqueta = new JLabel(
                "Funcionalidad \"" + nombreFuncionalidad + "\" — en construcción por otro integrante del equipo",
                SwingConstants.CENTER);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.ITALIC));
        add(etiqueta, BorderLayout.CENTER);
    }
}
