package co.edu.unbosque.model;

import java.io.Serializable;

public class UsuarioSesionDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String nombre;
	private String apellido;
	private String correo;
	private String rol;

	public UsuarioSesionDTO() {
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

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}
}
