package cr.ac.una.eif206.backend.red;

import cr.ac.una.eif206.common.modelo.CategoriaRecurso;
import cr.ac.una.eif206.common.modelo.EstadisticaItem;
import cr.ac.una.eif206.common.modelo.Recurso;
import cr.ac.una.eif206.common.modelo.TablaConsulta;
import cr.ac.una.eif206.common.protocolo.Comando;
import cr.ac.una.eif206.common.protocolo.Request;
import cr.ac.una.eif206.common.protocolo.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de INTEGRACION por SOCKET REAL de los comandos de Jordi: levanta el
 * servidor, inicia sesion y comprueba permisos y datos (todo debe ser
 * Serializable para poder viajar; si no, esta prueba falla).
 */
class RecursosConsultasSocketIT {

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

    /** Una conexion de prueba: envia un Request y devuelve el Response. */
    private static class Cliente implements AutoCloseable {
        private final Socket socket;
        private final ObjectOutputStream salida;
        private final ObjectInputStream entrada;

        Cliente() throws Exception {
            socket = new Socket("localhost", servidor.getPuerto());
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            entrada = new ObjectInputStream(socket.getInputStream());
        }

        Response enviar(Request request) throws Exception {
            salida.writeObject(request);
            salida.flush();
            salida.reset();
            return (Response) entrada.readObject();
        }

        void login(String id, String clave) throws Exception {
            Response r = enviar(new Request(Comando.LOGIN).agregar("id", id).agregar("clave", clave));
            assertTrue(r.isExito(), r.getMensaje());
        }

        @Override
        public void close() throws Exception {
            socket.close();
        }
    }

    @Test
    void sinSesionNoSePuedeConsultar() throws Exception {
        try (Cliente c = new Cliente()) {
            Response r = c.enviar(new Request(Comando.LISTAR_RECURSOS));
            assertFalse(r.isExito());
        }
    }

    @Test
    void funcionarioConsultaPeroNoPuedeAgregarRecursos() throws Exception {
        try (Cliente c = new Cliente()) {
            c.login("111", "111");

            Response lista = c.enviar(new Request(Comando.LISTAR_RECURSOS));
            assertTrue(lista.isExito(), lista.getMensaje());
            List<Recurso> recursos = lista.getDatos();
            assertTrue(recursos.size() >= 7);

            Response agregar = c.enviar(new Request(Comando.AGREGAR_RECURSO)
                    .agregar("id", "SOCK-1").agregar("idCategoria", 1).agregar("descripcion", "No debe crearse"));
            assertFalse(agregar.isExito());
            assertTrue(agregar.getMensaje().contains("ADMINISTRADOR"), agregar.getMensaje());
        }
    }

    @Test
    void administradorHaceElCrudCompletoPorSocket() throws Exception {
        try (Cliente c = new Cliente()) {
            c.login("admin", "admin1234");

            Response agregar = c.enviar(new Request(Comando.AGREGAR_RECURSO)
                    .agregar("id", "SOCK-2").agregar("idCategoria", 1).agregar("descripcion", "Sala de prueba socket"));
            assertTrue(agregar.isExito(), agregar.getMensaje());

            Response modificar = c.enviar(new Request(Comando.MODIFICAR_RECURSO)
                    .agregar("id", "SOCK-2").agregar("idCategoria", 1).agregar("descripcion", "Sala modificada"));
            assertTrue(modificar.isExito(), modificar.getMensaje());

            List<Recurso> deCategoria = c.enviar(new Request(Comando.LISTAR_RECURSOS_POR_CATEGORIA)
                    .agregar("idCategoria", 1)).getDatos();
            assertTrue(deCategoria.stream().anyMatch(r -> r.getId().equals("SOCK-2")
                    && r.getDescripcion().equals("Sala modificada")));

            Response eliminar = c.enviar(new Request(Comando.ELIMINAR_RECURSO).agregar("id", "SOCK-2"));
            assertTrue(eliminar.isExito(), eliminar.getMensaje());
        }
    }

    @Test
    void calendarizacionYActividadesViajanPorElSocket() throws Exception {
        try (Cliente c = new Cliente()) {
            c.login("222", "222");

            List<CategoriaRecurso> categorias = c.enviar(new Request(Comando.LISTAR_CATEGORIAS)).getDatos();
            int idSalas = categorias.stream().filter(x -> x.getDescripcion().equals("Sala para 10 personas"))
                    .findFirst().orElseThrow().getId();

            Response cal = c.enviar(new Request(Comando.CALENDARIZACION)
                    .agregar("fecha", LocalDate.of(2026, 10, 30)).agregar("idCategoria", idSalas));
            assertTrue(cal.isExito(), cal.getMensaje());
            TablaConsulta tabla = cal.getDatos();
            assertEquals("Hora", tabla.getEncabezados()[0]);

            Response semana = c.enviar(new Request(Comando.PROGRAMACION_SEMANAL)
                    .agregar("fecha", LocalDate.of(2026, 11, 2)));
            assertTrue(semana.isExito(), semana.getMensaje());
            assertEquals(8, ((TablaConsulta) semana.getDatos()).getCantidadColumnas());
        }
    }

    @Test
    void estadisticasViajanPorElSocketYValidanElRango() throws Exception {
        try (Cliente c = new Cliente()) {
            c.login("222", "222");

            Response ok = c.enviar(new Request(Comando.ESTADISTICAS_RECURSOS)
                    .agregar("desde", LocalDate.of(2026, 10, 1)).agregar("hasta", LocalDate.of(2026, 11, 30)));
            assertTrue(ok.isExito(), ok.getMensaje());
            List<EstadisticaItem> items = ok.getDatos();
            assertFalse(items.isEmpty());

            Response alReves = c.enviar(new Request(Comando.ESTADISTICAS_ACTIVIDADES)
                    .agregar("desde", LocalDate.of(2026, 11, 30)).agregar("hasta", LocalDate.of(2026, 10, 1)));
            assertFalse(alReves.isExito());
            assertEquals("La fecha desde no puede ser posterior a la fecha hasta.", alReves.getMensaje());
        }
    }
}
