package co.edu.unbosque.model;

import java.util.List;

public class GrupoDTO {

	private Integer idGrupo;
	private EstadoGrupo estadoGrupo;
	private List<EstudianteDTO> estudiantes;
	private List<SeccionDTO> secciones;
	private DocenteDTO docente;
	private CursoDTO curso;
	private Integer cupoMaximo;
	private Integer cupoMinimo;

	public GrupoDTO() {
	}

	public GrupoDTO(Integer idGrupo, EstadoGrupo estadoGrupo, List<EstudianteDTO> estudiantes,
			List<SeccionDTO> secciones, DocenteDTO docente, Integer cupoMaximo, Integer cupoMinimo) {
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

	public EstadoGrupo getEstadoGrupo() {
		return estadoGrupo;
	}

	public void setEstadoGrupo(EstadoGrupo estadoGrupo) {
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

	public CursoDTO getCurso() {
		return curso;
	}

	public void setCurso(CursoDTO curso) {
		this.curso = curso;
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
