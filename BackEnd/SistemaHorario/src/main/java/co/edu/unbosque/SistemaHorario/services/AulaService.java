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
    public AulaDTO crear(Integer idBloque, AulaDTO dto) throws BloqueNoEncontradoException, AulaRepetidaException {
        Bloque bloque = bloqueRepository.findById(idBloque)
                .orElseThrow(() -> new BloqueNoEncontradoException("No existe el bloque con id: " + idBloque));

        String numero = Validaciones.texto(dto.getNumeroAula(), "numeroAula");
        if (!numero.matches("[\\p{L}\\p{N}-]{1,10}")) {
            throw new IllegalArgumentException(
                    "El nombre del aula debe tener hasta 10 letras, números o guiones; por ejemplo, A-101.");
        }
        AulaId id = new AulaId(idBloque, numero);
        if (aulaRepository.existsById(id)) {
            throw new AulaRepetidaException("Ya existe el aula " + numero + " en el bloque " + bloque.getEtiquetaBloque());
        }

        Aula aula = new Aula();
        aula.setId(id);
        aula.setBloque(bloque);
        aula.setCapacidad(Validaciones.positivo(dto.getCapacidad(), "capacidad"));
        aula.setTipoAula(Validaciones.enumerado(TipoAula.class, dto.getTipoAula(), "tipoAula"));
        aula.setEstadoAula(dto.getEstadoAula() == null
                ? EstadoAula.DISPONIBLE
                : Validaciones.enumerado(EstadoAula.class, dto.getEstadoAula(), "estadoAula"));

        return mapeador.aula(aulaRepository.save(aula));
    }

    @Transactional
    public AulaDTO eliminar(Integer idBloque, String numeroAula) throws AulaNoEncontradaException {
        Aula aula = aulaRepository.findById(new AulaId(idBloque, numeroAula))
                .orElseThrow(() -> new AulaNoEncontradaException(
                        "No existe el aula " + numeroAula + " en el bloque " + idBloque + ", no se puede eliminar"));
        AulaDTO resultado = mapeador.aula(aula);
        aulaRepository.delete(aula);
        return resultado;
    }
}
