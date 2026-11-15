package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.*;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SeccionRepository extends JdbcRepositorio {

    public SeccionRepository(DataSource dataSource) {
        super(dataSource);
    }

    /** Todas las secciones con lo que necesita la vista de horarios: grupo, curso, programa, docente, aula, bloque y franja. */
    public List<Seccion> findAll() {
        String sql = "SELECT s.id_seccion, s.estado_seccion, g.id_grupo, "
                + "p.id AS d_id, p.nombre AS d_nombre, p.apellido AS d_apellido, "
                + columnas("c", "c_", COL_CURSO) + ", "
                + columnas("a", "a_", COL_AULA) + ", " + columnas("b", "b_", COL_BLOQUE) + ", "
                + columnas("f", "f_", COL_FRANJA) + ", "
                + "(SELECT ca.nombre_carrera FROM plan_estudios_curso pc"
                + "   JOIN plan_estudios pe ON pe.id_plan = pc.id_plan"
                + "   JOIN carrera ca ON ca.id_carrera = pe.id_carrera"
                + "  WHERE pc.id_curso = c.id_curso ORDER BY pe.id_plan LIMIT 1) AS programa "
                + "FROM seccion s JOIN grupo g ON g.id_grupo = s.id_grupo JOIN curso c ON c.id_curso = g.id_curso "
                + "JOIN persona p ON p.id = g.id_docente "
                + "JOIN aula a ON a.id_bloque = s.id_bloque AND a.numero_aula = s.numero_aula "
                + "JOIN bloque b ON b.id_bloque = a.id_bloque JOIN franja_horaria f ON f.id_franja = s.id_franja "
                + "ORDER BY s.id_seccion";
        return consultar(sql, rs -> {
            Curso curso = curso(rs, "c_");
            String programa = rs.getString("programa");
            if (programa != null) {
                Carrera carrera = new Carrera();
                carrera.setNombreCarrera(programa);
                PlanEstudios plan = new PlanEstudios();
                plan.setCarrera(carrera);
                curso.getPlanesEstudio().add(plan);
            }
            Docente docente = new Docente();
            docente.setId(rs.getString("d_id"));
            docente.setNombre(rs.getString("d_nombre"));
            docente.setApellido(rs.getString("d_apellido"));
            Grupo grupo = new Grupo();
            grupo.setIdGrupo(entero(rs, "id_grupo"));
            grupo.setCurso(curso);
            grupo.setDocente(docente);
            Seccion s = new Seccion(entero(rs, "id_seccion"), enumerado(rs, "estado_seccion", EstadoSeccion.class),
                    aula(rs, "a_", bloque(rs, "b_")), franja(rs, "f_"));
            s.setGrupo(grupo);
            return s;
        });
    }

    /** Inserta todas las secciones en una sola transacción: o entran todas o ninguna. */
    public List<Seccion> saveAll(List<Seccion> secciones) {
        return transaccion(() -> {
            List<Seccion> guardadas = new ArrayList<>();
            for (Seccion s : secciones) {
                s.setIdSeccion(insertar("INSERT INTO seccion (estado_seccion, id_grupo, id_bloque, numero_aula, id_franja)"
                                + " VALUES (?, ?, ?, ?, ?)",
                        s.getEstadoSeccion(), s.getGrupo().getIdGrupo(), s.getAula().getId().getIdBloque(),
                        s.getAula().getId().getNumeroAula(), s.getFranjaHoraria().getIdFranja()));
                guardadas.add(s);
            }
            return guardadas;
        });
    }

    public void deleteAll() {
        ejecutar("DELETE FROM seccion");
    }
}
