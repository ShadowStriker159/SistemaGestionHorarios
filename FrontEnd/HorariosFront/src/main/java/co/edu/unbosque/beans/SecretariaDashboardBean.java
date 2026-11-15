package co.edu.unbosque.beans;

import co.edu.unbosque.model.CarreraDTO;
import co.edu.unbosque.model.CursoDTO;
import co.edu.unbosque.model.DocenteDTO;
import co.edu.unbosque.model.EstadoDocente;
import co.edu.unbosque.model.EstadoEstudiante;
import co.edu.unbosque.model.FacultadDTO;
import co.edu.unbosque.model.GrupoDTO;
import co.edu.unbosque.model.PlanEstudiosDTO;
import co.edu.unbosque.model.EstudianteDTO;
import co.edu.unbosque.service.ApiClient;
import co.edu.unbosque.service.DocenteService;
import co.edu.unbosque.service.EstudianteService;
import co.edu.unbosque.service.FacultadService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.reflect.TypeToken;

@Named("dashboardBean")
@ViewScoped
public class SecretariaDashboardBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final int MAX_ITEMS = 5;

	@Inject
	private EstudianteService estudianteService;
	@Inject
	private DocenteService docenteService;
	@Inject
	private FacultadService facultadService;

	private List<EstudianteDTO> estudiantes = new ArrayList<>();
	private List<DocenteDTO> docentes = new ArrayList<>();
	private List<CursoResumen> cursos = new ArrayList<>();
	private final Map<String, Integer> gruposPorDocente = new HashMap<>();
	private int gruposTotales;
	private boolean estudiantesDisponibles;
	private boolean docentesDisponibles;
	private boolean academiaDisponible;

	@PostConstruct
	public void init() {
		cargarDatos();
	}

	public void cargarDatos() {
		estudiantes = new ArrayList<>();
		docentes = new ArrayList<>();
		cursos = new ArrayList<>();
		gruposPorDocente.clear();
		gruposTotales = 0;
		estudiantesDisponibles = false;
		docentesDisponibles = false;
		academiaDisponible = false;

		try {
			estudiantes = estudianteService.listarTodos();
			estudiantesDisponibles = true;
		} catch (ApiClient.ApiException e) {
			mostrarError("No se pudieron cargar los estudiantes: " + e.getMessage());
		}

		try {
			docentes = docenteService.listarTodos();
			docentesDisponibles = true;
		} catch (ApiClient.ApiException e) {
			mostrarError("No se pudieron cargar los docentes: " + e.getMessage());
		}

		try {
			cargarOfertaAcademica();
			academiaDisponible = true;
		} catch (ApiClient.ApiException e) {
			cursos = new ArrayList<>();
			gruposPorDocente.clear();
			gruposTotales = 0;
			mostrarError("No se pudieron cargar los cursos y grupos: " + e.getMessage());
		}
	}

	public String recargar() {
		cargarDatos();
		return null;
	}

	private void cargarOfertaAcademica() {
		List<FacultadDTO> facultades = parsear(facultadService.listarFacultades(), new TypeToken<List<FacultadDTO>>() {
		}.getType());

		for (FacultadDTO facultad : facultades) {
			List<CarreraDTO> carreras = parsear(facultadService.listarCarreras(facultad.getIdFacultad()),
					new TypeToken<List<CarreraDTO>>() {
					}.getType());
			for (CarreraDTO carrera : carreras) {
				List<PlanEstudiosDTO> planes = parsear(facultadService.listarPlanes(carrera.getIdCarrera()),
						new TypeToken<List<PlanEstudiosDTO>>() {
						}.getType());
				for (PlanEstudiosDTO plan : planes) {
					List<CursoDTO> cursosPlan = parsear(facultadService.listarCursosPorPlan(plan.getIdPlanEstudios()),
							new TypeToken<List<CursoDTO>>() {
							}.getType());
					for (CursoDTO curso : cursosPlan) {
						List<GrupoDTO> grupos = parsear(facultadService.listarGruposPorCurso(curso.getIdCurso()),
								new TypeToken<List<GrupoDTO>>() {
								}.getType());
						cursos.add(new CursoResumen(curso.getNombreCurso(), carrera.getNombreCarrera(), grupos.size()));
						gruposTotales += grupos.size();
						for (GrupoDTO grupo : grupos) {
							if (grupo.getDocente() != null && grupo.getDocente().getId() != null) {
								gruposPorDocente.merge(grupo.getDocente().getId(), 1, Integer::sum);
							}
						}
					}
				}
			}
		}
	}

	private <T> List<T> parsear(String json, Type tipo) {
		return Objects.requireNonNull(ApiClient.gson().fromJson(json, tipo),
				"El backend devolvió una lista académica nula");
	}

	private void mostrarError(String mensaje) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", mensaje));
	}

	public int getEstudiantesActivos() {
		return (int) estudiantes.stream()
				.filter(estudiante -> estudiante.getEstadoEstudiante() == EstadoEstudiante.ACTIVO).count();
	}

	public int getDocentesActivos() {
		return (int) docentes.stream().filter(docente -> docente.getEstadoDocente() == EstadoDocente.ACTIVO).count();
	}

	public int gruposAsignados(String idDocente) {
		return gruposPorDocente.getOrDefault(idDocente, 0);
	}

	public List<CursoResumen> getCursosDestacados() {
		return new ArrayList<>(cursos.subList(0, Math.min(MAX_ITEMS, cursos.size())));
	}

	public List<DocenteDTO> getDocentesDestacados() {
		return new ArrayList<>(docentes.subList(0, Math.min(MAX_ITEMS, docentes.size())));
	}

	public int getTotalEstudiantes() {
		return estudiantes.size();
	}

	public int getTotalDocentes() {
		return docentes.size();
	}

	public int getTotalCursos() {
		return cursos.size();
	}

	public int getGruposTotales() {
		return gruposTotales;
	}

	public boolean isEstudiantesDisponibles() {
		return estudiantesDisponibles;
	}

	public boolean isDocentesDisponibles() {
		return docentesDisponibles;
	}

	public boolean isAcademiaDisponible() {
		return academiaDisponible;
	}

	public static class CursoResumen implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String nombreCurso;
		private final String carrera;
		private final int cantidadGrupos;

		public CursoResumen(String nombreCurso, String carrera, int cantidadGrupos) {
			this.nombreCurso = nombreCurso;
			this.carrera = carrera;
			this.cantidadGrupos = cantidadGrupos;
		}

		public String getNombreCurso() {
			return nombreCurso;
		}

		public String getCarrera() {
			return carrera;
		}

		public int getCantidadGrupos() {
			return cantidadGrupos;
		}
	}
}
