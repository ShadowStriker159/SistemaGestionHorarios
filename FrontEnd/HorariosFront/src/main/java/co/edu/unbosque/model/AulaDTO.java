package co.edu.unbosque.model;

public class AulaDTO {

    private Integer idAula;
    private Integer capacidad;
    private TipoAula tipoAula;
    private EstadoAula estadoAula;
    private BloqueDTO bloque;

    public AulaDTO() {
    }

    public AulaDTO(Integer idAula, Integer capacidad, TipoAula tipoAula, EstadoAula estadoAula, BloqueDTO bloque) {
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

    public BloqueDTO getBloque() {
        return bloque;
    }

    public void setBloque(BloqueDTO bloque) {
        this.bloque = bloque;
    }
}
