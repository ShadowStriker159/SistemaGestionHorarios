package co.edu.unbosque.service;

import co.edu.unbosque.service.ApiClient.ApiResponse;

/**
 * HorarioService
 * Conecta con:
 *   GET  /api/horarios/listar
 *   POST /api/horarios/generar?idFacultad={id}
 */
public class HorarioService {

    public String listarHorarios() {
        return ApiClient.get("/listar");
    }

    public ApiResponse generarHorario(int idFacultad) {
        return ApiClient.post("/generar?idFacultad=" + idFacultad, "{}");
    }
}
