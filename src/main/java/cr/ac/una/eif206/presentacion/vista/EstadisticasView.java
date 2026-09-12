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
import java.awt.GridLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Date;
import java.util.Map;

public class EstadisticasView extends JPanel {
    private final JSpinner desdeRecursos = crearFecha();
    private final JSpinner hastaRecursos = crearFecha();
    private final JButton cargarRecursos = new JButton("Cargar");
    private final JButton imprimirRecursos = new JButton("PDF");
    private final JTable tablaRecursos = crearTabla("Categoría");
    private final GraficoBarrasPanel graficoRecursos = new GraficoBarrasPanel("Recursos usados", new Color(36, 112, 196));
    private final JLabel estadoRecursos = new JLabel("Seleccione el período que desea consultar.");

    private final JSpinner desdeActividades = crearFecha();
    private final JSpinner hastaActividades = crearFecha();
    private final JButton cargarActividades = new JButton("Cargar");
    private final JButton imprimirActividades = new JButton("PDF");
    private final JTable tablaActividades = crearTabla("Semana");
    private final GraficoBarrasPanel graficoActividades = new GraficoBarrasPanel("Actividades realizadas", new Color(226, 92, 64));
    private final JLabel estadoActividades = new JLabel("Seleccione el período que desea consultar.");

    public EstadisticasView() {
        setLayout(new GridLayout(1, 2, 10, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(crearSeccion("Recursos", desdeRecursos, hastaRecursos, cargarRecursos, imprimirRecursos,
                tablaRecursos, graficoRecursos, estadoRecursos));
        add(crearSeccion("Actividades", desdeActividades, hastaActividades, cargarActividades,
                imprimirActividades, tablaActividades, graficoActividades, estadoActividades));
    }

    private static JSpinner crearFecha() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setValue(new Date());
        return spinner;
    }

    private JTable crearTabla(String primeraColumna) {
        JTable tabla = new JTable(new DefaultTableModel(new Object[]{primeraColumna, "Cantidad"}, 0) {
            @Override public boolean isCellEditable(int f, int c) { return false; }
        });
        tabla.setRowHeight(24);
        tabla.getTableHeader().setBackground(new Color(49, 73, 102));
        tabla.getTableHeader().setForeground(Color.WHITE);
        return tabla;
    }

    private JPanel crearSeccion(String titulo, JSpinner desde, JSpinner hasta, JButton cargar,
                                JButton imprimir, JTable tabla, GraficoBarrasPanel grafico, JLabel estado) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        JPanel filtros = new JPanel(new GridLayout(2, 3, 5, 4));
        filtros.add(new JLabel("Desde:")); filtros.add(desde); filtros.add(cargar);
        filtros.add(new JLabel("Hasta:")); filtros.add(hasta); filtros.add(imprimir);
        JPanel superior = new JPanel(new BorderLayout(4, 4));
        superior.add(filtros, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Estadísticas"));
        superior.add(scroll, BorderLayout.CENTER);
        superior.add(estado, BorderLayout.SOUTH);
        panel.add(superior, BorderLayout.NORTH);
        grafico.setBorder(BorderFactory.createTitledBorder("Gráfico"));
        panel.add(grafico, BorderLayout.CENTER);
        return panel;
    }

    public void actualizarRecursos(Map<String, Integer> datos) {
        llenarTabla(tablaRecursos, datos); graficoRecursos.setDatos(datos);
        estadoRecursos.setText(datos.isEmpty() ? "No existen recursos reservados en este período."
                : "Categorías encontradas: " + datos.size());
        imprimirRecursos.setEnabled(!datos.isEmpty());
    }
    public void actualizarActividades(Map<String, Integer> datos) {
        llenarTabla(tablaActividades, datos); graficoActividades.setDatos(datos);
        boolean tieneActividades = datos.values().stream().anyMatch(cantidad -> cantidad > 0);
        estadoActividades.setText(tieneActividades ? "Semanas consultadas: " + datos.size()
                : "No existen actividades programadas en este período.");
        imprimirActividades.setEnabled(tieneActividades);
    }
    private void llenarTabla(JTable tabla, Map<String, Integer> datos) {
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0);
        datos.forEach((nombre, cantidad) -> modelo.addRow(new Object[]{nombre, cantidad}));
    }

    public JSpinner getDesdeRecursos() { return desdeRecursos; }
    public JSpinner getHastaRecursos() { return hastaRecursos; }
    public JButton getCargarRecursos() { return cargarRecursos; }
    public JButton getImprimirRecursos() { return imprimirRecursos; }
    public JTable getTablaRecursos() { return tablaRecursos; }
    public GraficoBarrasPanel getGraficoRecursos() { return graficoRecursos; }
    public JSpinner getDesdeActividades() { return desdeActividades; }
    public JSpinner getHastaActividades() { return hastaActividades; }
    public JButton getCargarActividades() { return cargarActividades; }
    public JButton getImprimirActividades() { return imprimirActividades; }
    public JTable getTablaActividades() { return tablaActividades; }
    public GraficoBarrasPanel getGraficoActividades() { return graficoActividades; }

    /**
     * Dibuja un gráfico de barras usando solamente Java Swing.
     * Al ser una clase interna no se necesita un archivo adicional.
     */
    public static class GraficoBarrasPanel extends JPanel {
        private Map<String, Integer> datos = new LinkedHashMap<>();
        private final Color colorBarra;
        private final String titulo;

        public GraficoBarrasPanel(String titulo, Color colorBarra) {
            this.titulo = titulo;
            this.colorBarra = colorBarra;
            setBackground(Color.WHITE);
        }

        /** Recibe los nuevos valores del gráfico y solicita que Swing lo vuelva a dibujar. */
        public void setDatos(Map<String, Integer> datos) {
            this.datos = new LinkedHashMap<>(datos);
            repaint();
        }

        /** Dibuja título, ejes, barras, cantidades y etiquetas. */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int ancho = getWidth();
            int alto = getHeight();
            g2.setColor(new Color(35, 48, 68));
            g2.setFont(getFont().deriveFont(java.awt.Font.BOLD, 15f));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(titulo, Math.max(10, (ancho - fm.stringWidth(titulo)) / 2), 22);
            int izquierda = 42, derecha = 15, arriba = 38, abajo = 42;
            int areaAncho = Math.max(1, ancho - izquierda - derecha);
            int areaAlto = Math.max(1, alto - arriba - abajo);
            g2.setColor(new Color(185, 193, 203));
            g2.drawLine(izquierda, arriba, izquierda, arriba + areaAlto);
            g2.drawLine(izquierda, arriba + areaAlto, izquierda + areaAncho, arriba + areaAlto);
            if (datos.isEmpty()) {
                g2.setColor(Color.GRAY);
                g2.drawString("Sin datos para mostrar", izquierda + 20, arriba + areaAlto / 2);
                g2.dispose();
                return;
            }
            int maximo = Math.max(1, datos.values().stream().max(Integer::compareTo).orElse(1));
            int espacio = Math.max(1, areaAncho / datos.size());
            int barra = Math.max(12, Math.min(48, espacio / 2));
            int i = 0;
            g2.setFont(getFont().deriveFont(10f));
            for (Map.Entry<String, Integer> entrada : datos.entrySet()) {
                int x = izquierda + i * espacio + (espacio - barra) / 2;
                int h = (int) ((entrada.getValue() / (double) maximo) * (areaAlto - 22));
                int y = arriba + areaAlto - h;
                g2.setColor(colorBarra);
                g2.fillRoundRect(x, y, barra, h, 8, 8);
                g2.setColor(new Color(45, 55, 70));
                String valor = String.valueOf(entrada.getValue());
                g2.drawString(valor, x + (barra - g2.getFontMetrics().stringWidth(valor)) / 2, y - 4);
                String etiqueta = abreviar(entrada.getKey(), 11);
                g2.drawString(etiqueta, Math.max(izquierda,
                        x + (barra - g2.getFontMetrics().stringWidth(etiqueta)) / 2), arriba + areaAlto + 17);
                i++;
            }
            g2.dispose();
        }

        /** Acorta etiquetas largas para que no se encimen debajo de las barras. */
        private String abreviar(String texto, int maximo) {
            return texto.length() <= maximo ? texto : texto.substring(0, maximo - 3) + "...";
        }
    }
}
