package co.edu.unbosque.SistemaHorario.model;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "franja_horaria")
public class FranjaHoraria {

    @Id
    @Column(name = "id_franja")
    private Integer idFranja;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    public FranjaHoraria() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FranjaHoraria that = (FranjaHoraria) obj;
        return idFranja != null && idFranja.equals(that.idFranja);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public Integer getIdFranja() {
        return idFranja;
    }

    public void setIdFranja(Integer idFranja) {
        this.idFranja = idFranja;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }
}