package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.backend.datos.FuncionarioDAO;
import cr.ac.una.eif206.common.modelo.Funcionario;
import cr.ac.una.eif206.common.modelo.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de INTEGRACION de funcionarios contra MySQL.
 * No envia correos: guarda directo con el DAO (el envio real se prueba a mano).
 */
class FuncionarioServiceIT {

    private static final String ID_PRUEBA = "999";

    private final FuncionarioService service = new FuncionarioService();
    private final FuncionarioDAO dao = new FuncionarioDAO();

    @AfterEach
    void borrarLoCreado() throws Exception {
        dao.eliminar(ID_PRUEBA);
    }

    private void crearFuncionarioDePrueba() throws Exception {
        dao.insertar(new Funcionario(ID_PRUEBA, "clave999", "prueba@ejemplo.com", "Prueba IT", "8888-9999"));
    }

    @Test
    void elFuncionarioInsertadoPuedeIniciarSesion() throws Exception {
        crearFuncionarioDePrueba();

        Usuario usuario = new AutenticacionService().login(ID_PRUEBA, "clave999");

        Funcionario f = assertInstanceOf(Funcionario.class, usuario);
        assertEquals("Prueba IT", f.getNombre());
    }

    @Test
    void buscarPorIdONombre() throws Exception {
        crearFuncionarioDePrueba();

        assertEquals(1, service.buscar("999").size());
        assertEquals(1, service.buscar("Prueba IT").size());
        assertNull(service.buscar("999").get(0).getClave(), "La clave no debe salir del backend");
    }

    @Test
    void modificarCambiaLosDatos() throws Exception {
        crearFuncionarioDePrueba();

        service.modificar(ID_PRUEBA, "Prueba Modificada", "8888-0000", "otro@ejemplo.com");

        Funcionario f = dao.buscarPorId(ID_PRUEBA).orElseThrow();
        assertEquals("Prueba Modificada", f.getNombre());
        assertEquals("8888-0000", f.getTelefono());
        assertEquals("otro@ejemplo.com", f.getEmail());
    }

    @Test
    void registrarConIdExistenteFallaAntesDeEnviarCorreo() {
        // "111" ya existe en schema.sql: debe fallar sin intentar enviar ningun correo
        Exception error = assertThrows(Exception.class,
                () -> service.registrar("111", "Repetido", "8888-1111", "repetido@ejemplo.com"));
        assertTrue(error.getMessage().contains("Ya existe"));
    }

    @Test
    void noPermiteEliminarFuncionarioConReservas() {
        // "222" (Maria Mata) tiene reservas en schema.sql
        Exception error = assertThrows(Exception.class, () -> service.eliminar("222"));
        assertTrue(error.getMessage().contains("reservas"));
    }
}