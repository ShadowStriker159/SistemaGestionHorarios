package co.edu.unbosque.SistemaHorario.respository;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class PersonaRepository extends JdbcRepositorio {

    public PersonaRepository(DataSource dataSource) {
        super(dataSource);
    }

    public boolean existsById(String id) {
        return existe("SELECT 1 FROM persona WHERE id = ?", id);
    }

    public boolean existsByCorreo(String correo) {
        return existe("SELECT 1 FROM persona WHERE correo = ?", correo);
    }

    /** ¿Hay OTRA persona (distinta de id) con ese correo? Sirve para editar sin chocar consigo mismo. */
    public boolean existsByCorreoAndIdNot(String correo, String id) {
        return existe("SELECT 1 FROM persona WHERE correo = ? AND id <> ?", correo, id);
    }
}
