package co.edu.unbosque.service;

import co.edu.unbosque.model.AreaDTO;
import co.edu.unbosque.model.CarreraDTO;
import co.edu.unbosque.model.CursoDTO;
import co.edu.unbosque.model.FacultadDTO;
import co.edu.unbosque.model.GrupoDTO;
import co.edu.unbosque.model.PlanEstudiosDTO;
import co.edu.unbosque.model.TipoAula;
import co.edu.unbosque.service.ApiClient.ApiResponse;
import com.google.gson.reflect.TypeToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class FacultadService {

	private static final String BASE = "/facultades";

	@Inject
	private ApiClient api;

	// Facultades

	public String listarFacultades() {
		return api.get(BASE);
	}

	public ApiResponse crearFacultad(String nombre) {
		FacultadDTO dto = new FacultadDTO();
		dto.setNombreFacultad(nombre);
		return api.post(BASE, ApiClient.gson().toJson(dto));
	}

	public ApiResponse eliminarFacultad(int id) {
		return api.delete(BASE + "/" + id);
	}

	// Áreas de una facultad

	public String listarAreas(int idFacultad) {
		return api.get(BASE + "/" + idFacultad + "/areas");
	}

	public ApiResponse crearArea(int idFacultad, String nombre) {
		AreaDTO dto = new AreaDTO();
		dto.setNombreArea(nombre);
		return api.post(BASE + "/" + idFacultad + "/areas", ApiClient.gson().toJson(dto));
	}

	// Carreras de una facultad

	public String listarCarreras(int idFacultad) {
		return api.get(BASE + "/" + idFacultad + "/carreras");
	}

	public List<CarreraDTO> listarTodasLasCarreras() {
		Type tipo = new TypeToken<List<CarreraDTO>>() {
		}.getType();
		return Objects.requireNonNull(ApiClient.gson().fromJson(api.get("/carreras"), tipo),
				"El backend devolvió una lista de carreras nula");
	}

	public ApiResponse crearCarrera(int idFacultad, String nombre) {
		CarreraDTO dto = new CarreraDTO();
		dto.setNombreCarrera(nombre);
		return api.post(BASE + "/" + idFacultad + "/carreras", ApiClient.gson().toJson(dto));
	}

	// Planes de estudio de una carrera 

	public String listarPlanes(int idCarrera) {
		return api.get("/carreras/" + idCarrera + "/planes");
	}

	public ApiResponse crearPlan(int idCarrera, String nombre) {
		PlanEstudiosDTO dto = new PlanEstudiosDTO();
		dto.setNombrePlanEstudios(nombre);
		return api.post("/carreras/" + idCarrera + "/planes", ApiClient.gson().toJson(dto));
	}

	public ApiResponse eliminarCarrera(int id) {
		return api.delete("/carreras/" + id);
	}

	public ApiResponse eliminarArea(int id) {
		return api.delete("/areas/" + id);
	}

	public ApiResponse eliminarPlan(int id) {
		return api.delete("/planes/" + id);
	}

	// ---- Cursos de un plan ----

	public String listarCursosPorPlan(int idPlan) {
		return api.get("/planes/" + idPlan + "/cursos");
	}

	public ApiResponse crearCurso(int idPlan, String nombre, int creditos, int intensidadHoraria, int semestre,
			String aulaRequerida) {
		CursoDTO dto = new CursoDTO();
		dto.setNombreCurso(nombre);
		dto.setCreditos(creditos);
		dto.setIntensidadHoraria(intensidadHoraria);
		dto.setSemestre(semestre);
		dto.setAulaRequerida(TipoAula.valueOf(aulaRequerida));
		return api.post("/planes/" + idPlan + "/cursos", ApiClient.gson().toJson(dto));
	}

	public ApiResponse eliminarCurso(int id) {
		return api.delete("/cursos/" + id);
	}

	//  Grupos de un curso 
	public String listarGruposPorCurso(int idCurso) {
		return api.get("/cursos/" + idCurso + "/grupos");
	}

	public ApiResponse crearGrupo(int idCurso, String idDocente, int cupoMinimo, int cupoMaximo) {
		GrupoDTO dto = new GrupoDTO();
		dto.setCupoMinimo(cupoMinimo);
		dto.setCupoMaximo(cupoMaximo);
		String queryId = java.net.URLEncoder.encode(idDocente, java.nio.charset.StandardCharsets.UTF_8);
		return api.post("/cursos/" + idCurso + "/grupos?idDocente=" + queryId, ApiClient.gson().toJson(dto));
	}

	public ApiResponse eliminarGrupo(int id) {
		return api.delete("/grupos/" + id);
	}
}
