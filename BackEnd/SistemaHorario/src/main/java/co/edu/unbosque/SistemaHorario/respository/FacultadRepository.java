package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.*;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class FacultadRepository extends JdbcRepositorio {

    private static final String SELECT = "SELECT id_facultad, nombre_facultad FROM facultad";

    /** Cursos que pertenecen a algún plan de alguna carrera de la facultad (?). */
    private static final String CURSOS_DE_FACULTAD =
            "SELECT pc.id_curso FROM plan_estudios_curso pc"
                    + " JOIN plan_estudios pe ON pe.id_plan = pc.id_plan"
                    + " JOIN carrera ca ON ca.id_carrera = pe.id_carrera WHERE ca.id_facultad = ?";

    public FacultadRepository(DataSource dataSource) {
        super(dataSource);
    }

    private static Facultad mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Facultad(entero(rs, "id_facultad"), rs.getString("nombre_facultad"), new ArrayList<>(), new ArrayList<>());
    }

    public boolean existsByNombreFacultad(String nombre) {
        return existe("SELECT 1 FROM facultad WHERE nombre_facultad = ?", nombre);
    }

    public List<Facultad> findAll() {
        return consultar(SELECT + " ORDER BY id_facultad", FacultadRepository::mapear);
    }

    public Optional<Facultad> findById(Integer id) {
        return consultarUno(SELECT + " WHERE id_facultad = ?", FacultadRepository::mapear, id);
    }

    public Facultad save(Facultad f) {
        if (f.getIdFacultad() == null) {
            f.setIdFacultad(insertar("INSERT INTO facultad (nombre_facultad) VALUES (?)", f.getNombreFacultad()));
        } else {
            ejecutar("UPDATE facultad SET nombre_facultad = ? WHERE id_facultad = ?", f.getNombreFacultad(), f.getIdFacultad());
        }
        return f;
    }

    public void delete(Facultad f) {
        ejecutar("DELETE FROM facultad WHERE id_facultad = ?", f.getIdFacultad());
    }

    /**
     * Carga la facultad con todo lo que necesita el algoritmo de horarios:
     * carreras -> planes -> cursos -> grupos -> docente (con su disponibilidad).
     * Una consulta por nivel; los objetos repetidos (un curso en varios planes) son la misma instancia.
     */
    public Optional<Facultad> findByIdCompleta(Integer id) {
        Optional<Facultad> encontrada = findById(id);
        if (encontrada.isEmpty()) {
            return encontrada;
        }
        Facultad facultad = encontrada.get();

        Map<Integer, Carrera> carreras = new LinkedHashMap<>();
        for (Carrera c : consultar("SELECT id_carrera, nombre_carrera FROM carrera WHERE id_facultad = ? ORDER BY id_carrera",
                rs -> new Carrera(entero(rs, "id_carrera"), rs.getString("nombre_carrera"), new ArrayList<>()), id)) {
            c.setFacultad(facultad);
            carreras.put(c.getIdCarrera(), c);
        }
        facultad.setCarreras(new ArrayList<>(carreras.values()));

        Map<Integer, PlanEstudios> planes = new LinkedHashMap<>();
        List<Object[]> filasPlanes = consultar("SELECT pe.id_plan, pe.nombre_plan, pe.id_carrera FROM plan_estudios pe"
                        + " JOIN carrera ca ON ca.id_carrera = pe.id_carrera WHERE ca.id_facultad = ? ORDER BY pe.id_plan",
                rs -> new Object[]{new PlanEstudios(entero(rs, "id_plan"), rs.getString("nombre_plan"), new ArrayList<>()),
                        entero(rs, "id_carrera")}, id);
        for (Object[] fila : filasPlanes) {
            PlanEstudios plan = (PlanEstudios) fila[0];
            Carrera carrera = carreras.get((Integer) fila[1]);
            plan.setCarrera(carrera);
            carrera.getPlanEstudios().add(plan);
            planes.put(plan.getIdPlanEstudios(), plan);
        }

        Map<Integer, Curso> cursos = new HashMap<>();
        List<Object[]> filasCursos = consultar("SELECT pc.id_plan AS pc_plan, " + columnas("c", "", COL_CURSO)
                        + " FROM plan_estudios_curso pc JOIN curso c ON c.id_curso = pc.id_curso"
                        + " JOIN plan_estudios pe ON pe.id_plan = pc.id_plan"
                        + " JOIN carrera ca ON ca.id_carrera = pe.id_carrera"
                        + " WHERE ca.id_facultad = ? ORDER BY pc.id_plan, c.id_curso",
                rs -> new Object[]{entero(rs, "pc_plan"), curso(rs, "")}, id);
        for (Object[] fila : filasCursos) {
            PlanEstudios plan = planes.get((Integer) fila[0]);
            Curso leido = (Curso) fila[1];
            Curso curso = cursos.computeIfAbsent(leido.getIdCurso(), k -> leido);
            plan.getCursos().add(curso);
            curso.getPlanesEstudio().add(plan);
        }

        Map<String, Docente> docentes = new HashMap<>();
        for (Docente d : consultar(SELECT_DOCENTE + "WHERE d.id IN (SELECT g.id_docente FROM grupo g WHERE g.id_curso IN ("
                + CURSOS_DE_FACULTAD + "))", rs -> docente(rs, ""), id)) {
            docentes.put(d.getId(), d);
        }
        cargarDisponibilidad(docentes.values());

        List<Object[]> filasGrupos = consultar("SELECT g.id_grupo, g.id_curso, g.id_docente, g.estado_grupo, g.cupo_maximo,"
                        + " g.cupo_minimo FROM grupo g WHERE g.id_curso IN (" + CURSOS_DE_FACULTAD + ") ORDER BY g.id_grupo",
                rs -> {
                    Grupo g = new Grupo();
                    g.setIdGrupo(entero(rs, "id_grupo"));
                    g.setEstadoGrupo(enumerado(rs, "estado_grupo", EstadoGrupo.class));
                    g.setCupoMaximo(entero(rs, "cupo_maximo"));
                    g.setCupoMinimo(entero(rs, "cupo_minimo"));
                    g.setEstudiantes(new ArrayList<>());
                    g.setSecciones(new ArrayList<>());
                    return new Object[]{g, entero(rs, "id_curso"), rs.getString("id_docente")};
                }, id);
        for (Object[] fila : filasGrupos) {
            Grupo grupo = (Grupo) fila[0];
            Curso curso = cursos.get((Integer) fila[1]);
            grupo.setCurso(curso);
            grupo.setDocente(docentes.get((String) fila[2]));
            curso.getGrupos().add(grupo);
        }
        return Optional.of(facultad);
    }
}
