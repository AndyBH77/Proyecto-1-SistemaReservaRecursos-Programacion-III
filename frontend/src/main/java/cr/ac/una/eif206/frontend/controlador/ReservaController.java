package cr.ac.una.eif206.frontend.controlador;

import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import cr.ac.una.eif206.common.modelo.DatosReservaExtraidos;
import cr.ac.una.eif206.common.modelo.Funcionario;
import cr.ac.una.eif206.common.modelo.Recurso;
import cr.ac.una.eif206.common.modelo.Reserva;
import cr.ac.una.eif206.common.modelo.ResultadoReserva;
import cr.ac.una.eif206.common.modelo.Usuario;
import cr.ac.una.eif206.frontend.proxy.ServiceProxy;
import cr.ac.una.eif206.frontend.reportes.ReportePdfGenerator;
import cr.ac.una.eif206.frontend.util.Constantes;
import cr.ac.una.eif206.frontend.util.Dialogos;
import cr.ac.una.eif206.frontend.vista.ReservaView;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controlador de la pestana Reservas.
 * Todo lo que necesita del backend lo pide al ServiceProxy (nunca a la BD).
 */
public class ReservaController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern(Constantes.FORMATO_FECHA);

    private final ReservaView vista;
    private final Usuario usuario;
    private final ServiceProxy proxy = ServiceProxy.getInstancia();

    private List<CategoriaRecurso> categorias = new ArrayList<>();
    private List<Reserva> reservas = new ArrayList<>();   // las que se ven en la tabla, en el mismo orden
    private Reserva reservaSeleccionada;

    public ReservaController(ReservaView vista, Usuario usuario) {
        this.vista = vista;
        this.usuario = usuario;
        registrarEventos();
        cargarCategorias();
        cargarReservas();
        limpiarFormulario();
    }

    private void registrarEventos() {
        vista.getBtnExtraerIA().addActionListener(e -> extraerConIA());
        vista.getBtnReservar().addActionListener(e -> crearReserva());
        vista.getBtnModificar().addActionListener(e -> modificarReserva());
        vista.getBtnCancelarReserva().addActionListener(e -> cancelarReserva());
        vista.getBtnLimpiar().addActionListener(e -> limpiarFormulario());
        vista.getBtnImprimir().addActionListener(e -> imprimir());

        vista.getTablaReservas().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });
    }

    // ==================================================================
    // Cargar datos
    // ==================================================================

    private void cargarCategorias() {
        try {
            categorias = proxy.listarCategorias();
            vista.getModeloListaCategorias().clear();
            for (CategoriaRecurso categoria : categorias) {
                vista.getModeloListaCategorias().addElement(categoria);
            }
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudieron cargar las categorias", ex);
        }
    }

    private void cargarReservas() {
        try {
            reservas = proxy.listarMisReservas();
        } catch (Exception ex) {
            reservas = new ArrayList<>();
            Dialogos.error(vista, "No se pudieron cargar las reservas", ex);
        }

        vista.getModeloTablaReservas().setRowCount(0);
        for (Reserva r : reservas) {
            vista.getModeloTablaReservas().addRow(new Object[]{
                    r.getId(),
                    r.getActividad(),
                    r.getFecha().format(FORMATO_FECHA),
                    r.getHoraInicio() + " - " + r.getHoraFin(),
                    String.join(", ", r.getDescripcionesRecursos()),
                    r.getEstado()
            });
        }
    }

    // ==================================================================
    // IA
    // ==================================================================

    private void extraerConIA() {
        String frase = vista.getTxtFrase().getText().trim();
        if (frase.isEmpty()) {
            Dialogos.advertencia(vista, "Frase vacia", "Escriba primero una frase describiendo la reserva.");
            return;
        }

        vista.getBtnExtraerIA().setEnabled(false);
        vista.getBtnExtraerIA().setText("Extrayendo...");

        // La IA puede tardar varios segundos: se hace en otro hilo
        new SwingWorker<DatosReservaExtraidos, Void>() {
            @Override
            protected DatosReservaExtraidos doInBackground() throws Exception {
                return proxy.extraerReservaIA(frase);
            }

            @Override
            protected void done() {
                vista.getBtnExtraerIA().setEnabled(true);
                vista.getBtnExtraerIA().setText("Extraer IA");
                try {
                    aplicarDatosExtraidos(get());
                } catch (Exception ex) {
                    Dialogos.error(vista, "No se pudo extraer la informacion", ex);
                }
            }
        }.execute();
    }

    private void aplicarDatosExtraidos(DatosReservaExtraidos datos) {
        if (datos.actividad != null && !datos.actividad.isBlank()) {
            vista.getTxtActividad().setText(datos.actividad);
        }
        if (datos.fecha != null) {
            vista.getSpinnerFecha().setValue(aDate(datos.fecha));
        }
        if (datos.horaInicio != null) {
            vista.getSpinnerHoraInicio().setValue(aDate(datos.horaInicio));
        }
        if (datos.horaFin != null) {
            vista.getSpinnerHoraFin().setValue(aDate(datos.horaFin));
        }

        vista.getListaCategorias().clearSelection();
        for (int i = 0; i < categorias.size(); i++) {
            String descripcion = categorias.get(i).getDescripcion();
            boolean coincide = datos.nombresCategorias.stream().anyMatch(n -> n.equalsIgnoreCase(descripcion));
            if (coincide) {
                vista.getListaCategorias().addSelectionInterval(i, i);
            }
        }

        Dialogos.info(vista, "Extraccion completada",
                "Se completo el formulario automaticamente. Revise los datos antes de reservar.");
    }

    // ==================================================================
    // Crear / modificar / cancelar
    // ==================================================================

    private void crearReserva() {
        try {
            DatosFormulario d = leerFormulario();
            ResultadoReserva resultado = proxy.crearReserva(d.actividad(), d.fecha(), d.horaInicio(),
                    d.horaFin(), d.idsCategorias());
            mostrarResultado(resultado, "creo");
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo reservar", ex);
        }
    }

    private void modificarReserva() {
        if (reservaSeleccionada == null) {
            Dialogos.advertencia(vista, "Ninguna reserva seleccionada",
                    "Seleccione primero una reserva de la tabla para modificarla.");
            return;
        }
        try {
            DatosFormulario d = leerFormulario();
            ResultadoReserva resultado = proxy.modificarReserva(reservaSeleccionada.getId(), d.actividad(),
                    d.fecha(), d.horaInicio(), d.horaFin(), d.idsCategorias());
            mostrarResultado(resultado, "modifico");
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo modificar", ex);
        }
    }

    private void mostrarResultado(ResultadoReserva resultado, String verbo) {
        if (resultado.isExito()) {
            Reserva r = resultado.getReserva();
            Dialogos.info(vista, "Exito", "La reserva se " + verbo + " correctamente (id: " + r.getId() + ").\n"
                    + "Recursos asignados: " + String.join(", ", r.getDescripcionesRecursos()));
            limpiarFormulario();
            cargarReservas();
        } else {
            Dialogos.advertencia(vista, "Sin disponibilidad",
                    "No hay disponibilidad para las siguientes categorias:\n"
                            + String.join(", ", resultado.getCategoriasNoDisponibles())
                            + "\n\nPuede modificar la reserva e intentar de nuevo.");
        }
    }

    private void cancelarReserva() {
        if (reservaSeleccionada == null) {
            Dialogos.advertencia(vista, "Ninguna reserva seleccionada", "Seleccione primero una reserva de la tabla.");
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(vista,
                "¿Seguro que desea cancelar la reserva \"" + reservaSeleccionada.getActividad() + "\"?",
                "Confirmar cancelacion", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            proxy.cancelarReserva(reservaSeleccionada.getId());
            Dialogos.info(vista, "Exito", "Reserva cancelada correctamente.");
            limpiarFormulario();
            cargarReservas();
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo cancelar", ex);
        }
    }

    // ==================================================================
    // Formulario
    // ==================================================================

    private void limpiarFormulario() {
        vista.getTxtFrase().setText("");
        vista.getTxtActividad().setText("");
        vista.getSpinnerFecha().setValue(aDate(LocalDate.now()));
        vista.getSpinnerHoraInicio().setValue(aDate(LocalTime.of(8, 0)));
        vista.getSpinnerHoraFin().setValue(aDate(LocalTime.of(9, 0)));
        vista.getListaCategorias().clearSelection();
        vista.getTablaReservas().clearSelection();
        reservaSeleccionada = null;
    }

    private void cargarSeleccionEnFormulario() {
        int fila = vista.getTablaReservas().getSelectedRow();
        if (fila < 0 || fila >= reservas.size()) {
            reservaSeleccionada = null;
            return;
        }
        reservaSeleccionada = reservas.get(fila);

        vista.getTxtActividad().setText(reservaSeleccionada.getActividad());
        vista.getSpinnerFecha().setValue(aDate(reservaSeleccionada.getFecha()));
        vista.getSpinnerHoraInicio().setValue(aDate(reservaSeleccionada.getHoraInicio()));
        vista.getSpinnerHoraFin().setValue(aDate(reservaSeleccionada.getHoraFin()));

        // Se marcan las categorias de los recursos que tiene la reserva
        Set<Integer> idsCategorias = new HashSet<>();
        for (Recurso recurso : reservaSeleccionada.getRecursos()) {
            idsCategorias.add(recurso.getIdCategoria());
        }
        vista.getListaCategorias().clearSelection();
        for (int i = 0; i < categorias.size(); i++) {
            if (idsCategorias.contains(categorias.get(i).getId())) {
                vista.getListaCategorias().addSelectionInterval(i, i);
            }
        }
    }

    /** Lee lo que el usuario puso en el formulario (las validaciones fuertes las hace el backend). */
    private DatosFormulario leerFormulario() {
        List<Integer> idsCategorias = new ArrayList<>();
        for (CategoriaRecurso categoria : vista.getListaCategorias().getSelectedValuesList()) {
            idsCategorias.add(categoria.getId());
        }
        return new DatosFormulario(
                vista.getTxtActividad().getText().trim(),
                aLocalDate(vista.getSpinnerFecha().getValue()),
                aLocalTime(vista.getSpinnerHoraInicio().getValue()),
                aLocalTime(vista.getSpinnerHoraFin().getValue()),
                idsCategorias);
    }

    /** "record": una clase pequena solo para agrupar los datos del formulario. */
    private record DatosFormulario(String actividad, LocalDate fecha, LocalTime horaInicio,
                                   LocalTime horaFin, List<Integer> idsCategorias) {
    }

    private void imprimir() {
        try {
            String nombre = (usuario instanceof Funcionario f) ? f.getNombre() : usuario.getId();
            String ruta = ReportePdfGenerator.generarReporteTabla("Reservas de " + nombre, vista.getTablaReservas());
            Dialogos.info(vista, "Reporte PDF generado", "Reporte generado en:\n" + ruta);
        } catch (Exception ex) {
            Dialogos.error(vista, "No se pudo generar el reporte", ex);
        }
    }

    // ==================================================================
    // Conversiones: los JSpinner trabajan con java.util.Date
    // ==================================================================

    private static Date aDate(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static Date aDate(LocalTime hora) {
        return Date.from(hora.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate aLocalDate(Object valor) {
        return ((Date) valor).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static LocalTime aLocalTime(Object valor) {
        return ((Date) valor).toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
                .withSecond(0).withNano(0);
    }
}