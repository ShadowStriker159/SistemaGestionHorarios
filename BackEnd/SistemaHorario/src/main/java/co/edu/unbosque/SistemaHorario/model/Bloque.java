package co.edu.unbosque.SistemaHorario.model;

public class Bloque {

    private Integer idBloque;

    private String nombreBloque;

    private String etiquetaBloque;

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
