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

/**
 * LoginBean — Autenticación en dos pasos real con el backend SGPHU.
 *
 * PASO 1: id + nombre + password → POST /api/horarios/login/{rol}
 *         Backend valida y envía OTP al correo registrado.
 *
 * PASO 2: codigo 6 dígitos → POST /api/horarios/login/verificar
 *         Backend valida OTP y retorna UsuarioSesionDTO si es correcto.
 */
@Named("loginBean")
@ViewScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private LoginService loginService;

    // ---- Paso 1 ----
    private String id       = "";
    private String nombre   = "";
    private String password = "";
    private String rol      = "administrador";

    // ---- Paso 2 ----
    private String otpCode  = "";

    // ---- Estado ----
    private int    paso         = 1;
    private String mensajeError = "";

    @PostConstruct
    public void init() {
        loginService = new LoginService();
    }

    // ================================================================
    //  PASO 1
    // ================================================================
    public String iniciarLogin() {
        mensajeError = "";

        if (isBlank(id))       { return error("Por favor ingresa tu número de identificación."); }
        if (isBlank(nombre))   { return error("Por favor ingresa tu nombre."); }
        if (isBlank(password)) { return error("Por favor ingresa tu contraseña."); }

        LoginResult res;
        try {
            res = switch (rolKey()) {
                case "docente"    -> loginService.loginDocente(id.trim(), nombre.trim(), password);
                case "estudiante" -> loginService.loginEstudiante(id.trim(), nombre.trim(), password);
                default           -> loginService.loginAdministrador(id.trim(), nombre.trim(), password);
            };
        } catch (Exception e) {
            return error("Error inesperado: " + e.getMessage());
        }

        if (res.isExito()) {
            otpCode = ""; paso = 2;
        } else if (res.isCredencialesInvalidas()) {
            return error("Datos incorrectos. Verifica tu ID, nombre y contraseña.");
        } else if (res.isErrorConexion()) {
            return error("No se pudo conectar al servidor backend. Asegúrate de que esté activo.");
        } else {
            return error("Error del servidor (HTTP " + res.getHttpCode() + "): " + res.getMensaje());
        }
        return null;
    }

    // ================================================================
    //  PASO 2
    // ================================================================
    public String verificarOtp() {
        mensajeError = "";
        String codigo = otpCode == null ? "" : otpCode.trim();

        if (codigo.length() != 6 || !codigo.matches("[0-9]{6}")) {
            return error("Ingresa el código de 6 dígitos que llegó a tu correo.");
        }

        LoginResult res;
        try {
            res = loginService.verificarOtp(rolKey(), id.trim(), codigo);
        } catch (Exception e) {
            return error("Error al verificar: " + e.getMessage());
        }

        if (res.isExito()) {
            redirigir();
        } else if (res.isCredencialesInvalidas()) {
            return error("Código incorrecto o expirado. Solicita uno nuevo.");
        } else if (res.isErrorConexion()) {
            return error("No se pudo verificar el código. Error de red.");
        } else {
            return error("Error al verificar (HTTP " + res.getHttpCode() + "): " + res.getMensaje());
        }
        return null;
    }

    public String volverPaso1() {
        paso = 1; otpCode = ""; mensajeError = ""; return null;
    }

    // ================================================================
    //  Helpers
    // ================================================================
    private void redirigir() {
        ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();
        String base = ext.getRequestContextPath();
        String dest = switch (rolKey()) {
            case "docente"    -> base + "/docente/dashboard.xhtml";
            case "estudiante" -> base + "/estudiante/dashboard.xhtml";
            default           -> base + "/secretaria/dashboard.xhtml";
        };
        try { ext.redirect(dest); }
        catch (IOException e) { error("Error al redirigir: " + e.getMessage()); }
    }

    private String error(String msg) {
        mensajeError = msg;
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", msg));
        return null;
    }

    private String rolKey() {
        return rol == null ? "administrador" : rol.toLowerCase();
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    // ---- Getters / Setters ----
    public String getId()                   { return id; }
    public void   setId(String id)          { this.id = id; }
    public String getNombre()               { return nombre; }
    public void   setNombre(String n)       { this.nombre = n; }
    public String getPassword()             { return password; }
    public void   setPassword(String p)     { this.password = p; }
    public String getRol()                  { return rol; }
    public void   setRol(String r)          { this.rol = r; }
    public String getOtpCode()              { return otpCode; }
    public void   setOtpCode(String c)      { this.otpCode = c; }
    public int    getPaso()                 { return paso; }
    public void   setPaso(int p)            { this.paso = p; }
    public String getMensajeError()         { return mensajeError; }
    public void   setMensajeError(String m) { this.mensajeError = m; }
    public boolean isPaso1()                { return paso == 1; }
    public boolean isPaso2()                { return paso == 2; }
}