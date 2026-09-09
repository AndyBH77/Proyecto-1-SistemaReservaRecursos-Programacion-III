package cr.ac.una.eif206.negocio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de UNIDAD (la ejecuta Surefire con "mvn test"): prueba una sola
 * pieza aislada del sistema, sin tocar archivos ni otras clases.
 */
class PasswordGeneratorTest {

    @Test
    void generaClaveConLongitudCorrecta() {
        String clave = PasswordGenerator.generar(10);
        assertEquals(10, clave.length());
    }

    @Test
    void generaClaveConAlMenosUnaMayusculaUnaMinusculaYUnNumero() {
        String clave = PasswordGenerator.generar(10);
        assertTrue(clave.chars().anyMatch(Character::isUpperCase));
        assertTrue(clave.chars().anyMatch(Character::isLowerCase));
        assertTrue(clave.chars().anyMatch(Character::isDigit));
    }

    @Test
    void siPidenMenosDeSeisCaracteresIgualGeneraSeis() {
        String clave = PasswordGenerator.generar(2);
        assertEquals(6, clave.length());
    }
}
