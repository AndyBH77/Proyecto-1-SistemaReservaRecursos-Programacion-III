package cr.ac.una.eif206.util;

import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;

/**
 * Contiene los colores y métodos utilizados para personalizar
 * toda la interfaz gráfica del sistema.
 */
public class Estilos {
    // Colores principales.
    public static final Color VERDE_OSCURO = new Color(21, 94, 89);
    public static final Color VERDE_PRINCIPAL = new Color(40, 168, 121);
    public static final Color VERDE_CLARO = new Color(232, 247, 242);
    public static final Color VERDE_FILA = new Color(239, 250, 247);
    public static final Color AZUL_PETROLEO = new Color(26, 142, 177);
    public static final Color AMARILLO_IA = new Color(230, 162, 60);
    public static final Color ROJO_CANCELAR = new Color(217, 83, 79);
    public static final Color TEXTO_OSCURO = new Color(36, 50, 61);
    public static final Color BLANCO = Color.WHITE;
    public static final Color BORDE = new Color(93, 190, 169);
    public static final Color GRIS_CLARO = new Color(245, 247, 248);

    /**
     * Constructor privado.
     *
     * Evita crear objetos Estilos porque todos los métodos
     * y colores de esta clase son estáticos.
     */
    private Estilos() {
    }

    /**
     * Aplica el estilo a toda la ventana principal.
     *
     * @param contenedor componente principal de la ventana.
     * @param pestanas pestañas utilizadas para navegar.
     */
    public static void aplicarEstiloGeneral(Container contenedor, JTabbedPane pestanas) {
        contenedor.setBackground(VERDE_CLARO);
        aplicarEstiloRecursivo(contenedor); // Recorre todos los componentes de la ventana.
        aplicarEstiloPestanas(pestanas); // Personaliza las pestañas.
    }

    /**
     * Recorre todos los componentes de un contenedor.
     *
     * Dependiendo del tipo de componente, aplica el estilo
     * correspondiente.
     */
    private static void aplicarEstiloRecursivo(Container contenedor) {

        for (Component componente : contenedor.getComponents()) {
            componente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            if (componente instanceof JButton boton) {
                aplicarEstiloBoton(boton);
            }
            if (componente instanceof JTable tabla) {
                aplicarEstiloTabla(tabla);
            }
            if (componente instanceof JTextField campo) {
                aplicarEstiloCampo(campo);
            }
            if (componente instanceof JSpinner spinner) {
                aplicarEstiloSpinner(spinner);
            }
            if (componente instanceof JList<?> lista) {
                aplicarEstiloLista(lista);
            }
            if (componente instanceof JLabel etiqueta) {
                etiqueta.setForeground(TEXTO_OSCURO);
            }
            if (componente instanceof JScrollPane scroll) {
                aplicarEstiloScroll(scroll);
            }
            if (componente instanceof JPanel panel) {
                aplicarEstiloPanel(panel);
            }
            if (componente instanceof Container hijo) { // Si el componente también contiene otros componentes, continúa recorriendo su contenido.
                aplicarEstiloRecursivo(hijo);
            }
        }
    }

    /**
     * Aplica el color correcto a cada botón según la acción
     * que realiza.
     */
    public static void aplicarEstiloBoton(JButton boton) {
        boton.setUI(new BasicButtonUI()); //BasicButtonUI permite que setBackground() funcione correctamente aunque Windows utilice su propio estilo.

        boton.setContentAreaFilled(true);
        boton.setBorderPainted(true);
        boton.setFocusPainted(false);
        boton.setOpaque(true);

        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setMargin(new Insets(8, 15, 8, 15));
        actualizarColorBoton(boton); //Aplica el color inicial.
         //Si el controlador habilita o deshabilita el botón, sus colores se vuelven a calcular.
         // Esto es necesario para botones como Modificar y Cancelar, que pueden comenzar deshabilitados.
        if (boton.getClientProperty("listenerColorAgregado") == null) {
            boton.addPropertyChangeListener("enabled", evento -> actualizarColorBoton(boton));
            boton.putClientProperty("listenerColorAgregado", true);
        }
    }

    /**
     * Decide el color del botón dependiendo de su texto
     * y de si se encuentra habilitado.
     */
    private static void actualizarColorBoton(JButton boton) {
        if (!boton.isEnabled()) { //Si el botón está deshabilitado, utiliza colores grises para indicar que todavía no puede usarse.
            boton.setBackground(new Color(210, 218, 216));
            boton.setForeground(new Color(105, 115, 113));
            boton.setBorder(new BordeRedondeado(new Color(180, 190, 188), 12));
            return;
        }
        String texto = boton.getText();
        if (texto == null) {
            texto = "";
        }
        String textoMinuscula = texto.toLowerCase();
        Color colorFondo;
        Color colorTexto;
        Color colorBorde;
        if (textoMinuscula.contains("extraer") || textoMinuscula.contains("ia")) {
            colorFondo = AMARILLO_IA;
            colorTexto = TEXTO_OSCURO;
            colorBorde = new Color(200, 135, 30);
        } else if (textoMinuscula.contains("cancelar") || textoMinuscula.contains("eliminar") || textoMinuscula.contains("borrar") || textoMinuscula.contains("salir")) { //Botones de cancelación o eliminación.
            colorFondo = ROJO_CANCELAR;
            colorTexto = BLANCO;
            colorBorde = new Color(180, 65, 62);
        } else if (textoMinuscula.contains("modificar") || textoMinuscula.contains("editar")) { //Botones de modificación.
            colorFondo = AZUL_PETROLEO;
            colorTexto = BLANCO;
            colorBorde = new Color(18, 110, 142);
        } else if (textoMinuscula.contains("reservar") || textoMinuscula.contains("guardar") || textoMinuscula.contains("registrar") || textoMinuscula.contains("cargar") || textoMinuscula.contains("ingresar")) {  //Botones principales.
            colorFondo = VERDE_PRINCIPAL;
            colorTexto = BLANCO;
            colorBorde = VERDE_OSCURO;
        } else { //Botones secundarios.
            colorFondo = BLANCO;
            colorTexto = VERDE_OSCURO;
            colorBorde = VERDE_PRINCIPAL;
        }
        boton.setBackground(colorFondo);
        boton.setForeground(colorTexto);
        boton.setBorder(new BordeRedondeado(colorBorde, 12));
        boton.revalidate(); //Obliga a Swing a actualizar visualmente el botón.
        boton.repaint();
    }
    /**
     * Configura los colores y tamaños de una JTable.
     */
    public static void aplicarEstiloTabla(JTable tabla) {
        tabla.setBackground(BLANCO);
        tabla.setForeground(TEXTO_OSCURO);
        tabla.setSelectionBackground(new Color(181, 232, 218));
        tabla.setSelectionForeground(TEXTO_OSCURO);
        tabla.setGridColor(new Color(210, 232, 226));
        tabla.setRowHeight(28);
        tabla.setShowHorizontalLines(true);
        tabla.setShowVerticalLines(true);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JTableHeader encabezado = tabla.getTableHeader();
        if (encabezado != null) {
            encabezado.setBackground(VERDE_OSCURO);
            encabezado.setForeground(BLANCO);
            encabezado.setFont(new Font("Segoe UI", Font.BOLD, 13));
            encabezado.setReorderingAllowed(false);
            encabezado.setOpaque(true);
        }
        TableCellRenderer rendererActual = tabla.getDefaultRenderer(Object.class); //Solo instala las filas alternadas cuando la tabla todavía utiliza el renderer predeterminado. Esto evita reemplazar los colores especiales de Calendarización y Actividades.
        if (rendererActual instanceof UIResource) {tabla.setDefaultRenderer(Object.class, new RenderizadorFilas());
        }
    }

    /**
     * Personaliza los campos de texto.
     */
    private static void aplicarEstiloCampo(JTextField campo) {
        campo.setBackground(BLANCO);
        campo.setForeground(TEXTO_OSCURO);
        campo.setCaretColor(VERDE_OSCURO);
        campo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(145, 180, 175)), BorderFactory.createEmptyBorder(5, 7, 5, 7)));
    }

    /**
     * Personaliza los selectores de fecha y hora.
     */
    private static void aplicarEstiloSpinner(JSpinner spinner) {
        spinner.setBackground(BLANCO);
        spinner.setForeground(TEXTO_OSCURO);
        spinner.setBorder(BorderFactory.createLineBorder(new Color(145, 180, 175)));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField campo = ((JSpinner.DefaultEditor) editor).getTextField();
            aplicarEstiloCampo(campo);
        }
    }

    /**
     * Personaliza listas como la selección de categorías.
     */
    private static void aplicarEstiloLista(JList<?> lista) {
        lista.setBackground(BLANCO);
        lista.setForeground(TEXTO_OSCURO);
        lista.setSelectionBackground(new Color(181, 232, 218));
        lista.setSelectionForeground(TEXTO_OSCURO);
        lista.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));
    }

    /**
     * Personaliza los paneles y sus títulos.
     */
    private static void aplicarEstiloPanel(JPanel panel) {
        panel.setBackground(VERDE_CLARO);
        Border bordeActual = panel.getBorder();
        if (bordeActual instanceof TitledBorder bordeTitulo) { //Si el panel tiene un TitledBorder, conserva el título pero cambia sus colores.
            TitledBorder nuevoBorde = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDE), bordeTitulo.getTitle());
            nuevoBorde.setTitleColor(VERDE_OSCURO);
            nuevoBorde.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
            panel.setBorder(nuevoBorde);
        }
    }

    /**
     * Configura el fondo y borde de los JScrollPane.
     */
    private static void aplicarEstiloScroll(JScrollPane scroll) {
        scroll.setBackground(BLANCO);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        JViewport viewport = scroll.getViewport();
        if (viewport != null) {
            viewport.setBackground(BLANCO);
        }
    }

    /**
     * Configura las pestañas y cambia sus colores cuando
     * el usuario selecciona una diferente.
     */
    private static void aplicarEstiloPestanas(JTabbedPane pestanas) {
        pestanas.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pestanas.setBackground(BLANCO);
        pestanas.setForeground(VERDE_OSCURO);
        actualizarPestanas(pestanas);
        pestanas.addChangeListener(evento -> actualizarPestanas(pestanas)); //Cuando se cambia de pestaña, actualiza cuál debe aparecer con fondo verde.
    }

    /**
     * Coloca un JLabel personalizado dentro de cada pestaña.
     * Así los colores funcionan correctamente en Windows.
     */
    private static void actualizarPestanas(JTabbedPane pestanas) {
        int seleccionada = pestanas.getSelectedIndex();
        for (int indice = 0; indice < pestanas.getTabCount(); indice++) {
            JLabel etiqueta;
            Component componenteActual = pestanas.getTabComponentAt(indice);
            if (componenteActual instanceof JLabel) { //Reutiliza la etiqueta si ya fue creada.
                etiqueta = (JLabel) componenteActual;
            } else {
                etiqueta = new JLabel(pestanas.getTitleAt(indice));
                etiqueta.setOpaque(true);
                etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 13));
                etiqueta.setHorizontalAlignment(SwingConstants.CENTER);
                etiqueta.setBorder(BorderFactory.createEmptyBorder(9, 13, 9, 13));
                pestanas.setTabComponentAt(indice,etiqueta);
            }
            if (indice == seleccionada) { //Pestaña seleccionada.
                etiqueta.setBackground(VERDE_PRINCIPAL);
                etiqueta.setForeground(BLANCO);
            } else { //Pestañas no seleccionadas.
                etiqueta.setBackground(BLANCO);
                etiqueta.setForeground(VERDE_OSCURO);
            }
        }
        pestanas.repaint();
    }

    /**
     * Renderer utilizado para alternar el color de las filas
     * de las tablas normales.
     */
    private static class RenderizadorFilas extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado, boolean tieneFoco, int fila, int columna) {
            Component componente = super.getTableCellRendererComponent(tabla, valor, seleccionado, tieneFoco, fila, columna);
            setHorizontalAlignment(SwingConstants.LEFT);
            if (seleccionado) {
                componente.setBackground(new Color(181, 232, 218));
                componente.setForeground(TEXTO_OSCURO);
            } else {
                if (fila % 2 == 0) {
                    componente.setBackground(BLANCO);
                } else {
                    componente.setBackground(VERDE_FILA);
                }
                componente.setForeground(TEXTO_OSCURO);
            }
            setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
            return componente;
        }
    }

    /**
     * Dibuja un borde redondeado alrededor de los botones.
     */
    private static class BordeRedondeado implements Border {

        private final Color color;
        private final int radio;

        public BordeRedondeado(Color color, int radio) {
            this.color = color;
            this.radio = radio;
        }

        @Override
        public Insets getBorderInsets(Component componente) {
            return new Insets(7, 12, 7, 12);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component componente, Graphics graficos, int x, int y, int ancho, int alto) {
            graficos.setColor(color);
            graficos.drawRoundRect(x, y, ancho - 1, alto - 1, radio, radio);
        }
    }
}