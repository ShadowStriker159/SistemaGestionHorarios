package co.edu.unbosque.service;

import co.edu.unbosque.model.AulaDTO;
import co.edu.unbosque.model.BloqueDTO;
import co.edu.unbosque.model.TipoAula;
import co.edu.unbosque.service.ApiClient.ApiResponse;
import com.google.gson.reflect.TypeToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class BloqueService {

	private static final String BASE = "/bloques";

	@Inject
	private ApiClient api;

	public String listarBloques() {
		return api.get(BASE);
	}

	public ApiResponse crearBloque(String nombre) {
		BloqueDTO dto = new BloqueDTO();
		dto.setNombreBloque(nombre);
		dto.setEtiquetaBloque(nombre);
		dto.setUbicacion("Universidad");
		return api.post(BASE, ApiClient.gson().toJson(dto));
	}

	public ApiResponse eliminarBloque(int id) {
		return api.delete(BASE + "/" + id);
	}

	public String listarAulas(int idBloque) {
		Type tipo = new TypeToken<List<AulaDTO>>() {
		}.getType();
		List<AulaDTO> aulas = Objects.requireNonNull(ApiClient.gson().fromJson(api.get("/aulas"), tipo),
				"El backend devolvió una lista de aulas nula");
		return ApiClient.gson()
				.toJson(aulas.stream().filter(
						aula -> aula.getBloque() != null && Objects.equals(aula.getBloque().getIdBloque(), idBloque))
						.toList());
	}

	public ApiResponse crearAula(int idBloque, String numeroAula, int capacidad, String tipoAula) {
		AulaDTO dto = new AulaDTO();
		dto.setNumeroAula(numeroAula);
		dto.setCapacidad(capacidad);
		dto.setTipoAula(TipoAula.valueOf(tipoAula));
		return api.post(BASE + "/" + idBloque + "/aulas", ApiClient.gson().toJson(dto));
	}

	public ApiResponse eliminarAula(int idBloque, String numeroAula) {
		String segmento = URLEncoder.encode(numeroAula, StandardCharsets.UTF_8).replace("+", "%20");
		return api.delete("/aulas/" + idBloque + "/" + segmento);
	}
}
