package co.edu.unbosque.SistemaHorario.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bloque")
public class Bloque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bloque")
    private Integer idBloque;

    @Column(name = "nombre_bloque", nullable = false, length = 100)
    private String nombreBloque;

    @Column(name = "etiqueta_bloque", nullable = false, length = 20)
    private String etiquetaBloque;

    @Column(name = "ubicacion", nullable = false, length = 200)
    private String ubicacion;
    public Bloque() {
    }

    public Bloque(Integer idBloque, String nombreBloque, String etiquetaBloque, String ubicacion) {
        this.idBloque = idBloque;
        this.nombreBloque = nombreBloque;
        this.etiquetaBloque = etiquetaBloque;
        this.ubicacion = ubicacion;
    }

    public Integer getIdBloque() {
        return idBloque;
    }

    public void setIdBloque(Integer idBloque) {
        this.idBloque = idBloque;
    }

    public String getNombreBloque() {
        return nombreBloque;
    }

    public void setNombreBloque(String nombreBloque) {
        this.nombreBloque = nombreBloque;
    }

    public String getEtiquetaBloque() {
        return etiquetaBloque;
    }

    public void setEtiquetaBloque(String etiquetaBloque) {
        this.etiquetaBloque = etiquetaBloque;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Bloque bloque = (Bloque) obj;
        return idBloque.equals(bloque.idBloque);
    }

}
