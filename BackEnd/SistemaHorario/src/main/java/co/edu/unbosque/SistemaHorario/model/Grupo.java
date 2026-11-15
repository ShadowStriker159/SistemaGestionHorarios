package co.edu.unbosque.SistemaHorario.model;

import java.util.List;

public class Grupo {

    private Integer idGrupo;

    private Curso curso;

    private Docente docente;

    private EstadoGrupo estadoGrupo;

    private Integer cupoMaximo;

    private Integer cupoMinimo;

    private List<Estudiante> estudiantes;

    private List<Seccion> secciones;

    public Grupo() {
    }

    public Grupo(Integer idGrupo, EstadoGrupo estadoGrupo, List<Estudiante> estudiantes, List<Seccion> secciones, Docente docente, Integer cupoMaximo, Integer cupoMinimo) {
        this.idGrupo = idGrupo;
        this.estadoGrupo = estadoGrupo;
        this.estudiantes = estudiantes;
        this.secciones = secciones;
        this.docente = docente;
        this.cupoMaximo = cupoMaximo;
        this.cupoMinimo = cupoMinimo;
    }

    public Integer getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(Integer idGrupo) {
        this.idGrupo = idGrupo;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public EstadoGrupo getEstadoGrupo() {
        return estadoGrupo;
    }

    public void setEstadoGrupo(EstadoGrupo estadoGrupo) {
        this.estadoGrupo = estadoGrupo;
    }

    public Integer getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(Integer cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public Integer getCupoMinimo() {
        return cupoMinimo;
    }

    public void setCupoMinimo(Integer cupoMinimo) {
        this.cupoMinimo = cupoMinimo;
    }

    public List<Estudiante> getEstudiantes() {
        return estudiantes;
    }

    public void setEstudiantes(List<Estudiante> estudiantes) {
        this.estudiantes = estudiantes;
    }

    public List<Seccion> getSecciones() {
        return secciones;
    }

    public void setSecciones(List<Seccion> secciones) {
        this.secciones = secciones;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Grupo other = (Grupo) obj;
        return idGrupo.equals(other.idGrupo);
    }
}
