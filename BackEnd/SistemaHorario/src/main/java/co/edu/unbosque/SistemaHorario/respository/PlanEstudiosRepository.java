package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Carrera;
import co.edu.unbosque.SistemaHorario.model.Curso;
import co.edu.unbosque.SistemaHorario.model.PlanEstudios;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class PlanEstudiosRepository extends JdbcRepositorio {

    private static final String SELECT = "SELECT pe.id_plan, pe.nombre_plan, pe.id_carrera, ca.nombre_carrera"
            + " FROM plan_estudios pe JOIN carrera ca ON ca.id_carrera = pe.id_carrera";

    public PlanEstudiosRepository(DataSource dataSource) {
        super(dataSource);
    }

    private static PlanEstudios mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        PlanEstudios plan = new PlanEstudios(entero(rs, "id_plan"), rs.getString("nombre_plan"), new ArrayList<>());
        Carrera carrera = new Carrera();
        carrera.setIdCarrera(entero(rs, "id_carrera"));
        carrera.setNombreCarrera(rs.getString("nombre_carrera"));
        plan.setCarrera(carrera);
        return plan;
    }

    /** Llena los cursos de los planes dados con una sola consulta. */
    private List<PlanEstudios> conCursos(List<PlanEstudios> planes) {
        if (planes.isEmpty()) {
            return planes;
        }
        Map<Integer, PlanEstudios> porId = planes.stream()
                .collect(Collectors.toMap(PlanEstudios::getIdPlanEstudios, p -> p));
        List<Object[]> filas = consultar("SELECT pc.id_plan AS pc_plan, " + columnas("c", "", COL_CURSO)
                        + " FROM plan_estudios_curso pc JOIN curso c ON c.id_curso = pc.id_curso"
                        + " WHERE pc.id_plan IN (" + marcadores(porId.size()) + ") ORDER BY pc.id_plan, c.id_curso",
                rs -> new Object[]{entero(rs, "pc_plan"), curso(rs, "")}, porId.keySet().toArray());
        for (Object[] fila : filas) {
            porId.get((Integer) fila[0]).getCursos().add((Curso) fila[1]);
        }
        return planes;
    }

    public List<PlanEstudios> findByCarrera(Carrera carrera) {
        return conCursos(consultar(SELECT + " WHERE pe.id_carrera = ? ORDER BY pe.id_plan",
                PlanEstudiosRepository::mapear, carrera.getIdCarrera()));
    }

    public boolean existsByNombrePlanEstudiosAndCarrera(String nombre, Carrera carrera) {
        return existe("SELECT 1 FROM plan_estudios WHERE nombre_plan = ? AND id_carrera = ?", nombre, carrera.getIdCarrera());
    }

    public boolean existsById(Integer id) {
        return existe("SELECT 1 FROM plan_estudios WHERE id_plan = ?", id);
    }

    public Optional<PlanEstudios> findById(Integer id) {
        List<PlanEstudios> planes = conCursos(consultar(SELECT + " WHERE pe.id_plan = ?", PlanEstudiosRepository::mapear, id));
        return planes.isEmpty() ? Optional.empty() : Optional.of(planes.get(0));
    }

    /** Guarda el plan y deja la tabla plan_estudios_curso igual a la lista de cursos del plan. */
    public PlanEstudios save(PlanEstudios plan) {
        return transaccion(() -> {
            if (plan.getIdPlanEstudios() == null) {
                plan.setIdPlanEstudios(insertar("INSERT INTO plan_estudios (id_carrera, nombre_plan) VALUES (?, ?)",
                        plan.getCarrera().getIdCarrera(), plan.getNombrePlanEstudios()));
            } else {
                ejecutar("UPDATE plan_estudios SET id_carrera = ?, nombre_plan = ? WHERE id_plan = ?",
                        plan.getCarrera().getIdCarrera(), plan.getNombrePlanEstudios(), plan.getIdPlanEstudios());
            }
            if (plan.getCursos() != null) {
                ejecutar("DELETE FROM plan_estudios_curso WHERE id_plan = ?", plan.getIdPlanEstudios());
                Set<Integer> ids = new LinkedHashSet<>();
                for (Curso c : plan.getCursos()) {
                    ids.add(c.getIdCurso());
                }
                for (Integer idCurso : ids) {
                    ejecutar("INSERT INTO plan_estudios_curso (id_plan, id_curso) VALUES (?, ?)", plan.getIdPlanEstudios(), idCurso);
                }
            }
            return plan;
        });
    }

    public void delete(PlanEstudios plan) {
        transaccion(() -> {
            ejecutar("DELETE FROM plan_estudios_curso WHERE id_plan = ?", plan.getIdPlanEstudios());
            return ejecutar("DELETE FROM plan_estudios WHERE id_plan = ?", plan.getIdPlanEstudios());
        });
    }
}
