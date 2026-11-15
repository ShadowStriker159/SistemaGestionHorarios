package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Carrera;
import co.edu.unbosque.SistemaHorario.model.Estudiante;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Repository
public class EstudianteRepository extends JdbcRepositorio {

    public EstudianteRepository(DataSource dataSource) {
        super(dataSource);
    }

    private boolean existsById(String id) {
        return existe("SELECT 1 FROM estudiante WHERE id = ?", id);
    }

    public List<Estudiante> findAll() {
        return consultar(SELECT_ESTUDIANTE + "ORDER BY p.id", rs -> estudiante(rs, ""));
    }

    public Optional<Estudiante> findById(String id) {
        return consultarUno(SELECT_ESTUDIANTE + "WHERE e.id = ?", rs -> estudiante(rs, ""), id);
    }

    public List<Estudiante> findByPlanEstudiosCarrera(Carrera carrera) {
        return consultar(SELECT_ESTUDIANTE + "WHERE ca.id_carrera = ? ORDER BY p.id", rs -> estudiante(rs, ""),
                carrera.getIdCarrera());
    }

    /** Inserta o actualiza persona + estudiante. */
    public Estudiante save(Estudiante e) {
        return transaccion(() -> {
            Integer idPlan = e.getPlanEstudios() == null ? null : e.getPlanEstudios().getIdPlanEstudios();
            if (existsById(e.getId())) {
                ejecutar("UPDATE persona SET nombre = ?, apellido = ?, password = ?, correo = ? WHERE id = ?",
                        e.getNombre(), e.getApellido(), e.getPassword(), e.getCorreo(), e.getId());
                ejecutar("UPDATE estudiante SET id_plan = ?, estado_estudiante = ?, semestre_cursa = ?, creditos_aprobados = ?"
                                + " WHERE id = ?",
                        idPlan, e.getEstadoEstudiante(), e.getSemestreCursa(), e.getCreditosAprobados(), e.getId());
            } else {
                ejecutar("INSERT INTO persona (id, nombre, apellido, password, correo) VALUES (?, ?, ?, ?, ?)",
                        e.getId(), e.getNombre(), e.getApellido(), e.getPassword(), e.getCorreo());
                ejecutar("INSERT INTO estudiante (id, id_plan, estado_estudiante, semestre_cursa, creditos_aprobados)"
                                + " VALUES (?, ?, ?, ?, ?)",
                        e.getId(), idPlan, e.getEstadoEstudiante(), e.getSemestreCursa(), e.getCreditosAprobados());
            }
            return e;
        });
    }

    public void delete(Estudiante e) {
        transaccion(() -> {
            ejecutar("DELETE FROM estudiante WHERE id = ?", e.getId());
            return ejecutar("DELETE FROM persona WHERE id = ?", e.getId());
        });
    }
}
