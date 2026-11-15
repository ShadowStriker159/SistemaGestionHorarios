package co.edu.unbosque.service;

import co.edu.unbosque.service.ApiClient.ApiResponse;

/**
 * FranjaService
 * Conecta con:
 *   GET    /api/horarios/franjas
 *   POST   /api/horarios/franjas   body: { horaInicio, horaFin, dia }
 *   DELETE /api/horarios/franjas/{id}
 */
public class FranjaService {

    private static final String BASE = "/franjas";

    public String listarTodas() {
        return ApiClient.get(BASE);
    }

    public ApiResponse crear(String horaInicio, String horaFin, String dia) {
        String body = "{"
            + "\"horaInicio\":\"" + ApiClient.esc(horaInicio) + "\","
            + "\"horaFin\":\""    + ApiClient.esc(horaFin)    + "\","
            + "\"dia\":\""        + ApiClient.esc(dia)        + "\""
            + "}";
        return ApiClient.post(BASE, body);
    }

    public ApiResponse eliminar(int id) {
        return ApiClient.delete(BASE + "/" + id);
    }
}
