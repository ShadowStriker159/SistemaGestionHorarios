package co.edu.unbosque.SistemaHorario.model;

import java.time.LocalTime;

public class FranjaHoraria {

    private Integer idFranja;

    private DiaSemana diaSemana;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private EstadoFranja estadoFranja;

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
