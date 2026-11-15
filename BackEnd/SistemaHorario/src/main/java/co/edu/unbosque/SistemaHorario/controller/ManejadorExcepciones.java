package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.exception.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorExcepciones {

    @ExceptionHandler({AreaNoEncontradaException.class, AulaNoEncontradaException.class, BloqueNoEncontradoException.class, CarreraNoEncontradaException.class, CursoNoEncontradoException.class, DocenteNoEncontradoException.class, EstudianteNoEncontradoException.class, FacultadNoEncontradaException.class, FranjaHorariaNoEncontradaException.class, GrupoNoEncontradoException.class, PlanEstudiosNoEncontradoException.class, SeccionNoEncontradaException.class, InformacionNoEncontradaException.class})
    public ResponseEntity<String> noEncontrado(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler({AreaRepetidaException.class, AulaRepetidaException.class, BloqueRepetidoException.class, CarreraRepetidaException.class, CursoRepetidoException.class, DocenteRepetidoException.class, DocenteConGruposException.class, EstudianteRepetidoException.class, FacultadRepetidaException.class, FranjaHorariaRepetidaException.class, GrupoRepetidoException.class, PlanEstudiosRepetidoException.class})
    public ResponseEntity<String> repetido(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> integridad(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("La operación no es posible porque hay registros asociados o datos duplicados");
    }
}
