package cr.ac.una.eif206.persistencia;

import cr.ac.una.eif206.modelo.Administrador;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO (Data Access Object) para Administradores.
 * Si el archivo aun no existe, se crea automaticamente un administrador
 * "semilla" (admin / admin1234) para poder ingresar al sistema la primera vez.
 */
public class AdministradorDAO {

    private static final String ARCHIVO = "administradores.xml";

    public List<Administrador> listarTodos() {
        AdministradoresData data = XmlManager.cargar(AdministradoresData.class, ARCHIVO);
        if (data == null) {
            data = crearDatosSemilla();
        }
        return data.getAdministradores();
    }

    public Optional<Administrador> buscarPorId(String id) {
        return listarTodos().stream()
                .filter(a -> a.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    public void guardarTodos(List<Administrador> administradores) {
        AdministradoresData data = new AdministradoresData();
        data.setAdministradores(administradores);
        XmlManager.guardar(data, AdministradoresData.class, ARCHIVO);
    }

    /**
     * Crea y guarda un administrador por defecto para que el sistema se
     * pueda usar la primera vez que se ejecuta.
     * Usuario: admin | Clave: admin1234
     */
    private AdministradoresData crearDatosSemilla() {
        AdministradoresData data = new AdministradoresData();
        List<Administrador> lista = new ArrayList<>();
        lista.add(new Administrador("admin", "admin1234", "admin@correo.com"));
        data.setAdministradores(lista);
        XmlManager.guardar(data, AdministradoresData.class, ARCHIVO);
        return data;
    }
}
