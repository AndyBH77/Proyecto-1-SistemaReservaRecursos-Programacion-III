package cr.ac.una.eif206.presentacion.vista;

import cr.ac.una.eif206.util.Constantes;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Ventana para registrar un nuevo funcionario. El sistema genera la clave
 * automaticamente y la envia al correo indicado (no se pide clave aqui).
 */
public class RegistroView extends JDialog {

    private final JTextField txtId = new JTextField(15);
    private final JTextField txtNombre = new JTextField(15);
    private final JTextField txtTelefono = new JTextField(15);
    private final JTextField txtCorreo = new JTextField(15);
    private final JButton btnRegistrar = new JButton("Registrar y enviar clave");
    private final JButton btnCancelar = new JButton("Cancelar");

    public RegistroView(JFrame padre) {
        super(padre, "Registrar nuevo usuario", true); // true = modal
        construirInterfaz();
    }

    private void construirInterfaz() {
        setSize(Constantes.TAMANO_VENTANA_REGISTRO);
        setResizable(false);
        setLocationRelativeTo(getParent());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        agregarFila(panel, c, 0, "ID nuevo:", txtId);
        agregarFila(panel, c, 1, "Nombre completo:", txtNombre);
        agregarFila(panel, c, 2, "Teléfono:", txtTelefono);
        agregarFila(panel, c, 3, "Correo electrónico:", txtCorreo);

        JLabel nota = new JLabel("<html><i>La clave se generará automáticamente<br>y se enviará a su correo.</i></html>");
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        panel.add(nota, c);

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnCancelar);

        c.gridy = 5;
        panel.add(panelBotones, c);

        add(panel);
    }

    private void agregarFila(JPanel panel, GridBagConstraints c, int fila, String etiqueta, JTextField campo) {
        c.gridx = 0;
        c.gridy = fila;
        c.gridwidth = 1;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        panel.add(campo, c);
    }

    public JTextField getTxtId() {
        return txtId;
    }

    public JTextField getTxtNombre() {
        return txtNombre;
    }

    public JTextField getTxtTelefono() {
        return txtTelefono;
    }

    public JTextField getTxtCorreo() {
        return txtCorreo;
    }

    public JButton getBtnRegistrar() {
        return btnRegistrar;
    }

    public JButton getBtnCancelar() {
        return btnCancelar;
    }
}
