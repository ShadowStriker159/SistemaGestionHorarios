package co.edu.unbosque.SistemaHorario.model;

import jakarta.persistence.*;

@Entity
@Table(name = "aula")
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aula")
    private Integer idAula;

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

    public Aula(Integer idAula, Integer capacidad, TipoAula tipoAula, EstadoAula estadoAula, Bloque bloque) {
        this.idAula = idAula;
        this.capacidad = capacidad;
        this.tipoAula = tipoAula;
        this.estadoAula = estadoAula;
        this.bloque = bloque;
    }

    public Integer getIdAula() {
        return idAula;
    }

    public void setIdAula(Integer idAula) {
        this.idAula = idAula;
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
        if (obj == null || getClass() != obj.getClass())
            return false;

        Aula aula = (Aula) obj;
        return idAula.equals(aula.idAula);
    }
}
