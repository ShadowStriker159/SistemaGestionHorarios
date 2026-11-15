package co.edu.unbosque.beans;

import co.edu.unbosque.service.DocenteService;
import co.edu.unbosque.service.EstudianteService;
import co.edu.unbosque.service.FacultadService;
import co.edu.unbosque.model.CarreraDTO;
import co.edu.unbosque.model.DocenteDTO;
import co.edu.unbosque.model.EstudianteDTO;
import co.edu.unbosque.model.PlanEstudiosDTO;
import co.edu.unbosque.service.ApiClient;
import co.edu.unbosque.service.ApiClient.ApiResponse;
import com.google.gson.reflect.TypeToken;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Named("personasBean")
@SessionScoped
public class SecretariaPersonasBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private DocenteService docenteService;

	@Inject
	private EstudianteService estudianteService;

	@Inject
	private FacultadService facultadService;

	// Listas recibidas del backend
	private List<DocenteDTO> docentes = new ArrayList<>();
	private List<EstudianteDTO> estudiantes = new ArrayList<>();
	private List<CarreraDTO> carrerasDisponibles = new ArrayList<>();
	private List<PlanEstudiosDTO> planesDisponibles = new ArrayList<>();

	// Formulario Docente
	private DocenteDTO docenteForm = new DocenteDTO();
	private EstudianteDTO estudianteForm = new EstudianteDTO();
	private boolean editandoDocente;
	private boolean mostrarFormularioDocente;
	private boolean editandoEstudiante;
	private boolean mostrarFormularioEstudiante;

	// Mensajes
	private String mensajeError = "";
	private String mensajeOk = "";

	@PostConstruct
	public void init() {
		cargarDocentes();
		cargarEstudiantes();
		cargarCarreras();
	}

	// DOCENTES

	public void cargarDocentes() {
		try {
			docentes = docenteService.listarTodos();
		} catch (co.edu.unbosque.service.ApiClient.ApiException e) {
			docentes = new ArrayList<>();
			error("No se pudieron cargar los docentes: " + e.getMessage());
		}
	}

	public String recargarDocentes() {
		cargarDocentes();
		return null;
	}

	public String crearDocente() {
		limpiarMensajes();
		if (isBlank(docenteForm.getId()) || isBlank(docenteForm.getNombre()) || isBlank(docenteForm.getApellido())
				|| isBlank(docenteForm.getCorreo()) || isBlank(docenteForm.getPassword())
				|| docenteForm.getEscalafon() == null || docenteForm.getEstadoDocente() == null
				|| docenteForm.getTipoVinculacion() == null) {
			return error("Completa todos los campos del docente.");
		}
		ApiResponse r = docenteService.crear(docenteForm);
		if (r.isOk()) {
			ok("Docente " + docenteForm.getNombre() + " registrado exitosamente.");
			docenteForm = new DocenteDTO();
			mostrarFormularioDocente = false;
			cargarDocentes();
		} else if (r.isConflict()) {
			error("Ya existe un docente con ese ID o correo.");
		} else {
			error("Error al registrar el docente: " + r.getBody());
		}
		return null;
	}

	public String mostrarFormularioDocente() {
		docenteForm = new DocenteDTO();
		editandoDocente = false;
		mostrarFormularioDocente = true;
		limpiarMensajes();
		return null;
	}

	public String editarDocente(DocenteDTO docente) {
		limpiarMensajes();
		docenteForm = new DocenteDTO(docente.getNombre(), docente.getApellido(), docente.getId(),
				docente.getEscalafon(), docente.getEstadoDocente(), docente.getTipoVinculacion(),
				docente.getDisponibilidad());
		docenteForm.setCorreo(docente.getCorreo());
		docenteForm.setPassword("");
		editandoDocente = true;
		mostrarFormularioDocente = true;
		return null;
	}

	public String guardarDocente() {
		if (!editandoDocente) {
			return crearDocente();
		}

		limpiarMensajes();
		if (isBlank(docenteForm.getId()) || isBlank(docenteForm.getNombre()) || isBlank(docenteForm.getApellido())
				|| isBlank(docenteForm.getCorreo()) || docenteForm.getEscalafon() == null
				|| docenteForm.getEstadoDocente() == null || docenteForm.getTipoVinculacion() == null) {
			return error("Completa todos los campos obligatorios del docente.");
		}

		ApiResponse r = docenteService.actualizar(docenteForm.getId(), docenteForm);
		if (r.isOk()) {
			ok("Docente actualizado exitosamente.");
			docenteForm = new DocenteDTO();
			editandoDocente = false;
			mostrarFormularioDocente = false;
			cargarDocentes();
		} else if (r.isConflict()) {
			error("Ya existe una persona con ese correo.");
		} else if (r.isNotFound()) {
			error("No se encontró el docente que intentas actualizar.");
		} else {
			error("Error al actualizar el docente: " + r.getBody());
		}
		return null;
	}

	public String cancelarEdicionDocente() {
		docenteForm = new DocenteDTO();
		editandoDocente = false;
		mostrarFormularioDocente = false;
		limpiarMensajes();
		return null;
	}

	public String eliminarDocente(DocenteDTO d) {
		limpiarMensajes();
		ApiResponse r = docenteService.eliminar(d.getId());
		if (r.isOk()) {
			ok("Docente eliminado.");
			cargarDocentes();
		} else if (r.isConflict())
			error(r.getBody());
		else if (r.getCode() == 404)
			error("No se encontró el docente que intentas eliminar.");
		else if (r.getCode() == 405) {
			error("El backend activo no tiene habilitada la eliminación de docentes. "
					+ "Recompila y reinicia SistemaHorario para activar sus endpoints actuales.");
		} else
			error("No se pudo eliminar: " + r.getBody());
		return null;
	}

	// ESTUDIANTES

	public void cargarEstudiantes() {
		try {
			estudiantes = estudianteService.listarTodos();
		} catch (co.edu.unbosque.service.ApiClient.ApiException e) {
			estudiantes = new ArrayList<>();
			error("No se pudieron cargar los estudiantes: " + e.getMessage());
		}
	}

	public String recargarEstudiantes() {
		cargarEstudiantes();
		return null;
	}

	public void cargarCarreras() {
		try {
			carrerasDisponibles = facultadService.listarTodasLasCarreras();
		} catch (co.edu.unbosque.service.ApiClient.ApiException e) {
			carrerasDisponibles = new ArrayList<>();
			error("No se pudieron cargar las carreras: " + e.getMessage());
		}
	}

	public void cargarPlanesEstudiante() {
		planesDisponibles = new ArrayList<>();
		estudianteForm.setIdPlanEstudios(null);
		Integer idCarrera = estudianteForm.getIdCarrera();
		if (idCarrera == null || idCarrera <= 0) {
			return;
		}
		try {
			Type tipo = new TypeToken<List<PlanEstudiosDTO>>() {
			}.getType();
			List<PlanEstudiosDTO> planes = ApiClient.gson().fromJson(facultadService.listarPlanes(idCarrera), tipo);
			if (planes == null) {
				throw new IllegalStateException("El backend devolvió una lista de planes nula.");
			}
			planesDisponibles = planes;
		} catch (ApiClient.ApiException e) {
			error("No se pudieron cargar los planes de la carrera: " + e.getMessage());
		}
	}

	public String crearEstudiante() {
		limpiarMensajes();
		if (estudianteForm.getIdCarrera() == null || estudianteForm.getIdCarrera() <= 0) {
			return error("Selecciona la carrera del estudiante.");
		}
		if (estudianteForm.getIdPlanEstudios() == null || estudianteForm.getIdPlanEstudios() <= 0) {
			return error("Selecciona el plan de estudios de la carrera.");
		}
		if (isBlank(estudianteForm.getId()) || isBlank(estudianteForm.getNombre())
				|| isBlank(estudianteForm.getApellido()) || isBlank(estudianteForm.getCorreo())
				|| isBlank(estudianteForm.getPassword()) || estudianteForm.getSemestreCursa() == null
				|| estudianteForm.getSemestreCursa() <= 0 || estudianteForm.getCreditosAprobados() == null
				|| estudianteForm.getCreditosAprobados() < 0 || estudianteForm.getEstadoEstudiante() == null) {
			return error("Completa todos los campos del estudiante.");
		}
		ApiResponse r = estudianteService.crear(estudianteForm);
		if (r.isOk()) {
			ok("Estudiante " + estudianteForm.getNombre() + " registrado.");
			estudianteForm = new EstudianteDTO();
			mostrarFormularioEstudiante = false;
			cargarEstudiantes();
		} else if (r.isConflict()) {
			error("Ya existe un estudiante con ese ID o correo.");
		} else {
			error("Error al registrar: " + r.getBody());
		}
		return null;
	}

	public String editarEstudiante(EstudianteDTO estudiante) {
		limpiarMensajes();
		estudianteForm = new EstudianteDTO(estudiante.getNombre(), estudiante.getApellido(), estudiante.getId(),
				estudiante.getCorreo(), estudiante.getSemestreCursa(), estudiante.getEstadoEstudiante(),
				estudiante.getCreditosAprobados());
		estudianteForm.setPassword("");
		editandoEstudiante = true;
		mostrarFormularioEstudiante = true;
		return null;
	}

	public String mostrarFormularioEstudiante() {
		estudianteForm = new EstudianteDTO();
		planesDisponibles = new ArrayList<>();
		cargarCarreras();
		editandoEstudiante = false;
		mostrarFormularioEstudiante = true;
		limpiarMensajes();
		return null;
	}

	public String guardarEstudiante() {
		if (!editandoEstudiante) {
			return crearEstudiante();
		}

		limpiarMensajes();
		if (isBlank(estudianteForm.getId()) || isBlank(estudianteForm.getNombre())
				|| isBlank(estudianteForm.getApellido()) || isBlank(estudianteForm.getCorreo())
				|| estudianteForm.getSemestreCursa() == null || estudianteForm.getSemestreCursa() <= 0
				|| estudianteForm.getCreditosAprobados() == null || estudianteForm.getCreditosAprobados() < 0
				|| estudianteForm.getEstadoEstudiante() == null) {
			return error("Completa todos los campos obligatorios del estudiante.");
		}

		ApiResponse r = estudianteService.actualizar(estudianteForm.getId(), estudianteForm);
		if (r.isOk()) {
			ok("Estudiante actualizado exitosamente.");
			estudianteForm = new EstudianteDTO();
			editandoEstudiante = false;
			mostrarFormularioEstudiante = false;
			cargarEstudiantes();
		} else if (r.isConflict()) {
			error("Ya existe un estudiante con ese correo.");
		} else if (r.isNotFound()) {
			error("No se encontró el estudiante que intentas actualizar.");
		} else {
			error("Error al actualizar el estudiante: " + r.getBody());
		}
		return null;
	}

	public String cancelarEdicionEstudiante() {
		estudianteForm = new EstudianteDTO();
		editandoEstudiante = false;
		mostrarFormularioEstudiante = false;
		limpiarMensajes();
		return null;
	}

	public String eliminarEstudiante(EstudianteDTO e) {
		limpiarMensajes();
		ApiResponse r = estudianteService.eliminar(e.getId());
		if (r.isOk()) {
			ok("Estudiante eliminado.");
			cargarEstudiantes();
		} else
			error("No se pudo eliminar: " + r.getBody());
		return null;
	}

	public List<String> getEscalafonesDisponibles() {
		Set<String> valores = new LinkedHashSet<>();
		for (DocenteDTO docente : docentes) {
			if (docente.getEscalafon() != null) {
				valores.add(docente.getEscalafon().name());
			}
		}
		return new ArrayList<>(valores);
	}

	public List<DocenteDTO> docentesPorEscalafon(String escalafon) {
		List<DocenteDTO> resultado = new ArrayList<>();
		for (DocenteDTO docente : docentes) {
			if (docente.getEscalafon() != null && docente.getEscalafon().name().equals(escalafon)) {
				resultado.add(docente);
			}
		}
		return resultado;
	}

	public String formatearEtiqueta(Enum<?> valor) {
		if (valor == null) {
			return "No disponible";
		}
		return formatearEtiqueta(valor.name());
	}

	public String formatearEtiqueta(String valor) {
		if (valor == null || valor.trim().isEmpty()) {
			return "No disponible";
		}
		String[] palabras = valor.toLowerCase().split("_");
		StringBuilder etiqueta = new StringBuilder();
		for (String palabra : palabras) {
			if (etiqueta.length() > 0) {
				etiqueta.append(' ');
			}
			etiqueta.append(Character.toUpperCase(palabra.charAt(0))).append(palabra.substring(1));
		}
		return etiqueta.toString();
	}

	// Helpers

	private void limpiarMensajes() {
		mensajeError = "";
		mensajeOk = "";
	}

	private String error(String m) {
		mensajeError = m;
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", m));
		return null;
	}

	private void ok(String m) {
		mensajeOk = m;
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", m));
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	// Getters / Setters

	public List<DocenteDTO> getDocentes() {
		return docentes;
	}

	public void setDocentes(List<DocenteDTO> v) {
		docentes = v;
	}

	public List<EstudianteDTO> getEstudiantes() {
		return estudiantes;
	}

	public void setEstudiantes(List<EstudianteDTO> v) {
		estudiantes = v;
	}

	public DocenteDTO getDocenteForm() {
		return docenteForm;
	}

	public void setDocenteForm(DocenteDTO v) {
		docenteForm = v;
	}

	public boolean isEditandoDocente() {
		return editandoDocente;
	}

	public boolean isMostrarFormularioDocente() {
		return mostrarFormularioDocente;
	}

	public EstudianteDTO getEstudianteForm() {
		return estudianteForm;
	}

	public void setEstudianteForm(EstudianteDTO v) {
		estudianteForm = v;
	}

	public List<CarreraDTO> getCarrerasDisponibles() {
		return carrerasDisponibles;
	}

	public List<PlanEstudiosDTO> getPlanesDisponibles() {
		return planesDisponibles;
	}

	public boolean isEditandoEstudiante() {
		return editandoEstudiante;
	}

	public boolean isMostrarFormularioEstudiante() {
		return mostrarFormularioEstudiante;
	}

	public String getMensajeError() {
		return mensajeError;
	}

	public void setMensajeError(String v) {
		mensajeError = v;
	}

	public String getMensajeOk() {
		return mensajeOk;
	}

	public void setMensajeOk(String v) {
		mensajeOk = v;
	}
}
