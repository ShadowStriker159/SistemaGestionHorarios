package co.edu.unbosque.beans;

import co.edu.unbosque.model.DocenteDTO;
import co.edu.unbosque.model.EstadoFranja;
import co.edu.unbosque.model.FranjaHorariaDTO;
import co.edu.unbosque.service.ApiClient.ApiResponse;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import co.edu.unbosque.service.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

@Named("academicoBean")
@ViewScoped
public class AcademicoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private FacultadService facultadService;

	@Inject
	private DocenteService docenteService;

	// JSONs devueltos por el backend
	private String facultadesJson = "[]";
	private String carrerasJson = "[]";
	private String planesJson = "[]";
	private String cursosJson = "[]";
	private String gruposJson = "[]";
	private List<SelectItem> docentesDisponibles = new ArrayList<>();
	private List<DocenteDTO> docentesCatalogo = new ArrayList<>();

	// Selección actual
	private int idFacultadSel = 0;
	private int idCarreraSel = 0;
	private int idPlanSel = 0;
	private int idPlanEliminarSel = 0;
	private int idCursoSel = 0;
	private int idGrupoSel = 0;
	private String facultadSeleccionada = "";
	private String carreraSeleccionada = "";
	private String planSeleccionado = "";
	private String cursoSeleccionado = "";

	// Formulario Facultad
	private String nombreFacultad = "";

	// Formulario Carrera
	private String nombreCarrera = "";

	// Formulario Plan
	private String nombrePlan = "";

	// Formulario Curso
	private String cuNombre = "";
	private String cuCreditos = "";
	private String cuIntensidad = "";
	private String cuSemestre = "";
	private String cuAulaRequerida = "GRUPAL";

	// Formulario Grupo
	private String idDocenteGrupo = "";
	private String cupoMinimoGrupo = "";
	private String cupoMaximoGrupo = "";

	// Mensajes
	private String mensajeError = "";
	private String mensajeOk = "";

	@PostConstruct
	public void init() {
		cargarFacultades();
		cargarDocentes();
	}

	// FACULTADES

	public void cargarFacultades() {
		facultadesJson = cargarJson(facultadService::listarFacultades);
	}

	public void cargarDocentes() {
		try {
			docentesDisponibles = new ArrayList<>();
			docentesCatalogo = docenteService.listarTodos();
			for (DocenteDTO docente : docentesCatalogo) {
				if (docente.getId() == null || docente.getId().isBlank()) {
					continue;
				}
				String nombre = ((docente.getNombre() == null ? "" : docente.getNombre()) + " "
						+ (docente.getApellido() == null ? "" : docente.getApellido())).trim();
				docentesDisponibles.add(new SelectItem(docente.getId(), nombre));
			}
		} catch (ApiClient.ApiException e) {
			docentesDisponibles = new ArrayList<>();
			docentesCatalogo = new ArrayList<>();
			error("No se pudieron cargar los docentes: " + e.getMessage());
		}
	}

	public String crearFacultad() {
		limpiarMensajes();
		if (isBlank(nombreFacultad))
			return error("Ingresa el nombre de la facultad.");
		ApiResponse r = facultadService.crearFacultad(nombreFacultad);
		if (r.isOk()) {
			ok("Facultad creada.");
			nombreFacultad = "";
			cargarFacultades();
		} else if (r.isConflict())
			error("Ya existe una facultad con ese nombre.");
		else
			error("Error: " + r.getBody());
		return null;
	}

	public String eliminarFacultad(int id) {
		limpiarMensajes();
		ApiResponse r = facultadService.eliminarFacultad(id);
		if (r.isOk()) {
			ok("Facultad eliminada.");
			if (idFacultadSel == id) {
				idFacultadSel = 0;
				facultadSeleccionada = "";
				carreraSeleccionada = "";
				planSeleccionado = "";
				cursoSeleccionado = "";
				idCarreraSel = 0;
				idPlanSel = 0;
				idPlanEliminarSel = 0;
				idCursoSel = 0;
				carrerasJson = "[]";
				planesJson = "[]";
				cursosJson = "[]";
				gruposJson = "[]";
			}
			cargarFacultades();
		} else
			error("No se puede eliminar. Puede tener carreras asociadas.");
		return null;
	}

	public String seleccionarFacultad(int id) {
		idFacultadSel = id;
		carrerasJson = cargarJson(() -> facultadService.listarCarreras(id));
		carreraSeleccionada = "";
		planSeleccionado = "";
		cursoSeleccionado = "";
		idCarreraSel = 0;
		planesJson = "[]";
		idPlanSel = 0;
		cursosJson = "[]";
		idCursoSel = 0;
		gruposJson = "[]";
		return null;
	}

	// CARRERAS

	public String crearCarrera() {
		limpiarMensajes();
		if (idFacultadSel == 0)
			return error("Selecciona primero una facultad.");
		if (isBlank(nombreCarrera))
			return error("Ingresa el nombre de la carrera.");
		ApiResponse r = facultadService.crearCarrera(idFacultadSel, nombreCarrera);
		if (r.isOk()) {
			ok("Carrera creada.");
			nombreCarrera = "";
			carrerasJson = cargarJson(() -> facultadService.listarCarreras(idFacultadSel));
		} else
			error("Error al crear carrera: " + r.getBody());
		return null;
	}

	public String eliminarCarrera(int id) {
		limpiarMensajes();
		ApiResponse r = facultadService.eliminarCarrera(id);
		if (r.isOk()) {
			ok("Carrera eliminada.");
			if (idCarreraSel == id) {
				idCarreraSel = 0;
				carreraSeleccionada = "";
				planSeleccionado = "";
				cursoSeleccionado = "";
				idPlanSel = 0;
				idPlanEliminarSel = 0;
				idCursoSel = 0;
				planesJson = "[]";
				cursosJson = "[]";
				gruposJson = "[]";
			}
			carrerasJson = cargarJson(() -> facultadService.listarCarreras(idFacultadSel));
		} else
			error("No se puede eliminar. Puede tener planes asociados.");
		return null;
	}

	public String seleccionarCarrera(int id) {
		idCarreraSel = id;
		planesJson = cargarJson(() -> facultadService.listarPlanes(id));
		planSeleccionado = "";
		cursoSeleccionado = "";
		idPlanSel = 0;
		cursosJson = "[]";
		idCursoSel = 0;
		gruposJson = "[]";
		return null;
	}

	// PLANES DE ESTUDIO
	public String crearPlan() {
		limpiarMensajes();
		if (idCarreraSel == 0)
			return error("Selecciona primero una carrera.");
		if (isBlank(nombrePlan))
			return error("Ingresa el nombre del plan.");
		ApiResponse r = facultadService.crearPlan(idCarreraSel, nombrePlan);
		if (r.isOk()) {
			ok("Plan de estudios creado.");
			nombrePlan = "";
			planesJson = cargarJson(() -> facultadService.listarPlanes(idCarreraSel));
		} else
			error("Error al crear plan: " + r.getBody());
		return null;
	}

	public String eliminarPlan(int id) {
		limpiarMensajes();
		ApiResponse r = facultadService.eliminarPlan(id);
		if (r.isOk()) {
			ok("Plan eliminado.");
			if (idPlanSel == id) {
				idPlanSel = 0;
				idPlanEliminarSel = 0;
				idCursoSel = 0;
				planSeleccionado = "";
				cursoSeleccionado = "";
				cursosJson = "[]";
				gruposJson = "[]";
			}
			planesJson = cargarJson(() -> facultadService.listarPlanes(idCarreraSel));
		} else
			error("No se puede eliminar. Puede tener cursos asociados.");
		return null;
	}

	public String seleccionarPlan(int id) {
		idPlanSel = id;
		cursosJson = cargarJson(() -> facultadService.listarCursosPorPlan(id));
		cursoSeleccionado = "";
		idCursoSel = 0;
		gruposJson = "[]";
		return null;
	}

	// CURSOS

	public String crearCurso() {
		limpiarMensajes();
		if (idPlanSel == 0)
			return error("Selecciona primero un plan de estudios.");
		if (isBlank(cuNombre))
			return error("Ingresa el nombre del curso.");
		Integer creditos = numeroPositivo(cuCreditos, "créditos");
		if (creditos == null)
			return null;
		Integer intensidad = numeroPositivo(cuIntensidad, "intensidad horaria");
		if (intensidad == null)
			return null;
		Integer semestre = numeroPositivo(cuSemestre, "semestre");
		if (semestre == null)
			return null;
		ApiResponse r = facultadService.crearCurso(idPlanSel, cuNombre.trim(), creditos, intensidad, semestre,
				cuAulaRequerida);
		if (r.isOk()) {
			ok("Curso creado.");
			cuNombre = "";
			cuCreditos = "";
			cuIntensidad = "";
			cuSemestre = "";
			cursosJson = cargarJson(() -> facultadService.listarCursosPorPlan(idPlanSel));
		} else
			error("Error al crear curso: " + r.getBody());
		return null;
	}

	public String eliminarCurso(int id) {
		limpiarMensajes();
		ApiResponse r = facultadService.eliminarCurso(id);
		if (r.isOk()) {
			ok("Curso eliminado.");
			if (idCursoSel == id) {
				idCursoSel = 0;
				cursoSeleccionado = "";
				gruposJson = "[]";
			}
			cursosJson = cargarJson(() -> facultadService.listarCursosPorPlan(idPlanSel));
		} else
			error("No se puede eliminar. Puede tener grupos asociados.");
		return null;
	}

	public String seleccionarCurso(int id) {
		idCursoSel = id;
		gruposJson = cargarJson(() -> facultadService.listarGruposPorCurso(id));
		return null;
	}

	// GRUPOS

	public String crearGrupo() {
		limpiarMensajes();
		if (idCursoSel == 0)
			return error("Selecciona primero un curso.");
		if (isBlank(idDocenteGrupo))
			return error("Selecciona un docente.");
		if (franjasDisponiblesDocente(idDocenteGrupo).isEmpty()) {
			return error("El docente seleccionado no ha registrado franjas disponibles activas. "
					+ "Solicítale registrar su disponibilidad antes de asignarle un grupo.");
		}
		Integer cupoMinimo = numeroPositivo(cupoMinimoGrupo, "cupo mínimo");
		if (cupoMinimo == null)
			return null;
		Integer cupoMaximo = numeroPositivo(cupoMaximoGrupo, "cupo máximo");
		if (cupoMaximo == null)
			return null;
		if (cupoMaximo < cupoMinimo)
			return error("El cupo máximo no puede ser menor al cupo mínimo.");
		ApiResponse r = facultadService.crearGrupo(idCursoSel, idDocenteGrupo, cupoMinimo, cupoMaximo);
		if (r.isOk()) {
			ok("Grupo creado.");
			cupoMinimoGrupo = "";
			cupoMaximoGrupo = "";
			idDocenteGrupo = "";
			gruposJson = cargarJson(() -> facultadService.listarGruposPorCurso(idCursoSel));
		} else
			error("Error al crear grupo: " + r.getBody());
		return null;
	}

	public String eliminarGrupo(int id) {
		limpiarMensajes();
		ApiResponse r = facultadService.eliminarGrupo(id);
		if (r.isOk()) {
			ok("Grupo eliminado.");
			gruposJson = cargarJson(() -> facultadService.listarGruposPorCurso(idCursoSel));
		} else
			error("No se puede eliminar.");
		return null;
	}

	// Helpers

	private void limpiarMensajes() {
		mensajeError = "";
		mensajeOk = "";
	}

	private String cargarJson(Supplier<String> consulta) {
		try {
			return consulta.get();
		} catch (ApiClient.ApiException e) {
			error("No se pudieron cargar los datos: " + e.getMessage());
			return "[]";
		}
	}

	private Integer numeroPositivo(String valor, String campo) {
		try {
			int numero = Integer.parseInt(valor);
			if (numero <= 0) {
				error("El valor de " + campo + " debe ser mayor que cero.");
				return null;
			}
			return numero;
		} catch (NumberFormatException e) {
			error("Ingresa un valor numérico válido para " + campo + ".");
			return null;
		}
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

	private List<FranjaHorariaDTO> franjasDisponiblesDocente(String idDocente) {
		return docentesCatalogo.stream().filter(docente -> idDocente.equals(docente.getId())).findFirst()
				.map(DocenteDTO::getDisponibilidad).orElseGet(ArrayList::new).stream()
				.filter(franja -> franja.getEstadoFranja() == EstadoFranja.DISPONIBLE).toList();
	}

	public String getDisponibilidadDocenteSeleccionado() {
		if (isBlank(idDocenteGrupo)) {
			return "Selecciona un docente para consultar su disponibilidad.";
		}
		List<FranjaHorariaDTO> franjas = franjasDisponiblesDocente(idDocenteGrupo);
		if (franjas.isEmpty()) {
			return "Este docente no tiene franjas disponibles activas. Debe registrar su disponibilidad antes de asignarle grupos.";
		}
		String detalle = franjas.stream()
				.map(franja -> etiquetaDia(franja.getDiaSemana()) + " "
						+ (franja.getHoraInicio() == null ? "—" : franja.getHoraInicio()) + "–"
						+ (franja.getHoraFin() == null ? "—" : franja.getHoraFin()))
				.collect(java.util.stream.Collectors.joining(", "));
		return "Disponibilidad del docente: " + detalle + ". El generador limitará el horario a estas franjas.";
	}

	private String etiquetaDia(Enum<?> dia) {
		if (dia == null) {
			return "Día no indicado";
		}
		String texto = dia.name().toLowerCase(Locale.ROOT);
		return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
	}

	// Getters / Setters
	public String getFacultadesJson() {
		return facultadesJson;
	}

	public String getCarrerasJson() {
		return carrerasJson;
	}

	public String getPlanesJson() {
		return planesJson;
	}

	public String getCursosJson() {
		return cursosJson;
	}

	public String getGruposJson() {
		return gruposJson;
	}

	public List<SelectItem> getDocentesDisponibles() {
		return docentesDisponibles;
	}

	public int getIdFacultadSel() {
		return idFacultadSel;
	}

	public void setIdFacultadSel(int v) {
		idFacultadSel = v;
	}

	public int getIdCarreraSel() {
		return idCarreraSel;
	}

	public void setIdCarreraSel(int v) {
		idCarreraSel = v;
	}

	public int getIdPlanSel() {
		return idPlanSel;
	}

	public void setIdPlanSel(int v) {
		idPlanSel = v;
	}

	public int getIdPlanEliminarSel() {
		return idPlanEliminarSel;
	}

	public void setIdPlanEliminarSel(int v) {
		idPlanEliminarSel = v;
	}

	public int getIdCursoSel() {
		return idCursoSel;
	}

	public void setIdCursoSel(int v) {
		idCursoSel = v;
	}

	public int getIdGrupoSel() {
		return idGrupoSel;
	}

	public void setIdGrupoSel(int v) {
		idGrupoSel = v;
	}

	public String getFacultadSeleccionada() {
		return facultadSeleccionada;
	}

	public void setFacultadSeleccionada(String v) {
		facultadSeleccionada = v;
	}

	public String getCarreraSeleccionada() {
		return carreraSeleccionada;
	}

	public void setCarreraSeleccionada(String v) {
		carreraSeleccionada = v;
	}

	public String getPlanSeleccionado() {
		return planSeleccionado;
	}

	public void setPlanSeleccionado(String v) {
		planSeleccionado = v;
	}

	public String getCursoSeleccionado() {
		return cursoSeleccionado;
	}

	public void setCursoSeleccionado(String v) {
		cursoSeleccionado = v;
	}

	public String getNombreFacultad() {
		return nombreFacultad;
	}

	public void setNombreFacultad(String v) {
		nombreFacultad = v;
	}

	public String getNombreCarrera() {
		return nombreCarrera;
	}

	public void setNombreCarrera(String v) {
		nombreCarrera = v;
	}

	public String getNombrePlan() {
		return nombrePlan;
	}

	public void setNombrePlan(String v) {
		nombrePlan = v;
	}

	public String getCuNombre() {
		return cuNombre;
	}

	public void setCuNombre(String v) {
		cuNombre = v;
	}

	public String getCuCreditos() {
		return cuCreditos;
	}

	public void setCuCreditos(String v) {
		cuCreditos = v;
	}

	public String getCuIntensidad() {
		return cuIntensidad;
	}

	public void setCuIntensidad(String v) {
		cuIntensidad = v;
	}

	public String getCuSemestre() {
		return cuSemestre;
	}

	public void setCuSemestre(String v) {
		cuSemestre = v;
	}

	public String getCuAulaRequerida() {
		return cuAulaRequerida;
	}

	public void setCuAulaRequerida(String v) {
		cuAulaRequerida = v;
	}

	public String getIdDocenteGrupo() {
		return idDocenteGrupo;
	}

	public void setIdDocenteGrupo(String v) {
		idDocenteGrupo = v;
	}

	public String getCupoMinimoGrupo() {
		return cupoMinimoGrupo;
	}

	public void setCupoMinimoGrupo(String v) {
		cupoMinimoGrupo = v;
	}

	public String getCupoMaximoGrupo() {
		return cupoMaximoGrupo;
	}

	public void setCupoMaximoGrupo(String v) {
		cupoMaximoGrupo = v;
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
