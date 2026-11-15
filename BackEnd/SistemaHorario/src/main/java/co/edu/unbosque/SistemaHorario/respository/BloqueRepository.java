package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Bloque;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloqueRepository extends JpaRepository<Bloque,Integer> {
    boolean existsByEtiquetaBloque(String etiquetaBloque);
}
