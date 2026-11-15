package co.edu.unbosque.service;

import co.edu.unbosque.model.AdministradorDTO;
import co.edu.unbosque.model.DocenteDTO;
import co.edu.unbosque.model.EstudianteDTO;
import co.edu.unbosque.model.UsuarioSesionDTO;
import co.edu.unbosque.model.VerificacionDTO;
import co.edu.unbosque.service.ApiClient.ApiResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LoginService {

	private static final String BASE = "/login";

	@Inject
	private ApiClient api;

	public LoginResult loginAdministrador(String id, String nombre, String password) {
		AdministradorDTO dto = new AdministradorDTO();
		dto.setId(id);
		dto.setNombre(nombre);
		dto.setPassword(password);
		return post("/administrador", ApiClient.gson().toJson(dto), false);
	}

	public LoginResult loginDocente(String id, String nombre, String password) {
		DocenteDTO dto = new DocenteDTO();
		dto.setId(id);
		dto.setNombre(nombre);
		dto.setPassword(password);
		return post("/docente", ApiClient.gson().toJson(dto), false);
	}

	public LoginResult loginEstudiante(String id, String nombre, String password) {
		EstudianteDTO dto = new EstudianteDTO();
		dto.setId(id);
		dto.setNombre(nombre);
		dto.setPassword(password);
		return post("/estudiante", ApiClient.gson().toJson(dto), false);
	}

	public LoginResult verificarOtp(String rol, String id, String codigo) {
		VerificacionDTO dto = new VerificacionDTO();
		dto.setRol(rol);
		dto.setId(id);
		dto.setCodigo(codigo);
		return post("/verificar", ApiClient.gson().toJson(dto), true);
	}

	private LoginResult post(String path, String jsonBody, boolean parseSession) {
		ApiResponse response = api.post(BASE + path, jsonBody);
		UsuarioSesionDTO usuario = null;
		if (parseSession && response.isOk()) {
			usuario = ApiClient.gson().fromJson(response.getBody(), UsuarioSesionDTO.class);
		}
		return new LoginResult(response.getCode(), response.getBody(), usuario);
	}

	public static class LoginResult {
		private final int code;
		private final String body;
		private final UsuarioSesionDTO usuarioSesion;

		public LoginResult(int code, String body, UsuarioSesionDTO usuarioSesion) {
			this.code = code;
			this.body = body == null ? "" : body;
			this.usuarioSesion = usuarioSesion;
		}

		public boolean isExito() {
			return code == 200;
		}

		public boolean isCredencialesInvalidas() {
			return code == 401;
		}

		public boolean isErrorConexion() {
			return code < 0;
		}

		public int getHttpCode() {
			return code;
		}

		public String getMensaje() {
			return body;
		}

		public UsuarioSesionDTO getUsuarioSesion() {
			return usuarioSesion;
		}
	}
}
