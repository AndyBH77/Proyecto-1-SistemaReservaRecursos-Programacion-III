package cr.ac.una.eif206.backend;

import cr.ac.una.eif206.backend.red.Servidor;
import cr.ac.una.eif206.backend.util.ConfigManager;

import java.io.IOException;

/**
 * Punto de entrada del BACKEND. Se ejecuta primero, antes de abrir los frontends.
 * No tiene ventana: todo lo que pasa se ve en la consola.
 */
public class ServidorMain {

    public static void main(String[] args) {
        int puerto = ConfigManager.obtenerEntero("servidor.puerto", 5000);

        try {
            Servidor servidor = new Servidor(puerto);
            servidor.iniciar();   // se queda aqui atendiendo clientes
        } catch (IOException e) {
            System.err.println("No se pudo abrir el puerto " + puerto + ": " + e.getMessage());
            System.err.println("Puede que ya haya otro servidor corriendo. Detengalo e intente de nuevo.");
        }
    }
}