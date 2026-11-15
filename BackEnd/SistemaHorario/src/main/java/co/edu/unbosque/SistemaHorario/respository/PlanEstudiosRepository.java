package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Carrera;
import co.edu.unbosque.SistemaHorario.model.PlanEstudios;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanEstudiosRepository extends JpaRepository<PlanEstudios,Integer> {
    List<PlanEstudios> findByCarrera(Carrera carrera);
    boolean existsByNombrePlanEstudiosAndCarrera(String nombrePlanEstudios, Carrera carrera);
}
