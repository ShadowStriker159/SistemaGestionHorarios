package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Facultad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
public interface FacultadRepository extends JpaRepository<Facultad, Integer> {
    @Query("SELECT f FROM Facultad f WHERE f.idFacultad = :id")
    Optional<Facultad> findByIdFacultad(@Param("id") Integer id);

    boolean existsByNombreFacultad(String nombreFacultad);
}
