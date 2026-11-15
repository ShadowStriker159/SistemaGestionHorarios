package co.edu.unbosque.SistemaHorario.service;

import co.edu.unbosque.SistemaHorario.model.*;
import co.edu.unbosque.SistemaHorario.model.dto.DocenteDTO;
import co.edu.unbosque.SistemaHorario.model.dto.FranjaHorariaDTO;
import co.edu.unbosque.SistemaHorario.model.exception.DocenteNoEncontradoException;
import co.edu.unbosque.SistemaHorario.model.exception.DocenteRepetidoException;
import co.edu.unbosque.SistemaHorario.respository.DocenteRepository;
import co.edu.unbosque.SistemaHorario.respository.FranjaHorariaRepository;
import co.edu.unbosque.SistemaHorario.respository.GrupoRepository;
import co.edu.unbosque.SistemaHorario.respository.PersonaRepository;
import co.edu.unbosque.SistemaHorario.services.DocenteService;
import co.edu.unbosque.SistemaHorario.services.MapeadorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** PRUEBA UNITARIA: lógica de DocenteService.actualizar con repositorios simulados. */
class DocenteServiceActualizarTest {

    private DocenteRepository docenteRepository;
    private PersonaRepository personaRepository;
    private FranjaHorariaRepository franjaRepository;
    private DocenteService service;
    private Docente existente;

    @BeforeEach
    void preparar() {
        docenteRepository = mock(DocenteRepository.class);
        personaRepository = mock(PersonaRepository.class);
        franjaRepository = mock(FranjaHorariaRepository.class);
        service = new DocenteService(docenteRepository, personaRepository, franjaRepository,
                mock(GrupoRepository.class), new MapeadorDTO());

        existente = new Docente();
        existente.setId("D1");
        existente.setNombre("Ana");
        existente.setApellido("Ruiz");
        existente.setCorreo("ana@u.edu");
        existente.setPassword("clave-vieja");
        existente.setEscalafon(Escalafon.PROFESOR_TITULAR);
        existente.setEstadoDocente(EstadoDocente.ACTIVO);
        existente.setTipoVinculacion(TipoVinculacion.TC);
        FranjaHoraria f = new FranjaHoraria(7, DiaSemana.LUNES, EstadoFranja.DISPONIBLE, Categoria.DIA,
                LocalTime.of(7, 0), LocalTime.of(9, 0));
        existente.setDisponibilidad(new ArrayList<>(List.of(f)));

        when(docenteRepository.findById("D1")).thenReturn(Optional.of(existente));
        when(docenteRepository.save(any(Docente.class))).thenAnswer(i -> i.getArgument(0));
    }

    private DocenteDTO dto() {
        DocenteDTO dto = new DocenteDTO();
        dto.setNombre("Ana María");
        dto.setApellido("Ruiz");
        dto.setCorreo("ana.nueva@u.edu");
        dto.setEscalafon("PROFESOR_ASOCIADO");
        dto.setEstadoDocente("VACACIONES");
        dto.setTipoVinculacion("MTO");
        return dto;
    }

    @Test
    void actualizaLosCamposBasicos() {
        DocenteDTO resultado = service.actualizar("D1", dto());
        assertEquals("Ana María", resultado.getNombre());
        assertEquals("ana.nueva@u.edu", resultado.getCorreo());
        assertEquals("PROFESOR_ASOCIADO", resultado.getEscalafon());
        assertEquals("VACACIONES", resultado.getEstadoDocente());
        assertEquals("MTO", resultado.getTipoVinculacion());
        assertNull(resultado.getPassword(), "El DTO de respuesta nunca debe exponer la contraseña");
    }

    @Test
    void sinPasswordConservaLaAnterior() {
        service.actualizar("D1", dto());
        assertEquals("clave-vieja", existente.getPassword());
    }

    @Test
    void passwordEnBlancoConservaLaAnterior() {
        DocenteDTO dto = dto();
        dto.setPassword("   ");
        service.actualizar("D1", dto);
        assertEquals("clave-vieja", existente.getPassword());
    }

    @Test
    void conPasswordLaReemplaza() {
        DocenteDTO dto = dto();
        dto.setPassword("clave-nueva");
        service.actualizar("D1", dto);
        assertEquals("clave-nueva", existente.getPassword());
    }

    @Test
    void sinDisponibilidadNoLaBorra() {
        service.actualizar("D1", dto());
        assertEquals(1, existente.getDisponibilidad().size());
        verifyNoInteractions(franjaRepository);
    }

    @Test
    void conDisponibilidadVaciaLaVacia() {
        DocenteDTO dto = dto();
        dto.setDisponibilidad(new ArrayList<>());
        service.actualizar("D1", dto);
        assertTrue(existente.getDisponibilidad().isEmpty());
    }

    @Test
    void conDisponibilidadLaReemplazaPorLasFranjasDelRepositorio() {
        FranjaHoraria nueva = new FranjaHoraria(9, DiaSemana.MARTES, EstadoFranja.DISPONIBLE, Categoria.DIA,
                LocalTime.of(9, 0), LocalTime.of(11, 0));
        when(franjaRepository.findById(9)).thenReturn(Optional.of(nueva));
        DocenteDTO dto = dto();
        FranjaHorariaDTO f = new FranjaHorariaDTO();
        f.setIdFranja(9);
        dto.setDisponibilidad(List.of(f));

        service.actualizar("D1", dto);

        assertEquals(1, existente.getDisponibilidad().size());
        assertEquals(9, existente.getDisponibilidad().get(0).getIdFranja());
    }

    @Test
    void docenteInexistenteLanzaNoEncontrado() {
        when(docenteRepository.findById("X")).thenReturn(Optional.empty());
        assertThrows(DocenteNoEncontradoException.class, () -> service.actualizar("X", dto()));
        verify(docenteRepository, never()).save(any());
    }

    @Test
    void correoDeOtraPersonaLanzaRepetido() {
        when(personaRepository.existsByCorreoAndIdNot("ana.nueva@u.edu", "D1")).thenReturn(true);
        assertThrows(DocenteRepetidoException.class, () -> service.actualizar("D1", dto()));
        verify(docenteRepository, never()).save(any());
    }

    @Test
    void correoVacioEsInvalido() {
        DocenteDTO dto = dto();
        dto.setCorreo(" ");
        assertThrows(IllegalArgumentException.class, () -> service.actualizar("D1", dto));
    }

    @Test
    void enumInvalidoEsInvalido() {
        DocenteDTO dto = dto();
        dto.setEscalafon("REY");
        assertThrows(IllegalArgumentException.class, () -> service.actualizar("D1", dto));
    }
}
