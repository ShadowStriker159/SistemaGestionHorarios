package co.edu.unbosque.SistemaHorario.respository;

import co.edu.unbosque.SistemaHorario.model.Bloque;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Repository
public class BloqueRepository extends JdbcRepositorio {

    private static final String SELECT = "SELECT id_bloque, nombre_bloque, etiqueta_bloque, ubicacion FROM bloque";

    public BloqueRepository(DataSource dataSource) {
        super(dataSource);
    }

    public boolean existsByEtiquetaBloque(String etiqueta) {
        return existe("SELECT 1 FROM bloque WHERE etiqueta_bloque = ?", etiqueta);
    }

    public List<Bloque> findAll() {
        return consultar(SELECT + " ORDER BY id_bloque", rs -> bloque(rs, ""));
    }

    public Optional<Bloque> findById(Integer id) {
        return consultarUno(SELECT + " WHERE id_bloque = ?", rs -> bloque(rs, ""), id);
    }

    public Bloque save(Bloque b) {
        if (b.getIdBloque() == null) {
            b.setIdBloque(insertar("INSERT INTO bloque (nombre_bloque, etiqueta_bloque, ubicacion) VALUES (?, ?, ?)",
                    b.getNombreBloque(), b.getEtiquetaBloque(), b.getUbicacion()));
        } else {
            ejecutar("UPDATE bloque SET nombre_bloque = ?, etiqueta_bloque = ?, ubicacion = ? WHERE id_bloque = ?",
                    b.getNombreBloque(), b.getEtiquetaBloque(), b.getUbicacion(), b.getIdBloque());
        }
        return b;
    }

    public void delete(Bloque b) {
        ejecutar("DELETE FROM bloque WHERE id_bloque = ?", b.getIdBloque());
    }
}
