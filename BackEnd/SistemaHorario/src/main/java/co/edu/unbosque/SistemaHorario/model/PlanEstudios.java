package co.edu.unbosque.SistemaHorario.model;

import java.util.List;

public class PlanEstudios {

    private Integer idPlanEstudios;

    private String nombrePlanEstudios;

    private List<Curso> cursos;

    private Carrera carrera;

    public PlanEstudios() {
    }

    public PlanEstudios(Integer idPlanEstudios, String nombrePlanEstudios, List<Curso> cursos) {
        this.idPlanEstudios = idPlanEstudios;
        this.nombrePlanEstudios = nombrePlanEstudios;
        this.cursos = cursos;
    }

    public Carrera getCarrera() {
        return carrera;
    }

    public void setCarrera(Carrera carrera) {
        this.carrera = carrera;
    }

    public Integer getIdPlanEstudios() {
        return idPlanEstudios;
    }

    public void setIdPlanEstudios(Integer idPlanEstudios) {
        this.idPlanEstudios = idPlanEstudios;
    }

    public String getNombrePlanEstudios() {
        return nombrePlanEstudios;
    }

    public void setNombrePlanEstudios(String nombrePlanEstudios) {
        this.nombrePlanEstudios = nombrePlanEstudios;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlanEstudios other = (PlanEstudios) obj;
        return idPlanEstudios.equals(other.idPlanEstudios);
    }
}
