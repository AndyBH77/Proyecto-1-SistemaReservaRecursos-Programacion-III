package cr.ac.una.eif206.persistencia;

import cr.ac.una.eif206.modelo.Funcionario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "funcionarios")
@XmlAccessorType(XmlAccessType.FIELD)
public class FuncionariosData {

    @XmlElement(name = "funcionario")
    private List<Funcionario> funcionarios = new ArrayList<>();

    public List<Funcionario> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(List<Funcionario> funcionarios) {
        this.funcionarios = funcionarios;
    }
}
