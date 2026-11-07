package cr.ac.una.eif206.backend.red;

import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import cr.ac.una.eif206.common.protocolo.Comando;
import cr.ac.una.eif206.common.protocolo.Request;
import cr.ac.una.eif206.common.protocolo.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de INTEGRACION: levanta el servidor de verdad, se conecta por
 * socket como lo haria un frontend, y pide datos que vienen de MySQL.
 */
class ServidorSocketIT {

    private static Servidor servidor;

    @BeforeAll
    static void levantarServidor() throws Exception {
        servidor = new Servidor(0);                 // 0 = cualquier puerto libre
        Thread hilo = new Thread(servidor::iniciar);
        hilo.setDaemon(true);                       // se cierra solo al terminar las pruebas
        hilo.start();
    }

    @AfterAll
    static void detenerServidor() {
        servidor.detener();
    }

    @Test
    void listarCategoriasPorSocket() throws Exception {
        try (Socket socket = new Socket("localhost", servidor.getPuerto())) {
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

            salida.writeObject(new Request(Comando.LISTAR_CATEGORIAS));
            salida.flush();
            Response respuesta = (Response) entrada.readObject();

            assertTrue(respuesta.isExito(), "El servidor respondio error: " + respuesta.getMensaje());
            List<CategoriaRecurso> categorias = respuesta.getDatos();
            assertFalse(categorias.isEmpty());
        }
    }

    @Test
    void variasPeticionesPorLaMismaConexion() throws Exception {
        try (Socket socket = new Socket("localhost", servidor.getPuerto())) {
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

            salida.writeObject(new Request(Comando.LISTAR_CATEGORIAS));
            salida.flush();
            List<CategoriaRecurso> primera = ((Response) entrada.readObject()).getDatos();

            salida.writeObject(new Request(Comando.LISTAR_CATEGORIAS));
            salida.flush();
            List<CategoriaRecurso> segunda = ((Response) entrada.readObject()).getDatos();

            assertEquals(primera.size(), segunda.size());
        }
    }
}