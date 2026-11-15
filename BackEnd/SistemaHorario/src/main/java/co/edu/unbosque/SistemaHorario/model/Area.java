package co.edu.unbosque.SistemaHorario.model;

public class Area {
    private Integer idArea;

    private Facultad facultad;

    private String nombreArea;

    private String descripcion;
    public Area() {
    }

    public Area(Integer idArea, String nombreArea, String descripcion) {
        this.idArea = idArea;
        this.nombreArea = nombreArea;
        this.descripcion = descripcion;
    }

    public Integer getIdArea() {
        return idArea;
    }

    public void setIdArea(Integer idArea) {
        this.idArea = idArea;
    }

    public Facultad getFacultad() {
        return facultad;
    }

    public void setFacultad(Facultad facultad) {
        this.facultad = facultad;
    }

    public String getNombreArea() {
        return nombreArea;
    }

    public void setNombreArea(String nombreArea) {
        this.nombreArea = nombreArea;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Area area = (Area) obj;
        return idArea.equals(area.getIdArea());
    }
}
