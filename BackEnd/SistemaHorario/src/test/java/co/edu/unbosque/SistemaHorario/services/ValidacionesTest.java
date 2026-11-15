package co.edu.unbosque.SistemaHorario.services;

import co.edu.unbosque.SistemaHorario.model.TipoAula;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** PRUEBA UNITARIA: reglas de validación compartidas por todos los servicios. */
class ValidacionesTest {

    @Test
    void textoRecortaEspacios() {
        assertEquals("hola", Validaciones.texto("  hola  ", "campo"));
    }

    @Test
    void textoNuloVacioOBlancoEsObligatorio() {
        for (String v : new String[]{null, "", "   "}) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Validaciones.texto(v, "nombre"));
            assertEquals("El campo nombre es obligatorio", e.getMessage());
        }
    }

    @Test
    void positivoAceptaMayoresACero() {
        assertEquals(1, Validaciones.positivo(1, "x"));
    }

    @Test
    void positivoRechazaNuloCeroYNegativos() {
        for (Integer v : new Integer[]{null, 0, -5}) {
            assertThrows(IllegalArgumentException.class, () -> Validaciones.positivo(v, "x"));
        }
    }

    @Test
    void noNegativoAceptaCero() {
        assertEquals(0, Validaciones.noNegativo(0, "x"));
    }

    @Test
    void noNegativoRechazaNuloYNegativos() {
        assertThrows(IllegalArgumentException.class, () -> Validaciones.noNegativo(null, "x"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.noNegativo(-1, "x"));
    }

    @Test
    void enumeradoConvierteIgnorandoEspacios() {
        assertEquals(TipoAula.SISTEMAS, Validaciones.enumerado(TipoAula.class, " SISTEMAS ", "tipoAula"));
    }

    @Test
    void enumeradoVacioEsObligatorio() {
        assertThrows(IllegalArgumentException.class, () -> Validaciones.enumerado(TipoAula.class, null, "tipoAula"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.enumerado(TipoAula.class, " ", "tipoAula"));
    }

    @Test
    void enumeradoInvalidoDiceElCampoYElValor() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Validaciones.enumerado(TipoAula.class, "NOEXISTE", "tipoAula"));
        assertTrue(e.getMessage().contains("tipoAula") && e.getMessage().contains("NOEXISTE"));
    }

    @Test
    void enumeradoEsSensibleAMayusculas() {
        assertThrows(IllegalArgumentException.class, () -> Validaciones.enumerado(TipoAula.class, "sistemas", "tipoAula"));
    }
}
