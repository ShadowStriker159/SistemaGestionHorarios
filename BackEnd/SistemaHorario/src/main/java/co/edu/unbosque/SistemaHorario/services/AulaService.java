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
public class AulaService {

    private final AulaRepository aulaRepository;
    private final BloqueRepository bloqueRepository;
    private final MapeadorDTO mapeador;

    public AulaService(AulaRepository aulaRepository, BloqueRepository bloqueRepository, MapeadorDTO mapeador) {
        this.aulaRepository = aulaRepository;
        this.bloqueRepository = bloqueRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<AulaDTO> listarTodos() {
        return aulaRepository.findAll().stream().map(mapeador::aula).toList();
    }

    @Transactional
    public AulaDTO crear(Integer idBloque, AulaDTO dto) throws BloqueNoEncontradoException {
        Bloque bloque = bloqueRepository.findById(idBloque)
                .orElseThrow(() -> new BloqueNoEncontradoException("No existe el bloque con id: " + idBloque));

        Aula aula = new Aula();
        aula.setBloque(bloque);
        aula.setCapacidad(Validaciones.positivo(dto.getCapacidad(), "capacidad"));
        aula.setTipoAula(Validaciones.enumerado(TipoAula.class, dto.getTipoAula(), "tipoAula"));
        aula.setEstadoAula(dto.getEstadoAula() == null
                ? EstadoAula.DISPONIBLE
                : Validaciones.enumerado(EstadoAula.class, dto.getEstadoAula(), "estadoAula"));

        return mapeador.aula(aulaRepository.save(aula));
    }

    @Transactional
    public AulaDTO eliminar(Integer id) throws AulaNoEncontradaException {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new AulaNoEncontradaException(
                        "No existe el aula con id: " + id + ", no se puede eliminar"));
        AulaDTO resultado = mapeador.aula(aula);
        aulaRepository.delete(aula);
        aulaRepository.flush();
        return resultado;
    }
}
