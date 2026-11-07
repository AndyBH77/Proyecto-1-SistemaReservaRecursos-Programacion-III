package cr.ac.una.eif206.frontend.controlador;

import cr.ac.una.eif206.common.modelo.EstadisticaItem;
import cr.ac.una.eif206.frontend.proxy.ServiceProxy;
import cr.ac.una.eif206.frontend.reportes.ReportePdfGenerator;
import cr.ac.una.eif206.frontend.util.Constantes;
import cr.ac.una.eif206.frontend.util.Dialogos;
import cr.ac.una.eif206.frontend.vista.EstadisticasView;

import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de la pestana Estadisticas. Las dos secciones (Recursos y
 * Actividades) son independientes: cada una tiene su periodo, su tabla y su grafico.
 * Los GROUP BY los hace MySQL en el backend; aqui solo se muestran los resultados.
 */
public class EstadisticasController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern(Constantes.FORMATO_FECHA);

    private final EstadisticasView vista;
    private final ServiceProxy proxy = ServiceProxy.getInstancia();

    public EstadisticasController(EstadisticasView vista) {
        this.vista = vista;
        vista.getConsultarRecursos().addActionListener(e -> consultarRecursos());
        vista.getImprimirRecursos().addActionListener(e -> imprimir("Estadisticas de Recursos Reservados",
                vista.getDesdeRecursos(), vista.getHastaRecursos(), vista.getTablaRecursos()));
        vista.getConsultarActividades().addActionListener(e -> consultarActividades());
        vista.getImprimirActividades().addActionListener(e -> imprimir("Estadisticas de Actividades por Semana",
                vista.getDesdeActividades(), vista.getHastaActividades(), vista.getTablaActividades()));
    }

    private void consultarRecursos() {
        LocalDate[] periodo = leerPeriodo(vista.getDesdeRecursos(), vista.getHastaRecursos());
        if (periodo == null) {
            return;
        }
        ejecutar(vista.getConsultarRecursos(), () -> proxy.estadisticasRecursos(periodo[0], periodo[1]),
                items -> vista.mostrarRecursos(aMapa(items, false)));
    }

    private void consultarActividades() {
        LocalDate[] periodo = leerPeriodo(vista.getDesdeActividades(), vista.getHastaActividades());
        if (periodo == null) {
            return;
        }
        // la etiqueta llega como lunes ISO (2026-10-26); aqui se muestra como dd/MM/yyyy
        ejecutar(vista.getConsultarActividades(), () -> proxy.estadisticasActividades(periodo[0], periodo[1]),
                items -> vista.mostrarActividades(aMapa(items, true)));
    }

    /** Pide los datos en otro hilo (para no congelar la ventana) y los entrega en el hilo de Swing. */
    private void ejecutar(JButton boton, Consulta consulta, java.util.function.Consumer<List<EstadisticaItem>> alTerminar) {
        boton.setEnabled(false);
        new SwingWorker<List<EstadisticaItem>, Void>() {
            @Override
            protected List<EstadisticaItem> doInBackground() throws Exception {
                return consulta.pedir();
            }

            @Override
            protected void done() {
                boton.setEnabled(true);
                try {
                    alTerminar.accept(get());
                } catch (Exception ex) {
                    Dialogos.error(vista, "No se pudieron cargar las estadísticas", ex);
                }
            }
        }.execute();
    }

    @FunctionalInterface
    private interface Consulta {
        List<EstadisticaItem> pedir() throws Exception;
    }

    /** Lee desde/hasta; si estan al reves avisa y devuelve null (el backend tambien lo valida). */
    private LocalDate[] leerPeriodo(JSpinner desdeSpinner, JSpinner hastaSpinner) {
        LocalDate desde = CalendarizacionController.aLocalDate((Date) desdeSpinner.getValue());
        LocalDate hasta = CalendarizacionController.aLocalDate((Date) hastaSpinner.getValue());
        if (desde.isAfter(hasta)) {
            Dialogos.advertencia(vista, "Rango de fechas inválido",
                    "La fecha desde no puede ser posterior a la fecha hasta.");
            return null;
        }
        return new LocalDate[]{desde, hasta};
    }

    private Map<String, Integer> aMapa(List<EstadisticaItem> items, boolean etiquetaEsFecha) {
        Map<String, Integer> datos = new LinkedHashMap<>();
        for (EstadisticaItem item : items) {
            String etiqueta = etiquetaEsFecha
                    ? LocalDate.parse(item.getEtiqueta()).format(FORMATO_FECHA) : item.getEtiqueta();
            datos.put(etiqueta, item.getCantidad());
        }
        return datos;
    }

    private void imprimir(String titulo, JSpinner desdeSpinner, JSpinner hastaSpinner, JTable tabla) {
        if (tabla.getRowCount() == 0) {
            Dialogos.advertencia(vista, "Nada que imprimir", "Consulte primero las estadísticas antes de imprimir.");
            return;
        }
        try {
            String desde = CalendarizacionController.aLocalDate((Date) desdeSpinner.getValue()).format(FORMATO_FECHA);
            String hasta = CalendarizacionController.aLocalDate((Date) hastaSpinner.getValue()).format(FORMATO_FECHA);
            String ruta = ReportePdfGenerator.generarReporteEstadisticas(titulo, desde, hasta, tabla);
            Dialogos.info(vista, "Reporte PDF generado", "Reporte generado en:\n" + ruta);
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo generar el reporte", ex);
        }
    }
}
