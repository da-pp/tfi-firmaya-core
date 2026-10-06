package com.firmaya.core.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.UsuarioContrato;
import com.firmaya.core.exception.NoAutenticadoException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.UsuarioContratoRepository;

/**
 * Validación del token de invitación de una parte externa (sin cuenta).
 * La usan los comentarios externos del CU-06.
 */
@Service
public class AccesoExternoService {

    static final String MENSAJE_TOKEN_INVALIDO =
            "El enlace de acceso no es válido o ha expirado. Solicite un nuevo enlace al dueño del contrato.";
    static final String MENSAJE_NO_DISPONIBLE = "Este contrato ya no está disponible.";

    private final UsuarioContratoRepository usuarioContratoRepository;

    public AccesoExternoService(UsuarioContratoRepository usuarioContratoRepository) {
        this.usuarioContratoRepository = usuarioContratoRepository;
    }

    /**
     * Token existente y vigente, contrato no archivado.
     */
    public UsuarioContrato buscarParteConAcceso(String token) {
        UsuarioContrato parte = usuarioContratoRepository.findByTokenInvitacion(token).orElse(null);
        if (parte == null || parte.getFechaExpiracionToken() == null
                || LocalDateTime.now().isAfter(parte.getFechaExpiracionToken())) {
            throw new NoAutenticadoException(MENSAJE_TOKEN_INVALIDO);
        }
        if (EstadoContrato.ARCHIVADO.equals(parte.getContrato().getEstado().getNombre())) {
            throw new ReglaNegocioException(MENSAJE_NO_DISPONIBLE);
        }
        return parte;
    }
}
