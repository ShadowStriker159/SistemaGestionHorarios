package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.dto.DocenteDTO;
import co.edu.unbosque.SistemaHorario.services.DocenteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios/docentes")
public class DocenteController {

    private final DocenteService docenteService;

    public DocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }

    @GetMapping
    public ResponseEntity<List<DocenteDTO>> listar() {
        return ResponseEntity.ok(docenteService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<DocenteDTO> crear(@RequestBody DocenteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(docenteService.crear(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DocenteDTO> eliminar(@PathVariable String id) {
        return ResponseEntity.ok(docenteService.eliminar(id));
    }
}
