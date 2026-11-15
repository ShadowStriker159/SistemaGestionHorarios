package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.*;
import co.edu.unbosque.SistemaHorario.model.exception.EstudianteNoEncontradoException;
import co.edu.unbosque.SistemaHorario.model.exception.GrupoNoEncontradoException;
import co.edu.unbosque.SistemaHorario.model.exception.GrupoRepetidoException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class GrupoRepository extends JdbcRepositorio {

    public GrupoRepository(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Carga grupos con todo lo que muestra la API: curso, docente (con disponibilidad), estudiantes
     * (con plan y carrera) y secciones (con aula, bloque y franja). Una consulta por nivel.
     */
    private List<Grupo> cargar(String condicion, Object... parametros) {
        String sql = "SELECT g.id_grupo, g.estado_grupo, g.cupo_maximo, g.cupo_minimo, "
                + columnas("c", "c_", COL_CURSO) + ", "
                + "p.id AS d_id, p.nombre AS d_nombre, p.apellido AS d_apellido, p.correo AS d_correo, p.password AS d_password, "
                + "d.escalafon AS d_escalafon, d.estado_docente AS d_estado_docente, d.tipo_vinculacion AS d_tipo_vinculacion "
                + "FROM grupo g JOIN curso c ON c.id_curso = g.id_curso "
                + "JOIN docente d ON d.id = g.id_docente JOIN persona p ON p.id = d.id "
                + "WHERE " + condicion + " ORDER BY g.id_grupo";
        List<Grupo> grupos = consultar(sql, rs -> {
            Grupo g = new Grupo();
            g.setIdGrupo(entero(rs, "id_grupo"));
            g.setEstadoGrupo(enumerado(rs, "estado_grupo", EstadoGrupo.class));
            g.setCupoMaximo(entero(rs, "cupo_maximo"));
            g.setCupoMinimo(entero(rs, "cupo_minimo"));
            g.setCurso(curso(rs, "c_"));
            g.setDocente(docente(rs, "d_"));
            g.setEstudiantes(new ArrayList<>());
            g.setSecciones(new ArrayList<>());
            return g;
        }, parametros);
        if (grupos.isEmpty()) {
            return grupos;
        }

        List<Docente> docentes = new ArrayList<>();
        Map<Integer, Grupo> porId = new HashMap<>();
        for (Grupo g : grupos) {
            docentes.add(g.getDocente());
            porId.put(g.getIdGrupo(), g);
        }
        cargarDisponibilidad(docentes);
        Object[] ids = porId.keySet().toArray();

        List<Object[]> estudiantes = consultar("SELECT ge.id_grupo AS ge_grupo, p.id, p.nombre, p.apellido, p.correo, p.password,"
                        + " e.semestre_cursa, e.estado_estudiante, e.creditos_aprobados, e.id_plan, pe.nombre_plan,"
                        + " ca.id_carrera, ca.nombre_carrera"
                        + " FROM grupo_estudiante ge JOIN estudiante e ON e.id = ge.id_estudiante"
                        + " JOIN persona p ON p.id = e.id"
                        + " LEFT JOIN plan_estudios pe ON pe.id_plan = e.id_plan"
                        + " LEFT JOIN carrera ca ON ca.id_carrera = pe.id_carrera"
                        + " WHERE ge.id_grupo IN (" + marcadores(ids.length) + ") ORDER BY ge.id_grupo, p.id",
                rs -> new Object[]{entero(rs, "ge_grupo"), estudiante(rs, "")}, ids);
        for (Object[] fila : estudiantes) {
            porId.get((Integer) fila[0]).getEstudiantes().add((Estudiante) fila[1]);
        }

        List<Object[]> secciones = consultar("SELECT s.id_seccion, s.estado_seccion, s.id_grupo AS s_grupo, "
                        + columnas("a", "a_", COL_AULA) + ", " + columnas("b", "b_", COL_BLOQUE) + ", "
                        + columnas("f", "f_", COL_FRANJA)
                        + " FROM seccion s JOIN aula a ON a.id_bloque = s.id_bloque AND a.numero_aula = s.numero_aula"
                        + " JOIN bloque b ON b.id_bloque = a.id_bloque JOIN franja_horaria f ON f.id_franja = s.id_franja"
                        + " WHERE s.id_grupo IN (" + marcadores(ids.length) + ") ORDER BY s.id_seccion",
                rs -> new Object[]{entero(rs, "s_grupo"),
                        new Seccion(entero(rs, "id_seccion"), enumerado(rs, "estado_seccion", EstadoSeccion.class),
                                aula(rs, "a_", bloque(rs, "b_")), franja(rs, "f_"))}, ids);
        for (Object[] fila : secciones) {
            Grupo g = porId.get((Integer) fila[0]);
            Seccion s = (Seccion) fila[1];
            s.setGrupo(g);
            g.getSecciones().add(s);
        }
        return grupos;
    }

    public List<Grupo> findByCurso(Curso curso) {
        return cargar("g.id_curso = ?", curso.getIdCurso());
    }

    public List<Grupo> findByDocente_Id(String idDocente) {
        return cargar("g.id_docente = ?", idDocente);
    }

    public List<Grupo> findByPlan(Integer idPlan) {
        return cargar("g.id_curso IN (SELECT id_curso FROM plan_estudios_curso WHERE id_plan = ?)", idPlan);
    }

    public boolean existsByDocente_Id(String idDocente) {
        return existe("SELECT 1 FROM grupo WHERE id_docente = ?", idDocente);
    }

    public Optional<Grupo> findById(Integer id) {
        List<Grupo> grupos = cargar("g.id_grupo = ?", id);
        return grupos.isEmpty() ? Optional.empty() : Optional.of(grupos.get(0));
    }

    /** Inserta o actualiza el grupo. Las inscripciones NO se tocan aquí: van por los procedimientos. */
    public Grupo save(Grupo g) {
        if (g.getIdGrupo() == null) {
            g.setIdGrupo(insertar("INSERT INTO grupo (id_curso, id_docente, estado_grupo, cupo_maximo, cupo_minimo)"
                            + " VALUES (?, ?, ?, ?, ?)",
                    g.getCurso().getIdCurso(), g.getDocente().getId(), g.getEstadoGrupo(), g.getCupoMaximo(), g.getCupoMinimo()));
        } else {
            ejecutar("UPDATE grupo SET id_curso = ?, id_docente = ?, estado_grupo = ?, cupo_maximo = ?, cupo_minimo = ?"
                            + " WHERE id_grupo = ?",
                    g.getCurso().getIdCurso(), g.getDocente().getId(), g.getEstadoGrupo(), g.getCupoMaximo(),
                    g.getCupoMinimo(), g.getIdGrupo());
        }
        return g;
    }

    /** Borra el grupo y sus inscripciones; si tiene secciones de horario, la llave foránea lo impide. */
    public void delete(Grupo g) {
        transaccion(() -> {
            ejecutar("DELETE FROM grupo_estudiante WHERE id_grupo = ?", g.getIdGrupo());
            return ejecutar("DELETE FROM grupo WHERE id_grupo = ?", g.getIdGrupo());
        });
    }

    // ------------------------------------------------------------------ procedimientos almacenados

    /** CALL sp_inscribir_estudiante. Devuelve los cupos que quedan en el grupo. */
    public int inscribirEstudiante(Integer idGrupo, String idEstudiante) {
        return llamar("{CALL sp_inscribir_estudiante(?, ?, ?)}", idGrupo, idEstudiante);
    }

    /** CALL sp_retirar_estudiante. Devuelve los cupos que quedan en el grupo. */
    public int retirarEstudiante(Integer idGrupo, String idEstudiante) {
        return llamar("{CALL sp_retirar_estudiante(?, ?, ?)}", idGrupo, idEstudiante);
    }

    /**
     * Los procedimientos manejan su propia transacción (START TRANSACTION / COMMIT / ROLLBACK), que no
     * puede mezclarse con la de Spring. Por eso usan una conexión propia, aparte de la del servicio.
     */
    private int llamar(String llamada, Integer idGrupo, String idEstudiante) {
        try (Connection c = dataSource.getConnection(); CallableStatement cs = c.prepareCall(llamada)) {
            cs.setInt(1, idGrupo);
            cs.setString(2, idEstudiante);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            return cs.getInt(3);
        } catch (SQLException e) {
            throw traducirProcedimiento(e);
        }
    }

    /** Errores propios de los procedimientos (SIGNAL con MYSQL_ERRNO 3000x) -> excepciones de dominio. */
    private RuntimeException traducirProcedimiento(SQLException e) {
        String mensaje = e.getMessage() == null ? "" : e.getMessage().replaceFirst("^\\(conn=\\d+\\)\\s*", "");
        switch (e.getErrorCode()) {
            case 30001:
                return new GrupoNoEncontradoException(mensaje);
            case 30002:
            case 30005:
                return new EstudianteNoEncontradoException(mensaje);
            case 30003:
                return new GrupoRepetidoException(mensaje);
            case 30004:
                return new IllegalArgumentException(mensaje);
            default:
                return traducir(e, "procedimiento almacenado");
        }
    }
}
