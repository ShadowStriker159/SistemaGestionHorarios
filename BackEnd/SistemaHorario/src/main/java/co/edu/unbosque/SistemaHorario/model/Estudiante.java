package co.edu.unbosque.SistemaHorario.model;

public class Estudiante extends Persona {

    private PlanEstudios planEstudios;

    private Integer semestreCursa;

    private EstadoEstudiante estadoEstudiante;

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

    public PlanEstudios getPlanEstudios() {
        return planEstudios;
    }

    public void setPlanEstudios(PlanEstudios planEstudios) {
        this.planEstudios = planEstudios;
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
