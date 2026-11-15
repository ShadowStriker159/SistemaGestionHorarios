package co.edu.unbosque.model;

import java.io.Serializable;
import java.time.LocalTime;

public class HorarioDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer numero;
	private String sedeCampus;
	private String bloque;
	private String espacioFisico;
	private String grupo;
	private String programa;
	private String materia;
	private String estado;
	private String dia;
	private LocalTime horaInicial;
	private LocalTime horaFinal;
	private String nombreDocente;

	public HorarioDTO() {
	}

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	public String getSedeCampus() {
		return sedeCampus;
	}

	public void setSedeCampus(String sedeCampus) {
		this.sedeCampus = sedeCampus;
	}

	public String getBloque() {
		return bloque;
	}

	public void setBloque(String bloque) {
		this.bloque = bloque;
	}

	public String getEspacioFisico() {
		return espacioFisico;
	}

	public void setEspacioFisico(String espacioFisico) {
		this.espacioFisico = espacioFisico;
	}

	public String getGrupo() {
		return grupo;
	}

	public void setGrupo(String grupo) {
		this.grupo = grupo;
	}

	public String getPrograma() {
		return programa;
	}

	public void setPrograma(String programa) {
		this.programa = programa;
	}

	public String getMateria() {
		return materia;
	}

	public void setMateria(String materia) {
		this.materia = materia;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getDia() {
		return dia;
	}

	public void setDia(String dia) {
		this.dia = dia;
	}

	public LocalTime getHoraInicial() {
		return horaInicial;
	}

	public void setHoraInicial(LocalTime horaInicial) {
		this.horaInicial = horaInicial;
	}

	public LocalTime getHoraFinal() {
		return horaFinal;
	}

	public void setHoraFinal(LocalTime horaFinal) {
		this.horaFinal = horaFinal;
	}

	public String getNombreDocente() {
		return nombreDocente;
	}

	public void setNombreDocente(String nombreDocente) {
		this.nombreDocente = nombreDocente;
	}
}
