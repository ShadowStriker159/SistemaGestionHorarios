package co.edu.unbosque.service;

import co.edu.unbosque.model.HorarioDTO;
import co.edu.unbosque.service.ApiClient.ApiResponse;
import com.google.gson.reflect.TypeToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class HorarioService {

	@Inject
	private ApiClient api;

	public List<HorarioDTO> listarHorarios() {
		Type tipo = new TypeToken<List<HorarioDTO>>() {
		}.getType();
		return Objects.requireNonNull(ApiClient.gson().fromJson(api.get("/listar"), tipo),
				"El backend devolvió una lista de horarios nula");
	}

	public ApiResponse generarHorario(int idFacultad) {
		return api.post("/generar?idFacultad=" + idFacultad, "{}");
	}
}
