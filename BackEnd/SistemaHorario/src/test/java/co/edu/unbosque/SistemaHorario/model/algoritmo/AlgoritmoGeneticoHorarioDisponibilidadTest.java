package co.edu.unbosque.SistemaHorario.model.algoritmo;

import co.edu.unbosque.SistemaHorario.model.*;
import co.edu.unbosque.SistemaHorario.model.exception.HorarioInalcanzableException;
import co.edu.unbosque.SistemaHorario.respository.AulaRepository;
import co.edu.unbosque.SistemaHorario.respository.FranjaHorariaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlgoritmoGeneticoHorarioDisponibilidadTest {

    private AlgoritmoGeneticoHorario algoritmo;
    private FranjaHoraria franjaNoElegida;
    private FranjaHoraria franjaPermitida;

    @BeforeEach
    void setUp() {
        AulaRepository aulaRepository = mock(AulaRepository.class);
        FranjaHorariaRepository franjaRepository = mock(FranjaHorariaRepository.class);
        Aula aula = new Aula(
                new AulaId(1, "101"), 30, TipoAula.GRUPAL, EstadoAula.DISPONIBLE, null);
        franjaNoElegida = crearFranja(1);
        franjaPermitida = crearFranja(2);

        when(aulaRepository.findByEstadoAula(EstadoAula.DISPONIBLE)).thenReturn(List.of(aula));
        when(franjaRepository.findByEstadoFranja(EstadoFranja.DISPONIBLE))
                .thenReturn(List.of(franjaNoElegida, franjaPermitida));

        algoritmo = new AlgoritmoGeneticoHorario();
        ReflectionTestUtils.setField(algoritmo, "aulaRepository", aulaRepository);
        ReflectionTestUtils.setField(algoritmo, "franjaHorariaRepository", franjaRepository);
    }

    @Test
    void horarioSoloUsaFranjasDeclaradasPorElDocente() {
        Facultad facultad = crearFacultad(List.of(franjaPermitida));

        List<Seccion> horario = algoritmo.generarHorario(facultad);

        assertEquals(1, horario.size());
        assertEquals(franjaPermitida.getIdFranja(),
                horario.get(0).getFranjaHoraria().getIdFranja());
    }

    @Test
    void fallaConMensajeClaroSiDocenteNoRegistroDisponibilidad() {
        Facultad facultad = crearFacultad(List.of());

        HorarioInalcanzableException error = assertThrows(
                HorarioInalcanzableException.class,
                () -> algoritmo.generarHorario(facultad));

        assertTrue(error.getMessage().contains("no tiene franjas disponibles"));
    }

    private Facultad crearFacultad(List<FranjaHoraria> disponibilidad) {
        Docente docente = new Docente();
        docente.setId("D-1");
        docente.setNombre("Ana");
        docente.setApellido("Docente");
        docente.setDisponibilidad(disponibilidad);

        Grupo grupo = new Grupo();
        grupo.setIdGrupo(1);
        grupo.setDocente(docente);
        grupo.setCurso(crearCurso(grupo));

        Curso curso = grupo.getCurso();
        PlanEstudios plan = new PlanEstudios();
        plan.setCursos(List.of(curso));

        Carrera carrera = new Carrera();
        carrera.setPlanEstudios(List.of(plan));

        Facultad facultad = new Facultad();
        facultad.setCarreras(List.of(carrera));
        return facultad;
    }

    private Curso crearCurso(Grupo grupo) {
        Curso curso = new Curso();
        curso.setIdCurso(1);
        curso.setAulaRequerida(TipoAula.GRUPAL);
        curso.setGrupos(List.of(grupo));
        return curso;
    }

    private FranjaHoraria crearFranja(Integer id) {
        return new FranjaHoraria(id, DiaSemana.LUNES, EstadoFranja.DISPONIBLE,
                Categoria.DIA, LocalTime.of(7, 0), LocalTime.of(9, 0));
    }
}
