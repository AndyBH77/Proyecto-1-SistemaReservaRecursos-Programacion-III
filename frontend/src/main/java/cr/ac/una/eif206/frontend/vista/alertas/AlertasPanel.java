package cr.ac.una.eif206.frontend.vista.alertas;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

/**
 * Area de ALERTAS (lado derecho de la ventana principal), como en el enunciado:
 * una tabla con las columnas  Actividad | Ver | Desc.
 *  - Actividad: "Funcionario - Actividad"
 *  - Ver: icono de carpeta (abre el detalle)
 *  - Desc.: icono rojo (descarta la alerta, solo de esta lista)
 *
 * Vista "tonta": la logica (agregar, ver, descartar) esta en AlertasController.
 */
public class AlertasPanel extends JPanel {

    public static final int COLUMNA_ACTIVIDAD = 0;
    public static final int COLUMNA_VER = 1;
    public static final int COLUMNA_DESCARTAR = 2;

    private static final Color FONDO_AREA = new Color(214, 218, 225);   // gris del espacio vacio
    private static final Color FILA_PAR = Color.WHITE;
    private static final Color FILA_IMPAR = new Color(242, 242, 242);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Actividad", "Ver", "Desc."}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        /** Las columnas 1 y 2 tienen iconos: asi la JTable los dibuja como imagen. */
        @Override
        public Class<?> getColumnClass(int columna) {
            return columna == COLUMNA_ACTIVIDAD ? String.class : Icon.class;
        }
    };

    /** JTable con filas de colores alternos (blanco / gris claro). */
    private final JTable tabla = new JTable(modelo) {
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int fila, int columna) {
            Component celda = super.prepareRenderer(renderer, fila, columna);
            if (!isRowSelected(fila)) {
                celda.setBackground(fila % 2 == 0 ? FILA_PAR : FILA_IMPAR);
                celda.setForeground(Color.BLACK);
            }
            return celda;
        }
    };

    public AlertasPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        tabla.setRowHeight(30);
        tabla.setShowGrid(false);
        tabla.setFillsViewportHeight(false);   // debajo de la ultima fila se ve el fondo gris
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Texto de la actividad con un pequeno margen a la izquierda
        DefaultTableCellRenderer textoConMargen = new DefaultTableCellRenderer();
        textoConMargen.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 4));
        tabla.getColumnModel().getColumn(COLUMNA_ACTIVIDAD).setCellRenderer(textoConMargen);

        // Columnas de iconos angostas y centradas
        configurarColumnaIcono(tabla.getColumnModel().getColumn(COLUMNA_VER));
        configurarColumnaIcono(tabla.getColumnModel().getColumn(COLUMNA_DESCARTAR));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(FONDO_AREA);
        add(scroll, BorderLayout.CENTER);
    }

    private void configurarColumnaIcono(TableColumn columna) {
        columna.setPreferredWidth(45);
        columna.setMaxWidth(50);
        columna.setMinWidth(40);
        columna.setResizable(false);
    }

    /** Agrega una fila al final: "Funcionario - Actividad" + iconos. */
    public void agregarFila(String titulo) {
        modelo.addRow(new Object[]{titulo, IconosAlertas.VER, IconosAlertas.DESCARTAR});
    }

    public void quitarFila(int fila) {
        modelo.removeRow(fila);
    }

    public JTable getTabla() {
        return tabla;
    }
}