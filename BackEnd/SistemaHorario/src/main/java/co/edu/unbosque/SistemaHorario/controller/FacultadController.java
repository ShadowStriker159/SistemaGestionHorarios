package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.dto.FacultadDTO;
import co.edu.unbosque.SistemaHorario.model.dto.AreaDTO;
import co.edu.unbosque.SistemaHorario.model.dto.CarreraDTO;
import co.edu.unbosque.SistemaHorario.services.FacultadService;
import co.edu.unbosque.SistemaHorario.services.AreaService;
import co.edu.unbosque.SistemaHorario.services.CarreraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios/facultades")
public class FacultadController {

    private final FacultadService facultadService;
    private final AreaService areaService;
    private final CarreraService carreraService;

    public FacultadController(FacultadService facultadService, AreaService areaService, CarreraService carreraService) {
        this.facultadService = facultadService;
        this.areaService = areaService;
        this.carreraService = carreraService;
    }

    @GetMapping
    public ResponseEntity<List<FacultadDTO>> listar() {
        return ResponseEntity.ok(facultadService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<FacultadDTO> crear(@RequestBody FacultadDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facultadService.crear(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<FacultadDTO> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(facultadService.eliminar(id));
    }

    @GetMapping("/{idFacultad}/areas")
    public ResponseEntity<List<AreaDTO>> listarAreas(@PathVariable Integer idFacultad) {
        return ResponseEntity.ok(areaService.listarPorFacultad(idFacultad));
    }

    @PostMapping("/{idFacultad}/areas")
    public ResponseEntity<AreaDTO> crearArea(@PathVariable Integer idFacultad, @RequestBody AreaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(areaService.crear(idFacultad, dto));
    }

    @GetMapping("/{idFacultad}/carreras")
    public ResponseEntity<List<CarreraDTO>> listarCarreras(@PathVariable Integer idFacultad) {
        return ResponseEntity.ok(carreraService.listarPorFacultad(idFacultad));
    }

    @PostMapping("/{idFacultad}/carreras")
    public ResponseEntity<CarreraDTO> crearCarrera(@PathVariable Integer idFacultad, @RequestBody CarreraDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carreraService.crear(idFacultad, dto));
    }
}
