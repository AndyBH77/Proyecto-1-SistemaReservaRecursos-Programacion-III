package cr.ac.una.eif206.frontend.controlador;

import cr.ac.una.eif206.common.modelo.Reserva;
import cr.ac.una.eif206.common.protocolo.Notificacion;
import cr.ac.una.eif206.frontend.vista.alertas.AlertasPanel;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Prueba de UNIDAD (Surefire) del area de alertas: no necesita servidor ni BD.
 * Simula que llegan alertas y revisa que aparezcan en la tabla.
 */
class AlertasControllerTest {

    private Notificacion alerta(String funcionario, String actividad) {
        Reserva r = new Reserva("111", actividad, LocalDate.of(2026, 11, 10), LocalTime.of(8, 0), LocalTime.of(10, 0));
        r.setNombreFuncionario(funcionario);
        return new Notificacion(r);
    }

    @Test
    void cadaAlertaRecibidaAgregaUnaFila() {
        AlertasPanel panel = new AlertasPanel();
        AlertasController controller = new AlertasController(panel);

        controller.alRecibir(alerta("Juan Mora", "Reunion clientes"));
        controller.alRecibir(alerta("Ana Lopez", "Coordinacion viaje"));

        assertEquals(2, controller.cantidadAlertas());
        assertEquals(2, panel.getTabla().getRowCount());
        assertEquals("Juan Mora - Reunion clientes", panel.getTabla().getValueAt(0, AlertasPanel.COLUMNA_ACTIVIDAD));
        assertEquals("Ana Lopez - Coordinacion viaje", panel.getTabla().getValueAt(1, AlertasPanel.COLUMNA_ACTIVIDAD));
    }
}