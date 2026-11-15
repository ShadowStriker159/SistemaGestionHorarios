package co.edu.unbosque.model;

import java.util.List;

public class CarreraDTO {

	private Integer idCarrera;
	private String nombreCarrera;
	private List<PlanEstudiosDTO> planEstudios;

	public CarreraDTO() {
	}

	public CarreraDTO(Integer idCarrera, String nombreCarrera, List<PlanEstudiosDTO> planEstudios) {
		this.idCarrera = idCarrera;
		this.nombreCarrera = nombreCarrera;
		this.planEstudios = planEstudios;
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

	public List<PlanEstudiosDTO> getPlanEstudios() {
		return planEstudios;
	}

	public void setPlanEstudios(List<PlanEstudiosDTO> planEstudios) {
		this.planEstudios = planEstudios;
	}
}
