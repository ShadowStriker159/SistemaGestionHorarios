package co.edu.unbosque.model;

public class EstudianteDTO extends PersonaDTO {

    private String correo;
    private Integer semestreCursa;
    private EstadoEstudiante estadoEstudiante;
    private Integer creditosAprobados;

    public EstudianteDTO() {
    }

    public EstudianteDTO(String nombre, String apellido, String id,
                         String correo, Integer semestreCursa,
                         EstadoEstudiante estadoEstudiante, Integer creditosAprobados) {
        super(nombre, apellido, id);
        this.correo = correo;
        this.semestreCursa = semestreCursa;
        this.estadoEstudiante = estadoEstudiante;
        this.creditosAprobados = creditosAprobados;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Integer getSemestreCursa() {
        return semestreCursa;
    }

    public void setSemestreCursa(Integer semestreCursa) {
        this.semestreCursa = semestreCursa;
    }

    public EstadoEstudiante getEstadoEstudiante() {
        return estadoEstudiante;
    }

    public void setEstadoEstudiante(EstadoEstudiante estadoEstudiante) {
        this.estadoEstudiante = estadoEstudiante;
    }

    public Integer getCreditosAprobados() {
        return creditosAprobados;
    }

    public void setCreditosAprobados(Integer creditosAprobados) {
        this.creditosAprobados = creditosAprobados;
    }
}
