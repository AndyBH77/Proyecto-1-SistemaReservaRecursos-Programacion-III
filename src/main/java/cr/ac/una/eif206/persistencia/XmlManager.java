package cr.ac.una.eif206.persistencia;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;

/**
 * Utilidad generica para leer y escribir objetos en archivos XML usando JAXB.
 *
 * Se usa siempre con una clase "envoltorio" (wrapper), por ejemplo
 * ReservasData, que contenga la lista real de datos, ya que JAXB necesita
 * un elemento raiz para poder guardar una lista completa.
 */
public class XmlManager {

    // Carpeta donde se guardan todos los archivos XML del sistema (actua
    // como "base de datos" de este proyecto). Se crea sola si no existe.
    private static final String CARPETA_DATOS = "data";

    private XmlManager() {
        // Clase de utilidades: no se instancia
    }

    /**
     * Devuelve la ruta completa dentro de la carpeta "data/" para el
     * archivo indicado, creando la carpeta si hace falta.
     */
    public static String rutaArchivo(String nombreArchivo) {
        File carpeta = new File(CARPETA_DATOS);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        return CARPETA_DATOS + File.separator + nombreArchivo;
    }

    public static <T> void guardar(T objetoRaiz, Class<T> clase, String nombreArchivo) {
        try {
            JAXBContext contexto = JAXBContext.newInstance(clase);
            Marshaller marshaller = contexto.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(objetoRaiz, new File(rutaArchivo(nombreArchivo)));
        } catch (JAXBException e) {
            // En un sistema mas avanzado esto se manejaria con una excepcion propia.
            // Para este proyecto lo dejamos simple con un mensaje en consola.
            System.err.println("Error guardando archivo XML " + nombreArchivo + ": " + e.getMessage());
            throw new RuntimeException("No se pudo guardar el archivo " + nombreArchivo, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T cargar(Class<T> clase, String nombreArchivo) {
        try {
            File archivo = new File(rutaArchivo(nombreArchivo));
            if (!archivo.exists()) {
                return null; // El llamador decide que hacer si no existe (ej: crear vacio)
            }
            JAXBContext contexto = JAXBContext.newInstance(clase);
            Unmarshaller unmarshaller = contexto.createUnmarshaller();
            return (T) unmarshaller.unmarshal(archivo);
        } catch (JAXBException e) {
            System.err.println("Error leyendo archivo XML " + nombreArchivo + ": " + e.getMessage());
            throw new RuntimeException("No se pudo leer el archivo " + nombreArchivo, e);
        }
    }
}
