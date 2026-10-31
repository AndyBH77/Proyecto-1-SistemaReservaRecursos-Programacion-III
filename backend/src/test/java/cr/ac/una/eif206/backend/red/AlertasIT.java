package cr.ac.una.eif206.backend.red;

import cr.ac.una.eif206.common.protocolo.Comando;
import cr.ac.una.eif206.common.protocolo.Notificacion;
import cr.ac.una.eif206.common.protocolo.Request;
import cr.ac.una.eif206.common.protocolo.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de INTEGRACION del canal de alertas:
 * un "frontend" se suscribe y otro dispara una alerta; la alerta debe llegar
 * por el canal del primero SIN que este la pida.
 */
class AlertasIT {

    private static Servidor servidor;

    @BeforeAll
    static void levantarServidor() throws Exception {
        servidor = new Servidor(0);
        Thread hilo = new Thread(servidor::iniciar);
        hilo.setDaemon(true);
        hilo.start();
    }

    @AfterAll
    static void detenerServidor() {
        servidor.detener();
    }

    @Test
    void laAlertaLlegaPorElCanalDeAlertas() throws Exception {
        try (Socket canalAlertas = new Socket("localhost", servidor.getPuerto());
             Socket otroFrontend = new Socket("localhost", servidor.getPuerto())) {

            // 1. Frontend A (usuario 111) abre su canal de alertas
            ObjectOutputStream salidaA = new ObjectOutputStream(canalAlertas.getOutputStream());
            salidaA.flush();
            ObjectInputStream entradaA = new ObjectInputStream(canalAlertas.getInputStream());
            salidaA.writeObject(new Request(Comando.SUSCRIBIR_ALERTAS).agregar("idUsuario", "111"));
            salidaA.flush();
            assertTrue(((Response) entradaA.readObject()).isExito());

            // 2. Frontend B inicia sesion (222) y dispara una alerta de prueba
            ObjectOutputStream salidaB = new ObjectOutputStream(otroFrontend.getOutputStream());
            salidaB.flush();
            ObjectInputStream entradaB = new ObjectInputStream(otroFrontend.getInputStream());

            salidaB.writeObject(new Request(Comando.LOGIN).agregar("id", "222").agregar("clave", "222"));
            salidaB.flush();
            assertTrue(((Response) entradaB.readObject()).isExito());

            salidaB.writeObject(new Request(Comando.ALERTA_PRUEBA));
            salidaB.flush();
            assertTrue(((Response) entradaB.readObject()).isExito());

            // 3. La notificacion llega a A sin que A haya pedido nada (max 5 segundos)
            canalAlertas.setSoTimeout(5000);
            Notificacion alerta = assertInstanceOf(Notificacion.class, entradaA.readObject());
            assertEquals("Alerta de prueba", alerta.getReserva().getActividad());
            assertEquals("Sistema - Alerta de prueba", alerta.getTitulo());
        }
    }
}