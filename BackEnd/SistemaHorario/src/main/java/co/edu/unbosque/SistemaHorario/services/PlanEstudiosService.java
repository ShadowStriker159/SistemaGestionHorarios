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
public class PlanEstudiosService {

    private final PlanEstudiosRepository planEstudiosRepository;
    private final CarreraRepository carreraRepository;
    private final MapeadorDTO mapeador;

    public PlanEstudiosService(PlanEstudiosRepository planEstudiosRepository, CarreraRepository carreraRepository, MapeadorDTO mapeador) {
        this.planEstudiosRepository = planEstudiosRepository;
        this.carreraRepository = carreraRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<PlanEstudiosDTO> listarPorCarrera(Integer idCarrera) throws CarreraNoEncontradaException {
        Carrera carrera = buscarCarrera(idCarrera);
        return planEstudiosRepository.findByCarrera(carrera).stream().map(mapeador::plan).toList();
    }

    @Transactional
    public PlanEstudiosDTO crear(Integer idCarrera, PlanEstudiosDTO dto)
            throws CarreraNoEncontradaException, PlanEstudiosRepetidoException {
        Carrera carrera = buscarCarrera(idCarrera);
        String nombre = Validaciones.texto(dto.getNombrePlanEstudios(), "nombrePlanEstudios");
        if (planEstudiosRepository.existsByNombrePlanEstudiosAndCarrera(nombre, carrera)) {
            throw new PlanEstudiosRepetidoException("Ya existe un plan con el nombre: " + nombre + " en esta carrera");
        }
        PlanEstudios plan = new PlanEstudios();
        plan.setNombrePlanEstudios(nombre);
        plan.setCarrera(carrera);
        plan.setCursos(new ArrayList<>());
        return mapeador.plan(planEstudiosRepository.save(plan));
    }

    @Transactional
    public PlanEstudiosDTO eliminar(Integer id) throws PlanEstudiosNoEncontradoException {
        PlanEstudios plan = planEstudiosRepository.findById(id)
                .orElseThrow(() -> new PlanEstudiosNoEncontradoException(
                        "No existe el plan con id: " + id + ", no se puede eliminar"));
        planEstudiosRepository.delete(plan);
        return mapeador.plan(plan);
    }

    private Carrera buscarCarrera(Integer idCarrera) throws CarreraNoEncontradaException {
        return carreraRepository.findById(idCarrera)
                .orElseThrow(() -> new CarreraNoEncontradaException("No existe la carrera con id: " + idCarrera));
    }
}
