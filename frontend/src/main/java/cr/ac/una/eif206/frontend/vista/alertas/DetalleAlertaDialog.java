package cr.ac.una.eif206.frontend.vista.alertas;

import cr.ac.una.eif206.common.modelo.Reserva;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

/**
 * Ventana emergente "Ver alerta", como en el enunciado:
 * titulo = actividad; Encargado, Fecha, Desde, Hasta, lista de Recursos y boton OK.
 */
public class DetalleAlertaDialog extends JDialog {

    private final JButton btnOk = new JButton("OK");

    public DetalleAlertaDialog(Window padre, Reserva reserva) {
        super(padre, reserva.getActividad(), ModalityType.APPLICATION_MODAL);
        construirInterfaz(reserva);
    }

    private void construirInterfaz(Reserva reserva) {
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel datos = new JPanel(new GridBagLayout());
        datos.setBorder(BorderFactory.createEmptyBorder(15, 18, 5, 18));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 18);
        c.anchor = GridBagConstraints.WEST;

        agregarFila(datos, c, 0, "Encargado:", reserva.getNombreFuncionario());
        agregarFila(datos, c, 1, "Fecha:", String.valueOf(reserva.getFecha()));        // ej: 2026-07-31
        agregarFila(datos, c, 2, "Desde:", String.valueOf(reserva.getHoraInicio()));   // ej: 08:00
        agregarFila(datos, c, 3, "Hasta:", String.valueOf(reserva.getHoraFin()));

        // Recursos: etiqueta a la izquierda (centrada en vertical) y lista a la derecha
        c.gridx = 0;
        c.gridy = 4;
        datos.add(etiqueta("Recursos"), c);

        JList<String> listaRecursos = new JList<>(reserva.getDescripcionesRecursos().toArray(new String[0]));
        listaRecursos.setForeground(Color.GRAY);
        listaRecursos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listaRecursos.setFocusable(false);
        JScrollPane scroll = new JScrollPane(listaRecursos);
        scroll.setPreferredSize(new Dimension(260, 220));

        c.gridx = 1;
        c.insets = new Insets(8, 4, 4, 4);
        datos.add(scroll, c);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 12));
        btnOk.setPreferredSize(new Dimension(80, 34));
        btnOk.addActionListener(e -> dispose());
        botones.add(btnOk);

        setLayout(new BorderLayout());
        add(datos, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnOk);   // Enter = OK

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void agregarFila(JPanel panel, GridBagConstraints c, int fila, String nombre, String valor) {
        c.gridx = 0;
        c.gridy = fila;
        panel.add(etiqueta(nombre), c);
        c.gridx = 1;
        panel.add(etiqueta(valor), c);
    }

    private JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        return etiqueta;
    }
}