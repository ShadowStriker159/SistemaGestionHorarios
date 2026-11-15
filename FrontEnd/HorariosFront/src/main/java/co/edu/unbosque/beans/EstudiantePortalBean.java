package co.edu.unbosque.beans;

import co.edu.unbosque.model.CursoDTO;
import co.edu.unbosque.model.DiaSemana;
import co.edu.unbosque.model.EstadoGrupo;
import co.edu.unbosque.model.EstudianteDTO;
import co.edu.unbosque.model.GrupoDTO;
import co.edu.unbosque.model.UsuarioSesionDTO;
import co.edu.unbosque.service.ApiClient;
import co.edu.unbosque.service.ApiClient.ApiResponse;
import co.edu.unbosque.service.EstudianteService;
import co.edu.unbosque.service.FacultadService;
import co.edu.unbosque.service.GrupoService;
import com.google.gson.JsonParseException;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Named("estudiantePortalBean")
@ViewScoped
public class EstudiantePortalBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EstudianteService estudianteService;

	@Inject
	private FacultadService facultadService;

	@Inject
	private GrupoService grupoService;

	private String idEstudiante = "";
	private EstudianteDTO estudiante;
	private List<CursoDTO> cursosCarrera = new ArrayList<>();
	private List<GrupoDTO> gruposInscritos = new ArrayList<>();
	private String mensajeError = "";
	private String mensajeOk = "";

	@PostConstruct
	public void init() {
		Object usuario = jakarta.faces.context.FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.get("usuarioSesion");
		if (!(usuario instanceof UsuarioSesionDTO sesion) || sesion.getId() == null || sesion.getId().isBlank()) {
			mensajeError = "No se encontró una sesión válida de estudiante. Inicia sesión nuevamente.";
			return;
		}
		idEstudiante = sesion.getId();
		cargarDatos();
	}

	public void cargarDatos() {
		mensajeError = "";
		try {
			estudiante = estudianteService.obtener(idEstudiante);
			if (estudiante.getIdPlanEstudios() == null) {
				cursosCarrera = new ArrayList<>();
				gruposInscritos = new ArrayList<>();
				mensajeError = "Tu usuario no tiene un plan de estudios asignado. Comunícate con Secretaría.";
				return;
			}

			int idPlan = estudiante.getIdPlanEstudios();
			cursosCarrera = ApiClient.parseList(facultadService.listarCursosPorPlan(idPlan), CursoDTO.class);
			List<GrupoDTO> grupos = ApiClient.parseList(facultadService.listarGruposPorPlan(idPlan), GrupoDTO.class);

			Map<Integer, List<GrupoDTO>> porCurso = new HashMap<>();
			gruposInscritos = new ArrayList<>();
			for (GrupoDTO grupo : grupos) {
				if (grupo.getCurso() != null) {
					porCurso.computeIfAbsent(grupo.getCurso().getIdCurso(), k -> new ArrayList<>()).add(grupo);
				}
				if (estaInscrito(grupo)) {
					gruposInscritos.add(grupo);
				}
			}
			for (CursoDTO curso : cursosCarrera) {
				curso.setGrupos(porCurso.getOrDefault(curso.getIdCurso(), new ArrayList<>()));
			}
		} catch (ApiClient.ApiException e) {
			cursosCarrera = new ArrayList<>();
			gruposInscritos = new ArrayList<>();
			mensajeError = "No se pudo cargar tu plan de estudios: " + e.getMessage();
		} catch (IllegalStateException e) {
			cursosCarrera = new ArrayList<>();
			gruposInscritos = new ArrayList<>();
			mensajeError = e.getMessage();
		} catch (JsonParseException e) {
			cursosCarrera = new ArrayList<>();
			gruposInscritos = new ArrayList<>();
			mensajeError = "El backend devolvió datos que no se pudieron interpretar. Revisa sus DTO y vuelve a intentarlo.";
		}
	}

	public String inscribir(Integer idGrupo) {
		limpiarMensajes();
		if (idGrupo == null || idEstudiante.isBlank()) {
			return error("No se pudo identificar el grupo o el estudiante.");
		}
		GrupoDTO grupo = buscarGrupo(idGrupo);
		if (grupo == null) {
			return error("El grupo ya no está disponible en tu plan de estudios. Recarga la página.");
		}
		if (estaInscrito(grupo)) {
			return error("Ya estás inscrito en este grupo.");
		}
		if (cursoYaInscrito(grupo)) {
			return error("Ya estás inscrito en otro grupo de esta materia. Retírate de ese grupo antes de cambiar.");
		}
		ApiResponse respuesta = grupoService.inscribirEstudiante(idGrupo, idEstudiante);
		if (respuesta.isOk()) {
			mensajeOk = "Inscripción realizada.";
			cargarDatos();
		} else if (respuesta.isConflict()) {
			error(respuesta.getBody());
		} else if (respuesta.getCode() == 400) {
			error(respuesta.getBody());
		} else if (respuesta.isNotFound()) {
			error("No se encontró el grupo o el estudiante.");
		} else {
			error("No se pudo realizar la inscripción: " + respuesta.getBody());
		}
		return null;
	}

	public String retirar(Integer idGrupo) {
		limpiarMensajes();
		if (idGrupo == null || idEstudiante.isBlank()) {
			return error("No se pudo identificar el grupo o el estudiante.");
		}
		ApiResponse respuesta = grupoService.retirarEstudiante(idGrupo, idEstudiante);
		if (respuesta.isOk()) {
			mensajeOk = "Te retiraste del grupo.";
			cargarDatos();
		} else if (respuesta.isNotFound()) {
			error("No se encontró la inscripción que intentas retirar.");
		} else {
			error("No se pudo retirar del grupo: " + respuesta.getBody());
		}
		return null;
	}

	private GrupoDTO buscarGrupo(Integer idGrupo) {
		return cursosCarrera.stream().filter(curso -> curso.getGrupos() != null)
				.flatMap(curso -> curso.getGrupos().stream())
				.filter(grupo -> Objects.equals(grupo.getIdGrupo(), idGrupo)).findFirst().orElse(null);
	}

	private boolean estaInscrito(GrupoDTO grupo) {
		return grupo.getEstudiantes() != null
				&& grupo.getEstudiantes().stream().anyMatch(item -> Objects.equals(item.getId(), idEstudiante));
	}

	private boolean cursoYaInscrito(GrupoDTO seleccionado) {
		return cursosCarrera.stream()
				.filter(curso -> curso.getGrupos() != null && curso.getGrupos().contains(seleccionado))
				.anyMatch(curso -> curso.getGrupos().stream()
						.anyMatch(grupo -> !Objects.equals(grupo.getIdGrupo(), seleccionado.getIdGrupo())
								&& estaInscrito(grupo)));
	}

	public boolean grupoLleno(GrupoDTO grupo) {
		int inscritos = grupo.getEstudiantes() == null ? 0 : grupo.getEstudiantes().size();
		return grupo.getCupoMaximo() != null && inscritos >= grupo.getCupoMaximo();
	}

	public boolean grupoInscrito(GrupoDTO grupo) {
		return grupo != null && estaInscrito(grupo);
	}

	public boolean cursoTieneInscripcion(CursoDTO curso) {
		return curso != null && curso.getGrupos() != null && curso.getGrupos().stream().anyMatch(this::estaInscrito);
	}

	public boolean grupoDisponible(GrupoDTO grupo) {
		return grupo != null && grupo.getEstadoGrupo() == EstadoGrupo.ABIERTO && !grupoLleno(grupo);
	}

	public String etiquetaDia(DiaSemana dia) {
		if (dia == null) {
			return "Día no indicado";
		}
		return switch (dia.name()) {
		case "LUNES" -> "Lunes";
		case "MARTES" -> "Martes";
		case "MIERCOLES" -> "Miércoles";
		case "JUEVES" -> "Jueves";
		case "VIERNES" -> "Viernes";
		case "SABADO" -> "Sábado";
		default -> dia.name();
		};
	}

	public String formatoHora(LocalTime hora) {
		return hora == null ? "—" : hora.toString();
	}

	public String nombreDocente(GrupoDTO grupo) {
		if (grupo == null || grupo.getDocente() == null) {
			return "Por asignar";
		}
		String nombre = grupo.getDocente().getNombre() == null ? "" : grupo.getDocente().getNombre();
		String apellido = grupo.getDocente().getApellido() == null ? "" : grupo.getDocente().getApellido();
		String nombreCompleto = (nombre + " " + apellido).trim();
		return nombreCompleto.isEmpty() ? "Por asignar" : nombreCompleto;
	}

	public String getNombreCompleto() {
		return estudiante == null ? "Estudiante"
				: ((estudiante.getNombre() == null ? "" : estudiante.getNombre()) + " "
						+ (estudiante.getApellido() == null ? "" : estudiante.getApellido())).trim();
	}

	public String getIniciales() {
		if (estudiante == null) {
			return "E";
		}
		String nombre = estudiante.getNombre() == null ? "" : estudiante.getNombre().trim();
		String apellido = estudiante.getApellido() == null ? "" : estudiante.getApellido().trim();
		return (nombre.isEmpty() ? "" : nombre.substring(0, 1)) + (apellido.isEmpty() ? "" : apellido.substring(0, 1));
	}

	public int getCreditosInscritos() {
		return gruposInscritos.stream()
				.filter(grupo -> grupo.getCurso() != null && grupo.getCurso().getCreditos() != null)
				.mapToInt(grupo -> grupo.getCurso().getCreditos()).sum();
	}

	private String error(String mensaje) {
		mensajeError = mensaje;
		return null;
	}

	private void limpiarMensajes() {
		mensajeError = "";
		mensajeOk = "";
	}

	public String getIdEstudiante() {
		return idEstudiante;
	}

	public EstudianteDTO getEstudiante() {
		return estudiante;
	}

	public List<CursoDTO> getCursosCarrera() {
		return cursosCarrera;
	}

	public List<GrupoDTO> getGruposInscritos() {
		return gruposInscritos;
	}

	public String getMensajeError() {
		return mensajeError;
	}

	public String getMensajeOk() {
		return mensajeOk;
	}
}
