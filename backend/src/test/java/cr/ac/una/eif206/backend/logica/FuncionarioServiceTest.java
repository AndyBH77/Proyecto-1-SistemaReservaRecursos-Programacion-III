package cr.ac.una.eif206.backend.logica;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Prueba de UNIDAD (Surefire): validaciones de datos de funcionario, sin base de datos.
 */
class FuncionarioServiceTest {

    @Test
    void datosValidosNoLanzanError() {
        assertDoesNotThrow(() -> FuncionarioService.validarDatos("Juan Mora", "8888-1111", "juan@gmail.com"));
    }

    @Test
    void correoInvalidoLanzaError() {
        assertThrows(Exception.class, () -> FuncionarioService.validarDatos("Juan", "8888-1111", "abc"));
        assertThrows(Exception.class, () -> FuncionarioService.validarDatos("Juan", "8888-1111", "abc@"));
    }

    @Test
    void nombreVacioLanzaError() {
        assertThrows(Exception.class, () -> FuncionarioService.validarDatos("  ", "8888-1111", "juan@gmail.com"));
    }

    @Test
    void telefonoConLetrasLanzaError() {
        assertThrows(Exception.class, () -> FuncionarioService.validarDatos("Juan", "telefono", "juan@gmail.com"));
    }
}