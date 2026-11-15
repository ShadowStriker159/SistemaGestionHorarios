package co.edu.unbosque.SistemaHorario.model;

import java.util.ArrayList;
import java.util.List;

public class Docente extends Persona {

    private Escalafon escalafon;

    private EstadoDocente estadoDocente;

    private TipoVinculacion tipoVinculacion;

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
