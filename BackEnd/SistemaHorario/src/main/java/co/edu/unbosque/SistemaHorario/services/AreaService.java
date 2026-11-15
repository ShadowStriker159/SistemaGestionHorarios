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
public class AreaService {

    private final AreaRepository areaRepository;
    private final FacultadRepository facultadRepository;
    private final MapeadorDTO mapeador;

    public AreaService(AreaRepository areaRepository, FacultadRepository facultadRepository, MapeadorDTO mapeador) {
        this.areaRepository = areaRepository;
        this.facultadRepository = facultadRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<AreaDTO> listarPorFacultad(Integer idFacultad) throws FacultadNoEncontradaException {
        Facultad facultad = buscarFacultad(idFacultad);
        return areaRepository.findByFacultad(facultad).stream().map(mapeador::area).toList();
    }

    @Transactional
    public AreaDTO crear(Integer idFacultad, AreaDTO dto) throws FacultadNoEncontradaException, AreaRepetidaException {
        Facultad facultad = buscarFacultad(idFacultad);
        String nombre = Validaciones.texto(dto.getNombreArea(), "nombreArea");
        if (areaRepository.existsByNombreAreaAndFacultad(nombre, facultad)) {
            throw new AreaRepetidaException("Ya existe un área con el nombre: " + nombre + " en esta facultad");
        }
        Area area = new Area();
        area.setNombreArea(nombre);
        area.setDescripcion(dto.getDescripcion());
        area.setFacultad(facultad);
        return mapeador.area(areaRepository.save(area));
    }

    @Transactional
    public AreaDTO eliminar(Integer id) throws AreaNoEncontradaException {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new AreaNoEncontradaException(
                        "No existe el área con id: " + id + ", no se puede eliminar"));
        areaRepository.delete(area);
        return mapeador.area(area);
    }

    private Facultad buscarFacultad(Integer idFacultad) throws FacultadNoEncontradaException {
        return facultadRepository.findById(idFacultad)
                .orElseThrow(() -> new FacultadNoEncontradaException("No existe la facultad con id: " + idFacultad));
    }
}
