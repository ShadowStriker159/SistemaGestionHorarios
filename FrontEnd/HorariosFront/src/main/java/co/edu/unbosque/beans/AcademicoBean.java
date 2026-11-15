package co.edu.unbosque.beans;

import co.edu.unbosque.service.FacultadService;
import co.edu.unbosque.service.ApiClient.ApiResponse;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * AcademicoBean
 * Gestiona desde la vista de secretaria:
 *   - Facultades (crear, listar, eliminar)
 *   - Áreas (por facultad)
 *   - Carreras (por facultad)
 *   - Planes de Estudio (por carrera)
 *   - Cursos (por plan)
 *   - Grupos (por curso)
 *
 * Toda la comunicación va a través de FacultadService → ApiClient → Backend.
 */
@Named("academicoBean")
@ViewScoped
public class AcademicoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private FacultadService facultadService;

    // ---- JSONs devueltos por el backend (se pasan al JS de la vista) ----
    private String facultadesJson = "[]";
    private String areasJson      = "[]";
    private String carrerasJson   = "[]";
    private String planesJson     = "[]";
    private String cursosJson     = "[]";
    private String gruposJson     = "[]";
    private String docentesJson   = "[]";

    // ---- Selección actual ----
    private int    idFacultadSel = 0;
    private int    idCarreraSel  = 0;
    private int    idPlanSel     = 0;
    private int    idCursoSel    = 0;

    // ---- Formulario Facultad ----
    private String fNombre = "";

    // ---- Formulario Área ----
    private String aNombre = "";

    // ---- Formulario Carrera ----
    private String cNombre = "";

    // ---- Formulario Plan ----
    private String pNombre = "";
    private String pAnio   = "";

    // ---- Formulario Curso ----
    private String cuNombre   = "";
    private String cuCreditos = "";

    // ---- Formulario Grupo ----
    private String gNombre    = "";
    private String gIdDocente = "";

    // ---- Mensajes ----
    private String mensajeError = "";
    private String mensajeOk    = "";

    @PostConstruct
    public void init() {
        facultadService = new FacultadService();
        cargarFacultades();
        // Cargar docentes para el select de grupos
        docentesJson = new co.edu.unbosque.service.DocenteService().listarTodos();
    }

    // ================================================================
    //  FACULTADES
    // ================================================================
    public void cargarFacultades() {
        facultadesJson = facultadService.listarFacultades();
    }

    public String crearFacultad() {
        limpiarMensajes();
        if (isBlank(fNombre)) return error("Ingresa el nombre de la facultad.");
        ApiResponse r = facultadService.crearFacultad(fNombre);
        if (r.isOk()) { ok("Facultad creada."); fNombre = ""; cargarFacultades(); }
        else if (r.isConflict()) error("Ya existe una facultad con ese nombre.");
        else error("Error: " + r.getBody());
        return null;
    }

    public String eliminarFacultad(int id) {
        limpiarMensajes();
        ApiResponse r = facultadService.eliminarFacultad(id);
        if (r.isOk()) { ok("Facultad eliminada."); if (idFacultadSel == id) idFacultadSel = 0; cargarFacultades(); }
        else error("No se puede eliminar. Puede tener carreras asociadas.");
        return null;
    }

    public String seleccionarFacultad(int id) {
        idFacultadSel = id;
        areasJson   = facultadService.listarAreas(id);
        carrerasJson = facultadService.listarCarreras(id);
        idCarreraSel = 0; planesJson = "[]"; idPlanSel = 0; cursosJson = "[]";
        return null;
    }

    // ================================================================
    //  ÁREAS
    // ================================================================
    public String crearArea() {
        limpiarMensajes();
        if (idFacultadSel == 0) return error("Selecciona primero una facultad.");
        if (isBlank(aNombre))  return error("Ingresa el nombre del área.");
        ApiResponse r = facultadService.crearArea(idFacultadSel, aNombre);
        if (r.isOk()) { ok("Área creada."); aNombre = ""; areasJson = facultadService.listarAreas(idFacultadSel); }
        else error("Error al crear área: " + r.getBody());
        return null;
    }

    public String eliminarArea(int id) {
        limpiarMensajes();
        ApiResponse r = facultadService.eliminarArea(id);
        if (r.isOk()) { ok("Área eliminada."); areasJson = facultadService.listarAreas(idFacultadSel); }
        else error("No se puede eliminar el área.");
        return null;
    }

    // ================================================================
    //  CARRERAS
    // ================================================================
    public String crearCarrera() {
        limpiarMensajes();
        if (idFacultadSel == 0) return error("Selecciona primero una facultad.");
        if (isBlank(cNombre))   return error("Ingresa el nombre de la carrera.");
        ApiResponse r = facultadService.crearCarrera(idFacultadSel, cNombre);
        if (r.isOk()) { ok("Carrera creada."); cNombre = ""; carrerasJson = facultadService.listarCarreras(idFacultadSel); }
        else error("Error al crear carrera: " + r.getBody());
        return null;
    }

    public String eliminarCarrera(int id) {
        limpiarMensajes();
        ApiResponse r = facultadService.eliminarCarrera(id);
        if (r.isOk()) { ok("Carrera eliminada."); carrerasJson = facultadService.listarCarreras(idFacultadSel); }
        else error("No se puede eliminar. Puede tener planes asociados.");
        return null;
    }

    public String seleccionarCarrera(int id) {
        idCarreraSel = id;
        planesJson = facultadService.listarPlanes(id);
        idPlanSel = 0; cursosJson = "[]";
        return null;
    }

    // ================================================================
    //  PLANES DE ESTUDIO
    // ================================================================
    public String crearPlan() {
        limpiarMensajes();
        if (idCarreraSel == 0) return error("Selecciona primero una carrera.");
        if (isBlank(pNombre))  return error("Ingresa el nombre del plan.");
        ApiResponse r = facultadService.crearPlan(idCarreraSel, pNombre, pAnio);
        if (r.isOk()) { ok("Plan de estudios creado."); pNombre = ""; pAnio = ""; planesJson = facultadService.listarPlanes(idCarreraSel); }
        else error("Error al crear plan: " + r.getBody());
        return null;
    }

    public String eliminarPlan(int id) {
        limpiarMensajes();
        ApiResponse r = facultadService.eliminarPlan(id);
        if (r.isOk()) { ok("Plan eliminado."); planesJson = facultadService.listarPlanes(idCarreraSel); }
        else error("No se puede eliminar. Puede tener cursos asociados.");
        return null;
    }

    public String seleccionarPlan(int id) {
        idPlanSel = id;
        cursosJson = facultadService.listarCursosPorPlan(id);
        idCursoSel = 0; gruposJson = "[]";
        return null;
    }

    // ================================================================
    //  CURSOS
    // ================================================================
    public String crearCurso() {
        limpiarMensajes();
        if (idPlanSel == 0)    return error("Selecciona primero un plan de estudios.");
        if (isBlank(cuNombre)) return error("Ingresa el nombre del curso.");
        ApiResponse r = facultadService.crearCurso(idPlanSel, cuNombre, cuCreditos);
        if (r.isOk()) { ok("Curso creado."); cuNombre = ""; cuCreditos = ""; cursosJson = facultadService.listarCursosPorPlan(idPlanSel); }
        else error("Error al crear curso: " + r.getBody());
        return null;
    }

    public String eliminarCurso(int id) {
        limpiarMensajes();
        ApiResponse r = facultadService.eliminarCurso(id);
        if (r.isOk()) { ok("Curso eliminado."); cursosJson = facultadService.listarCursosPorPlan(idPlanSel); }
        else error("No se puede eliminar. Puede tener grupos asociados.");
        return null;
    }

    public String seleccionarCurso(int id) {
        idCursoSel = id;
        gruposJson = facultadService.listarGruposPorCurso(id);
        return null;
    }

    // ================================================================
    //  GRUPOS
    // ================================================================
    public String crearGrupo() {
        limpiarMensajes();
        if (idCursoSel == 0)    return error("Selecciona primero un curso.");
        if (isBlank(gNombre))   return error("Ingresa el nombre del grupo.");
        if (isBlank(gIdDocente)) return error("Selecciona un docente.");
        ApiResponse r = facultadService.crearGrupo(idCursoSel, gIdDocente, gNombre);
        if (r.isOk()) { ok("Grupo creado."); gNombre = ""; gIdDocente = ""; gruposJson = facultadService.listarGruposPorCurso(idCursoSel); }
        else error("Error al crear grupo: " + r.getBody());
        return null;
    }

    public String eliminarGrupo(int id) {
        limpiarMensajes();
        ApiResponse r = facultadService.eliminarGrupo(id);
        if (r.isOk()) { ok("Grupo eliminado."); gruposJson = facultadService.listarGruposPorCurso(idCursoSel); }
        else error("No se puede eliminar.");
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
    public String getFacultadesJson()           { return facultadesJson; }
    public String getAreasJson()                { return areasJson; }
    public String getCarrerasJson()             { return carrerasJson; }
    public String getPlanesJson()               { return planesJson; }
    public String getCursosJson()               { return cursosJson; }
    public String getGruposJson()               { return gruposJson; }
    public String getDocentesJson()             { return docentesJson; }

    public int    getIdFacultadSel()            { return idFacultadSel; }
    public void   setIdFacultadSel(int v)       { idFacultadSel = v; }
    public int    getIdCarreraSel()             { return idCarreraSel; }
    public void   setIdCarreraSel(int v)        { idCarreraSel = v; }
    public int    getIdPlanSel()                { return idPlanSel; }
    public void   setIdPlanSel(int v)           { idPlanSel = v; }
    public int    getIdCursoSel()               { return idCursoSel; }
    public void   setIdCursoSel(int v)          { idCursoSel = v; }

    public String getFNombre()                  { return fNombre; }
    public void   setFNombre(String v)          { fNombre = v; }
    public String getANombre()                  { return aNombre; }
    public void   setANombre(String v)          { aNombre = v; }
    public String getCNombre()                  { return cNombre; }
    public void   setCNombre(String v)          { cNombre = v; }
    public String getPNombre()                  { return pNombre; }
    public void   setPNombre(String v)          { pNombre = v; }
    public String getPAnio()                    { return pAnio; }
    public void   setPAnio(String v)            { pAnio = v; }
    public String getCuNombre()                 { return cuNombre; }
    public void   setCuNombre(String v)         { cuNombre = v; }
    public String getCuCreditos()               { return cuCreditos; }
    public void   setCuCreditos(String v)       { cuCreditos = v; }
    public String getGNombre()                  { return gNombre; }
    public void   setGNombre(String v)          { gNombre = v; }
    public String getGIdDocente()               { return gIdDocente; }
    public void   setGIdDocente(String v)       { gIdDocente = v; }

    public String getMensajeError()             { return mensajeError; }
    public void   setMensajeError(String v)     { mensajeError = v; }
    public String getMensajeOk()                { return mensajeOk; }
    public void   setMensajeOk(String v)        { mensajeOk = v; }
}
