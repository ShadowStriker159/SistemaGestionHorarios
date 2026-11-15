package co.edu.unbosque.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginService {

	private static final String BASE_URL = "http://localhost:8080/api/horarios/login";

	private static final int CONNECT_TIMEOUT = 5000;
	private static final int READ_TIMEOUT = 8000;


	public LoginResult loginAdministrador(String id, String nombre, String password) {
		return enviarPost(BASE_URL + "/administrador", construirJsonLogin(id, nombre, password));
	}

	public LoginResult loginDocente(String id, String nombre, String password) {
		return enviarPost(BASE_URL + "/docente", construirJsonLogin(id, nombre, password));
	}

	public LoginResult loginEstudiante(String id, String nombre, String password) {
		return enviarPost(BASE_URL + "/estudiante", construirJsonLogin(id, nombre, password));
	}

	public LoginResult verificarOtp(String rol, String id, String codigo) {
		String body = "{" + "\"id\":\"" + escaparJson(id) + "\"," + "\"rol\":\"" + escaparJson(rol) + "\","
				+ "\"codigo\":\"" + escaparJson(codigo) + "\"" + "}";
		return enviarPost(BASE_URL + "/verificar", body);
	}

	private String construirJsonLogin(String id, String nombre, String password) {
		return "{" + "\"id\":\"" + escaparJson(id) + "\"," + "\"nombre\":\"" + escaparJson(nombre) + "\","
				+ "\"password\":\"" + escaparJson(password) + "\"" + "}";
	}

	private LoginResult enviarPost(String endpoint, String jsonBody) {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(endpoint);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			conn.setReadTimeout(READ_TIMEOUT);
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("Accept", "text/plain, application/json");

			byte[] bodyBytes = jsonBody.getBytes(StandardCharsets.UTF_8);
			conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));
			try (OutputStream os = conn.getOutputStream()) {
				os.write(bodyBytes);
				os.flush();
			}

			int httpCode = conn.getResponseCode();
			String mensaje = leerRespuesta(conn, httpCode);
			return new LoginResult(httpCode, mensaje);

		} catch (ConnectException ce) {
			return new LoginResult(-1,
					"No se pudo conectar al servidor. Verifica que el backend esté corriendo en: " + BASE_URL);
		} catch (SocketTimeoutException ste) {
			return new LoginResult(-2, "El servidor tardó demasiado en responder. Intenta de nuevo.");
		} catch (Exception e) {
			return new LoginResult(-3, "Error de comunicación: " + e.getMessage());
		} finally {
			if (conn != null)
				conn.disconnect();
		}
	}

	private String leerRespuesta(HttpURLConnection conn, int httpCode) {
		try {
			InputStream stream = (httpCode >= 200 && httpCode < 300) ? conn.getInputStream() : conn.getErrorStream();
			if (stream == null)
				return "";
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			return "";
		}
	}

	private String escaparJson(String valor) {
		if (valor == null)
			return "";
		return valor.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t",
				"\\t");
	}

	// ---------- Resultado ----------

	public static class LoginResult {

		private final int httpCode;
		private final String mensaje;

		public LoginResult(int httpCode, String mensaje) {
			this.httpCode = httpCode;
			this.mensaje = mensaje;
		}

		public boolean isExito() {
			return httpCode == 200;
		}

		public boolean isCredencialesInvalidas() {
			return httpCode == 401;
		}

		public boolean isErrorConexion() {
			return httpCode < 0;
		}

		public int getHttpCode() {
			return httpCode;
		}

		public String getMensaje() {
			return mensaje;
		}

		public String getCampo(String campo) {
			if (mensaje == null)
				return "";
			Pattern p = Pattern.compile("\"" + Pattern.quote(campo) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
			Matcher m = p.matcher(mensaje);
			if (!m.find())
				return "";
			return m.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
		}

		@Override
		public String toString() {
			return "LoginResult{httpCode=" + httpCode + ", mensaje='" + mensaje + "'}";
		}
	}
}