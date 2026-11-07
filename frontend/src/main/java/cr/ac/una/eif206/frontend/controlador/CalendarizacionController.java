package cr.ac.una.eif206.frontend.controlador;

import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import cr.ac.una.eif206.common.modelo.TablaConsulta;
import cr.ac.una.eif206.frontend.proxy.ServiceProxy;
import cr.ac.una.eif206.frontend.reportes.ReportePdfGenerator;
import cr.ac.una.eif206.frontend.util.Constantes;
import cr.ac.una.eif206.frontend.util.Dialogos;
import cr.ac.una.eif206.frontend.vista.CalendarizacionView;

import javax.swing.SwingWorker;
import java.awt.event.HierarchyEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Controlador de la pestana Calendarizacion: con una fecha y una categoria
 * pide al backend la matriz horas x recursos y la muestra.
 */
public class CalendarizacionController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern(Constantes.FORMATO_FECHA);

    private final CalendarizacionView vista;
    private final ServiceProxy proxy = ServiceProxy.getInstancia();
    private boolean hayConsulta;   // ya se consulto algo (para refrescarlo al volver a la pestana)

    public CalendarizacionController(CalendarizacionView vista) {
        this.vista = vista;
        vista.getBtnConsultar().addActionListener(e -> consultar());
        vista.getBtnImprimir().addActionListener(e -> imprimir());

        // Al mostrar la pestana: categorias al dia (el admin pudo crear nuevas)
        // y, si ya habia una consulta, se repite para ver reservas nuevas.
        vista.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && vista.isShowing()) {
                cargarCategorias();
                if (hayConsulta) {
                    consultar();
                }
            }
        });
    }

    private void cargarCategorias() {
        CategoriaRecurso elegida = (CategoriaRecurso) vista.getComboCategoria().getSelectedItem();
        try {
            List<CategoriaRecurso> categorias = proxy.listarCategorias();
            vista.setCategorias(categorias);
            if (elegida != null) {
                vista.getComboCategoria().setSelectedItem(elegida);
            }
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudieron cargar las categorías", ex);
        }
    }

    private void consultar() {
        CategoriaRecurso categoria = (CategoriaRecurso) vista.getComboCategoria().getSelectedItem();
        if (categoria == null) {
            Dialogos.advertencia(vista, "Sin categorías",
                    "No existen categorías de recursos. Regístrelas primero en la pestaña Categorías.");
            return;
        }
        LocalDate fecha = aLocalDate((Date) vista.getSpinnerFecha().getValue());

        vista.getBtnConsultar().setEnabled(false);
        new SwingWorker<TablaConsulta, Void>() {
            @Override
            protected TablaConsulta doInBackground() throws Exception {
                return proxy.calendarizacion(fecha, categoria.getId());
            }

            @Override
            protected void done() {
                vista.getBtnConsultar().setEnabled(true);
                try {
                    TablaConsulta datos = get();
                    vista.mostrarTabla(datos);
                    hayConsulta = true;
                    vista.getLblEstado().setText(datos.getCantidadFilas() == 0
                            ? "La categoría \"" + categoria.getDescripcion() + "\" no tiene recursos registrados."
                            : "Calendarización de \"" + categoria.getDescripcion() + "\" para el "
                            + fecha.format(FORMATO_FECHA) + ".");
                } catch (Exception ex) {
                    Dialogos.error(vista, "No se pudo consultar la calendarización", ex);
                }
            }
        }.execute();
    }

    private void imprimir() {
        if (vista.getTabla().getRowCount() == 0) {
            Dialogos.advertencia(vista, "Nada que imprimir", "Consulte primero una calendarización antes de imprimir.");
            return;
        }
        try {
            String ruta = ReportePdfGenerator.generarReporteTabla("Calendarizacion de Recursos", vista.getTabla());
            Dialogos.info(vista, "Reporte PDF generado", "Reporte generado en:\n" + ruta);
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo generar el reporte", ex);
        }
    }

    static LocalDate aLocalDate(Date fecha) {
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
