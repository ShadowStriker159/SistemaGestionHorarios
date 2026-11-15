package co.edu.unbosque.SistemaHorario.services;

import co.edu.unbosque.SistemaHorario.model.Administrador;
import co.edu.unbosque.SistemaHorario.model.Docente;
import co.edu.unbosque.SistemaHorario.model.Estudiante;
import co.edu.unbosque.SistemaHorario.model.dto.AdministradorDTO;
import co.edu.unbosque.SistemaHorario.model.dto.DocenteDTO;
import co.edu.unbosque.SistemaHorario.model.dto.EstudianteDTO;
import co.edu.unbosque.SistemaHorario.model.dto.UsuarioSesionDTO;
import co.edu.unbosque.SistemaHorario.respository.AdministradorRepository;
import co.edu.unbosque.SistemaHorario.respository.DocenteRepository;
import co.edu.unbosque.SistemaHorario.respository.EstudianteRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UsuarioService {

    private static final long MINUTOS_EXPIRACION = 5;
    private static final int MAX_INTENTOS = 3;

    private static final String ROL_ESTUDIANTE = "estudiante";
    private static final String ROL_DOCENTE = "docente";
    private static final String ROL_ADMINISTRADOR = "administrador";

    private final EmailService emailService;
    private final EstudianteRepository estudianteRepository;
    private final DocenteRepository docenteRepository;
    private final AdministradorRepository administradorRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    private final Map<String, CodigoPendiente> codigosPendientes = new ConcurrentHashMap<>();

    private static class CodigoPendiente {
        final String codigo;
        final Instant expira;
        final AtomicInteger intentos = new AtomicInteger(0);

        CodigoPendiente(String codigo, Instant expira) {
            this.codigo = codigo;
            this.expira = expira;
        }
    }

    public UsuarioService(EmailService emailService,
                          EstudianteRepository estudianteRepository,
                          DocenteRepository docenteRepository,
                          AdministradorRepository administradorRepository) {
        this.emailService = emailService;
        this.estudianteRepository = estudianteRepository;
        this.docenteRepository = docenteRepository;
        this.administradorRepository = administradorRepository;
    }

    public boolean iniciarLoginEstudiante(EstudianteDTO dto) {
        Optional<Estudiante> encontrado = estudianteRepository.findById(dto.getId());
        return encontrado
                .filter(e -> credencialesValidas(e.getNombre(), e.getPassword(), dto.getNombre(), dto.getPassword()))
                .map(e -> {
                    enviarClave(ROL_ESTUDIANTE, e.getId(), e.getCorreo());
                    return true;
                })
                .orElse(false);
    }

    public boolean iniciarLoginDocente(DocenteDTO dto) {
        Optional<Docente> encontrado = docenteRepository.findById(dto.getId());
        return encontrado
                .filter(d -> credencialesValidas(d.getNombre(), d.getPassword(), dto.getNombre(), dto.getPassword()))
                .map(d -> {
                    enviarClave(ROL_DOCENTE, d.getId(), d.getCorreo());
                    return true;
                })
                .orElse(false);
    }

    public boolean iniciarLoginAdministrador(AdministradorDTO dto) {
        Optional<Administrador> encontrado = administradorRepository.findById(dto.getId());
        return encontrado
                .filter(a -> credencialesValidas(a.getNombre(), a.getPassword(), dto.getNombre(), dto.getPassword()))
                .map(a -> {
                    enviarClave(ROL_ADMINISTRADOR, a.getId(), a.getCorreo());
                    return true;
                })
                .orElse(false);
    }

    public Optional<UsuarioSesionDTO> verificarCodigo(String rol, String id, String codigo) {
        if (rol == null || id == null || codigo == null) {
            return Optional.empty();
        }

        String llave = llave(rol, id);
        CodigoPendiente pendiente = codigosPendientes.get(llave);
        if (pendiente == null) {
            return Optional.empty();
        }

        if (Instant.now().isAfter(pendiente.expira)
                || pendiente.intentos.incrementAndGet() > MAX_INTENTOS) {
            codigosPendientes.remove(llave);
            return Optional.empty();
        }

        if (!pendiente.codigo.equals(codigo.trim())) {
            return Optional.empty();
        }

        codigosPendientes.remove(llave);
        return construirSesion(rol.toLowerCase(), id);
    }

    private Optional<UsuarioSesionDTO> construirSesion(String rol, String id) {
        return switch (rol) {
            case ROL_ESTUDIANTE -> estudianteRepository.findById(id)
                    .map(e -> new UsuarioSesionDTO(
                            e.getId(), e.getNombre(), e.getApellido(), e.getCorreo(), ROL_ESTUDIANTE));
            case ROL_DOCENTE -> docenteRepository.findById(id)
                    .map(d -> new UsuarioSesionDTO(
                            d.getId(), d.getNombre(), d.getApellido(), d.getCorreo(), ROL_DOCENTE));
            case ROL_ADMINISTRADOR -> administradorRepository.findById(id)
                    .map(a -> new UsuarioSesionDTO(
                            a.getId(), a.getNombre(), a.getApellido(), a.getCorreo(), ROL_ADMINISTRADOR));
            default -> Optional.empty();
        };
    }

    private boolean credencialesValidas(String nombreGuardado, String passwordGuardada,
                                        String nombreIngresado, String passwordIngresada) {
        return nombreGuardado != null && nombreGuardado.equals(nombreIngresado)
                && passwordGuardada != null && passwordGuardada.equals(passwordIngresada);
    }

    private void enviarClave(String rol, String id, String correo) {
        String clave = generarClave();
        codigosPendientes.put(
                llave(rol, id),
                new CodigoPendiente(clave, Instant.now().plusSeconds(MINUTOS_EXPIRACION * 60)));
        enviarCorreo(correo, "Código de verificación", clave);
    }

    private String generarClave() {
        int numeroAleatorio = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(numeroAleatorio);
    }

    private void enviarCorreo(String correo, String asunto, String clave) {
        String cuerpo = "Tu código de verificación es: " + clave
                + "\nExpira en " + MINUTOS_EXPIRACION + " minutos.";
        emailService.enviarCorreoSimple(correo, asunto, cuerpo);
    }

    private String llave(String rol, String id) {
        return rol.toLowerCase() + ":" + id;
    }
}
