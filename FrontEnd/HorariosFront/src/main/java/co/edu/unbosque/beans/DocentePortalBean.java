package co.edu.unbosque.beans;

import co.edu.unbosque.model.DocenteDTO;
import co.edu.unbosque.model.EstadoFranja;
import co.edu.unbosque.model.FranjaHorariaDTO;
import co.edu.unbosque.model.GrupoDTO;
import co.edu.unbosque.model.UsuarioSesionDTO;
import co.edu.unbosque.service.ApiClient;
import co.edu.unbosque.service.ApiClient.ApiResponse;
import co.edu.unbosque.service.DocenteService;
import co.edu.unbosque.service.FranjaService;
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
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Named("docentePortalBean")
@ViewScoped
public class DocentePortalBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private DocenteService docenteService;

	@Inject
	private FranjaService franjaService;

	private String idDocente = "";
	private DocenteDTO docente;
	private List<GrupoDTO> grupos = new ArrayList<>();
	private List<FranjaHorariaDTO> franjasDisponibles = new ArrayList<>();
	private List<String> franjasSeleccionadas = new ArrayList<>();
	private String mensajeError = "";
	private String mensajeOk = "";

	@PostConstruct
	public void init() {
		Object usuario = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("usuarioSesion");
		if (!(usuario instanceof UsuarioSesionDTO sesion) || sesion.getId() == null || sesion.getId().isBlank()) {
			error("No se encontró una sesión válida de docente. Inicia sesión nuevamente.");
			return;
		}
		idDocente = sesion.getId();
		cargarFranjas();
		cargarPerfilYGrupos();
	}

	public void cargarPerfilYGrupos() {
		if (idDocente.isBlank()) {
			return;
		}
		try {
			docente = docenteService.obtener(idDocente);
			grupos = docenteService.listarGrupos(idDocente);
			cargarFranjasSeleccionadas();
		} catch (ApiClient.ApiException e) {
			if (e.getCode() == 405) {
				error("El backend activo está desactualizado y no permite consultar al docente por ID. "
						+ "Recompila y reinicia SistemaHorario para activar sus endpoints actuales.");
			} else {
				error("No se pudo cargar la información del docente: " + e.getMessage());
			}
		}
	}

	public void cargarFranjas() {
		try {
			Type tipo = new TypeToken<List<FranjaHorariaDTO>>() {
			}.getType();
			List<FranjaHorariaDTO> todas = ApiClient.gson().fromJson(franjaService.listarTodas(), tipo);
			if (todas == null) {
				throw new IllegalStateException("El backend devolvió una lista de franjas nula.");
			}
			franjasDisponibles = todas.stream().filter(franja -> franja.getEstadoFranja() == EstadoFranja.DISPONIBLE)
					.toList();
		} catch (ApiClient.ApiException e) {
			error("No se pudieron cargar las franjas horarias: " + e.getMessage());
		}
	}

	public String guardarDisponibilidad() {
		limpiarMensajes();
		if (idDocente.isBlank()) {
			return error("No se encontró la sesión del docente.");
		}
		if (franjasSeleccionadas == null || franjasSeleccionadas.isEmpty()) {
			return error("Selecciona al menos una franja antes de guardar tu disponibilidad.");
		}

		List<FranjaHorariaDTO> seleccionadas = new ArrayList<>();
		List<String> idsSolicitados = new ArrayList<>(franjasSeleccionadas);
		for (String idSeleccionado : idsSolicitados) {
			Integer idFranja;
			try {
				idFranja = Integer.valueOf(idSeleccionado);
			} catch (NumberFormatException e) {
				return error("La selección contiene una franja inválida. Recarga la página.");
			}
			FranjaHorariaDTO franja = franjasDisponibles.stream()
					.filter(opcion -> Objects.equals(opcion.getIdFranja(), idFranja)).findFirst().orElse(null);
			if (franja == null) {
				return error("Una de las franjas seleccionadas ya no está disponible. Recarga la página.");
			}
			seleccionadas.add(franja);
		}

		ApiResponse respuesta = docenteService.actualizarDisponibilidad(idDocente, seleccionadas);
		if (respuesta.isOk()) {
			cargarPerfilYGrupos();
			if (!mensajeError.isBlank()) {
				return null;
			}
			if (!franjasSeleccionadas.containsAll(idsSolicitados)) {
				return error("El backend aceptó la solicitud, pero no devolvió las franjas guardadas. "
						+ "Verifica la persistencia de disponibilidad en el backend.");
			}
			ok("Disponibilidad guardada y verificada.");
		} else if (respuesta.isNotFound()) {
			error("No se encontró el docente para actualizar la disponibilidad.");
		} else {
			error("No se pudo guardar la disponibilidad: " + respuesta.getBody());
		}
		return null;
	}

	private void cargarFranjasSeleccionadas() {
		franjasSeleccionadas = new ArrayList<>();
		if (docente == null || docente.getDisponibilidad() == null) {
			return;
		}
		for (FranjaHorariaDTO franja : docente.getDisponibilidad()) {
			if (franja.getIdFranja() != null && franjasDisponibles.stream()
					.anyMatch(opcion -> Objects.equals(opcion.getIdFranja(), franja.getIdFranja()))) {
				franjasSeleccionadas.add(String.valueOf(franja.getIdFranja()));
			}
		}
	}

	public List<SelectItem> getOpcionesFranjas() {
		List<SelectItem> items = new ArrayList<>();
		for (FranjaHorariaDTO franja : franjasDisponibles) {
			items.add(new SelectItem(String.valueOf(franja.getIdFranja()),
					etiquetaDia(franja.getDiaSemana()) + " · " + formatoHora(franja.getHoraInicio()) + "–"
							+ formatoHora(franja.getHoraFin()) + " · " + formatearEtiqueta(franja.getCategoria())));
		}
		return items;
	}

	public int getTotalEstudiantes() {
		return grupos.stream().filter(grupo -> grupo.getEstudiantes() != null)
				.mapToInt(grupo -> grupo.getEstudiantes().size()).sum();
	}

	public int getCursosDistintos() {
		Set<Integer> ids = new HashSet<>();
		for (GrupoDTO grupo : grupos) {
			if (grupo.getCurso() != null && grupo.getCurso().getIdCurso() != null) {
				ids.add(grupo.getCurso().getIdCurso());
			}
		}
		return ids.size();
	}

	public long getHorasSemanales() {
		long minutos = 0;
		for (GrupoDTO grupo : grupos) {
			if (grupo.getSecciones() == null) {
				continue;
			}
			for (var seccion : grupo.getSecciones()) {
				if (seccion.getFranjaHoraria() != null && seccion.getFranjaHoraria().getHoraInicio() != null
						&& seccion.getFranjaHoraria().getHoraFin() != null) {
					minutos += Duration.between(seccion.getFranjaHoraria().getHoraInicio(),
							seccion.getFranjaHoraria().getHoraFin()).toMinutes();
				}
			}
		}
		return minutos / 60;
	}

	public String etiquetaDia(Object valor) {
		return valor == null ? "Sin horario asignado" : formatearEtiqueta(valor);
	}

	public String formatearEtiqueta(Object valor) {
		if (valor == null) {
			return "No disponible";
		}
		String texto = valor instanceof Enum<?> enumValue ? enumValue.name() : valor.toString();
		if (texto.isBlank()) {
			return "No disponible";
		}
		String[] palabras = texto.toLowerCase().split("_");
		StringBuilder etiqueta = new StringBuilder();
		for (String palabra : palabras) {
			if (etiqueta.length() > 0) {
				etiqueta.append(' ');
			}
			etiqueta.append(Character.toUpperCase(palabra.charAt(0))).append(palabra.substring(1));
		}
		return etiqueta.toString();
	}

	public String formatoHora(LocalTime hora) {
		return hora == null ? "—" : hora.toString();
	}

	public String getNombreCompleto() {
		if (docente == null) {
			return "Docente";
		}
		return ((docente.getNombre() == null ? "" : docente.getNombre()) + " "
				+ (docente.getApellido() == null ? "" : docente.getApellido())).trim();
	}

	public String getIniciales() {
		if (docente == null) {
			return "D";
		}
		String nombre = docente.getNombre() == null ? "" : docente.getNombre().trim();
		String apellido = docente.getApellido() == null ? "" : docente.getApellido().trim();
		return (nombre.isEmpty() ? "" : nombre.substring(0, 1)) + (apellido.isEmpty() ? "" : apellido.substring(0, 1));
	}

	private String error(String mensaje) {
		mensajeError = mensaje;
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", mensaje));
		return null;
	}

	private void ok(String mensaje) {
		mensajeOk = mensaje;
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", mensaje));
	}

	private void limpiarMensajes() {
		mensajeError = "";
		mensajeOk = "";
	}

	public String getIdDocente() {
		return idDocente;
	}

	public DocenteDTO getDocente() {
		return docente;
	}

	public List<GrupoDTO> getGrupos() {
		return grupos;
	}

	public List<FranjaHorariaDTO> getFranjasDisponibles() {
		return franjasDisponibles;
	}

	public List<String> getFranjasSeleccionadas() {
		return franjasSeleccionadas;
	}

	public void setFranjasSeleccionadas(List<String> value) {
		franjasSeleccionadas = value == null ? new ArrayList<>() : value;
	}

	public String getMensajeError() {
		return mensajeError;
	}

	public String getMensajeOk() {
		return mensajeOk;
	}
}
