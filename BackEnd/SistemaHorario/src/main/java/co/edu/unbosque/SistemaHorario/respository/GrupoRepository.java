package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Curso;
import co.edu.unbosque.SistemaHorario.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrupoRepository extends JpaRepository<Grupo,Integer> {
    List<Grupo> findByCurso(Curso curso);
}
