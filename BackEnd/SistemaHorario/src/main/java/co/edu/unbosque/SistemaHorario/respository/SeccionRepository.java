package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.EstadoSeccion;
import co.edu.unbosque.SistemaHorario.model.FranjaHoraria;
import co.edu.unbosque.SistemaHorario.model.Grupo;
import co.edu.unbosque.SistemaHorario.model.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SeccionRepository extends JpaRepository<Seccion,Integer> {
    List<Seccion> findByGrupo(Grupo grupo);

    List<Seccion> findByEstadoSeccion(EstadoSeccion estado);

    long countByFranjaHoraria(FranjaHoraria franja);

    @Query("""
    SELECT s.franjaHoraria.idFranja FROM Seccion s
    WHERE s.grupo.docente.id = :idDocente
    AND s.franjaHoraria IS NOT NULL
    AND s.estadoSeccion = 'ASIGNADA'
""")
    List<Integer> findFranjasOcupadasPorDocente(String idDocente);
}
