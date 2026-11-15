package co.edu.unbosque.SistemaHorario.sistema;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PRUEBA DE SISTEMA: arranca la aplicación completa en un puerto real, contra la base MySQL de pruebas
 * (sistema_horarios_test), y recorre por HTTP el mismo flujo que usa el front (secretaría crea la
 * estructura; el estudiante consulta su portal). Los pasos dependen entre sí y se ejecutan en orden.
 *
 * No corre por defecto: ejecutar con  mvn test -Dhorarios.bd.pruebas=true
 * Se puede ejecutar todas las veces que se quiera: los nombres son únicos por corrida y el último paso
 * borra lo que se creó (incluida la auditoría de sus inscripciones).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "horarios.bd.pruebas", matches = "true")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FlujoCompletoSistemaTest {

    @Value("${local.server.port}")
    private int puerto;

    @Autowired
    private JdbcTemplate jdbc;

    // Identificadores únicos por corrida: una corrida interrumpida no bloquea a la siguiente.
    private static final String SUF = String.valueOf(System.currentTimeMillis() % 1_000_000);
    private static final String D1 = "TD1" + SUF;
    private static final String D2 = "TD2" + SUF;
    private static final String E1 = "TE1" + SUF;

    private final HttpClient http = HttpClient.newHttpClient();

    // Estado compartido entre pasos
    private static int idFacultad, idCarrera, idPlan, idPlanVacio, idCurso, idGrupo;

    private HttpResponse<String> enviar(String metodo, String ruta, String json) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/horarios" + ruta))
                .header("Content-Type", "application/json");
        HttpRequest.BodyPublisher cuerpo = json == null
                ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json);
        return http.send(b.method(metodo, cuerpo).build(), HttpResponse.BodyHandlers.ofString());
    }

    private static int id(String json, String campo) {
        Matcher m = Pattern.compile("\"" + campo + "\"\\s*:\\s*(\\d+)").matcher(json);
        assertTrue(m.find(), "No se encontró " + campo + " en: " + json);
        return Integer.parseInt(m.group(1));
    }

    @Test @Order(1)
    void secretariaCreaLaEstructuraAcademica() throws Exception {
        HttpResponse<String> r = enviar("POST", "/facultades", "{\"nombreFacultad\":\"Ingeniería " + SUF + "\"}");
        assertEquals(201, r.statusCode(), r.body());
        idFacultad = id(r.body(), "idFacultad");

        r = enviar("POST", "/facultades/" + idFacultad + "/carreras", "{\"nombreCarrera\":\"Sistemas " + SUF + "\"}");
        assertEquals(201, r.statusCode(), r.body());
        idCarrera = id(r.body(), "idCarrera");

        r = enviar("POST", "/carreras/" + idCarrera + "/planes", "{\"nombrePlanEstudios\":\"Plan 2026 " + SUF + "\"}");
        assertEquals(201, r.statusCode(), r.body());
        idPlan = id(r.body(), "idPlanEstudios");

        r = enviar("POST", "/carreras/" + idCarrera + "/planes", "{\"nombrePlanEstudios\":\"Plan sin grupos " + SUF + "\"}");
        assertEquals(201, r.statusCode(), r.body());
        idPlanVacio = id(r.body(), "idPlanEstudios");

        r = enviar("POST", "/planes/" + idPlan + "/cursos",
                "{\"nombreCurso\":\"Programación " + SUF + "\",\"creditos\":3,\"intensidadHoraria\":4,"
                        + "\"semestre\":1,\"aulaRequerida\":\"SISTEMAS\"}");
        assertEquals(201, r.statusCode(), r.body());
        idCurso = id(r.body(), "idCurso");
    }

    @Test @Order(2)
    void secretariaCreaDocenteYGrupo() throws Exception {
        HttpResponse<String> r = enviar("POST", "/docentes",
                "{\"id\":\"" + D1 + "\",\"nombre\":\"Ana\",\"apellido\":\"Ruiz\",\"correo\":\"ana" + SUF + "@u.edu\","
                        + "\"password\":\"clave1\",\"escalafon\":\"PROFESOR_TITULAR\","
                        + "\"estadoDocente\":\"ACTIVO\",\"tipoVinculacion\":\"TC\"}");
        assertEquals(201, r.statusCode(), r.body());
        assertFalse(r.body().contains("clave1"), "La respuesta no debe exponer la contraseña");

        r = enviar("POST", "/cursos/" + idCurso + "/grupos?idDocente=" + D1 + "",
                "{\"cupoMaximo\":30,\"cupoMinimo\":5}");
        assertEquals(201, r.statusCode(), r.body());
        idGrupo = id(r.body(), "idGrupo");
    }

    @Test @Order(3)
    void secretariaEditaElDocenteSinCambiarSuPassword() throws Exception {
        HttpResponse<String> r = enviar("PUT", "/docentes/" + D1 + "",
                "{\"nombre\":\"Ana María\",\"apellido\":\"Ruiz\",\"correo\":\"ana.maria" + SUF + "@u.edu\","
                        + "\"escalafon\":\"PROFESOR_ASOCIADO\",\"estadoDocente\":\"VACACIONES\","
                        + "\"tipoVinculacion\":\"MTO\"}");
        assertEquals(200, r.statusCode(), r.body());
        assertTrue(r.body().contains("Ana María"));

        r = enviar("GET", "/docentes/" + D1 + "", null);
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("PROFESOR_ASOCIADO") && r.body().contains("ana.maria" + SUF + "@u.edu"));
    }

    @Test @Order(4)
    void editarDocenteConCorreoAjenoDa409() throws Exception {
        enviar("POST", "/docentes",
                "{\"id\":\"" + D2 + "\",\"nombre\":\"Pedro\",\"apellido\":\"Gil\",\"correo\":\"pedro" + SUF + "@u.edu\","
                        + "\"password\":\"x\",\"escalafon\":\"PROFESOR_TITULAR\","
                        + "\"estadoDocente\":\"ACTIVO\",\"tipoVinculacion\":\"TC\"}");
        HttpResponse<String> r = enviar("PUT", "/docentes/" + D2 + "",
                "{\"nombre\":\"Pedro\",\"apellido\":\"Gil\",\"correo\":\"ana.maria" + SUF + "@u.edu\","
                        + "\"escalafon\":\"PROFESOR_TITULAR\",\"estadoDocente\":\"ACTIVO\",\"tipoVinculacion\":\"TC\"}");
        assertEquals(409, r.statusCode(), r.body());
    }

    @Test @Order(5)
    void editarDocenteInexistenteDa404() throws Exception {
        HttpResponse<String> r = enviar("PUT", "/docentes/NOEXISTE",
                "{\"nombre\":\"X\",\"apellido\":\"Y\",\"correo\":\"x@u.edu\",\"escalafon\":\"PROFESOR_TITULAR\","
                        + "\"estadoDocente\":\"ACTIVO\",\"tipoVinculacion\":\"TC\"}");
        assertEquals(404, r.statusCode(), r.body());
    }

    @Test @Order(6)
    void secretariaCreaYEditaAlEstudiante() throws Exception {
        HttpResponse<String> r = enviar("POST", "/estudiantes",
                "{\"id\":\"" + E1 + "\",\"nombre\":\"Luis\",\"apellido\":\"Mora\",\"correo\":\"luis" + SUF + "@u.edu\","
                        + "\"password\":\"clave2\",\"semestreCursa\":1,\"creditosAprobados\":0,"
                        + "\"estadoEstudiante\":\"ACTIVO\",\"idPlanEstudios\":" + idPlan
                        + ",\"idCarrera\":" + idCarrera + "}");
        assertEquals(201, r.statusCode(), r.body());

        r = enviar("PUT", "/estudiantes/" + E1 + "",
                "{\"nombre\":\"Luis Fernando\",\"apellido\":\"Mora\",\"correo\":\"luis" + SUF + "@u.edu\","
                        + "\"semestreCursa\":2,\"creditosAprobados\":15,\"estadoEstudiante\":\"ACTIVO\"}");
        assertEquals(200, r.statusCode(), r.body());
        assertTrue(r.body().contains("Luis Fernando"));
        assertEquals(idPlan, id(r.body(), "idPlanEstudios"), "Sin idPlanEstudios en el PUT, el plan no cambia");
    }

    @Test @Order(7)
    void portalDelEstudianteObtieneSusDatosYLosGruposDelPlan() throws Exception {
        HttpResponse<String> r = enviar("GET", "/estudiantes/" + E1 + "", null);
        assertEquals(200, r.statusCode(), r.body());
        assertEquals(idPlan, id(r.body(), "idPlanEstudios"));
        assertEquals(idCarrera, id(r.body(), "idCarrera"));
        assertTrue(r.body().contains("Sistemas"));
        assertFalse(r.body().contains("clave2"));

        r = enviar("GET", "/planes/" + idPlan + "/grupos", null);
        assertEquals(200, r.statusCode(), r.body());
        assertEquals(idGrupo, id(r.body(), "idGrupo"));
        assertEquals(idCurso, id(r.body(), "idCurso"));
    }

    @Test @Order(8)
    void inscripcionSeVeEnLosGruposDelPlan() throws Exception {
        HttpResponse<String> r = enviar("POST", "/grupos/" + idGrupo + "/estudiantes/" + E1 + "", null);
        assertEquals(201, r.statusCode(), r.body());

        r = enviar("GET", "/planes/" + idPlan + "/grupos", null);
        assertTrue(r.body().contains("\"" + E1 + "\""), "El estudiante inscrito debe aparecer en el grupo: " + r.body());

        r = enviar("DELETE", "/grupos/" + idGrupo + "/estudiantes/" + E1 + "", null);
        assertEquals(200, r.statusCode(), r.body());
        r = enviar("GET", "/planes/" + idPlan + "/grupos", null);
        assertFalse(r.body().contains("\"" + E1 + "\""));
    }

    @Test @Order(9)
    void planSinGruposDevuelveArregloVacio() throws Exception {
        HttpResponse<String> r = enviar("GET", "/planes/" + idPlanVacio + "/grupos", null);
        assertEquals(200, r.statusCode());
        assertEquals("[]", r.body().trim());
    }

    @Test @Order(10)
    void recursosInexistentesDan404() throws Exception {
        assertEquals(404, enviar("GET", "/planes/999999/grupos", null).statusCode());
        assertEquals(404, enviar("GET", "/estudiantes/NOEXISTE", null).statusCode());
        assertEquals(404, enviar("PUT", "/estudiantes/NOEXISTE",
                "{\"nombre\":\"X\",\"apellido\":\"Y\",\"correo\":\"z@u.edu\",\"semestreCursa\":1,"
                        + "\"creditosAprobados\":0,\"estadoEstudiante\":\"ACTIVO\"}").statusCode());
    }

    @Test @Order(11)
    void datosInvalidosDan400() throws Exception {
        HttpResponse<String> r = enviar("PUT", "/estudiantes/" + E1 + "",
                "{\"nombre\":\"Luis\",\"apellido\":\"Mora\",\"correo\":\"luis" + SUF + "@u.edu\",\"semestreCursa\":0,"
                        + "\"creditosAprobados\":0,\"estadoEstudiante\":\"ACTIVO\"}");
        assertEquals(400, r.statusCode(), r.body());
    }

    @Test @Order(99)
    void limpiezaBorraLoCreado() throws Exception {
        // En orden inverso a las dependencias. Si alguno ya no existe, se ignora.
        for (String ruta : List.of("/grupos/" + idGrupo, "/estudiantes/" + E1 + "", "/docentes/" + D1 + "", "/docentes/" + D2 + "",
                "/cursos/" + idCurso, "/planes/" + idPlan, "/planes/" + idPlanVacio,
                "/carreras/" + idCarrera, "/facultades/" + idFacultad)) {
            enviar("DELETE", ruta, null);
        }
        assertEquals(404, enviar("GET", "/estudiantes/" + E1 + "", null).statusCode());
        // la auditoría de las inscripciones de este estudiante tampoco se queda en la base
        jdbc.update("DELETE FROM auditoria WHERE id_estudiante = ?", E1);
    }
}
