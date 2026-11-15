package co.edu.unbosque.model;

import java.util.List;

public class PlanEstudiosDTO {

    private Integer idPlanEstudios;
    private String nombrePlanEstudios;
    private List<CursoDTO> cursos;

    public PlanEstudiosDTO() {
    }

    public PlanEstudiosDTO(Integer idPlanEstudios, String nombrePlanEstudios, List<CursoDTO> cursos) {
        this.idPlanEstudios = idPlanEstudios;
        this.nombrePlanEstudios = nombrePlanEstudios;
        this.cursos = cursos;
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

    public List<CursoDTO> getCursos() {
        return cursos;
    }

    public void setCursos(List<CursoDTO> cursos) {
        this.cursos = cursos;
    }
}
