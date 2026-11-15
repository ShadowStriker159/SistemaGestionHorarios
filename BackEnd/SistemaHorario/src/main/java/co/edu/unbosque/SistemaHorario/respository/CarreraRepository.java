package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Carrera;
import co.edu.unbosque.SistemaHorario.model.Facultad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarreraRepository extends JpaRepository<Carrera,Integer> {
    List<Carrera> findByFacultad(Facultad facultad);
    boolean existsByNombreCarreraAndFacultad(String nombreCarrera, Facultad facultad);
}
