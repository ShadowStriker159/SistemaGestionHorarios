package co.edu.unbosque.SistemaHorario.model;

import java.io.Serializable;
import java.util.Objects;

public class AulaId implements Serializable {

    private Integer idBloque;

    private String numeroAula;

    public AulaId() {
    }

    public AulaId(Integer idBloque, String numeroAula) {
        this.idBloque = idBloque;
        this.numeroAula = numeroAula;
    }

    public Integer getIdBloque() {
        return idBloque;
    }

    public void setIdBloque(Integer idBloque) {
        this.idBloque = idBloque;
    }

    public String getNumeroAula() {
        return numeroAula;
    }

    public void setNumeroAula(String numeroAula) {
        this.numeroAula = numeroAula;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AulaId)) return false;
        AulaId other = (AulaId) obj;
        return Objects.equals(idBloque, other.idBloque) && Objects.equals(numeroAula, other.numeroAula);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idBloque, numeroAula);
    }
}
