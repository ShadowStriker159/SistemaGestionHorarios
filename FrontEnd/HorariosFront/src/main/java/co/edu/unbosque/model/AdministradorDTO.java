package co.edu.unbosque.model;

public class AdministradorDTO {

	private String id;
	private String nombre;
	private String apellido;
	private String correo;
	private String password;
	private String tipoAdministrador;

	public AdministradorDTO() {
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

	public String getTipoAdministrador() {
		return tipoAdministrador;
	}

	public void setTipoAdministrador(String tipoAdministrador) {
		this.tipoAdministrador = tipoAdministrador;
	}
}
