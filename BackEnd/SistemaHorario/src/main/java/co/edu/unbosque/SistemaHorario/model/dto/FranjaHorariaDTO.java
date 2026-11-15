package co.edu.unbosque.SistemaHorario.model.dto;

import java.time.LocalTime;

public class FranjaHorariaDTO {
    private Integer idFranja;
    private String diaSemana;
    private String estadoFranja;
    private String categoria;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    public FranjaHorariaDTO() {
    }

    public FranjaHorariaDTO(Integer idFranja, String diaSemana, String estadoFranja, String categoria, LocalTime horaInicio, LocalTime horaFin) {
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

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public String getEstadoFranja() {
        return estadoFranja;
    }

    public void setEstadoFranja(String estadoFranja) {
        this.estadoFranja = estadoFranja;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
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
