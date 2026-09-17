package cr.ac.una.eif206.presentacion.vista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class RecursoView extends JPanel {

    private final JTextField txtId = new JTextField(15);
    private final JTextField txtIdCategoria = new JTextField(15);
    private final JTextField txtDescripcion = new JTextField(25);

    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");

    private final JTextField txtBusqueda = new JTextField(20);
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnImprimir = new JButton("Imprimir PDF");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "ID Categoría", "Descripción"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public RecursoView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(construirPanelFormulario(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(construirPanelBusqueda(), BorderLayout.SOUTH);
    }

    private JPanel construirPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Recurso"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        panel.add(txtId, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("ID Categoría:"), gbc);
        gbc.gridx = 1;
        panel.add(txtIdCategoria, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        panel.add(txtDescripcion, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(panelBotones, gbc);

        return panel;
    }

    private JPanel construirPanelBusqueda() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Buscar:"));
        panel.add(txtBusqueda);
        panel.add(btnBuscar);
        panel.add(btnImprimir);
        return panel;
    }

    public JTextField getTxtId() {
        return txtId;
    }

    public JTextField getTxtIdCategoria() {
        return txtIdCategoria;
    }

    public JTextField getTxtDescripcion() {
        return txtDescripcion;
    }

    public JButton getBtnGuardar() {
        return btnGuardar;
    }

    public JButton getBtnEliminar() {
        return btnEliminar;
    }

    public JButton getBtnLimpiar() {
        return btnLimpiar;
    }

    public JTextField getTxtBusqueda() {
        return txtBusqueda;
    }

    public JButton getBtnBuscar() {
        return btnBuscar;
    }

    public JButton getBtnImprimir() {
        return btnImprimir;
    }

    public DefaultTableModel getModeloTabla() {
        return modeloTabla;
    }

    public JTable getTabla() {
        return tabla;
    }
}