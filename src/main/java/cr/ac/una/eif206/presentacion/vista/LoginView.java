package cr.ac.una.eif206.presentacion.vista;

import cr.ac.una.eif206.util.Constantes;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * Ventana de inicio de sesion. Es una vista "tonta": solo arma los
 * componentes graficos. Toda la logica (validar credenciales, abrir
 * la ventana principal, etc.) vive en LoginController.
 */
public class LoginView extends JFrame {

    private final JTextField txtId = new JTextField(15);
    private final JPasswordField txtClave = new JPasswordField(15);
    private final JButton btnIngresar = new JButton("Ingresar");
    private final JButton btnRegistrarse = new JButton("Registrarse");
    private final JButton btnOlvideClave = new JButton("Olvidé mi clave");
    private final JButton btnSalir = new JButton("Salir");

    public LoginView() {
        super("Sistema de Reservas - Ingreso");
        construirInterfaz();
    }

    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Constantes.TAMANO_VENTANA_LOGIN);
        setResizable(false); // Requerimiento: ventanas de tamano fijo
        setLocationRelativeTo(null); // Centrar en pantalla

        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        panelPrincipal.add(new JLabel("ID:"), c);
        c.gridx = 1;
        panelPrincipal.add(txtId, c);

        c.gridx = 0;
        c.gridy = 1;
        panelPrincipal.add(new JLabel("Clave:"), c);
        c.gridx = 1;
        panelPrincipal.add(txtClave, c);

        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 5, 5));
        panelBotones.add(btnIngresar);
        panelBotones.add(btnSalir);
        panelBotones.add(btnRegistrarse);
        panelBotones.add(btnOlvideClave);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        panelPrincipal.add(panelBotones, c);

        add(panelPrincipal);
    }

    // Getters para que el controlador pueda leer/enlazar eventos
    public JTextField getTxtId() {
        return txtId;
    }

    public JPasswordField getTxtClave() {
        return txtClave;
    }

    public JButton getBtnIngresar() {
        return btnIngresar;
    }

    public JButton getBtnRegistrarse() {
        return btnRegistrarse;
    }

    public JButton getBtnOlvideClave() {
        return btnOlvideClave;
    }

    public JButton getBtnSalir() {
        return btnSalir;
    }
}
