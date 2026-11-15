package co.edu.unbosque.service;

import co.edu.unbosque.service.ApiClient.ApiResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GrupoService {

	@Inject
	private ApiClient api;

	public ApiResponse inscribirEstudiante(Integer idGrupo, String idEstudiante) {
		return api.post("/grupos/" + idGrupo + "/estudiantes/" + idEstudiante, null);
	}

	public ApiResponse retirarEstudiante(Integer idGrupo, String idEstudiante) {
		return api.delete("/grupos/" + idGrupo + "/estudiantes/" + idEstudiante);
	}
}
