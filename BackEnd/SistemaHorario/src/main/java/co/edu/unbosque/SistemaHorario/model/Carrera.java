package co.edu.unbosque.SistemaHorario.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "carrera")
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrera")
    private Integer idCarrera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_facultad", nullable = false)
    private Facultad facultad;

    @Column(name = "nombre_carrera", nullable = false, length = 150)
    private String nombreCarrera;

    @OneToMany(mappedBy = "carrera", fetch = FetchType.LAZY)
    private List<PlanEstudios> planEstudios;
    public Carrera() {
    }

    public Carrera(Integer idCarrera, String nombreCarrera, List<PlanEstudios> planEstudios) {
        this.idCarrera = idCarrera;
        this.nombreCarrera = nombreCarrera;
        this.planEstudios = planEstudios;
    }

    public Integer getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(Integer idCarrera) {
        this.idCarrera = idCarrera;
    }

    public Facultad getFacultad() {
        return facultad;
    }

    public void setFacultad(Facultad facultad) {
        this.facultad = facultad;
    }

    public String getNombreCarrera() {
        return nombreCarrera;
    }

    public void setNombreCarrera(String nombreCarrera) {
        this.nombreCarrera = nombreCarrera;
    }

    public List<PlanEstudios> getPlanEstudios() {
        return planEstudios;
    }

    public void setPlanEstudios(List<PlanEstudios> planEstudios) {
        this.planEstudios = planEstudios;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Carrera other = (Carrera) obj;
        return idCarrera.equals(other.idCarrera);
    }
}
