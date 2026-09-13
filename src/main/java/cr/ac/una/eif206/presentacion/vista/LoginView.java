package cr.ac.una.eif206.presentacion.vista;

import cr.ac.una.eif206.util.Constantes;
import cr.ac.una.eif206.util.Estilos;

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
import java.awt.Color;
import java.awt.Font;
import java.awt.BorderLayout;

/**
 * Ventana de inicio de sesion. Es una vista "tonta": solo arma los
 * componentes graficos. Toda la logica vive en LoginController.
 *
 * NOTA: el registro de nuevos funcionarios ya NO se hace desde aqui.
 * Ahora solo el Administrador puede crear funcionarios, desde la pestaña
 * "Funcionarios" de la ventana principal (ver FuncionarioView).
 */
public class LoginView extends JFrame {

    private final JTextField txtId = new JTextField(15);
    private final JPasswordField txtClave = new JPasswordField(15);
    private final JButton btnIngresar = new JButton("Ingresar");
    private final JButton btnOlvideClave = new JButton("Olvidé mi clave");
    private final JButton btnSalir = new JButton("Salir");

    public LoginView() {
        super("Sistema de Reservas - Ingreso");
        construirInterfaz();
    }
  // se le hace cambio para que se vea igual que las pestañas internas
    /**
     * Construye la interfaz gráfica del Login y aplica
     * el mismo diseño verde de la ventana principal.
     */
    private void construirInterfaz() {

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 380);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panelCabecera = new JPanel(new BorderLayout());
        panelCabecera.setBackground(Estilos.VERDE_OSCURO);
        panelCabecera.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel lblTitulo = new JLabel("SISTEMA DE RESERVAS");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 21));

        JLabel lblSubtitulo = new JLabel("Inicio de sesión");
        lblSubtitulo.setForeground(new Color(205, 240, 233));
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel panelTitulos = new JPanel(new GridLayout(2, 1, 0, 3));
        panelTitulos.setOpaque(false);
        panelTitulos.add(lblTitulo);
        panelTitulos.add(lblSubtitulo);
        panelCabecera.add(panelTitulos, BorderLayout.WEST);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(Estilos.VERDE_CLARO);
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 7, 7, 7);
        c.fill = GridBagConstraints.HORIZONTAL;

        //Etiqueta del ID.
        JLabel lblId = new JLabel("ID:");
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblId.setForeground(Estilos.TEXTO_OSCURO);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        panelFormulario.add(lblId, c);

        //Campo del ID.
        txtId.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtId.setBackground(Color.WHITE);
        txtId.setForeground(Estilos.TEXTO_OSCURO);
        txtId.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilos.BORDE), BorderFactory.createEmptyBorder(7, 9, 7, 9)));

        c.gridx = 1;
        c.weightx = 1;
        panelFormulario.add(txtId, c);

        //Etiqueta de la clave.
        JLabel lblClave = new JLabel("Clave:");
        lblClave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblClave.setForeground(Estilos.TEXTO_OSCURO);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        panelFormulario.add(lblClave, c);

        //Campo de contraseña.
        txtClave.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtClave.setBackground(Color.WHITE);
        txtClave.setForeground(Estilos.TEXTO_OSCURO);
        txtClave.setCaretColor(Estilos.VERDE_OSCURO);
        txtClave.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilos.BORDE), BorderFactory.createEmptyBorder(7, 9, 7, 9)));

        c.gridx = 1;
        c.weightx = 1;

        panelFormulario.add(txtClave, c);
        JPanel panelBotones = new JPanel(new GridLayout(3, 1, 8, 8));
        panelBotones.setOpaque(false);

        //Aplica el estilo general definido en Estilos.
        Estilos.aplicarEstiloBoton(btnIngresar);
        Estilos.aplicarEstiloBoton(btnOlvideClave);
        Estilos.aplicarEstiloBoton(btnSalir);

        btnIngresar.setBackground(Estilos.VERDE_PRINCIPAL); //Ingresar es la acción principal.
        btnIngresar.setForeground(Color.WHITE);
        btnOlvideClave.setBackground(Color.WHITE); //Olvidé mi clave es una acción secundaria.
        btnOlvideClave.setForeground(Estilos.VERDE_OSCURO);
        btnSalir.setBackground(Estilos.ROJO_CANCELAR); //Salir utiliza rojo porque cierra el programa.
        btnSalir.setForeground(Color.WHITE);

        panelBotones.add(btnIngresar);
        panelBotones.add(btnOlvideClave);
        panelBotones.add(btnSalir);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.weightx = 1;
        c.insets = new Insets(18, 7, 7, 7);

        panelFormulario.add(panelBotones, c);

        add(panelCabecera, BorderLayout.NORTH); //Agrega la cabecera y el formulario al JFrame.
        add(panelFormulario, BorderLayout.CENTER);
    }

    public JTextField getTxtId() {
        return txtId;
    }

    public JPasswordField getTxtClave() {
        return txtClave;
    }

    public JButton getBtnIngresar() {
        return btnIngresar;
    }

    public JButton getBtnOlvideClave() {
        return btnOlvideClave;
    }

    public JButton getBtnSalir() {
        return btnSalir;
    }
}