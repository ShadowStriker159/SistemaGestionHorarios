package co.edu.unbosque.SistemaHorario.model.algoritmo;

import co.edu.unbosque.SistemaHorario.model.Facultad;
import co.edu.unbosque.SistemaHorario.model.Seccion;
import co.edu.unbosque.SistemaHorario.model.exception.HorarioInalcanzableException;

import java.util.List;

public interface IAlgoritmoHorario {

    List<Seccion> generarHorario(Facultad facultad)
            throws HorarioInalcanzableException;
}
