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

/** PRUEBA UNITARIA: EstudianteService (listados, duplicados al crear, asignar plan, eliminar). */
class EstudianteServiceCrudTest {

    private EstudianteRepository estudianteRepo;
    private PersonaRepository personaRepo;
    private PlanEstudiosRepository planRepo;
    private CarreraRepository carreraRepo;
    private EstudianteService service;
    private PlanEstudios plan;
    private Carrera carrera;

    @BeforeEach
    void preparar() {
        estudianteRepo = mock(EstudianteRepository.class);
        personaRepo = mock(PersonaRepository.class);
        planRepo = mock(PlanEstudiosRepository.class);
        carreraRepo = mock(CarreraRepository.class);
        service = new EstudianteService(estudianteRepo, personaRepo, planRepo, carreraRepo, new MapeadorDTO());
        carrera = new Carrera();
        carrera.setIdCarrera(3);
        plan = new PlanEstudios();
        plan.setIdPlanEstudios(10);
        plan.setCarrera(carrera);
        when(planRepo.findById(10)).thenReturn(Optional.of(plan));
        when(estudianteRepo.save(any(Estudiante.class))).thenAnswer(i -> i.getArgument(0));
    }

    private EstudianteDTO dto() {
        EstudianteDTO d = new EstudianteDTO();
        d.setId("E1");
        d.setNombre("Luis");
        d.setApellido("Mora");
        d.setCorreo("luis@u.edu");
        d.setPassword("clave");
        d.setSemestreCursa(1);
        d.setCreditosAprobados(0);
        d.setEstadoEstudiante("ACTIVO");
        d.setIdPlanEstudios(10);
        d.setIdCarrera(3);
        return d;
    }

    private Estudiante existente() {
        Estudiante e = new Estudiante();
        e.setId("E1");
        e.setNombre("Luis");
        e.setApellido("Mora");
        e.setCorreo("luis@u.edu");
        e.setPlanEstudios(plan);
        e.setSemestreCursa(1);
        e.setCreditosAprobados(0);
        return e;
    }

    @Test void crearConIdExistente() {
        when(personaRepo.existsById("E1")).thenReturn(true);
        assertThrows(EstudianteRepetidoException.class, () -> service.crear(dto()));
        verify(estudianteRepo, never()).save(any());
    }
    @Test void crearConCorreoExistente() {
        when(personaRepo.existsByCorreo("luis@u.edu")).thenReturn(true);
        assertThrows(EstudianteRepetidoException.class, () -> service.crear(dto()));
    }
    @Test void crearGuardaLaPasswordYNoLaExpone() {
        EstudianteDTO r = service.crear(dto());
        assertNull(r.getPassword());
        verify(estudianteRepo).save(argThat(e -> "clave".equals(e.getPassword()) && e.getPlanEstudios() == plan));
    }
    @Test void crearValidaCamposObligatorios() {
        EstudianteDTO a = dto(); a.setPassword(" ");
        assertThrows(IllegalArgumentException.class, () -> service.crear(a));
        EstudianteDTO b = dto(); b.setEstadoEstudiante("XX");
        assertThrows(IllegalArgumentException.class, () -> service.crear(b));
        EstudianteDTO c = dto(); c.setSemestreCursa(0);
        assertThrows(IllegalArgumentException.class, () -> service.crear(c));
        EstudianteDTO d = dto(); d.setCreditosAprobados(-5);
        assertThrows(IllegalArgumentException.class, () -> service.crear(d));
        EstudianteDTO e = dto(); e.setIdPlanEstudios(null);
        assertThrows(IllegalArgumentException.class, () -> service.crear(e));
    }
    @Test void crearConPlanInexistente() {
        when(planRepo.findById(77)).thenReturn(Optional.empty());
        EstudianteDTO d = dto(); d.setIdPlanEstudios(77);
        assertThrows(PlanEstudiosNoEncontradoException.class, () -> service.crear(d));
    }
    @Test void listarTodos() {
        when(estudianteRepo.findAll()).thenReturn(List.of(existente()));
        assertEquals(1, service.listarTodos().size());
    }
    @Test void listarPorCarrera() {
        when(carreraRepo.findById(3)).thenReturn(Optional.of(carrera));
        when(estudianteRepo.findByPlanEstudiosCarrera(carrera)).thenReturn(List.of(existente()));
        assertEquals("E1", service.listarPorCarrera(3).get(0).getId());
    }
    @Test void listarPorCarreraInexistente() {
        when(carreraRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(CarreraNoEncontradaException.class, () -> service.listarPorCarrera(9));
    }
    @Test void asignarPlanCambiaElPlan() {
        Estudiante e = existente();
        e.setPlanEstudios(null);
        when(estudianteRepo.findById("E1")).thenReturn(Optional.of(e));
        EstudianteDTO r = service.asignarPlan("E1", 10);
        assertEquals(10, r.getIdPlanEstudios());
        assertSame(plan, e.getPlanEstudios());
    }
    @Test void asignarPlanEstudianteOPlanInexistente() {
        when(estudianteRepo.findById("X")).thenReturn(Optional.empty());
        assertThrows(EstudianteNoEncontradoException.class, () -> service.asignarPlan("X", 10));
        when(estudianteRepo.findById("E1")).thenReturn(Optional.of(existente()));
        when(planRepo.findById(99)).thenReturn(Optional.empty());
        assertThrows(PlanEstudiosNoEncontradoException.class, () -> service.asignarPlan("E1", 99));
    }
    @Test void eliminar() {
        Estudiante e = existente();
        when(estudianteRepo.findById("E1")).thenReturn(Optional.of(e));
        assertEquals("E1", service.eliminar("E1").getId());
        verify(estudianteRepo).delete(e);
    }
    @Test void eliminarInexistente() {
        when(estudianteRepo.findById("X")).thenReturn(Optional.empty());
        assertThrows(EstudianteNoEncontradoException.class, () -> service.eliminar("X"));
    }
}
