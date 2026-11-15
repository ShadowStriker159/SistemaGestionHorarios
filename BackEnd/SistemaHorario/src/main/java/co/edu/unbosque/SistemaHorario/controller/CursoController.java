package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.dto.CursoDTO;
import co.edu.unbosque.SistemaHorario.model.dto.GrupoDTO;
import co.edu.unbosque.SistemaHorario.services.CursoService;
import co.edu.unbosque.SistemaHorario.services.GrupoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios/cursos")
public class CursoController {

    private final CursoService cursoService;
    private final GrupoService grupoService;

    public CursoController(CursoService cursoService, GrupoService grupoService) {
        this.cursoService = cursoService;
        this.grupoService = grupoService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CursoDTO> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(cursoService.eliminar(id));
    }

    @GetMapping("/{idCurso}/grupos")
    public ResponseEntity<List<GrupoDTO>> listarGrupos(@PathVariable Integer idCurso) {
        return ResponseEntity.ok(grupoService.listarPorCurso(idCurso));
    }

    @PostMapping("/{idCurso}/grupos")
    public ResponseEntity<GrupoDTO> crearGrupo(@PathVariable Integer idCurso,
                                               @RequestParam String idDocente,
                                               @RequestBody GrupoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grupoService.crear(idCurso, idDocente, dto));
    }
}
