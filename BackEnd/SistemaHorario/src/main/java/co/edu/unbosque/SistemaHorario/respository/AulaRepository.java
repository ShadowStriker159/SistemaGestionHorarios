package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Aula;
import co.edu.unbosque.SistemaHorario.model.AulaId;
import co.edu.unbosque.SistemaHorario.model.EstadoAula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AulaRepository extends JpaRepository<Aula, AulaId> {
    List<Aula> findByEstadoAula(EstadoAula estadoAula);
}
