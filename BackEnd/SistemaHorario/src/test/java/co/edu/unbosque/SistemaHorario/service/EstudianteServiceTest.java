package co.edu.unbosque.SistemaHorario.service;

import co.edu.unbosque.SistemaHorario.model.*;
import co.edu.unbosque.SistemaHorario.model.dto.EstudianteDTO;
import co.edu.unbosque.SistemaHorario.model.exception.EstudianteNoEncontradoException;
import co.edu.unbosque.SistemaHorario.model.exception.EstudianteRepetidoException;
import co.edu.unbosque.SistemaHorario.model.exception.PlanEstudiosNoEncontradoException;
import co.edu.unbosque.SistemaHorario.respository.CarreraRepository;
import co.edu.unbosque.SistemaHorario.respository.EstudianteRepository;
import co.edu.unbosque.SistemaHorario.respository.PersonaRepository;
import co.edu.unbosque.SistemaHorario.respository.PlanEstudiosRepository;
import co.edu.unbosque.SistemaHorario.services.EstudianteService;
import co.edu.unbosque.SistemaHorario.services.MapeadorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** PRUEBA UNITARIA: EstudianteService.obtener / actualizar / resolverPlan (vía crear). */
class EstudianteServiceTest {

    private EstudianteRepository estudianteRepository;
    private PersonaRepository personaRepository;
    private PlanEstudiosRepository planRepository;
    private EstudianteService service;
    private Estudiante existente;
    private PlanEstudios plan10;

    @BeforeEach
    void preparar() {
        estudianteRepository = mock(EstudianteRepository.class);
        personaRepository = mock(PersonaRepository.class);
        planRepository = mock(PlanEstudiosRepository.class);
        service = new EstudianteService(estudianteRepository, personaRepository, planRepository,
                mock(CarreraRepository.class), new MapeadorDTO());

        Carrera carrera = new Carrera();
        carrera.setIdCarrera(3);
        carrera.setNombreCarrera("Ingeniería");
        plan10 = new PlanEstudios();
        plan10.setIdPlanEstudios(10);
        plan10.setCarrera(carrera);

        existente = new Estudiante();
        existente.setId("E1");
        existente.setNombre("Luis");
        existente.setApellido("Mora");
        existente.setCorreo("luis@u.edu");
        existente.setPassword("clave-vieja");
        existente.setSemestreCursa(2);
        existente.setCreditosAprobados(20);
        existente.setEstadoEstudiante(EstadoEstudiante.ACTIVO);
        existente.setPlanEstudios(plan10);

        when(estudianteRepository.findById("E1")).thenReturn(Optional.of(existente));
        when(estudianteRepository.save(any(Estudiante.class))).thenAnswer(i -> i.getArgument(0));
        when(planRepository.findById(10)).thenReturn(Optional.of(plan10));
    }

    private EstudianteDTO dto() {
        EstudianteDTO dto = new EstudianteDTO();
        dto.setNombre("Luis Fernando");
        dto.setApellido("Mora");
        dto.setCorreo("luis.nuevo@u.edu");
        dto.setSemestreCursa(4);
        dto.setCreditosAprobados(55);
        dto.setEstadoEstudiante("PRUEBA_ACADEMICA");
        return dto;
    }

    @Test
    void obtenerDevuelveElEstudianteConPlanYCarrera() {
        EstudianteDTO r = service.obtener("E1");
        assertEquals("E1", r.getId());
        assertEquals(10, r.getIdPlanEstudios());
        assertEquals(3, r.getIdCarrera());
        assertEquals("Ingeniería", r.getNombreCarrera());
        assertNull(r.getPassword());
    }

    @Test
    void obtenerInexistenteLanzaNoEncontrado() {
        when(estudianteRepository.findById("X")).thenReturn(Optional.empty());
        assertThrows(EstudianteNoEncontradoException.class, () -> service.obtener("X"));
    }

    @Test
    void actualizaCamposYConservaPasswordYPlan() {
        EstudianteDTO r = service.actualizar("E1", dto());
        assertEquals("Luis Fernando", r.getNombre());
        assertEquals(4, r.getSemestreCursa());
        assertEquals(55, r.getCreditosAprobados());
        assertEquals("PRUEBA_ACADEMICA", r.getEstadoEstudiante());
        assertEquals("clave-vieja", existente.getPassword());
        assertSame(plan10, existente.getPlanEstudios());
        verifyNoInteractions(planRepository);
    }

    @Test
    void conPasswordLaReemplaza() {
        EstudianteDTO dto = dto();
        dto.setPassword("nueva");
        service.actualizar("E1", dto);
        assertEquals("nueva", existente.getPassword());
    }

    @Test
    void conPlanValidoLoCambia() {
        Carrera otra = new Carrera();
        otra.setIdCarrera(4);
        PlanEstudios plan20 = new PlanEstudios();
        plan20.setIdPlanEstudios(20);
        plan20.setCarrera(otra);
        when(planRepository.findById(20)).thenReturn(Optional.of(plan20));

        EstudianteDTO dto = dto();
        dto.setIdPlanEstudios(20);
        dto.setIdCarrera(4);
        service.actualizar("E1", dto);

        assertSame(plan20, existente.getPlanEstudios());
    }

    @Test
    void planQueNoCorrespondeALaCarreraEsInvalido() {
        EstudianteDTO dto = dto();
        dto.setIdPlanEstudios(10);
        dto.setIdCarrera(99);
        assertThrows(IllegalArgumentException.class, () -> service.actualizar("E1", dto));
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    void planInexistenteLanzaNoEncontrado() {
        when(planRepository.findById(77)).thenReturn(Optional.empty());
        EstudianteDTO dto = dto();
        dto.setIdPlanEstudios(77);
        dto.setIdCarrera(3);
        assertThrows(PlanEstudiosNoEncontradoException.class, () -> service.actualizar("E1", dto));
    }

    @Test
    void correoDeOtraPersonaLanzaRepetido() {
        when(personaRepository.existsByCorreoAndIdNot("luis.nuevo@u.edu", "E1")).thenReturn(true);
        assertThrows(EstudianteRepetidoException.class, () -> service.actualizar("E1", dto()));
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    void estudianteInexistenteLanzaNoEncontrado() {
        when(estudianteRepository.findById("X")).thenReturn(Optional.empty());
        assertThrows(EstudianteNoEncontradoException.class, () -> service.actualizar("X", dto()));
    }

    @Test
    void semestreCeroEsInvalido() {
        EstudianteDTO dto = dto();
        dto.setSemestreCursa(0);
        assertThrows(IllegalArgumentException.class, () -> service.actualizar("E1", dto));
    }

    @Test
    void creditosNegativosSonInvalidos() {
        EstudianteDTO dto = dto();
        dto.setCreditosAprobados(-1);
        assertThrows(IllegalArgumentException.class, () -> service.actualizar("E1", dto));
    }

    @Test
    void crearSigueValidandoPlanCarrera() {
        EstudianteDTO dto = dto();
        dto.setId("E9");
        dto.setPassword("x");
        dto.setIdPlanEstudios(10);
        dto.setIdCarrera(99);
        assertThrows(IllegalArgumentException.class, () -> service.crear(dto));
    }

    @Test
    void crearConPlanYCarreraCorrectosGuarda() {
        EstudianteDTO dto = dto();
        dto.setId("E9");
        dto.setPassword("x");
        dto.setIdPlanEstudios(10);
        dto.setIdCarrera(3);
        EstudianteDTO r = service.crear(dto);
        assertEquals("E9", r.getId());
        assertEquals(10, r.getIdPlanEstudios());
    }
}
