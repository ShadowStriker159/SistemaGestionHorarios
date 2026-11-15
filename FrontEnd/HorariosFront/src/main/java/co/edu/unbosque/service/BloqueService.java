package co.edu.unbosque.service;

import co.edu.unbosque.service.ApiClient.ApiResponse;

/**
 * BloqueService
 * Conecta con:
 *   GET    /api/horarios/bloques
 *   POST   /api/horarios/bloques              body: { nombre }
 *   DELETE /api/horarios/bloques/{id}
 *   GET    /api/horarios/bloques/{id}/aulas
 *   POST   /api/horarios/bloques/{id}/aulas   body: { nombre, capacidad, tipoAula }
 *   DELETE /api/horarios/aulas/{id}
 */
public class BloqueService {

    private static final String BASE = "/bloques";

    public String listarBloques() {
        return ApiClient.get(BASE);
    }

    public ApiResponse crearBloque(String nombre) {
        String body = "{\"nombre\":\"" + ApiClient.esc(nombre) + "\"}";
        return ApiClient.post(BASE, body);
    }

    public ApiResponse eliminarBloque(int id) {
        return ApiClient.delete(BASE + "/" + id);
    }

    public String listarAulas(int idBloque) {
        return ApiClient.get(BASE + "/" + idBloque + "/aulas");
    }

    public ApiResponse crearAula(int idBloque, String nombre, String capacidad, String tipoAula) {
        String body = "{"
            + "\"nombre\":\""    + ApiClient.esc(nombre)    + "\","
            + "\"capacidad\":"   + (capacidad.isEmpty() ? "0" : ApiClient.esc(capacidad)) + ","
            + "\"tipoAula\":\"" + ApiClient.esc(tipoAula)  + "\""
            + "}";
        return ApiClient.post(BASE + "/" + idBloque + "/aulas", body);
    }

    public ApiResponse eliminarAula(int id) {
        return ApiClient.delete("/aulas/" + id);
    }
}
