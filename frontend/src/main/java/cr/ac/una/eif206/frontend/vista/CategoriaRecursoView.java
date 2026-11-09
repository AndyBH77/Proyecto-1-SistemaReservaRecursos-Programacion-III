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
 * Pestana de Categorias de recursos (solo ADMINISTRADOR).
 * Vista "tonta": solo arma los componentes. La logica esta en CategoriaRecursoController.
 *
 * El id NO se escribe: lo genera MySQL (por eso el campo es solo de lectura).
 */
public class CategoriaRecursoView extends JPanel {

    private final JTextField txtId = new JTextField(8);
    private final JTextField txtDescripcion = new JTextField(30);

    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");

    private final JTextField txtBuscar = new JTextField(25);
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnImprimir = new JButton("Imprimir PDF");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Id", "Descripción"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public CategoriaRecursoView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtId.setEditable(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(construirPanelFormulario(), BorderLayout.NORTH);
        add(construirPanelTabla(), BorderLayout.CENTER);
    }

    private JPanel construirPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Categoría"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Id (automático):"), c);
        c.gridx = 1;
        panel.add(txtId, c);

        c.gridx = 0;
        c.gridy = 1;
        panel.add(new JLabel("Descripción:"), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(txtDescripcion, c);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnAgregar);
        botones.add(btnModificar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        panel.add(botones, c);

        return panel;
    }

    private JPanel construirPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Categorías registradas"));

        JPanel barraBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barraBusqueda.add(new JLabel("Buscar por descripción:"));
        barraBusqueda.add(txtBuscar);
        barraBusqueda.add(btnBuscar);
        barraBusqueda.add(btnImprimir);

        panel.add(barraBusqueda, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    public JTextField getTxtId() { return txtId; }
    public JTextField getTxtDescripcion() { return txtDescripcion; }
    public JButton getBtnAgregar() { return btnAgregar; }
    public JButton getBtnModificar() { return btnModificar; }
    public JButton getBtnEliminar() { return btnEliminar; }
    public JButton getBtnLimpiar() { return btnLimpiar; }
    public JTextField getTxtBuscar() { return txtBuscar; }
    public JButton getBtnBuscar() { return btnBuscar; }
    public JButton getBtnImprimir() { return btnImprimir; }
    public DefaultTableModel getModeloTabla() { return modeloTabla; }
    public JTable getTabla() { return tabla; }
}