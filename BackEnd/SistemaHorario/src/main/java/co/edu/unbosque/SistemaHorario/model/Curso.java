package co.edu.unbosque.SistemaHorario.model;

import java.util.List;

public class Curso {

    private Integer idCurso;

    private String nombreCurso;

    private Integer intensidadHoraria;

    private Integer semestre;

    private Integer creditos;

    private TipoAula aulaRequerida;

    private List<Curso> prerrequisitos;

    private List<Grupo> grupos;

    private List<PlanEstudios> planesEstudio;

    public Curso() {
    }

    public Curso(Integer idCurso, String nombreCurso, Integer intensidadHoraria, Integer semestre, Integer creditos, TipoAula aulaRequerida, List<Curso> prerrequisitos, List<Grupo> grupos) {
        this.idCurso = idCurso;
        this.nombreCurso = nombreCurso;
        this.intensidadHoraria = intensidadHoraria;
        this.semestre = semestre;
        this.creditos = creditos;
        this.aulaRequerida = aulaRequerida;
        this.prerrequisitos = prerrequisitos;
        this.grupos = grupos;
    }

    public Integer getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(Integer idCurso) {
        this.idCurso = idCurso;
    }

    public String getNombreCurso() {
        return nombreCurso;
    }

    public void setNombreCurso(String nombreCurso) {
        this.nombreCurso = nombreCurso;
    }

    public Integer getIntensidadHoraria() {
        return intensidadHoraria;
    }

    public void setIntensidadHoraria(Integer intensidadHoraria) {
        this.intensidadHoraria = intensidadHoraria;
    }

    public Integer getSemestre() {
        return semestre;
    }

    public void setSemestre(Integer semestre) {
        this.semestre = semestre;
    }

    public Integer getCreditos() {
        return creditos;
    }

    public void setCreditos(Integer creditos) {
        this.creditos = creditos;
    }

    public TipoAula getAulaRequerida() {
        return aulaRequerida;
    }

    public void setAulaRequerida(TipoAula aulaRequerida) {
        this.aulaRequerida = aulaRequerida;
    }

    public List<Curso> getPrerrequisitos() {
        return prerrequisitos;
    }

    public void setPrerrequisitos(List<Curso> prerrequisitos) {
        this.prerrequisitos = prerrequisitos;
    }

    public List<Grupo> getGrupos() {
        return grupos;
    }

    public void setGrupos(List<Grupo> grupos) {
        this.grupos = grupos;
    }

    public List<PlanEstudios> getPlanesEstudio() {
        return planesEstudio;
    }

    public void setPlanesEstudio(List<PlanEstudios> planesEstudio) {
        this.planesEstudio = planesEstudio;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Curso other = (Curso) obj;
        return idCurso.equals(other.idCurso);
    }
}
