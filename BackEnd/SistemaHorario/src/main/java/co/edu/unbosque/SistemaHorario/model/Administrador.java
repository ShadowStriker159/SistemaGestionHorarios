package co.edu.unbosque.SistemaHorario.model;

public class Administrador extends Persona {

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
