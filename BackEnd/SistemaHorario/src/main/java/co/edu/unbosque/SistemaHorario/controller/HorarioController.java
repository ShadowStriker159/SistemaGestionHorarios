package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.dto.HorarioDTO;
import co.edu.unbosque.SistemaHorario.model.exception.FacultadNoEncontradaException;
import co.edu.unbosque.SistemaHorario.model.exception.HorarioInalcanzableException;
import co.edu.unbosque.SistemaHorario.services.HorarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    private final HorarioService horarioService;

    public HorarioController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    @GetMapping("/listar")
    public ResponseEntity<List<HorarioDTO>> listarHorarios() {
        return ResponseEntity.ok(horarioService.listarHorarios());
    }

    @PostMapping("/generar")
    public ResponseEntity<?> generarHorario(@RequestParam(defaultValue = "1") Integer idFacultad) {
        try {
            return ResponseEntity.ok(horarioService.generarHorario(idFacultad));
        } catch (FacultadNoEncontradaException | HorarioInalcanzableException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
