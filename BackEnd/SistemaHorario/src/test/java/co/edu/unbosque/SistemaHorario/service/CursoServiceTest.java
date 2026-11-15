package co.edu.unbosque.SistemaHorario.service;

import co.edu.unbosque.SistemaHorario.model.*;
import co.edu.unbosque.SistemaHorario.model.algoritmo.IAlgoritmoHorario;
import co.edu.unbosque.SistemaHorario.model.dto.*;
import co.edu.unbosque.SistemaHorario.model.exception.*;
import co.edu.unbosque.SistemaHorario.respository.*;
import co.edu.unbosque.SistemaHorario.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** PRUEBA UNITARIA: CursoService (creación en un plan, prerrequisitos y eliminación). */
class CursoServiceTest {

    private CursoRepository cursoRepo;
    private PlanEstudiosRepository planRepo;
    private CursoService service;
    private PlanEstudios plan;

    @BeforeEach
    void preparar() {
        cursoRepo = mock(CursoRepository.class);
        planRepo = mock(PlanEstudiosRepository.class);
        service = new CursoService(cursoRepo, planRepo, new MapeadorDTO());
        plan = new PlanEstudios();
        plan.setIdPlanEstudios(10);
        plan.setCursos(new ArrayList<>());
        when(planRepo.findById(10)).thenReturn(Optional.of(plan));
        when(cursoRepo.save(any(Curso.class))).thenAnswer(i -> {
            Curso c = i.getArgument(0);
            if (c.getIdCurso() == null) c.setIdCurso(100);
            return c;
        });
    }

    private CursoDTO dto(String nombre) {
        CursoDTO d = new CursoDTO();
        d.setNombreCurso(nombre);
        d.setCreditos(3);
        d.setIntensidadHoraria(4);
        d.setSemestre(1);
        d.setAulaRequerida("SISTEMAS");
        return d;
    }

    @Test void crearAgregaElCursoAlPlanYLoGuarda() {
        CursoDTO r = service.crear(10, dto("  Cálculo "));
        assertEquals("Cálculo", r.getNombreCurso());
        assertEquals("SISTEMAS", r.getAulaRequerida());
        assertEquals(1, plan.getCursos().size());
        verify(planRepo).save(plan);
    }
    @Test void crearInicializaLaListaDeCursosSiEraNula() {
        plan.setCursos(null);
        service.crear(10, dto("A"));
        assertEquals(1, plan.getCursos().size());
    }
    @Test void crearPlanInexistente() {
        when(planRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(PlanEstudiosNoEncontradoException.class, () -> service.crear(9, dto("A")));
    }
    @Test void crearRepetidoEnElPlan() {
        when(cursoRepo.existsByNombreInPlan("A", plan)).thenReturn(true);
        assertThrows(CursoRepetidoException.class, () -> service.crear(10, dto("A")));
        verify(cursoRepo, never()).save(any());
    }
    @Test void crearValidaCamposNumericosYEnum() {
        CursoDTO d = dto("A"); d.setCreditos(0);
        assertThrows(IllegalArgumentException.class, () -> service.crear(10, d));
        CursoDTO e = dto("A"); e.setIntensidadHoraria(null);
        assertThrows(IllegalArgumentException.class, () -> service.crear(10, e));
        CursoDTO f = dto("A"); f.setSemestre(-1);
        assertThrows(IllegalArgumentException.class, () -> service.crear(10, f));
        CursoDTO g = dto("A"); g.setAulaRequerida("PISCINA");
        assertThrows(IllegalArgumentException.class, () -> service.crear(10, g));
        CursoDTO h = dto(" ");
        assertThrows(IllegalArgumentException.class, () -> service.crear(10, h));
    }
    @Test void crearConPrerrequisitosSinDuplicados() {
        Curso pre = new Curso();
        pre.setIdCurso(5);
        when(cursoRepo.findById(5)).thenReturn(Optional.of(pre));
        CursoDTO p = new CursoDTO();
        p.setIdCurso(5);
        CursoDTO d = dto("B");
        d.setPrerrequisitos(List.of(p, p));
        service.crear(10, d);
        verify(cursoRepo).save(argThat(c -> c.getPrerrequisitos().size() == 1));
    }
    @Test void crearPrerrequisitoInexistente() {
        when(cursoRepo.findById(77)).thenReturn(Optional.empty());
        CursoDTO p = new CursoDTO();
        p.setIdCurso(77);
        CursoDTO d = dto("B");
        d.setPrerrequisitos(List.of(p));
        assertThrows(CursoNoEncontradoException.class, () -> service.crear(10, d));
    }
    @Test void listarPorPlan() {
        Curso c = new Curso();
        c.setIdCurso(1);
        c.setNombreCurso("X");
        when(cursoRepo.findByPlanEstudios(plan)).thenReturn(List.of(c));
        assertEquals("X", service.listarPorPlan(10).get(0).getNombreCurso());
    }
    @Test void listarPorPlanInexistente() {
        when(planRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(PlanEstudiosNoEncontradoException.class, () -> service.listarPorPlan(9));
    }
    @Test void eliminarLoQuitaDeTodosLosPlanes() {
        Curso c = new Curso();
        c.setIdCurso(1);
        c.setNombreCurso("X");
        Curso otro = new Curso();
        otro.setIdCurso(2);
        plan.setCursos(new ArrayList<>(List.of(c, otro)));
        c.setPlanesEstudio(new ArrayList<>(List.of(plan)));
        when(cursoRepo.findById(1)).thenReturn(Optional.of(c));

        CursoDTO r = service.eliminar(1);

        assertEquals(1, r.getIdCurso());
        assertEquals(1, plan.getCursos().size());
        assertEquals(2, plan.getCursos().get(0).getIdCurso());
        verify(cursoRepo).delete(c);
    }
    @Test void eliminarSinPlanesNoFalla() {
        Curso c = new Curso();
        c.setIdCurso(1);
        c.setPlanesEstudio(null);
        when(cursoRepo.findById(1)).thenReturn(Optional.of(c));
        assertDoesNotThrow(() -> service.eliminar(1));
    }
    @Test void eliminarInexistente() {
        when(cursoRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(CursoNoEncontradoException.class, () -> service.eliminar(9));
    }
}
