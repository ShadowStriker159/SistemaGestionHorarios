package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.dto.CarreraDTO;
import co.edu.unbosque.SistemaHorario.model.dto.PlanEstudiosDTO;
import co.edu.unbosque.SistemaHorario.services.CarreraService;
import co.edu.unbosque.SistemaHorario.services.PlanEstudiosService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios/carreras")
public class CarreraController {

    private final CarreraService carreraService;
    private final PlanEstudiosService planEstudiosService;

    public CarreraController(CarreraService carreraService, PlanEstudiosService planEstudiosService) {
        this.carreraService = carreraService;
        this.planEstudiosService = planEstudiosService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CarreraDTO> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(carreraService.eliminar(id));
    }

    @GetMapping("/{idCarrera}/planes")
    public ResponseEntity<List<PlanEstudiosDTO>> listarPlanes(@PathVariable Integer idCarrera) {
        return ResponseEntity.ok(planEstudiosService.listarPorCarrera(idCarrera));
    }

    @PostMapping("/{idCarrera}/planes")
    public ResponseEntity<PlanEstudiosDTO> crearPlan(@PathVariable Integer idCarrera,
                                                     @RequestBody PlanEstudiosDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planEstudiosService.crear(idCarrera, dto));
    }
}
