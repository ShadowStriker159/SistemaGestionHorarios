package co.edu.unbosque.model;

import java.io.Serializable;

public class PersonaDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nombre;
	private String apellido;
	private String id;

	public PersonaDTO() {
	}

	public PersonaDTO(String nombre, String apellido, String id) {
		this.nombre = nombre;
		this.apellido = apellido;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
