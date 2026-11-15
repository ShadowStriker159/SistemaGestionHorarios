package co.edu.unbosque.service;

import co.edu.unbosque.model.CursoDTO;
import com.google.gson.JsonParseException;
import org.junit.Assume;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/** PRUEBA UNITARIA: utilidades de ApiClient (sin red). */
public class ApiClientTest {

	@Test
	public void parseListConvierteUnArregloJsonEnListaTipada() {
		List<CursoDTO> cursos = ApiClient.parseList(
				"[{\"idCurso\":1,\"nombreCurso\":\"Cálculo\",\"creditos\":3},{\"idCurso\":2,\"nombreCurso\":\"Física\"}]",
				CursoDTO.class);
		assertEquals(2, cursos.size());
		assertEquals(Integer.valueOf(1), cursos.get(0).getIdCurso());
		assertEquals("Física", cursos.get(1).getNombreCurso());
	}

	@Test
	public void parseListDeArregloVacioDevuelveListaVacia() {
		assertTrue(ApiClient.parseList("[]", CursoDTO.class).isEmpty());
	}

	@Test(expected = JsonParseException.class)
	public void parseListConNullFallaConClaridad() {
		ApiClient.parseList("null", CursoDTO.class);
	}

	@Test(expected = JsonParseException.class)
	public void parseListConJsonInvalidoFalla() {
		ApiClient.parseList("{esto no es json", CursoDTO.class);
	}

	@Test
	public void parseListDeUnObjetoEnVezDeArregloFalla() {
		try {
			ApiClient.parseList("{\"idCurso\":1}", CursoDTO.class);
			fail("Debía fallar: un objeto no es una lista");
		} catch (JsonParseException esperado) {
			// ok
		}
	}

	@Test
	public void baseUrlPorDefectoApuntaAlBackLocal() {
		Assume.assumeTrue("Solo aplica sin configuración externa",
				System.getProperty("horarios.api.url") == null && System.getenv("HORARIOS_API_URL") == null);
		assertEquals("http://localhost:8087/api/horarios", ApiClient.BASE_URL);
	}

	@Test
	public void baseUrlNoTerminaEnBarra() {
		assertFalse(ApiClient.BASE_URL.endsWith("/"));
		assertTrue(ApiClient.BASE_URL.startsWith("http"));
	}

	@Test
	public void escEscapaComillasYBarras() {
		assertEquals("a\\\"b\\\\c", ApiClient.esc("a\"b\\c"));
		assertEquals("", ApiClient.esc(null));
	}
}
