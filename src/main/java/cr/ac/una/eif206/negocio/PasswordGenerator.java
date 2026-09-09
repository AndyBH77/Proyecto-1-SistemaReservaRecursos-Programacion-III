package cr.ac.una.eif206.negocio;

import java.security.SecureRandom;

/**
 * Genera claves aleatorias que cumplen las reglas basicas pedidas:
 * al menos una mayuscula, una minuscula y un numero.
 */
public class PasswordGenerator {

    // Se excluyen caracteres facilmente confundibles (I, O, 0, 1, l)
    private static final String MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijkmnpqrstuvwxyz";
    private static final String NUMEROS = "23456789";
    private static final String TODOS = MAYUSCULAS + MINUSCULAS + NUMEROS;

    private static final SecureRandom random = new SecureRandom();

    private PasswordGenerator() {
    }

    /**
     * Genera una clave aleatoria de la longitud indicada (minimo 6),
     * garantizando al menos una mayuscula, una minuscula y un numero.
     */
    public static String generar(int longitud) {
        if (longitud < 6) {
            longitud = 6;
        }

        StringBuilder clave = new StringBuilder();

        // Se garantiza al menos un caracter de cada tipo requerido
        clave.append(MAYUSCULAS.charAt(random.nextInt(MAYUSCULAS.length())));
        clave.append(MINUSCULAS.charAt(random.nextInt(MINUSCULAS.length())));
        clave.append(NUMEROS.charAt(random.nextInt(NUMEROS.length())));

        // El resto de caracteres se completan al azar
        for (int i = clave.length(); i < longitud; i++) {
            clave.append(TODOS.charAt(random.nextInt(TODOS.length())));
        }

        // Se mezclan los caracteres para que los primeros 3 no siempre
        // queden en el mismo orden fijo (Mayuscula, minuscula, numero)
        return mezclar(clave.toString());
    }

    private static String mezclar(String texto) {
        char[] caracteres = texto.toCharArray();
        for (int i = caracteres.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = caracteres[i];
            caracteres[i] = caracteres[j];
            caracteres[j] = temp;
        }
        return new String(caracteres);
    }
}
