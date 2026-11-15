package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Docente;
import co.edu.unbosque.SistemaHorario.model.FranjaHoraria;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class DocenteRepository extends JdbcRepositorio {

    public DocenteRepository(DataSource dataSource) {
        super(dataSource);
    }

    public boolean existsById(String id) {
        return existe("SELECT 1 FROM docente WHERE id = ?", id);
    }

    public List<Docente> findAll() {
        List<Docente> docentes = consultar(SELECT_DOCENTE + "ORDER BY p.id", rs -> docente(rs, ""));
        cargarDisponibilidad(docentes);
        return docentes;
    }

    public Optional<Docente> findById(String id) {
        Optional<Docente> encontrado = consultarUno(SELECT_DOCENTE + "WHERE d.id = ?", rs -> docente(rs, ""), id);
        encontrado.ifPresent(d -> cargarDisponibilidad(List.of(d)));
        return encontrado;
    }

    /** Inserta o actualiza persona + docente y deja docente_franja igual a su disponibilidad. */
    public Docente save(Docente d) {
        return transaccion(() -> {
            if (existsById(d.getId())) {
                ejecutar("UPDATE persona SET nombre = ?, apellido = ?, password = ?, correo = ? WHERE id = ?",
                        d.getNombre(), d.getApellido(), d.getPassword(), d.getCorreo(), d.getId());
                ejecutar("UPDATE docente SET escalafon = ?, estado_docente = ?, tipo_vinculacion = ? WHERE id = ?",
                        d.getEscalafon(), d.getEstadoDocente(), d.getTipoVinculacion(), d.getId());
            } else {
                ejecutar("INSERT INTO persona (id, nombre, apellido, password, correo) VALUES (?, ?, ?, ?, ?)",
                        d.getId(), d.getNombre(), d.getApellido(), d.getPassword(), d.getCorreo());
                ejecutar("INSERT INTO docente (id, escalafon, estado_docente, tipo_vinculacion) VALUES (?, ?, ?, ?)",
                        d.getId(), d.getEscalafon(), d.getEstadoDocente(), d.getTipoVinculacion());
            }
            if (d.getDisponibilidad() != null) {
                ejecutar("DELETE FROM docente_franja WHERE id_docente = ?", d.getId());
                Set<Integer> ids = new LinkedHashSet<>();
                for (FranjaHoraria f : d.getDisponibilidad()) {
                    ids.add(f.getIdFranja());
                }
                for (Integer idFranja : ids) {
                    ejecutar("INSERT INTO docente_franja (id_docente, id_franja) VALUES (?, ?)", d.getId(), idFranja);
                }
            }
            return d;
        });
    }

    public void delete(Docente d) {
        transaccion(() -> {
            ejecutar("DELETE FROM docente_franja WHERE id_docente = ?", d.getId());
            ejecutar("DELETE FROM docente WHERE id = ?", d.getId());
            return ejecutar("DELETE FROM persona WHERE id = ?", d.getId());
        });
    }
}
