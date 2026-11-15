package co.edu.unbosque.SistemaHorario.services;

import co.edu.unbosque.SistemaHorario.model.*;
import co.edu.unbosque.SistemaHorario.model.dto.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MapeadorDTO {

    private String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    public FranjaHorariaDTO franja(FranjaHoraria franja) {
        return new FranjaHorariaDTO(franja.getIdFranja(), texto(franja.getDiaSemana()),
                texto(franja.getEstadoFranja()), texto(franja.getCategoria()),
                franja.getHoraInicio(), franja.getHoraFin());
    }

    public DocenteDTO docente(Docente docente) {
        List<FranjaHorariaDTO> disponibilidad = new ArrayList<>();
        if (docente.getDisponibilidad() != null) {
            for (FranjaHoraria franja : docente.getDisponibilidad()) {
                disponibilidad.add(franja(franja));
            }
        }
        return new DocenteDTO(docente.getId(), docente.getNombre(), docente.getApellido(),
                docente.getCorreo(), null, texto(docente.getEscalafon()),
                texto(docente.getEstadoDocente()), texto(docente.getTipoVinculacion()), disponibilidad);
    }

    public EstudianteDTO estudiante(Estudiante estudiante) {
        EstudianteDTO dto = new EstudianteDTO(estudiante.getId(), estudiante.getNombre(), estudiante.getApellido(),
                estudiante.getCorreo(), null, estudiante.getSemestreCursa(),
                texto(estudiante.getEstadoEstudiante()), estudiante.getCreditosAprobados());
        PlanEstudios plan = estudiante.getPlanEstudios();
        if (plan != null) {
            dto.setIdPlanEstudios(plan.getIdPlanEstudios());
            if (plan.getCarrera() != null) {
                dto.setIdCarrera(plan.getCarrera().getIdCarrera());
                dto.setNombreCarrera(plan.getCarrera().getNombreCarrera());
            }
        }
        return dto;
    }

    public FacultadDTO facultad(Facultad facultad) {
        FacultadDTO dto = new FacultadDTO();
        dto.setIdFacultad(facultad.getIdFacultad());
        dto.setNombreFacultad(facultad.getNombreFacultad());
        return dto;
    }

    public AreaDTO area(Area area) {
        AreaDTO dto = new AreaDTO();
        dto.setIdArea(area.getIdArea());
        dto.setNombreArea(area.getNombreArea());
        dto.setDescripcion(area.getDescripcion());
        return dto;
    }

    public CarreraDTO carrera(Carrera carrera) {
        CarreraDTO dto = new CarreraDTO();
        dto.setIdCarrera(carrera.getIdCarrera());
        dto.setNombreCarrera(carrera.getNombreCarrera());
        return dto;
    }

    public PlanEstudiosDTO plan(PlanEstudios plan) {
        PlanEstudiosDTO dto = new PlanEstudiosDTO();
        dto.setIdPlanEstudios(plan.getIdPlanEstudios());
        dto.setNombrePlanEstudios(plan.getNombrePlanEstudios());
        return dto;
    }

    public CursoDTO curso(Curso curso) {
        CursoDTO dto = new CursoDTO();
        dto.setIdCurso(curso.getIdCurso());
        dto.setNombreCurso(curso.getNombreCurso());
        dto.setIntensidadHoraria(curso.getIntensidadHoraria());
        dto.setSemestre(curso.getSemestre());
        dto.setCreditos(curso.getCreditos());
        dto.setAulaRequerida(texto(curso.getAulaRequerida()));
        return dto;
    }

    public GrupoDTO grupo(Grupo grupo) {
        GrupoDTO dto = new GrupoDTO();
        dto.setIdGrupo(grupo.getIdGrupo());
        dto.setEstadoGrupo(texto(grupo.getEstadoGrupo()));
        dto.setCupoMaximo(grupo.getCupoMaximo());
        dto.setCupoMinimo(grupo.getCupoMinimo());
        if (grupo.getCurso() != null) {
            dto.setCurso(curso(grupo.getCurso()));
        }
        if (grupo.getDocente() != null) {
            dto.setDocente(docente(grupo.getDocente()));
        }
        dto.setEstudiantes(grupo.getEstudiantes() == null
                ? List.of()
                : grupo.getEstudiantes().stream().map(this::estudiante).toList());
        dto.setSecciones(grupo.getSecciones() == null
                ? List.of()
                : grupo.getSecciones().stream().map(this::seccion).toList());
        return dto;
    }

    public SeccionDTO seccion(Seccion seccion) {
        SeccionDTO dto = new SeccionDTO();
        dto.setIdSeccion(seccion.getIdSeccion());
        dto.setEstadoSeccion(texto(seccion.getEstadoSeccion()));
        if (seccion.getAula() != null) {
            dto.setAula(aula(seccion.getAula()));
        }
        if (seccion.getFranjaHoraria() != null) {
            dto.setFranjaHoraria(franja(seccion.getFranjaHoraria()));
        }
        return dto;
    }

    public BloqueDTO bloque(Bloque bloque) {
        BloqueDTO dto = new BloqueDTO();
        dto.setIdBloque(bloque.getIdBloque());
        dto.setNombreBloque(bloque.getNombreBloque());
        dto.setEtiquetaBloque(bloque.getEtiquetaBloque());
        dto.setUbicacion(bloque.getUbicacion());
        return dto;
    }

    public AulaDTO aula(Aula aula) {
        AulaDTO dto = new AulaDTO();
        dto.setNumeroAula(aula.getNumeroAula());
        dto.setCapacidad(aula.getCapacidad());
        dto.setTipoAula(texto(aula.getTipoAula()));
        dto.setEstadoAula(texto(aula.getEstadoAula()));
        if (aula.getBloque() != null) {
            dto.setBloque(bloque(aula.getBloque()));
        }
        return dto;
    }
}
