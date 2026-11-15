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
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final CursoRepository cursoRepository;
    private final DocenteRepository docenteRepository;
    private final MapeadorDTO mapeador;

    public GrupoService(GrupoRepository grupoRepository, CursoRepository cursoRepository, DocenteRepository docenteRepository, MapeadorDTO mapeador) {
        this.grupoRepository = grupoRepository;
        this.cursoRepository = cursoRepository;
        this.docenteRepository = docenteRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<GrupoDTO> listarPorCurso(Integer idCurso) throws CursoNoEncontradoException {
        Curso curso = cursoRepository.findById(idCurso)
                .orElseThrow(() -> new CursoNoEncontradoException("No existe el curso con id: " + idCurso));
        return grupoRepository.findByCurso(curso).stream().map(mapeador::grupo).toList();
    }

    @Transactional
    public GrupoDTO crear(Integer idCurso, String idDocente, GrupoDTO dto)
            throws CursoNoEncontradoException, DocenteNoEncontradoException {
        Curso curso = cursoRepository.findById(idCurso)
                .orElseThrow(() -> new CursoNoEncontradoException("No existe el curso con id: " + idCurso));
        Docente docente = docenteRepository.findById(idDocente)
                .orElseThrow(() -> new DocenteNoEncontradoException("No existe el docente con id: " + idDocente));

        int cupoMaximo = Validaciones.positivo(dto.getCupoMaximo(), "cupoMaximo");
        int cupoMinimo = Validaciones.positivo(dto.getCupoMinimo(), "cupoMinimo");
        if (cupoMaximo < cupoMinimo) {
            throw new IllegalArgumentException("El cupo máximo no puede ser menor al cupo mínimo");
        }

        Grupo grupo = new Grupo();
        grupo.setCurso(curso);
        grupo.setDocente(docente);
        grupo.setCupoMaximo(cupoMaximo);
        grupo.setCupoMinimo(cupoMinimo);
        grupo.setEstadoGrupo(dto.getEstadoGrupo() == null
                ? EstadoGrupo.ABIERTO
                : Validaciones.enumerado(EstadoGrupo.class, dto.getEstadoGrupo(), "estadoGrupo"));

        return mapeador.grupo(grupoRepository.save(grupo));
    }

    @Transactional
    public GrupoDTO eliminar(Integer id) throws GrupoNoEncontradoException {
        Grupo grupo = grupoRepository.findById(id)
                .orElseThrow(() -> new GrupoNoEncontradoException(
                        "No existe el grupo con id: " + id + ", no se puede eliminar"));
        GrupoDTO resultado = mapeador.grupo(grupo);
        grupoRepository.delete(grupo);
        grupoRepository.flush();
        return resultado;
    }
}
