package cr.ac.una.eif206.frontend.vista;

import cr.ac.una.eif206.frontend.util.Constantes;

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
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pestana de Estadisticas (administrador y funcionario).
 * Dos secciones lado a lado, cada una con su periodo, tabla y grafico de barras:
 * Recursos (cantidad por categoria) y Actividades (cantidad por semana).
 * Vista "tonta": la logica esta en EstadisticasController.
 */
public class EstadisticasView extends JPanel {

    private final JSpinner desdeRecursos = crearFecha(-1);
    private final JSpinner hastaRecursos = crearFecha(1);
    private final JButton consultarRecursos = new JButton("Consultar");
    private final JButton imprimirRecursos = new JButton("Imprimir PDF");
    private final JTable tablaRecursos = crearTabla("Categoría");
    private final GraficoBarrasPanel graficoRecursos =
            new GraficoBarrasPanel("Recursos reservados por categoría", new Color(36, 112, 196));
    private final JLabel estadoRecursos = new JLabel("Seleccione el período que desea consultar.");

    private final JSpinner desdeActividades = crearFecha(-1);
    private final JSpinner hastaActividades = crearFecha(1);
    private final JButton consultarActividades = new JButton("Consultar");
    private final JButton imprimirActividades = new JButton("Imprimir PDF");
    private final JTable tablaActividades = crearTabla("Semana (inicia el lunes)");
    private final GraficoBarrasPanel graficoActividades =
            new GraficoBarrasPanel("Actividades por semana", new Color(226, 92, 64));
    private final JLabel estadoActividades = new JLabel("Seleccione el período que desea consultar.");

    public EstadisticasView() {
        setLayout(new GridLayout(1, 2, 10, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(crearSeccion("Recursos", desdeRecursos, hastaRecursos, consultarRecursos, imprimirRecursos,
                tablaRecursos, graficoRecursos, estadoRecursos));
        add(crearSeccion("Actividades", desdeActividades, hastaActividades, consultarActividades,
                imprimirActividades, tablaActividades, graficoActividades, estadoActividades));
    }

    /** Spinner de fecha; "mesesDesdeHoy" = -1 (un mes antes) o 1 (un mes despues) del dia de hoy. */
    private static JSpinner crearFecha(int mesesDesdeHoy) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, Constantes.FORMATO_FECHA));
        LocalDate fecha = LocalDate.now().plusMonths(mesesDesdeHoy);
        spinner.setValue(Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        return spinner;
    }

    private static JTable crearTabla(String primeraColumna) {
        return new JTable(new DefaultTableModel(new Object[]{primeraColumna, "Cantidad"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        });
    }

    private JPanel crearSeccion(String titulo, JSpinner desde, JSpinner hasta, JButton consultar,
                                JButton imprimir, JTable tabla, GraficoBarrasPanel grafico, JLabel estado) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(titulo));

        JPanel filtros = new JPanel(new GridLayout(2, 3, 5, 4));
        filtros.add(new JLabel("Desde:"));
        filtros.add(desde);
        filtros.add(consultar);
        filtros.add(new JLabel("Hasta:"));
        filtros.add(hasta);
        filtros.add(imprimir);

        // tabla arriba y grafico abajo, cada uno con la mitad del espacio
        JPanel centro = new JPanel(new GridLayout(2, 1, 0, 8));
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Datos"));
        grafico.setBorder(BorderFactory.createTitledBorder(grafico.getTitulo()));
        centro.add(scroll);
        centro.add(grafico);

        panel.add(filtros, BorderLayout.NORTH);
        panel.add(centro, BorderLayout.CENTER);
        panel.add(estado, BorderLayout.SOUTH);
        return panel;
    }

    public void mostrarRecursos(Map<String, Integer> datos) {
        llenarTabla(tablaRecursos, datos);
        graficoRecursos.setDatos(datos);
        estadoRecursos.setText(datos.isEmpty()
                ? "No hay recursos reservados en este período."
                : "Categorías reservadas: " + datos.size());
    }

    public void mostrarActividades(Map<String, Integer> datos) {
        llenarTabla(tablaActividades, datos);
        graficoActividades.setDatos(datos);
        int total = datos.values().stream().mapToInt(Integer::intValue).sum();
        estadoActividades.setText(total == 0
                ? "No hay actividades programadas en este período."
                : "Semanas consultadas: " + datos.size() + " | Actividades: " + total);
    }

    private void llenarTabla(JTable tabla, Map<String, Integer> datos) {
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0);
        datos.forEach((nombre, cantidad) -> modelo.addRow(new Object[]{nombre, cantidad}));
    }

    public JSpinner getDesdeRecursos() { return desdeRecursos; }
    public JSpinner getHastaRecursos() { return hastaRecursos; }
    public JButton getConsultarRecursos() { return consultarRecursos; }
    public JButton getImprimirRecursos() { return imprimirRecursos; }
    public JTable getTablaRecursos() { return tablaRecursos; }
    public JSpinner getDesdeActividades() { return desdeActividades; }
    public JSpinner getHastaActividades() { return hastaActividades; }
    public JButton getConsultarActividades() { return consultarActividades; }
    public JButton getImprimirActividades() { return imprimirActividades; }
    public JTable getTablaActividades() { return tablaActividades; }

    /** Grafico de barras dibujado a mano con Swing (no necesita librerias). */
    public static class GraficoBarrasPanel extends JPanel {

        private Map<String, Integer> datos = new LinkedHashMap<>();
        private final Color colorBarra;
        private final String titulo;

        public GraficoBarrasPanel(String titulo, Color colorBarra) {
            this.titulo = titulo;
            this.colorBarra = colorBarra;
            setBackground(Color.WHITE);
        }

        public String getTitulo() {
            return titulo;
        }

        public void setDatos(Map<String, Integer> datos) {
            this.datos = new LinkedHashMap<>(datos);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int ancho = getWidth();
            int alto = getHeight();

            int izquierda = 36, derecha = 12, arriba = 16, abajo = 34;
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
            int barra = Math.max(4, Math.min(48, espacio * 2 / 3));
            // si no caben todas las etiquetas, se escribe una de cada "salto" para que no se encimen
            int salto = Math.max(1, (int) Math.ceil(64.0 / espacio));

            g2.setFont(getFont().deriveFont(10f));
            FontMetrics fm = g2.getFontMetrics();
            int i = 0;
            for (Map.Entry<String, Integer> entrada : datos.entrySet()) {
                int x = izquierda + i * espacio + (espacio - barra) / 2;
                int h = (int) ((entrada.getValue() / (double) maximo) * (areaAlto - 18));
                int y = arriba + areaAlto - h;
                g2.setColor(colorBarra);
                g2.fillRoundRect(x, y, barra, h, 6, 6);

                g2.setColor(new Color(45, 55, 70));
                String valor = String.valueOf(entrada.getValue());
                g2.drawString(valor, x + (barra - fm.stringWidth(valor)) / 2, y - 3);
                if (i % salto == 0) {
                    String etiqueta = abreviar(entrada.getKey(), Math.max(6, Math.min(24, espacio * salto / 6)));
                    g2.drawString(etiqueta, Math.max(2, x + (barra - fm.stringWidth(etiqueta)) / 2),
                            arriba + areaAlto + 14);
                }
                i++;
            }
            g2.dispose();
        }

        private String abreviar(String texto, int maximo) {
            return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + "…";
        }
    }
}
