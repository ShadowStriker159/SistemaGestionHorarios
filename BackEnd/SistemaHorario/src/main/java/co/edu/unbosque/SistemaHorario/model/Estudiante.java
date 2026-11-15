package co.edu.unbosque.SistemaHorario.model;

import jakarta.persistence.*;

@Entity
@Table(name = "estudiante")
@PrimaryKeyJoinColumn(name = "id")
public class Estudiante extends Persona {

    @Column(name = "semestre_cursa")
    private Integer semestreCursa;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_estudiante")
    private EstadoEstudiante estadoEstudiante;

    @Column(name = "creditos_aprobados")
    private Integer creditosAprobados;

    public Estudiante() {
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public EstadoEstudiante getEstadoEstudiante() {
        return estadoEstudiante;
    }

    public void setEstadoEstudiante(EstadoEstudiante estadoEstudiante) {
        this.estadoEstudiante = estadoEstudiante;
    }

    public Integer getSemestreCursa() {
        return semestreCursa;
    }

    public void setSemestreCursa(Integer semestreCursa) {
        this.semestreCursa = semestreCursa;
    }

    public Integer getCreditosAprobados() {
        return creditosAprobados;
    }

    public void setCreditosAprobados(Integer creditosAprobados) {
        this.creditosAprobados = creditosAprobados;
    }
}
