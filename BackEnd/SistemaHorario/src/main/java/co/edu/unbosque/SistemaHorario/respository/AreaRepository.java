package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Area;
import co.edu.unbosque.SistemaHorario.model.Facultad;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Repository
public class AreaRepository extends JdbcRepositorio {

    private static final String SELECT = "SELECT a.id_area, a.nombre_area, a.descripcion, a.id_facultad, f.nombre_facultad"
            + " FROM area a JOIN facultad f ON f.id_facultad = a.id_facultad";

    public AreaRepository(DataSource dataSource) {
        super(dataSource);
    }

    private static Area mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        Area a = new Area(entero(rs, "id_area"), rs.getString("nombre_area"), rs.getString("descripcion"));
        Facultad f = new Facultad();
        f.setIdFacultad(entero(rs, "id_facultad"));
        f.setNombreFacultad(rs.getString("nombre_facultad"));
        a.setFacultad(f);
        return a;
    }

    public List<Area> findByFacultad(Facultad facultad) {
        return consultar(SELECT + " WHERE a.id_facultad = ? ORDER BY a.id_area", AreaRepository::mapear, facultad.getIdFacultad());
    }

    public boolean existsByNombreAreaAndFacultad(String nombre, Facultad facultad) {
        return existe("SELECT 1 FROM area WHERE nombre_area = ? AND id_facultad = ?", nombre, facultad.getIdFacultad());
    }

    public Optional<Area> findById(Integer id) {
        return consultarUno(SELECT + " WHERE a.id_area = ?", AreaRepository::mapear, id);
    }

    public Area save(Area a) {
        if (a.getIdArea() == null) {
            a.setIdArea(insertar("INSERT INTO area (id_facultad, nombre_area, descripcion) VALUES (?, ?, ?)",
                    a.getFacultad().getIdFacultad(), a.getNombreArea(), a.getDescripcion()));
        } else {
            ejecutar("UPDATE area SET id_facultad = ?, nombre_area = ?, descripcion = ? WHERE id_area = ?",
                    a.getFacultad().getIdFacultad(), a.getNombreArea(), a.getDescripcion(), a.getIdArea());
        }
        return a;
    }

    public void delete(Area a) {
        ejecutar("DELETE FROM area WHERE id_area = ?", a.getIdArea());
    }
}
