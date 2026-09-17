package cr.ac.una.eif206.presentacion.controlador;

import cr.ac.una.eif206.modelo.CategoriaRecurso;
import cr.ac.una.eif206.persistencia.CategoriaRecursoDAO;
import cr.ac.una.eif206.presentacion.vista.CategoriaRecursoView;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class CategoriaRecursoController {

    private final CategoriaRecursoView vista;
    private final CategoriaRecursoDAO dao;

    public CategoriaRecursoController(CategoriaRecursoView vista) {
        this.vista = vista;
        this.dao = new CategoriaRecursoDAO();
        registrarEventos();
        cargarTabla(dao.listarTodas());
    }

    private void registrarEventos() {
        vista.getBtnGuardar().addActionListener(e -> guardar());
        vista.getBtnEliminar().addActionListener(e -> eliminar());
        vista.getBtnLimpiar().addActionListener(e -> limpiarFormulario());
        vista.getBtnBuscar().addActionListener(e -> buscar());

        vista.getTabla().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });
    }

    private void guardar() {
        String id = vista.getTxtId().getText().trim();
        String descripcion = vista.getTxtDescripcion().getText().trim();

        if (id.isEmpty() || descripcion.isEmpty()) {
            JOptionPane.showMessageDialog(vista,
                    "Debe completar el ID y la descripción.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            dao.guardar(new CategoriaRecurso(id, descripcion));
            JOptionPane.showMessageDialog(vista,
                    "Categoría guardada correctamente.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            cargarTabla(dao.listarTodas());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista,
                    "Ocurrió un error al guardar la categoría: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        String id = vista.getTxtId().getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(vista,
                    "Seleccione o ingrese el ID de la categoría a eliminar.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(vista,
                "¿Está seguro de eliminar la categoría " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            dao.eliminar(id);
            JOptionPane.showMessageDialog(vista,
                    "Categoría eliminada correctamente.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            cargarTabla(dao.listarTodas());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista,
                    "Ocurrió un error al eliminar la categoría: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscar() {
        String texto = vista.getTxtBusqueda().getText();
        cargarTabla(dao.buscarPorTexto(texto));
    }

    private void limpiarFormulario() {
        vista.getTxtId().setText("");
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
        vista.getTxtDescripcion().setText(String.valueOf(modelo.getValueAt(fila, 1)));
    }

    private void cargarTabla(List<CategoriaRecurso> categorias) {
        DefaultTableModel modelo = vista.getModeloTabla();
        modelo.setRowCount(0);
        for (CategoriaRecurso c : categorias) {
            modelo.addRow(new Object[]{c.getId(), c.getDescripcion()});
        }
    }
}