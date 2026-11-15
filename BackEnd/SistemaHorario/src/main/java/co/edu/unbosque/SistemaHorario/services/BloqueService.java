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
public class BloqueService {

    private final BloqueRepository bloqueRepository;
    private final MapeadorDTO mapeador;

    public BloqueService(BloqueRepository bloqueRepository, MapeadorDTO mapeador) {
        this.bloqueRepository = bloqueRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<BloqueDTO> listarTodos() {
        return bloqueRepository.findAll().stream().map(mapeador::bloque).toList();
    }

    @Transactional
    public BloqueDTO crear(BloqueDTO dto) throws BloqueRepetidoException {
        String etiqueta = Validaciones.texto(dto.getEtiquetaBloque(), "etiquetaBloque");
        if (bloqueRepository.existsByEtiquetaBloque(etiqueta)) {
            throw new BloqueRepetidoException("Ya existe un bloque con etiqueta: " + etiqueta);
        }
        Bloque bloque = new Bloque();
        bloque.setEtiquetaBloque(etiqueta);
        bloque.setNombreBloque(Validaciones.texto(dto.getNombreBloque(), "nombreBloque"));
        bloque.setUbicacion(Validaciones.texto(dto.getUbicacion(), "ubicacion"));
        return mapeador.bloque(bloqueRepository.save(bloque));
    }

    @Transactional
    public BloqueDTO eliminar(Integer id) throws BloqueNoEncontradoException {
        Bloque bloque = bloqueRepository.findById(id)
                .orElseThrow(() -> new BloqueNoEncontradoException(
                        "No existe el bloque con id: " + id + ", no se puede eliminar"));
        bloqueRepository.delete(bloque);
        bloqueRepository.flush();
        return mapeador.bloque(bloque);
    }
}
