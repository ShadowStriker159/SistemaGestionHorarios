package co.edu.unbosque.SistemaHorario.model.dto;

import java.util.List;

public class DocenteDTO {

    private String id;
    private String nombre;
    private String apellido;
    private String correo;
    private String password;
    private String escalafon;
    private String estadoDocente;
    private String tipoVinculacion;
    private List<FranjaHorariaDTO> disponibilidad;

    public DocenteDTO() {
    }

    public DocenteDTO(String id, String nombre, String apellido, String correo, String password,
                      String escalafon, String estadoDocente,
                      String tipoVinculacion, List<FranjaHorariaDTO> disponibilidad) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.password = password;
        this.escalafon = escalafon;
        this.estadoDocente = estadoDocente;
        this.tipoVinculacion = tipoVinculacion;
        this.disponibilidad = disponibilidad;
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

    public String getEscalafon() {
        return escalafon;
    }

    public void setEscalafon(String escalafon) {
        this.escalafon = escalafon;
    }

    public String getEstadoDocente() {
        return estadoDocente;
    }

    public void setEstadoDocente(String estadoDocente) {
        this.estadoDocente = estadoDocente;
    }

    public String getTipoVinculacion() {
        return tipoVinculacion;
    }

    public void setTipoVinculacion(String tipoVinculacion) {
        this.tipoVinculacion = tipoVinculacion;
    }

    public List<FranjaHorariaDTO> getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(List<FranjaHorariaDTO> disponibilidad) {
        this.disponibilidad = disponibilidad;
    }
}