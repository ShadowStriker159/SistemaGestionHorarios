package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.dto.AulaDTO;
import co.edu.unbosque.SistemaHorario.services.AulaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios/aulas")
public class AulaController {

    private final AulaService aulaService;

    public AulaController(AulaService aulaService) {
        this.aulaService = aulaService;
    }

    @GetMapping
    public ResponseEntity<List<AulaDTO>> listar() {
        return ResponseEntity.ok(aulaService.listarTodos());
    }

    @DeleteMapping("/{idBloque}/{numeroAula}")
    public ResponseEntity<AulaDTO> eliminar(@PathVariable Integer idBloque, @PathVariable String numeroAula) {
        return ResponseEntity.ok(aulaService.eliminar(idBloque, numeroAula));
    }
}
