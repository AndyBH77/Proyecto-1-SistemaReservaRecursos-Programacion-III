package cr.ac.una.eif206.presentacion.controlador;

import cr.ac.una.eif206.modelo.CategoriaRecurso;
import cr.ac.una.eif206.modelo.Reserva;
import cr.ac.una.eif206.modelo.RolUsuario;
import cr.ac.una.eif206.modelo.Usuario;
import cr.ac.una.eif206.negocio.ReservaService;
import cr.ac.una.eif206.negocio.llm.DatosReservaExtraidos;
import cr.ac.una.eif206.negocio.llm.LlmExtractorService;
import cr.ac.una.eif206.presentacion.vista.ReservaView;
import cr.ac.una.eif206.reportes.ReportePdfGenerator;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReservaController {

    private final ReservaView vista;
    private final Usuario usuarioActual;
    private final boolean esAdministrador;
    private final ReservaService reservaService = new ReservaService();
    private final LlmExtractorService llmExtractorService = new LlmExtractorService();

    private List<CategoriaRecurso> categoriasDisponibles;
    private String idReservaSeleccionada;

    public ReservaController(ReservaView vista, Usuario usuarioActual) {
        this.vista = vista;
        this.usuarioActual = usuarioActual;
        this.esAdministrador = usuarioActual.getRol() == RolUsuario.ADMINISTRADOR;

        vista.setTituloListado(esAdministrador ? "Todas las reservas" : "Mis reservas");

        cargarCategorias();
        cargarTablaReservas();
        registrarEventos();
    }

    private void registrarEventos() {
        vista.getBtnExtraerIA().addActionListener(e -> extraerConIA());
        vista.getBtnReservar().addActionListener(e -> crearReserva());
        vista.getBtnModificar().addActionListener(e -> modificarReserva());
        vista.getBtnCancelarReserva().addActionListener(e -> cancelarReserva());
        vista.getBtnLimpiar().addActionListener(e -> limpiarFormulario());
        vista.getBtnImprimir().addActionListener(e -> imprimirReservas());

        vista.getTablaReservas().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });
    }

    private void cargarCategorias() {
        categoriasDisponibles = reservaService.obtenerCategoriasDisponibles();
        vista.getModeloListaCategorias().clear();
        for (CategoriaRecurso categoria : categoriasDisponibles) {
            vista.getModeloListaCategorias().addElement(categoria.getDescripcion());
        }
    }

    private List<Reserva> obtenerReservasParaMostrar() {
        return esAdministrador
                ? reservaService.obtenerTodasLasReservas()
                : reservaService.obtenerReservasDe(usuarioActual.getId());
    }

    private void cargarTablaReservas() {
        vista.getModeloTablaReservas().setRowCount(0);
        List<Reserva> reservas = obtenerReservasParaMostrar();

        for (Reserva r : reservas) {
            String horario = r.getHoraInicio() + " - " + r.getHoraFin();
            String recursos = String.join(", ", r.getIdsRecursosAsignados());
            vista.getModeloTablaReservas().addRow(new Object[]{
                    r.getId(), r.getIdFuncionario(), r.getActividad(), r.getFecha(), horario, recursos, r.getEstado()
            });
        }
    }

    private void extraerConIA() {
        String frase = vista.getTxtFrase().getText().trim();
        if (frase.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Escriba primero una frase describiendo la reserva.",
                    "Frase vacía", JOptionPane.WARNING_MESSAGE);
            return;
        }

        vista.getBtnExtraerIA().setEnabled(false);
        vista.getBtnExtraerIA().setText("Extrayendo...");

        SwingWorker<DatosReservaExtraidos, Void> tarea = new SwingWorker<>() {
            @Override
            protected DatosReservaExtraidos doInBackground() {
                return llmExtractorService.extraer(frase, categoriasDisponibles);
            }

            @Override
            protected void done() {
                vista.getBtnExtraerIA().setEnabled(true);
                vista.getBtnExtraerIA().setText("Extraer IA");
                try {
                    DatosReservaExtraidos datos = get();
                    aplicarDatosExtraidos(datos);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(vista, "No se pudo extraer la información: " + ex.getMessage(),
                            "Error de IA", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        tarea.execute();
    }

    private void aplicarDatosExtraidos(DatosReservaExtraidos datos) {
        if (datos.actividad != null) {
            vista.getTxtActividad().setText(datos.actividad);
        }
        if (datos.fecha != null) {
            vista.getSpinnerFecha().setValue(Date.from(datos.fecha.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        if (datos.horaInicio != null) {
            vista.getSpinnerHoraInicio().setValue(Date.from(datos.horaInicio.atDate(LocalDate.now())
                    .atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (datos.horaFin != null) {
            vista.getSpinnerHoraFin().setValue(Date.from(datos.horaFin.atDate(LocalDate.now())
                    .atZone(ZoneId.systemDefault()).toInstant()));
        }

        vista.getListaCategorias().clearSelection();
        for (int i = 0; i < categoriasDisponibles.size(); i++) {
            String descripcion = categoriasDisponibles.get(i).getDescripcion();
            boolean coincide = datos.nombresCategorias.stream()
                    .anyMatch(nombre -> nombre.equalsIgnoreCase(descripcion));
            if (coincide) {
                vista.getListaCategorias().addSelectionInterval(i, i);
            }
        }

        JOptionPane.showMessageDialog(vista,
                "Se completó el formulario automáticamente. Revise los datos antes de reservar.",
                "Extracción completada", JOptionPane.INFORMATION_MESSAGE);
    }

    private void crearReserva() {
        try {
            DatosFormulario datos = leerFormulario();

            ReservaService.ResultadoReserva resultado = reservaService.crearReserva(
                    usuarioActual.getId(), datos.actividad, datos.fecha, datos.horaInicio, datos.horaFin, datos.idsCategorias);

            mostrarResultado(resultado, "creó");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, ex.getMessage(), "No se pudo reservar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarReserva() {
        if (idReservaSeleccionada == null) {
            JOptionPane.showMessageDialog(vista, "Seleccione primero una reserva de la tabla para modificarla.",
                    "Ninguna reserva seleccionada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            DatosFormulario datos = leerFormulario();

            ReservaService.ResultadoReserva resultado = reservaService.modificarReserva(
                    idReservaSeleccionada, datos.actividad, datos.fecha, datos.horaInicio, datos.horaFin, datos.idsCategorias);

            mostrarResultado(resultado, "modificó");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, ex.getMessage(), "No se pudo modificar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarResultado(ReservaService.ResultadoReserva resultado, String verbo) {
        if (resultado.exito) {
            JOptionPane.showMessageDialog(vista, "La reserva se " + verbo + " correctamente (id: "
                    + resultado.reserva.getId() + ").", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            cargarTablaReservas();
        } else {
            String categorias = String.join(", ", resultado.categoriasNoDisponibles);
            JOptionPane.showMessageDialog(vista,
                    "No hay disponibilidad para las siguientes categorías:\n" + categorias
                            + "\n\nPuede modificar la reserva e intentar de nuevo.",
                    "Sin disponibilidad", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cancelarReserva() {
        if (idReservaSeleccionada == null) {
            JOptionPane.showMessageDialog(vista, "Seleccione primero una reserva de la tabla.",
                    "Ninguna reserva seleccionada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(vista,
                "¿Seguro que desea cancelar la reserva " + idReservaSeleccionada + "?",
                "Confirmar cancelación", JOptionPane.YES_NO_OPTION);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            reservaService.cancelarReserva(idReservaSeleccionada);
            JOptionPane.showMessageDialog(vista, "Reserva cancelada correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            cargarTablaReservas();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, ex.getMessage(), "No se pudo cancelar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        vista.getTxtFrase().setText("");
        vista.getTxtActividad().setText("");
        vista.getSpinnerFecha().setValue(new Date());
        vista.getListaCategorias().clearSelection();
        vista.getTablaReservas().clearSelection();
        idReservaSeleccionada = null;
    }

    private void cargarSeleccionEnFormulario() {
        int filaSeleccionada = vista.getTablaReservas().getSelectedRow();
        if (filaSeleccionada == -1) {
            idReservaSeleccionada = null;
            return;
        }

        idReservaSeleccionada = (String) vista.getModeloTablaReservas().getValueAt(filaSeleccionada, 0);

        reservaService.buscarPorId(idReservaSeleccionada)
                .ifPresent(this::mostrarReservaEnFormulario);
    }

    private void mostrarReservaEnFormulario(Reserva reserva) {
        vista.getTxtActividad().setText(reserva.getActividad());
        vista.getSpinnerFecha().setValue(Date.from(reserva.getFechaComoLocalDate()
                .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        vista.getSpinnerHoraInicio().setValue(Date.from(reserva.getHoraInicioComoLocalTime()
                .atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant()));
        vista.getSpinnerHoraFin().setValue(Date.from(reserva.getHoraFinComoLocalTime()
                .atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant()));

        vista.getListaCategorias().clearSelection();
        for (int i = 0; i < categoriasDisponibles.size(); i++) {
            if (reserva.getIdsCategoriasSolicitadas().contains(categoriasDisponibles.get(i).getId())) {
                vista.getListaCategorias().addSelectionInterval(i, i);
            }
        }
    }

    private void imprimirReservas() {
        try {
            List<Reserva> reservas = obtenerReservasParaMostrar();
            String rutaArchivo = ReportePdfGenerator.generarReporteReservas(usuarioActual.getId(), reservas);
            JOptionPane.showMessageDialog(vista, "Reporte generado en:\n" + rutaArchivo,
                    "Reporte PDF generado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, "No se pudo generar el reporte: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private DatosFormulario leerFormulario() {
        String actividad = vista.getTxtActividad().getText().trim();

        Date fechaDate = (Date) vista.getSpinnerFecha().getValue();
        LocalDate fecha = fechaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Date horaInicioDate = (Date) vista.getSpinnerHoraInicio().getValue();
        LocalTime horaInicio = horaInicioDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
                .withSecond(0).withNano(0);

        Date horaFinDate = (Date) vista.getSpinnerHoraFin().getValue();
        LocalTime horaFin = horaFinDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
                .withSecond(0).withNano(0);

        List<String> idsCategorias = new ArrayList<>();
        for (int indice : vista.getListaCategorias().getSelectedIndices()) {
            idsCategorias.add(categoriasDisponibles.get(indice).getId());
        }

        return new DatosFormulario(actividad, fecha, horaInicio, horaFin, idsCategorias);
    }

    private static class DatosFormulario {
        final String actividad;
        final LocalDate fecha;
        final LocalTime horaInicio;
        final LocalTime horaFin;
        final List<String> idsCategorias;

        DatosFormulario(String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, List<String> idsCategorias) {
            this.actividad = actividad;
            this.fecha = fecha;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.idsCategorias = idsCategorias;
        }
    }
}