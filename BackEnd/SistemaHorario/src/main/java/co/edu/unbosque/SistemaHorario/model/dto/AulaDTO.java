package co.edu.unbosque.SistemaHorario.model.dto;

public class AulaDTO {
    private Integer idAula;
    private Integer capacidad;
    private String tipoAula;
    private String estadoAula;
    private BloqueDTO bloque;

    public AulaDTO() {
    }

    public AulaDTO(Integer idAula, Integer capacidad, String tipoAula, String estadoAula, BloqueDTO bloque) {
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

    public String getTipoAula() {
        return tipoAula;
    }

    public void setTipoAula(String tipoAula) {
        this.tipoAula = tipoAula;
    }

    public String getEstadoAula() {
        return estadoAula;
    }

    public void setEstadoAula(String estadoAula) {
        this.estadoAula = estadoAula;
    }

    public BloqueDTO getBloque() {
        return bloque;
    }

    public void setBloque(BloqueDTO bloque) {
        this.bloque = bloque;
    }
}
