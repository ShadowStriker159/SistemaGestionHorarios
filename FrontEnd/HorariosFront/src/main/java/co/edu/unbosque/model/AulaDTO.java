package co.edu.unbosque.model;

public class AulaDTO {

	private String numeroAula;
	private Integer capacidad;
	private TipoAula tipoAula;
	private EstadoAula estadoAula;
	private BloqueDTO bloque;

	public AulaDTO() {
	}

	public AulaDTO(String numeroAula, Integer capacidad, TipoAula tipoAula, EstadoAula estadoAula, BloqueDTO bloque) {
		this.numeroAula = numeroAula;
		this.capacidad = capacidad;
		this.tipoAula = tipoAula;
		this.estadoAula = estadoAula;
		this.bloque = bloque;
	}

	public String getNumeroAula() {
		return numeroAula;
	}

	public void setNumeroAula(String numeroAula) {
		this.numeroAula = numeroAula;
	}

	public Integer getCapacidad() {
		return capacidad;
	}

	public void setCapacidad(Integer capacidad) {
		this.capacidad = capacidad;
	}

	public TipoAula getTipoAula() {
		return tipoAula;
	}

	public void setTipoAula(TipoAula tipoAula) {
		this.tipoAula = tipoAula;
	}

	public EstadoAula getEstadoAula() {
		return estadoAula;
	}

	public void setEstadoAula(EstadoAula estadoAula) {
		this.estadoAula = estadoAula;
	}

	public BloqueDTO getBloque() {
		return bloque;
	}

	public void setBloque(BloqueDTO bloque) {
		this.bloque = bloque;
	}
}
