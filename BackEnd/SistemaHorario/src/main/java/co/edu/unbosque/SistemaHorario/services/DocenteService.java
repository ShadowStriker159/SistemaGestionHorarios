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
    private final GrupoRepository grupoRepository;
    private final MapeadorDTO mapeador;

    public DocenteService(DocenteRepository docenteRepository, PersonaRepository personaRepository,
                          FranjaHorariaRepository franjaHorariaRepository, GrupoRepository grupoRepository,
                          MapeadorDTO mapeador) {
        this.docenteRepository = docenteRepository;
        this.personaRepository = personaRepository;
        this.franjaHorariaRepository = franjaHorariaRepository;
        this.grupoRepository = grupoRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<DocenteDTO> listarTodos() {
        return docenteRepository.findAll().stream().map(mapeador::docente).toList();
    }

    @Transactional(readOnly = true)
    public DocenteDTO obtener(String id) throws DocenteNoEncontradoException {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new DocenteNoEncontradoException("No existe el docente con id: " + id));
        return mapeador.docente(docente);
    }

    @Transactional
    public DocenteDTO actualizarDisponibilidad(String id, List<FranjaHorariaDTO> disponibilidad)
            throws DocenteNoEncontradoException, FranjaHorariaNoEncontradaException {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new DocenteNoEncontradoException("No existe el docente con id: " + id));
        docente.setDisponibilidad(resolverDisponibilidad(disponibilidad));
        return mapeador.docente(docenteRepository.save(docente));
    }

    @Transactional
    public DocenteDTO actualizar(String id, DocenteDTO dto)
            throws DocenteNoEncontradoException, DocenteRepetidoException, FranjaHorariaNoEncontradaException {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new DocenteNoEncontradoException("No existe el docente con id: " + id));
        String correo = Validaciones.texto(dto.getCorreo(), "correo");
        if (personaRepository.existsByCorreoAndIdNot(correo, id)) {
            throw new DocenteRepetidoException("Ya existe una persona con correo: " + correo);
        }
        docente.setNombre(Validaciones.texto(dto.getNombre(), "nombre"));
        docente.setApellido(Validaciones.texto(dto.getApellido(), "apellido"));
        docente.setCorreo(correo);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            docente.setPassword(dto.getPassword().trim());
        }
        docente.setEscalafon(Validaciones.enumerado(Escalafon.class, dto.getEscalafon(), "escalafon"));
        docente.setEstadoDocente(Validaciones.enumerado(EstadoDocente.class, dto.getEstadoDocente(), "estadoDocente"));
        docente.setTipoVinculacion(
                Validaciones.enumerado(TipoVinculacion.class, dto.getTipoVinculacion(), "tipoVinculacion"));
        if (dto.getDisponibilidad() != null) {
            docente.setDisponibilidad(resolverDisponibilidad(dto.getDisponibilidad()));
        }
        return mapeador.docente(docenteRepository.save(docente));
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
    public DocenteDTO eliminar(String id) throws DocenteNoEncontradoException, DocenteConGruposException {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new DocenteNoEncontradoException(
                        "No existe el docente con id: " + id + ", no se puede eliminar"));
        if (grupoRepository.existsByDocente_Id(id)) {
            throw new DocenteConGruposException(
                    "No se puede eliminar el docente porque tiene grupos asignados. "
                            + "Reasigna o elimina esos grupos primero.");
        }
        DocenteDTO resultado = mapeador.docente(docente);
        docenteRepository.delete(docente);
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
