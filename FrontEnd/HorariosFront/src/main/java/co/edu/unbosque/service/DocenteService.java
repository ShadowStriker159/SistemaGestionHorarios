package co.edu.unbosque.service;

import co.edu.unbosque.model.DocenteDTO;
import co.edu.unbosque.model.GrupoDTO;
import co.edu.unbosque.model.FranjaHorariaDTO;
import co.edu.unbosque.service.ApiClient.ApiResponse;
import com.google.gson.reflect.TypeToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class DocenteService {

    @Inject
    private ApiClient api;

    private static final String BASE = "/docentes";

    public List<DocenteDTO> listarTodos() {
        String json = api.get(BASE);
        Type tipo = new TypeToken<List<DocenteDTO>>(){}.getType();
        return Objects.requireNonNull(
                ApiClient.gson().fromJson(json, tipo),
                "El backend devolvió una lista de docentes nula");
    }

    public DocenteDTO obtener(String id) {
        return ApiClient.gson().fromJson(api.get(BASE + "/" + id), DocenteDTO.class);
    }

    public List<GrupoDTO> listarGrupos(String id) {
        Type tipo = new TypeToken<List<GrupoDTO>>(){}.getType();
        return Objects.requireNonNull(
                ApiClient.gson().fromJson(api.get(BASE + "/" + id + "/grupos"), tipo),
                "El backend devolvió una lista de grupos nula");
    }

    public ApiResponse actualizarDisponibilidad(String id, List<FranjaHorariaDTO> disponibilidad) {
        return api.put(BASE + "/" + id + "/disponibilidad", ApiClient.gson().toJson(disponibilidad));
    }

    public ApiResponse crear(DocenteDTO dto) {
        String body = ApiClient.gson().toJson(dto);
        return api.post(BASE, body);
    }

    public ApiResponse actualizar(String id, DocenteDTO dto) {
        String body = ApiClient.gson().toJson(dto);
        return api.put(BASE + "/" + id, body);
    }

    public ApiResponse eliminar(String id) {
        return api.delete(BASE + "/" + id);
    }
}
