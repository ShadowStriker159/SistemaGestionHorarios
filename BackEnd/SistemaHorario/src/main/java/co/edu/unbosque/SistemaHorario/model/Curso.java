package co.edu.unbosque.SistemaHorario.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "curso")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_curso")
    private Integer idCurso;

    @Column(name = "nombre_curso", nullable = false, length = 200)
    private String nombreCurso;

    @Column(name = "intensidad_horaria", nullable = false)
    private Integer intensidadHoraria;

    @Column(name = "semestre", nullable = false)
    private Integer semestre;

    @Column(name = "creditos", nullable = false)
    private Integer creditos;

    @Enumerated(EnumType.STRING)
    @Column(name = "aula_requerida", nullable = false)
    private TipoAula aulaRequerida;

    @ManyToMany
    @JoinTable(
            name = "curso_prerrequisito",
            joinColumns = @JoinColumn(name = "id_curso"),
            inverseJoinColumns = @JoinColumn(name = "id_prerrequisito")
    )
    private List<Curso> prerrequisitos;

    @OneToMany(mappedBy = "curso", fetch = FetchType.LAZY)
    private List<Grupo> grupos;

    @ManyToMany(mappedBy = "cursos")
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
