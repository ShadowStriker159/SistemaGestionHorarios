package co.edu.unbosque.SistemaHorario.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "grupo")
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo")
    private Integer idGrupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_grupo", nullable = false)
    private EstadoGrupo estadoGrupo;

    @Column(name = "cupo_maximo", nullable = false)
    private Integer cupoMaximo;

    @Column(name = "cupo_minimo", nullable = false)
    private Integer cupoMinimo;

    @ManyToMany
    @JoinTable(
            name = "grupo_estudiante",
            joinColumns = @JoinColumn(name = "id_grupo"),
            inverseJoinColumns = @JoinColumn(name = "id_estudiante")
    )
    private List<Estudiante> estudiantes;

    @OneToMany(mappedBy = "grupo", fetch = FetchType.LAZY)
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
