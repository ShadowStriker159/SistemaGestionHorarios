package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Carrera;
import co.edu.unbosque.SistemaHorario.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstudianteRepository extends JpaRepository<Estudiante, String> {
    List<Estudiante> findByPlanEstudiosCarrera(Carrera carrera);

    boolean existsEstudianteById(String id);
}
