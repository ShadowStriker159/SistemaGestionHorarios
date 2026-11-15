package co.edu.unbosque.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

@ApplicationScoped
public class ApiClient {

	public static final String BASE_URL = "http://localhost:8087/api/horarios";
	private static final int TIMEOUT_MS = 8000;

	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(LocalTime.class, new TypeAdapter<LocalTime>() {
				@Override
				public void write(JsonWriter out, LocalTime value) throws IOException {
					out.value(value.toString());
				}

				@Override
				public LocalTime read(JsonReader in) throws IOException {
					if (in.peek() == JsonToken.NULL) {
						in.nextNull();
						return null;
					}
					return LocalTime.parse(in.nextString());
				}
			}.nullSafe()).create();

	// Métodos HTTP

	/** GET → retorna el body como String (JSON) */
	public String get(String path) {
		ApiResponse response = ejecutar("GET", path, null);
		if (!response.isOk()) {
			throw new ApiException(response);
		}
		return response.getBody();
	}

	// POST con body JSON //
	public ApiResponse post(String path, String jsonBody) {
		return ejecutar("POST", path, jsonBody);
	}

	// PUT con body JSON //
	public ApiResponse put(String path, String jsonBody) {
		return ejecutar("PUT", path, jsonBody);
	}

	// DELETE//
	public ApiResponse delete(String path) {
		return ejecutar("DELETE", path, null);
	}

	// Gson helpers expuestos para los services

	public static Gson gson() {
		return GSON;
	}

	public static String esc(String v) {
		if (v == null)
			return "";
		return v.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	// Implementación interna

	private ApiResponse ejecutar(String method, String path, String jsonBody) {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(BASE_URL + path);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method);
			conn.setConnectTimeout(TIMEOUT_MS);
			conn.setReadTimeout(TIMEOUT_MS);
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("Accept", "application/json, text/plain");

			if (jsonBody != null) {
				conn.setDoOutput(true);
				byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
				conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
				try (OutputStream os = conn.getOutputStream()) {
					os.write(bytes);
					os.flush();
				}
			}

			int code = conn.getResponseCode();
			InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
			String body = stream == null ? "" : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
			return new ApiResponse(code, body);

		} catch (java.net.ConnectException e) {
			return new ApiResponse(-1, "No se pudo conectar al backend: " + BASE_URL);
		} catch (IOException e) {
			return new ApiResponse(-2, "Error de comunicación con el backend: " + e.getMessage());
		} finally {
			if (conn != null)
				conn.disconnect();
		}
	}

	// Clase de respuesta

	public static class ApiException extends RuntimeException {
		private final int code;

		public ApiException(ApiResponse response) {
			super(response.getCode() < 0 ? response.getBody()
					: "El backend respondió HTTP " + response.getCode() + ": " + response.getBody());
			this.code = response.getCode();
		}

		public int getCode() {
			return code;
		}
	}

	public static class ApiResponse {
		private final int code;
		private final String body;

		public ApiResponse(int code, String body) {
			this.code = code;
			this.body = body == null ? "" : body;
		}

		public boolean isOk() {
			return code >= 200 && code < 300;
		}

		public boolean isConflict() {
			return code == 409;
		}

		public boolean isNotFound() {
			return code == 404;
		}

		public boolean isError() {
			return code < 0;
		}

		public int getCode() {
			return code;
		}

		public String getBody() {
			return body;
		}
	}
}
