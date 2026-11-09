package cr.ac.una.eif206.frontend.vista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Pestana de Funcionarios (solo ADMINISTRADOR).
 * Vista "tonta": solo arma los componentes. La logica esta en FuncionarioController.
 *
 * Al registrar, el sistema genera la clave y la envia al correo indicado.
 */
public class FuncionarioView extends JPanel {

    private final JTextField txtId = new JTextField(12);
    private final JTextField txtNombre = new JTextField(25);
    private final JTextField txtTelefono = new JTextField(12);
    private final JTextField txtCorreo = new JTextField(25);

    private final JButton btnRegistrar = new JButton("Registrar y enviar clave");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");

    private final JTextField txtBuscar = new JTextField(25);
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnImprimir = new JButton("Imprimir PDF");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Id", "Nombre", "Teléfono", "Correo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public FuncionarioView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(construirPanelFormulario(), BorderLayout.NORTH);
        add(construirPanelTabla(), BorderLayout.CENTER);
    }

    private JPanel construirPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Funcionario"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Id:"), c);
        c.gridx = 1;
        panel.add(txtId, c);
        c.gridx = 2;
        panel.add(new JLabel("Nombre:"), c);
        c.gridx = 3;
        c.weightx = 1;
        panel.add(txtNombre, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        panel.add(new JLabel("Teléfono:"), c);
        c.gridx = 1;
        panel.add(txtTelefono, c);
        c.gridx = 2;
        panel.add(new JLabel("Correo:"), c);
        c.gridx = 3;
        c.weightx = 1;
        panel.add(txtCorreo, c);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnRegistrar);
        botones.add(btnModificar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 4;
        c.weightx = 0;
        panel.add(botones, c);

        return panel;
    }

    private JPanel construirPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Funcionarios registrados"));

        JPanel barraBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraBusqueda.add(new JLabel("Buscar por id o nombre:"));
        barraBusqueda.add(txtBuscar);
        barraBusqueda.add(btnBuscar);
        barraBusqueda.add(btnImprimir);

        panel.add(barraBusqueda, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    public JTextField getTxtId() { return txtId; }
    public JTextField getTxtNombre() { return txtNombre; }
    public JTextField getTxtTelefono() { return txtTelefono; }
    public JTextField getTxtCorreo() { return txtCorreo; }
    public JButton getBtnRegistrar() { return btnRegistrar; }
    public JButton getBtnModificar() { return btnModificar; }
    public JButton getBtnEliminar() { return btnEliminar; }
    public JButton getBtnLimpiar() { return btnLimpiar; }
    public JTextField getTxtBuscar() { return txtBuscar; }
    public JButton getBtnBuscar() { return btnBuscar; }
    public JButton getBtnImprimir() { return btnImprimir; }
    public DefaultTableModel getModeloTabla() { return modeloTabla; }
    public JTable getTabla() { return tabla; }
}