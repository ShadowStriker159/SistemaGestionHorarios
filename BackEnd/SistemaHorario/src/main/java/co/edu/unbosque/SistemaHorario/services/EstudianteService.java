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
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final PersonaRepository personaRepository;
    private final PlanEstudiosRepository planEstudiosRepository;
    private final CarreraRepository carreraRepository;
    private final MapeadorDTO mapeador;

    public EstudianteService(EstudianteRepository estudianteRepository, PersonaRepository personaRepository,
                             PlanEstudiosRepository planEstudiosRepository,
                             CarreraRepository carreraRepository, MapeadorDTO mapeador) {
        this.estudianteRepository = estudianteRepository;
        this.personaRepository = personaRepository;
        this.planEstudiosRepository = planEstudiosRepository;
        this.carreraRepository = carreraRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<EstudianteDTO> listarTodos() {
        return estudianteRepository.findAll().stream().map(mapeador::estudiante).toList();
    }

    @Transactional(readOnly = true)
    public List<EstudianteDTO> listarPorCarrera(Integer idCarrera) throws CarreraNoEncontradaException {
        Carrera carrera = carreraRepository.findById(idCarrera)
                .orElseThrow(() -> new CarreraNoEncontradaException("No existe la carrera con id: " + idCarrera));
        return estudianteRepository.findByPlanEstudiosCarrera(carrera).stream().map(mapeador::estudiante).toList();
    }

    @Transactional
    public EstudianteDTO crear(EstudianteDTO dto) throws EstudianteRepetidoException, PlanEstudiosNoEncontradoException {
        String id = Validaciones.texto(dto.getId(), "id");
        String correo = Validaciones.texto(dto.getCorreo(), "correo");

        if (personaRepository.existsById(id)) {
            throw new EstudianteRepetidoException("Ya existe una persona con número de identificación: " + id);
        }
        if (personaRepository.existsByCorreo(correo)) {
            throw new EstudianteRepetidoException("Ya existe una persona con correo: " + correo);
        }

        PlanEstudios plan = resolverPlan(dto);

        Estudiante estudiante = new Estudiante();
        estudiante.setPlanEstudios(plan);
        estudiante.setId(id);
        estudiante.setNombre(Validaciones.texto(dto.getNombre(), "nombre"));
        estudiante.setApellido(Validaciones.texto(dto.getApellido(), "apellido"));
        estudiante.setCorreo(correo);
        estudiante.setPassword(Validaciones.texto(dto.getPassword(), "password"));
        estudiante.setSemestreCursa(Validaciones.positivo(dto.getSemestreCursa(), "semestreCursa"));
        estudiante.setCreditosAprobados(Validaciones.noNegativo(dto.getCreditosAprobados(), "creditosAprobados"));
        estudiante.setEstadoEstudiante(
                Validaciones.enumerado(EstadoEstudiante.class, dto.getEstadoEstudiante(), "estadoEstudiante"));

        return mapeador.estudiante(estudianteRepository.save(estudiante));
    }

    @Transactional(readOnly = true)
    public EstudianteDTO obtener(String id) throws EstudianteNoEncontradoException {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new EstudianteNoEncontradoException("No existe el estudiante con id: " + id));
        return mapeador.estudiante(estudiante);
    }

    @Transactional
    public EstudianteDTO actualizar(String id, EstudianteDTO dto) throws EstudianteNoEncontradoException,
            EstudianteRepetidoException, PlanEstudiosNoEncontradoException {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new EstudianteNoEncontradoException("No existe el estudiante con id: " + id));
        String correo = Validaciones.texto(dto.getCorreo(), "correo");
        if (personaRepository.existsByCorreoAndIdNot(correo, id)) {
            throw new EstudianteRepetidoException("Ya existe una persona con correo: " + correo);
        }
        estudiante.setNombre(Validaciones.texto(dto.getNombre(), "nombre"));
        estudiante.setApellido(Validaciones.texto(dto.getApellido(), "apellido"));
        estudiante.setCorreo(correo);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            estudiante.setPassword(dto.getPassword().trim());
        }
        estudiante.setSemestreCursa(Validaciones.positivo(dto.getSemestreCursa(), "semestreCursa"));
        estudiante.setCreditosAprobados(Validaciones.noNegativo(dto.getCreditosAprobados(), "creditosAprobados"));
        estudiante.setEstadoEstudiante(
                Validaciones.enumerado(EstadoEstudiante.class, dto.getEstadoEstudiante(), "estadoEstudiante"));
        if (dto.getIdPlanEstudios() != null) {
            estudiante.setPlanEstudios(resolverPlan(dto));
        }
        return mapeador.estudiante(estudianteRepository.save(estudiante));
    }

    private PlanEstudios resolverPlan(EstudianteDTO dto) throws PlanEstudiosNoEncontradoException {
        Integer idPlan = Validaciones.positivo(dto.getIdPlanEstudios(), "idPlanEstudios");
        PlanEstudios plan = planEstudiosRepository.findById(idPlan)
                .orElseThrow(() -> new PlanEstudiosNoEncontradoException("No existe el plan con id: " + idPlan));
        Integer idCarrera = Validaciones.positivo(dto.getIdCarrera(), "idCarrera");
        if (plan.getCarrera() == null || !idCarrera.equals(plan.getCarrera().getIdCarrera())) {
            throw new IllegalArgumentException("El plan de estudios seleccionado no corresponde a la carrera.");
        }
        return plan;
    }

    @Transactional
    public EstudianteDTO asignarPlan(String id, Integer idPlan)
            throws EstudianteNoEncontradoException, PlanEstudiosNoEncontradoException {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new EstudianteNoEncontradoException("No existe el estudiante con id: " + id));
        PlanEstudios plan = planEstudiosRepository.findById(idPlan)
                .orElseThrow(() -> new PlanEstudiosNoEncontradoException("No existe el plan con id: " + idPlan));
        estudiante.setPlanEstudios(plan);
        return mapeador.estudiante(estudianteRepository.save(estudiante));
    }

    @Transactional
    public EstudianteDTO eliminar(String id) throws EstudianteNoEncontradoException {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new EstudianteNoEncontradoException(
                        "No existe el estudiante con id: " + id + ", no se puede eliminar"));
        estudianteRepository.delete(estudiante);
        return mapeador.estudiante(estudiante);
    }
}
