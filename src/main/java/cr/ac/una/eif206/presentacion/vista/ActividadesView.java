package cr.ac.una.eif206.presentacion.vista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Date;

public class ActividadesView extends JPanel {
    private final JSpinner spinnerFecha = new JSpinner(new SpinnerDateModel());
    private final JButton btnAnterior = new JButton("Semana anterior");
    private final JButton btnCargar = new JButton("Cargar semana");
    private final JButton btnSiguiente = new JButton("Semana siguiente");
    private final JButton btnImprimir = new JButton("Imprimir PDF");
    private final JLabel lblSemana = new JLabel("Semana seleccionada");
    private final JTable tabla = new JTable();

    public ActividadesView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        spinnerFecha.setEditor(new JSpinner.DateEditor(spinnerFecha, "dd/MM/yyyy"));
        spinnerFecha.setValue(new Date());
        tabla.setDefaultEditor(Object.class, null);
        tabla.setDefaultRenderer(Object.class, new CalendarizacionView.CeldaCalendarizacionRenderer());
        tabla.setRowHeight(52);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.getTableHeader().setBackground(new Color(49, 73, 102));
        tabla.getTableHeader().setForeground(Color.WHITE);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 8));
        filtros.setBorder(BorderFactory.createTitledBorder("Semana"));
        filtros.add(new JLabel("Fecha de referencia:")); filtros.add(spinnerFecha);
        filtros.add(btnAnterior); filtros.add(btnCargar); filtros.add(btnSiguiente); filtros.add(btnImprimir);
        JPanel superior = new JPanel(new BorderLayout());
        superior.add(filtros, BorderLayout.NORTH);
        superior.add(lblSemana, BorderLayout.SOUTH);
        add(superior, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Actividades semanales"));
        add(scroll, BorderLayout.CENTER);
    }

    public void actualizarTabla(String[] columnas, Object[][] datos) {
        tabla.setModel(new DefaultTableModel(datos, columnas) {
            @Override public boolean isCellEditable(int f, int c) { return false; }
        });
        tabla.setDefaultRenderer(Object.class, new CalendarizacionView.CeldaCalendarizacionRenderer());
        tabla.setRowHeight(52);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(58);
        for (int i = 1; i < columnas.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(145);
    }

    public JSpinner getSpinnerFecha() { return spinnerFecha; }
    public JButton getBtnAnterior() { return btnAnterior; }
    public JButton getBtnCargar() { return btnCargar; }
    public JButton getBtnSiguiente() { return btnSiguiente; }
    public JButton getBtnImprimir() { return btnImprimir; }
    public JLabel getLblSemana() { return lblSemana; }
    public JTable getTabla() { return tabla; }
}
