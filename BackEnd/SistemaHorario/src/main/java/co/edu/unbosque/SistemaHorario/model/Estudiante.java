package co.edu.unbosque.SistemaHorario.model;

import jakarta.persistence.*;

@Entity
@Table(name = "estudiante")
@PrimaryKeyJoinColumn(name = "id")
public class Estudiante extends Persona {

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_estudiante")
    private EstadoEstudiante estadoEstudiante;

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
}