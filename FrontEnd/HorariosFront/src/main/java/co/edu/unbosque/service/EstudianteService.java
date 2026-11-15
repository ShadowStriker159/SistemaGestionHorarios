package co.edu.unbosque.service;

import co.edu.unbosque.service.ApiClient.ApiResponse;

/**
 * EstudianteService
 * Conecta con:
 *   GET    /api/horarios/estudiantes
 *   POST   /api/horarios/estudiantes  body: { id, nombre, apellido, correo, password, estadoEstudiante }
 *   DELETE /api/horarios/estudiantes/{id}
 */
public class EstudianteService {

    private static final String BASE = "/estudiantes";

    public String listarTodos() {
        return ApiClient.get(BASE);
    }

    public ApiResponse crear(String id, String nombre, String apellido,
                             String correo, String password, String estado) {
        String body = "{"
            + "\"id\":\""                + ApiClient.esc(id)       + "\","
            + "\"nombre\":\""            + ApiClient.esc(nombre)   + "\","
            + "\"apellido\":\""          + ApiClient.esc(apellido) + "\","
            + "\"correo\":\""            + ApiClient.esc(correo)   + "\","
            + "\"password\":\""          + ApiClient.esc(password) + "\","
            + "\"estadoEstudiante\":\"" + ApiClient.esc(estado)   + "\""
            + "}";
        return ApiClient.post(BASE, body);
    }

    public ApiResponse eliminar(String id) {
        return ApiClient.delete(BASE + "/" + ApiClient.esc(id));
    }
}
