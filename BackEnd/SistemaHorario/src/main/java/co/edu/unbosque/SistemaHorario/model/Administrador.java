package co.edu.unbosque.SistemaHorario.model;

import jakarta.persistence.*;

@Entity
@Table(name = "administrador")
@PrimaryKeyJoinColumn(name = "id")
public class Administrador extends Persona {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_administrador")
    private TipoAdministrador tipoAdministrador;

    public Administrador() {
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public TipoAdministrador getTipoAdministrador() {
        return tipoAdministrador;
    }

    public void setTipoAdministrador(TipoAdministrador tipoAdministrador) {
        this.tipoAdministrador = tipoAdministrador;
    }
}
