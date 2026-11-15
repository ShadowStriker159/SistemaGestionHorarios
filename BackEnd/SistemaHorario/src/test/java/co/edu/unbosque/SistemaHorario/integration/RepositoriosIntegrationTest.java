package co.edu.unbosque.SistemaHorario.integration;

import co.edu.unbosque.SistemaHorario.model.Seccion;
import co.edu.unbosque.SistemaHorario.model.EstadoSeccion;
import co.edu.unbosque.SistemaHorario.model.dto.*;
import co.edu.unbosque.SistemaHorario.model.exception.*;
import co.edu.unbosque.SistemaHorario.respository.*;
import co.edu.unbosque.SistemaHorario.services.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PRUEBA DE INTEGRACIÓN: servicios + repositorios escritos a mano (JDBC) + procedimientos almacenados,
 * contra una base MySQL real (sistema_horarios_test, cargada con los scripts 01, 02 y 03).
 *
 * No corre por defecto: ejecutar con  mvn test -Dhorarios.bd.pruebas=true
 * Se puede ejecutar todas las veces que se quiera: antes de cada prueba se respaldan las secciones del horario
 * y la auditoría, y después se restauran; las pruebas que crean datos los borran al terminar.
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "horarios.bd.pruebas", matches = "true")
class RepositoriosIntegrationTest {

    @Autowired JdbcTemplate jdbc;
    @Autowired FacultadService facultadService;
    @Autowired CarreraService carreraService;
    @Autowired PlanEstudiosService planService;
    @Autowired CursoService cursoService;
    @Autowired DocenteService docenteService;
    @Autowired EstudianteService estudianteService;
    @Autowired GrupoService grupoService;
    @Autowired AulaService aulaService;
    @Autowired BloqueService bloqueService;
    @Autowired HorarioService horarioService;
    @Autowired SeccionRepository seccionRepository;
    @Autowired GrupoRepository grupoRepository;

    private List<Map<String, Object>> respaldoSecciones;
    private long ultimaAuditoria;

    /** Foto del horario y de la auditoría antes de cada prueba. */
    @BeforeEach
    void respaldar() {
        respaldoSecciones = jdbc.queryForList("SELECT id_seccion, estado_seccion, id_grupo, id_bloque, numero_aula, id_franja"
                + " FROM seccion ORDER BY id_seccion");
        ultimaAuditoria = jdbc.queryForObject("SELECT COALESCE(MAX(id_auditoria), 0) FROM auditoria", Long.class);
    }

    /** Deja el horario como estaba y borra la auditoría que generó la prueba. */
    @AfterEach
    void restaurar() {
        jdbc.update("DELETE FROM seccion");
        for (Map<String, Object> s : respaldoSecciones) {
            jdbc.update("INSERT INTO seccion (id_seccion, estado_seccion, id_grupo, id_bloque, numero_aula, id_franja)"
                            + " VALUES (?, ?, ?, ?, ?, ?)",
                    s.get("id_seccion"), s.get("estado_seccion"), s.get("id_grupo"), s.get("id_bloque"),
                    s.get("numero_aula"), s.get("id_franja"));
        }
        jdbc.update("DELETE FROM auditoria WHERE id_auditoria > ?", ultimaAuditoria);
    }

    private static String sufijo() {
        return String.valueOf(System.currentTimeMillis() % 1_000_000);
    }

    // ------------------------------------------------------------------ lecturas (datos de demostración)

    @Test
    void leeLosDatosDeDemostracionConSusRelaciones() {
        DocenteDTO docente = docenteService.obtener("1001");
        assertEquals("Ana", docente.getNombre());
        assertNull(docente.getPassword(), "La contraseña nunca se expone");
        assertEquals(12, docente.getDisponibilidad().size());

        EstudianteDTO estudiante = estudianteService.obtener("2001");
        assertEquals(1, estudiante.getIdPlanEstudios());
        assertEquals("Ingeniería de Sistemas", estudiante.getNombreCarrera());

        assertEquals(7, cursoService.listarPorPlan(1).size());
        GrupoDTO grupo = grupoService.listarPorPlan(1).stream().filter(g -> g.getIdGrupo() == 1).findFirst().orElseThrow();
        assertEquals("Cálculo Diferencial", grupo.getCurso().getNombreCurso());
        assertEquals(2, grupo.getEstudiantes().size());
        assertEquals(12, grupo.getDocente().getDisponibilidad().size());
    }

    @Test
    void listarPorPlanDevuelveSoloLosGruposDeLosCursosDeEsePlan() {
        List<GrupoDTO> plan1 = grupoService.listarPorPlan(1);
        List<GrupoDTO> plan3 = grupoService.listarPorPlan(3);
        assertTrue(plan1.stream().allMatch(g -> List.of(1, 2, 3, 4, 5, 6, 7).contains(g.getCurso().getIdCurso())));
        assertTrue(plan3.stream().allMatch(g -> List.of(10, 11).contains(g.getCurso().getIdCurso())));
        assertThrows(PlanEstudiosNoEncontradoException.class, () -> grupoService.listarPorPlan(987654));
    }

    @Test
    void lasTildesYLaEnieSeGuardanYSeLeenIntactas() {
        String s = sufijo();
        FacultadDTO f = new FacultadDTO();
        f.setNombreFacultad("Facultad Ñandú " + s);
        int id = facultadService.crear(f).getIdFacultad();
        try {
            assertTrue(facultadService.listarTodos().stream()
                    .anyMatch(x -> x.getIdFacultad() == id && x.getNombreFacultad().equals("Facultad Ñandú " + s)));
        } finally {
            facultadService.eliminar(id);
        }
    }

    // ------------------------------------------------------------------ inscripción por procedimiento

    @Test
    void inscribirYRetirarUsanLosProcedimientosYQuedaAuditoria() {
        int antes = jdbc.queryForObject("select count(*) from auditoria", Integer.class);

        GrupoDTO tras = grupoService.inscribirEstudiante(1, "2006");
        assertEquals(3, tras.getEstudiantes().size());
        assertEquals(2, grupoService.retirarEstudiante(1, "2006").getEstudiantes().size());

        assertEquals(antes + 2, jdbc.queryForObject("select count(*) from auditoria", Integer.class));
    }

    @Test
    void losErroresDelProcedimientoLlegaranComoExcepcionesDeDominio() {
        assertThrows(GrupoRepetidoException.class, () -> grupoService.inscribirEstudiante(1, "2001"));
        assertThrows(GrupoNoEncontradoException.class, () -> grupoService.inscribirEstudiante(99999, "2001"));
        assertThrows(EstudianteNoEncontradoException.class, () -> grupoService.inscribirEstudiante(1, "NOEXISTE"));
        IllegalArgumentException cerrado = assertThrows(IllegalArgumentException.class,
                () -> grupoService.inscribirEstudiante(11, "2001"));
        assertEquals("El grupo está cerrado", cerrado.getMessage());
        assertThrows(IllegalArgumentException.class, () -> grupoService.inscribirEstudiante(12, "2007"));
        assertThrows(EstudianteNoEncontradoException.class, () -> grupoService.retirarEstudiante(1, "2005"));
    }

    // ------------------------------------------------------------------ escrituras con limpieza

    @Test
    void estructuraAcademicaCompletaCreaYSeBorra() {
        String s = sufijo();
        FacultadDTO fd = new FacultadDTO();
        fd.setNombreFacultad("Facultad IT " + s);
        Integer idFac = null, idCar = null, idPlan = null, idC1 = null, idC2 = null;
        try {
            idFac = facultadService.crear(fd).getIdFacultad();
            CarreraDTO cd = new CarreraDTO();
            cd.setNombreCarrera("Carrera IT " + s);
            idCar = carreraService.crear(idFac, cd).getIdCarrera();
            PlanEstudiosDTO pd = new PlanEstudiosDTO();
            pd.setNombrePlanEstudios("Plan IT " + s);
            idPlan = planService.crear(idCar, pd).getIdPlanEstudios();
            idC1 = nuevoCurso(idPlan, "Curso A " + s, null);
            CursoDTO pre = new CursoDTO();
            pre.setIdCurso(idC1);
            idC2 = nuevoCurso(idPlan, "Curso B " + s, List.of(pre));

            assertEquals(2, cursoService.listarPorPlan(idPlan).size());
            assertEquals(1, jdbc.queryForObject(
                    "select count(*) from curso_prerrequisito where id_curso = ? and id_prerrequisito = ?", Integer.class, idC2, idC1));
            final int plan = idPlan;
            assertThrows(CursoRepetidoException.class, () -> nuevoCurso(plan, "Curso A " + s, null));
            assertThrows(FacultadRepetidaException.class, () -> facultadService.crear(fd));
        } finally {
            // se borra en orden inverso, y solo lo que llegó a crearse (aunque la prueba falle a la mitad)
            if (idC2 != null) cursoService.eliminar(idC2);
            if (idC1 != null) cursoService.eliminar(idC1);
            if (idPlan != null) planService.eliminar(idPlan);
            if (idCar != null) carreraService.eliminar(idCar);
            if (idFac != null) facultadService.eliminar(idFac);
        }
        assertEquals(0, jdbc.queryForObject("select count(*) from facultad where nombre_facultad like ?", Integer.class, "%" + s));
    }

    private int nuevoCurso(int idPlan, String nombre, List<CursoDTO> prerrequisitos) {
        CursoDTO c = new CursoDTO();
        c.setNombreCurso(nombre);
        c.setCreditos(3);
        c.setIntensidadHoraria(4);
        c.setSemestre(1);
        c.setAulaRequerida("GRUPAL");
        c.setPrerrequisitos(prerrequisitos);
        return cursoService.crear(idPlan, c).getIdCurso();
    }

    @Test
    void docenteSeCreaSeEditaConservaPasswordYDisponibilidadYSeBorra() {
        String s = sufijo();
        String id = "TD" + s;
        DocenteDTO d = new DocenteDTO();
        d.setId(id);
        d.setNombre("Zoé");
        d.setApellido("Peña");
        d.setCorreo("zoe" + s + "@x.co");
        d.setPassword("clave-vieja");
        d.setEscalafon("PROFESOR_TITULAR");
        d.setEstadoDocente("ACTIVO");
        d.setTipoVinculacion("TC");
        FranjaHorariaDTO f1 = new FranjaHorariaDTO();
        f1.setIdFranja(1);
        FranjaHorariaDTO f2 = new FranjaHorariaDTO();
        f2.setIdFranja(2);
        d.setDisponibilidad(List.of(f1, f2, f1));
        docenteService.crear(d);
        try {
            assertEquals(2, docenteService.obtener(id).getDisponibilidad().size(), "Sin franjas duplicadas");

            DocenteDTO cambio = new DocenteDTO();
            cambio.setNombre("Zoé María");
            cambio.setApellido("Peña");
            cambio.setCorreo(d.getCorreo());
            cambio.setEscalafon("PROFESOR_ASOCIADO");
            cambio.setEstadoDocente("VACACIONES");
            cambio.setTipoVinculacion("MTO");
            docenteService.actualizar(id, cambio);
            assertEquals("Zoé María", docenteService.obtener(id).getNombre());
            assertEquals("clave-vieja", jdbc.queryForObject("select password from persona where id = ?", String.class, id));
            assertEquals(2, docenteService.obtener(id).getDisponibilidad().size());

            docenteService.actualizarDisponibilidad(id, List.of(f2));
            assertEquals(1, docenteService.obtener(id).getDisponibilidad().size());

            DocenteDTO choque = new DocenteDTO();
            choque.setNombre("X");
            choque.setApellido("Y");
            choque.setCorreo("luis.mora@ejemplo.edu.co");
            choque.setEscalafon("PROFESOR_TITULAR");
            choque.setEstadoDocente("ACTIVO");
            choque.setTipoVinculacion("TC");
            assertThrows(DocenteRepetidoException.class, () -> docenteService.actualizar(id, choque));
        } finally {
            docenteService.eliminar(id);
        }
    }

    @Test
    void estudianteSeCreaSeEditaYCambiaDePlan() {
        String s = sufijo();
        String id = "TE" + s;
        EstudianteDTO e = new EstudianteDTO();
        e.setId(id);
        e.setNombre("Íñigo");
        e.setApellido("Núñez");
        e.setCorreo("inigo" + s + "@x.co");
        e.setPassword("p1");
        e.setSemestreCursa(1);
        e.setCreditosAprobados(0);
        e.setEstadoEstudiante("ACTIVO");
        e.setIdPlanEstudios(1);
        e.setIdCarrera(1);
        estudianteService.crear(e);
        try {
            EstudianteDTO cambio = new EstudianteDTO();
            cambio.setNombre("Íñigo");
            cambio.setApellido("Núñez");
            cambio.setCorreo(e.getCorreo());
            cambio.setSemestreCursa(5);
            cambio.setCreditosAprobados(70);
            cambio.setEstadoEstudiante("PRUEBA_ACADEMICA");
            estudianteService.actualizar(id, cambio);
            EstudianteDTO leido = estudianteService.obtener(id);
            assertEquals(5, leido.getSemestreCursa());
            assertEquals(1, leido.getIdPlanEstudios(), "Sin idPlanEstudios el plan no cambia");
            assertEquals("p1", jdbc.queryForObject("select password from persona where id = ?", String.class, id));

            estudianteService.asignarPlan(id, 2);
            assertEquals(2, estudianteService.obtener(id).getIdPlanEstudios());
            assertEquals("Ingeniería Industrial", estudianteService.obtener(id).getNombreCarrera());
        } finally {
            estudianteService.eliminar(id);
        }
    }

    @Test
    void lasLlavesForaneasImpidenBorrarLoQueEstaEnUso() {
        assertThrows(DocenteConGruposException.class, () -> docenteService.eliminar("1001"));
        assertThrows(DataIntegrityViolationException.class, () -> planService.eliminar(1));
        assertThrows(DataIntegrityViolationException.class, () -> cursoService.eliminar(1));
        // el rechazo no dejó el curso a medias: sigue en su plan y con sus prerrequisitos
        assertEquals(7, cursoService.listarPorPlan(1).size());
        assertEquals(1, jdbc.queryForObject("select count(*) from curso_prerrequisito where id_curso = 4", Integer.class));
    }

    @Test
    void infraestructuraSeCreaYSeBorra() {
        String s = sufijo();
        int antes = aulaService.listarTodos().size();
        int idB = bloqueService.crear(new BloqueDTO(null, "Bloque Z " + s, "Z" + s.substring(0, 3), "Sede oeste")).getIdBloque();
        try {
            aulaService.crear(idB, new AulaDTO("Z-1", 20, "GRUPAL", null, null));
            assertEquals(antes + 1, aulaService.listarTodos().size());
            assertThrows(AulaRepetidaException.class, () -> aulaService.crear(idB, new AulaDTO("Z-1", 20, "GRUPAL", null, null)));
            aulaService.eliminar(idB, "Z-1");
        } finally {
            bloqueService.eliminar(idB);
        }
        assertEquals(antes, aulaService.listarTodos().size());
    }

    // ------------------------------------------------------------------ horario

    @Test
    void generarHorarioGuardaSeccionesSinChoquesYSeLeenConTodosSusDatos() {
        List<HorarioDTO> generado = horarioService.generarHorario(1);

        assertFalse(generado.isEmpty());
        assertEquals(generado.size(), jdbc.queryForObject("select count(*) from seccion", Integer.class));
        assertEquals(0, jdbc.queryForObject(
                "select count(*) from (select id_bloque, numero_aula, id_franja from seccion group by 1,2,3 having count(*) > 1) x", Integer.class),
                "Un aula no puede estar ocupada dos veces en la misma franja");
        assertEquals(0, jdbc.queryForObject(
                "select count(*) from (select g.id_docente, s.id_franja from seccion s join grupo g on g.id_grupo = s.id_grupo"
                        + " group by 1,2 having count(*) > 1) x", Integer.class),
                "Un docente no puede estar en dos sitios a la vez");
        assertEquals(0, jdbc.queryForObject(
                "select count(*) from seccion s join grupo g on g.id_grupo = s.id_grupo"
                        + " left join docente_franja df on df.id_docente = g.id_docente and df.id_franja = s.id_franja"
                        + " where df.id_franja is null", Integer.class),
                "Cada sección debe caer en una franja disponible del docente");

        List<HorarioDTO> leidos = horarioService.listarHorarios();
        assertEquals(generado.size(), leidos.size());
        HorarioDTO h = leidos.get(0);
        assertNotNull(h.getPrograma());
        assertTrue(h.getNombreDocente().contains(" "));
        assertNotNull(h.getMateria());
        assertNotNull(h.getEspacioFisico());
        assertNotNull(h.getBloque());
        assertNotNull(h.getDia());

        assertEquals(leidos.size(), horarioService.generarHorario(1).size(), "Regenerar reemplaza, no duplica");
        assertEquals(leidos.size(), jdbc.queryForObject("select count(*) from seccion", Integer.class));
        assertEquals(1, grupoService.listarPorCurso(1).stream().filter(g -> g.getIdGrupo() == 1).findFirst()
                .orElseThrow().getSecciones().size(), "El grupo trae su sección con aula y franja");
        assertThrows(FacultadNoEncontradaException.class, () -> horarioService.generarHorario(99999));
    }

    @Test
    void guardarSeccionesEsAtomicoSiUnaFallaNoQuedaNinguna() {
        seccionRepository.deleteAll();
        // una fila válida y otra con una franja que no existe: la segunda viola la llave foránea
        var aula0 = jdbc.queryForMap("select id_bloque, numero_aula from aula order by id_bloque, numero_aula limit 1");
        var aula1 = jdbc.queryForMap("select id_bloque, numero_aula from aula order by id_bloque, numero_aula limit 1 offset 1");
        List<Seccion> lote = new ArrayList<>();
        lote.add(seccion(1, aula0, 1));
        lote.add(seccion(2, aula1, 99999));
        assertThrows(RuntimeException.class, () -> seccionRepository.saveAll(lote));
        assertEquals(0, jdbc.queryForObject("select count(*) from seccion", Integer.class),
                "ROLLBACK: la sección válida tampoco se guardó");
    }

    private Seccion seccion(int idGrupo, java.util.Map<String, Object> aula, int idFranja) {
        var grupo = grupoRepository.findById(idGrupo).orElseThrow();
        var franja = new co.edu.unbosque.SistemaHorario.model.FranjaHoraria(idFranja,
                co.edu.unbosque.SistemaHorario.model.DiaSemana.LUNES, co.edu.unbosque.SistemaHorario.model.EstadoFranja.DISPONIBLE,
                co.edu.unbosque.SistemaHorario.model.Categoria.DIA, java.time.LocalTime.of(7, 0), java.time.LocalTime.of(8, 0));
        var aulaEntidad = new co.edu.unbosque.SistemaHorario.model.Aula(
                new co.edu.unbosque.SistemaHorario.model.AulaId(((Number) aula.get("id_bloque")).intValue(), (String) aula.get("numero_aula")),
                10, null, null, null);
        var s = new Seccion(null, EstadoSeccion.ASIGNADA, aulaEntidad, franja);
        s.setGrupo(grupo);
        return s;
    }
}
