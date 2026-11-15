package co.edu.unbosque.SistemaHorario.model.dto;

public class AulaDTO {
    private String numeroAula;
    private Integer capacidad;
    private String tipoAula;
    private String estadoAula;
    private BloqueDTO bloque;

    public AulaDTO() {
    }

    public AulaDTO(String numeroAula, Integer capacidad, String tipoAula, String estadoAula, BloqueDTO bloque) {
        this.numeroAula = numeroAula;
        this.capacidad = capacidad;
        this.tipoAula = tipoAula;
        this.estadoAula = estadoAula;
        this.bloque = bloque;
    }

    public String getNumeroAula() {
        return numeroAula;
    }

    public void setNumeroAula(String numeroAula) {
        this.numeroAula = numeroAula;
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
