package cr.ac.una.eif206.presentacion.controlador;

import cr.ac.una.eif206.modelo.CategoriaRecurso;
import cr.ac.una.eif206.negocio.ConsultaService;
import cr.ac.una.eif206.presentacion.vista.CalendarizacionView;
import cr.ac.una.eif206.presentacion.vista.EstadisticasView;
import cr.ac.una.eif206.presentacion.vista.ActividadesView;
import cr.ac.una.eif206.reportes.ReportePdfGenerator;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Controla las tres pantallas asignadas
 * Une sus eventos en una sola clase para mantener el proyecto sencillo.
 */
public class Controller {
    private final CalendarizacionView calendarizacionView;
    private final ActividadesView actividadesView;
    private final EstadisticasView estadisticaView;
    private final ConsultaService servicio = new ConsultaService();

    /** Guarda las vistas, carga los datos iniciales y registra todos los botones. */
    public Controller(CalendarizacionView calendarizacionView,
                             ActividadesView actividadesView,
                              EstadisticasView estadisticaView) {
        this.calendarizacionView = calendarizacionView;
        this.actividadesView = actividadesView;
        this.estadisticaView = estadisticaView;
        prepararCalendarizacion();
        prepararActividades();
        prepararEstadisticas();
    }

    /** Llena el combo de categorías y conecta los botones de Calendarización. */
    private void prepararCalendarizacion() {
        servicio.listarCategorias().forEach(calendarizacionView.getComboCategoria()::addItem);
        calendarizacionView.getBtnCargar().addActionListener(e -> cargarCalendarizacion());
        calendarizacionView.getBtnImprimir().addActionListener(e -> imprimirCalendarizacion());
        cargarCalendarizacion();
    }

    /** Construye la matriz de recursos para la fecha y categoría seleccionadas. */
    private void cargarCalendarizacion() {
        CategoriaRecurso categoria = (CategoriaRecurso) calendarizacionView.getComboCategoria().getSelectedItem();
        if (categoria == null) {
            calendarizacionView.getLblEstado().setText("No hay categorías registradas.");
            return;
        }
        LocalDate fecha = convertirFecha(calendarizacionView.getSpinnerFecha().getValue());
        String[] columnas = servicio.encabezadosCalendarizacion(categoria.getId());
        calendarizacionView.actualizarTabla(columnas,
                servicio.crearCalendarizacion(fecha, categoria.getId()));
        calendarizacionView.getLblEstado().setText(
                "Mostrando " + categoria.getDescripcion() + " para " + fecha);
    }

    /** Actualiza la matriz y la exporta como reporte PDF. */
    private void imprimirCalendarizacion() {
        try {
            cargarCalendarizacion();
            String ruta = ReportePdfGenerator.generarReporteTabla(
                    "Calendarización de recursos", calendarizacionView.getTabla());
            mostrarReporteGenerado(calendarizacionView, ruta);
        } catch (Exception ex) {
            mostrarError(calendarizacionView, ex);
        }
    }

    /** Conecta los botones de consulta, navegación semanal e impresión de Actividades. */
    private void prepararActividades() {
        actividadesView.getBtnCargar().addActionListener(e -> cargarActividades());
        actividadesView.getBtnAnterior().addActionListener(e -> moverSemana(-1));
        actividadesView.getBtnSiguiente().addActionListener(e -> moverSemana(1));
        actividadesView.getBtnImprimir().addActionListener(e -> imprimirActividades());
        cargarActividades();
    }

    /** Construye la matriz de horas contra días para la semana seleccionada. */
    private void cargarActividades() {
        LocalDate fecha = convertirFecha(actividadesView.getSpinnerFecha().getValue());
        LocalDate lunes = servicio.inicioSemana(fecha);
        actividadesView.actualizarTabla(servicio.encabezadosSemana(fecha),
                servicio.crearProgramacionSemanal(fecha));
        actividadesView.getLblSemana().setText(
                "Semana del " + lunes + " al " + lunes.plusDays(6));
    }

    /** Cambia la fecha de referencia una semana hacia atrás o hacia adelante. */
    private void moverSemana(int cantidad) {
        LocalDate nuevaFecha = convertirFecha(actividadesView.getSpinnerFecha().getValue())
                .plusWeeks(cantidad);
        actividadesView.getSpinnerFecha().setValue(
                Date.from(nuevaFecha.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        cargarActividades();
    }

    /** Actualiza la programación semanal y la exporta a PDF. */
    private void imprimirActividades() {
        try {
            cargarActividades();
            String ruta = ReportePdfGenerator.generarReporteTabla(
                    "Programación semanal de actividades", actividadesView.getTabla());
            mostrarReporteGenerado(actividadesView, ruta);
        } catch (Exception ex) {
            mostrarError(actividadesView, ex);
        }
    }

    /** Conecta los botones de consulta y PDF de las dos estadísticas. */
    private void prepararEstadisticas() {
        estadisticaView.getCargarRecursos().addActionListener(e -> cargarEstadisticasRecursos());
        estadisticaView.getCargarActividades().addActionListener(e -> cargarEstadisticasActividades());
        estadisticaView.getImprimirRecursos().addActionListener(e -> imprimirEstadisticaRecursos());
        estadisticaView.getImprimirActividades().addActionListener(e -> imprimirEstadisticaActividades());
        cargarEstadisticasRecursos();
        cargarEstadisticasActividades();
    }

    /** Calcula cuántas reservas de cada categoría existen en el período. */
    private void cargarEstadisticasRecursos() {
        try {
            estadisticaView.actualizarRecursos(servicio.estadisticasRecursos(
                    convertirFecha(estadisticaView.getDesdeRecursos().getValue()),
                    convertirFecha(estadisticaView.getHastaRecursos().getValue())));
        } catch (Exception ex) {
            mostrarError(estadisticaView, ex);
        }
    }

    /** Calcula cuántas actividades existen en cada semana del período. */
    private void cargarEstadisticasActividades() {
        try {
            estadisticaView.actualizarActividades(servicio.estadisticasActividades(
                    convertirFecha(estadisticaView.getDesdeActividades().getValue()),
                    convertirFecha(estadisticaView.getHastaActividades().getValue())));
        } catch (Exception ex) {
            mostrarError(estadisticaView, ex);
        }
    }

    /** Genera el PDF de recursos con período, tabla y gráfico. */
    private void imprimirEstadisticaRecursos() {
        imprimirEstadistica("Estadísticas de recursos", estadisticaView.getTablaRecursos(),
                estadisticaView.getGraficoRecursos(),
                convertirFecha(estadisticaView.getDesdeRecursos().getValue()),
                convertirFecha(estadisticaView.getHastaRecursos().getValue()));
    }

    /** Genera el PDF de actividades con período, tabla y gráfico. */
    private void imprimirEstadisticaActividades() {
        imprimirEstadistica("Estadísticas de actividades", estadisticaView.getTablaActividades(),
                estadisticaView.getGraficoActividades(),
                convertirFecha(estadisticaView.getDesdeActividades().getValue()),
                convertirFecha(estadisticaView.getHastaActividades().getValue()));
    }

    /** Método común que evita repetir la generación de los dos PDF estadísticos. */
    private void imprimirEstadistica(String titulo, JTable tabla, JPanel grafico,
                                     LocalDate desde, LocalDate hasta) {
        try {
            String ruta = ReportePdfGenerator.generarReporteEstadisticas(titulo, desde.toString(), hasta.toString(), tabla);
            mostrarReporteGenerado(estadisticaView, ruta);
        } catch (Exception ex) {
            mostrarError(estadisticaView, ex);
        }
    }

    /** Convierte la fecha antigua de JSpinner al tipo moderno LocalDate. */
    private LocalDate convertirFecha(Object valor) {
        return ((Date) valor).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** Informa al usuario dónde quedó guardado el PDF. */
    private void mostrarReporteGenerado(JPanel vista, String ruta) {
        JOptionPane.showMessageDialog(vista, "Reporte generado en:\n" + ruta);
    }

    /** Muestra cualquier error de consulta o generación de reportes. */
    private void mostrarError(JPanel vista, Exception ex) {
        JOptionPane.showMessageDialog(vista, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
