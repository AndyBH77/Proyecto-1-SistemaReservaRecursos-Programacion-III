package cr.ac.una.eif206.persistencia;

import cr.ac.una.eif206.modelo.Reserva;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservaDAO {

    private static final String ARCHIVO = "reservas.xml";

    public List<Reserva> listarTodas() {
        ReservasData data = XmlManager.cargar(ReservasData.class, ARCHIVO);
        if (data == null) {
            data = new ReservasData();
            data.setReservas(new ArrayList<>());
            XmlManager.guardar(data, ReservasData.class, ARCHIVO);
        }
        return data.getReservas();
    }

    public List<Reserva> listarPorFuncionario(String idFuncionario) {
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : listarTodas()) {
            if (r.getIdFuncionario().equalsIgnoreCase(idFuncionario)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    public Optional<Reserva> buscarPorId(String id) {
        return listarTodas().stream()
                .filter(r -> r.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    public void guardar(Reserva reserva) {
        List<Reserva> lista = listarTodas();
        lista.removeIf(r -> r.getId().equalsIgnoreCase(reserva.getId()));
        lista.add(reserva);
        guardarTodas(lista);
    }

    public void eliminar(String id) {
        List<Reserva> lista = listarTodas();
        lista.removeIf(r -> r.getId().equalsIgnoreCase(id));
        guardarTodas(lista);
    }

    public void guardarTodas(List<Reserva> lista) {
        ReservasData data = new ReservasData();
        data.setReservas(lista);
        XmlManager.guardar(data, ReservasData.class, ARCHIVO);
    }

    /**
     * Genera el siguiente id consecutivo tipo RES-000001, RES-000002, etc.
     */
    public String generarSiguienteId() {
        int maximo = 0;
        for (Reserva r : listarTodas()) {
            String numero = r.getId().replace("RES-", "");
            try {
                int valor = Integer.parseInt(numero);
                if (valor > maximo) {
                    maximo = valor;
                }
            } catch (NumberFormatException ignored) {
                // Si algun id no sigue el formato esperado, simplemente se ignora
            }
        }
        return String.format("RES-%06d", maximo + 1);
    }
}
