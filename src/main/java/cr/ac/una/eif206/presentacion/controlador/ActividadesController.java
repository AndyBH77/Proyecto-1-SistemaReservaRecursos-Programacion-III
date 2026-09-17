package cr.ac.una.eif206.presentacion.controlador;

import cr.ac.una.eif206.negocio.ConsultaService;
import cr.ac.una.eif206.presentacion.vista.ActividadesView;
import cr.ac.una.eif206.reportes.ReportePdfGenerator;

import javax.swing.JOptionPane;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Controlador de la pestaña "Actividades" (funcionalidad 7 del enunciado).
 * Para cualquier semana (a partir de una fecha de referencia) muestra la
 * matriz hora x día con las actividades programadas.
 */
public class ActividadesController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ActividadesView vista;
    private final ConsultaService consultaService;

    public ActividadesController(ActividadesView vista) {
        this.vista = vista;
        this.consultaService = new ConsultaService();
        registrarEventos();
        cargarSemana();
    }

    private void registrarEventos() {
        vista.getBtnCargar().addActionListener(e -> cargarSemana());
        vista.getBtnAnterior().addActionListener(e -> moverSemana(-7));
        vista.getBtnSiguiente().addActionListener(e -> moverSemana(7));
        vista.getBtnImprimir().addActionListener(e -> imprimir());
    }

    private void moverSemana(int dias) {
        LocalDate nuevaFecha = obtenerFecha().plusDays(dias);
        vista.getSpinnerFecha().setValue(Date.from(nuevaFecha.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        cargarSemana();
    }

    private void cargarSemana() {
        LocalDate fechaReferencia = obtenerFecha();
        String[] encabezados = consultaService.encabezadosSemana(fechaReferencia);
        Object[][] datos = consultaService.crearProgramacionSemanal(fechaReferencia);
        vista.actualizarTabla(encabezados, datos);

        LocalDate lunes = consultaService.inicioSemana(fechaReferencia);
        LocalDate domingo = lunes.plusDays(6);
        vista.getLblSemana().setText("Semana del " + lunes.format(FORMATO_FECHA)
                + " al " + domingo.format(FORMATO_FECHA) + ".");
    }

    private void imprimir() {
        if (vista.getTabla().getRowCount() == 0 || vista.getTabla().getColumnCount() == 0) {
            JOptionPane.showMessageDialog(vista, "Cargue primero una semana antes de imprimir.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String ruta = ReportePdfGenerator.generarReporteTabla("Programacion de Actividades", vista.getTabla());
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
