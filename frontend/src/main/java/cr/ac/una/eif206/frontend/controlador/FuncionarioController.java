package cr.ac.una.eif206.frontend.controlador;

import cr.ac.una.eif206.common.modelo.Funcionario;
import cr.ac.una.eif206.frontend.proxy.ServiceProxy;
import cr.ac.una.eif206.frontend.reportes.ReportePdfGenerator;
import cr.ac.una.eif206.frontend.util.Dialogos;
import cr.ac.una.eif206.frontend.vista.FuncionarioView;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de la pestana Funcionarios (solo ADMINISTRADOR).
 *
 * Registrar: el backend genera una clave, la envia al correo y luego guarda
 * el funcionario. Como enviar el correo tarda unos segundos, se hace en un
 * SwingWorker para que la ventana no se congele.
 */
public class FuncionarioController {

    private final FuncionarioView vista;
    private final ServiceProxy proxy = ServiceProxy.getInstancia();

    private List<Funcionario> funcionarios = new ArrayList<>();   // los de la tabla, en el mismo orden
    private Funcionario seleccionado;

    public FuncionarioController(FuncionarioView vista) {
        this.vista = vista;
        registrarEventos();
        limpiarFormulario();
    }

    private void registrarEventos() {
        vista.getBtnRegistrar().addActionListener(e -> registrar());
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
            funcionarios = proxy.buscarFuncionarios(vista.getTxtBuscar().getText().trim());
        } catch (Exception ex) {
            funcionarios = new ArrayList<>();
            Dialogos.error(vista, "No se pudieron cargar los funcionarios", ex);
        }

        vista.getModeloTabla().setRowCount(0);
        for (Funcionario f : funcionarios) {
            vista.getModeloTabla().addRow(new Object[]{f.getId(), f.getNombre(), f.getTelefono(), f.getEmail()});
        }
        limpiarFormulario();
    }

    // ==================================================================
    // Registrar / modificar / eliminar
    // ==================================================================

    private void registrar() {
        if (!formularioCompleto()) {
            return;
        }
        String id = vista.getTxtId().getText().trim();
        String nombre = vista.getTxtNombre().getText().trim();
        String telefono = vista.getTxtTelefono().getText().trim();
        String correo = vista.getTxtCorreo().getText().trim();

        vista.getBtnRegistrar().setEnabled(false);
        vista.getBtnRegistrar().setText("Enviando correo...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return proxy.registrarFuncionario(id, nombre, telefono, correo);
            }

            @Override
            protected void done() {
                vista.getBtnRegistrar().setEnabled(true);
                vista.getBtnRegistrar().setText("Registrar y enviar clave");
                try {
                    String enviadoA = get();
                    Dialogos.info(vista, "Funcionario registrado",
                            "El funcionario se registró correctamente.\nSe envió su clave a: " + enviadoA);
                    vista.getTxtBuscar().setText("");
                    buscar();
                } catch (Exception ex) {
                    Dialogos.error(vista, "No se pudo registrar el funcionario", ex);
                }
            }
        }.execute();
    }

    private void modificar() {
        if (seleccionado == null) {
            Dialogos.advertencia(vista, "Ningún funcionario seleccionado",
                    "Seleccione primero un funcionario de la tabla para modificarlo.");
            return;
        }
        if (!formularioCompleto()) {
            return;
        }
        try {
            proxy.modificarFuncionario(seleccionado.getId(), vista.getTxtNombre().getText().trim(),
                    vista.getTxtTelefono().getText().trim(), vista.getTxtCorreo().getText().trim());
            Dialogos.info(vista, "Funcionario modificado", "Los datos se modificaron correctamente.");
            buscar();
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo modificar el funcionario", ex);
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            Dialogos.advertencia(vista, "Ningún funcionario seleccionado",
                    "Seleccione primero un funcionario de la tabla para eliminarlo.");
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(vista,
                "¿Seguro que desea eliminar a " + seleccionado.getNombre() + " (" + seleccionado.getId() + ")?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            proxy.eliminarFuncionario(seleccionado.getId());
            Dialogos.info(vista, "Funcionario eliminado", "El funcionario se eliminó correctamente.");
            buscar();
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo eliminar el funcionario", ex);
        }
    }

    // ==================================================================
    // Formulario
    // ==================================================================

    /** Revision rapida en pantalla; las validaciones completas las hace el backend. */
    private boolean formularioCompleto() {
        if (vista.getTxtId().getText().isBlank() || vista.getTxtNombre().getText().isBlank()
                || vista.getTxtTelefono().getText().isBlank() || vista.getTxtCorreo().getText().isBlank()) {
            Dialogos.advertencia(vista, "Datos incompletos", "Debe llenar id, nombre, teléfono y correo.");
            return false;
        }
        return true;
    }

    private void cargarSeleccionEnFormulario() {
        int fila = vista.getTabla().getSelectedRow();
        if (fila < 0 || fila >= funcionarios.size()) {
            seleccionado = null;
            vista.getTxtId().setEditable(true);
            return;
        }
        seleccionado = funcionarios.get(fila);
        vista.getTxtId().setText(seleccionado.getId());
        vista.getTxtId().setEditable(false);   // el id no se puede cambiar
        vista.getTxtNombre().setText(seleccionado.getNombre());
        vista.getTxtTelefono().setText(seleccionado.getTelefono());
        vista.getTxtCorreo().setText(seleccionado.getEmail());
    }

    private void limpiarFormulario() {
        vista.getTxtId().setText("");
        vista.getTxtId().setEditable(true);
        vista.getTxtNombre().setText("");
        vista.getTxtTelefono().setText("");
        vista.getTxtCorreo().setText("");
        vista.getTabla().clearSelection();
        seleccionado = null;
    }

    private void imprimir() {
        try {
            String ruta = ReportePdfGenerator.generarReporteTabla("Listado de Funcionarios", vista.getTabla());
            Dialogos.info(vista, "Reporte PDF generado", "Reporte generado en:\n" + ruta);
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo generar el reporte", ex);
        }
    }
}