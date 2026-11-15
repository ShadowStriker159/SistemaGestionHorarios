package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.dto.DocenteDTO;
import co.edu.unbosque.SistemaHorario.model.dto.FranjaHorariaDTO;
import co.edu.unbosque.SistemaHorario.model.dto.GrupoDTO;
import co.edu.unbosque.SistemaHorario.services.DocenteService;
import co.edu.unbosque.SistemaHorario.services.GrupoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios/docentes")
public class DocenteController {

    private final DocenteService docenteService;
    private final GrupoService grupoService;

    public DocenteController(DocenteService docenteService, GrupoService grupoService) {
        this.docenteService = docenteService;
        this.grupoService = grupoService;
    }

    @GetMapping
    public ResponseEntity<List<DocenteDTO>> listar() {
        return ResponseEntity.ok(docenteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocenteDTO> obtener(@PathVariable String id) {
        return ResponseEntity.ok(docenteService.obtener(id));
    }

    @GetMapping("/{id}/grupos")
    public ResponseEntity<List<GrupoDTO>> listarGrupos(@PathVariable String id) {
        return ResponseEntity.ok(grupoService.listarPorDocente(id));
    }

    @PutMapping("/{id}/disponibilidad")
    public ResponseEntity<DocenteDTO> actualizarDisponibilidad(
            @PathVariable String id, @RequestBody List<FranjaHorariaDTO> disponibilidad) {
        return ResponseEntity.ok(docenteService.actualizarDisponibilidad(id, disponibilidad));
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
