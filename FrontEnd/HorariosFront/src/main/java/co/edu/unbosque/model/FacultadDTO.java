package co.edu.unbosque.model;

import java.util.List;

public class FacultadDTO {

    private Integer idFacultad;
    private String nombreFacultad;
    private List<AreaDTO> areas;
    private List<CarreraDTO> carreras;

    public FacultadDTO() {
    }

    public FacultadDTO(Integer idFacultad, String nombreFacultad,
                       List<AreaDTO> areas, List<CarreraDTO> carreras) {
        this.idFacultad = idFacultad;
        this.nombreFacultad = nombreFacultad;
        this.areas = areas;
        this.carreras = carreras;
    }

    public Integer getIdFacultad() {
        return idFacultad;
    }

    public void setIdFacultad(Integer idFacultad) {
        this.idFacultad = idFacultad;
    }

    public String getNombreFacultad() {
        return nombreFacultad;
    }

    public void setNombreFacultad(String nombreFacultad) {
        this.nombreFacultad = nombreFacultad;
    }

    public List<AreaDTO> getAreas() {
        return areas;
    }

    public void setAreas(List<AreaDTO> areas) {
        this.areas = areas;
    }

    public List<CarreraDTO> getCarreras() {
        return carreras;
    }

    public void setCarreras(List<CarreraDTO> carreras) {
        this.carreras = carreras;
    }
}
