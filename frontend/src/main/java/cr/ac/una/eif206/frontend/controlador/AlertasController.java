package cr.ac.una.eif206.frontend.controlador;

import cr.ac.una.eif206.common.protocolo.Notificacion;
import cr.ac.una.eif206.frontend.proxy.AlertaListener;
import cr.ac.una.eif206.frontend.vista.alertas.AlertasPanel;
import cr.ac.una.eif206.frontend.vista.alertas.DetalleAlertaDialog;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador del area de ALERTAS.
 *
 * - alRecibir(...): lo llama EscuchaAlertas (en el hilo de Swing) cada vez que
 *   el backend avisa de una reserva nueva hecha por OTRO usuario. Se agrega una fila.
 * - Clic en "Ver": abre DetalleAlertaDialog con los datos de la reserva.
 * - Clic en "Desc.": pregunta "Descartar?" y, si acepta, quita la fila.
 *   Esto es SOLO local: NO se envia nada al backend (asi lo pide el enunciado).
 */
public class AlertasController implements AlertaListener {

    private final AlertasPanel vista;

    /** Las notificaciones, en el MISMO orden que las filas de la tabla. */
    private final List<Notificacion> alertas = new ArrayList<>();

    public AlertasController(AlertasPanel vista) {
        this.vista = vista;
        registrarEventos();
    }

    @Override
    public void alRecibir(Notificacion notificacion) {
        alertas.add(notificacion);
        vista.agregarFila(notificacion.getTitulo());
    }

    private void registrarEventos() {
        JTable tabla = vista.getTabla();

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                int columna = tabla.columnAtPoint(e.getPoint());
                if (fila < 0 || fila >= alertas.size()) {
                    return;
                }
                if (columna == AlertasPanel.COLUMNA_VER) {
                    ver(fila);
                } else if (columna == AlertasPanel.COLUMNA_DESCARTAR) {
                    descartar(fila);
                }
            }
        });

        // Manito al pasar sobre los iconos, para que se note que se pueden presionar
        tabla.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                int columna = tabla.columnAtPoint(e.getPoint());
                boolean sobreIcono = fila >= 0
                        && (columna == AlertasPanel.COLUMNA_VER || columna == AlertasPanel.COLUMNA_DESCARTAR);
                tabla.setCursor(sobreIcono ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        });
    }

    private void ver(int fila) {
        Notificacion alerta = alertas.get(fila);
        new DetalleAlertaDialog(SwingUtilities.getWindowAncestor(vista), alerta.getReserva()).setVisible(true);
    }

    private void descartar(int fila) {
        Notificacion alerta = alertas.get(fila);
        int opcion = JOptionPane.showConfirmDialog(vista,
                alerta.getReserva().getActividad(),
                "Descartar?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (opcion == JOptionPane.YES_OPTION) {
            alertas.remove(fila);
            vista.quitarFila(fila);
        }
    }

    /** Cantidad de alertas en la lista (util para pruebas). */
    public int cantidadAlertas() {
        return alertas.size();
    }
}