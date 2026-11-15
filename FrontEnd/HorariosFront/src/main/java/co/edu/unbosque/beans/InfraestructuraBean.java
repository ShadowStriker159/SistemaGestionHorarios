package co.edu.unbosque.beans;

import co.edu.unbosque.service.BloqueService;
import co.edu.unbosque.service.FranjaService;
import co.edu.unbosque.service.HorarioService;
import co.edu.unbosque.service.ApiClient.ApiResponse;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * InfraestructuraBean
 * Gestiona Bloques, Aulas, Franjas Horarias y Generación de Horario.
 *
 * Endpoints:
 *   GET/POST/DELETE /api/horarios/bloques
 *   GET/POST/DELETE /api/horarios/bloques/{id}/aulas
 *   DELETE          /api/horarios/aulas/{id}
 *   GET/POST/DELETE /api/horarios/franjas
 *   GET             /api/horarios/listar
 *   POST            /api/horarios/generar?idFacultad={id}
 */
@Named("infraBean")
@ViewScoped
public class InfraestructuraBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private BloqueService bloqueService;
    private FranjaService franjaService;
    private HorarioService horarioService;

    // ---- Datos ----
    private String bloquesJson  = "[]";
    private String aulasJson    = "[]";
    private String franjasJson  = "[]";
    private String horariosJson = "[]";

    // ---- Selección ----
    private int idBloqueSel = 0;
    private int idFacultadGenerar = 1;

    // ---- Formularios ----
    private String bNombre    = "";
    private String auNombre   = "";
    private String auCapacidad = "";
    private String auTipo     = "SALON_ESTANDAR";

    private String frHoraInicio = "";
    private String frHoraFin    = "";
    private String frDia        = "LUNES";

    // ---- Mensajes ----
    private String mensajeError = "";
    private String mensajeOk    = "";

    @PostConstruct
    public void init() {
        bloqueService  = new BloqueService();
        franjaService  = new FranjaService();
        horarioService = new HorarioService();
        cargarBloques();
        cargarFranjas();
        cargarHorarios();
    }

    // ================================================================
    //  BLOQUES
    // ================================================================
    public void cargarBloques() { bloquesJson = bloqueService.listarBloques(); }

    public String crearBloque() {
        limpiarMensajes();
        if (isBlank(bNombre)) return error("Ingresa el nombre del bloque.");
        ApiResponse r = bloqueService.crearBloque(bNombre);
        if (r.isOk()) { ok("Bloque creado."); bNombre = ""; cargarBloques(); }
        else error("Error al crear bloque: " + r.getBody());
        return null;
    }

    public String eliminarBloque(int id) {
        limpiarMensajes();
        ApiResponse r = bloqueService.eliminarBloque(id);
        if (r.isOk()) { ok("Bloque eliminado."); if (idBloqueSel == id) { idBloqueSel = 0; aulasJson = "[]"; } cargarBloques(); }
        else error("No se puede eliminar. Puede tener aulas asociadas.");
        return null;
    }

    public String seleccionarBloque(int id) {
        idBloqueSel = id;
        aulasJson = bloqueService.listarAulas(id);
        return null;
    }

    // ================================================================
    //  AULAS
    // ================================================================
    public String crearAula() {
        limpiarMensajes();
        if (idBloqueSel == 0)  return error("Selecciona un bloque.");
        if (isBlank(auNombre)) return error("Ingresa el nombre del aula.");
        ApiResponse r = bloqueService.crearAula(idBloqueSel, auNombre, auCapacidad, auTipo);
        if (r.isOk()) { ok("Aula creada."); auNombre = ""; auCapacidad = ""; aulasJson = bloqueService.listarAulas(idBloqueSel); }
        else error("Error al crear aula: " + r.getBody());
        return null;
    }

    public String eliminarAula(int id) {
        limpiarMensajes();
        ApiResponse r = bloqueService.eliminarAula(id);
        if (r.isOk()) { ok("Aula eliminada."); aulasJson = bloqueService.listarAulas(idBloqueSel); }
        else error("No se puede eliminar el aula.");
        return null;
    }

    // ================================================================
    //  FRANJAS HORARIAS
    // ================================================================
    public void cargarFranjas() { franjasJson = franjaService.listarTodas(); }

    public String crearFranja() {
        limpiarMensajes();
        if (isBlank(frHoraInicio) || isBlank(frHoraFin)) return error("Ingresa la hora de inicio y fin.");
        ApiResponse r = franjaService.crear(frHoraInicio, frHoraFin, frDia);
        if (r.isOk()) { ok("Franja horaria creada."); frHoraInicio = ""; frHoraFin = ""; cargarFranjas(); }
        else error("Error al crear franja: " + r.getBody());
        return null;
    }

    public String eliminarFranja(int id) {
        limpiarMensajes();
        ApiResponse r = franjaService.eliminar(id);
        if (r.isOk()) { ok("Franja eliminada."); cargarFranjas(); }
        else error("No se puede eliminar la franja.");
        return null;
    }

    // ================================================================
    //  HORARIOS
    // ================================================================
    public void cargarHorarios() { horariosJson = horarioService.listarHorarios(); }

    public String generarHorario() {
        limpiarMensajes();
        ApiResponse r = horarioService.generarHorario(idFacultadGenerar);
        if (r.isOk()) { ok("Horario generado exitosamente."); cargarHorarios(); }
        else error("No se pudo generar el horario: " + r.getBody());
        return null;
    }

    // ================================================================
    //  Helpers
    // ================================================================
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
    public String getBloquesJson()              { return bloquesJson; }
    public String getAulasJson()                { return aulasJson; }
    public String getFranjasJson()              { return franjasJson; }
    public String getHorariosJson()             { return horariosJson; }

    public int    getIdBloqueSel()              { return idBloqueSel; }
    public void   setIdBloqueSel(int v)         { idBloqueSel = v; }
    public int    getIdFacultadGenerar()        { return idFacultadGenerar; }
    public void   setIdFacultadGenerar(int v)   { idFacultadGenerar = v; }

    public String getBNombre()                  { return bNombre; }
    public void   setBNombre(String v)          { bNombre = v; }
    public String getAuNombre()                 { return auNombre; }
    public void   setAuNombre(String v)         { auNombre = v; }
    public String getAuCapacidad()              { return auCapacidad; }
    public void   setAuCapacidad(String v)      { auCapacidad = v; }
    public String getAuTipo()                   { return auTipo; }
    public void   setAuTipo(String v)           { auTipo = v; }

    public String getFrHoraInicio()             { return frHoraInicio; }
    public void   setFrHoraInicio(String v)     { frHoraInicio = v; }
    public String getFrHoraFin()                { return frHoraFin; }
    public void   setFrHoraFin(String v)        { frHoraFin = v; }
    public String getFrDia()                    { return frDia; }
    public void   setFrDia(String v)            { frDia = v; }

    public String getMensajeError()             { return mensajeError; }
    public void   setMensajeError(String v)     { mensajeError = v; }
    public String getMensajeOk()                { return mensajeOk; }
    public void   setMensajeOk(String v)        { mensajeOk = v; }
}
