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
public class CursoService {

    private final CursoRepository cursoRepository;
    private final PlanEstudiosRepository planEstudiosRepository;
    private final MapeadorDTO mapeador;

    public CursoService(CursoRepository cursoRepository, PlanEstudiosRepository planEstudiosRepository, MapeadorDTO mapeador) {
        this.cursoRepository = cursoRepository;
        this.planEstudiosRepository = planEstudiosRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<CursoDTO> listarPorPlan(Integer idPlan) throws PlanEstudiosNoEncontradoException {
        PlanEstudios plan = buscarPlan(idPlan);
        return cursoRepository.findByPlanEstudios(plan).stream().map(mapeador::curso).toList();
    }

    @Transactional
    public CursoDTO crear(Integer idPlan, CursoDTO dto)
            throws PlanEstudiosNoEncontradoException, CursoNoEncontradoException, CursoRepetidoException {
        PlanEstudios plan = buscarPlan(idPlan);
        String nombre = Validaciones.texto(dto.getNombreCurso(), "nombreCurso");
        int creditos = Validaciones.positivo(dto.getCreditos(), "creditos");
        int intensidad = Validaciones.positivo(dto.getIntensidadHoraria(), "intensidadHoraria");
        int semestre = Validaciones.positivo(dto.getSemestre(), "semestre");
        TipoAula aula = Validaciones.enumerado(TipoAula.class, dto.getAulaRequerida(), "aulaRequerida");

        if (cursoRepository.existsByNombreInPlan(nombre, plan)) {
            throw new CursoRepetidoException("Ya existe un curso con el nombre: " + nombre + " en este plan");
        }

        Curso curso = new Curso();
        curso.setNombreCurso(nombre);
        curso.setCreditos(creditos);
        curso.setIntensidadHoraria(intensidad);
        curso.setSemestre(semestre);
        curso.setAulaRequerida(aula);
        curso.setPrerrequisitos(resolverPrerrequisitos(dto.getPrerrequisitos()));

        Curso guardado = cursoRepository.save(curso);
        if (plan.getCursos() == null) {
            plan.setCursos(new ArrayList<>());
        }
        plan.getCursos().add(guardado);
        planEstudiosRepository.save(plan);

        return mapeador.curso(guardado);
    }

    @Transactional
    public CursoDTO eliminar(Integer id) throws CursoNoEncontradoException {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new CursoNoEncontradoException(
                        "No existe el curso con id: " + id + ", no se puede eliminar"));
        if (curso.getPlanesEstudio() != null) {
            for (PlanEstudios plan : curso.getPlanesEstudio()) {
                plan.getCursos().remove(curso);
            }
        }
        cursoRepository.delete(curso);
        cursoRepository.flush();
        return mapeador.curso(curso);
    }

    private PlanEstudios buscarPlan(Integer idPlan) throws PlanEstudiosNoEncontradoException {
        return planEstudiosRepository.findById(idPlan)
                .orElseThrow(() -> new PlanEstudiosNoEncontradoException("No existe el plan con id: " + idPlan));
    }

    private List<Curso> resolverPrerrequisitos(List<CursoDTO> solicitados) throws CursoNoEncontradoException {
        List<Curso> prerrequisitos = new ArrayList<>();
        if (solicitados == null) {
            return prerrequisitos;
        }
        for (CursoDTO solicitado : solicitados) {
            Curso prerrequisito = cursoRepository.findById(solicitado.getIdCurso())
                    .orElseThrow(() -> new CursoNoEncontradoException(
                            "No existe el curso prerrequisito con id: " + solicitado.getIdCurso()));
            if (!prerrequisitos.contains(prerrequisito)) {
                prerrequisitos.add(prerrequisito);
            }
        }
        return prerrequisitos;
    }
}
