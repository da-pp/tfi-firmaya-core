package com.firmaya.core.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.InvitacionResponse;
import com.firmaya.core.dto.InvitarParteRequest;
import com.firmaya.core.dto.ParteResponse;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.UsuarioContrato;
import com.firmaya.core.exception.RecursoNoEncontradoException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.UsuarioContratoRepository;
import com.firmaya.core.repository.UsuarioRepository;

@Service
public class ParteService {

    static final String MENSAJE_YA_INVITADO = "Esta dirección ya ha sido invitada a este contrato";
    static final String MENSAJE_ERROR_CORREO =
            "No se pudo enviar el correo de invitación. ¿Desea reintentar o copiar el enlace manualmente?";
    static final String TIPO_NOTIFICACION = "Invitación";
    static final String ASUNTO_CORREO = "FirmaYA - Invitación a un contrato";

    // Duración de los tokens sin plazo definido en el documento
    static final int MINUTOS_TOKEN_INVITACION = 15;

    private final UsuarioContratoRepository usuarioContratoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContratoService contratoService;
    private final NotificacionService notificacionService;
    private final AuditoriaService auditoriaService;
    private final GeneradorToken generadorToken;
    private final String frontendUrl;

    public ParteService(UsuarioContratoRepository usuarioContratoRepository, UsuarioRepository usuarioRepository,
            ContratoService contratoService, NotificacionService notificacionService,
            AuditoriaService auditoriaService, GeneradorToken generadorToken,
            @Value("${firmaya.frontend-url}") String frontendUrl) {
        this.usuarioContratoRepository = usuarioContratoRepository;
        this.usuarioRepository = usuarioRepository;
        this.contratoService = contratoService;
        this.notificacionService = notificacionService;
        this.auditoriaService = auditoriaService;
        this.generadorToken = generadorToken;
        this.frontendUrl = frontendUrl;
    }

    @Transactional(readOnly = true)
    public List<ParteResponse> listarPartes(Integer idContrato) {
        contratoService.buscarContrato(idContrato);
        List<ParteResponse> respuesta = new ArrayList<>();
        for (UsuarioContrato parte : usuarioContratoRepository.findByContratoIdContratoOrderByIdParte(idContrato)) {
            respuesta.add(toResponse(parte));
        }
        return respuesta;
    }

    @Transactional
    public InvitacionResponse invitarParte(Integer idContrato, InvitarParteRequest request, Usuario usuario,
            String direccionIp) {
        Contrato contrato = contratoService.buscarContrato(idContrato);
        if (EstadoContrato.ARCHIVADO.equals(contrato.getEstado().getNombre())) {
            throw new ReglaNegocioException("No se pueden invitar partes a un contrato archivado.");
        }

        String email = request.getEmail().trim();
        if (usuarioContratoRepository.existsByContratoIdContratoAndCorreoInvitadoIgnoreCase(idContrato, email)) {
            throw new ReglaNegocioException("email", MENSAJE_YA_INVITADO);
        }

        // Token único de acceso para la parte invitada
        LocalDateTime ahora = LocalDateTime.now();
        UsuarioContrato parte = new UsuarioContrato();
        parte.setContrato(contrato);
        parte.setUsuario(usuarioRepository.findByEmailIgnoreCase(email).orElse(null));
        parte.setCorreoInvitado(email);
        parte.setNombreParte(request.getNombre().trim());
        parte.setRolParte(request.getRol());
        parte.setEstadoInvitacion(UsuarioContrato.INVITACION_PENDIENTE);
        parte.setFechaInvitacion(ahora);
        parte.setTokenInvitacion(generadorToken.generar());
        parte.setFechaExpiracionToken(ahora.plusMinutes(MINUTOS_TOKEN_INVITACION));
        usuarioContratoRepository.save(parte);

        auditoriaService.registrarEnContrato(usuario, "Invitación", idContrato, null,
                "Invitación de " + parte.getNombreParte() + " (" + email + ") como " + parte.getRolParte(),
                direccionIp, null, null);

        boolean enviado = notificacionService.enviarCorreo(parte.getUsuario(), idContrato, TIPO_NOTIFICACION, email,
                ASUNTO_CORREO, armarCorreo(parte, request.getMensaje()));
        return resultadoEnvio(parte, enviado);
    }

    @Transactional
    public InvitacionResponse reenviarInvitacion(Integer idContrato, Integer idParte) {
        UsuarioContrato parte = usuarioContratoRepository.findByIdParteAndContratoIdContrato(idParte, idContrato)
                .orElseThrow(() -> new RecursoNoEncontradoException("Parte no encontrada"));
        boolean enviado = notificacionService.reenviarUltimoCorreo(parte.getUsuario(), idContrato,
                TIPO_NOTIFICACION, parte.getCorreoInvitado(), ASUNTO_CORREO);
        return resultadoEnvio(parte, enviado);
    }

    String enlace(UsuarioContrato parte) {
        return frontendUrl + "/acceso/" + parte.getTokenInvitacion();
    }

    private InvitacionResponse resultadoEnvio(UsuarioContrato parte, boolean enviado) {
        if (enviado) {
            parte.setEstadoInvitacion(UsuarioContrato.INVITACION_ENVIADA);
            usuarioContratoRepository.save(parte);
        }
        InvitacionResponse response = new InvitacionResponse();
        response.setParte(toResponse(parte));
        response.setCorreoEnviado(enviado);
        response.setMensaje(enviado ? "Invitación enviada exitosamente a " + parte.getCorreoInvitado()
                : MENSAJE_ERROR_CORREO);
        return response;
    }

    private String armarCorreo(UsuarioContrato parte, String mensajePersonalizado) {
        StringBuilder correo = new StringBuilder();
        correo.append("Hola ").append(parte.getNombreParte()).append(",\n\n");
        correo.append("Recibiste una invitación para participar en el contrato \"")
                .append(parte.getContrato().getNombre()).append("\" con el rol ").append(parte.getRolParte())
                .append(".\n\n");
        if (mensajePersonalizado != null && !mensajePersonalizado.trim().isEmpty()) {
            correo.append(mensajePersonalizado.trim()).append("\n\n");
        }
        correo.append("Para acceder al contrato ingresá al siguiente enlace:\n").append(enlace(parte)).append("\n\n");
        correo.append("El enlace expira en ").append(MINUTOS_TOKEN_INVITACION).append(" minutos.");
        return correo.toString();
    }

    private ParteResponse toResponse(UsuarioContrato parte) {
        ParteResponse response = new ParteResponse();
        response.setIdParte(parte.getIdParte());
        response.setNombre(parte.getNombreParte());
        response.setEmail(parte.getCorreoInvitado());
        response.setRol(parte.getRolParte());
        response.setEstadoInvitacion(parte.getEstadoInvitacion());
        response.setEnlace(enlace(parte));
        return response;
    }
}
