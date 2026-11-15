package co.edu.unbosque.service;

import co.edu.unbosque.model.EstudianteDTO;
import co.edu.unbosque.service.ApiClient.ApiResponse;
import com.google.gson.reflect.TypeToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class EstudianteService {

	@Inject
	private ApiClient api;

	private static final String BASE = "/estudiantes";

	public List<EstudianteDTO> listarTodos() {
		String json = api.get(BASE);
		Type tipo = new TypeToken<List<EstudianteDTO>>() {
		}.getType();
		return Objects.requireNonNull(ApiClient.gson().fromJson(json, tipo),
				"El backend devolvió una lista de estudiantes nula");
	}

	public EstudianteDTO obtener(String id) {
		EstudianteDTO estudiante = ApiClient.gson().fromJson(api.get(BASE + "/" + id), EstudianteDTO.class);
		if (estudiante == null) {
			throw new IllegalStateException("El backend devolvió un estudiante nulo.");
		}
		return estudiante;
	}

	public ApiResponse crear(EstudianteDTO dto) {
		String body = ApiClient.gson().toJson(dto);
		return api.post(BASE, body);
	}

	public ApiResponse actualizar(String id, EstudianteDTO dto) {
		String body = ApiClient.gson().toJson(dto);
		return api.put(BASE + "/" + id, body);
	}

	public ApiResponse eliminar(String id) {
		return api.delete(BASE + "/" + id);
	}
}
