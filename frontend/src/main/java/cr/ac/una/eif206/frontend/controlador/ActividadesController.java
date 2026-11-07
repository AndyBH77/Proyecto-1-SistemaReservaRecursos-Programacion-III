package cr.ac.una.eif206.frontend.controlador;

import cr.ac.una.eif206.common.modelo.TablaConsulta;
import cr.ac.una.eif206.frontend.proxy.ServiceProxy;
import cr.ac.una.eif206.frontend.reportes.ReportePdfGenerator;
import cr.ac.una.eif206.frontend.util.Constantes;
import cr.ac.una.eif206.frontend.util.Dialogos;
import cr.ac.una.eif206.frontend.vista.ActividadesView;

import javax.swing.SwingWorker;
import java.awt.event.HierarchyEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * Controlador de la pestana Actividades: para la semana de la fecha elegida
 * pide al backend la matriz horas x dias y la muestra. Se recarga cada vez
 * que se muestra la pestana, para ver las reservas que hicieron los demas.
 */
public class ActividadesController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern(Constantes.FORMATO_FECHA);

    private final ActividadesView vista;
    private final ServiceProxy proxy = ServiceProxy.getInstancia();

    public ActividadesController(ActividadesView vista) {
        this.vista = vista;
        vista.getBtnConsultar().addActionListener(e -> consultar());
        vista.getBtnAnterior().addActionListener(e -> moverSemana(-7));
        vista.getBtnSiguiente().addActionListener(e -> moverSemana(7));
        vista.getBtnImprimir().addActionListener(e -> imprimir());

        vista.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && vista.isShowing()) {
                consultar();
            }
        });
    }

    private LocalDate fechaElegida() {
        return CalendarizacionController.aLocalDate((Date) vista.getSpinnerFecha().getValue());
    }

    private void moverSemana(int dias) {
        LocalDate nueva = fechaElegida().plusDays(dias);
        vista.getSpinnerFecha().setValue(Date.from(nueva.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        consultar();
    }

    private void consultar() {
        LocalDate fecha = fechaElegida();

        vista.getBtnConsultar().setEnabled(false);
        new SwingWorker<TablaConsulta, Void>() {
            @Override
            protected TablaConsulta doInBackground() throws Exception {
                return proxy.programacionSemanal(fecha);
            }

            @Override
            protected void done() {
                vista.getBtnConsultar().setEnabled(true);
                try {
                    vista.mostrarTabla(get());
                    LocalDate lunes = fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    vista.getLblSemana().setText("Semana del " + lunes.format(FORMATO_FECHA)
                            + " al " + lunes.plusDays(6).format(FORMATO_FECHA) + ".");
                } catch (Exception ex) {
                    Dialogos.error(vista, "No se pudieron cargar las actividades", ex);
                }
            }
        }.execute();
    }

    private void imprimir() {
        if (vista.getTabla().getRowCount() == 0) {
            Dialogos.advertencia(vista, "Nada que imprimir", "Consulte primero una semana antes de imprimir.");
            return;
        }
        try {
            String ruta = ReportePdfGenerator.generarReporteTabla("Programacion de Actividades", vista.getTabla());
            Dialogos.info(vista, "Reporte PDF generado", "Reporte generado en:\n" + ruta);
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo generar el reporte", ex);
        }
    }
}
