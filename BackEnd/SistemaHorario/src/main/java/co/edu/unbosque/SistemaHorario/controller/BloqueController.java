package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.dto.BloqueDTO;
import co.edu.unbosque.SistemaHorario.model.dto.AulaDTO;
import co.edu.unbosque.SistemaHorario.services.BloqueService;
import co.edu.unbosque.SistemaHorario.services.AulaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios/bloques")
public class BloqueController {

    private final BloqueService bloqueService;
    private final AulaService aulaService;

    public BloqueController(BloqueService bloqueService, AulaService aulaService) {
        this.bloqueService = bloqueService;
        this.aulaService = aulaService;
    }

    @GetMapping
    public ResponseEntity<List<BloqueDTO>> listar() {
        return ResponseEntity.ok(bloqueService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<BloqueDTO> crear(@RequestBody BloqueDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bloqueService.crear(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BloqueDTO> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(bloqueService.eliminar(id));
    }

    @PostMapping("/{idBloque}/aulas")
    public ResponseEntity<AulaDTO> crearAula(@PathVariable Integer idBloque, @RequestBody AulaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aulaService.crear(idBloque, dto));
    }
}
