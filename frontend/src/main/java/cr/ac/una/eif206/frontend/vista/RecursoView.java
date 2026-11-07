package cr.ac.una.eif206.frontend.vista;

import cr.ac.una.eif206.common.modelo.CategoriaRecurso;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
 * Pestana de Recursos (solo ADMINISTRADOR).
 * Vista "tonta": solo arma los componentes. La logica esta en RecursoController.
 *
 * Tiene dos combos de categorias:
 *  - el del FILTRO (arriba de la tabla), con la opcion "Todas las categorias";
 *  - el del FORMULARIO, solo con categorias reales (se elige una al guardar).
 */
public class RecursoView extends JPanel {

    private final JTextField txtId = new JTextField(15);
    private final JComboBox<CategoriaRecurso> comboCategoria = new JComboBox<>();
    private final JTextField txtDescripcion = new JTextField(30);

    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnLimpiar = new JButton("Limpiar");

    private final JComboBox<CategoriaRecurso> comboFiltro = new JComboBox<>();
    private final JButton btnImprimir = new JButton("Imprimir PDF");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Número de activo", "Categoría", "Descripción"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public RecursoView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(construirPanelFormulario(), BorderLayout.NORTH);
        add(construirPanelTabla(), BorderLayout.CENTER);
    }

    private JPanel construirPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Recurso"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Número de activo:"), c);
        c.gridx = 1;
        panel.add(txtId, c);
        c.gridx = 2;
        panel.add(new JLabel("Categoría:"), c);
        c.gridx = 3;
        c.weightx = 1;
        panel.add(comboCategoria, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        panel.add(new JLabel("Descripción:"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        c.weightx = 1;
        panel.add(txtDescripcion, c);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnAgregar);
        botones.add(btnModificar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 4;
        panel.add(botones, c);
        return panel;
    }

    private JPanel construirPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Recursos registrados"));

        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtro.add(new JLabel("Filtrar por categoría:"));
        filtro.add(comboFiltro);
        filtro.add(btnImprimir);

        panel.add(filtro, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    /** Reemplaza las categorias de los dos combos (el del filtro lleva "Todas" de primera). */
    public void setCategorias(Iterable<CategoriaRecurso> categorias, CategoriaRecurso opcionTodas) {
        DefaultComboBoxModel<CategoriaRecurso> modeloForm = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<CategoriaRecurso> modeloFiltro = new DefaultComboBoxModel<>();
        modeloFiltro.addElement(opcionTodas);
        for (CategoriaRecurso categoria : categorias) {
            modeloForm.addElement(categoria);
            modeloFiltro.addElement(categoria);
        }
        comboCategoria.setModel(modeloForm);
        comboFiltro.setModel(modeloFiltro);
    }

    public JTextField getTxtId() { return txtId; }
    public JComboBox<CategoriaRecurso> getComboCategoria() { return comboCategoria; }
    public JTextField getTxtDescripcion() { return txtDescripcion; }
    public JButton getBtnAgregar() { return btnAgregar; }
    public JButton getBtnModificar() { return btnModificar; }
    public JButton getBtnEliminar() { return btnEliminar; }
    public JButton getBtnLimpiar() { return btnLimpiar; }
    public JComboBox<CategoriaRecurso> getComboFiltro() { return comboFiltro; }
    public JButton getBtnImprimir() { return btnImprimir; }
    public DefaultTableModel getModeloTabla() { return modeloTabla; }
    public JTable getTabla() { return tabla; }
}
