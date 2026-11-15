package co.edu.unbosque.model;

import java.util.List;

public class CarreraDTO {

    private Integer idCarrera;
    private String nombreCarrera;
    private List<PlanEstudiosDTO> planesEstudios;

    public CarreraDTO() {
    }

    public CarreraDTO(Integer idCarrera, String nombreCarrera, List<PlanEstudiosDTO> planesEstudios) {
        this.idCarrera = idCarrera;
        this.nombreCarrera = nombreCarrera;
        this.planesEstudios = planesEstudios;
    }

    public Integer getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(Integer idCarrera) {
        this.idCarrera = idCarrera;
    }

    public String getNombreCarrera() {
        return nombreCarrera;
    }

    public void setNombreCarrera(String nombreCarrera) {
        this.nombreCarrera = nombreCarrera;
    }

    public List<PlanEstudiosDTO> getPlanesEstudios() {
        return planesEstudios;
    }

    public void setPlanesEstudios(List<PlanEstudiosDTO> planesEstudios) {
        this.planesEstudios = planesEstudios;
    }
}
