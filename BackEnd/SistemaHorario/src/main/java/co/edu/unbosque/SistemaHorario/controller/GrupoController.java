package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.dto.GrupoDTO;
import co.edu.unbosque.SistemaHorario.services.GrupoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios/grupos")
public class GrupoController {

    private final GrupoService grupoService;

    public GrupoController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    @PostMapping("/{idGrupo}/estudiantes/{idEstudiante}")
    public ResponseEntity<GrupoDTO> inscribir(@PathVariable Integer idGrupo, @PathVariable String idEstudiante) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grupoService.inscribirEstudiante(idGrupo, idEstudiante));
    }

    @DeleteMapping("/{idGrupo}/estudiantes/{idEstudiante}")
    public ResponseEntity<GrupoDTO> retirar(@PathVariable Integer idGrupo, @PathVariable String idEstudiante) {
        return ResponseEntity.ok(grupoService.retirarEstudiante(idGrupo, idEstudiante));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GrupoDTO> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(grupoService.eliminar(id));
    }
}
