package co.edu.unbosque.model;

import java.util.List;

public class CursoDTO {

	private Integer idCurso;
	private String nombreCurso;
	private Integer intensidadHoraria;
	private Integer semestre;
	private Integer creditos;
	private TipoAula aulaRequerida;
	private List<CursoDTO> prerrequisitos;
	private List<GrupoDTO> grupos;

	public CursoDTO() {
	}

	public CursoDTO(Integer idCurso, String nombreCurso, Integer intensidadHoraria, Integer semestre, Integer creditos,
			TipoAula aulaRequerida, List<CursoDTO> prerrequisitos, List<GrupoDTO> grupos) {
		this.idCurso = idCurso;
		this.nombreCurso = nombreCurso;
		this.intensidadHoraria = intensidadHoraria;
		this.semestre = semestre;
		this.creditos = creditos;
		this.aulaRequerida = aulaRequerida;
		this.prerrequisitos = prerrequisitos;
		this.grupos = grupos;
	}

	public Integer getIdCurso() {
		return idCurso;
	}

	public void setIdCurso(Integer idCurso) {
		this.idCurso = idCurso;
	}

	public String getNombreCurso() {
		return nombreCurso;
	}

	public void setNombreCurso(String nombreCurso) {
		this.nombreCurso = nombreCurso;
	}

	public Integer getIntensidadHoraria() {
		return intensidadHoraria;
	}

	public void setIntensidadHoraria(Integer intensidadHoraria) {
		this.intensidadHoraria = intensidadHoraria;
	}

	public Integer getSemestre() {
		return semestre;
	}

	public void setSemestre(Integer semestre) {
		this.semestre = semestre;
	}

	public Integer getCreditos() {
		return creditos;
	}

	public void setCreditos(Integer creditos) {
		this.creditos = creditos;
	}

	public TipoAula getAulaRequerida() {
		return aulaRequerida;
	}

	public void setAulaRequerida(TipoAula aulaRequerida) {
		this.aulaRequerida = aulaRequerida;
	}

	public List<CursoDTO> getPrerrequisitos() {
		return prerrequisitos;
	}

	public void setPrerrequisitos(List<CursoDTO> prerrequisitos) {
		this.prerrequisitos = prerrequisitos;
	}

	public List<GrupoDTO> getGrupos() {
		return grupos;
	}

	public void setGrupos(List<GrupoDTO> grupos) {
		this.grupos = grupos;
	}
}
