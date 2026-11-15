package co.edu.unbosque.SistemaHorario.model.dto;

import java.time.LocalTime;

public class FranjaHorariaDTO {

    private Integer idFranja;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    public FranjaHorariaDTO() {
    }

    public FranjaHorariaDTO(Integer idFranja, LocalTime horaInicio, LocalTime horaFin) {
        this.idFranja = idFranja;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
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
}