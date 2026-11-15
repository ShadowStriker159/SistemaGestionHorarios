package co.edu.unbosque.service;

import co.edu.unbosque.service.ApiClient.ApiResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * FacultadService
 * Conecta con:
 *   GET    /api/horarios/facultades
 *   POST   /api/horarios/facultades          body: { nombre }
 *   DELETE /api/horarios/facultades/{id}
 *   GET    /api/horarios/facultades/{id}/areas
 *   POST   /api/horarios/facultades/{id}/areas  body: { nombre }
 *   GET    /api/horarios/facultades/{id}/carreras
 *   POST   /api/horarios/facultades/{id}/carreras body: { nombre }
 */
public class FacultadService {

    private static final String BASE = "/facultades";

    // ---- Facultades ----

    public String listarFacultades() {
        return ApiClient.get(BASE);
    }

    public ApiResponse crearFacultad(String nombre) {
        String body = "{\"nombre\":\"" + ApiClient.esc(nombre) + "\"}";
        return ApiClient.post(BASE, body);
    }

    public ApiResponse eliminarFacultad(int id) {
        return ApiClient.delete(BASE + "/" + id);
    }

    // ---- Áreas de una facultad ----

    public String listarAreas(int idFacultad) {
        return ApiClient.get(BASE + "/" + idFacultad + "/areas");
    }

    public ApiResponse crearArea(int idFacultad, String nombre) {
        String body = "{\"nombre\":\"" + ApiClient.esc(nombre) + "\"}";
        return ApiClient.post(BASE + "/" + idFacultad + "/areas", body);
    }

    // ---- Carreras de una facultad ----

    public String listarCarreras(int idFacultad) {
        return ApiClient.get(BASE + "/" + idFacultad + "/carreras");
    }

    public ApiResponse crearCarrera(int idFacultad, String nombre) {
        String body = "{\"nombre\":\"" + ApiClient.esc(nombre) + "\"}";
        return ApiClient.post(BASE + "/" + idFacultad + "/carreras", body);
    }

    // ---- Planes de estudio de una carrera ----

    public String listarPlanes(int idCarrera) {
        return ApiClient.get("/carreras/" + idCarrera + "/planes");
    }

    public ApiResponse crearPlan(int idCarrera, String nombre, String anio) {
        String body = "{\"nombre\":\"" + ApiClient.esc(nombre) + "\","
                    + "\"anio\":\"" + ApiClient.esc(anio) + "\"}";
        return ApiClient.post("/carreras/" + idCarrera + "/planes", body);
    }

    public ApiResponse eliminarCarrera(int id) {
        return ApiClient.delete("/carreras/" + id);
    }

    public ApiResponse eliminarArea(int id) {
        return ApiClient.delete("/areas/" + id);
    }

    public ApiResponse eliminarPlan(int id) {
        return ApiClient.delete("/planes/" + id);
    }

    // ---- Cursos de un plan ----

    public String listarCursosPorPlan(int idPlan) {
        return ApiClient.get("/planes/" + idPlan + "/cursos");
    }

    public ApiResponse crearCurso(int idPlan, String nombre, String creditos) {
        String body = "{\"nombre\":\"" + ApiClient.esc(nombre) + "\","
                    + "\"creditos\":" + (creditos.isEmpty() ? "0" : ApiClient.esc(creditos)) + "}";
        return ApiClient.post("/planes/" + idPlan + "/cursos", body);
    }

    public ApiResponse eliminarCurso(int id) {
        return ApiClient.delete("/cursos/" + id);
    }

    // ---- Grupos de un curso ----

    public String listarGruposPorCurso(int idCurso) {
        return ApiClient.get("/cursos/" + idCurso + "/grupos");
    }

    public ApiResponse crearGrupo(int idCurso, String idDocente, String nombre) {
        String body = "{\"nombre\":\"" + ApiClient.esc(nombre) + "\"}";
        return ApiClient.post("/cursos/" + idCurso + "/grupos?idDocente=" + ApiClient.esc(idDocente), body);
    }

    public ApiResponse eliminarGrupo(int id) {
        return ApiClient.delete("/grupos/" + id);
    }
}
