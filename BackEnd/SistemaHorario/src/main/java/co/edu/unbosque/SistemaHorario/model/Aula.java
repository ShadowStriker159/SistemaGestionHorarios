package co.edu.unbosque.SistemaHorario.model;

import jakarta.persistence.*;

@Entity
@Table(name = "aula")
public class Aula {

    @EmbeddedId
    private AulaId id;

    @MapsId("idBloque")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bloque", nullable = false)
    private Bloque bloque;

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_aula", nullable = false)
    private TipoAula tipoAula;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_aula", nullable = false)
    private EstadoAula estadoAula;

    public Aula() {
    }

    public Aula(AulaId id, Integer capacidad, TipoAula tipoAula, EstadoAula estadoAula, Bloque bloque) {
        this.id = id;
        this.capacidad = capacidad;
        this.tipoAula = tipoAula;
        this.estadoAula = estadoAula;
        this.bloque = bloque;
    }

    public AulaId getId() {
        return id;
    }

    public void setId(AulaId id) {
        this.id = id;
    }

    public String getNumeroAula() {
        return id == null ? null : id.getNumeroAula();
    }

    public Bloque getBloque() {
        return bloque;
    }

    public void setBloque(Bloque bloque) {
        this.bloque = bloque;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public TipoAula getTipoAula() {
        return tipoAula;
    }

    public void setTipoAula(TipoAula tipoAula) {
        this.tipoAula = tipoAula;
    }

    public EstadoAula getEstadoAula() {
        return estadoAula;
    }

    public void setEstadoAula(EstadoAula estadoAula) {
        this.estadoAula = estadoAula;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Aula))
            return false;

        Aula aula = (Aula) obj;
        return id != null && id.equals(aula.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
