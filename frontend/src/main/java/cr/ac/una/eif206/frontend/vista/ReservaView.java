package cr.ac.una.eif206.frontend.vista;

import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import cr.ac.una.eif206.frontend.util.Constantes;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Pestana de Reservas (solo FUNCIONARIO).
 * Vista "tonta": solo arma los componentes. La logica esta en ReservaController.
 */
public class ReservaView extends JPanel {

    private final JTextField txtFrase = new JTextField();
    private final JButton btnExtraerIA = new JButton("Extraer IA");

    private final JTextField txtActividad = new JTextField();
    private final JSpinner spinnerFecha = crearSpinner(Constantes.FORMATO_FECHA);
    private final JSpinner spinnerHoraInicio = crearSpinner(Constantes.FORMATO_HORA);
    private final JSpinner spinnerHoraFin = crearSpinner(Constantes.FORMATO_HORA);

    // La lista guarda los OBJETOS categoria; en pantalla se ve su toString() (la descripcion)
    private final DefaultListModel<CategoriaRecurso> modeloListaCategorias = new DefaultListModel<>();
    private final JList<CategoriaRecurso> listaCategorias = new JList<>(modeloListaCategorias);

    private final JButton btnReservar = new JButton("Reservar");
    private final JButton btnModificar = new JButton("Modificar reserva seleccionada");
    private final JButton btnCancelarReserva = new JButton("Cancelar reserva seleccionada");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnImprimir = new JButton("Imprimir");

    private final DefaultTableModel modeloTablaReservas = new DefaultTableModel(
            new Object[]{"Id", "Actividad", "Fecha", "Horario", "Recursos", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tablaReservas = new JTable(modeloTablaReservas);

    public ReservaView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(construirPanelFormulario(), BorderLayout.NORTH);
        add(construirPanelTabla(), BorderLayout.CENTER);
    }

    private static JSpinner crearSpinner(String formato) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, formato));
        return spinner;
    }

    private JPanel construirPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Nueva reserva / Editar reserva"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Frase:"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        c.weightx = 1;
        panel.add(txtFrase, c);
        c.gridx = 4;
        c.gridwidth = 1;
        c.weightx = 0;
        panel.add(btnExtraerIA, c);

        c.gridx = 0;
        c.gridy = 1;
        panel.add(new JLabel("Actividad:"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        panel.add(txtActividad, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        panel.add(new JLabel("Fecha:"), c);
        c.gridx = 1;
        panel.add(spinnerFecha, c);
        c.gridx = 2;
        panel.add(new JLabel("Hora inicio:"), c);
        c.gridx = 3;
        panel.add(spinnerHoraInicio, c);

        c.gridx = 2;
        c.gridy = 3;
        panel.add(new JLabel("Hora fin:"), c);
        c.gridx = 3;
        panel.add(spinnerHoraFin, c);

        c.gridx = 0;
        c.gridy = 4;
        panel.add(new JLabel("Categorías:"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        c.gridheight = 2;
        listaCategorias.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaCategorias.setVisibleRowCount(4);
        panel.add(new JScrollPane(listaCategorias), c);
        c.gridheight = 1;

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnReservar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnCancelarReserva);
        panelBotones.add(btnLimpiar);

        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 5;
        panel.add(panelBotones, c);

        return panel;
    }

    private JPanel construirPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Mis reservas"));

        tablaReservas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);

        JPanel panelDerecha = new JPanel();
        panelDerecha.add(btnImprimir);
        panel.add(panelDerecha, BorderLayout.EAST);

        return panel;
    }

    public JTextField getTxtFrase() { return txtFrase; }
    public JButton getBtnExtraerIA() { return btnExtraerIA; }
    public JTextField getTxtActividad() { return txtActividad; }
    public JSpinner getSpinnerFecha() { return spinnerFecha; }
    public JSpinner getSpinnerHoraInicio() { return spinnerHoraInicio; }
    public JSpinner getSpinnerHoraFin() { return spinnerHoraFin; }
    public DefaultListModel<CategoriaRecurso> getModeloListaCategorias() { return modeloListaCategorias; }
    public JList<CategoriaRecurso> getListaCategorias() { return listaCategorias; }
    public JButton getBtnReservar() { return btnReservar; }
    public JButton getBtnModificar() { return btnModificar; }
    public JButton getBtnCancelarReserva() { return btnCancelarReserva; }
    public JButton getBtnLimpiar() { return btnLimpiar; }
    public JButton getBtnImprimir() { return btnImprimir; }
    public DefaultTableModel getModeloTablaReservas() { return modeloTablaReservas; }
    public JTable getTablaReservas() { return tablaReservas; }
}