package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Curso;
import co.edu.unbosque.SistemaHorario.model.PlanEstudios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CursoRepository extends JpaRepository<Curso, Integer> {

    @Query("SELECT c FROM PlanEstudios pe JOIN pe.cursos c WHERE pe = :plan")
    List<Curso> findByPlanEstudios(@Param("plan") PlanEstudios plan);

    @Query("SELECT COUNT(c) > 0 FROM PlanEstudios pe JOIN pe.cursos c WHERE pe = :plan AND c.nombreCurso = :nombre")
    boolean existsByNombreInPlan(@Param("nombre") String nombre, @Param("plan") PlanEstudios plan);
}
