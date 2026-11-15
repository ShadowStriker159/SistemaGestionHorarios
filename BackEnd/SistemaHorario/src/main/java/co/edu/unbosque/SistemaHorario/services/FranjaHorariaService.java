package co.edu.unbosque.SistemaHorario.services;

import co.edu.unbosque.SistemaHorario.model.*;
import co.edu.unbosque.SistemaHorario.model.dto.*;
import co.edu.unbosque.SistemaHorario.model.exception.*;
import co.edu.unbosque.SistemaHorario.respository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class FranjaHorariaService {

    private final FranjaHorariaRepository franjaHorariaRepository;
    private final MapeadorDTO mapeador;

    public FranjaHorariaService(FranjaHorariaRepository franjaHorariaRepository, MapeadorDTO mapeador) {
        this.franjaHorariaRepository = franjaHorariaRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<FranjaHorariaDTO> listarTodos() {
        return franjaHorariaRepository.findAll().stream().map(mapeador::franja).toList();
    }

    @Transactional
    public FranjaHorariaDTO crear(FranjaHorariaDTO dto) throws FranjaHorariaRepetidaException {
        DiaSemana dia = Validaciones.enumerado(DiaSemana.class, dto.getDiaSemana(), "diaSemana");
        Categoria categoria = Validaciones.enumerado(Categoria.class, dto.getCategoria(), "categoria");
        if (dto.getHoraInicio() == null || dto.getHoraFin() == null) {
            throw new IllegalArgumentException("Las horas de inicio y fin son obligatorias");
        }
        if (!dto.getHoraFin().isAfter(dto.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }
        if (franjaHorariaRepository.existsByDiaSemanaAndHoraInicioAndHoraFin(
                dia, dto.getHoraInicio(), dto.getHoraFin())) {
            throw new FranjaHorariaRepetidaException("Ya existe una franja para " + dia
                    + " de " + dto.getHoraInicio() + " a " + dto.getHoraFin());
        }

        FranjaHoraria franja = new FranjaHoraria();
        franja.setDiaSemana(dia);
        franja.setCategoria(categoria);
        franja.setHoraInicio(dto.getHoraInicio());
        franja.setHoraFin(dto.getHoraFin());
        franja.setEstadoFranja(dto.getEstadoFranja() == null
                ? EstadoFranja.DISPONIBLE
                : Validaciones.enumerado(EstadoFranja.class, dto.getEstadoFranja(), "estadoFranja"));

        return mapeador.franja(franjaHorariaRepository.save(franja));
    }

    @Transactional
    public FranjaHorariaDTO eliminar(Integer id) throws FranjaHorariaNoEncontradaException {
        FranjaHoraria franja = franjaHorariaRepository.findById(id)
                .orElseThrow(() -> new FranjaHorariaNoEncontradaException(
                        "No existe la franja con id: " + id + ", no se puede eliminar"));
        FranjaHorariaDTO resultado = mapeador.franja(franja);
        franjaHorariaRepository.delete(franja);
        franjaHorariaRepository.flush();
        return resultado;
    }
}
