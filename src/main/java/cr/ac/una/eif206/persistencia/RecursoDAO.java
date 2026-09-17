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
        int idContador = 1;

        for (int i = 1; i <= 4; i++) {
            lista.add(new Recurso(String.format("REC-%06d", idContador++), "CAT-000001", "Sala para 10 personas - Nivel " + i));
        }

        for (int i = 1; i <= 4; i++) {
            lista.add(new Recurso(String.format("REC-%06d", idContador++), "CAT-000002", "Laptop Dell Latitude #" + i));
        }

        lista.add(new Recurso(String.format("REC-%06d", idContador++), "CAT-000003", "Sala de Juntas - Torre A"));
        lista.add(new Recurso(String.format("REC-%06d", idContador++), "CAT-000003", "Sala de Juntas - Torre B"));
        lista.add(new Recurso(String.format("REC-%06d", idContador++), "CAT-000003", "Sala de Juntas - Ejecutiva"));
        lista.add(new Recurso(String.format("REC-%06d", idContador++), "CAT-000003", "Sala de Juntas - Anexo"));

        for (int cat = 4; cat <= 14; cat++) {
            String idCat = String.format("CAT-%06d", cat);
            String nombreAula = (cat <= 9) ? "100" + (cat - 3) : "200" + (cat - 9);
            for (int i = 1; i <= 4; i++) {
                lista.add(new Recurso(String.format("REC-%06d", idContador++), idCat, "Estación de trabajo " + i + " - Aula " + nombreAula));
            }
        }

        for (int i = 1; i <= 4; i++) {
            lista.add(new Recurso(String.format("REC-%06d", idContador++), "CAT-000015", "Silla ergonómica negra #" + i));
        }


        for (int i = 1; i <= 4; i++) {
            lista.add(new Recurso(String.format("REC-%06d", idContador++), "CAT-000016", "Silla estándar azul #" + i));
        }

        for (int i = 1; i <= 4; i++) {
            lista.add(new Recurso(String.format("REC-%06d", idContador++), "CAT-000017", "Lote de 10 sillas plegables - Bodega " + i));
        }

        for (int i = 1; i <= 4; i++) {
            lista.add(new Recurso(String.format("REC-%06d", idContador++), "CAT-000018", "MacBook Pro M2 - Diseño #" + i));
        }

        for (int i = 1; i <= 4; i++) {
            lista.add(new Recurso(String.format("REC-%06d", idContador++), "CAT-000019", "PC Escritorio HP - Lab " + i));
        }

        for (int i = 1; i <= 4; i++) {
            lista.add(new Recurso(String.format("REC-%06d", idContador++), "CAT-000020", "ThinkPad Ubuntu - IT #" + i));
        }

        for (int i = 1; i <= 4; i++) {
            lista.add(new Recurso(String.format("REC-%06d", idContador++), "CAT-000021", "iPad Pro 11 - Sala Juntas #" + i));
        }

        data.setRecursos(lista);
        XmlManager.guardar(data, RecursosData.class, ARCHIVO);
        return data;
    }
}
