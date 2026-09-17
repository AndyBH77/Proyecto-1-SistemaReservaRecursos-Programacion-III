package cr.ac.una.eif206.presentacion.controlador;

import cr.ac.una.eif206.modelo.CategoriaRecurso;
import cr.ac.una.eif206.negocio.ConsultaService;
import cr.ac.una.eif206.presentacion.vista.CalendarizacionView;
import cr.ac.una.eif206.reportes.ReportePdfGenerator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Controlador de la pestaña "Calendarización" (funcionalidad 6 del
 * enunciado). Al seleccionar una fecha y una categoría de recurso, muestra
 * la matriz hora x recurso con el estado de cada celda.
 */
public class CalendarizacionController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CalendarizacionView vista;
    private final ConsultaService consultaService;

    public CalendarizacionController(CalendarizacionView vista) {
        this.vista = vista;
        this.consultaService = new ConsultaService();
        cargarCategorias();
        registrarEventos();
    }

    private void cargarCategorias() {
        List<CategoriaRecurso> categorias = consultaService.listarCategorias();
        vista.getComboCategoria().setModel(new DefaultComboBoxModel<>(categorias.toArray(new CategoriaRecurso[0])));
    }

    private void registrarEventos() {
        vista.getBtnCargar().addActionListener(e -> cargarCalendarizacion());
        vista.getBtnImprimir().addActionListener(e -> imprimir());
    }

    private void cargarCalendarizacion() {
        CategoriaRecurso categoria = (CategoriaRecurso) vista.getComboCategoria().getSelectedItem();
        if (categoria == null) {
            JOptionPane.showMessageDialog(vista,
                    "No existen categorías de recursos registradas. Regístrelas primero en la pestaña Categorías.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate fecha = obtenerFecha();
        String[] encabezados = consultaService.encabezadosCalendarizacion(categoria.getId());

        if (encabezados.length == 1) {
            vista.actualizarTabla(encabezados, new Object[0][encabezados.length]);
            vista.getLblEstado().setText("La categoría \"" + categoria.getDescripcion()
                    + "\" no tiene recursos registrados.");
            return;
        }

        Object[][] datos = consultaService.crearCalendarizacion(fecha, categoria.getId());
        vista.actualizarTabla(encabezados, datos);
        vista.getLblEstado().setText("Calendarización de \"" + categoria.getDescripcion() + "\" para el "
                + fecha.format(FORMATO_FECHA) + ".");
    }

    private void imprimir() {
        if (vista.getTabla().getRowCount() == 0 || vista.getTabla().getColumnCount() == 0) {
            JOptionPane.showMessageDialog(vista, "Cargue primero una calendarización antes de imprimir.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String ruta = ReportePdfGenerator.generarReporteTabla("Calendarizacion de Recursos", vista.getTabla());
            JOptionPane.showMessageDialog(vista, "Reporte generado en:\n" + ruta,
                    "Reporte PDF generado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, "No se pudo generar el reporte: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate obtenerFecha() {
        Date fechaDate = (Date) vista.getSpinnerFecha().getValue();
        return fechaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
