package co.edu.unbosque.SistemaHorario.service;

import co.edu.unbosque.SistemaHorario.model.*;
import co.edu.unbosque.SistemaHorario.model.dto.*;
import co.edu.unbosque.SistemaHorario.services.MapeadorDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** PRUEBA UNITARIA: conversión entidad → DTO (nulos, listas vacías y contraseñas). */
class MapeadorDTOTest {

    private final MapeadorDTO m = new MapeadorDTO();

    @Test void docenteNuncaExponeLaPasswordYSinDisponibilidadDaListaVacia() {
        Docente d = new Docente();
        d.setId("D1"); d.setNombre("Ana"); d.setApellido("Ruiz"); d.setCorreo("a@u.edu"); d.setPassword("secreta");
        d.setEscalafon(Escalafon.PROFESOR_TITULAR);
        d.setEstadoDocente(EstadoDocente.ACTIVO);
        d.setTipoVinculacion(TipoVinculacion.TC);
        d.setDisponibilidad(null);

        DocenteDTO dto = m.docente(d);

        assertNull(dto.getPassword());
        assertNotNull(dto.getDisponibilidad());
        assertTrue(dto.getDisponibilidad().isEmpty());
        assertEquals("PROFESOR_TITULAR", dto.getEscalafon());
        assertEquals("TC", dto.getTipoVinculacion());
    }

    @Test void docenteMapeaSuDisponibilidad() {
        Docente d = new Docente();
        d.setId("D1");
        d.setDisponibilidad(List.of(new FranjaHoraria(3, DiaSemana.MARTES, EstadoFranja.DISPONIBLE, Categoria.NOCHE,
                LocalTime.of(18, 0), LocalTime.of(20, 0))));
        FranjaHorariaDTO f = m.docente(d).getDisponibilidad().get(0);
        assertEquals(3, f.getIdFranja());
        assertEquals("MARTES", f.getDiaSemana());
        assertEquals("NOCHE", f.getCategoria());
        assertEquals(LocalTime.of(20, 0), f.getHoraFin());
    }

    @Test void estudianteConPlanExponeIdsYNombreDeCarrera() {
        Carrera c = new Carrera();
        c.setIdCarrera(3);
        c.setNombreCarrera("Sistemas");
        PlanEstudios p = new PlanEstudios();
        p.setIdPlanEstudios(10);
        p.setCarrera(c);
        Estudiante e = new Estudiante();
        e.setId("E1"); e.setPassword("secreta"); e.setSemestreCursa(4); e.setCreditosAprobados(50);
        e.setEstadoEstudiante(EstadoEstudiante.PRUEBA_ACADEMICA);
        e.setPlanEstudios(p);

        EstudianteDTO dto = m.estudiante(e);

        assertNull(dto.getPassword());
        assertEquals(10, dto.getIdPlanEstudios());
        assertEquals(3, dto.getIdCarrera());
        assertEquals("Sistemas", dto.getNombreCarrera());
        assertEquals("PRUEBA_ACADEMICA", dto.getEstadoEstudiante());
        assertEquals(4, dto.getSemestreCursa());
    }

    @Test void estudianteSinPlanNoLanza() {
        Estudiante e = new Estudiante();
        e.setId("E1");
        EstudianteDTO dto = m.estudiante(e);
        assertNull(dto.getIdPlanEstudios());
        assertNull(dto.getIdCarrera());
    }

    @Test void estudianteConPlanSinCarreraNoLanza() {
        PlanEstudios p = new PlanEstudios();
        p.setIdPlanEstudios(10);
        Estudiante e = new Estudiante();
        e.setPlanEstudios(p);
        EstudianteDTO dto = m.estudiante(e);
        assertEquals(10, dto.getIdPlanEstudios());
        assertNull(dto.getIdCarrera());
    }

    @Test void cursoSinAulaRequeridaDaNulo() {
        Curso c = new Curso();
        c.setIdCurso(1);
        assertNull(m.curso(c).getAulaRequerida());
        c.setAulaRequerida(TipoAula.ARTES);
        assertEquals("ARTES", m.curso(c).getAulaRequerida());
    }

    @Test void grupoSinEstudiantesNiSeccionesDaListasVacias() {
        Grupo g = new Grupo();
        g.setIdGrupo(1);
        g.setEstudiantes(null);
        g.setSecciones(null);
        GrupoDTO dto = m.grupo(g);
        assertTrue(dto.getEstudiantes().isEmpty());
        assertTrue(dto.getSecciones().isEmpty());
        assertNull(dto.getCurso());
        assertNull(dto.getDocente());
    }

    @Test void grupoCompletoMapeaCursoDocenteYEstudiantes() {
        Curso c = new Curso(); c.setIdCurso(8); c.setNombreCurso("Cálculo");
        Docente d = new Docente(); d.setId("D1");
        Estudiante e = new Estudiante(); e.setId("E1");
        Grupo g = new Grupo();
        g.setIdGrupo(4); g.setCurso(c); g.setDocente(d);
        g.setEstadoGrupo(EstadoGrupo.CERRADO);
        g.setCupoMaximo(30); g.setCupoMinimo(5);
        g.setEstudiantes(new ArrayList<>(List.of(e)));

        GrupoDTO dto = m.grupo(g);

        assertEquals("CERRADO", dto.getEstadoGrupo());
        assertEquals(8, dto.getCurso().getIdCurso());
        assertEquals("D1", dto.getDocente().getId());
        assertEquals("E1", dto.getEstudiantes().get(0).getId());
    }

    @Test void aulaConBloqueYSeccionConAulaYFranja() {
        Bloque b = new Bloque(1, "Bloque A", "A", "Sede norte");
        Aula a = new Aula(new AulaId(1, "A-101"), 30, TipoAula.GRUPAL, EstadoAula.MANTENIMIENTO, b);
        FranjaHoraria f = new FranjaHoraria(3, DiaSemana.LUNES, EstadoFranja.DISPONIBLE, Categoria.DIA,
                LocalTime.of(7, 0), LocalTime.of(9, 0));
        Seccion s = new Seccion();
        s.setIdSeccion(9);
        s.setEstadoSeccion(EstadoSeccion.ASIGNADA);
        s.setAula(a);
        s.setFranjaHoraria(f);

        SeccionDTO dto = m.seccion(s);

        assertEquals("ASIGNADA", dto.getEstadoSeccion());
        assertEquals("A-101", dto.getAula().getNumeroAula());
        assertEquals("MANTENIMIENTO", dto.getAula().getEstadoAula());
        assertEquals("A", dto.getAula().getBloque().getEtiquetaBloque());
        assertEquals(3, dto.getFranjaHoraria().getIdFranja());
    }

    @Test void seccionSinAulaNiFranja() {
        Seccion s = new Seccion();
        s.setIdSeccion(1);
        SeccionDTO dto = m.seccion(s);
        assertNull(dto.getAula());
        assertNull(dto.getFranjaHoraria());
    }

    @Test void aulaSinBloque() {
        Aula a = new Aula(new AulaId(1, "A1"), 10, TipoAula.GRUPAL, EstadoAula.DISPONIBLE, null);
        assertNull(m.aula(a).getBloque());
    }
}
