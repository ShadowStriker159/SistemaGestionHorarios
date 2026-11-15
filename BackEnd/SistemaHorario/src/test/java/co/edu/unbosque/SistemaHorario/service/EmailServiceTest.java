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

/** PRUEBA UNITARIA: EmailService arma y envía el mensaje al JavaMailSender. */
class EmailServiceTest {

    @Test void enviaUnMensajeSimpleConLosDatosPedidos() {
        JavaMailSender sender = mock(JavaMailSender.class);
        EmailService service = new EmailService();
        ReflectionTestUtils.setField(service, "mailSender", sender);

        service.enviarCorreoSimple("a@u.edu", "Asunto", "Contenido");

        ArgumentCaptor<SimpleMailMessage> msg = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(msg.capture());
        assertArrayEquals(new String[]{"a@u.edu"}, msg.getValue().getTo());
        assertEquals("Asunto", msg.getValue().getSubject());
        assertEquals("Contenido", msg.getValue().getText());
    }
}
