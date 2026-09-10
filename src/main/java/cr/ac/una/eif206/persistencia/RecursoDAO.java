package cr.ac.una.eif206.persistencia;

import cr.ac.una.eif206.modelo.Recurso;

import java.util.ArrayList;
import java.util.List;

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
