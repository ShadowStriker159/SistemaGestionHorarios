package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Aula;
import co.edu.unbosque.SistemaHorario.model.EstadoAula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface AulaRepository extends JpaRepository<Aula,Integer> {
    @Query("""
    SELECT s.aula.idAula FROM Seccion s
    WHERE s.franjaHoraria.idFranja = :idFranja
    AND s.aula IS NOT NULL
    AND s.estadoSeccion = 'ASIGNADA'
""")
    List<Integer> findAulasOcupadasEnFranja(Integer idFranja);
    List<Aula> findByEstadoAula(EstadoAula estadoAula);
}
