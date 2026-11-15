package co.edu.unbosque.SistemaHorario.model;

import java.util.List;

public class Facultad {

    private Integer idFacultad;

    private String nombreFacultad;

    private List<Area> areas;

    private List<Carrera> carreras;

    public Facultad() {
    }

    public Facultad(Integer idFacultad, String nombreFacultad, List<Area> areas, List<Carrera> carreras) {
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

    public List<Area> getAreas() {
        return areas;
    }

    public void setAreas(List<Area> areas) {
        this.areas = areas;
    }

    public List<Carrera> getCarreras() {
        return carreras;
    }

    public void setCarreras(List<Carrera> carreras) {
        this.carreras = carreras;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Facultad other = (Facultad) obj;
        return idFacultad.equals(other.idFacultad);
    }
}
