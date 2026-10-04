package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.firmaya.core.entity.Notificacion;
import com.firmaya.core.entity.PreferenciaNotificacion;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.repository.NotificacionRepository;
import com.firmaya.core.repository.PreferenciaNotificacionRepository;

class NotificacionServiceTest {

    private JavaMailSender mailSender;
    private NotificacionRepository notificacionRepository;
    private PreferenciaNotificacionRepository preferenciaRepository;
    private NotificacionService service;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        mailSender = mock(JavaMailSender.class);
        notificacionRepository = mock(NotificacionRepository.class);
        preferenciaRepository = mock(PreferenciaNotificacionRepository.class);
        service = new NotificacionService(mailSender, notificacionRepository, new PreferenciaService(preferenciaRepository),
                "no-reply@firmaya.com");

        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setEmail("maria@mail.com");
        when(preferenciaRepository.findFirstByUsuarioIdUsuarioAndTipoEventoAndCanalIsNull(any(), any()))
                .thenReturn(Optional.empty());
        when(preferenciaRepository.findFirstByUsuarioIdUsuarioAndCanalAndTipoEventoIsNull(any(), any()))
                .thenReturn(Optional.empty());
    }

    @Test
    void parteExternaSiempreRecibeCorreo() {
        service.notificarEvento(null, "ana@mail.com", 10, PreferenciaNotificacion.EVENTO_CAMBIO_ESTADO, "Asunto",
                "Mensaje");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void usuarioSinPreferenciasRecibeCorreoYPlataforma() {
        service.notificarEvento(usuario, "maria@mail.com", 10, PreferenciaNotificacion.EVENTO_CAMBIO_ESTADO,
                "Asunto", "Mensaje");

        verify(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(2)).save(captor.capture());
        List<Notificacion> guardadas = captor.getAllValues();
        assertEquals(Notificacion.CANAL_CORREO, guardadas.get(0).getCanal());
        assertEquals(PreferenciaNotificacion.CANAL_PLATAFORMA, guardadas.get(1).getCanal());
    }

    @Test
    void eventoDesactivadoNoSeNotifica() {
        PreferenciaNotificacion desactivado = new PreferenciaNotificacion();
        desactivado.setActiva(false);
        when(preferenciaRepository.findFirstByUsuarioIdUsuarioAndTipoEventoAndCanalIsNull(1,
                PreferenciaNotificacion.EVENTO_CAMBIO_ESTADO)).thenReturn(Optional.of(desactivado));

        service.notificarEvento(usuario, "maria@mail.com", 10, PreferenciaNotificacion.EVENTO_CAMBIO_ESTADO,
                "Asunto", "Mensaje");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    void canalCorreoDesactivadoSoloRegistraEnPlataforma() {
        PreferenciaNotificacion sinCorreo = new PreferenciaNotificacion();
        sinCorreo.setActiva(false);
        when(preferenciaRepository.findFirstByUsuarioIdUsuarioAndCanalAndTipoEventoIsNull(1,
                PreferenciaNotificacion.CANAL_CORREO)).thenReturn(Optional.of(sinCorreo));

        service.notificarEvento(usuario, "maria@mail.com", 10, PreferenciaNotificacion.EVENTO_NUEVO_COMENTARIO,
                "Asunto", "Mensaje");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(notificacionRepository).save(any(Notificacion.class));
    }
}
