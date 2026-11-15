package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.dto.AreaDTO;
import co.edu.unbosque.SistemaHorario.services.AreaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios/areas")
public class AreaController {

    private final AreaService areaService;

    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AreaDTO> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(areaService.eliminar(id));
    }
}
