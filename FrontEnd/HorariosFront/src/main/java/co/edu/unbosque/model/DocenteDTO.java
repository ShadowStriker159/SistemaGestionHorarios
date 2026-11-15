package co.edu.unbosque.model;

import java.util.List;

public class DocenteDTO extends PersonaDTO {

    private Escalafon escalafon;
    private EstadoDocente estadoDocente;
    private TipoVinculacion tipoVinculacion;
    private List<FranjaHorariaDTO> disponibilidad;

    public DocenteDTO() {
    }

    public DocenteDTO(String nombre, String apellido, String id,
                      Escalafon escalafon, EstadoDocente estadoDocente,
                      TipoVinculacion tipoVinculacion,
                      List<FranjaHorariaDTO> disponibilidad) {
        super(nombre, apellido, id);
        this.escalafon = escalafon;
        this.estadoDocente = estadoDocente;
        this.tipoVinculacion = tipoVinculacion;
        this.disponibilidad = disponibilidad;
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

    public java.util.List<FranjaHorariaDTO> getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(java.util.List<FranjaHorariaDTO> disponibilidad) {
        this.disponibilidad = disponibilidad;
    }
}
