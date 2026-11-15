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

/** PRUEBA UNITARIA: bloques, aulas y franjas horarias. */
class InfraestructuraServiceTest {

    private BloqueRepository bloqueRepo;
    private AulaRepository aulaRepo;
    private FranjaHorariaRepository franjaRepo;
    private BloqueService bloqueService;
    private AulaService aulaService;
    private FranjaHorariaService franjaService;
    private Bloque bloque;

    @BeforeEach
    void preparar() {
        bloqueRepo = mock(BloqueRepository.class);
        aulaRepo = mock(AulaRepository.class);
        franjaRepo = mock(FranjaHorariaRepository.class);
        MapeadorDTO m = new MapeadorDTO();
        bloqueService = new BloqueService(bloqueRepo, m);
        aulaService = new AulaService(aulaRepo, bloqueRepo, m);
        franjaService = new FranjaHorariaService(franjaRepo, m);
        bloque = new Bloque(1, "Bloque A", "A", "Sede norte");
        when(bloqueRepo.findById(1)).thenReturn(Optional.of(bloque));
        when(bloqueRepo.save(any(Bloque.class))).thenAnswer(i -> i.getArgument(0));
        when(aulaRepo.save(any(Aula.class))).thenAnswer(i -> i.getArgument(0));
        when(franjaRepo.save(any(FranjaHoraria.class))).thenAnswer(i -> i.getArgument(0));
    }

    // ---- Bloque
    private BloqueDTO bloqueDto(String etiqueta, String nombre, String ubicacion) {
        return new BloqueDTO(null, nombre, etiqueta, ubicacion);
    }
    @Test void bloqueCrearValido() {
        BloqueDTO r = bloqueService.crear(bloqueDto(" B ", "Bloque B", "Sede sur"));
        assertEquals("B", r.getEtiquetaBloque());
    }
    @Test void bloqueCrearEtiquetaRepetida() {
        when(bloqueRepo.existsByEtiquetaBloque("A")).thenReturn(true);
        assertThrows(BloqueRepetidoException.class, () -> bloqueService.crear(bloqueDto("A", "x", "y")));
    }
    @Test void bloqueCrearCamposObligatorios() {
        assertThrows(IllegalArgumentException.class, () -> bloqueService.crear(bloqueDto("", "x", "y")));
        assertThrows(IllegalArgumentException.class, () -> bloqueService.crear(bloqueDto("C", " ", "y")));
        assertThrows(IllegalArgumentException.class, () -> bloqueService.crear(bloqueDto("C", "x", null)));
    }
    @Test void bloqueListarYEliminar() {
        when(bloqueRepo.findAll()).thenReturn(List.of(bloque));
        assertEquals(1, bloqueService.listarTodos().size());
        assertEquals(1, bloqueService.eliminar(1).getIdBloque());
    }
    @Test void bloqueEliminarInexistente() {
        when(bloqueRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(BloqueNoEncontradoException.class, () -> bloqueService.eliminar(9));
    }

    // ---- Aula
    private AulaDTO aulaDto(String numero, Integer capacidad, String tipo, String estado) {
        return new AulaDTO(numero, capacidad, tipo, estado, null);
    }
    @Test void aulaCrearValidaPorDefectoQuedaDisponible() {
        AulaDTO r = aulaService.crear(1, aulaDto("A-101", 30, "GRUPAL", null));
        assertEquals("A-101", r.getNumeroAula());
        assertEquals("DISPONIBLE", r.getEstadoAula());
        assertEquals("GRUPAL", r.getTipoAula());
    }
    @Test void aulaCrearConEstadoExplicito() {
        assertEquals("MANTENIMIENTO", aulaService.crear(1, aulaDto("A1", 10, "SISTEMAS", "MANTENIMIENTO")).getEstadoAula());
    }
    @Test void aulaCrearAceptaLetrasConTildeYNumeros() {
        assertDoesNotThrow(() -> aulaService.crear(1, aulaDto("Ñandú-1", 10, "GRUPAL", null)));
    }
    @Test void aulaCrearNombreInvalido() {
        for (String malo : new String[]{"A 101", "A_101", "ABCDEFGHIJK", "A/1"}) {
            assertThrows(IllegalArgumentException.class, () -> aulaService.crear(1, aulaDto(malo, 10, "GRUPAL", null)), malo);
        }
    }
    @Test void aulaCrearNombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> aulaService.crear(1, aulaDto(" ", 10, "GRUPAL", null)));
    }
    @Test void aulaCrearBloqueInexistente() {
        when(bloqueRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(BloqueNoEncontradoException.class, () -> aulaService.crear(9, aulaDto("A1", 10, "GRUPAL", null)));
    }
    @Test void aulaCrearRepetidaEnElBloque() {
        when(aulaRepo.existsById(new AulaId(1, "A1"))).thenReturn(true);
        assertThrows(AulaRepetidaException.class, () -> aulaService.crear(1, aulaDto("A1", 10, "GRUPAL", null)));
    }
    @Test void aulaCrearCapacidadYTipoInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> aulaService.crear(1, aulaDto("A1", 0, "GRUPAL", null)));
        assertThrows(IllegalArgumentException.class, () -> aulaService.crear(1, aulaDto("A1", 10, "NOEXISTE", null)));
        assertThrows(IllegalArgumentException.class, () -> aulaService.crear(1, aulaDto("A1", 10, null, null)));
        assertThrows(IllegalArgumentException.class, () -> aulaService.crear(1, aulaDto("A1", 10, "GRUPAL", "ROTA")));
    }
    @Test void aulaEliminarYListar() {
        Aula a = new Aula(new AulaId(1, "A1"), 10, TipoAula.GRUPAL, EstadoAula.DISPONIBLE, bloque);
        when(aulaRepo.findById(new AulaId(1, "A1"))).thenReturn(Optional.of(a));
        when(aulaRepo.findAll()).thenReturn(List.of(a));
        assertEquals(1, aulaService.listarTodos().size());
        assertEquals("A1", aulaService.eliminar(1, "A1").getNumeroAula());
        verify(aulaRepo).delete(a);
    }
    @Test void aulaEliminarInexistente() {
        when(aulaRepo.findById(any())).thenReturn(Optional.empty());
        assertThrows(AulaNoEncontradaException.class, () -> aulaService.eliminar(1, "Z9"));
    }

    // ---- Franja horaria
    private FranjaHorariaDTO franjaDto(String dia, String cat, LocalTime ini, LocalTime fin, String estado) {
        return new FranjaHorariaDTO(null, dia, estado, cat, ini, fin);
    }
    @Test void franjaCrearValidaPorDefectoDisponible() {
        FranjaHorariaDTO r = franjaService.crear(franjaDto("LUNES", "DIA", LocalTime.of(7, 0), LocalTime.of(9, 0), null));
        assertEquals("LUNES", r.getDiaSemana());
        assertEquals("DISPONIBLE", r.getEstadoFranja());
        assertEquals(LocalTime.of(9, 0), r.getHoraFin());
    }
    @Test void franjaCrearBloqueada() {
        assertEquals("BLOQUEADA", franjaService.crear(
                franjaDto("MARTES", "NOCHE", LocalTime.of(18, 0), LocalTime.of(20, 0), "BLOQUEADA")).getEstadoFranja());
    }
    @Test void franjaFinAnteriorOIgualAlInicioEsInvalida() {
        assertThrows(IllegalArgumentException.class, () -> franjaService.crear(
                franjaDto("LUNES", "DIA", LocalTime.of(9, 0), LocalTime.of(7, 0), null)));
        assertThrows(IllegalArgumentException.class, () -> franjaService.crear(
                franjaDto("LUNES", "DIA", LocalTime.of(9, 0), LocalTime.of(9, 0), null)));
    }
    @Test void franjaSinHorasEsInvalida() {
        assertThrows(IllegalArgumentException.class, () -> franjaService.crear(franjaDto("LUNES", "DIA", null, LocalTime.of(9, 0), null)));
        assertThrows(IllegalArgumentException.class, () -> franjaService.crear(franjaDto("LUNES", "DIA", LocalTime.of(7, 0), null, null)));
    }
    @Test void franjaDiaOCategoriaInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> franjaService.crear(franjaDto("DOMINGO", "DIA", LocalTime.of(7, 0), LocalTime.of(9, 0), null)));
        assertThrows(IllegalArgumentException.class, () -> franjaService.crear(franjaDto("LUNES", "MADRUGADA", LocalTime.of(7, 0), LocalTime.of(9, 0), null)));
    }
    @Test void franjaRepetida() {
        when(franjaRepo.existsByDiaSemanaAndHoraInicioAndHoraFin(DiaSemana.LUNES, LocalTime.of(7, 0), LocalTime.of(9, 0))).thenReturn(true);
        assertThrows(FranjaHorariaRepetidaException.class, () -> franjaService.crear(
                franjaDto("LUNES", "DIA", LocalTime.of(7, 0), LocalTime.of(9, 0), null)));
    }
    @Test void franjaListarYEliminar() {
        FranjaHoraria f = new FranjaHoraria(3, DiaSemana.JUEVES, EstadoFranja.DISPONIBLE, Categoria.TARDE, LocalTime.of(14, 0), LocalTime.of(16, 0));
        when(franjaRepo.findAll()).thenReturn(List.of(f));
        when(franjaRepo.findById(3)).thenReturn(Optional.of(f));
        assertEquals(1, franjaService.listarTodos().size());
        assertEquals(3, franjaService.eliminar(3).getIdFranja());
    }
    @Test void franjaEliminarInexistente() {
        when(franjaRepo.findById(9)).thenReturn(Optional.empty());
        assertThrows(FranjaHorariaNoEncontradaException.class, () -> franjaService.eliminar(9));
    }
}
