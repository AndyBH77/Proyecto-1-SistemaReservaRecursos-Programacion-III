package cr.ac.una.eif206.frontend.vista;

import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import cr.ac.una.eif206.common.modelo.TablaConsulta;
import cr.ac.una.eif206.frontend.util.Constantes;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Date;

/**
 * Pestana de Calendarizacion (administrador y funcionario).
 * Vista "tonta": al elegir fecha y categoria muestra la matriz horas x recursos.
 * La logica esta en CalendarizacionController.
 */
public class CalendarizacionView extends JPanel {

    private final JSpinner spinnerFecha = new JSpinner(new SpinnerDateModel());
    private final JComboBox<CategoriaRecurso> comboCategoria = new JComboBox<>();
    private final JButton btnConsultar = new JButton("Consultar");
    private final JButton btnImprimir = new JButton("Imprimir PDF");
    private final JLabel lblEstado = new JLabel("Seleccione una fecha y una categoría.");
    private final JTable tabla = new JTable();

    public CalendarizacionView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        spinnerFecha.setEditor(new JSpinner.DateEditor(spinnerFecha, Constantes.FORMATO_FECHA));
        spinnerFecha.setValue(new Date());
        configurarTabla(tabla, true);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filtros.setBorder(BorderFactory.createTitledBorder("Consulta de calendarización"));
        filtros.add(new JLabel("Fecha:"));
        filtros.add(spinnerFecha);
        filtros.add(new JLabel("Categoría:"));
        comboCategoria.setPrototypeDisplayValue(new CategoriaRecurso(0, "Categoría de recurso larga"));
        filtros.add(comboCategoria);
        filtros.add(btnConsultar);
        filtros.add(btnImprimir);

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(filtros, BorderLayout.NORTH);
        superior.add(lblEstado, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Calendarización de recursos"));

        add(superior, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(crearLeyenda(), BorderLayout.SOUTH);
    }

    private JPanel crearLeyenda() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel disponible = new JLabel("  Disponible  ");
        disponible.setOpaque(true);
        disponible.setBackground(CeldaMatrizRenderer.COLOR_DISPONIBLE);
        JLabel reservado = new JLabel("  Reservado  ");
        reservado.setOpaque(true);
        reservado.setBackground(CeldaMatrizRenderer.COLOR_RESERVADO);
        panel.add(new JLabel("Leyenda:"));
        panel.add(disponible);
        panel.add(reservado);
        return panel;
    }

    public void setCategorias(Iterable<CategoriaRecurso> categorias) {
        DefaultComboBoxModel<CategoriaRecurso> modelo = new DefaultComboBoxModel<>();
        for (CategoriaRecurso c : categorias) {
            modelo.addElement(c);
        }
        comboCategoria.setModel(modelo);
    }

    /** Muestra la matriz que mando el backend. */
    public void mostrarTabla(TablaConsulta datos) {
        llenarMatriz(tabla, datos, 190, true);
    }

    // ---------- ayudas compartidas con ActividadesView ----------

    /** Una JTable de solo lectura que dibuja sus celdas con CeldaMatrizRenderer. */
    static void configurarTabla(JTable tabla, boolean marcarDisponible) {
        tabla.setDefaultEditor(Object.class, null);
        tabla.setDefaultRenderer(Object.class, new CeldaMatrizRenderer(marcarDisponible));
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setRowHeight(36);
    }

    /**
     * Pone los encabezados y filas en la tabla, fija el ancho de las columnas y
     * ajusta la altura de cada fila al texto mas largo (varias actividades en una celda).
     */
    static void llenarMatriz(JTable tabla, TablaConsulta datos, int anchoColumna, boolean marcarDisponible) {
        tabla.setModel(new DefaultTableModel(datos.getFilas(), datos.getEncabezados()) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        });
        tabla.setDefaultRenderer(Object.class, new CeldaMatrizRenderer(marcarDisponible));
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 65 : anchoColumna);
        }
        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            int alto = 36;
            for (int col = 1; col < tabla.getColumnCount(); col++) {
                Component c = tabla.prepareRenderer(tabla.getCellRenderer(fila, col), fila, col);
                c.setSize(new Dimension(anchoColumna, Short.MAX_VALUE));
                alto = Math.max(alto, c.getPreferredSize().height);
            }
            tabla.setRowHeight(fila, alto);
        }
    }

    public JSpinner getSpinnerFecha() { return spinnerFecha; }
    public JComboBox<CategoriaRecurso> getComboCategoria() { return comboCategoria; }
    public JButton getBtnConsultar() { return btnConsultar; }
    public JButton getBtnImprimir() { return btnImprimir; }
    public JLabel getLblEstado() { return lblEstado; }
    public JTable getTabla() { return tabla; }

    /**
     * Dibuja cada celda con varias lineas y color: la hora en gris, "Disponible"
     * en verde y lo reservado en amarillo. En Actividades las celdas vacias
     * quedan blancas (no hay nada "disponible" que marcar).
     */
    public static class CeldaMatrizRenderer extends JTextArea implements TableCellRenderer {

        static final Color COLOR_DISPONIBLE = new Color(226, 244, 234);
        static final Color COLOR_RESERVADO = new Color(255, 238, 196);
        private static final Color COLOR_HORA = new Color(224, 230, 238);

        private final boolean marcarDisponible;

        public CeldaMatrizRenderer(boolean marcarDisponible) {
            this.marcarDisponible = marcarDisponible;
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 5));
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
                                                       boolean foco, int fila, int columna) {
            String texto = valor == null ? "" : valor.toString();
            setText(texto);
            if (columna == 0) {
                setBackground(COLOR_HORA);
                setForeground(new Color(35, 48, 68));
            } else if (texto.isBlank()) {
                setBackground(Color.WHITE);
                setForeground(Color.DARK_GRAY);
            } else if (marcarDisponible && texto.equals("Disponible")) {
                setBackground(COLOR_DISPONIBLE);
                setForeground(new Color(42, 103, 65));
            } else {
                setBackground(COLOR_RESERVADO);
                setForeground(new Color(90, 61, 18));
            }
            if (seleccionado) {
                setBackground(new Color(181, 232, 218));
            }
            setToolTipText(texto.isBlank() ? null : "<html>" + texto.replace("\n", "<br>") + "</html>");
            return this;
        }
    }
}
