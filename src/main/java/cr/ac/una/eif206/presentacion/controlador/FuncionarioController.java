package cr.ac.una.eif206.presentacion.controlador;

import cr.ac.una.eif206.modelo.Funcionario;
import cr.ac.una.eif206.negocio.AutenticacionService;
import cr.ac.una.eif206.persistencia.FuncionarioDAO;
import cr.ac.una.eif206.presentacion.vista.FuncionarioView;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.util.List;

public class FuncionarioController {

    private final FuncionarioView vista;
    private final AutenticacionService autenticacionService = new AutenticacionService();
    private final FuncionarioDAO funcionarioDAO = new FuncionarioDAO();

    private String idSeleccionado;

    public FuncionarioController(FuncionarioView vista) {
        this.vista = vista;
        cargarTabla(funcionarioDAO.listarTodos());
        registrarEventos();
    }

    private void registrarEventos() {
        vista.getBtnRegistrar().addActionListener(e -> registrar());
        vista.getBtnModificar().addActionListener(e -> modificar());
        vista.getBtnLimpiar().addActionListener(e -> limpiarFormulario());
        vista.getBtnEliminar().addActionListener(e -> eliminarSeleccionado());
        vista.getBtnBuscar().addActionListener(e -> buscar());
        vista.getBtnMostrarTodos().addActionListener(e -> {
            vista.getTxtBuscar().setText("");
            cargarTabla(funcionarioDAO.listarTodos());
        });

        vista.getTablaFuncionarios().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });
    }

    private void cargarTabla(List<Funcionario> funcionarios) {
        vista.getModeloTabla().setRowCount(0);
        for (Funcionario f : funcionarios) {
            vista.getModeloTabla().addRow(new Object[]{
                    f.getId(), f.getNombre(), f.getTelefono(), f.getEmail()
            });
        }
    }

    private void buscar() {
        String texto = vista.getTxtBuscar().getText().trim();
        if (texto.isEmpty()) {
            cargarTabla(funcionarioDAO.listarTodos());
        } else {
            cargarTabla(funcionarioDAO.buscarPorTexto(texto));
        }
    }

    private void cargarSeleccionEnFormulario() {
        int filaSeleccionada = vista.getTablaFuncionarios().getSelectedRow();
        if (filaSeleccionada == -1) {
            return;
        }

        idSeleccionado = (String) vista.getModeloTabla().getValueAt(filaSeleccionada, 0);
        String nombre = (String) vista.getModeloTabla().getValueAt(filaSeleccionada, 1);
        String telefono = (String) vista.getModeloTabla().getValueAt(filaSeleccionada, 2);
        String correo = (String) vista.getModeloTabla().getValueAt(filaSeleccionada, 3);

        vista.getTxtId().setText(idSeleccionado);
        vista.getTxtNombre().setText(nombre);
        vista.getTxtTelefono().setText(telefono);
        vista.getTxtCorreo().setText(correo);

        vista.getTxtId().setEditable(false);
        vista.getBtnRegistrar().setEnabled(false);
        vista.getBtnModificar().setEnabled(true);
    }

    private void registrar() {
        String id = vista.getTxtId().getText().trim();
        String nombre = vista.getTxtNombre().getText().trim();
        String telefono = vista.getTxtTelefono().getText().trim();
        String correo = vista.getTxtCorreo().getText().trim();

        vista.getBtnRegistrar().setEnabled(false);
        vista.getBtnRegistrar().setText("Enviando...");

        SwingWorker<Void, Void> tarea = new SwingWorker<>() {
            private Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    autenticacionService.registrarNuevoFuncionario(id, nombre, telefono, correo);
                } catch (Exception e) {
                    error = e;
                }
                return null;
            }

            @Override
            protected void done() {
                vista.getBtnRegistrar().setEnabled(true);
                vista.getBtnRegistrar().setText("Registrar y enviar clave");

                if (error != null) {
                    JOptionPane.showMessageDialog(vista, error.getMessage(),
                            "No se pudo registrar", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(vista,
                            "Funcionario registrado correctamente.\nSe envió la clave al correo indicado.",
                            "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormulario();
                    cargarTabla(funcionarioDAO.listarTodos());
                }
            }
        };

        tarea.execute();
    }

    private void modificar() {
        if (idSeleccionado == null) {
            JOptionPane.showMessageDialog(vista, "Seleccione primero un funcionario de la tabla para modificarlo.",
                    "Ninguno seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = vista.getTxtNombre().getText().trim();
        String telefono = vista.getTxtTelefono().getText().trim();
        String correo = vista.getTxtCorreo().getText().trim();

        try {
            autenticacionService.modificarDatosFuncionario(idSeleccionado, nombre, telefono, correo);
            JOptionPane.showMessageDialog(vista, "Datos actualizados correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            cargarTabla(funcionarioDAO.listarTodos());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, ex.getMessage(),
                    "No se pudo modificar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarSeleccionado() {
        int filaSeleccionada = vista.getTablaFuncionarios().getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(vista, "Seleccione primero un funcionario de la tabla.",
                    "Ninguno seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = (String) vista.getModeloTabla().getValueAt(filaSeleccionada, 0);

        int confirmacion = JOptionPane.showConfirmDialog(vista,
                "¿Seguro que desea eliminar al funcionario " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        funcionarioDAO.eliminar(id);
        limpiarFormulario();
        cargarTabla(funcionarioDAO.listarTodos());
    }

    private void limpiarFormulario() {
        vista.getTxtId().setText("");
        vista.getTxtNombre().setText("");
        vista.getTxtTelefono().setText("");
        vista.getTxtCorreo().setText("");
        vista.getTxtId().setEditable(true);
        vista.getBtnRegistrar().setEnabled(true);
        vista.getBtnModificar().setEnabled(false);
        vista.getTablaFuncionarios().clearSelection();
        idSeleccionado = null;
    }
}