package cr.ac.una.eif206.presentacion.vista;

import cr.ac.una.eif206.modelo.CategoriaRecurso;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Date;

public class CalendarizacionView extends JPanel {
    private final JSpinner spinnerFecha = new JSpinner(new SpinnerDateModel());
    private final JComboBox<CategoriaRecurso> comboCategoria = new JComboBox<>();
    private final JButton btnCargar = new JButton("Cargar");
    private final JButton btnImprimir = new JButton("Imprimir PDF");
    private final JLabel lblEstado = new JLabel("Seleccione una fecha y una categoría.");
    private final JTable tabla = new JTable();

    public CalendarizacionView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        spinnerFecha.setEditor(new JSpinner.DateEditor(spinnerFecha, "dd/MM/yyyy"));
        spinnerFecha.setValue(new Date());
        tabla.setDefaultEditor(Object.class, null);
        tabla.setDefaultRenderer(Object.class, new CeldaCalendarizacionRenderer());
        tabla.setRowHeight(48);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.getTableHeader().setBackground(new Color(49, 73, 102));
        tabla.getTableHeader().setForeground(Color.WHITE);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filtros.setBorder(BorderFactory.createTitledBorder("Filtros de calendarización"));
        filtros.add(new JLabel("Fecha:"));
        filtros.add(spinnerFecha);
        filtros.add(new JLabel("Categoría:"));
        comboCategoria.setPrototypeDisplayValue(new CategoriaRecurso("", "Categoría de recurso larga"));
        filtros.add(comboCategoria);
        filtros.add(btnCargar);
        filtros.add(btnImprimir);

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(filtros, BorderLayout.NORTH);
        superior.add(lblEstado, BorderLayout.SOUTH);
        add(superior, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Calendarización de recursos"));
        add(scroll, BorderLayout.CENTER);
        add(crearLeyenda(), BorderLayout.SOUTH);
    }

    private JPanel crearLeyenda() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel disponible = new JLabel("  Disponible  ");
        disponible.setOpaque(true); disponible.setBackground(new Color(226, 244, 234));
        JLabel reservado = new JLabel("  Reservado  ");
        reservado.setOpaque(true); reservado.setBackground(new Color(255, 238, 196));
        panel.add(new JLabel("Leyenda:")); panel.add(disponible); panel.add(reservado);
        return panel;
    }

    public void actualizarTabla(String[] columnas, Object[][] datos) {
        tabla.setModel(new DefaultTableModel(datos, columnas) {
            @Override public boolean isCellEditable(int f, int c) { return false; }
        });
        tabla.setDefaultRenderer(Object.class, new CeldaCalendarizacionRenderer());
        tabla.setRowHeight(48);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(65);
        for (int i = 1; i < columnas.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(190);
    }

    public JSpinner getSpinnerFecha() { return spinnerFecha; }
    public JComboBox<CategoriaRecurso> getComboCategoria() { return comboCategoria; }
    public JButton getBtnCargar() { return btnCargar; }
    public JButton getBtnImprimir() { return btnImprimir; }
    public JLabel getLblEstado() { return lblEstado; }
    public JTable getTabla() { return tabla; }

    /**
     * Da color y permite varias líneas en cada celda de la matriz.
     * Es interna porque solamente la necesitan las vistas.
     */
    public static class CeldaCalendarizacionRenderer extends JTextArea implements TableCellRenderer {
        public CeldaCalendarizacionRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 5));
        }

        /** Aplica colores según la celda sea una hora, esté libre o esté ocupada. */
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
                                                       boolean foco, int fila, int columna) {
            String texto = valor == null ? "" : valor.toString();
            setText(texto);
            if (columna == 0) {
                setBackground(new Color(224, 230, 238));
                setForeground(new Color(35, 48, 68));
            } else if (texto.equals("Disponible") || texto.isBlank()) {
                setBackground(new Color(226, 244, 234));
                setForeground(new Color(42, 103, 65));
            } else {
                setBackground(new Color(255, 238, 196));
                setForeground(new Color(90, 61, 18));
            }
            if (seleccionado) setBackground(new Color(179, 210, 242));
            setToolTipText(texto.isBlank() ? null : "<html>" + texto.replace("\n", "<br>") + "</html>");
            return this;
        }
    }
}
