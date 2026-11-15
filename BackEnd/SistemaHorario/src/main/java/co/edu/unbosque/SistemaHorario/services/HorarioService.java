package co.edu.unbosque.SistemaHorario.services;

import co.edu.unbosque.SistemaHorario.model.*;
import co.edu.unbosque.SistemaHorario.model.algoritmo.IAlgoritmoHorario;
import co.edu.unbosque.SistemaHorario.model.dto.HorarioDTO;
import co.edu.unbosque.SistemaHorario.model.exception.FacultadNoEncontradaException;
import co.edu.unbosque.SistemaHorario.model.exception.HorarioInalcanzableException;
import co.edu.unbosque.SistemaHorario.respository.FacultadRepository;
import co.edu.unbosque.SistemaHorario.respository.SeccionRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class HorarioService {

    private final FacultadRepository facultadRepository;
    private final SeccionRepository seccionRepository;
    private final IAlgoritmoHorario algoritmoHorario;

    public HorarioService(FacultadRepository facultadRepository,
                          SeccionRepository seccionRepository,
                          @Qualifier("algoritmoGeneticoHorario") IAlgoritmoHorario algoritmoHorario) {
        this.facultadRepository = facultadRepository;
        this.seccionRepository = seccionRepository;
        this.algoritmoHorario = algoritmoHorario;
    }

    @Transactional(readOnly = true)
    public List<HorarioDTO> listarHorarios() {
        List<HorarioDTO> horariosDTO = new ArrayList<>();
        for (Seccion seccion : seccionRepository.findAll()) {
            horariosDTO.add(mapearHorarioDTO(seccion));
        }
        return horariosDTO;
    }

    @Transactional
    public List<HorarioDTO> generarHorario(Integer idFacultad)
            throws FacultadNoEncontradaException, HorarioInalcanzableException {

        Facultad facultad = facultadRepository.findById(idFacultad)
                .orElseThrow(() -> new FacultadNoEncontradaException("Facultad no encontrada."));

        List<Seccion> seccionesGeneradas = algoritmoHorario.generarHorario(facultad);

        if (seccionesGeneradas == null || seccionesGeneradas.isEmpty()) {
            throw new HorarioInalcanzableException("No se generaron secciones.");
        }

        seccionRepository.deleteAll();
        seccionRepository.flush();

        for (Seccion s : seccionesGeneradas) {
            s.setIdSeccion(null);
        }

        List<HorarioDTO> horariosDTO = new ArrayList<>();
        for (Seccion seccion : seccionRepository.saveAll(seccionesGeneradas)) {
            horariosDTO.add(mapearHorarioDTO(seccion));
        }
        return horariosDTO;
    }

    private HorarioDTO mapearHorarioDTO(Seccion seccion) {
        HorarioDTO dto = new HorarioDTO();
        dto.setNumero(seccion.getIdSeccion());

        if (seccion.getEstadoSeccion() != null) {
            dto.setEstado(seccion.getEstadoSeccion().toString());
        }

        Grupo grupo = seccion.getGrupo();
        if (grupo != null) {
            dto.setGrupo(String.valueOf(grupo.getIdGrupo()));

            if (grupo.getDocente() != null) {
                dto.setNombreDocente(grupo.getDocente().getNombre() + " " + grupo.getDocente().getApellido());
            }

            Curso curso = grupo.getCurso();
            if (curso != null) {
                dto.setMateria(curso.getNombreCurso());

                if (curso.getPlanesEstudio() != null && !curso.getPlanesEstudio().isEmpty()) {
                    PlanEstudios plan = curso.getPlanesEstudio().get(0);
                    if (plan.getCarrera() != null) {
                        dto.setPrograma(plan.getCarrera().getNombreCarrera());
                    }
                }
            }
        }

        Aula aula = seccion.getAula();
        if (aula != null) {
            dto.setEspacioFisico("Aula " + aula.getNumeroAula());

            Bloque bloque = aula.getBloque();
            if (bloque != null) {
                dto.setBloque(bloque.getEtiquetaBloque());
                dto.setSedeCampus(bloque.getUbicacion());
            }
        }

        FranjaHoraria franja = seccion.getFranjaHoraria();
        if (franja != null) {
            dto.setDia(franja.getDiaSemana().toString());
            dto.setHoraInicial(franja.getHoraInicio());
            dto.setHoraFinal(franja.getHoraFin());
        }

        return dto;
    }
}
