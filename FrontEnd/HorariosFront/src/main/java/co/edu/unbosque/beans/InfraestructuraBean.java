package co.edu.unbosque.beans;

import co.edu.unbosque.model.Categoria;
import co.edu.unbosque.model.FacultadDTO;
import co.edu.unbosque.service.BloqueService;
import co.edu.unbosque.service.FacultadService;
import co.edu.unbosque.service.FranjaService;
import co.edu.unbosque.service.HorarioService;
import co.edu.unbosque.service.ApiClient;
import co.edu.unbosque.service.ApiClient.ApiResponse;
import com.google.gson.reflect.TypeToken;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Named("infraBean")
@ViewScoped
public class InfraestructuraBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private BloqueService bloqueService;

	@Inject
	private FranjaService franjaService;

	@Inject
	private HorarioService horarioService;

	@Inject
	private FacultadService facultadService;

	// Datos
	private String bloquesJson = "[]";
	private String aulasJson = "[]";
	private String franjasJson = "[]";
	private String horariosJson = "[]";

	// Seleccion
	private int idBloqueSel = 0;
	private int idBloqueEliminarSel = 0;
	private String numeroAulaSel = "";
	private int idFranjaSel = 0;
	private int idFacultadGenerar = 0;
	private String bloqueSeleccionado = "";
	private List<SelectItem> facultadesDisponibles = new ArrayList<>();

	// Formularios
	private String bNombre = "";
	private String auNumeroAula = "";
	private String auCapacidad = "";
	private String auTipo = "GRUPAL";

	private String frHoraInicio = "";
	private String frHoraFin = "";
	private String frDia = "LUNES";
	private String frCategoria = Categoria.DIA.name();

	// Mensajes
	private String mensajeError = "";
	private String mensajeOk = "";

	@PostConstruct
	public void init() {
		cargarBloques();
		cargarFranjas();
		cargarHorarios();
		cargarFacultades();
	}

	// BLOQUES
	public void cargarBloques() {
		bloquesJson = cargarJson(bloqueService::listarBloques);
	}

	public String crearBloque() {
		limpiarMensajes();
		if (isBlank(bNombre))
			return error("Ingresa el nombre del bloque.");
		ApiResponse r = bloqueService.crearBloque(bNombre.trim());
		if (r.isOk()) {
			ok("Bloque creado.");
			bNombre = "";
			cargarBloques();
		} else
			error("Error al crear bloque: " + r.getBody());
		return null;
	}

	public String eliminarBloque(int id) {
		limpiarMensajes();
		ApiResponse r = bloqueService.eliminarBloque(id);
		if (r.isOk()) {
			ok("Bloque eliminado.");
			if (idBloqueSel == id) {
				idBloqueSel = 0;
				bloqueSeleccionado = "";
				aulasJson = "[]";
			}
			cargarBloques();
		} else
			error("No se puede eliminar. Puede tener aulas asociadas.");
		return null;
	}

	public String seleccionarBloque(int id, String nombre) {
		idBloqueSel = id;
		bloqueSeleccionado = nombre == null ? "" : nombre;
		aulasJson = cargarJson(() -> bloqueService.listarAulas(id));
		return null;
	}

	// AULAS

	public String crearAula() {
		limpiarMensajes();
		if (idBloqueSel == 0)
			return error("Selecciona un bloque.");
		if (isBlank(auNumeroAula))
			return error("Ingresa el nombre o número del aula.");
		String numeroAula = auNumeroAula.trim();
		if (!numeroAula.matches("[\\p{L}\\p{N}-]{1,10}")) {
			return error("El nombre del aula debe tener hasta 10 letras, números o guiones; por ejemplo, A-101.");
		}
		Integer capacidad = numeroPositivo(auCapacidad, "capacidad");
		if (capacidad == null)
			return null;
		ApiResponse r = bloqueService.crearAula(idBloqueSel, numeroAula, capacidad, auTipo);
		if (r.isOk()) {
			ok("Aula creada.");
			auNumeroAula = "";
			auCapacidad = "";
			aulasJson = cargarJson(() -> bloqueService.listarAulas(idBloqueSel));
		} else
			error("Error al crear aula: " + r.getBody());
		return null;
	}

	public String eliminarAula(String numeroAula) {
		limpiarMensajes();
		if (idBloqueSel <= 0 || isBlank(numeroAula))
			return error("Selecciona un aula válida.");
		ApiResponse r = bloqueService.eliminarAula(idBloqueSel, numeroAula);
		if (r.isOk()) {
			ok("Aula eliminada.");
			aulasJson = cargarJson(() -> bloqueService.listarAulas(idBloqueSel));
		} else
			error("No se puede eliminar el aula.");
		return null;
	}

	// FRANJAS HORARIAS

	public void cargarFranjas() {
		franjasJson = cargarJson(franjaService::listarTodas);
	}

	public String crearFranja() {
		limpiarMensajes();
		if (isBlank(frHoraInicio) || isBlank(frHoraFin))
			return error("Ingresa la hora de inicio y fin.");
		try {
			DateTimeFormatter formatoHora = new DateTimeFormatterBuilder()
					.appendValue(ChronoField.HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE).appendLiteral(':')
					.appendValue(ChronoField.MINUTE_OF_HOUR, 2).toFormatter();
			LocalTime inicio = LocalTime.parse(frHoraInicio.trim(), formatoHora);
			LocalTime fin = LocalTime.parse(frHoraFin.trim(), formatoHora);
			if (!fin.isAfter(inicio)) {
				return error("La hora de fin debe ser posterior a la hora de inicio.");
			}
			ApiResponse r = franjaService.crear(inicio.toString(), fin.toString(), frDia, frCategoria);
			if (r.isOk()) {
				ok("Franja horaria creada.");
				frHoraInicio = "";
				frHoraFin = "";
				cargarFranjas();
			} else
				error("Error al crear franja: " + r.getBody());
		} catch (DateTimeParseException e) {
			return error("Ingresa las horas en formato H:mm o HH:mm, por ejemplo 7:00 o 07:00.");
		}
		return null;
	}

	public String eliminarFranja(int id) {
		limpiarMensajes();
		ApiResponse r = franjaService.eliminar(id);
		if (r.isOk()) {
			ok("Franja eliminada.");
			cargarFranjas();
		} else
			error("No se puede eliminar la franja.");
		return null;
	}

	// HORARIOS

	public void cargarHorarios() {
		horariosJson = cargarJson(() -> ApiClient.gson().toJson(horarioService.listarHorarios()));
	}

	public String generarHorario() {
		limpiarMensajes();
		if (idFacultadGenerar <= 0)
			return error("Selecciona la facultad para generar el horario.");
		ApiResponse r = horarioService.generarHorario(idFacultadGenerar);
		if (r.isOk()) {
			ok("Horario generado exitosamente.");
			cargarHorarios();
		} else
			error("No se pudo generar el horario: " + r.getBody());
		return null;
	}

	public void cargarFacultades() {
		try {
			Type tipo = new TypeToken<List<FacultadDTO>>() {
			}.getType();
			List<FacultadDTO> facultades = Objects.requireNonNull(
					ApiClient.gson().fromJson(facultadService.listarFacultades(), tipo),
					"El backend devolvió una lista de facultades nula");
			facultadesDisponibles = new ArrayList<>();
			for (FacultadDTO facultad : facultades) {
				if (facultad.getIdFacultad() == null || facultad.getNombreFacultad() == null) {
					continue;
				}
				String etiqueta = facultad.getNombreFacultad() + " (ID: " + facultad.getIdFacultad() + ")";
				facultadesDisponibles.add(new SelectItem(facultad.getIdFacultad(), etiqueta));
			}
		} catch (ApiClient.ApiException e) {
			facultadesDisponibles = new ArrayList<>();
			error("No se pudieron cargar las facultades: " + e.getMessage());
		}
	}

	// Helpers

	private void limpiarMensajes() {
		mensajeError = "";
		mensajeOk = "";
	}

	private String cargarJson(Supplier<String> consulta) {
		try {
			return consulta.get();
		} catch (ApiClient.ApiException e) {
			error("No se pudieron cargar los datos: " + e.getMessage());
			return "[]";
		}
	}

	private Integer numeroPositivo(String valor, String campo) {
		try {
			int numero = Integer.parseInt(valor);
			if (numero <= 0) {
				error("El valor de " + campo + " debe ser mayor que cero.");
				return null;
			}
			return numero;
		} catch (NumberFormatException e) {
			error("Ingresa un valor numérico válido para " + campo + ".");
			return null;
		}
	}

	private String error(String m) {
		mensajeError = m;
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", m));
		return null;
	}

	private void ok(String m) {
		mensajeOk = m;
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", m));
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	// Getters / Setters

	public String getBloquesJson() {
		return bloquesJson;
	}

	public String getAulasJson() {
		return aulasJson;
	}

	public String getFranjasJson() {
		return franjasJson;
	}

	public String getHorariosJson() {
		return horariosJson;
	}

	public int getIdBloqueSel() {
		return idBloqueSel;
	}

	public void setIdBloqueSel(int v) {
		idBloqueSel = v;
	}

	public int getIdBloqueEliminarSel() {
		return idBloqueEliminarSel;
	}

	public void setIdBloqueEliminarSel(int v) {
		idBloqueEliminarSel = v;
	}

	public String getBloqueSeleccionado() {
		return bloqueSeleccionado;
	}

	public void setBloqueSeleccionado(String v) {
		bloqueSeleccionado = v;
	}

	public String getNumeroAulaSel() {
		return numeroAulaSel;
	}

	public void setNumeroAulaSel(String v) {
		numeroAulaSel = v;
	}

	public int getIdFranjaSel() {
		return idFranjaSel;
	}

	public void setIdFranjaSel(int v) {
		idFranjaSel = v;
	}

	public int getIdFacultadGenerar() {
		return idFacultadGenerar;
	}

	public void setIdFacultadGenerar(int v) {
		idFacultadGenerar = v;
	}

	public List<SelectItem> getFacultadesDisponibles() {
		return facultadesDisponibles;
	}

	public String getbNombre() {
		return bNombre;
	}

	public void setbNombre(String v) {
		bNombre = v;
	}

	public String getAuNumeroAula() {
		return auNumeroAula;
	}

	public void setAuNumeroAula(String v) {
		auNumeroAula = v;
	}

	public String getAuCapacidad() {
		return auCapacidad;
	}

	public void setAuCapacidad(String v) {
		auCapacidad = v;
	}

	public String getAuTipo() {
		return auTipo;
	}

	public void setAuTipo(String v) {
		auTipo = v;
	}

	public String getFrHoraInicio() {
		return frHoraInicio;
	}

	public void setFrHoraInicio(String v) {
		frHoraInicio = v;
	}

	public String getFrHoraFin() {
		return frHoraFin;
	}

	public void setFrHoraFin(String v) {
		frHoraFin = v;
	}

	public String getFrDia() {
		return frDia;
	}

	public void setFrDia(String v) {
		frDia = v;
	}

	public String getFrCategoria() {
		return frCategoria;
	}

	public void setFrCategoria(String v) {
		frCategoria = v;
	}

	public String getMensajeError() {
		return mensajeError;
	}

	public void setMensajeError(String v) {
		mensajeError = v;
	}

	public String getMensajeOk() {
		return mensajeOk;
	}

	public void setMensajeOk(String v) {
		mensajeOk = v;
	}
}
