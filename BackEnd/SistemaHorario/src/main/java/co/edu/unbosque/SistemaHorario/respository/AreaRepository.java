package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Area;
import co.edu.unbosque.SistemaHorario.model.Facultad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Integer> {
    List<Area> findByFacultad(Facultad facultad);

    boolean existsByNombreAreaAndFacultad(String nombreArea, Facultad facultad);
}
