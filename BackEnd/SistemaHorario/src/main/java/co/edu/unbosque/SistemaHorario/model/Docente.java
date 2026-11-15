package co.edu.unbosque.SistemaHorario.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "docente")
@PrimaryKeyJoinColumn(name = "id")
public class Docente extends Persona {

    @Enumerated(EnumType.STRING)
    @Column(name = "escalafon")
    private Escalafon escalafon;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_docente")
    private EstadoDocente estadoDocente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vinculacion")
    private TipoVinculacion tipoVinculacion;

    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FranjaHoraria> disponibilidad = new ArrayList<>();

    public Docente() {
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public Escalafon getEscalafon() {
        return escalafon;
    }

    public void setEscalafon(Escalafon escalafon) {
        this.escalafon = escalafon;
    }

    public EstadoDocente getEstadoDocente() {
        return estadoDocente;
    }

    public void setEstadoDocente(EstadoDocente estadoDocente) {
        this.estadoDocente = estadoDocente;
    }

    public TipoVinculacion getTipoVinculacion() {
        return tipoVinculacion;
    }

    public void setTipoVinculacion(TipoVinculacion tipoVinculacion) {
        this.tipoVinculacion = tipoVinculacion;
    }

    public List<FranjaHoraria> getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(List<FranjaHoraria> disponibilidad) {
        this.disponibilidad = disponibilidad;
    }
}