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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** PRUEBA UNITARIA: HorarioService con el algoritmo simulado. */
class HorarioServiceTest {

    private FacultadRepository facultadRepo;
    private SeccionRepository seccionRepo;
    private IAlgoritmoHorario algoritmo;
    private HorarioService service;
    private Facultad facultad;

    @BeforeEach
    void preparar() {
        facultadRepo = mock(FacultadRepository.class);
        seccionRepo = mock(SeccionRepository.class);
        algoritmo = mock(IAlgoritmoHorario.class);
        service = new HorarioService(facultadRepo, seccionRepo, algoritmo);
        facultad = new Facultad();
        facultad.setIdFacultad(1);
        when(facultadRepo.findByIdCompleta(1)).thenReturn(Optional.of(facultad));
        when(seccionRepo.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
    }

    private Seccion seccionCompleta(Integer id) {
        Carrera carrera = new Carrera();
        carrera.setNombreCarrera("Sistemas");
        PlanEstudios plan = new PlanEstudios();
        plan.setCarrera(carrera);
        Curso curso = new Curso();
        curso.setIdCurso(8);
        curso.setNombreCurso("Cálculo");
        curso.setPlanesEstudio(new ArrayList<>(List.of(plan)));
        Docente docente = new Docente();
        docente.setId("D1");
        docente.setNombre("Ana");
        docente.setApellido("Ruiz");
        Grupo grupo = new Grupo();
        grupo.setIdGrupo(4);
        grupo.setCurso(curso);
        grupo.setDocente(docente);
        Bloque bloque = new Bloque(1, "Bloque A", "A", "Sede norte");
        Aula aula = new Aula(new AulaId(1, "A-101"), 30, TipoAula.GRUPAL, EstadoAula.DISPONIBLE, bloque);
        FranjaHoraria franja = new FranjaHoraria(3, DiaSemana.LUNES, EstadoFranja.DISPONIBLE, Categoria.DIA,
                LocalTime.of(7, 0), LocalTime.of(9, 0));
        Seccion s = new Seccion();
        s.setIdSeccion(id);
        s.setEstadoSeccion(EstadoSeccion.ASIGNADA);
        s.setGrupo(grupo);
        s.setAula(aula);
        s.setFranjaHoraria(franja);
        return s;
    }

    @Test void listarHorariosMapeaTodosLosCampos() {
        when(seccionRepo.findAll()).thenReturn(List.of(seccionCompleta(50)));
        HorarioDTO h = service.listarHorarios().get(0);
        assertEquals(50, h.getNumero());
        assertEquals("ASIGNADA", h.getEstado());
        assertEquals("4", h.getGrupo());
        assertEquals("Ana Ruiz", h.getNombreDocente());
        assertEquals("Cálculo", h.getMateria());
        assertEquals("Sistemas", h.getPrograma());
        assertEquals("Aula A-101", h.getEspacioFisico());
        assertEquals("A", h.getBloque());
        assertEquals("Sede norte", h.getSedeCampus());
        assertEquals("LUNES", h.getDia());
        assertEquals(LocalTime.of(7, 0), h.getHoraInicial());
        assertEquals(LocalTime.of(9, 0), h.getHoraFinal());
    }
    @Test void listarHorariosToleraSeccionesIncompletas() {
        Seccion vacia = new Seccion();
        vacia.setIdSeccion(1);
        when(seccionRepo.findAll()).thenReturn(List.of(vacia));
        HorarioDTO h = service.listarHorarios().get(0);
        assertEquals(1, h.getNumero());
        assertNull(h.getMateria());
        assertNull(h.getDia());
        assertNull(h.getEspacioFisico());
    }
    @Test void listarHorariosVacio() {
        when(seccionRepo.findAll()).thenReturn(List.of());
        assertTrue(service.listarHorarios().isEmpty());
    }
    @Test void generarBorraLasAnterioresAntesDeGuardarYReiniciaIds() {
        Seccion nueva = seccionCompleta(777);
        when(algoritmo.generarHorario(facultad)).thenReturn(List.of(nueva));

        List<HorarioDTO> r = service.generarHorario(1);

        assertEquals(1, r.size());
        InOrder orden = inOrder(seccionRepo);
        orden.verify(seccionRepo).deleteAll();
        @SuppressWarnings({"unchecked", "rawtypes"})
        ArgumentCaptor<List<Seccion>> guardadas = ArgumentCaptor.forClass((Class) List.class);
        orden.verify(seccionRepo).saveAll(guardadas.capture());
        assertNull(guardadas.getValue().get(0).getIdSeccion(), "Los ids deben reiniciarse para insertar de nuevo");
    }
    @Test void generarFacultadInexistente() {
        when(facultadRepo.findByIdCompleta(9)).thenReturn(Optional.empty());
        assertThrows(FacultadNoEncontradaException.class, () -> service.generarHorario(9));
        verifyNoInteractions(algoritmo);
    }
    @Test void generarSinSeccionesLanzaInalcanzableYNoBorraNada() {
        when(algoritmo.generarHorario(facultad)).thenReturn(List.of());
        assertThrows(HorarioInalcanzableException.class, () -> service.generarHorario(1));
        verify(seccionRepo, never()).deleteAll();
    }
    @Test void generarConResultadoNuloLanzaInalcanzable() {
        when(algoritmo.generarHorario(facultad)).thenReturn(null);
        assertThrows(HorarioInalcanzableException.class, () -> service.generarHorario(1));
        verify(seccionRepo, never()).deleteAll();
    }
    @Test void generarPropagaLaExcepcionDelAlgoritmoSinBorrar() {
        when(algoritmo.generarHorario(facultad)).thenThrow(new HorarioInalcanzableException("sin solución"));
        assertThrows(HorarioInalcanzableException.class, () -> service.generarHorario(1));
        verify(seccionRepo, never()).deleteAll();
    }
}
