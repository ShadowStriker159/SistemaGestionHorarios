package co.edu.unbosque.service;

import co.edu.unbosque.service.ApiClient.ApiResponse;

/**
 * DocenteService
 * Conecta con:
 *   GET    /api/horarios/docentes
 *   POST   /api/horarios/docentes    body: { id, nombre, apellido, correo, password, ... }
 *   DELETE /api/horarios/docentes/{id}
 */
public class DocenteService {

    private static final String BASE = "/docentes";

    public String listarTodos() {
        return ApiClient.get(BASE);
    }

    public ApiResponse crear(String id, String nombre, String apellido,
                             String correo, String password,
                             String escalafon, String tipoVinculacion) {
        String body = "{"
            + "\"id\":\""              + ApiClient.esc(id)             + "\","
            + "\"nombre\":\""          + ApiClient.esc(nombre)         + "\","
            + "\"apellido\":\""        + ApiClient.esc(apellido)       + "\","
            + "\"correo\":\""          + ApiClient.esc(correo)         + "\","
            + "\"password\":\""        + ApiClient.esc(password)       + "\","
            + "\"escalafon\":\""       + ApiClient.esc(escalafon)      + "\","
            + "\"tipoVinculacion\":\"" + ApiClient.esc(tipoVinculacion) + "\""
            + "}";
        return ApiClient.post(BASE, body);
    }

    public ApiResponse eliminar(String id) {
        return ApiClient.delete(BASE + "/" + ApiClient.esc(id));
    }
}
