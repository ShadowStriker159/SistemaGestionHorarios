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

/** PRUEBA UNITARIA: DocenteService (listar, obtener, crear, disponibilidad, eliminar). */
class DocenteServiceCrudTest {

    private DocenteRepository docenteRepo;
    private PersonaRepository personaRepo;
    private FranjaHorariaRepository franjaRepo;
    private GrupoRepository grupoRepo;
    private DocenteService service;

    @BeforeEach
    void preparar() {
        docenteRepo = mock(DocenteRepository.class);
        personaRepo = mock(PersonaRepository.class);
        franjaRepo = mock(FranjaHorariaRepository.class);
        grupoRepo = mock(GrupoRepository.class);
        service = new DocenteService(docenteRepo, personaRepo, franjaRepo, grupoRepo, new MapeadorDTO());
        when(docenteRepo.save(any(Docente.class))).thenAnswer(i -> i.getArgument(0));
    }

    private DocenteDTO dto() {
        DocenteDTO d = new DocenteDTO();
        d.setId("D1");
        d.setNombre(" Ana ");
        d.setApellido("Ruiz");
        d.setCorreo("ana@u.edu");
        d.setPassword("clave");
        d.setEscalafon("PROFESOR_TITULAR");
        d.setEstadoDocente("ACTIVO");
        d.setTipoVinculacion("TC");
        return d;
    }

    private FranjaHoraria franja(int id) {
        return new FranjaHoraria(id, DiaSemana.LUNES, EstadoFranja.DISPONIBLE, Categoria.DIA, LocalTime.of(7, 0), LocalTime.of(9, 0));
    }

    private FranjaHorariaDTO franjaDto(int id) {
        FranjaHorariaDTO f = new FranjaHorariaDTO();
        f.setIdFranja(id);
        return f;
    }

    private Docente docenteExistente() {
        Docente d = new Docente();
        d.setId("D1");
        d.setNombre("Ana");
        d.setApellido("Ruiz");
        d.setCorreo("ana@u.edu");
        d.setPassword("x");
        d.setDisponibilidad(new ArrayList<>());
        return d;
    }

    @Test void crearGuardaTodosLosCamposRecortandoTexto() {
        DocenteDTO r = service.crear(dto());
        assertEquals("D1", r.getId());
        assertEquals("Ana", r.getNombre());
        assertEquals("TC", r.getTipoVinculacion());
        assertNull(r.getPassword());
        assertTrue(r.getDisponibilidad().isEmpty());
        verify(docenteRepo).save(argThat(d -> "clave".equals(d.getPassword())));
    }
    @Test void crearConIdExistente() {
        when(personaRepo.existsById("D1")).thenReturn(true);
        assertThrows(DocenteRepetidoException.class, () -> service.crear(dto()));
        verify(docenteRepo, never()).save(any());
    }
    @Test void crearConCorreoExistente() {
        when(personaRepo.existsByCorreo("ana@u.edu")).thenReturn(true);
        assertThrows(DocenteRepetidoException.class, () -> service.crear(dto()));
    }
    @Test void crearValidaCamposObligatorios() {
        DocenteDTO a = dto(); a.setId(" ");
        assertThrows(IllegalArgumentException.class, () -> service.crear(a));
        DocenteDTO b = dto(); b.setPassword(null);
        assertThrows(IllegalArgumentException.class, () -> service.crear(b));
        DocenteDTO c = dto(); c.setTipoVinculacion("XX");
        assertThrows(IllegalArgumentException.class, () -> service.crear(c));
        DocenteDTO e = dto(); e.setNombre("");
        assertThrows(IllegalArgumentException.class, () -> service.crear(e));
    }
    @Test void crearConDisponibilidadResuelveLasFranjasSinDuplicados() {
        when(franjaRepo.findById(7)).thenReturn(Optional.of(franja(7)));
        DocenteDTO d = dto();
        d.setDisponibilidad(List.of(franjaDto(7), franjaDto(7)));
        DocenteDTO r = service.crear(d);
        assertEquals(1, r.getDisponibilidad().size());
        assertEquals(7, r.getDisponibilidad().get(0).getIdFranja());
    }
    @Test void crearConFranjaInexistente() {
        when(franjaRepo.findById(99)).thenReturn(Optional.empty());
        DocenteDTO d = dto();
        d.setDisponibilidad(List.of(franjaDto(99)));
        assertThrows(FranjaHorariaNoEncontradaException.class, () -> service.crear(d));
        verify(docenteRepo, never()).save(any());
    }
    @Test void listarTodosYObtener() {
        Docente d = docenteExistente();
        when(docenteRepo.findAll()).thenReturn(List.of(d));
        when(docenteRepo.findById("D1")).thenReturn(Optional.of(d));
        assertEquals(1, service.listarTodos().size());
        assertEquals("ana@u.edu", service.obtener("D1").getCorreo());
    }
    @Test void obtenerInexistente() {
        when(docenteRepo.findById("X")).thenReturn(Optional.empty());
        assertThrows(DocenteNoEncontradoException.class, () -> service.obtener("X"));
    }
    @Test void actualizarDisponibilidadReemplazaLasFranjas() {
        Docente d = docenteExistente();
        d.getDisponibilidad().add(franja(1));
        when(docenteRepo.findById("D1")).thenReturn(Optional.of(d));
        when(franjaRepo.findById(2)).thenReturn(Optional.of(franja(2)));

        DocenteDTO r = service.actualizarDisponibilidad("D1", List.of(franjaDto(2)));

        assertEquals(1, r.getDisponibilidad().size());
        assertEquals(2, r.getDisponibilidad().get(0).getIdFranja());
    }
    @Test void actualizarDisponibilidadConListaNulaLaVacia() {
        Docente d = docenteExistente();
        d.getDisponibilidad().add(franja(1));
        when(docenteRepo.findById("D1")).thenReturn(Optional.of(d));
        assertTrue(service.actualizarDisponibilidad("D1", null).getDisponibilidad().isEmpty());
    }
    @Test void actualizarDisponibilidadDocenteInexistente() {
        when(docenteRepo.findById("X")).thenReturn(Optional.empty());
        assertThrows(DocenteNoEncontradoException.class, () -> service.actualizarDisponibilidad("X", List.of()));
    }
    @Test void eliminarSinGruposBorraYHaceFlush() {
        Docente d = docenteExistente();
        when(docenteRepo.findById("D1")).thenReturn(Optional.of(d));
        when(grupoRepo.existsByDocente_Id("D1")).thenReturn(false);
        assertEquals("D1", service.eliminar("D1").getId());
        verify(docenteRepo).delete(d);
    }
    @Test void eliminarConGruposLanzaConflictoYNoBorra() {
        when(docenteRepo.findById("D1")).thenReturn(Optional.of(docenteExistente()));
        when(grupoRepo.existsByDocente_Id("D1")).thenReturn(true);
        DocenteConGruposException e = assertThrows(DocenteConGruposException.class, () -> service.eliminar("D1"));
        assertTrue(e.getMessage().contains("grupos asignados"));
        verify(docenteRepo, never()).delete(any());
    }
    @Test void eliminarInexistente() {
        when(docenteRepo.findById("X")).thenReturn(Optional.empty());
        assertThrows(DocenteNoEncontradoException.class, () -> service.eliminar("X"));
    }
}
