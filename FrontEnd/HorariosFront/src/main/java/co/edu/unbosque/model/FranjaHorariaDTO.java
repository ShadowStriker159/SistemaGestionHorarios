package co.edu.unbosque.model;

import java.time.LocalTime;
import java.util.List;

public class FranjaHorariaDTO {

    private Integer idFranja;
    private DiaSemana diaSemana;
    private EstadoFranja estadoFranja;
    private Categoria categoria;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    public FranjaHorariaDTO() {
    }

    public FranjaHorariaDTO(Integer idFranja, DiaSemana diaSemana, EstadoFranja estadoFranja,
                             Categoria categoria, LocalTime horaInicio, LocalTime horaFin) {
        this.idFranja = idFranja;
        this.diaSemana = diaSemana;
        this.estadoFranja = estadoFranja;
        this.categoria = categoria;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public Integer getIdFranja() {
        return idFranja;
    }

    public void setIdFranja(Integer idFranja) {
        this.idFranja = idFranja;
    }

    public DiaSemana getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(DiaSemana diaSemana) {
        this.diaSemana = diaSemana;
    }

    public EstadoFranja getEstadoFranja() {
        return estadoFranja;
    }

    public void setEstadoFranja(EstadoFranja estadoFranja) {
        this.estadoFranja = estadoFranja;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
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
}
