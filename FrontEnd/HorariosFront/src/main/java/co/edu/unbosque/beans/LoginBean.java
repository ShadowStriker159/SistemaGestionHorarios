package co.edu.unbosque.beans;

import co.edu.unbosque.service.LoginService;
import co.edu.unbosque.service.LoginService.LoginResult;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;

@Named("loginBean")
@ViewScoped
public class LoginBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private LoginService loginService;

	private String id = "";
	private String nombre = "";
	private String password = "";
	private String rol = "administrador";

	private String otpCode = "";

	private int paso = 1;
	private String mensajeError = "";

	@PostConstruct
	public void init() {
		loginService = new LoginService();
	}


	public String iniciarLogin() {
		mensajeError = "";

		if (id == null || id.trim().isEmpty()) {
			mensajeError = "Por favor ingresa tu número de identificación o código.";
			addError(mensajeError);
			return null;
		}
		if (nombre == null || nombre.trim().isEmpty()) {
			mensajeError = "Por favor ingresa tu nombre.";
			addError(mensajeError);
			return null;
		}
		if (password == null || password.isEmpty()) {
			mensajeError = "Por favor ingresa tu contraseña.";
			addError(mensajeError);
			return null;
		}

		LoginResult resultado;
		try {
			switch (rolNormalizado()) {
			case "docente":
				resultado = loginService.loginDocente(id.trim(), nombre.trim(), password);
				break;
			case "estudiante":
				resultado = loginService.loginEstudiante(id.trim(), nombre.trim(), password);
				break;
			default:
				resultado = loginService.loginAdministrador(id.trim(), nombre.trim(), password);
				break;
			}
		} catch (Exception e) {
			mensajeError = "Error inesperado: " + e.getMessage();
			addError(mensajeError);
			return null;
		}

		if (resultado.isExito()) {
			otpCode = "";
			password = ""; 
			paso = 2;

		} else if (resultado.isCredencialesInvalidas()) {
			mensajeError = "Datos incorrectos. Verifica tu ID, nombre y contraseña.";
			addError(mensajeError);

		} else if (resultado.isErrorConexion()) {
			mensajeError = "No se pudo conectar con el servidor. " + "Verifica que el backend esté en ejecución.";
			addError(mensajeError);

		} else {
			mensajeError = "Error del servidor (HTTP " + resultado.getHttpCode() + "): " + resultado.getMensaje();
			addError(mensajeError);
		}

		return null;
	}


	public String verificarOtp() {
		mensajeError = "";

		String codigo = (otpCode == null) ? "" : otpCode.trim();
		if (!codigo.matches("[0-9]{6}")) {
			mensajeError = "Ingresa el código de 6 dígitos que llegó a tu correo.";
			addError(mensajeError);
			return null;
		}

		LoginResult r = loginService.verificarOtp(rolNormalizado(), id.trim(), codigo);

		if (r.isExito()) {
			guardarSesion(r);
			redirigirAlDashboard();
			return null;
		}

		if (r.isCredencialesInvalidas()) {
			mensajeError = "Código incorrecto o expirado. Si pasaron 5 minutos o fallaste "
					+ "3 veces, vuelve al paso anterior para recibir un código nuevo.";
		} else if (r.isErrorConexion()) {
			mensajeError = "No se pudo conectar con el servidor. " + "Verifica que el backend esté en ejecución.";
		} else {
			mensajeError = "Error del servidor (HTTP " + r.getHttpCode() + "): " + r.getMensaje();
		}
		addError(mensajeError);
		return null;
	}

	public String volverPaso1() {
		paso = 1;
		otpCode = "";
		mensajeError = "";
		return null;
	}

	public String cerrarSesion() {
		ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();
		ext.invalidateSession();
		try {
			ext.redirect(ext.getRequestContextPath() + "/index.xhtml");
		} catch (IOException e) {
			// sin acción
		}
		return null;
	}


	private String rolNormalizado() {
		return (rol == null || rol.isBlank()) ? "administrador" : rol.toLowerCase();
	}


	private void guardarSesion(LoginResult r) {
		ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();
		ext.getSessionMap().put("usuarioId", r.getCampo("id"));
		ext.getSessionMap().put("usuarioNombre", r.getCampo("nombre"));
		ext.getSessionMap().put("usuarioApellido", r.getCampo("apellido"));
		ext.getSessionMap().put("usuarioCorreo", r.getCampo("correo"));
		ext.getSessionMap().put("usuarioRol", r.getCampo("rol"));
	}

	private void redirigirAlDashboard() {
		ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();
		String base = ext.getRequestContextPath();

		String destino;
		switch (rolNormalizado()) {
		case "docente":
			destino = base + "/docente/dashboard.xhtml";
			break;
		case "estudiante":
			destino = base + "/estudiante/dashboard.xhtml";
			break;
		default:
			destino = base + "/secretaria/dashboard.xhtml";
			break;
		}

		try {
			ext.redirect(destino);
		} catch (IOException e) {
			mensajeError = "Error al redirigir: " + e.getMessage();
			addError(mensajeError);
		}
	}

	private void addError(String texto) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", texto));
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
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

	public void setRol(String rol) {
		this.rol = rol;
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

	public void setPaso(int paso) {
		this.paso = paso;
	}

	public String getMensajeError() {
		return mensajeError;
	}

	public void setMensajeError(String m) {
		this.mensajeError = m;
	}

	public boolean isPaso1() {
		return paso == 1;
	}

	public boolean isPaso2() {
		return paso == 2;
	}
}