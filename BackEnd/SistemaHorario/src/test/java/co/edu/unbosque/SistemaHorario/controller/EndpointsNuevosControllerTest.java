package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.dto.DocenteDTO;
import co.edu.unbosque.SistemaHorario.model.dto.EstudianteDTO;
import co.edu.unbosque.SistemaHorario.model.dto.GrupoDTO;
import co.edu.unbosque.SistemaHorario.model.exception.DocenteNoEncontradoException;
import co.edu.unbosque.SistemaHorario.model.exception.DocenteRepetidoException;
import co.edu.unbosque.SistemaHorario.model.exception.EstudianteNoEncontradoException;
import co.edu.unbosque.SistemaHorario.model.exception.PlanEstudiosNoEncontradoException;
import co.edu.unbosque.SistemaHorario.services.CursoService;
import co.edu.unbosque.SistemaHorario.services.DocenteService;
import co.edu.unbosque.SistemaHorario.services.EstudianteService;
import co.edu.unbosque.SistemaHorario.services.GrupoService;
import co.edu.unbosque.SistemaHorario.services.PlanEstudiosService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PRUEBA DE COMPONENTE: capa web de los endpoints nuevos (rutas, códigos HTTP, JSON y manejo de
 * excepciones) con los servicios simulados. No levanta Spring completo ni base de datos.
 */
class EndpointsNuevosControllerTest {

    private DocenteService docenteService;
    private EstudianteService estudianteService;
    private GrupoService grupoService;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        docenteService = mock(DocenteService.class);
        estudianteService = mock(EstudianteService.class);
        grupoService = mock(GrupoService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(
                        new DocenteController(docenteService, grupoService),
                        new EstudianteController(estudianteService),
                        new PlanEstudiosController(mock(PlanEstudiosService.class), mock(CursoService.class),
                                grupoService))
                .setControllerAdvice(new ManejadorExcepciones())
                .build();
    }

    private static final String JSON_DOCENTE = "{\"nombre\":\"Ana\",\"apellido\":\"Ruiz\",\"correo\":\"a@u.edu\","
            + "\"escalafon\":\"PROFESOR_TITULAR\",\"estadoDocente\":\"ACTIVO\",\"tipoVinculacion\":\"TC\"}";
    private static final String JSON_ESTUDIANTE = "{\"nombre\":\"Luis\",\"apellido\":\"Mora\",\"correo\":\"l@u.edu\","
            + "\"semestreCursa\":3,\"creditosAprobados\":10,\"estadoEstudiante\":\"ACTIVO\"}";

    private String cuerpo(org.springframework.test.web.servlet.ResultActions r) throws Exception {
        return r.andReturn().getResponse().getContentAsString();
    }

    // ---- PUT /docentes/{id} ----

    @Test
    void putDocenteResponde200ConElDto() throws Exception {
        DocenteDTO salida = new DocenteDTO();
        salida.setId("D1");
        salida.setNombre("Ana");
        when(docenteService.actualizar(eq("D1"), any(DocenteDTO.class))).thenReturn(salida);

        var r = mvc.perform(put("/api/horarios/docentes/D1").contentType(MediaType.APPLICATION_JSON)
                .content(JSON_DOCENTE)).andExpect(status().isOk());

        assertTrue(cuerpo(r).contains("\"id\":\"D1\""));
    }

    @Test
    void putDocenteDeserializaElCuerpoYUsaElIdDeLaRuta() throws Exception {
        when(docenteService.actualizar(any(), any())).thenReturn(new DocenteDTO());
        mvc.perform(put("/api/horarios/docentes/D1").contentType(MediaType.APPLICATION_JSON).content(JSON_DOCENTE))
                .andExpect(status().isOk());

        ArgumentCaptor<DocenteDTO> dto = ArgumentCaptor.forClass(DocenteDTO.class);
        verify(docenteService).actualizar(eq("D1"), dto.capture());
        assertEquals("a@u.edu", dto.getValue().getCorreo());
        assertEquals("PROFESOR_TITULAR", dto.getValue().getEscalafon());
        assertNull(dto.getValue().getPassword());
    }

    @Test
    void putDocenteInexistenteResponde404ConElMensaje() throws Exception {
        when(docenteService.actualizar(any(), any()))
                .thenThrow(new DocenteNoEncontradoException("No existe el docente con id: D9"));
        var r = mvc.perform(put("/api/horarios/docentes/D9").contentType(MediaType.APPLICATION_JSON)
                .content(JSON_DOCENTE)).andExpect(status().isNotFound());
        assertEquals("No existe el docente con id: D9", cuerpo(r));
    }

    @Test
    void putDocenteConCorreoRepetidoResponde409() throws Exception {
        when(docenteService.actualizar(any(), any())).thenThrow(new DocenteRepetidoException("correo repetido"));
        mvc.perform(put("/api/horarios/docentes/D1").contentType(MediaType.APPLICATION_JSON).content(JSON_DOCENTE))
                .andExpect(status().isConflict());
    }

    @Test
    void putDocenteConDatosInvalidosResponde400() throws Exception {
        when(docenteService.actualizar(any(), any())).thenThrow(new IllegalArgumentException("campo inválido"));
        mvc.perform(put("/api/horarios/docentes/D1").contentType(MediaType.APPLICATION_JSON).content(JSON_DOCENTE))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putDocenteSinCuerpoResponde400() throws Exception {
        mvc.perform(put("/api/horarios/docentes/D1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(docenteService);
    }

    // ---- GET/PUT /estudiantes/{id} ----

    @Test
    void getEstudianteResponde200() throws Exception {
        EstudianteDTO salida = new EstudianteDTO();
        salida.setId("E1");
        salida.setIdPlanEstudios(10);
        when(estudianteService.obtener("E1")).thenReturn(salida);

        var r = mvc.perform(get("/api/horarios/estudiantes/E1")).andExpect(status().isOk());

        String json = cuerpo(r);
        assertTrue(json.contains("\"id\":\"E1\""));
        assertTrue(json.contains("\"idPlanEstudios\":10"));
    }

    @Test
    void getEstudianteInexistenteResponde404() throws Exception {
        when(estudianteService.obtener("X")).thenThrow(new EstudianteNoEncontradoException("No existe"));
        mvc.perform(get("/api/horarios/estudiantes/X")).andExpect(status().isNotFound());
    }

    @Test
    void putEstudianteResponde200YPasaElDto() throws Exception {
        when(estudianteService.actualizar(any(), any())).thenReturn(new EstudianteDTO());
        mvc.perform(put("/api/horarios/estudiantes/E1").contentType(MediaType.APPLICATION_JSON)
                .content(JSON_ESTUDIANTE)).andExpect(status().isOk());

        ArgumentCaptor<EstudianteDTO> dto = ArgumentCaptor.forClass(EstudianteDTO.class);
        verify(estudianteService).actualizar(eq("E1"), dto.capture());
        assertEquals(3, dto.getValue().getSemestreCursa());
    }

    @Test
    void putEstudianteConPlanInexistenteResponde404() throws Exception {
        when(estudianteService.actualizar(any(), any()))
                .thenThrow(new PlanEstudiosNoEncontradoException("No existe el plan"));
        mvc.perform(put("/api/horarios/estudiantes/E1").contentType(MediaType.APPLICATION_JSON)
                .content(JSON_ESTUDIANTE)).andExpect(status().isNotFound());
    }

    @Test
    void rutaPutPlanExistenteNoSeRompe() throws Exception {
        when(estudianteService.asignarPlan("E1", 10)).thenReturn(new EstudianteDTO());
        mvc.perform(put("/api/horarios/estudiantes/E1/plan/10")).andExpect(status().isOk());
        verify(estudianteService).asignarPlan("E1", 10);
    }

    // ---- GET /planes/{id}/grupos ----

    @Test
    void getGruposDePlanResponde200ConLista() throws Exception {
        GrupoDTO g = new GrupoDTO();
        g.setIdGrupo(4);
        g.setEstadoGrupo("ABIERTO");
        when(grupoService.listarPorPlan(10)).thenReturn(List.of(g));

        var r = mvc.perform(get("/api/horarios/planes/10/grupos")).andExpect(status().isOk());

        String json = cuerpo(r);
        assertTrue(json.startsWith("["));
        assertTrue(json.contains("\"idGrupo\":4"));
    }

    @Test
    void getGruposDePlanVacioResponde200ConArregloVacio() throws Exception {
        when(grupoService.listarPorPlan(10)).thenReturn(List.of());
        var r = mvc.perform(get("/api/horarios/planes/10/grupos")).andExpect(status().isOk());
        assertEquals("[]", cuerpo(r));
    }

    @Test
    void getGruposDePlanInexistenteResponde404() throws Exception {
        when(grupoService.listarPorPlan(99)).thenThrow(new PlanEstudiosNoEncontradoException("No existe el plan"));
        mvc.perform(get("/api/horarios/planes/99/grupos")).andExpect(status().isNotFound());
    }

    @Test
    void idDePlanNoNumericoResponde400() throws Exception {
        mvc.perform(get("/api/horarios/planes/abc/grupos")).andExpect(status().isBadRequest());
    }
}
