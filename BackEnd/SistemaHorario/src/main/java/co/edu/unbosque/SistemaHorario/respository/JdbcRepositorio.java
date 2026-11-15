package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.*;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Base de los repositorios: JDBC puro (java.sql), sin ORM. Cada repositorio escribe su SQL a mano.
 * Usa DataSourceUtils para que, si el servicio está en una transacción de Spring (@Transactional),
 * todas las sentencias compartan la misma conexión.
 */
abstract class JdbcRepositorio {

    @FunctionalInterface
    interface Fila<T> {
        T mapear(ResultSet rs) throws SQLException;
    }

    protected final DataSource dataSource;
    /** Conexión de la transacción propia (cuando no hay una de Spring en curso). */
    private final ThreadLocal<Connection> propia = new ThreadLocal<>();

    protected JdbcRepositorio(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ------------------------------------------------------------------ ejecución

    protected <T> List<T> consultar(String sql, Fila<T> fila, Object... parametros) {
        Connection c = abrir();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            asignar(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> resultado = new ArrayList<>();
                while (rs.next()) {
                    resultado.add(fila.mapear(rs));
                }
                return resultado;
            }
        } catch (SQLException e) {
            throw traducir(e, sql);
        } finally {
            cerrar(c);
        }
    }

    protected <T> Optional<T> consultarUno(String sql, Fila<T> fila, Object... parametros) {
        List<T> filas = consultar(sql, fila, parametros);
        return filas.isEmpty() ? Optional.empty() : Optional.of(filas.get(0));
    }

    protected boolean existe(String sql, Object... parametros) {
        return !consultar(sql + " LIMIT 1", rs -> 1, parametros).isEmpty();
    }

    /** INSERT, UPDATE o DELETE. Devuelve las filas afectadas. */
    protected int ejecutar(String sql, Object... parametros) {
        Connection c = abrir();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            asignar(ps, parametros);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw traducir(e, sql);
        } finally {
            cerrar(c);
        }
    }

    /** INSERT que devuelve la llave autogenerada. */
    protected Integer insertar(String sql, Object... parametros) {
        Connection c = abrir();
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(ps, parametros);
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                return llaves.next() ? llaves.getInt(1) : null;
            }
        } catch (SQLException e) {
            throw traducir(e, sql);
        } finally {
            cerrar(c);
        }
    }

    // ------------------------------------------------------------------ transacciones

    /**
     * Ejecuta varias sentencias de forma atómica. Si ya hay una transacción de Spring, se une a ella;
     * si no, abre la suya: COMMIT si todo sale bien, ROLLBACK si algo falla.
     */
    protected <T> T transaccion(Supplier<T> trabajo) {
        if (propia.get() != null || TransactionSynchronizationManager.isActualTransactionActive()) {
            return trabajo.get();
        }
        Connection c = DataSourceUtils.getConnection(dataSource);
        try {
            boolean autoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            propia.set(c);
            try {
                T resultado = trabajo.get();
                c.commit();
                return resultado;
            } catch (RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                propia.remove();
                c.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw traducir(e, "transacción");
        } finally {
            DataSourceUtils.releaseConnection(c, dataSource);
        }
    }

    private Connection abrir() {
        Connection c = propia.get();
        return c != null ? c : DataSourceUtils.getConnection(dataSource);
    }

    private void cerrar(Connection c) {
        if (propia.get() == null) {
            DataSourceUtils.releaseConnection(c, dataSource);
        }
    }

    // ------------------------------------------------------------------ utilidades

    private static void asignar(PreparedStatement ps, Object[] parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            Object valor = parametros[i];
            if (valor instanceof Enum<?>) {
                valor = ((Enum<?>) valor).name();
            } else if (valor instanceof LocalTime) {
                valor = Time.valueOf((LocalTime) valor);
            }
            ps.setObject(i + 1, valor);
        }
    }

    /** "?, ?, ?" para un IN (...) de n elementos. */
    protected static String marcadores(int n) {
        return String.join(", ", java.util.Collections.nCopies(n, "?"));
    }

    /** "t.col1 AS p_col1, t.col2 AS p_col2" para evitar choques de nombres en los JOIN. */
    protected static String columnas(String tabla, String prefijo, String... cols) {
        List<String> partes = new ArrayList<>();
        for (String col : cols) {
            partes.add(tabla + "." + col + " AS " + prefijo + col);
        }
        return String.join(", ", partes);
    }

    /** Las llaves foráneas y UNIQUE violadas se reportan igual que antes (DataIntegrityViolationException). */
    protected RuntimeException traducir(SQLException e, String sql) {
        String mensaje = "Error de base de datos: " + e.getMessage();
        if (e instanceof SQLIntegrityConstraintViolationException
                || (e.getSQLState() != null && e.getSQLState().startsWith("23"))) {
            return new DataIntegrityViolationException(mensaje, e);
        }
        return new DataAccessResourceFailureException(mensaje + " [SQL: " + sql + "]", e);
    }

    // ------------------------------------------------------------------ lectura de columnas

    protected static Integer entero(ResultSet rs, String columna) throws SQLException {
        int valor = rs.getInt(columna);
        return rs.wasNull() ? null : valor;
    }

    protected static <E extends Enum<E>> E enumerado(ResultSet rs, String columna, Class<E> tipo) throws SQLException {
        String valor = rs.getString(columna);
        return valor == null ? null : Enum.valueOf(tipo, valor);
    }

    protected static LocalTime hora(ResultSet rs, String columna) throws SQLException {
        Time valor = rs.getTime(columna);
        return valor == null ? null : valor.toLocalTime();
    }

    // ------------------------------------------------------------------ filas → entidades (compartidas)

    protected static final String[] COL_FRANJA = {"id_franja", "dia_semana", "hora_inicio", "hora_fin", "estado_franja", "categoria"};
    protected static final String[] COL_BLOQUE = {"id_bloque", "nombre_bloque", "etiqueta_bloque", "ubicacion"};
    protected static final String[] COL_AULA = {"id_bloque", "numero_aula", "capacidad", "tipo_aula", "estado_aula"};
    protected static final String[] COL_CURSO = {"id_curso", "nombre_curso", "intensidad_horaria", "semestre", "creditos", "aula_requerida"};
    protected static final String[] COL_DOCENTE = {"id", "nombre", "apellido", "correo", "password"};

    protected static FranjaHoraria franja(ResultSet rs, String p) throws SQLException {
        return new FranjaHoraria(entero(rs, p + "id_franja"), enumerado(rs, p + "dia_semana", DiaSemana.class),
                enumerado(rs, p + "estado_franja", EstadoFranja.class), enumerado(rs, p + "categoria", Categoria.class),
                hora(rs, p + "hora_inicio"), hora(rs, p + "hora_fin"));
    }

    protected static Bloque bloque(ResultSet rs, String p) throws SQLException {
        return new Bloque(entero(rs, p + "id_bloque"), rs.getString(p + "nombre_bloque"),
                rs.getString(p + "etiqueta_bloque"), rs.getString(p + "ubicacion"));
    }

    protected static Aula aula(ResultSet rs, String p, Bloque bloque) throws SQLException {
        return new Aula(new AulaId(entero(rs, p + "id_bloque"), rs.getString(p + "numero_aula")),
                entero(rs, p + "capacidad"), enumerado(rs, p + "tipo_aula", TipoAula.class),
                enumerado(rs, p + "estado_aula", EstadoAula.class), bloque);
    }

    protected static Curso curso(ResultSet rs, String p) throws SQLException {
        Curso c = new Curso();
        c.setIdCurso(entero(rs, p + "id_curso"));
        c.setNombreCurso(rs.getString(p + "nombre_curso"));
        c.setIntensidadHoraria(entero(rs, p + "intensidad_horaria"));
        c.setSemestre(entero(rs, p + "semestre"));
        c.setCreditos(entero(rs, p + "creditos"));
        c.setAulaRequerida(enumerado(rs, p + "aula_requerida", TipoAula.class));
        c.setPrerrequisitos(new ArrayList<>());
        c.setGrupos(new ArrayList<>());
        c.setPlanesEstudio(new ArrayList<>());
        return c;
    }

    protected static Docente docente(ResultSet rs, String p) throws SQLException {
        Docente d = new Docente();
        d.setId(rs.getString(p + "id"));
        d.setNombre(rs.getString(p + "nombre"));
        d.setApellido(rs.getString(p + "apellido"));
        d.setCorreo(rs.getString(p + "correo"));
        d.setPassword(rs.getString(p + "password"));
        d.setEscalafon(enumerado(rs, p + "escalafon", Escalafon.class));
        d.setEstadoDocente(enumerado(rs, p + "estado_docente", EstadoDocente.class));
        d.setTipoVinculacion(enumerado(rs, p + "tipo_vinculacion", TipoVinculacion.class));
        d.setDisponibilidad(new ArrayList<>());
        return d;
    }

    /** Estudiante con su plan y su carrera (columnas: persona, estudiante, e_plan_*, e_carrera_*). */
    protected static Estudiante estudiante(ResultSet rs, String p) throws SQLException {
        Estudiante e = new Estudiante();
        e.setId(rs.getString(p + "id"));
        e.setNombre(rs.getString(p + "nombre"));
        e.setApellido(rs.getString(p + "apellido"));
        e.setCorreo(rs.getString(p + "correo"));
        e.setPassword(rs.getString(p + "password"));
        e.setSemestreCursa(entero(rs, p + "semestre_cursa"));
        e.setEstadoEstudiante(enumerado(rs, p + "estado_estudiante", EstadoEstudiante.class));
        e.setCreditosAprobados(entero(rs, p + "creditos_aprobados"));
        Integer idPlan = entero(rs, p + "id_plan");
        if (idPlan != null) {
            PlanEstudios plan = new PlanEstudios();
            plan.setIdPlanEstudios(idPlan);
            plan.setNombrePlanEstudios(rs.getString(p + "nombre_plan"));
            plan.setCursos(new ArrayList<>());
            Integer idCarrera = entero(rs, p + "id_carrera");
            if (idCarrera != null) {
                Carrera carrera = new Carrera();
                carrera.setIdCarrera(idCarrera);
                carrera.setNombreCarrera(rs.getString(p + "nombre_carrera"));
                plan.setCarrera(carrera);
            }
            e.setPlanEstudios(plan);
        }
        return e;
    }

    /** SELECT de estudiante con plan y carrera; el llamador agrega WHERE / ORDER BY. */
    protected static final String SELECT_ESTUDIANTE =
            "SELECT p.id, p.nombre, p.apellido, p.correo, p.password, "
                    + "e.semestre_cursa, e.estado_estudiante, e.creditos_aprobados, e.id_plan, "
                    + "pe.nombre_plan, ca.id_carrera, ca.nombre_carrera "
                    + "FROM estudiante e JOIN persona p ON p.id = e.id "
                    + "LEFT JOIN plan_estudios pe ON pe.id_plan = e.id_plan "
                    + "LEFT JOIN carrera ca ON ca.id_carrera = pe.id_carrera ";

    protected static final String SELECT_DOCENTE =
            "SELECT p.id, p.nombre, p.apellido, p.correo, p.password, "
                    + "d.escalafon, d.estado_docente, d.tipo_vinculacion "
                    + "FROM docente d JOIN persona p ON p.id = d.id ";

    /** Llena la disponibilidad (franjas) de los docentes dados con una sola consulta. */
    protected void cargarDisponibilidad(Collection<Docente> docentes) {
        if (docentes.isEmpty()) {
            return;
        }
        Map<String, List<Docente>> porId = new HashMap<>();
        for (Docente d : docentes) {
            porId.computeIfAbsent(d.getId(), k -> new ArrayList<>()).add(d);
        }
        String sql = "SELECT df.id_docente AS docente_id, " + columnas("f", "", COL_FRANJA)
                + " FROM docente_franja df JOIN franja_horaria f ON f.id_franja = df.id_franja"
                + " WHERE df.id_docente IN (" + marcadores(porId.size()) + ") ORDER BY f.id_franja";
        List<Object[]> filas = consultar(sql, rs -> new Object[]{rs.getString("docente_id"), franja(rs, "")},
                porId.keySet().toArray());
        for (Object[] fila : filas) {
            for (Docente d : porId.get((String) fila[0])) {
                d.getDisponibilidad().add((FranjaHoraria) fila[1]);
            }
        }
    }
}
