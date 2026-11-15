package co.edu.unbosque.SistemaHorario.controller;

import co.edu.unbosque.SistemaHorario.model.dto.AdministradorDTO;
import co.edu.unbosque.SistemaHorario.model.dto.DocenteDTO;
import co.edu.unbosque.SistemaHorario.model.dto.EstudianteDTO;
import co.edu.unbosque.SistemaHorario.model.dto.UsuarioSesionDTO;
import co.edu.unbosque.SistemaHorario.model.dto.VerificacionDTO;
import co.edu.unbosque.SistemaHorario.services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/horarios/login")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/estudiante")
    public ResponseEntity<String> loginEstudiante(@RequestBody EstudianteDTO dto) {
        return respuestaLogin(usuarioService.iniciarLoginEstudiante(dto));
    }

    @PostMapping("/docente")
    public ResponseEntity<String> loginDocente(@RequestBody DocenteDTO dto) {
        return respuestaLogin(usuarioService.iniciarLoginDocente(dto));
    }

    @PostMapping("/administrador")
    public ResponseEntity<String> loginAdministrador(@RequestBody AdministradorDTO dto) {
        return respuestaLogin(usuarioService.iniciarLoginAdministrador(dto));
    }

    @PostMapping("/verificar")
    public ResponseEntity<?> verificarCodigo(@RequestBody VerificacionDTO dto) {
        return usuarioService.verificarCodigo(dto.getRol(), dto.getId(), dto.getCodigo())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Código incorrecto o expirado"));
    }

    private ResponseEntity<String> respuestaLogin(boolean enviado) {
        if (!enviado) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciales incorrectas");
        }
        return ResponseEntity.ok("Código enviado al correo registrado");
    }
}