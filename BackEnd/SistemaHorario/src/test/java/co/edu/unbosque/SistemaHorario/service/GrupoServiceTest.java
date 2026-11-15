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

/** PRUEBA UNITARIA: GrupoService (creación, inscripción, retiro y listados). */
class GrupoServiceTest {

    private GrupoRepository grupoRepo;
    private CursoRepository cursoRepo;
    private DocenteRepository docenteRepo;
    private GrupoService service;
    private Curso curso;
    private Docente docente;
    private Grupo grupo;

    @BeforeEach
    void preparar() {
        grupoRepo = mock(GrupoRepository.class);
        cursoRepo = mock(CursoRepository.class);
        docenteRepo = mock(DocenteRepository.class);
        service = new GrupoService(grupoRepo, cursoRepo, docenteRepo, new MapeadorDTO(),
                mock(PlanEstudiosRepository.class));

        curso = new Curso();
        curso.setIdCurso(8);
        curso.setNombreCurso("Cálculo");
        docente = new Docente();
        docente.setId("D1");
        grupo = new Grupo();
        grupo.setIdGrupo(1);
        grupo.setCurso(curso);
        grupo.setDocente(docente);
        grupo.setEstadoGrupo(EstadoGrupo.ABIERTO);
        grupo.setCupoMaximo(2);
        grupo.setCupoMinimo(1);
        grupo.setEstudiantes(new ArrayList<>());

        when(cursoRepo.findById(8)).thenReturn(Optional.of(curso));
        when(docenteRepo.findById("D1")).thenReturn(Optional.of(docente));
        when(grupoRepo.findById(1)).thenReturn(Optional.of(grupo));
        when(grupoRepo.save(any(Grupo.class))).thenAnswer(i -> i.getArgument(0));
    }

    private GrupoDTO dto(Integer max, Integer min, String estado) {
        GrupoDTO d = new GrupoDTO();
        d.setCupoMaximo(max);
        d.setCupoMinimo(min);
        d.setEstadoGrupo(estado);
        return d;
    }

    // ---- crear
    @Test void crearPorDefectoQuedaAbierto() {
        GrupoDTO r = service.crear(8, "D1", dto(30, 5, null));
        assertEquals("ABIERTO", r.getEstadoGrupo());
        assertEquals(30, r.getCupoMaximo());
        assertEquals(8, r.getCurso().getIdCurso());
        assertEquals("D1", r.getDocente().getId());
    }
    @Test void crearConEstadoCerrado() {
        assertEquals("CERRADO", service.crear(8, "D1", dto(30, 5, "CERRADO")).getEstadoGrupo());
    }
    @Test void crearCupoMaximoMenorAlMinimoEsInvalido() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> service.crear(8, "D1", dto(3, 10, null)));
        assertTrue(e.getMessage().contains("cupo máximo"));
    }
    @Test void crearCuposMaximoIgualAlMinimoEsValido() {
        assertDoesNotThrow(() -> service.crear(8, "D1", dto(5, 5, null)));
    }
    @Test void crearCuposInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> service.crear(8, "D1", dto(null, 5, null)));
        assertThrows(IllegalArgumentException.class, () -> service.crear(8, "D1", dto(30, 0, null)));
        assertThrows(IllegalArgumentException.class, () -> service.crear(8, "D1", dto(30, 5, "ENIGMA")));
    }
    @Test void crearCursoODocenteInexistente() {
        when(cursoRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(CursoNoEncontradoException.class, () -> service.crear(9, "D1", dto(30, 5, null)));
        when(docenteRepo.findById("X")).thenReturn(Optional.empty());
        assertThrows(DocenteNoEncontradoException.class, () -> service.crear(8, "X", dto(30, 5, null)));
        verify(grupoRepo, never()).save(any());
    }

    // ---- listados / eliminar
    @Test void listarPorCurso() {
        when(grupoRepo.findByCurso(curso)).thenReturn(List.of(grupo));
        assertEquals(1, service.listarPorCurso(8).size());
    }
    @Test void listarPorCursoInexistente() {
        when(cursoRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(CursoNoEncontradoException.class, () -> service.listarPorCurso(9));
    }
    @Test void listarPorDocente() {
        when(docenteRepo.existsById("D1")).thenReturn(true);
        when(grupoRepo.findByDocente_Id("D1")).thenReturn(List.of(grupo));
        assertEquals(1, service.listarPorDocente("D1").size());
    }
    @Test void listarPorDocenteInexistente() {
        when(docenteRepo.existsById("X")).thenReturn(false);
        assertThrows(DocenteNoEncontradoException.class, () -> service.listarPorDocente("X"));
    }
    @Test void eliminar() {
        assertEquals(1, service.eliminar(1).getIdGrupo());
        verify(grupoRepo).delete(grupo);
    }
    @Test void eliminarInexistente() {
        when(grupoRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(GrupoNoEncontradoException.class, () -> service.eliminar(9));
    }

    // ---- inscribir / retirar: la lógica vive en los procedimientos almacenados; el servicio solo delega ----
    @Test void inscribirLlamaAlProcedimientoYDevuelveElGrupoActualizado() {
        Estudiante e = new Estudiante();
        e.setId("E1");
        when(grupoRepo.inscribirEstudiante(1, "E1")).thenAnswer(i -> {
            grupo.setEstudiantes(new ArrayList<>(List.of(e)));
            return 1;
        });

        GrupoDTO r = service.inscribirEstudiante(1, "E1");

        verify(grupoRepo).inscribirEstudiante(1, "E1");
        assertEquals(1, r.getEstudiantes().size());
        assertEquals("E1", r.getEstudiantes().get(0).getId());
    }
    @Test void inscribirPrimeroEjecutaElProcedimientoYDespuesLeeElGrupo() {
        service.inscribirEstudiante(1, "E1");
        InOrder orden = inOrder(grupoRepo);
        orden.verify(grupoRepo).inscribirEstudiante(1, "E1");
        orden.verify(grupoRepo).findById(1);
    }
    @Test void inscribirPropagaLosErroresDelProcedimiento() {
        when(grupoRepo.inscribirEstudiante(1, "E1")).thenThrow(new GrupoRepetidoException("ya inscrito"));
        assertThrows(GrupoRepetidoException.class, () -> service.inscribirEstudiante(1, "E1"));
        when(grupoRepo.inscribirEstudiante(1, "E2")).thenThrow(new IllegalArgumentException("El grupo está cerrado"));
        assertThrows(IllegalArgumentException.class, () -> service.inscribirEstudiante(1, "E2"));
        when(grupoRepo.inscribirEstudiante(9, "E1")).thenThrow(new GrupoNoEncontradoException("no existe"));
        assertThrows(GrupoNoEncontradoException.class, () -> service.inscribirEstudiante(9, "E1"));
        when(grupoRepo.inscribirEstudiante(1, "X")).thenThrow(new EstudianteNoEncontradoException("no existe"));
        assertThrows(EstudianteNoEncontradoException.class, () -> service.inscribirEstudiante(1, "X"));
    }
    @Test void siElProcedimientoFallaNoSeLeeElGrupo() {
        when(grupoRepo.inscribirEstudiante(1, "E1")).thenThrow(new IllegalArgumentException("sin cupos"));
        assertThrows(IllegalArgumentException.class, () -> service.inscribirEstudiante(1, "E1"));
        verify(grupoRepo, never()).findById(any());
    }
    @Test void retirarLlamaAlProcedimientoYDevuelveElGrupoActualizado() {
        GrupoDTO r = service.retirarEstudiante(1, "E1");
        InOrder orden = inOrder(grupoRepo);
        orden.verify(grupoRepo).retirarEstudiante(1, "E1");
        orden.verify(grupoRepo).findById(1);
        assertEquals(1, r.getIdGrupo());
    }
    @Test void retirarPropagaLosErroresDelProcedimiento() {
        when(grupoRepo.retirarEstudiante(1, "E1")).thenThrow(new EstudianteNoEncontradoException("no está inscrito"));
        assertThrows(EstudianteNoEncontradoException.class, () -> service.retirarEstudiante(1, "E1"));
        when(grupoRepo.retirarEstudiante(9, "E1")).thenThrow(new GrupoNoEncontradoException("no existe"));
        assertThrows(GrupoNoEncontradoException.class, () -> service.retirarEstudiante(9, "E1"));
    }
}
