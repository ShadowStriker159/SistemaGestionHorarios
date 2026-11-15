package co.edu.unbosque.beans;

import co.edu.unbosque.model.CursoDTO;
import co.edu.unbosque.model.GrupoDTO;
import co.edu.unbosque.service.ApiClient;
import co.edu.unbosque.service.EstudianteService;
import co.edu.unbosque.service.FacultadService;
import co.edu.unbosque.service.GrupoService;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * PRUEBA DE COMPONENTE: EstudiantePortalBean + servicios reales del front, con un ApiClient simulado que
 * responde el mismo JSON que el back. Verifica la carga en 3 peticiones y el reparto de grupos por curso.
 */
public class EstudiantePortalBeanTest {

	/** ApiClient falso: responde JSON por ruta y registra las llamadas. */
	static class ApiFalso extends ApiClient {
		final Map<String, String> respuestas = new HashMap<>();
		final List<String> llamadas = new ArrayList<>();

		@Override
		public String get(String path) {
			llamadas.add(path);
			String r = respuestas.get(path);
			if (r == null) {
				throw new ApiException(new ApiResponse(404, "No existe: " + path));
			}
			return r;
		}
	}

	private static final String ESTUDIANTE = "{\"id\":\"E1\",\"nombre\":\"Luis\",\"apellido\":\"Mora\","
			+ "\"semestreCursa\":2,\"estadoEstudiante\":\"ACTIVO\",\"creditosAprobados\":10,"
			+ "\"idPlanEstudios\":10,\"idCarrera\":3,\"nombreCarrera\":\"Sistemas\"}";
	private static final String CURSOS = "[{\"idCurso\":1,\"nombreCurso\":\"Cálculo\",\"creditos\":3,\"semestre\":1},"
			+ "{\"idCurso\":2,\"nombreCurso\":\"Física\",\"creditos\":4,\"semestre\":1},"
			+ "{\"idCurso\":3,\"nombreCurso\":\"Sin grupos\",\"creditos\":2,\"semestre\":1}]";
	private static final String GRUPOS = "["
			+ "{\"idGrupo\":11,\"estadoGrupo\":\"ABIERTO\",\"cupoMaximo\":30,\"cupoMinimo\":5,"
			+ "\"curso\":{\"idCurso\":1,\"nombreCurso\":\"Cálculo\",\"creditos\":3},\"estudiantes\":[{\"id\":\"E1\"}]},"
			+ "{\"idGrupo\":12,\"estadoGrupo\":\"ABIERTO\",\"cupoMaximo\":30,\"cupoMinimo\":5,"
			+ "\"curso\":{\"idCurso\":1,\"nombreCurso\":\"Cálculo\",\"creditos\":3},\"estudiantes\":[]},"
			+ "{\"idGrupo\":13,\"estadoGrupo\":\"CERRADO\",\"cupoMaximo\":20,\"cupoMinimo\":5,"
			+ "\"curso\":{\"idCurso\":2,\"nombreCurso\":\"Física\",\"creditos\":4},\"estudiantes\":[{\"id\":\"E9\"}]}]";

	private ApiFalso api;
	private EstudiantePortalBean bean;

	private static void inyectar(Object destino, String campo, Object valor) throws Exception {
		Field f = destino.getClass().getDeclaredField(campo);
		f.setAccessible(true);
		f.set(destino, valor);
	}

	@Before
	public void preparar() throws Exception {
		api = new ApiFalso();
		api.respuestas.put("/estudiantes/E1", ESTUDIANTE);
		api.respuestas.put("/planes/10/cursos", CURSOS);
		api.respuestas.put("/planes/10/grupos", GRUPOS);

		EstudianteService estudianteService = new EstudianteService();
		inyectar(estudianteService, "api", api);
		FacultadService facultadService = new FacultadService();
		inyectar(facultadService, "api", api);

		bean = new EstudiantePortalBean();
		inyectar(bean, "estudianteService", estudianteService);
		inyectar(bean, "facultadService", facultadService);
		inyectar(bean, "grupoService", new GrupoService());
		inyectar(bean, "idEstudiante", "E1");
	}

	@Test
	public void cargaConExactamenteTresPeticiones() {
		bean.cargarDatos();
		assertEquals("", bean.getMensajeError());
		assertEquals(List.of("/estudiantes/E1", "/planes/10/cursos", "/planes/10/grupos"), api.llamadas);
	}

	@Test
	public void cargaLosDatosDelEstudiante() {
		bean.cargarDatos();
		assertEquals("Luis Mora", bean.getNombreCompleto());
		assertEquals("LM", bean.getIniciales());
		assertEquals("Sistemas", bean.getEstudiante().getNombreCarrera());
	}

	@Test
	public void repartePorCursoLosGruposDelPlan() {
		bean.cargarDatos();
		List<CursoDTO> cursos = bean.getCursosCarrera();
		assertEquals(3, cursos.size());
		assertEquals(2, cursos.get(0).getGrupos().size());
		assertEquals(1, cursos.get(1).getGrupos().size());
		assertTrue("Un curso sin grupos debe quedar con lista vacía, no nula", cursos.get(2).getGrupos().isEmpty());
	}

	@Test
	public void detectaLosGruposEnLosQueEstaInscrito() {
		bean.cargarDatos();
		List<GrupoDTO> inscritos = bean.getGruposInscritos();
		assertEquals(1, inscritos.size());
		assertEquals(Integer.valueOf(11), inscritos.get(0).getIdGrupo());
		assertEquals(3, bean.getCreditosInscritos());
	}

	@Test
	public void disponibilidadDeGruposRespetaEstadoYCupo() {
		bean.cargarDatos();
		GrupoDTO abierto = bean.getCursosCarrera().get(0).getGrupos().get(1);
		GrupoDTO cerrado = bean.getCursosCarrera().get(1).getGrupos().get(0);
		assertTrue(bean.grupoDisponible(abierto));
		assertFalse(bean.grupoDisponible(cerrado));
	}

	@Test
	public void recargarNoDuplicaLosInscritos() {
		bean.cargarDatos();
		bean.cargarDatos();
		assertEquals(1, bean.getGruposInscritos().size());
	}

	@Test
	public void estudianteSinPlanMuestraAvisoYNoPideCursosNiGrupos() {
		api.respuestas.put("/estudiantes/E1", "{\"id\":\"E1\",\"nombre\":\"Luis\",\"apellido\":\"Mora\"}");
		bean.cargarDatos();
		assertTrue(bean.getMensajeError().contains("plan de estudios"));
		assertTrue(bean.getCursosCarrera().isEmpty());
		assertEquals(List.of("/estudiantes/E1"), api.llamadas);
	}

	@Test
	public void errorDelBackSeMuestraSinRomperLaPagina() {
		api.respuestas.remove("/planes/10/grupos");
		bean.cargarDatos();
		assertTrue(bean.getMensajeError().startsWith("No se pudo cargar tu plan de estudios"));
		assertTrue(bean.getCursosCarrera().isEmpty());
		assertTrue(bean.getGruposInscritos().isEmpty());
	}

	@Test
	public void respuestaNulaDelBackSeReportaComoError() {
		api.respuestas.put("/planes/10/grupos", "null");
		bean.cargarDatos();
		assertFalse(bean.getMensajeError().isEmpty());
		assertTrue(bean.getCursosCarrera().isEmpty());
	}

	@Test
	public void estudianteNuloDelBackSeReportaSinExcepcion() {
		api.respuestas.put("/estudiantes/E1", "null");
		bean.cargarDatos();
		assertTrue(bean.getMensajeError().contains("estudiante nulo"));
	}
}
