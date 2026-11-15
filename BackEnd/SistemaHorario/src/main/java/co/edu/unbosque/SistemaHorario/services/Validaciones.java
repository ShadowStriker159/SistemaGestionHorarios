package co.edu.unbosque.SistemaHorario.services;

final class Validaciones {

    private Validaciones() {
    }

    static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        return valor.trim();
    }

    static int positivo(Integer valor, String campo) {
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser mayor a 0");
        }
        return valor;
    }

    static int noNegativo(Integer valor, String campo) {
        if (valor == null || valor < 0) {
            throw new IllegalArgumentException("El campo " + campo + " no puede ser negativo");
        }
        return valor;
    }

    static <E extends Enum<E>> E enumerado(Class<E> tipo, String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        try {
            return Enum.valueOf(tipo, valor.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Valor no válido para " + campo + ": " + valor);
        }
    }
}
