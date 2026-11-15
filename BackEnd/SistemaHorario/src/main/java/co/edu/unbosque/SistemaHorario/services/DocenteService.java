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
public class DocenteService {

    private final DocenteRepository docenteRepository;
    private final PersonaRepository personaRepository;
    private final FranjaHorariaRepository franjaHorariaRepository;
    private final MapeadorDTO mapeador;

    public DocenteService(DocenteRepository docenteRepository, PersonaRepository personaRepository, FranjaHorariaRepository franjaHorariaRepository, MapeadorDTO mapeador) {
        this.docenteRepository = docenteRepository;
        this.personaRepository = personaRepository;
        this.franjaHorariaRepository = franjaHorariaRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<DocenteDTO> listarTodos() {
        return docenteRepository.findAll().stream().map(mapeador::docente).toList();
    }

    @Transactional
    public DocenteDTO crear(DocenteDTO dto) throws DocenteRepetidoException, FranjaHorariaNoEncontradaException {
        String id = Validaciones.texto(dto.getId(), "id");
        String correo = Validaciones.texto(dto.getCorreo(), "correo");

        if (personaRepository.existsById(id)) {
            throw new DocenteRepetidoException("Ya existe una persona con número de identificación: " + id);
        }
        if (personaRepository.existsByCorreo(correo)) {
            throw new DocenteRepetidoException("Ya existe una persona con correo: " + correo);
        }

        Docente docente = new Docente();
        docente.setId(id);
        docente.setNombre(Validaciones.texto(dto.getNombre(), "nombre"));
        docente.setApellido(Validaciones.texto(dto.getApellido(), "apellido"));
        docente.setCorreo(correo);
        docente.setPassword(Validaciones.texto(dto.getPassword(), "password"));
        docente.setEscalafon(Validaciones.enumerado(Escalafon.class, dto.getEscalafon(), "escalafon"));
        docente.setEstadoDocente(Validaciones.enumerado(EstadoDocente.class, dto.getEstadoDocente(), "estadoDocente"));
        docente.setTipoVinculacion(
                Validaciones.enumerado(TipoVinculacion.class, dto.getTipoVinculacion(), "tipoVinculacion"));
        docente.setDisponibilidad(resolverDisponibilidad(dto.getDisponibilidad()));

        return mapeador.docente(docenteRepository.save(docente));
    }

    @Transactional
    public DocenteDTO eliminar(String id) throws DocenteNoEncontradoException {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new DocenteNoEncontradoException(
                        "No existe el docente con id: " + id + ", no se puede eliminar"));
        DocenteDTO resultado = mapeador.docente(docente);
        docenteRepository.delete(docente);
        docenteRepository.flush();
        return resultado;
    }

    private List<FranjaHoraria> resolverDisponibilidad(List<FranjaHorariaDTO> solicitadas)
            throws FranjaHorariaNoEncontradaException {
        List<FranjaHoraria> franjas = new ArrayList<>();
        if (solicitadas == null) {
            return franjas;
        }
        for (FranjaHorariaDTO solicitada : solicitadas) {
            FranjaHoraria franja = franjaHorariaRepository.findById(solicitada.getIdFranja())
                    .orElseThrow(() -> new FranjaHorariaNoEncontradaException(
                            "No existe la franja con id: " + solicitada.getIdFranja()));
            if (!franjas.contains(franja)) {
                franjas.add(franja);
            }
        }
        return franjas;
    }
}
