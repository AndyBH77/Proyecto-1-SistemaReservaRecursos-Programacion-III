package cr.ac.una.eif206.persistencia;

import cr.ac.una.eif206.modelo.Funcionario;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FuncionarioDAO {

    private static final String ARCHIVO = "funcionarios.xml";

    public List<Funcionario> listarTodos() {
        FuncionariosData data = XmlManager.cargar(FuncionariosData.class, ARCHIVO);
        if (data == null) {
            data = new FuncionariosData();
            data.setFuncionarios(new ArrayList<>());
            XmlManager.guardar(data, FuncionariosData.class, ARCHIVO);
        }
        return data.getFuncionarios();
    }

    public Optional<Funcionario> buscarPorId(String id) {
        return listarTodos().stream()
                .filter(f -> f.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    /**
     Busca funcionarios cuyo id o nombre contengan el texto dado
     (sin importar mayusculas/minusculas). Se usa en la pantalla de
     Funcionarios para el campo de busqueda.
     */
    public List<Funcionario> buscarPorTexto(String texto) {
        String textoBusqueda = texto.toLowerCase();
        List<Funcionario> resultado = new ArrayList<>();
        for (Funcionario f : listarTodos()) {
            boolean coincideId = f.getId().toLowerCase().contains(textoBusqueda);
            boolean coincideNombre = f.getNombre().toLowerCase().contains(textoBusqueda);
            if (coincideId || coincideNombre) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    public boolean existeId(String id) {
        return buscarPorId(id).isPresent();
    }

    public void guardar(Funcionario funcionario) {
        List<Funcionario> lista = listarTodos();
        lista.removeIf(f -> f.getId().equalsIgnoreCase(funcionario.getId()));
        lista.add(funcionario);
        guardarTodos(lista);
    }

    public void eliminar(String id) {
        List<Funcionario> lista = listarTodos();
        lista.removeIf(f -> f.getId().equalsIgnoreCase(id));
        guardarTodos(lista);
    }

    private void guardarTodos(List<Funcionario> lista) {
        FuncionariosData data = new FuncionariosData();
        data.setFuncionarios(lista);
        XmlManager.guardar(data, FuncionariosData.class, ARCHIVO);
    }
}