package co.edu.unbosque.SistemaHorario.model;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(
        name = "franja_horaria",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"dia_semana", "hora_inicio", "hora_fin"})
        }
)
public class FranjaHoraria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_franja")
    private Integer idFranja;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DiaSemana diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_franja", nullable = false)
    private EstadoFranja estadoFranja;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private Categoria categoria;

    public FranjaHoraria() {
    }

    public FranjaHoraria(Integer idFranja, DiaSemana diaSemana, EstadoFranja estadoFranja, Categoria categoria, LocalTime horaInicio, LocalTime horaFin) {
        this.idFranja = idFranja;
        this.diaSemana = diaSemana;
        this.estadoFranja = estadoFranja;
        this.categoria = categoria;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public EstadoFranja getEstadoFranja() {
        return estadoFranja;
    }

    public void setEstadoFranja(EstadoFranja estadoFranja) {
        this.estadoFranja = estadoFranja;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public DiaSemana getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(DiaSemana diaSemana) {
        this.diaSemana = diaSemana;
    }

    public Integer getIdFranja() {
        return idFranja;
    }

    public void setIdFranja(Integer idFranja) {
        this.idFranja = idFranja;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        FranjaHoraria other = (FranjaHoraria) obj;
        return idFranja.equals(other.idFranja);
    }
}
