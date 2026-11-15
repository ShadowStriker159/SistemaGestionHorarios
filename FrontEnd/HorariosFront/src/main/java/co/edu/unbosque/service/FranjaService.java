package co.edu.unbosque.service;

import co.edu.unbosque.model.Categoria;
import co.edu.unbosque.model.DiaSemana;
import co.edu.unbosque.model.FranjaHorariaDTO;
import co.edu.unbosque.service.ApiClient.ApiResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalTime;

@ApplicationScoped
public class FranjaService {

	private static final String BASE = "/franjas";

	@Inject
	private ApiClient api;

	public String listarTodas() {
		return api.get(BASE);
	}

	public ApiResponse crear(String horaInicio, String horaFin, String diaSemana, String categoria) {
		FranjaHorariaDTO dto = new FranjaHorariaDTO();
		dto.setHoraInicio(LocalTime.parse(horaInicio));
		dto.setHoraFin(LocalTime.parse(horaFin));
		dto.setDiaSemana(DiaSemana.valueOf(diaSemana));
		dto.setCategoria(Categoria.valueOf(categoria));
		return api.post(BASE, ApiClient.gson().toJson(dto));
	}

	public ApiResponse eliminar(int id) {
		return api.delete(BASE + "/" + id);
	}
}
