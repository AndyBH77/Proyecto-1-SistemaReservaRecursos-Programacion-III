package cr.ac.una.eif206.frontend.controlador;

import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import cr.ac.una.eif206.frontend.proxy.ServiceProxy;
import cr.ac.una.eif206.frontend.reportes.ReportePdfGenerator;
import cr.ac.una.eif206.frontend.util.Dialogos;
import cr.ac.una.eif206.frontend.vista.CategoriaRecursoView;

import javax.swing.JOptionPane;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de la pestana Categorias (solo ADMINISTRADOR).
 * Todo lo pide al backend por el ServiceProxy. Las validaciones fuertes
 * (repetidas, borrar una categoria con recursos) las hace el backend y su
 * mensaje se muestra tal cual.
 */
public class CategoriaRecursoController {

    private final CategoriaRecursoView vista;
    private final ServiceProxy proxy = ServiceProxy.getInstancia();

    private List<CategoriaRecurso> categorias = new ArrayList<>();   // las de la tabla, en el mismo orden
    private CategoriaRecurso seleccionada;

    public CategoriaRecursoController(CategoriaRecursoView vista) {
        this.vista = vista;
        registrarEventos();
        limpiarFormulario();
    }

    private void registrarEventos() {
        vista.getBtnAgregar().addActionListener(e -> agregar());
        vista.getBtnModificar().addActionListener(e -> modificar());
        vista.getBtnEliminar().addActionListener(e -> eliminar());
        vista.getBtnLimpiar().addActionListener(e -> limpiarFormulario());
        vista.getBtnBuscar().addActionListener(e -> buscar());
        vista.getTxtBuscar().addActionListener(e -> buscar());          // Enter en el campo
        vista.getBtnImprimir().addActionListener(e -> imprimir());

        vista.getTabla().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });

        // Cada vez que se muestra la pestana se recarga la lista
        vista.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && vista.isShowing()) {
                buscar();
            }
        });
    }

    // ==================================================================
    // Cargar / buscar
    // ==================================================================

    private void buscar() {
        try {
            categorias = proxy.buscarCategorias(vista.getTxtBuscar().getText().trim());
        } catch (Exception ex) {
            categorias = new ArrayList<>();
            Dialogos.error(vista, "No se pudieron cargar las categorías", ex);
        }

        vista.getModeloTabla().setRowCount(0);
        for (CategoriaRecurso c : categorias) {
            vista.getModeloTabla().addRow(new Object[]{c.getId(), c.getDescripcion()});
        }
        limpiarFormulario();
    }

    // ==================================================================
    // Agregar / modificar / eliminar
    // ==================================================================

    private void agregar() {
        String descripcion = vista.getTxtDescripcion().getText().trim();
        if (descripcion.isEmpty()) {
            Dialogos.advertencia(vista, "Dato faltante", "Escriba la descripción de la categoría.");
            return;
        }
        try {
            proxy.agregarCategoria(descripcion);
            Dialogos.info(vista, "Categoría agregada", "La categoría se agregó correctamente.");
            vista.getTxtBuscar().setText("");
            buscar();
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo agregar la categoría", ex);
        }
    }

    private void modificar() {
        if (seleccionada == null) {
            Dialogos.advertencia(vista, "Ninguna categoría seleccionada",
                    "Seleccione primero una categoría de la tabla para modificarla.");
            return;
        }
        String descripcion = vista.getTxtDescripcion().getText().trim();
        if (descripcion.isEmpty()) {
            Dialogos.advertencia(vista, "Dato faltante", "Escriba la descripción de la categoría.");
            return;
        }
        try {
            proxy.modificarCategoria(seleccionada.getId(), descripcion);
            Dialogos.info(vista, "Categoría modificada", "La categoría se modificó correctamente.");
            buscar();
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo modificar la categoría", ex);
        }
    }

    private void eliminar() {
        if (seleccionada == null) {
            Dialogos.advertencia(vista, "Ninguna categoría seleccionada",
                    "Seleccione primero una categoría de la tabla para eliminarla.");
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(vista,
                "¿Seguro que desea eliminar la categoría \"" + seleccionada.getDescripcion() + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            proxy.eliminarCategoria(seleccionada.getId());
            Dialogos.info(vista, "Categoría eliminada", "La categoría se eliminó correctamente.");
            buscar();
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo eliminar la categoría", ex);
        }
    }

    // ==================================================================
    // Formulario
    // ==================================================================

    private void cargarSeleccionEnFormulario() {
        int fila = vista.getTabla().getSelectedRow();
        if (fila < 0 || fila >= categorias.size()) {
            seleccionada = null;
            return;
        }
        seleccionada = categorias.get(fila);
        vista.getTxtId().setText(String.valueOf(seleccionada.getId()));
        vista.getTxtDescripcion().setText(seleccionada.getDescripcion());
    }

    private void limpiarFormulario() {
        vista.getTxtId().setText("");
        vista.getTxtDescripcion().setText("");
        vista.getTabla().clearSelection();
        seleccionada = null;
    }

    private void imprimir() {
        try {
            String ruta = ReportePdfGenerator.generarReporteTabla("Listado de Categorias", vista.getTabla());
            Dialogos.info(vista, "Reporte PDF generado", "Reporte generado en:\n" + ruta);
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo generar el reporte", ex);
        }
    }
}