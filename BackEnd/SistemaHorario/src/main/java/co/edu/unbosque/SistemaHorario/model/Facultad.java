package co.edu.unbosque.SistemaHorario.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "facultad")
public class Facultad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_facultad")
    private Integer idFacultad;

    @Column(name = "nombre_facultad", nullable = false, length = 150, unique = true)
    private String nombreFacultad;

    @OneToMany(mappedBy = "facultad", fetch = FetchType.LAZY)
    private List<Area> areas;

    @OneToMany(mappedBy = "facultad", fetch = FetchType.LAZY)
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
