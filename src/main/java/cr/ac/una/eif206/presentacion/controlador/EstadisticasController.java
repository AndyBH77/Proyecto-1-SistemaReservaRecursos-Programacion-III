package cr.ac.una.eif206.presentacion.controlador;

import cr.ac.una.eif206.negocio.ConsultaService;
import cr.ac.una.eif206.presentacion.vista.EstadisticasView;
import cr.ac.una.eif206.reportes.ReportePdfGenerator;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * Controlador de la pestaña "Estadísticas" (funcionalidad 8 del enunciado).
 * Maneja de forma independiente la sección de Recursos y la de Actividades:
 * cada una tiene su propio rango de fechas, tabla y gráfico de barras.
 */
public class EstadisticasController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EstadisticasView vista;
    private final ConsultaService consultaService;

    public EstadisticasController(EstadisticasView vista) {
        this.vista = vista;
        this.consultaService = new ConsultaService();
        registrarEventos();
        vista.getImprimirRecursos().setEnabled(false);
        vista.getImprimirActividades().setEnabled(false);
    }

    private void registrarEventos() {
        vista.getCargarRecursos().addActionListener(e -> cargarRecursos());
        vista.getImprimirRecursos().addActionListener(e -> imprimir("Estadisticas de Recursos Reservados",
                vista.getDesdeRecursos(), vista.getHastaRecursos(), vista.getTablaRecursos()));

        vista.getCargarActividades().addActionListener(e -> cargarActividades());
        vista.getImprimirActividades().addActionListener(e -> imprimir("Estadisticas de Actividades Realizadas",
                vista.getDesdeActividades(), vista.getHastaActividades(), vista.getTablaActividades()));
    }

    private void cargarRecursos() {
        try {
            LocalDate desde = obtenerFecha(vista.getDesdeRecursos());
            LocalDate hasta = obtenerFecha(vista.getHastaRecursos());
            Map<String, Integer> datos = consultaService.estadisticasRecursos(desde, hasta);
            vista.actualizarRecursos(datos);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(vista, ex.getMessage(), "Rango de fechas inválido",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cargarActividades() {
        try {
            LocalDate desde = obtenerFecha(vista.getDesdeActividades());
            LocalDate hasta = obtenerFecha(vista.getHastaActividades());
            Map<String, Integer> datos = consultaService.estadisticasActividades(desde, hasta);
            vista.actualizarActividades(datos);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(vista, ex.getMessage(), "Rango de fechas inválido",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void imprimir(String titulo, JSpinner desdeSpinner, JSpinner hastaSpinner, JTable tabla) {
        if (tabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(vista, "Cargue primero las estadísticas antes de imprimir.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String desdeTexto = obtenerFecha(desdeSpinner).format(FORMATO_FECHA);
            String hastaTexto = obtenerFecha(hastaSpinner).format(FORMATO_FECHA);
            String ruta = ReportePdfGenerator.generarReporteEstadisticas(titulo, desdeTexto, hastaTexto, tabla);
            JOptionPane.showMessageDialog(vista, "Reporte generado en:\n" + ruta,
                    "Reporte PDF generado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, "No se pudo generar el reporte: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate obtenerFecha(JSpinner spinner) {
        Date fechaDate = (Date) spinner.getValue();
        return fechaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
