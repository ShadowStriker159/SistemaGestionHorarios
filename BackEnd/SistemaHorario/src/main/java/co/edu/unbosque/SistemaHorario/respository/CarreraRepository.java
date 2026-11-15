package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Carrera;
import co.edu.unbosque.SistemaHorario.model.Facultad;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CarreraRepository extends JdbcRepositorio {

    private static final String SELECT = "SELECT c.id_carrera, c.nombre_carrera, c.id_facultad, f.nombre_facultad"
            + " FROM carrera c JOIN facultad f ON f.id_facultad = c.id_facultad";

    public CarreraRepository(DataSource dataSource) {
        super(dataSource);
    }

    private static Carrera mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        Carrera c = new Carrera(entero(rs, "id_carrera"), rs.getString("nombre_carrera"), new ArrayList<>());
        Facultad f = new Facultad();
        f.setIdFacultad(entero(rs, "id_facultad"));
        f.setNombreFacultad(rs.getString("nombre_facultad"));
        c.setFacultad(f);
        return c;
    }

    public List<Carrera> findByFacultad(Facultad facultad) {
        return consultar(SELECT + " WHERE c.id_facultad = ? ORDER BY c.id_carrera", CarreraRepository::mapear,
                facultad.getIdFacultad());
    }

    public boolean existsByNombreCarreraAndFacultad(String nombre, Facultad facultad) {
        return existe("SELECT 1 FROM carrera WHERE nombre_carrera = ? AND id_facultad = ?", nombre, facultad.getIdFacultad());
    }

    public List<Carrera> findAll() {
        return consultar(SELECT + " ORDER BY c.id_carrera", CarreraRepository::mapear);
    }

    public Optional<Carrera> findById(Integer id) {
        return consultarUno(SELECT + " WHERE c.id_carrera = ?", CarreraRepository::mapear, id);
    }

    public Carrera save(Carrera c) {
        if (c.getIdCarrera() == null) {
            c.setIdCarrera(insertar("INSERT INTO carrera (id_facultad, nombre_carrera) VALUES (?, ?)",
                    c.getFacultad().getIdFacultad(), c.getNombreCarrera()));
        } else {
            ejecutar("UPDATE carrera SET id_facultad = ?, nombre_carrera = ? WHERE id_carrera = ?",
                    c.getFacultad().getIdFacultad(), c.getNombreCarrera(), c.getIdCarrera());
        }
        return c;
    }

    public void delete(Carrera c) {
        ejecutar("DELETE FROM carrera WHERE id_carrera = ?", c.getIdCarrera());
    }
}
