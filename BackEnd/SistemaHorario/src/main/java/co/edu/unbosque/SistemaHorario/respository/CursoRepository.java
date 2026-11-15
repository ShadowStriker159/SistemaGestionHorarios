package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Carrera;
import co.edu.unbosque.SistemaHorario.model.Curso;
import co.edu.unbosque.SistemaHorario.model.PlanEstudios;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class CursoRepository extends JdbcRepositorio {

    private static final String SELECT = "SELECT " + String.join(", ", COL_CURSO) + " FROM curso";

    public CursoRepository(DataSource dataSource) {
        super(dataSource);
    }

    public List<Curso> findByPlanEstudios(PlanEstudios plan) {
        return consultar("SELECT " + columnas("c", "", COL_CURSO) + " FROM curso c"
                        + " JOIN plan_estudios_curso pc ON pc.id_curso = c.id_curso WHERE pc.id_plan = ? ORDER BY c.id_curso",
                rs -> curso(rs, ""), plan.getIdPlanEstudios());
    }

    /** ¿Ya existe un curso con ese nombre dentro del plan? */
    public boolean existsByNombreInPlan(String nombre, PlanEstudios plan) {
        return existe("SELECT 1 FROM curso c JOIN plan_estudios_curso pc ON pc.id_curso = c.id_curso"
                + " WHERE pc.id_plan = ? AND c.nombre_curso = ?", plan.getIdPlanEstudios(), nombre);
    }

    /** Curso con sus prerrequisitos y los planes a los que pertenece. */
    public Optional<Curso> findById(Integer id) {
        Optional<Curso> encontrado = consultarUno(SELECT + " WHERE id_curso = ?", rs -> curso(rs, ""), id);
        if (encontrado.isEmpty()) {
            return encontrado;
        }
        Curso curso = encontrado.get();
        curso.setPrerrequisitos(consultar("SELECT " + columnas("c", "", COL_CURSO) + " FROM curso_prerrequisito cp"
                        + " JOIN curso c ON c.id_curso = cp.id_prerrequisito WHERE cp.id_curso = ? ORDER BY c.id_curso",
                rs -> curso(rs, ""), id));
        List<PlanEstudios> planes = consultar("SELECT pe.id_plan, pe.nombre_plan, pe.id_carrera, ca.nombre_carrera"
                        + " FROM plan_estudios_curso pc JOIN plan_estudios pe ON pe.id_plan = pc.id_plan"
                        + " JOIN carrera ca ON ca.id_carrera = pe.id_carrera WHERE pc.id_curso = ? ORDER BY pe.id_plan",
                rs -> {
                    PlanEstudios plan = new PlanEstudios(entero(rs, "id_plan"), rs.getString("nombre_plan"), new ArrayList<>());
                    Carrera carrera = new Carrera();
                    carrera.setIdCarrera(entero(rs, "id_carrera"));
                    carrera.setNombreCarrera(rs.getString("nombre_carrera"));
                    plan.setCarrera(carrera);
                    return plan;
                }, id);
        curso.setPlanesEstudio(planes);
        return Optional.of(curso);
    }

    /** Guarda el curso y deja curso_prerrequisito igual a su lista de prerrequisitos. */
    public Curso save(Curso c) {
        return transaccion(() -> {
            if (c.getIdCurso() == null) {
                c.setIdCurso(insertar("INSERT INTO curso (nombre_curso, intensidad_horaria, semestre, creditos, aula_requerida)"
                                + " VALUES (?, ?, ?, ?, ?)",
                        c.getNombreCurso(), c.getIntensidadHoraria(), c.getSemestre(), c.getCreditos(), c.getAulaRequerida()));
            } else {
                ejecutar("UPDATE curso SET nombre_curso = ?, intensidad_horaria = ?, semestre = ?, creditos = ?,"
                                + " aula_requerida = ? WHERE id_curso = ?",
                        c.getNombreCurso(), c.getIntensidadHoraria(), c.getSemestre(), c.getCreditos(), c.getAulaRequerida(),
                        c.getIdCurso());
            }
            if (c.getPrerrequisitos() != null) {
                ejecutar("DELETE FROM curso_prerrequisito WHERE id_curso = ?", c.getIdCurso());
                Set<Integer> ids = new LinkedHashSet<>();
                for (Curso pre : c.getPrerrequisitos()) {
                    ids.add(pre.getIdCurso());
                }
                for (Integer idPre : ids) {
                    ejecutar("INSERT INTO curso_prerrequisito (id_curso, id_prerrequisito) VALUES (?, ?)", c.getIdCurso(), idPre);
                }
            }
            return c;
        });
    }

    /** Quita el curso de los planes y de los prerrequisitos; si tiene grupos, la llave foránea lo impide. */
    public void delete(Curso c) {
        transaccion(() -> {
            ejecutar("DELETE FROM curso_prerrequisito WHERE id_curso = ? OR id_prerrequisito = ?", c.getIdCurso(), c.getIdCurso());
            ejecutar("DELETE FROM plan_estudios_curso WHERE id_curso = ?", c.getIdCurso());
            return ejecutar("DELETE FROM curso WHERE id_curso = ?", c.getIdCurso());
        });
    }
}
