package cr.ac.una.eif206.frontend.controlador;

import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import cr.ac.una.eif206.common.modelo.Recurso;
import cr.ac.una.eif206.frontend.proxy.ServiceProxy;
import cr.ac.una.eif206.frontend.reportes.ReportePdfGenerator;
import cr.ac.una.eif206.frontend.util.Dialogos;
import cr.ac.una.eif206.frontend.vista.RecursoView;

import javax.swing.JOptionPane;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de la pestana Recursos (solo ADMINISTRADOR).
 * CRUD de recursos con filtro por categoria. Todo lo pide al backend por el
 * ServiceProxy; las validaciones fuertes (repetidos, borrar algo en uso) las
 * hace el backend y su mensaje se muestra tal cual.
 */
public class RecursoController {

    /** Opcion del filtro que significa "sin filtrar" (id 0 = no es una categoria real). */
    private static final CategoriaRecurso TODAS = new CategoriaRecurso(0, "Todas las categorías");

    private final RecursoView vista;
    private final ServiceProxy proxy = ServiceProxy.getInstancia();

    private List<CategoriaRecurso> categorias = new ArrayList<>();
    private List<Recurso> recursos = new ArrayList<>();   // los de la tabla, en el mismo orden
    private boolean actualizandoCombos;                   // evita recargar mientras se llenan los combos

    public RecursoController(RecursoView vista) {
        this.vista = vista;
        registrarEventos();
        actualizarBotones(false);
    }

    private void registrarEventos() {
        vista.getBtnAgregar().addActionListener(e -> agregar());
        vista.getBtnModificar().addActionListener(e -> modificar());
        vista.getBtnEliminar().addActionListener(e -> eliminar());
        vista.getBtnLimpiar().addActionListener(e -> limpiarFormulario());
        vista.getBtnImprimir().addActionListener(e -> imprimir());

        vista.getComboFiltro().addActionListener(e -> {
            if (!actualizandoCombos) {
                cargarRecursos();
            }
        });

        vista.getTabla().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });

        // Cada vez que se muestra la pestana se recarga: asi aparecen las
        // categorias que el administrador creo en la pestana Categorias.
        vista.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && vista.isShowing()) {
                recargarTodo();
            }
        });
    }

    // ==================================================================
    // Cargar datos
    // ==================================================================

    /** Recarga categorias y tabla, conservando el filtro y la categoria elegidos. */
    private void recargarTodo() {
        CategoriaRecurso filtroActual = (CategoriaRecurso) vista.getComboFiltro().getSelectedItem();
        CategoriaRecurso formActual = (CategoriaRecurso) vista.getComboCategoria().getSelectedItem();
        try {
            categorias = proxy.listarCategorias();
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudieron cargar las categorías", ex);
            return;
        }

        actualizandoCombos = true;
        try {
            vista.setCategorias(categorias, TODAS);
            if (filtroActual != null) {
                vista.getComboFiltro().setSelectedItem(filtroActual);   // equals por id
            }
            if (formActual != null) {
                vista.getComboCategoria().setSelectedItem(formActual);
            }
        } finally {
            actualizandoCombos = false;
        }
        cargarRecursos();
    }

    private void cargarRecursos() {
        CategoriaRecurso filtro = (CategoriaRecurso) vista.getComboFiltro().getSelectedItem();
        try {
            recursos = (filtro == null || filtro.getId() == 0)
                    ? proxy.listarRecursos()
                    : proxy.listarRecursosPorCategoria(filtro.getId());
        } catch (Exception ex) {
            recursos = new ArrayList<>();
            Dialogos.error(vista, "No se pudieron cargar los recursos", ex);
        }

        vista.getModeloTabla().setRowCount(0);
        for (Recurso r : recursos) {
            vista.getModeloTabla().addRow(new Object[]{r.getId(), r.getDescripcionCategoria(), r.getDescripcion()});
        }
        limpiarFormulario();
    }

    // ==================================================================
    // Agregar / modificar / eliminar
    // ==================================================================

    private void agregar() {
        if (!formularioCompleto()) {
            return;
        }
        try {
            proxy.agregarRecurso(vista.getTxtId().getText().trim(), categoriaDelFormulario().getId(),
                    vista.getTxtDescripcion().getText().trim());
            Dialogos.info(vista, "Recurso agregado", "El recurso se agregó correctamente.");
            cargarRecursos();
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo agregar el recurso", ex);
        }
    }

    private void modificar() {
        Recurso seleccionado = recursoSeleccionado();
        if (seleccionado == null) {
            Dialogos.advertencia(vista, "Ningún recurso seleccionado",
                    "Seleccione primero un recurso de la tabla para modificarlo.");
            return;
        }
        if (!formularioCompleto()) {
            return;
        }
        try {
            proxy.modificarRecurso(seleccionado.getId(), categoriaDelFormulario().getId(),
                    vista.getTxtDescripcion().getText().trim());
            Dialogos.info(vista, "Recurso modificado", "El recurso se modificó correctamente.");
            cargarRecursos();
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo modificar el recurso", ex);
        }
    }

    private void eliminar() {
        Recurso seleccionado = recursoSeleccionado();
        if (seleccionado == null) {
            Dialogos.advertencia(vista, "Ningún recurso seleccionado",
                    "Seleccione primero un recurso de la tabla para eliminarlo.");
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(vista,
                "¿Está seguro de eliminar el recurso \"" + seleccionado.getDescripcion() + "\" ("
                        + seleccionado.getId() + ")?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            proxy.eliminarRecurso(seleccionado.getId());
            Dialogos.info(vista, "Recurso eliminado", "El recurso se eliminó correctamente.");
            cargarRecursos();
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo eliminar el recurso", ex);
        }
    }

    // ==================================================================
    // Formulario y tabla
    // ==================================================================

    /** Revisa lo basico antes de molestar al servidor; el resto lo valida el backend. */
    private boolean formularioCompleto() {
        if (categorias.isEmpty() || categoriaDelFormulario() == null) {
            Dialogos.advertencia(vista, "Sin categorías",
                    "No hay categorías disponibles. Regístrelas primero en la pestaña Categorías.");
            return false;
        }
        if (vista.getTxtId().getText().isBlank()) {
            Dialogos.advertencia(vista, "Dato faltante", "Debe indicar el número de activo del recurso.");
            return false;
        }
        if (vista.getTxtDescripcion().getText().isBlank()) {
            Dialogos.advertencia(vista, "Dato faltante", "Debe indicar la descripción del recurso.");
            return false;
        }
        return true;
    }

    private CategoriaRecurso categoriaDelFormulario() {
        return (CategoriaRecurso) vista.getComboCategoria().getSelectedItem();
    }

    private Recurso recursoSeleccionado() {
        int fila = vista.getTabla().getSelectedRow();
        return (fila >= 0 && fila < recursos.size()) ? recursos.get(fila) : null;
    }

    private void limpiarFormulario() {
        vista.getTabla().clearSelection();
        vista.getTxtId().setText("");
        vista.getTxtId().setEditable(true);
        vista.getTxtDescripcion().setText("");
        if (vista.getComboCategoria().getItemCount() > 0) {
            vista.getComboCategoria().setSelectedIndex(0);
        }
        actualizarBotones(false);
    }

    /** Al elegir una fila, sus datos pasan al formulario y el numero de activo no se puede cambiar. */
    private void cargarSeleccionEnFormulario() {
        Recurso r = recursoSeleccionado();
        if (r == null) {
            return;
        }
        vista.getTxtId().setText(r.getId());
        vista.getTxtId().setEditable(false);
        vista.getTxtDescripcion().setText(r.getDescripcion());
        for (CategoriaRecurso c : categorias) {
            if (c.getId() == r.getIdCategoria()) {
                vista.getComboCategoria().setSelectedItem(c);
            }
        }
        actualizarBotones(true);
    }

    /** Con una fila elegida se puede modificar/eliminar; sin fila, solo agregar. */
    private void actualizarBotones(boolean hayFilaElegida) {
        vista.getBtnAgregar().setEnabled(!hayFilaElegida);
        vista.getBtnModificar().setEnabled(hayFilaElegida);
        vista.getBtnEliminar().setEnabled(hayFilaElegida);
    }

    private void imprimir() {
        if (vista.getTabla().getRowCount() == 0) {
            Dialogos.advertencia(vista, "Nada que imprimir", "No hay recursos para imprimir.");
            return;
        }
        try {
            String ruta = ReportePdfGenerator.generarReporteTabla("Listado de Recursos", vista.getTabla());
            Dialogos.info(vista, "Reporte PDF generado", "Reporte generado en:\n" + ruta);
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo generar el reporte", ex);
        }
    }
}
