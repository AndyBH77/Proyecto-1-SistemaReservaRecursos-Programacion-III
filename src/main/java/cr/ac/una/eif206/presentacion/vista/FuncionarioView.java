package cr.ac.una.eif206.presentacion.vista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class FuncionarioView extends JPanel {

    private final JTextField txtId = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JButton btnRegistrar = new JButton("Registrar y enviar clave");
    private final JButton btnModificar = new JButton("Modificar datos");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnEliminar = new JButton("Eliminar seleccionado");

    private final JTextField txtBuscar = new JTextField();
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnMostrarTodos = new JButton("Mostrar todos");

    private final TitledBorder bordeTabla = BorderFactory.createTitledBorder("Funcionarios registrados");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Id", "Nombre", "Teléfono", "Correo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tablaFuncionarios = new JTable(modeloTabla);

    public FuncionarioView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(construirPanelFormulario(), BorderLayout.NORTH);
        add(construirPanelTabla(), BorderLayout.CENTER);
    }

    private JPanel construirPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Registrar / modificar funcionario"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("ID:"), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(txtId, c);

        c.gridx = 2;
        c.weightx = 0;
        panel.add(new JLabel("Nombre:"), c);
        c.gridx = 3;
        c.weightx = 1;
        panel.add(txtNombre, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        panel.add(new JLabel("Teléfono:"), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(txtTelefono, c);

        c.gridx = 2;
        c.weightx = 0;
        panel.add(new JLabel("Correo:"), c);
        c.gridx = 3;
        c.weightx = 1;
        panel.add(txtCorreo, c);

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnLimpiar);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 4;
        c.weightx = 0;
        panel.add(panelBotones, c);

        return panel;
    }

    private JPanel construirPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(bordeTabla);

        JPanel panelBusqueda = new JPanel();
        panelBusqueda.add(new JLabel("Buscar (id o nombre):"));
        txtBuscar.setColumns(15);
        panelBusqueda.add(txtBuscar);
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnMostrarTodos);

        panel.add(panelBusqueda, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaFuncionarios), BorderLayout.CENTER);

        JPanel panelDerecha = new JPanel();
        panelDerecha.add(btnEliminar);
        panel.add(panelDerecha, BorderLayout.EAST);

        return panel;
    }

    public JTextField getTxtId() { return txtId; }
    public JTextField getTxtNombre() { return txtNombre; }
    public JTextField getTxtTelefono() { return txtTelefono; }
    public JTextField getTxtCorreo() { return txtCorreo; }
    public JButton getBtnRegistrar() { return btnRegistrar; }
    public JButton getBtnModificar() { return btnModificar; }
    public JButton getBtnLimpiar() { return btnLimpiar; }
    public JButton getBtnEliminar() { return btnEliminar; }
    public JTextField getTxtBuscar() { return txtBuscar; }
    public JButton getBtnBuscar() { return btnBuscar; }
    public JButton getBtnMostrarTodos() { return btnMostrarTodos; }
    public DefaultTableModel getModeloTabla() { return modeloTabla; }
    public JTable getTablaFuncionarios() { return tablaFuncionarios; }
}