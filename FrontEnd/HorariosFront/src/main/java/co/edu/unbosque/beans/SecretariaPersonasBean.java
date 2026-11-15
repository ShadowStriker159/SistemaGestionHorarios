package co.edu.unbosque.beans;

import co.edu.unbosque.service.DocenteService;
import co.edu.unbosque.service.EstudianteService;
import co.edu.unbosque.service.ApiClient;
import co.edu.unbosque.service.ApiClient.ApiResponse;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * SecretariaPersonasBean
 * Gestiona el CRUD de Docentes y Estudiantes desde la vista secretaria.
 *
 * Conectado a:
 *   GET/POST/DELETE /api/horarios/docentes
 *   GET/POST/DELETE /api/horarios/estudiantes
 */
@Named("personasBean")
@ViewScoped
public class SecretariaPersonasBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private DocenteService    docenteService;
    private EstudianteService estudianteService;

    // ---- Vista activa: "docentes" o "estudiantes" ----
    private String vistaActiva = "docentes";

    // ---- Listas en JSON (se pasan tal cual al JS de la vista) ----
    private String docentesJson    = "[]";
    private String estudiantesJson = "[]";

    // ---- Formulario Docente ----
    private String dId              = "";
    private String dNombre          = "";
    private String dApellido        = "";
    private String dCorreo          = "";
    private String dPassword        = "";
    private String dEscalafon       = "ASISTENTE";
    private String dTipoVinculacion = "PLANTA";

    // ---- Formulario Estudiante ----
    private String eId       = "";
    private String eNombre   = "";
    private String eApellido = "";
    private String eCorreo   = "";
    private String ePassword = "";
    private String eEstado   = "ACTIVO";

    // ---- Mensajes ----
    private String mensajeError = "";
    private String mensajeOk    = "";

    @PostConstruct
    public void init() {
        docenteService    = new DocenteService();
        estudianteService = new EstudianteService();
        cargarDocentes();
        cargarEstudiantes();
    }

    // ================================================================
    //  DOCENTES
    // ================================================================
    public void cargarDocentes() {
        docentesJson = docenteService.listarTodos();
    }

    public String crearDocente() {
        limpiarMensajes();
        if (isBlank(dId) || isBlank(dNombre) || isBlank(dApellido) || isBlank(dCorreo) || isBlank(dPassword)) {
            return error("Completa todos los campos del docente.");
        }
        ApiResponse r = docenteService.crear(dId, dNombre, dApellido, dCorreo, dPassword, dEscalafon, dTipoVinculacion);
        if (r.isOk()) {
            ok("Docente registrado exitosamente.");
            limpiarFormDocente(); cargarDocentes();
        } else if (r.isConflict()) {
            error("Ya existe un docente con ese ID o correo.");
        } else {
            error("Error al registrar el docente: " + r.getBody());
        }
        return null;
    }

    public String eliminarDocente(String id) {
        limpiarMensajes();
        ApiResponse r = docenteService.eliminar(id);
        if (r.isOk()) { ok("Docente eliminado."); cargarDocentes(); }
        else error("No se pudo eliminar el docente: " + r.getBody());
        return null;
    }

    // ================================================================
    //  ESTUDIANTES
    // ================================================================
    public void cargarEstudiantes() {
        estudiantesJson = estudianteService.listarTodos();
    }

    public String crearEstudiante() {
        limpiarMensajes();
        if (isBlank(eId) || isBlank(eNombre) || isBlank(eApellido) || isBlank(eCorreo) || isBlank(ePassword)) {
            return error("Completa todos los campos del estudiante.");
        }
        ApiResponse r = estudianteService.crear(eId, eNombre, eApellido, eCorreo, ePassword, eEstado);
        if (r.isOk()) {
            ok("Estudiante registrado exitosamente.");
            limpiarFormEstudiante(); cargarEstudiantes();
        } else if (r.isConflict()) {
            error("Ya existe un estudiante con ese ID o correo.");
        } else {
            error("Error al registrar el estudiante: " + r.getBody());
        }
        return null;
    }

    public String eliminarEstudiante(String id) {
        limpiarMensajes();
        ApiResponse r = estudianteService.eliminar(id);
        if (r.isOk()) { ok("Estudiante eliminado."); cargarEstudiantes(); }
        else error("No se pudo eliminar el estudiante: " + r.getBody());
        return null;
    }

    // ================================================================
    //  Helpers
    // ================================================================
    private void limpiarFormDocente() {
        dId = ""; dNombre = ""; dApellido = ""; dCorreo = "";
        dPassword = ""; dEscalafon = "ASISTENTE"; dTipoVinculacion = "PLANTA";
    }
    private void limpiarFormEstudiante() {
        eId = ""; eNombre = ""; eApellido = ""; eCorreo = ""; ePassword = ""; eEstado = "ACTIVO";
    }
    private void limpiarMensajes() { mensajeError = ""; mensajeOk = ""; }
    private String error(String m) {
        mensajeError = m;
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", m));
        return null;
    }
    private void ok(String m) {
        mensajeOk = m;
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", m));
    }
    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    // ================================================================
    //  Getters / Setters
    // ================================================================
    public String getVistaActiva()            { return vistaActiva; }
    public void   setVistaActiva(String v)    { this.vistaActiva = v; }

    public String getDocentesJson()           { return docentesJson; }
    public String getEstudiantesJson()        { return estudiantesJson; }

    public String getDId()                    { return dId; }
    public void   setDId(String v)            { dId = v; }
    public String getDNombre()                { return dNombre; }
    public void   setDNombre(String v)        { dNombre = v; }
    public String getDApellido()              { return dApellido; }
    public void   setDApellido(String v)      { dApellido = v; }
    public String getDCorreo()                { return dCorreo; }
    public void   setDCorreo(String v)        { dCorreo = v; }
    public String getDPassword()              { return dPassword; }
    public void   setDPassword(String v)      { dPassword = v; }
    public String getDEscalafon()             { return dEscalafon; }
    public void   setDEscalafon(String v)     { dEscalafon = v; }
    public String getDTipoVinculacion()       { return dTipoVinculacion; }
    public void   setDTipoVinculacion(String v){ dTipoVinculacion = v; }

    public String getEId()                    { return eId; }
    public void   setEId(String v)            { eId = v; }
    public String getENombre()                { return eNombre; }
    public void   setENombre(String v)        { eNombre = v; }
    public String getEApellido()              { return eApellido; }
    public void   setEApellido(String v)      { eApellido = v; }
    public String getECorreo()                { return eCorreo; }
    public void   setECorreo(String v)        { eCorreo = v; }
    public String getEPassword()              { return ePassword; }
    public void   setEPassword(String v)      { ePassword = v; }
    public String getEEstado()                { return eEstado; }
    public void   setEEstado(String v)        { eEstado = v; }

    public String getMensajeError()           { return mensajeError; }
    public void   setMensajeError(String v)   { mensajeError = v; }
    public String getMensajeOk()              { return mensajeOk; }
    public void   setMensajeOk(String v)      { mensajeOk = v; }

    public boolean isVistaDocentes()          { return "docentes".equals(vistaActiva); }
    public boolean isVistaEstudiantes()       { return "estudiantes".equals(vistaActiva); }
}
