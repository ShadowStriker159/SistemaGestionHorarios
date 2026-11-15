package co.edu.unbosque.SistemaHorario.model.dto;

public class EstudianteDTO {

    private String id;
    private String nombre;
    private String apellido;
    private String correo;
    private String password;
    private Integer semestreCursa;
    private String estadoEstudiante;
    private Integer creditosAprobados;

    public EstudianteDTO() {
    }

    public EstudianteDTO(String id, String nombre, String apellido, String correo,
                         String password, Integer semestreCursa,
                         String estadoEstudiante, Integer creditosAprobados) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.password = password;
        this.semestreCursa = semestreCursa;
        this.estadoEstudiante = estadoEstudiante;
        this.creditosAprobados = creditosAprobados;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEstadoEstudiante() {
        return estadoEstudiante;
    }

    public void setEstadoEstudiante(String estadoEstudiante) {
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
