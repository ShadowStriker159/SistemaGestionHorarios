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
public class CarreraService {

    private final CarreraRepository carreraRepository;
    private final FacultadRepository facultadRepository;
    private final MapeadorDTO mapeador;

    public CarreraService(CarreraRepository carreraRepository, FacultadRepository facultadRepository, MapeadorDTO mapeador) {
        this.carreraRepository = carreraRepository;
        this.facultadRepository = facultadRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<CarreraDTO> listarPorFacultad(Integer idFacultad) throws FacultadNoEncontradaException {
        Facultad facultad = buscarFacultad(idFacultad);
        return carreraRepository.findByFacultad(facultad).stream().map(mapeador::carrera).toList();
    }

    @Transactional
    public CarreraDTO crear(Integer idFacultad, CarreraDTO dto)
            throws FacultadNoEncontradaException, CarreraRepetidaException {
        Facultad facultad = buscarFacultad(idFacultad);
        String nombre = Validaciones.texto(dto.getNombreCarrera(), "nombreCarrera");
        if (carreraRepository.existsByNombreCarreraAndFacultad(nombre, facultad)) {
            throw new CarreraRepetidaException("Ya existe una carrera con el nombre: " + nombre + " en esta facultad");
        }
        Carrera carrera = new Carrera();
        carrera.setNombreCarrera(nombre);
        carrera.setFacultad(facultad);
        return mapeador.carrera(carreraRepository.save(carrera));
    }

    @Transactional
    public CarreraDTO eliminar(Integer id) throws CarreraNoEncontradaException {
        Carrera carrera = carreraRepository.findById(id)
                .orElseThrow(() -> new CarreraNoEncontradaException(
                        "No existe la carrera con id: " + id + ", no se puede eliminar"));
        carreraRepository.delete(carrera);
        carreraRepository.flush();
        return mapeador.carrera(carrera);
    }

    private Facultad buscarFacultad(Integer idFacultad) throws FacultadNoEncontradaException {
        return facultadRepository.findById(idFacultad)
                .orElseThrow(() -> new FacultadNoEncontradaException("No existe la facultad con id: " + idFacultad));
    }
}
