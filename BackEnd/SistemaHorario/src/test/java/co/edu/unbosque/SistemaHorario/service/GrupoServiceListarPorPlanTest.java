package co.edu.unbosque.SistemaHorario.service;

import co.edu.unbosque.SistemaHorario.model.Curso;
import co.edu.unbosque.SistemaHorario.model.Docente;
import co.edu.unbosque.SistemaHorario.model.EstadoGrupo;
import co.edu.unbosque.SistemaHorario.model.Grupo;
import co.edu.unbosque.SistemaHorario.model.dto.GrupoDTO;
import co.edu.unbosque.SistemaHorario.model.exception.PlanEstudiosNoEncontradoException;
import co.edu.unbosque.SistemaHorario.respository.*;
import co.edu.unbosque.SistemaHorario.services.GrupoService;
import co.edu.unbosque.SistemaHorario.services.MapeadorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** PRUEBA UNITARIA: GrupoService.listarPorPlan. */
class GrupoServiceListarPorPlanTest {

    private GrupoRepository grupoRepository;
    private PlanEstudiosRepository planRepository;
    private GrupoService service;

    @BeforeEach
    void preparar() {
        grupoRepository = mock(GrupoRepository.class);
        planRepository = mock(PlanEstudiosRepository.class);
        service = new GrupoService(grupoRepository, mock(CursoRepository.class), mock(DocenteRepository.class),
                new MapeadorDTO(), planRepository);
    }

    @Test
    void planInexistenteLanzaNoEncontrado() {
        when(planRepository.existsById(5)).thenReturn(false);
        assertThrows(PlanEstudiosNoEncontradoException.class, () -> service.listarPorPlan(5));
        verify(grupoRepository, never()).findByPlan(any());
    }

    @Test
    void planSinGruposDevuelveListaVacia() {
        when(planRepository.existsById(5)).thenReturn(true);
        when(grupoRepository.findByPlan(5)).thenReturn(List.of());
        assertTrue(service.listarPorPlan(5).isEmpty());
    }

    @Test
    void mapeaLosGruposDelPlan() {
        Curso curso = new Curso();
        curso.setIdCurso(8);
        curso.setNombreCurso("Cálculo");
        Docente docente = new Docente();
        docente.setId("D1");
        Grupo grupo = new Grupo();
        grupo.setIdGrupo(1);
        grupo.setCurso(curso);
        grupo.setDocente(docente);
        grupo.setCupoMaximo(30);
        grupo.setCupoMinimo(5);
        grupo.setEstadoGrupo(EstadoGrupo.ABIERTO);
        when(planRepository.existsById(5)).thenReturn(true);
        when(grupoRepository.findByPlan(5)).thenReturn(List.of(grupo));

        List<GrupoDTO> r = service.listarPorPlan(5);

        assertEquals(1, r.size());
        assertEquals(1, r.get(0).getIdGrupo());
        assertEquals(8, r.get(0).getCurso().getIdCurso());
        assertEquals("ABIERTO", r.get(0).getEstadoGrupo());
    }
}
