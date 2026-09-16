package cr.ac.una.eif206.persistencia;

import cr.ac.una.eif206.modelo.CategoriaRecurso;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO basico de Categorias de Recurso.
 *
 * IMPORTANTE PARA EL EQUIPO: el CRUD completo (pantalla de busqueda,
 * inclusion, modificacion y borrado) de esta clase le corresponde a quien
 * desarrolle "4-Lista de categorias de recursos". Este DAO se incluye aqui
 * unicamente para que la pantalla de Reservas tenga categorias de donde
 * elegir, y se agregan algunas categorias de ejemplo (semilla) para probar.
 */
public class CategoriaRecursoDAO {

    private static final String ARCHIVO = "categorias.xml";

    public List<CategoriaRecurso> listarTodas() {
        CategoriasData data = XmlManager.cargar(CategoriasData.class, ARCHIVO);
        if (data == null) {
            data = crearDatosSemilla();
        }
        return data.getCategorias();
    }

    public Optional<CategoriaRecurso> buscarPorId(String id) {
        return listarTodas().stream()
                .filter(c -> c.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    public void guardarTodas(List<CategoriaRecurso> categorias) {
        CategoriasData data = new CategoriasData();
        data.setCategorias(categorias);
        XmlManager.guardar(data, CategoriasData.class, ARCHIVO);
    }

    /**
     * Inserta la categoría si el ID no existe, o actualiza la descripción
     * si ya existe (upsert).
     */
    public void guardar(CategoriaRecurso categoria) {
        List<CategoriaRecurso> lista = listarTodas();
        Optional<CategoriaRecurso> existente = lista.stream()
                .filter(c -> c.getId().equalsIgnoreCase(categoria.getId()))
                .findFirst();

        if (existente.isPresent()) {
            existente.get().setDescripcion(categoria.getDescripcion());
        } else {
            lista.add(categoria);
        }
        guardarTodas(lista);
    }

    public void eliminar(String id) {
        List<CategoriaRecurso> lista = listarTodas();
        lista.removeIf(c -> c.getId().equalsIgnoreCase(id));
        guardarTodas(lista);
    }

    public List<CategoriaRecurso> buscarPorTexto(String texto) {
        String filtro = (texto == null) ? "" : texto.trim().toLowerCase();
        List<CategoriaRecurso> resultado = new ArrayList<>();
        for (CategoriaRecurso c : listarTodas()) {
            if (c.getId().toLowerCase().contains(filtro)
                    || c.getDescripcion().toLowerCase().contains(filtro)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    private CategoriasData crearDatosSemilla() {
        CategoriasData data = new CategoriasData();
        List<CategoriaRecurso> lista = new ArrayList<>();
        lista.add(new CategoriaRecurso("CAT-000001", "Sala para 10 personas"));
        lista.add(new CategoriaRecurso("CAT-000002", "Laptop windows"));
        lista.add(new CategoriaRecurso("CAT-000003", "Sala de Juntas"));
        data.setCategorias(lista);
        XmlManager.guardar(data, CategoriasData.class, ARCHIVO);
        return data;
    }
}