package co.edu.unbosque.SistemaHorario.service;

import co.edu.unbosque.SistemaHorario.model.*;
import co.edu.unbosque.SistemaHorario.model.dto.*;
import co.edu.unbosque.SistemaHorario.model.exception.*;
import co.edu.unbosque.SistemaHorario.respository.*;
import co.edu.unbosque.SistemaHorario.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** PRUEBA UNITARIA: servicios de la jerarquía académica (facultad, carrera, área, plan). */
class FacultadCarreraAreaPlanServiceTest {

    private FacultadRepository facultadRepo;
    private CarreraRepository carreraRepo;
    private AreaRepository areaRepo;
    private PlanEstudiosRepository planRepo;
    private FacultadService facultadService;
    private CarreraService carreraService;
    private AreaService areaService;
    private PlanEstudiosService planService;
    private Facultad facultad;
    private Carrera carrera;

    @BeforeEach
    void preparar() {
        facultadRepo = mock(FacultadRepository.class);
        carreraRepo = mock(CarreraRepository.class);
        areaRepo = mock(AreaRepository.class);
        planRepo = mock(PlanEstudiosRepository.class);
        MapeadorDTO m = new MapeadorDTO();
        facultadService = new FacultadService(facultadRepo, m);
        carreraService = new CarreraService(carreraRepo, facultadRepo, m);
        areaService = new AreaService(areaRepo, facultadRepo, m);
        planService = new PlanEstudiosService(planRepo, carreraRepo, m);

        facultad = new Facultad();
        facultad.setIdFacultad(1);
        facultad.setNombreFacultad("Ingeniería");
        carrera = new Carrera();
        carrera.setIdCarrera(2);
        carrera.setNombreCarrera("Sistemas");
        carrera.setFacultad(facultad);
        when(facultadRepo.findById(1)).thenReturn(Optional.of(facultad));
        when(carreraRepo.findById(2)).thenReturn(Optional.of(carrera));
        when(facultadRepo.save(any(Facultad.class))).thenAnswer(i -> i.getArgument(0));
        when(carreraRepo.save(any(Carrera.class))).thenAnswer(i -> i.getArgument(0));
        when(areaRepo.save(any(Area.class))).thenAnswer(i -> i.getArgument(0));
        when(planRepo.save(any(PlanEstudios.class))).thenAnswer(i -> i.getArgument(0));
    }

    private FacultadDTO fac(String n) { FacultadDTO d = new FacultadDTO(); d.setNombreFacultad(n); return d; }
    private CarreraDTO car(String n) { CarreraDTO d = new CarreraDTO(); d.setNombreCarrera(n); return d; }
    private AreaDTO are(String n) { AreaDTO d = new AreaDTO(); d.setNombreArea(n); d.setDescripcion("desc"); return d; }
    private PlanEstudiosDTO pla(String n) { PlanEstudiosDTO d = new PlanEstudiosDTO(); d.setNombrePlanEstudios(n); return d; }

    // ---- Facultad
    @Test void facultadCrearRecortaElNombre() {
        assertEquals("Artes", facultadService.crear(fac("  Artes  ")).getNombreFacultad());
    }
    @Test void facultadCrearRepetidaLanzaConflicto() {
        when(facultadRepo.existsByNombreFacultad("Artes")).thenReturn(true);
        assertThrows(FacultadRepetidaException.class, () -> facultadService.crear(fac("Artes")));
        verify(facultadRepo, never()).save(any());
    }
    @Test void facultadCrearSinNombreEsInvalido() {
        assertThrows(IllegalArgumentException.class, () -> facultadService.crear(fac(" ")));
    }
    @Test void facultadListarTodos() {
        when(facultadRepo.findAll()).thenReturn(List.of(facultad));
        assertEquals(1, facultadService.listarTodos().size());
    }
    @Test void facultadEliminarHaceFlushYDevuelveElDto() {
        FacultadDTO r = facultadService.eliminar(1);
        assertEquals(1, r.getIdFacultad());
        verify(facultadRepo).delete(facultad);
    }
    @Test void facultadEliminarInexistente() {
        when(facultadRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(FacultadNoEncontradaException.class, () -> facultadService.eliminar(9));
    }

    // ---- Carrera
    @Test void carreraCrearEnFacultadExistente() {
        CarreraDTO r = carreraService.crear(1, car(" Medicina "));
        assertEquals("Medicina", r.getNombreCarrera());
    }
    @Test void carreraCrearEnFacultadInexistente() {
        when(facultadRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(FacultadNoEncontradaException.class, () -> carreraService.crear(9, car("X")));
    }
    @Test void carreraCrearRepetidaEnLaMismaFacultad() {
        when(carreraRepo.existsByNombreCarreraAndFacultad("Sistemas", facultad)).thenReturn(true);
        assertThrows(CarreraRepetidaException.class, () -> carreraService.crear(1, car("Sistemas")));
    }
    @Test void carreraListarPorFacultad() {
        when(carreraRepo.findByFacultad(facultad)).thenReturn(List.of(carrera));
        assertEquals("Sistemas", carreraService.listarPorFacultad(1).get(0).getNombreCarrera());
    }
    @Test void carreraListarPorFacultadInexistente() {
        when(facultadRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(FacultadNoEncontradaException.class, () -> carreraService.listarPorFacultad(9));
    }
    @Test void carreraListarTodasYEliminar() {
        when(carreraRepo.findAll()).thenReturn(List.of(carrera));
        assertEquals(1, carreraService.listarTodas().size());
        assertEquals(2, carreraService.eliminar(2).getIdCarrera());
    }
    @Test void carreraEliminarInexistente() {
        when(carreraRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(CarreraNoEncontradaException.class, () -> carreraService.eliminar(9));
    }

    // ---- Área
    @Test void areaCrearGuardaDescripcionYFacultad() {
        AreaDTO r = areaService.crear(1, are("Matemáticas"));
        assertEquals("Matemáticas", r.getNombreArea());
        assertEquals("desc", r.getDescripcion());
    }
    @Test void areaCrearRepetida() {
        when(areaRepo.existsByNombreAreaAndFacultad("Matemáticas", facultad)).thenReturn(true);
        assertThrows(AreaRepetidaException.class, () -> areaService.crear(1, are("Matemáticas")));
    }
    @Test void areaCrearEnFacultadInexistente() {
        when(facultadRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(FacultadNoEncontradaException.class, () -> areaService.crear(9, are("X")));
    }
    @Test void areaListarYEliminar() {
        Area a = new Area(5, "Física", "d");
        when(areaRepo.findByFacultad(facultad)).thenReturn(List.of(a));
        when(areaRepo.findById(5)).thenReturn(Optional.of(a));
        assertEquals(1, areaService.listarPorFacultad(1).size());
        assertEquals(5, areaService.eliminar(5).getIdArea());
    }
    @Test void areaEliminarInexistente() {
        when(areaRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(AreaNoEncontradaException.class, () -> areaService.eliminar(9));
    }

    // ---- Plan de estudios
    @Test void planCrearInicializaCursosVacios() {
        PlanEstudiosDTO r = planService.crear(2, pla("Plan 2026"));
        assertEquals("Plan 2026", r.getNombrePlanEstudios());
        verify(planRepo).save(argThat(p -> p.getCursos() != null && p.getCursos().isEmpty()
                && p.getCarrera() == carrera));
    }
    @Test void planCrearRepetidoEnLaMismaCarrera() {
        when(planRepo.existsByNombrePlanEstudiosAndCarrera("P", carrera)).thenReturn(true);
        assertThrows(PlanEstudiosRepetidoException.class, () -> planService.crear(2, pla("P")));
    }
    @Test void planCrearEnCarreraInexistente() {
        when(carreraRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(CarreraNoEncontradaException.class, () -> planService.crear(9, pla("P")));
    }
    @Test void planListarPorCarreraYEliminar() {
        PlanEstudios p = new PlanEstudios();
        p.setIdPlanEstudios(7);
        p.setCarrera(carrera);
        when(planRepo.findByCarrera(carrera)).thenReturn(List.of(p));
        when(planRepo.findById(7)).thenReturn(Optional.of(p));
        assertEquals(1, planService.listarPorCarrera(2).size());
        assertEquals(7, planService.eliminar(7).getIdPlanEstudios());
    }
    @Test void planEliminarInexistente() {
        when(planRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(PlanEstudiosNoEncontradoException.class, () -> planService.eliminar(9));
    }
}
