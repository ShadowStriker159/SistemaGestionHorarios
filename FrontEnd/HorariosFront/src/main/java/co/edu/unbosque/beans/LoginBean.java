package co.edu.unbosque.beans;

import co.edu.unbosque.model.UsuarioSesionDTO;
import co.edu.unbosque.service.LoginService;
import co.edu.unbosque.service.LoginService.LoginResult;
import co.edu.unbosque.util.AutorizacionFilter;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.Serializable;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private LoginService loginService;

	// Paso 1
	private String id = "";
	private String nombre = "";
	private String password = "";
	private String rol = "administrador";

	// Paso 2
	private String otpCode = "";

	// Estado
	private int paso = 1;
	private String mensajeError = "";
	private UsuarioSesionDTO usuarioSesion;

	// PASO 1

	public String iniciarLogin() {
		mensajeError = "";
		usuarioSesion = null;
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("usuarioSesion");

		if (isBlank(id)) {
			return error("Ingresa tu número de identificación.");
		}
		if (isBlank(nombre)) {
			return error("Ingresa tu nombre.");
		}
		if (isBlank(password)) {
			return error("Ingresa tu contraseña.");
		}

		LoginResult res;
		try {
			if ("docente".equals(rolKey()))
				res = loginService.loginDocente(id.trim(), nombre.trim(), password);
			else if ("estudiante".equals(rolKey()))
				res = loginService.loginEstudiante(id.trim(), nombre.trim(), password);
			else
				res = loginService.loginAdministrador(id.trim(), nombre.trim(), password);
		} catch (Exception e) {
			return error("Error inesperado: " + e.getMessage());
		}

		if (res.isExito()) {
			otpCode = "";
			paso = 2;
		} else if (res.isCredencialesInvalidas()) {
			return error("ID, nombre o contraseña incorrectos.");
		} else if (res.isErrorConexion()) {
			return error("No se pudo conectar al backend. Verifica que el servicio esté activo.");
		} else {
			return error("Error del servidor (HTTP " + res.getHttpCode() + "): " + res.getMensaje());
		}
		return null;
	}

	// PASO 2

	public String verificarOtp() {
		mensajeError = "";
		String codigo = otpCode == null ? "" : otpCode.trim();

		if (codigo.length() != 6 || !codigo.matches("[0-9]{6}")) {
			return error("Ingresa el código de 6 dígitos enviado a tu correo.");
		}

		LoginResult res;
		try {
			res = loginService.verificarOtp(rolKey(), id.trim(), codigo);
		} catch (Exception e) {
			return error("Error al verificar: " + e.getMessage());
		}

		if (res.isExito()) {
			usuarioSesion = res.getUsuarioSesion();
			if (usuarioSesion == null || !AutorizacionFilter.rolValido(usuarioSesion.getRol())) {
				usuarioSesion = null;
				return error("El backend no devolvió un rol válido para la sesión.");
			}
			if (!AutorizacionFilter.normalizarRol(rolKey())
					.equals(AutorizacionFilter.normalizarRol(usuarioSesion.getRol()))) {
				usuarioSesion = null;
				return error("El rol verificado no coincide con el rol seleccionado para iniciar sesión.");
			}
			ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();
			HttpServletRequest request = (HttpServletRequest) ext.getRequest();
			if (request.getSession(false) != null) {
				request.changeSessionId();
			}
			ext.getSessionMap().put("usuarioSesion", usuarioSesion);
			redirigir();
		} else if (res.isCredencialesInvalidas()) {
			return error("Código incorrecto o expirado.");
		} else if (res.isErrorConexion()) {
			return error("Error de red al verificar.");
		} else {
			return error("Error (HTTP " + res.getHttpCode() + ")");
		}
		return null;
	}

	public String volverPaso1() {
		paso = 1;
		otpCode = "";
		mensajeError = "";
		return null;
	}

	// Helpers

	private void redirigir() {
		ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();
		String dest = ext.getRequestContextPath() + AutorizacionFilter.paginaInicio(rolKey());
		try {
			ext.redirect(dest);
		} catch (IOException e) {
			error("Error al redirigir: " + e.getMessage());
		}
	}

	private String error(String msg) {
		mensajeError = msg;
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", msg));
		return null;
	}

	private String rolKey() {
		return rol == null ? "administrador" : rol.toLowerCase();
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	// Getters / Setters

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String n) {
		this.nombre = n;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String p) {
		this.password = p;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String r) {
		this.rol = r;
	}

	public String getOtpCode() {
		return otpCode;
	}

	public void setOtpCode(String c) {
		this.otpCode = c;
	}

	public int getPaso() {
		return paso;
	}

	public void setPaso(int p) {
		this.paso = p;
	}

	public String getMensajeError() {
		return mensajeError;
	}

	public void setMensajeError(String m) {
		this.mensajeError = m;
	}

	public UsuarioSesionDTO getUsuarioSesion() {
		return usuarioSesion;
	}

	public boolean isPaso1() {
		return paso == 1;
	}

	public boolean isPaso2() {
		return paso == 2;
	}
}