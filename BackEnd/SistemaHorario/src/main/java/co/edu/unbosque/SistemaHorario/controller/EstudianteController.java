package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.dto.EstudianteDTO;
import co.edu.unbosque.SistemaHorario.services.EstudianteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @GetMapping
    public ResponseEntity<List<EstudianteDTO>> listar() {
        return ResponseEntity.ok(estudianteService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<EstudianteDTO> crear(@RequestBody EstudianteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estudianteService.crear(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EstudianteDTO> eliminar(@PathVariable String id) {
        return ResponseEntity.ok(estudianteService.eliminar(id));
    }
}
