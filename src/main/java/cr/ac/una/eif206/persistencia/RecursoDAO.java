package cr.ac.una.eif206.persistencia;

import cr.ac.una.eif206.modelo.Recurso;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO basico de Recursos. Igual que CategoriaRecursoDAO, el CRUD completo
 * ("5-Lista de recursos") lo implementa otro integrante del equipo; aqui
 * solo se deja lo necesario para que Reservas pueda consultar que recursos
 * existen por categoria, con datos de ejemplo.
 */
public class RecursoDAO {

    private static final String ARCHIVO = "recursos.xml";

    public List<Recurso> listarTodos() {
        RecursosData data = XmlManager.cargar(RecursosData.class, ARCHIVO);
        if (data == null) {
            data = crearDatosSemilla();
        }
        return data.getRecursos();
    }

    public List<Recurso> listarPorCategoria(String idCategoria) {
        List<Recurso> resultado = new ArrayList<>();
        for (Recurso r : listarTodos()) {
            if (r.getIdCategoria().equalsIgnoreCase(idCategoria)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    public void guardarTodos(List<Recurso> recursos) {
        RecursosData data = new RecursosData();
        data.setRecursos(recursos);
        XmlManager.guardar(data, RecursosData.class, ARCHIVO);
    }

    /**
     * Inserta el recurso si el ID no existe, o actualiza sus datos
     * si ya existe (upsert).
     */
    public void guardar(Recurso recurso) {
        List<Recurso> lista = listarTodos();
        Optional<Recurso> existente = lista.stream()
                .filter(r -> r.getId().equalsIgnoreCase(recurso.getId()))
                .findFirst();

        if (existente.isPresent()) {
            Recurso actual = existente.get();
            actual.setIdCategoria(recurso.getIdCategoria());
            actual.setDescripcion(recurso.getDescripcion());
        } else {
            lista.add(recurso);
        }
        guardarTodos(lista);
    }

    public void eliminar(String id) {
        List<Recurso> lista = listarTodos();
        lista.removeIf(r -> r.getId().equalsIgnoreCase(id));
        guardarTodos(lista);
    }

    public List<Recurso> buscarPorTexto(String texto) {
        String filtro = (texto == null) ? "" : texto.trim().toLowerCase();
        List<Recurso> resultado = new ArrayList<>();
        for (Recurso r : listarTodos()) {
            if (r.getId().toLowerCase().contains(filtro)
                    || r.getIdCategoria().toLowerCase().contains(filtro)
                    || r.getDescripcion().toLowerCase().contains(filtro)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    public Optional<Recurso> buscarPorId(String id) {
        return listarTodos().stream()
                .filter(r -> r.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    private RecursosData crearDatosSemilla() {
        RecursosData data = new RecursosData();
        List<Recurso> lista = new ArrayList<>();
        lista.add(new Recurso("34343", "CAT-000001", "Sala 1 primer piso"));
        lista.add(new Recurso("238715", "CAT-000002", "Laptop #238715"));
        lista.add(new Recurso("45238", "CAT-000002", "Laptop #45238"));
        lista.add(new Recurso("452784", "CAT-000003", "Sala de Juntas - Torre A"));
        data.setRecursos(lista);
        XmlManager.guardar(data, RecursosData.class, ARCHIVO);
        return data;
    }
}
