package cr.ac.una.eif206.backend.logica;

import cr.ac.una.eif206.common.modelo.Funcionario;
import cr.ac.una.eif206.common.modelo.RolUsuario;
import cr.ac.una.eif206.common.modelo.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Prueba de INTEGRACION del login contra MySQL.
 * Usa los usuarios de prueba de schema.sql (admin/admin1234 y 222/222).
 * Si cambio esas claves a mano, vuelva a ejecutar schema.sql.
 */
class AutenticacionServiceIT {

    private final AutenticacionService service = new AutenticacionService();

    @Test
    void loginAdministradorCorrecto() throws Exception {
        Usuario usuario = service.login("admin", "admin1234");

        assertEquals(RolUsuario.ADMINISTRADOR, usuario.getRol());
        assertNull(usuario.getClave(), "La clave no debe viajar al frontend");
    }

    @Test
    void loginFuncionarioTraeSuNombre() throws Exception {
        Usuario usuario = service.login("222", "222");

        Funcionario funcionario = assertInstanceOf(Funcionario.class, usuario);
        assertEquals("Maria Mata", funcionario.getNombre());
    }

    @Test
    void loginConClaveIncorrectaLanzaError() {
        Exception error = assertThrows(Exception.class, () -> service.login("admin", "mala"));
        assertEquals("El id o la clave no son correctos.", error.getMessage());
    }

    @Test
    void loginConUsuarioInexistenteLanzaError() {
        assertThrows(Exception.class, () -> service.login("noexiste", "123"));
    }

    @Test
    void loginSinDatosLanzaError() {
        assertThrows(Exception.class, () -> service.login("", ""));
    }
}