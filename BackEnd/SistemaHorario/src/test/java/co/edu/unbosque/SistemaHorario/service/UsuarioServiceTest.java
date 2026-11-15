package co.edu.unbosque.SistemaHorario.service;

import co.edu.unbosque.SistemaHorario.model.*;
import co.edu.unbosque.SistemaHorario.model.algoritmo.IAlgoritmoHorario;
import co.edu.unbosque.SistemaHorario.model.dto.*;
import co.edu.unbosque.SistemaHorario.model.exception.*;
import co.edu.unbosque.SistemaHorario.respository.*;
import co.edu.unbosque.SistemaHorario.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** PRUEBA UNITARIA: login en dos pasos (credenciales + código por correo) de UsuarioService. */
class UsuarioServiceTest {

    private EmailService email;
    private EstudianteRepository estudianteRepo;
    private DocenteRepository docenteRepo;
    private AdministradorRepository adminRepo;
    private UsuarioService service;

    @BeforeEach
    void preparar() {
        email = mock(EmailService.class);
        estudianteRepo = mock(EstudianteRepository.class);
        docenteRepo = mock(DocenteRepository.class);
        adminRepo = mock(AdministradorRepository.class);
        service = new UsuarioService(email, estudianteRepo, docenteRepo, adminRepo);

        Estudiante e = new Estudiante();
        e.setId("E1"); e.setNombre("Luis"); e.setApellido("Mora"); e.setCorreo("luis@u.edu"); e.setPassword("pe");
        Docente d = new Docente();
        d.setId("D1"); d.setNombre("Ana"); d.setApellido("Ruiz"); d.setCorreo("ana@u.edu"); d.setPassword("pd");
        Administrador a = new Administrador();
        a.setId("A1"); a.setNombre("Sara"); a.setApellido("Gil"); a.setCorreo("sara@u.edu"); a.setPassword("pa");
        when(estudianteRepo.findById("E1")).thenReturn(Optional.of(e));
        when(docenteRepo.findById("D1")).thenReturn(Optional.of(d));
        when(adminRepo.findById("A1")).thenReturn(Optional.of(a));
    }

    private EstudianteDTO est(String id, String nombre, String pass) {
        EstudianteDTO d = new EstudianteDTO();
        d.setId(id); d.setNombre(nombre); d.setPassword(pass);
        return d;
    }

    /** Devuelve el código de 6 dígitos que se envió por correo en la última llamada. */
    private String codigoEnviado(String correoEsperado) {
        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(email, atLeastOnce()).enviarCorreoSimple(eq(correoEsperado), anyString(), cuerpo.capture());
        Matcher m = Pattern.compile("(\\d{6})").matcher(cuerpo.getValue());
        assertTrue(m.find(), "El correo debe traer un código de 6 dígitos: " + cuerpo.getValue());
        return m.group(1);
    }

    // ---- Paso 1: credenciales
    @Test void credencialesCorrectasEnvianCorreo() {
        assertTrue(service.iniciarLoginEstudiante(est("E1", "Luis", "pe")));
        verify(email).enviarCorreoSimple(eq("luis@u.edu"), anyString(), contains("Tu código de verificación es"));
    }
    @Test void passwordIncorrectaNoEnviaNada() {
        assertFalse(service.iniciarLoginEstudiante(est("E1", "Luis", "mala")));
        verifyNoInteractions(email);
    }
    @Test void nombreIncorrectoNoEnviaNada() {
        assertFalse(service.iniciarLoginEstudiante(est("E1", "Otro", "pe")));
        verifyNoInteractions(email);
    }
    @Test void usuarioInexistenteNoEnviaNada() {
        when(estudianteRepo.findById("X")).thenReturn(Optional.empty());
        assertFalse(service.iniciarLoginEstudiante(est("X", "Luis", "pe")));
        verifyNoInteractions(email);
    }
    @Test void passwordNulaNoEntra() {
        assertFalse(service.iniciarLoginEstudiante(est("E1", "Luis", null)));
    }
    @Test void loginDocenteYAdministrador() {
        DocenteDTO d = new DocenteDTO();
        d.setId("D1"); d.setNombre("Ana"); d.setPassword("pd");
        assertTrue(service.iniciarLoginDocente(d));
        AdministradorDTO a = new AdministradorDTO();
        a.setId("A1"); a.setNombre("Sara"); a.setPassword("pa");
        assertTrue(service.iniciarLoginAdministrador(a));
        a.setPassword("mala");
        assertFalse(service.iniciarLoginAdministrador(a));
    }

    // ---- Paso 2: código
    @Test void codigoCorrectoDevuelveLaSesionYSeConsume() {
        service.iniciarLoginEstudiante(est("E1", "Luis", "pe"));
        String codigo = codigoEnviado("luis@u.edu");

        Optional<UsuarioSesionDTO> r = service.verificarCodigo("estudiante", "E1", codigo);

        assertTrue(r.isPresent());
        assertEquals("E1", r.get().getId());
        assertEquals("estudiante", r.get().getRol());
        assertEquals("luis@u.edu", r.get().getCorreo());
        assertTrue(service.verificarCodigo("estudiante", "E1", codigo).isEmpty(), "El código solo sirve una vez");
    }
    @Test void codigoIncorrectoNoDaSesion() {
        service.iniciarLoginEstudiante(est("E1", "Luis", "pe"));
        assertTrue(service.verificarCodigo("estudiante", "E1", "000000").isEmpty());
    }
    @Test void sePuedeCorregirDespuesDeUnErrorDentroDelLimite() {
        service.iniciarLoginEstudiante(est("E1", "Luis", "pe"));
        String codigo = codigoEnviado("luis@u.edu");
        assertTrue(service.verificarCodigo("estudiante", "E1", "111111").isEmpty());
        assertTrue(service.verificarCodigo("estudiante", "E1", "222222").isEmpty());
        assertTrue(service.verificarCodigo("estudiante", "E1", codigo).isPresent());
    }
    @Test void tras3IntentosFallidosElCuartoYaNoValeNiConElCodigoCorrecto() {
        service.iniciarLoginEstudiante(est("E1", "Luis", "pe"));
        String codigo = codigoEnviado("luis@u.edu");
        for (int i = 0; i < 3; i++) {
            assertTrue(service.verificarCodigo("estudiante", "E1", "999999").isEmpty());
        }
        assertTrue(service.verificarCodigo("estudiante", "E1", codigo).isEmpty(), "Tras 3 intentos se invalida el código");
    }
    @Test void codigoConEspaciosSeAcepta() {
        service.iniciarLoginEstudiante(est("E1", "Luis", "pe"));
        String codigo = codigoEnviado("luis@u.edu");
        assertTrue(service.verificarCodigo("estudiante", "E1", "  " + codigo + " ").isPresent());
    }
    @Test void elRolNoDistingueMayusculas() {
        service.iniciarLoginEstudiante(est("E1", "Luis", "pe"));
        String codigo = codigoEnviado("luis@u.edu");
        assertTrue(service.verificarCodigo("ESTUDIANTE", "E1", codigo).isPresent());
    }
    @Test void elCodigoDeUnRolNoSirveParaOtro() {
        service.iniciarLoginEstudiante(est("E1", "Luis", "pe"));
        String codigo = codigoEnviado("luis@u.edu");
        assertTrue(service.verificarCodigo("docente", "E1", codigo).isEmpty());
        assertTrue(service.verificarCodigo("administrador", "E1", codigo).isEmpty());
    }
    @Test void sinLoginPrevioNoHayCodigo() {
        assertTrue(service.verificarCodigo("estudiante", "E1", "123456").isEmpty());
    }
    @Test void argumentosNulosDevuelvenVacio() {
        assertTrue(service.verificarCodigo(null, "E1", "123456").isEmpty());
        assertTrue(service.verificarCodigo("estudiante", null, "123456").isEmpty());
        assertTrue(service.verificarCodigo("estudiante", "E1", null).isEmpty());
    }
    @Test void loginDocenteCompletoDevuelveSesionDeDocente() {
        DocenteDTO d = new DocenteDTO();
        d.setId("D1"); d.setNombre("Ana"); d.setPassword("pd");
        service.iniciarLoginDocente(d);
        String codigo = codigoEnviado("ana@u.edu");
        UsuarioSesionDTO s = service.verificarCodigo("docente", "D1", codigo).orElseThrow();
        assertEquals("docente", s.getRol());
        assertEquals("Ruiz", s.getApellido());
    }
    @Test void loginAdministradorCompletoDevuelveSesionDeAdministrador() {
        AdministradorDTO a = new AdministradorDTO();
        a.setId("A1"); a.setNombre("Sara"); a.setPassword("pa");
        service.iniciarLoginAdministrador(a);
        String codigo = codigoEnviado("sara@u.edu");
        assertEquals("administrador", service.verificarCodigo("administrador", "A1", codigo).orElseThrow().getRol());
    }
    @Test void unNuevoLoginReemplazaAlCodigoAnterior() {
        service.iniciarLoginEstudiante(est("E1", "Luis", "pe"));
        service.iniciarLoginEstudiante(est("E1", "Luis", "pe"));
        ArgumentCaptor<String> cuerpos = ArgumentCaptor.forClass(String.class);
        verify(email, times(2)).enviarCorreoSimple(eq("luis@u.edu"), anyString(), cuerpos.capture());
        Matcher m = Pattern.compile("(\\d{6})").matcher(cuerpos.getAllValues().get(1));
        assertTrue(m.find());
        assertTrue(service.verificarCodigo("estudiante", "E1", m.group(1)).isPresent());
    }
}
