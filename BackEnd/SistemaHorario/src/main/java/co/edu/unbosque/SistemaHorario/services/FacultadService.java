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
public class FacultadService {

    private final FacultadRepository facultadRepository;
    private final MapeadorDTO mapeador;

    public FacultadService(FacultadRepository facultadRepository, MapeadorDTO mapeador) {
        this.facultadRepository = facultadRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<FacultadDTO> listarTodos() {
        return facultadRepository.findAll().stream().map(mapeador::facultad).toList();
    }

    @Transactional
    public FacultadDTO crear(FacultadDTO dto) throws FacultadRepetidaException {
        String nombre = Validaciones.texto(dto.getNombreFacultad(), "nombreFacultad");
        if (facultadRepository.existsByNombreFacultad(nombre)) {
            throw new FacultadRepetidaException("Ya existe una facultad con el nombre: " + nombre);
        }
        Facultad facultad = new Facultad();
        facultad.setNombreFacultad(nombre);
        return mapeador.facultad(facultadRepository.save(facultad));
    }

    @Transactional
    public FacultadDTO eliminar(Integer id) throws FacultadNoEncontradaException {
        Facultad facultad = facultadRepository.findById(id)
                .orElseThrow(() -> new FacultadNoEncontradaException(
                        "No existe la facultad con id: " + id + ", no se puede eliminar"));
        facultadRepository.delete(facultad);
        return mapeador.facultad(facultad);
    }
}
