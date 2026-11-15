package co.edu.unbosque.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * LoginService — autenticación en dos pasos
 *
 * PASO 1: POST /api/horarios/login/{rol}  → body: { id, nombre, password }
 *         Backend valida con nombre+password, busca por id, envía OTP al correo.
 *         Responde 200 o 401.
 *
 * PASO 2: POST /api/horarios/login/verificar → body: { rol, id, codigo }
 *         Backend verifica el OTP. Responde 200 + UsuarioSesionDTO o 401.
 */
public class LoginService {

    private static final String BASE = ApiClient.BASE_URL + "/login";
    private static final int TIMEOUT = 6000;

    public LoginResult loginAdministrador(String id, String nombre, String password) {
        return post(BASE + "/administrador", buildCredJson(id, nombre, password));
    }

    public LoginResult loginDocente(String id, String nombre, String password) {
        return post(BASE + "/docente", buildCredJson(id, nombre, password));
    }

    public LoginResult loginEstudiante(String id, String nombre, String password) {
        return post(BASE + "/estudiante", buildCredJson(id, nombre, password));
    }

    /**
     * Paso 2 — Verificar OTP.
     * El backend espera un VerificacionDTO con rol, id y codigo.
     * Retorna el UsuarioSesionDTO si el código es correcto.
     */
    public LoginResult verificarOtp(String rol, String id, String codigo) {
        String body = "{\"rol\":\"" + esc(rol) + "\","
                    + "\"id\":\""   + esc(id)  + "\","
                    + "\"codigo\":\"" + esc(codigo) + "\"}";
        return post(BASE + "/verificar", body);
    }

    // ----------------------------------------------------------------
    private String buildCredJson(String id, String nombre, String password) {
        return "{\"id\":\""       + esc(id)       + "\","
             + "\"nombre\":\""    + esc(nombre)   + "\","
             + "\"password\":\"" + esc(password) + "\"}";
    }

    private LoginResult post(String endpoint, String jsonBody) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json, text/plain");

            byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
            try (OutputStream os = conn.getOutputStream()) { os.write(bytes); os.flush(); }

            int code = conn.getResponseCode();
            java.io.InputStream stream = (code >= 200 && code < 300)
                    ? conn.getInputStream() : conn.getErrorStream();
            String msg = stream == null ? "" : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return new LoginResult(code, msg);

        } catch (java.net.ConnectException e) {
            return new LoginResult(-1, "No se pudo conectar al backend. Verifica que esté activo.");
        } catch (Exception e) {
            return new LoginResult(-2, "Error: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private String esc(String v) { return ApiClient.esc(v); }

    public static class LoginResult {
        private final int code; private final String body;
        public LoginResult(int code, String body) { this.code = code; this.body = body == null ? "" : body; }
        public boolean isExito()                { return code == 200; }
        public boolean isCredencialesInvalidas(){ return code == 401; }
        public boolean isErrorConexion()        { return code < 0; }
        public int     getHttpCode()            { return code; }
        public String  getMensaje()             { return body; }
    }
}