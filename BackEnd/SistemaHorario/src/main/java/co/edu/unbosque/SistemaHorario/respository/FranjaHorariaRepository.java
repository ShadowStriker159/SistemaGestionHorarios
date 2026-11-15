package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.DiaSemana;
import co.edu.unbosque.SistemaHorario.model.EstadoFranja;
import co.edu.unbosque.SistemaHorario.model.FranjaHoraria;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class FranjaHorariaRepository extends JdbcRepositorio {

    private static final String SELECT =
            "SELECT id_franja, dia_semana, hora_inicio, hora_fin, estado_franja, categoria FROM franja_horaria";

    public FranjaHorariaRepository(DataSource dataSource) {
        super(dataSource);
    }

    public boolean existsByDiaSemanaAndHoraInicioAndHoraFin(DiaSemana dia, LocalTime inicio, LocalTime fin) {
        return existe("SELECT 1 FROM franja_horaria WHERE dia_semana = ? AND hora_inicio = ? AND hora_fin = ?",
                dia, inicio, fin);
    }

    public List<FranjaHoraria> findByEstadoFranja(EstadoFranja estado) {
        return consultar(SELECT + " WHERE estado_franja = ? ORDER BY id_franja", rs -> franja(rs, ""), estado);
    }

    public List<FranjaHoraria> findAll() {
        return consultar(SELECT + " ORDER BY id_franja", rs -> franja(rs, ""));
    }

    public Optional<FranjaHoraria> findById(Integer id) {
        return consultarUno(SELECT + " WHERE id_franja = ?", rs -> franja(rs, ""), id);
    }

    public FranjaHoraria save(FranjaHoraria f) {
        if (f.getIdFranja() == null) {
            f.setIdFranja(insertar("INSERT INTO franja_horaria (dia_semana, hora_inicio, hora_fin, estado_franja, categoria)"
                            + " VALUES (?, ?, ?, ?, ?)",
                    f.getDiaSemana(), f.getHoraInicio(), f.getHoraFin(), f.getEstadoFranja(), f.getCategoria()));
        } else {
            ejecutar("UPDATE franja_horaria SET dia_semana = ?, hora_inicio = ?, hora_fin = ?, estado_franja = ?,"
                            + " categoria = ? WHERE id_franja = ?",
                    f.getDiaSemana(), f.getHoraInicio(), f.getHoraFin(), f.getEstadoFranja(), f.getCategoria(),
                    f.getIdFranja());
        }
        return f;
    }

    public void delete(FranjaHoraria f) {
        // La disponibilidad de los docentes que usaba esta franja se retira junto con ella
        transaccion(() -> {
            ejecutar("DELETE FROM docente_franja WHERE id_franja = ?", f.getIdFranja());
            return ejecutar("DELETE FROM franja_horaria WHERE id_franja = ?", f.getIdFranja());
        });
    }
}
