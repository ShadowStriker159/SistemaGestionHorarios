package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.dto.FranjaHorariaDTO;
import co.edu.unbosque.SistemaHorario.services.FranjaHorariaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios/franjas")
public class FranjaHorariaController {

    private final FranjaHorariaService franjaHorariaService;

    public FranjaHorariaController(FranjaHorariaService franjaHorariaService) {
        this.franjaHorariaService = franjaHorariaService;
    }

    @GetMapping
    public ResponseEntity<List<FranjaHorariaDTO>> listar() {
        return ResponseEntity.ok(franjaHorariaService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<FranjaHorariaDTO> crear(@RequestBody FranjaHorariaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(franjaHorariaService.crear(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<FranjaHorariaDTO> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(franjaHorariaService.eliminar(id));
    }
}
