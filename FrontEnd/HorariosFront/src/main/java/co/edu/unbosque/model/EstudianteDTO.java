package co.edu.unbosque.model;

public class EstudianteDTO extends PersonaDTO {

	private String correo;
	private String password;
	private Integer semestreCursa;
	private EstadoEstudiante estadoEstudiante;
	private Integer creditosAprobados;
	private Integer idPlanEstudios;
	private Integer idCarrera;
	private String nombreCarrera;

	public EstudianteDTO() {
	}

	public EstudianteDTO(String nombre, String apellido, String id, String correo, Integer semestreCursa,
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public Integer getIdPlanEstudios() {
		return idPlanEstudios;
	}

	public void setIdPlanEstudios(Integer idPlanEstudios) {
		this.idPlanEstudios = idPlanEstudios;
	}

	public Integer getIdCarrera() {
		return idCarrera;
	}

	public void setIdCarrera(Integer idCarrera) {
		this.idCarrera = idCarrera;
	}

	public String getNombreCarrera() {
		return nombreCarrera;
	}

	public void setNombreCarrera(String nombreCarrera) {
		this.nombreCarrera = nombreCarrera;
	}
}
