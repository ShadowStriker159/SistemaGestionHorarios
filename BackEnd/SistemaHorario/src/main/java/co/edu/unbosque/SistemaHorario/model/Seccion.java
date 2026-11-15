package co.edu.unbosque.SistemaHorario.model;

import jakarta.persistence.*;

@Entity
@Table(name = "seccion")
public class Seccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seccion")
    private Integer idSeccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_seccion", nullable = false)
    private EstadoSeccion estadoSeccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo", nullable = false)
    private Grupo grupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "id_bloque", referencedColumnName = "id_bloque", nullable = false),
            @JoinColumn(name = "numero_aula", referencedColumnName = "numero_aula", nullable = false)
    })
    private Aula aula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_franja", nullable = false)
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
