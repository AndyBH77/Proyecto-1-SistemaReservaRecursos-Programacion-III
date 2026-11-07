package cr.ac.una.eif206.frontend.vista;

import cr.ac.una.eif206.common.modelo.TablaConsulta;
import cr.ac.una.eif206.frontend.util.Constantes;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Date;

/**
 * Pestana de Actividades (administrador y funcionario).
 * Vista "tonta": para cualquier semana muestra la matriz horas x dias.
 * La logica esta en ActividadesController.
 */
public class ActividadesView extends JPanel {

    private final JSpinner spinnerFecha = new JSpinner(new SpinnerDateModel());
    private final JButton btnAnterior = new JButton("< Semana anterior");
    private final JButton btnConsultar = new JButton("Consultar semana");
    private final JButton btnSiguiente = new JButton("Semana siguiente >");
    private final JButton btnImprimir = new JButton("Imprimir PDF");
    private final JLabel lblSemana = new JLabel("Seleccione una fecha de la semana que desea consultar.");
    private final JTable tabla = new JTable();

    public ActividadesView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        spinnerFecha.setEditor(new JSpinner.DateEditor(spinnerFecha, Constantes.FORMATO_FECHA));
        spinnerFecha.setValue(new Date());
        CalendarizacionView.configurarTabla(tabla, false);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filtros.setBorder(BorderFactory.createTitledBorder("Semana"));
        filtros.add(new JLabel("Fecha de la semana:"));
        filtros.add(spinnerFecha);
        filtros.add(btnAnterior);
        filtros.add(btnConsultar);
        filtros.add(btnSiguiente);
        filtros.add(btnImprimir);

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(filtros, BorderLayout.NORTH);
        superior.add(lblSemana, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Actividades de la semana"));

        add(superior, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void mostrarTabla(TablaConsulta datos) {
        CalendarizacionView.llenarMatriz(tabla, datos, 150, false);
    }

    public JSpinner getSpinnerFecha() { return spinnerFecha; }
    public JButton getBtnAnterior() { return btnAnterior; }
    public JButton getBtnConsultar() { return btnConsultar; }
    public JButton getBtnSiguiente() { return btnSiguiente; }
    public JButton getBtnImprimir() { return btnImprimir; }
    public JLabel getLblSemana() { return lblSemana; }
    public JTable getTabla() { return tabla; }
}
