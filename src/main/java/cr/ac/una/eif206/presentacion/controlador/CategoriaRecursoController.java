package cr.ac.una.eif206.presentacion.controlador;

import cr.ac.una.eif206.modelo.CategoriaRecurso;
import cr.ac.una.eif206.persistencia.CategoriaRecursoDAO;
import cr.ac.una.eif206.presentacion.vista.CategoriaRecursoView;
import cr.ac.una.eif206.reportes.ReportePdfGenerator;

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
        vista.getBtnImprimir().addActionListener(e -> imprimir());

        vista.getTabla().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });
    }

    private void guardar() {
        String id = vista.getTxtId().getText().trim();
        String descripcion = vista.getTxtDescripcion().getText().trim();

        // Ahora solo validamos que la descripción no esté vacía
        if (descripcion.isEmpty()) {
            JOptionPane.showMessageDialog(vista,
                    "Debe completar la descripción.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Si el campo ID está vacío, es un registro nuevo y lo generamos
        if (id.isEmpty()) {
            id = generarNuevoId();
        }

        try {
            dao.guardar(new CategoriaRecurso(id, descripcion));
            JOptionPane.showMessageDialog(vista,
                    "Categoría guardada correctamente.\nID asignado: " + id,
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            cargarTabla(dao.listarTodas());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista,
                    "Ocurrió un error al guardar la categoría: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String generarNuevoId() {
        int maximo = 0;
        for (CategoriaRecurso c : dao.listarTodas()) {
            String idActual = c.getId();
            if (idActual != null && idActual.toUpperCase().startsWith("CAT-")) {
                try {
                    // Extraemos el número después de "CAT-"
                    int numero = Integer.parseInt(idActual.substring(4));
                    if (numero > maximo) {
                        maximo = numero;
                    }
                } catch (NumberFormatException ignored) {
                    // Si hay un ID con formato extraño, lo ignoramos en el conteo
                }
            }
        }
        // Retornamos el siguiente número con formato de 6 dígitos
        return String.format("CAT-%06d", maximo + 1);
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

    private void imprimir() {
        if (vista.getTabla().getRowCount() == 0) {
            JOptionPane.showMessageDialog(vista, "No hay categorías para imprimir.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String ruta = ReportePdfGenerator.generarReporteTabla("Listado de Categorias", vista.getTabla());
            JOptionPane.showMessageDialog(vista, "Reporte generado en:\n" + ruta,
                    "Reporte PDF generado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, "No se pudo generar el reporte: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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