package co.edu.unbosque.SistemaHorario.model.dto;

public class SeccionDTO {
    private Integer idSeccion;
    private String estadoSeccion;
    private AulaDTO aula;
    private FranjaHorariaDTO franjaHoraria;

    public SeccionDTO() {
    }

    public SeccionDTO(Integer idSeccion, String estadoSeccion, AulaDTO aula, FranjaHorariaDTO franjaHoraria) {
        this.idSeccion = idSeccion;
        this.estadoSeccion = estadoSeccion;
        this.aula = aula;
        this.franjaHoraria = franjaHoraria;
    }

    public Integer getIdSeccion() {
        return idSeccion;
    }

    public void setIdSeccion(Integer idSeccion) {
        this.idSeccion = idSeccion;
    }

    public String getEstadoSeccion() {
        return estadoSeccion;
    }

    public void setEstadoSeccion(String estadoSeccion) {
        this.estadoSeccion = estadoSeccion;
    }

    public AulaDTO getAula() {
        return aula;
    }

    public void setAula(AulaDTO aula) {
        this.aula = aula;
    }

    public FranjaHorariaDTO getFranjaHoraria() {
        return franjaHoraria;
    }

    public void setFranjaHoraria(FranjaHorariaDTO franjaHoraria) {
        this.franjaHoraria = franjaHoraria;
    }
}
