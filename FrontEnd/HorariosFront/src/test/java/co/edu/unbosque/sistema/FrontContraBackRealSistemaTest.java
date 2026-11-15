package co.edu.unbosque.sistema;

import co.edu.unbosque.beans.EstudiantePortalBean;
import co.edu.unbosque.model.*;
import co.edu.unbosque.service.ApiClient;
import co.edu.unbosque.service.ApiClient.ApiResponse;
import co.edu.unbosque.service.DocenteService;
import co.edu.unbosque.service.EstudianteService;
import co.edu.unbosque.service.FacultadService;
import co.edu.unbosque.service.GrupoService;
import com.google.gson.JsonParser;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.*;

/**
 * PRUEBA DE SISTEMA (front + back reales).
 *
 * Usa los servicios y el bean REALES del front contra el back REAL encendido (sin simulaciones). Valida el
 * contrato completo: serialización de los DTO del front, rutas, códigos HTTP y lectura del JSON del back.
 *
 * NO corre por defecto: escribe datos en la base del back. Para ejecutarla:
 *   1) Levanta el back preferiblemente con H2 en memoria (no toca MySQL):
 *        mvnw spring-boot:run -Dspring-boot.run.useTestClasspath=true -Dspring-boot.run.profiles=test
 *   2) En el front:  mvn test -Dhorarios.system.tests=true
 *      (otra URL del back: -Dhorarios.api.url=http://host:puerto/api/horarios)
 * Si el back no responde, la clase se omite sin fallar. Al terminar borra lo que creó.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FrontContraBackRealSistemaTest {

	private static final ApiClient API = new ApiClient();
	private static final FacultadService FACULTAD = new FacultadService();
	private static final DocenteService DOCENTE = new DocenteService();
	private static final EstudianteService ESTUDIANTE = new EstudianteService();
	private static final GrupoService GRUPO = new GrupoService();

	private static final String SUF = String.valueOf(System.currentTimeMillis() % 100_000_000L);
	private static final String ID_DOCENTE = "ST" + SUF + "D";
	private static final String ID_ESTUDIANTE = "ST" + SUF + "E";

	private static int idFacultad, idCarrera, idPlan, idPlanVacio, idCurso, idGrupo;
	private static boolean estudianteCreado, docenteCreado;

	private static void inyectar(Object destino, String campo, Object valor) throws Exception {
		Field f = destino.getClass().getDeclaredField(campo);
		f.setAccessible(true);
		f.set(destino, valor);
	}

	@BeforeClass
	public static void prepararOOmitir() throws Exception {
		Assume.assumeTrue("Prueba de sistema desactivada (usa -Dhorarios.system.tests=true)",
				Boolean.getBoolean("horarios.system.tests"));
		inyectar(FACULTAD, "api", API);
		inyectar(DOCENTE, "api", API);
		inyectar(ESTUDIANTE, "api", API);
		inyectar(GRUPO, "api", API);
		try {
			API.get("/facultades");
		} catch (ApiClient.ApiException e) {
			Assume.assumeTrue("El back no responde en " + ApiClient.BASE_URL + ": " + e.getMessage(), false);
		}
	}

	private static int idDe(ApiResponse r, String campo) {
		assertTrue("Respuesta inesperada (" + r.getCode() + "): " + r.getBody(), r.isOk());
		return JsonParser.parseString(r.getBody()).getAsJsonObject().get(campo).getAsInt();
	}

	private static DocenteDTO docente() {
		DocenteDTO d = new DocenteDTO();
		d.setId(ID_DOCENTE);
		d.setNombre("Ana");
		d.setApellido("Sistema");
		d.setCorreo("st" + SUF + "d@test.local");
		d.setPassword("clave-d");
		d.setEscalafon(Escalafon.PROFESOR_TITULAR);
		d.setEstadoDocente(EstadoDocente.ACTIVO);
		d.setTipoVinculacion(TipoVinculacion.TC);
		return d;
	}

	private static EstudianteDTO estudiante() {
		EstudianteDTO e = new EstudianteDTO();
		e.setId(ID_ESTUDIANTE);
		e.setNombre("Luis");
		e.setApellido("Sistema");
		e.setCorreo("st" + SUF + "e@test.local");
		e.setPassword("clave-e");
		e.setSemestreCursa(1);
		e.setCreditosAprobados(0);
		e.setEstadoEstudiante(EstadoEstudiante.ACTIVO);
		e.setIdPlanEstudios(idPlan);
		e.setIdCarrera(idCarrera);
		return e;
	}

	@Test
	public void t01_secretariaCreaLaEstructuraAcademica() {
		idFacultad = idDe(FACULTAD.crearFacultad("Fac ST " + SUF), "idFacultad");
		idCarrera = idDe(FACULTAD.crearCarrera(idFacultad, "Carrera ST " + SUF), "idCarrera");
		idPlan = idDe(FACULTAD.crearPlan(idCarrera, "Plan ST " + SUF), "idPlanEstudios");
		idPlanVacio = idDe(FACULTAD.crearPlan(idCarrera, "Plan vacío ST " + SUF), "idPlanEstudios");
		idCurso = idDe(FACULTAD.crearCurso(idPlan, "Curso ST " + SUF, 3, 4, 1, "GRUPAL"), "idCurso");
	}

	@Test
	public void t02_secretariaCreaDocenteYGrupo() {
		ApiResponse r = DOCENTE.crear(docente());
		assertTrue(r.getBody(), r.isOk());
		docenteCreado = true;
		idGrupo = idDe(FACULTAD.crearGrupo(idCurso, ID_DOCENTE, 5, 30), "idGrupo");
	}

	@Test
	public void t03_secretariaCreaAlEstudiante() {
		ApiResponse r = ESTUDIANTE.crear(estudiante());
		assertTrue(r.getBody(), r.isOk());
		estudianteCreado = true;
	}

	@Test
	public void t04_elFrontLeeCorrectamenteElJsonDelBack() {
		EstudianteDTO e = ESTUDIANTE.obtener(ID_ESTUDIANTE);
		assertEquals(ID_ESTUDIANTE, e.getId());
		assertEquals(Integer.valueOf(idPlan), e.getIdPlanEstudios());
		assertEquals(Integer.valueOf(idCarrera), e.getIdCarrera());
		assertEquals("Carrera ST " + SUF, e.getNombreCarrera());
		assertEquals(EstadoEstudiante.ACTIVO, e.getEstadoEstudiante());
		assertNull("El back no debe devolver la contraseña", e.getPassword());

		List<GrupoDTO> grupos = ApiClient.parseList(FACULTAD.listarGruposPorPlan(idPlan), GrupoDTO.class);
		assertEquals(1, grupos.size());
		assertEquals(Integer.valueOf(idGrupo), grupos.get(0).getIdGrupo());
		assertEquals(Integer.valueOf(idCurso), grupos.get(0).getCurso().getIdCurso());
		assertEquals(EstadoGrupo.ABIERTO, grupos.get(0).getEstadoGrupo());
		assertEquals(ID_DOCENTE, grupos.get(0).getDocente().getId());

		assertTrue(ApiClient.parseList(FACULTAD.listarGruposPorPlan(idPlanVacio), GrupoDTO.class).isEmpty());
	}

	@Test
	public void t05_portalDelEstudianteCargaInscribeYRetira() throws Exception {
		EstudiantePortalBean bean = new EstudiantePortalBean();
		inyectar(bean, "estudianteService", ESTUDIANTE);
		inyectar(bean, "facultadService", FACULTAD);
		inyectar(bean, "grupoService", GRUPO);
		inyectar(bean, "idEstudiante", ID_ESTUDIANTE);

		bean.cargarDatos();
		assertEquals("", bean.getMensajeError());
		assertEquals(1, bean.getCursosCarrera().size());
		assertEquals(1, bean.getCursosCarrera().get(0).getGrupos().size());
		assertTrue(bean.getGruposInscritos().isEmpty());

		bean.inscribir(idGrupo);
		assertEquals(bean.getMensajeError(), "Inscripción realizada.", bean.getMensajeOk());
		assertEquals(1, bean.getGruposInscritos().size());
		assertEquals(3, bean.getCreditosInscritos());

		bean.inscribir(idGrupo);
		assertEquals("Ya estás inscrito en este grupo.", bean.getMensajeError());

		bean.retirar(idGrupo);
		assertEquals(bean.getMensajeError(), "Te retiraste del grupo.", bean.getMensajeOk());
		assertTrue(bean.getGruposInscritos().isEmpty());
	}

	@Test
	public void t06_editarEstudianteDesdeElFront() {
		EstudianteDTO e = ESTUDIANTE.obtener(ID_ESTUDIANTE);
		e.setSemestreCursa(4);
		e.setCreditosAprobados(60);
		e.setNombre("Luis Fernando");
		ApiResponse r = ESTUDIANTE.actualizar(ID_ESTUDIANTE, e);
		assertTrue("PUT /estudiantes/{id}: " + r.getCode() + " " + r.getBody(), r.isOk());

		EstudianteDTO despues = ESTUDIANTE.obtener(ID_ESTUDIANTE);
		assertEquals(Integer.valueOf(4), despues.getSemestreCursa());
		assertEquals(Integer.valueOf(60), despues.getCreditosAprobados());
		assertEquals("Luis Fernando", despues.getNombre());
		assertEquals(Integer.valueOf(idPlan), despues.getIdPlanEstudios());
	}

	@Test
	public void t07_editarDocenteDesdeElFrontSinTocarLaPassword() {
		DocenteDTO d = DOCENTE.obtener(ID_DOCENTE);
		d.setNombre("Ana María");
		d.setEstadoDocente(EstadoDocente.VACACIONES);
		d.setPassword(null);
		ApiResponse r = DOCENTE.actualizar(ID_DOCENTE, d);
		assertTrue("PUT /docentes/{id}: " + r.getCode() + " " + r.getBody(), r.isOk());

		DocenteDTO despues = DOCENTE.obtener(ID_DOCENTE);
		assertEquals("Ana María", despues.getNombre());
		assertEquals(EstadoDocente.VACACIONES, despues.getEstadoDocente());
	}

	@Test
	public void t08_correoDeOtraPersonaDa409() {
		DocenteDTO d = DOCENTE.obtener(ID_DOCENTE);
		d.setCorreo("st" + SUF + "e@test.local"); // correo del estudiante
		ApiResponse r = DOCENTE.actualizar(ID_DOCENTE, d);
		assertTrue("Esperaba 409, fue " + r.getCode() + ": " + r.getBody(), r.isConflict());
	}

	@Test
	public void t09_erroresDelBackLlegandoAlFront() {
		try {
			ESTUDIANTE.obtener("NOEXISTE" + SUF);
			fail("Debía lanzar ApiException 404");
		} catch (ApiClient.ApiException e) {
			assertEquals(404, e.getCode());
		}
		ApiResponse r = DOCENTE.eliminar(ID_DOCENTE);
		assertTrue("Esperaba 409, fue " + r.getCode() + ": " + r.getBody(), r.isConflict());
		assertTrue(r.getBody(), r.getBody().contains("grupos asignados"));
	}

	/** Limpieza en orden inverso de dependencias; nunca hace fallar la prueba. */
	@AfterClass
	public static void limpiar() {
		try {
			if (estudianteCreado) ESTUDIANTE.eliminar(ID_ESTUDIANTE);
			if (idGrupo > 0) FACULTAD.eliminarGrupo(idGrupo);
			if (docenteCreado) DOCENTE.eliminar(ID_DOCENTE);
			if (idCurso > 0) FACULTAD.eliminarCurso(idCurso);
			if (idPlan > 0) FACULTAD.eliminarPlan(idPlan);
			if (idPlanVacio > 0) FACULTAD.eliminarPlan(idPlanVacio);
			if (idCarrera > 0) FACULTAD.eliminarCarrera(idCarrera);
			if (idFacultad > 0) FACULTAD.eliminarFacultad(idFacultad);
		} catch (RuntimeException ignorada) {
			// limpieza de mejor esfuerzo
		}
	}
}
