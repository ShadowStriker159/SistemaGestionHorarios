package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.dto.PlanEstudiosDTO;
import co.edu.unbosque.SistemaHorario.model.dto.CursoDTO;
import co.edu.unbosque.SistemaHorario.services.PlanEstudiosService;
import co.edu.unbosque.SistemaHorario.services.CursoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios/planes")
public class PlanEstudiosController {

    private final PlanEstudiosService planEstudiosService;
    private final CursoService cursoService;

    public PlanEstudiosController(PlanEstudiosService planEstudiosService, CursoService cursoService) {
        this.planEstudiosService = planEstudiosService;
        this.cursoService = cursoService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PlanEstudiosDTO> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(planEstudiosService.eliminar(id));
    }

    @GetMapping("/{idPlan}/cursos")
    public ResponseEntity<List<CursoDTO>> listarCursos(@PathVariable Integer idPlan) {
        return ResponseEntity.ok(cursoService.listarPorPlan(idPlan));
    }

    @PostMapping("/{idPlan}/cursos")
    public ResponseEntity<CursoDTO> crearCurso(@PathVariable Integer idPlan, @RequestBody CursoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cursoService.crear(idPlan, dto));
    }
}
