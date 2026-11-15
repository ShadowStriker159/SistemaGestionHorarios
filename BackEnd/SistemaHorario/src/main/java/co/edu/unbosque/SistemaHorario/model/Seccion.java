package co.edu.unbosque.SistemaHorario.model;

public class Seccion {

    private Integer idSeccion;

    private EstadoSeccion estadoSeccion;

    private Grupo grupo;

    private Aula aula;

    private FranjaHoraria franjaHoraria;

    public Seccion() {

    }

    public Seccion(Integer idSeccion, EstadoSeccion estadoSeccion, Aula aula, FranjaHoraria franjaHoraria) {
        this.idSeccion = idSeccion;
        this.estadoSeccion = estadoSeccion;
        this.aula = aula;
        this.franjaHoraria = franjaHoraria;
    }

    public Integer getIdSeccion() {
        return idSeccion;
    }

    public void setIdSeccion(Integer idSeccion) {
        this.idSeccion = idSeccion;
    }

    public EstadoSeccion getEstadoSeccion() {
        return estadoSeccion;
    }

    public void setEstadoSeccion(EstadoSeccion estadoSeccion) {
        this.estadoSeccion = estadoSeccion;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public Aula getAula() {
        return aula;
    }

    public void setAula(Aula aula) {
        this.aula = aula;
    }

    public FranjaHoraria getFranjaHoraria() {
        return franjaHoraria;
    }

    public void setFranjaHoraria(FranjaHoraria franjaHoraria) {
        this.franjaHoraria = franjaHoraria;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Seccion other = (Seccion) obj;
        return idSeccion.equals(other.idSeccion);
    }
}
