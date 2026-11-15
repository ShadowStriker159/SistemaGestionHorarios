package co.edu.unbosque.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Cliente HTTP genérico para comunicarse con el backend Spring Boot (SGPHU).
 * Todos los Services del frontend lo usan en lugar de duplicar código HTTP.
 *
 * BASE_URL apunta al contexto del backend. Ajusta el puerto si es necesario.
 */
public class ApiClient {

    public static final String BASE_URL = "http://localhost:8080/api/horarios";

    private static final int TIMEOUT_CONN  = 5000;
    private static final int TIMEOUT_READ  = 8000;

    // ----------------------------------------------------------------
    //  Métodos públicos
    // ----------------------------------------------------------------

    /** GET → retorna el body como String (JSON) */
    public static String get(String path) {
        return ejecutar("GET", path, null);
    }

    /** POST con body JSON → retorna body de la respuesta */
    public static ApiResponse post(String path, String jsonBody) {
        String body  = ejecutar("POST", path, jsonBody);
        int    code  = lastCode.get();
        return new ApiResponse(code, body);
    }

    /** DELETE → retorna body de la respuesta */
    public static ApiResponse delete(String path) {
        String body = ejecutar("DELETE", path, null);
        int    code = lastCode.get();
        return new ApiResponse(code, body);
    }

    // ----------------------------------------------------------------
    //  Estado del último código HTTP (ThreadLocal para seguridad)
    // ----------------------------------------------------------------
    private static final ThreadLocal<Integer> lastCode = ThreadLocal.withInitial(() -> 0);

    // ----------------------------------------------------------------
    //  Implementación
    // ----------------------------------------------------------------

    private static String ejecutar(String method, String path, String jsonBody) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(BASE_URL + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(TIMEOUT_CONN);
            conn.setReadTimeout(TIMEOUT_READ);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json, text/plain");

            if (jsonBody != null) {
                conn.setDoOutput(true);
                byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
                conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bytes); os.flush();
                }
            }

            int code = conn.getResponseCode();
            lastCode.set(code);
            InputStream stream = (code >= 200 && code < 300)
                    ? conn.getInputStream() : conn.getErrorStream();
            if (stream == null) return "";
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);

        } catch (java.net.ConnectException ce) {
            lastCode.set(-1);
            return "{\"error\":\"No se pudo conectar al backend: " + BASE_URL + "\"}";
        } catch (Exception e) {
            lastCode.set(-2);
            return "{\"error\":\"" + escapar(e.getMessage()) + "\"}";
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String escapar(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ----------------------------------------------------------------
    //  Helpers de serialización JSON mínima (sin librerías externas)
    //  Para los campos más comunes de los DTOs del SGPHU.
    // ----------------------------------------------------------------

    /** Extrae el valor de un campo JSON simple: "campo":"valor" o "campo":123 */
    public static String extraerCampo(String json, String campo) {
        if (json == null || campo == null) return "";
        String patron = "\"" + campo + "\"";
        int idx = json.indexOf(patron);
        if (idx < 0) return "";
        int colon = json.indexOf(':', idx + patron.length());
        if (colon < 0) return "";
        int start = colon + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (start >= json.length()) return "";
        char primer = json.charAt(start);
        if (primer == '"') {
            int end = json.indexOf('"', start + 1);
            return end < 0 ? "" : json.substring(start + 1, end);
        } else {
            int end = start;
            while (end < json.length() && ",}]".indexOf(json.charAt(end)) < 0) end++;
            return json.substring(start, end).trim();
        }
    }

    /** Escapa un valor para incluirlo dentro de un JSON string */
    public static String esc(String v) {
        return escapar(v == null ? "" : v);
    }

    // ----------------------------------------------------------------
    //  Clase resultado
    // ----------------------------------------------------------------

    public static class ApiResponse {
        private final int    code;
        private final String body;

        public ApiResponse(int code, String body) {
            this.code = code;
            this.body = body == null ? "" : body;
        }

        public boolean isOk()       { return code >= 200 && code < 300; }
        public boolean isConflict() { return code == 409; }
        public boolean isNotFound() { return code == 404; }
        public boolean isError()    { return code < 0; }
        public int     getCode()    { return code; }
        public String  getBody()    { return body; }

        @Override
        public String toString() {
            return "ApiResponse{code=" + code + ", body='" + body + "'}";
        }
    }
}
