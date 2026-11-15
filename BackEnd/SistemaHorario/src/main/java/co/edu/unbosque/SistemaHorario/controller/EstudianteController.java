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

    @GetMapping("/{id}")
    public ResponseEntity<EstudianteDTO> obtener(@PathVariable String id) {
        return ResponseEntity.ok(estudianteService.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstudianteDTO> actualizar(@PathVariable String id, @RequestBody EstudianteDTO dto) {
        return ResponseEntity.ok(estudianteService.actualizar(id, dto));
    }

    @PostMapping
    public ResponseEntity<EstudianteDTO> crear(@RequestBody EstudianteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estudianteService.crear(dto));
    }

    @PutMapping("/{id}/plan/{idPlan}")
    public ResponseEntity<EstudianteDTO> asignarPlan(@PathVariable String id, @PathVariable Integer idPlan) {
        return ResponseEntity.ok(estudianteService.asignarPlan(id, idPlan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EstudianteDTO> eliminar(@PathVariable String id) {
        return ResponseEntity.ok(estudianteService.eliminar(id));
    }
}
