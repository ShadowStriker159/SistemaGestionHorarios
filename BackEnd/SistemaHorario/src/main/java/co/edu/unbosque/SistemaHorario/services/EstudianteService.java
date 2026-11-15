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
    private final MapeadorDTO mapeador;

    public EstudianteService(EstudianteRepository estudianteRepository, PersonaRepository personaRepository, MapeadorDTO mapeador) {
        this.estudianteRepository = estudianteRepository;
        this.personaRepository = personaRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<EstudianteDTO> listarTodos() {
        return estudianteRepository.findAll().stream().map(mapeador::estudiante).toList();
    }

    @Transactional
    public EstudianteDTO crear(EstudianteDTO dto) throws EstudianteRepetidoException {
        String id = Validaciones.texto(dto.getId(), "id");
        String correo = Validaciones.texto(dto.getCorreo(), "correo");

        if (personaRepository.existsById(id)) {
            throw new EstudianteRepetidoException("Ya existe una persona con número de identificación: " + id);
        }
        if (personaRepository.existsByCorreo(correo)) {
            throw new EstudianteRepetidoException("Ya existe una persona con correo: " + correo);
        }

        Estudiante estudiante = new Estudiante();
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

    @Transactional
    public EstudianteDTO eliminar(String id) throws EstudianteNoEncontradoException {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new EstudianteNoEncontradoException(
                        "No existe el estudiante con id: " + id + ", no se puede eliminar"));
        estudianteRepository.delete(estudiante);
        estudianteRepository.flush();
        return mapeador.estudiante(estudiante);
    }
}
