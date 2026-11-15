package co.edu.unbosque.SistemaHorario.model.algoritmo;

import co.edu.unbosque.SistemaHorario.model.*;
import co.edu.unbosque.SistemaHorario.model.exception.HorarioInalcanzableException;
import co.edu.unbosque.SistemaHorario.respository.AulaRepository;
import co.edu.unbosque.SistemaHorario.respository.FranjaHorariaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * PRUEBA UNITARIA: algoritmo genético de horarios. Como es aleatorio, no se compara contra un resultado
 * fijo: se verifican las propiedades que SIEMPRE deben cumplirse (sin choques, disponibilidad del
 * docente, tipo de aula) y los mensajes de error cuando no hay solución.
 */
class AlgoritmoGeneticoHorarioTest {

    private AulaRepository aulaRepo;
    private FranjaHorariaRepository franjaRepo;
    private AlgoritmoGeneticoHorario algoritmo;

    @BeforeEach
    void preparar() {
        aulaRepo = mock(AulaRepository.class);
        franjaRepo = mock(FranjaHorariaRepository.class);
        algoritmo = new AlgoritmoGeneticoHorario();
        ReflectionTestUtils.setField(algoritmo, "aulaRepository", aulaRepo);
        ReflectionTestUtils.setField(algoritmo, "franjaHorariaRepository", franjaRepo);
    }

    // ---------- fábricas de datos ----------
    private static FranjaHoraria franja(int id) {
        return new FranjaHoraria(id, DiaSemana.LUNES, EstadoFranja.DISPONIBLE, Categoria.DIA,
                LocalTime.of(6 + id, 0), LocalTime.of(7 + id, 0));
    }

    private static Aula aula(int numero, TipoAula tipo) {
        return new Aula(new AulaId(1, String.valueOf(numero)), 30, tipo, EstadoAula.DISPONIBLE, null);
    }

    private static Docente docente(String id, FranjaHoraria... disponibles) {
        Docente d = new Docente();
        d.setId(id);
        d.setNombre("Doc " + id);
        d.setApellido("Apellido");
        d.setDisponibilidad(new ArrayList<>(List.of(disponibles)));
        return d;
    }

    private static Grupo grupo(int id, Docente docente, TipoAula tipo) {
        Curso curso = new Curso();
        curso.setIdCurso(id);
        curso.setAulaRequerida(tipo);
        Grupo g = new Grupo();
        g.setIdGrupo(id);
        g.setDocente(docente);
        g.setCurso(curso);
        curso.setGrupos(new ArrayList<>(List.of(g)));
        return g;
    }

    private static Facultad facultad(Grupo... grupos) {
        List<Curso> cursos = new ArrayList<>();
        for (Grupo g : grupos) cursos.add(g.getCurso());
        PlanEstudios plan = new PlanEstudios();
        plan.setCursos(cursos);
        Carrera carrera = new Carrera();
        carrera.setPlanEstudios(new ArrayList<>(List.of(plan)));
        Facultad f = new Facultad();
        f.setCarreras(new ArrayList<>(List.of(carrera)));
        return f;
    }

    private void recursos(List<Aula> aulas, List<FranjaHoraria> franjas) {
        when(aulaRepo.findByEstadoAula(EstadoAula.DISPONIBLE)).thenReturn(aulas);
        when(franjaRepo.findByEstadoFranja(EstadoFranja.DISPONIBLE)).thenReturn(franjas);
    }

    // ---------- propiedades del resultado ----------
    @Test
    void unDocenteConDosGruposNuncaChocaEnLaMismaFranja() {
        FranjaHoraria f1 = franja(1), f2 = franja(2);
        recursos(List.of(aula(101, TipoAula.GRUPAL)), List.of(f1, f2));
        Docente d = docente("D1", f1, f2);

        List<Seccion> r = algoritmo.generarHorario(facultad(grupo(1, d, TipoAula.GRUPAL), grupo(2, d, TipoAula.GRUPAL)));

        assertEquals(2, r.size());
        assertNotEquals(r.get(0).getFranjaHoraria().getIdFranja(), r.get(1).getFranjaHoraria().getIdFranja());
    }

    @Test
    void dosDocentesConUnaSolaAulaNoComparteAulaYFranja() {
        FranjaHoraria f1 = franja(1), f2 = franja(2);
        recursos(List.of(aula(101, TipoAula.GRUPAL)), List.of(f1, f2));

        List<Seccion> r = algoritmo.generarHorario(facultad(
                grupo(1, docente("D1", f1, f2), TipoAula.GRUPAL),
                grupo(2, docente("D2", f1, f2), TipoAula.GRUPAL)));

        assertNotEquals(r.get(0).getFranjaHoraria().getIdFranja(), r.get(1).getFranjaHoraria().getIdFranja());
    }

    @Test
    void respetaElTipoDeAulaRequerido() {
        FranjaHoraria f1 = franja(1);
        recursos(List.of(aula(101, TipoAula.GRUPAL), aula(102, TipoAula.SISTEMAS)), List.of(f1));

        List<Seccion> r = algoritmo.generarHorario(facultad(grupo(1, docente("D1", f1), TipoAula.SISTEMAS)));

        assertEquals(TipoAula.SISTEMAS, r.get(0).getAula().getTipoAula());
    }

    @Test
    void conRecursosHolgadosNoHayNingunChoque() {
        List<FranjaHoraria> franjas = new ArrayList<>();
        for (int i = 1; i <= 6; i++) franjas.add(franja(i));
        FranjaHoraria[] todas = franjas.toArray(new FranjaHoraria[0]);
        recursos(List.of(aula(101, TipoAula.GRUPAL), aula(102, TipoAula.GRUPAL), aula(103, TipoAula.GRUPAL)), franjas);
        Docente d1 = docente("D1", todas), d2 = docente("D2", todas), d3 = docente("D3", todas);

        List<Seccion> r = algoritmo.generarHorario(facultad(
                grupo(1, d1, TipoAula.GRUPAL), grupo(2, d1, TipoAula.GRUPAL),
                grupo(3, d2, TipoAula.GRUPAL), grupo(4, d2, TipoAula.GRUPAL),
                grupo(5, d3, TipoAula.GRUPAL), grupo(6, d3, TipoAula.GRUPAL)));

        assertEquals(6, r.size());
        Set<String> aulaFranja = new HashSet<>();
        Set<String> docenteFranja = new HashSet<>();
        for (Seccion s : r) {
            String claveAula = s.getAula().getNumeroAula() + "@" + s.getFranjaHoraria().getIdFranja();
            String claveDocente = s.getGrupo().getDocente().getId() + "@" + s.getFranjaHoraria().getIdFranja();
            assertTrue(aulaFranja.add(claveAula), "Aula y franja repetidas: " + claveAula);
            assertTrue(docenteFranja.add(claveDocente), "Docente en dos sitios a la vez: " + claveDocente);
        }
    }

    @Test
    void soloUsaFranjasEnLasQueElDocenteEstaDisponible() {
        FranjaHoraria f1 = franja(1), f2 = franja(2), f3 = franja(3);
        recursos(List.of(aula(101, TipoAula.GRUPAL), aula(102, TipoAula.GRUPAL)), List.of(f1, f2, f3));
        Docente soloLaTercera = docente("D1", f3);

        List<Seccion> r = algoritmo.generarHorario(facultad(grupo(1, soloLaTercera, TipoAula.GRUPAL)));

        assertEquals(3, r.get(0).getFranjaHoraria().getIdFranja());
    }

    @Test
    void lasSeccionesGeneradasVienenAsignadasYSinId() {
        FranjaHoraria f1 = franja(1);
        recursos(List.of(aula(101, TipoAula.GRUPAL)), List.of(f1));
        Seccion s = algoritmo.generarHorario(facultad(grupo(1, docente("D1", f1), TipoAula.GRUPAL))).get(0);
        assertEquals(EstadoSeccion.ASIGNADA, s.getEstadoSeccion());
        assertNull(s.getIdSeccion());
        assertNotNull(s.getGrupo());
    }

    @Test
    void ejecutarlaDosVecesSeguidasFunciona() {
        FranjaHoraria f1 = franja(1), f2 = franja(2);
        recursos(List.of(aula(101, TipoAula.GRUPAL)), List.of(f1, f2));
        Facultad f = facultad(grupo(1, docente("D1", f1, f2), TipoAula.GRUPAL));
        assertEquals(1, algoritmo.generarHorario(f).size());
        assertEquals(1, algoritmo.generarHorario(f).size());
    }

    @Test
    void unGrupoRepetidoEnDosPlanesSeProgramaUnaSolaVez() {
        FranjaHoraria f1 = franja(1);
        recursos(List.of(aula(101, TipoAula.GRUPAL)), List.of(f1));
        Grupo g = grupo(1, docente("D1", f1), TipoAula.GRUPAL);
        PlanEstudios plan1 = new PlanEstudios();
        plan1.setCursos(List.of(g.getCurso()));
        PlanEstudios plan2 = new PlanEstudios();
        plan2.setCursos(List.of(g.getCurso()));
        Carrera carrera = new Carrera();
        carrera.setPlanEstudios(List.of(plan1, plan2));
        Facultad f = new Facultad();
        f.setCarreras(List.of(carrera));

        assertEquals(1, algoritmo.generarHorario(f).size());
    }

    // ---------- errores ----------
    @Test
    void sinSolucionLanzaInalcanzable() {
        FranjaHoraria f1 = franja(1), f2 = franja(2);
        recursos(List.of(aula(101, TipoAula.GRUPAL), aula(102, TipoAula.GRUPAL)), List.of(f1, f2));
        Docente d = docente("D1", f1, f2);

        HorarioInalcanzableException e = assertThrows(HorarioInalcanzableException.class, () ->
                algoritmo.generarHorario(facultad(
                        grupo(1, d, TipoAula.GRUPAL), grupo(2, d, TipoAula.GRUPAL), grupo(3, d, TipoAula.GRUPAL))));

        assertEquals("No fue posible generar un horario sin conflictos.", e.getMessage());
    }

    @Test
    void facultadNulaOSinGruposNoTieneSeccionesBase() {
        recursos(List.of(aula(101, TipoAula.GRUPAL)), List.of(franja(1)));
        assertEquals("No hay secciones base para generar el horario.",
                assertThrows(HorarioInalcanzableException.class, () -> algoritmo.generarHorario(null)).getMessage());
        assertEquals("No hay secciones base para generar el horario.",
                assertThrows(HorarioInalcanzableException.class, () -> algoritmo.generarHorario(new Facultad())).getMessage());
    }

    @Test
    void sinAulasDisponibles() {
        FranjaHoraria f1 = franja(1);
        recursos(List.of(), List.of(f1));
        HorarioInalcanzableException e = assertThrows(HorarioInalcanzableException.class,
                () -> algoritmo.generarHorario(facultad(grupo(1, docente("D1", f1), TipoAula.GRUPAL))));
        assertEquals("No hay aulas disponibles.", e.getMessage());
    }

    @Test
    void sinFranjasDisponibles() {
        FranjaHoraria f1 = franja(1);
        recursos(List.of(aula(101, TipoAula.GRUPAL)), List.of());
        HorarioInalcanzableException e = assertThrows(HorarioInalcanzableException.class,
                () -> algoritmo.generarHorario(facultad(grupo(1, docente("D1", f1), TipoAula.GRUPAL))));
        assertEquals("No hay franjas disponibles.", e.getMessage());
    }

    @Test
    void grupoSinDocente() {
        recursos(List.of(aula(101, TipoAula.GRUPAL)), List.of(franja(1)));
        HorarioInalcanzableException e = assertThrows(HorarioInalcanzableException.class,
                () -> algoritmo.generarHorario(facultad(grupo(7, null, TipoAula.GRUPAL))));
        assertTrue(e.getMessage().contains("no tiene un docente asignado"));
        assertTrue(e.getMessage().contains("7"));
    }

    @Test
    void docenteDisponibleSoloEnFranjasQueNoExistenEnElHorarioGlobal() {
        recursos(List.of(aula(101, TipoAula.GRUPAL)), List.of(franja(1)));
        Docente d = docente("D1", franja(5));
        HorarioInalcanzableException e = assertThrows(HorarioInalcanzableException.class,
                () -> algoritmo.generarHorario(facultad(grupo(1, d, TipoAula.GRUPAL))));
        assertTrue(e.getMessage().contains("no tiene franjas disponibles compatibles"));
    }
}
