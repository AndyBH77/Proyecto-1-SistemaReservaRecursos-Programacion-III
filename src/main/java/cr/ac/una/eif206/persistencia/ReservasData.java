package cr.ac.una.eif206.persistencia;

import cr.ac.una.eif206.modelo.Reserva;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "reservas")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReservasData {

    @XmlElement(name = "reserva")
    private List<Reserva> reservas = new ArrayList<>();

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }
}
