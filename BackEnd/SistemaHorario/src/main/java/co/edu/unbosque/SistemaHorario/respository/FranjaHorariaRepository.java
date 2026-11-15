package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.DiaSemana;
import co.edu.unbosque.SistemaHorario.model.EstadoFranja;
import co.edu.unbosque.SistemaHorario.model.FranjaHoraria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface FranjaHorariaRepository extends JpaRepository<FranjaHoraria,Integer> {
    boolean existsByDiaSemanaAndHoraInicioAndHoraFin(DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFin);

    List<FranjaHoraria> findByEstadoFranja(EstadoFranja estadoFranja);
}
