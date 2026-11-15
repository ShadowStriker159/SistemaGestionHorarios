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
    private final EstudianteRepository estudianteRepository;
    private final MapeadorDTO mapeador;

    public GrupoService(GrupoRepository grupoRepository, CursoRepository cursoRepository, DocenteRepository docenteRepository,
                        EstudianteRepository estudianteRepository, MapeadorDTO mapeador) {
        this.grupoRepository = grupoRepository;
        this.cursoRepository = cursoRepository;
        this.docenteRepository = docenteRepository;
        this.estudianteRepository = estudianteRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<GrupoDTO> listarPorCurso(Integer idCurso) throws CursoNoEncontradoException {
        Curso curso = cursoRepository.findById(idCurso)
                .orElseThrow(() -> new CursoNoEncontradoException("No existe el curso con id: " + idCurso));
        return grupoRepository.findByCurso(curso).stream().map(mapeador::grupo).toList();
    }

    @Transactional(readOnly = true)
    public List<GrupoDTO> listarPorDocente(String idDocente) throws DocenteNoEncontradoException {
        if (!docenteRepository.existsById(idDocente)) {
            throw new DocenteNoEncontradoException("No existe el docente con id: " + idDocente);
        }
        return grupoRepository.findByDocente_Id(idDocente).stream().map(mapeador::grupo).toList();
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

    @Transactional
    public GrupoDTO inscribirEstudiante(Integer idGrupo, String idEstudiante)
            throws GrupoNoEncontradoException, EstudianteNoEncontradoException, GrupoRepetidoException {
        Grupo grupo = buscarGrupo(idGrupo);
        Estudiante estudiante = buscarEstudiante(idEstudiante);

        if (estudiante.getEstadoEstudiante() == EstadoEstudiante.INACTIVO) {
            throw new IllegalArgumentException("El estudiante está inactivo y no puede inscribirse");
        }
        if (grupo.getEstadoGrupo() == EstadoGrupo.CERRADO) {
            throw new IllegalArgumentException("El grupo está cerrado");
        }

        PlanEstudios plan = estudiante.getPlanEstudios();
        if (plan == null) {
            throw new IllegalArgumentException("El estudiante no tiene un plan de estudios asignado");
        }
        Integer idCurso = grupo.getCurso().getIdCurso();
        if (plan.getCursos() == null || plan.getCursos().stream().noneMatch(c -> c.getIdCurso().equals(idCurso))) {
            throw new IllegalArgumentException("El curso " + grupo.getCurso().getNombreCurso()
                    + " no pertenece al plan de estudios del estudiante");
        }

        if (grupo.getEstudiantes() == null) {
            grupo.setEstudiantes(new ArrayList<>());
        }
        if (grupo.getEstudiantes().stream().anyMatch(e -> e.getId().equals(idEstudiante))) {
            throw new GrupoRepetidoException("El estudiante ya está inscrito en el grupo");
        }
        if (grupo.getEstudiantes().size() >= grupo.getCupoMaximo()) {
            throw new IllegalArgumentException("El grupo no tiene cupos disponibles");
        }

        grupo.getEstudiantes().add(estudiante);
        return mapeador.grupo(grupoRepository.save(grupo));
    }

    @Transactional
    public GrupoDTO retirarEstudiante(Integer idGrupo, String idEstudiante)
            throws GrupoNoEncontradoException, EstudianteNoEncontradoException {
        Grupo grupo = buscarGrupo(idGrupo);
        buscarEstudiante(idEstudiante);

        if (grupo.getEstudiantes() == null || !grupo.getEstudiantes().removeIf(e -> e.getId().equals(idEstudiante))) {
            throw new EstudianteNoEncontradoException("El estudiante no está inscrito en el grupo");
        }
        return mapeador.grupo(grupoRepository.save(grupo));
    }

    private Grupo buscarGrupo(Integer idGrupo) throws GrupoNoEncontradoException {
        return grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new GrupoNoEncontradoException("No existe el grupo con id: " + idGrupo));
    }

    private Estudiante buscarEstudiante(String idEstudiante) throws EstudianteNoEncontradoException {
        return estudianteRepository.findById(idEstudiante)
                .orElseThrow(() -> new EstudianteNoEncontradoException(
                        "No existe el estudiante con id: " + idEstudiante));
    }
}
