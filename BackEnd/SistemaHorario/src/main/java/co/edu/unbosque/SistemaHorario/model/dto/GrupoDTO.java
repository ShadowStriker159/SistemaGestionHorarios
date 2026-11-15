package co.edu.unbosque.SistemaHorario.model.dto;

import java.util.List;

public class GrupoDTO {
    private Integer idGrupo;
    private String estadoGrupo;
    private List<EstudianteDTO> estudiantes;
    private List<SeccionDTO> secciones;
    private DocenteDTO docente;
    private Integer cupoMaximo;
    private Integer cupoMinimo;

    public GrupoDTO() {
    }

    public GrupoDTO(Integer idGrupo, String estadoGrupo, List<EstudianteDTO> estudiantes, List<SeccionDTO> secciones, DocenteDTO docente, Integer cupoMaximo, Integer cupoMinimo) {
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

    public String getEstadoGrupo() {
        return estadoGrupo;
    }

    public void setEstadoGrupo(String estadoGrupo) {
        this.estadoGrupo = estadoGrupo;
    }

    public List<EstudianteDTO> getEstudiantes() {
        return estudiantes;
    }

    public void setEstudiantes(List<EstudianteDTO> estudiantes) {
        this.estudiantes = estudiantes;
    }

    public List<SeccionDTO> getSecciones() {
        return secciones;
    }

    public void setSecciones(List<SeccionDTO> secciones) {
        this.secciones = secciones;
    }

    public DocenteDTO getDocente() {
        return docente;
    }

    public void setDocente(DocenteDTO docente) {
        this.docente = docente;
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
}
