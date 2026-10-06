package com.firmaya.core.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.firmaya.core.entity.Notificacion;
import com.firmaya.core.entity.PreferenciaNotificacion;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.repository.NotificacionRepository;

/**
 * Envía correos y registra cada envío en la tabla NOTIFICACION.
 */
@Service
public class NotificacionService {

    static final String ENVIO_REGISTRADA = "Registrada";

    private final JavaMailSender mailSender;
    private final NotificacionRepository notificacionRepository;
    private final PreferenciaService preferenciaService;
    private final String remitente;

    public NotificacionService(JavaMailSender mailSender, NotificacionRepository notificacionRepository,
            PreferenciaService preferenciaService,
            @Value("${firmaya.mail.remitente}") String remitente) {
        this.mailSender = mailSender;
        this.notificacionRepository = notificacionRepository;
        this.preferenciaService = preferenciaService;
        this.remitente = remitente;
    }

    /**
     * Devuelve true si el correo se envió. Si el servicio de correo falla devuelve false,
     * para que cada caso de uso resuelva su camino alternativo.
     */
    public boolean enviarCorreo(Usuario usuario, Integer idContrato, String tipo, String correoDestino,
            String asunto, String mensaje) {
        boolean enviado;
        try {
            SimpleMailMessage correo = new SimpleMailMessage();
            correo.setFrom(remitente);
            correo.setTo(correoDestino);
            correo.setSubject(asunto);
            correo.setText(mensaje);
            mailSender.send(correo);
            enviado = true;
        } catch (MailException ex) {
            enviado = false;
        }

        guardar(usuario, idContrato, tipo, Notificacion.CANAL_CORREO, correoDestino, mensaje,
                enviado ? Notificacion.ENVIO_ENVIADO : Notificacion.ENVIO_ERROR);
        return enviado;
    }

    /**
     * Reenvía el último correo de un tipo enviado a una dirección por un contrato ("Reintentar").
     * Devuelve false si no hay un correo previo o si el envío vuelve a fallar.
     */
    public boolean reenviarUltimoCorreo(Usuario usuario, Integer idContrato, String tipo, String correoDestino,
            String asunto) {
        Notificacion anterior = notificacionRepository
                .findTopByIdContratoAndCorreoDestinoAndTipoOrderByIdNotificacionDesc(idContrato, correoDestino, tipo)
                .orElse(null);
        if (anterior == null) {
            return false;
        }
        return enviarCorreo(usuario, idContrato, tipo, correoDestino, asunto, anterior.getMensaje());
    }

    /**
     * Notificación de un evento a quien tenga "notificaciones activas" (CU-05, CU-06).
     * - Parte externa (sin usuario): siempre por correo.
     * - Usuario interno: según sus preferencias del CU-20 (evento y canales). Sin preferencias guardadas,
     *   todo se considera activo.
     */
    public void notificarEvento(Usuario usuario, String correoDestino, Integer idContrato, String evento,
            String asunto, String mensaje) {
        if (usuario == null) {
            enviarCorreo(null, idContrato, evento, correoDestino, asunto, mensaje);
            return;
        }
        if (!preferenciaService.eventoActivo(usuario, evento)) {
            return;
        }
        if (preferenciaService.canalActivo(usuario, PreferenciaNotificacion.CANAL_CORREO)) {
            enviarCorreo(usuario, idContrato, evento, correoDestino, asunto, mensaje);
        }
        if (preferenciaService.canalActivo(usuario, PreferenciaNotificacion.CANAL_PLATAFORMA)) {
            guardar(usuario, idContrato, evento, PreferenciaNotificacion.CANAL_PLATAFORMA, usuario.getEmail(),
                    mensaje, ENVIO_REGISTRADA);
        }
    }

    private void guardar(Usuario usuario, Integer idContrato, String tipo, String canal, String correoDestino,
            String mensaje, String estadoEnvio) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setIdContrato(idContrato);
        notificacion.setTipo(tipo);
        notificacion.setCanal(canal);
        notificacion.setCorreoDestino(correoDestino);
        notificacion.setMensaje(mensaje);
        notificacion.setEstadoEnvio(estadoEnvio);
        notificacion.setFechaEnvio(LocalDateTime.now());
        notificacionRepository.save(notificacion);
    }
}
