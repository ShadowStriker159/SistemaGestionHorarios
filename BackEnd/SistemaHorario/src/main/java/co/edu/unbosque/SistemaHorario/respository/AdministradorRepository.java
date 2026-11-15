package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Administrador;
import co.edu.unbosque.SistemaHorario.model.TipoAdministrador;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Optional;

@Repository
public class AdministradorRepository extends JdbcRepositorio {

    public AdministradorRepository(DataSource dataSource) {
        super(dataSource);
    }

    public Optional<Administrador> findById(String id) {
        return consultarUno("SELECT p.id, p.nombre, p.apellido, p.correo, p.password, a.tipo_administrador"
                        + " FROM administrador a JOIN persona p ON p.id = a.id WHERE a.id = ?",
                rs -> {
                    Administrador a = new Administrador();
                    a.setId(rs.getString("id"));
                    a.setNombre(rs.getString("nombre"));
                    a.setApellido(rs.getString("apellido"));
                    a.setCorreo(rs.getString("correo"));
                    a.setPassword(rs.getString("password"));
                    a.setTipoAdministrador(enumerado(rs, "tipo_administrador", TipoAdministrador.class));
                    return a;
                }, id);
    }
}
