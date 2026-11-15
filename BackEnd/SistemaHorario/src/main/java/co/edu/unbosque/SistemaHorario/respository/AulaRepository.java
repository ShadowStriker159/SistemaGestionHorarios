package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Aula;
import co.edu.unbosque.SistemaHorario.model.AulaId;
import co.edu.unbosque.SistemaHorario.model.EstadoAula;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Repository
public class AulaRepository extends JdbcRepositorio {

    private static final String SELECT =
            "SELECT a.id_bloque, a.numero_aula, a.capacidad, a.tipo_aula, a.estado_aula,"
                    + " b.nombre_bloque, b.etiqueta_bloque, b.ubicacion"
                    + " FROM aula a JOIN bloque b ON b.id_bloque = a.id_bloque";

    public AulaRepository(DataSource dataSource) {
        super(dataSource);
    }

    private static Aula mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        return aula(rs, "", bloque(rs, ""));
    }

    public List<Aula> findByEstadoAula(EstadoAula estado) {
        return consultar(SELECT + " WHERE a.estado_aula = ? ORDER BY a.id_bloque, a.numero_aula",
                AulaRepository::mapear, estado);
    }

    public List<Aula> findAll() {
        return consultar(SELECT + " ORDER BY a.id_bloque, a.numero_aula", AulaRepository::mapear);
    }

    public boolean existsById(AulaId id) {
        return existe("SELECT 1 FROM aula WHERE id_bloque = ? AND numero_aula = ?", id.getIdBloque(), id.getNumeroAula());
    }

    public Optional<Aula> findById(AulaId id) {
        return consultarUno(SELECT + " WHERE a.id_bloque = ? AND a.numero_aula = ?", AulaRepository::mapear,
                id.getIdBloque(), id.getNumeroAula());
    }

    public Aula save(Aula a) {
        if (existsById(a.getId())) {
            ejecutar("UPDATE aula SET capacidad = ?, tipo_aula = ?, estado_aula = ? WHERE id_bloque = ? AND numero_aula = ?",
                    a.getCapacidad(), a.getTipoAula(), a.getEstadoAula(), a.getId().getIdBloque(), a.getId().getNumeroAula());
        } else {
            ejecutar("INSERT INTO aula (id_bloque, numero_aula, capacidad, tipo_aula, estado_aula) VALUES (?, ?, ?, ?, ?)",
                    a.getId().getIdBloque(), a.getId().getNumeroAula(), a.getCapacidad(), a.getTipoAula(), a.getEstadoAula());
        }
        return a;
    }

    public void delete(Aula a) {
        ejecutar("DELETE FROM aula WHERE id_bloque = ? AND numero_aula = ?", a.getId().getIdBloque(), a.getId().getNumeroAula());
    }
}
