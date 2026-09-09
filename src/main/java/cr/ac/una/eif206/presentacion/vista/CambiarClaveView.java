package cr.ac.una.eif206.presentacion.vista;

import cr.ac.una.eif206.util.Constantes;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class CambiarClaveView extends JDialog {

    private final JPasswordField txtClaveActual = new JPasswordField(15);
    private final JPasswordField txtClaveNueva = new JPasswordField(15);
    private final JPasswordField txtConfirmarClave = new JPasswordField(15);
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnCancelar = new JButton("Cancelar");

    public CambiarClaveView(JFrame padre) {
        super(padre, "Cambiar clave", true);
        construirInterfaz();
    }

    private void construirInterfaz() {
        setSize(Constantes.TAMANO_VENTANA_CAMBIAR_CLAVE);
        setResizable(false);
        setLocationRelativeTo(getParent());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Clave actual:"), c);
        c.gridx = 1;
        panel.add(txtClaveActual, c);

        c.gridx = 0;
        c.gridy = 1;
        panel.add(new JLabel("Clave nueva:"), c);
        c.gridx = 1;
        panel.add(txtClaveNueva, c);

        c.gridx = 0;
        c.gridy = 2;
        panel.add(new JLabel("Confirmar clave:"), c);
        c.gridx = 1;
        panel.add(txtConfirmarClave, c);

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        panel.add(panelBotones, c);

        add(panel);
    }

    public JPasswordField getTxtClaveActual() {
        return txtClaveActual;
    }

    public JPasswordField getTxtClaveNueva() {
        return txtClaveNueva;
    }

    public JPasswordField getTxtConfirmarClave() {
        return txtConfirmarClave;
    }

    public JButton getBtnGuardar() {
        return btnGuardar;
    }

    public JButton getBtnCancelar() {
        return btnCancelar;
    }
}
