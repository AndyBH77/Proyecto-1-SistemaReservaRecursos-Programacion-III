package cr.ac.una.eif206.presentacion.controlador;

import cr.ac.una.eif206.modelo.Recurso;
import cr.ac.una.eif206.persistencia.CategoriaRecursoDAO;
import cr.ac.una.eif206.persistencia.RecursoDAO;
import cr.ac.una.eif206.presentacion.vista.RecursoView;
import cr.ac.una.eif206.reportes.ReportePdfGenerator;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class RecursoController {

    private final RecursoView vista;
    private final RecursoDAO dao;
    private final CategoriaRecursoDAO categoriaDAO;

    public RecursoController(RecursoView vista) {
        this.vista = vista;
        this.dao = new RecursoDAO();
        this.categoriaDAO = new CategoriaRecursoDAO();
        registrarEventos();
        cargarTabla(dao.listarTodos());
    }

    private void registrarEventos() {
        vista.getBtnGuardar().addActionListener(e -> guardar());
        vista.getBtnEliminar().addActionListener(e -> eliminar());
        vista.getBtnLimpiar().addActionListener(e -> limpiarFormulario());
        vista.getBtnBuscar().addActionListener(e -> buscar());
        vista.getBtnImprimir().addActionListener(e -> imprimir());

        vista.getTabla().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });
    }

    private void guardar() {
        String id = vista.getTxtId().getText().trim();
        String idCategoria = vista.getTxtIdCategoria().getText().trim();
        String descripcion = vista.getTxtDescripcion().getText().trim();

        if (id.isEmpty() || idCategoria.isEmpty() || descripcion.isEmpty()) {
            JOptionPane.showMessageDialog(vista,
                    "Debe completar el ID, el ID de categoría y la descripción.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (categoriaDAO.buscarPorId(idCategoria).isEmpty()) {
            JOptionPane.showMessageDialog(vista,
                    "No existe una categoría con el ID: " + idCategoria,
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            dao.guardar(new Recurso(id, idCategoria, descripcion));
            JOptionPane.showMessageDialog(vista,
                    "Recurso guardado correctamente.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            cargarTabla(dao.listarTodos());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista,
                    "Ocurrió un error al guardar el recurso: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        String id = vista.getTxtId().getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(vista,
                    "Seleccione o ingrese el ID del recurso a eliminar.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(vista,
                "¿Está seguro de eliminar el recurso " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            dao.eliminar(id);
            JOptionPane.showMessageDialog(vista,
                    "Recurso eliminado correctamente.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            cargarTabla(dao.listarTodos());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista,
                    "Ocurrió un error al eliminar el recurso: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscar() {
        String texto = vista.getTxtBusqueda().getText();
        cargarTabla(dao.buscarPorTexto(texto));
    }

    private void imprimir() {
        if (vista.getTabla().getRowCount() == 0) {
            JOptionPane.showMessageDialog(vista, "No hay recursos para imprimir.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String ruta = ReportePdfGenerator.generarReporteTabla("Listado de Recursos", vista.getTabla());
            JOptionPane.showMessageDialog(vista, "Reporte generado en:\n" + ruta,
                    "Reporte PDF generado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, "No se pudo generar el reporte: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        vista.getTxtId().setText("");
        vista.getTxtIdCategoria().setText("");
        vista.getTxtDescripcion().setText("");
        vista.getTabla().clearSelection();
    }

    private void cargarSeleccionEnFormulario() {
        int fila = vista.getTabla().getSelectedRow();
        if (fila == -1) {
            return;
        }
        DefaultTableModel modelo = vista.getModeloTabla();
        vista.getTxtId().setText(String.valueOf(modelo.getValueAt(fila, 0)));
        vista.getTxtIdCategoria().setText(String.valueOf(modelo.getValueAt(fila, 1)));
        vista.getTxtDescripcion().setText(String.valueOf(modelo.getValueAt(fila, 2)));
    }

    private void cargarTabla(List<Recurso> recursos) {
        DefaultTableModel modelo = vista.getModeloTabla();
        modelo.setRowCount(0);
        for (Recurso r : recursos) {
            modelo.addRow(new Object[]{r.getId(), r.getIdCategoria(), r.getDescripcion()});
        }
    }
}