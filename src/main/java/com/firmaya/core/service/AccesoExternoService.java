package com.firmaya.core.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.AccesoExternoResponse;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.UsuarioContrato;
import com.firmaya.core.entity.VersionContrato;
import com.firmaya.core.exception.NoAutenticadoException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.UsuarioContratoRepository;

/**
 * CU-04 – Ver contrato como parte invitada (acceso con el token de invitación, sin cuenta).
 */
@Service
public class AccesoExternoService {

    static final String MENSAJE_TOKEN_INVALIDO =
            "El enlace de acceso no es válido o ha expirado. Solicite un nuevo enlace al dueño del contrato.";
    static final String MENSAJE_NO_DISPONIBLE = "Este contrato ya no está disponible.";

    private final UsuarioContratoRepository usuarioContratoRepository;
    private final ContratoService contratoService;
    private final AuditoriaService auditoriaService;

    public AccesoExternoService(UsuarioContratoRepository usuarioContratoRepository,
            ContratoService contratoService, AuditoriaService auditoriaService) {
        this.usuarioContratoRepository = usuarioContratoRepository;
        this.contratoService = contratoService;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Pasos 2 a 16.
     */
    @Transactional
    public AccesoExternoResponse verContrato(String token, String direccionIp) {
        UsuarioContrato parte = buscarParteConAcceso(token);
        Contrato contrato = parte.getContrato();
        VersionContrato actual = contratoService.versionActual(contrato.getIdContrato());

        // Paso 4: registro del acceso con parte, fecha, hora e IP
        auditoriaService.registrarEnContrato(parte.getUsuario(), "Acceso externo", contrato.getIdContrato(),
                actual.getIdVersion(), "Acceso de " + parte.getNombreParte() + " (" + parte.getCorreoInvitado()
                        + ") como " + parte.getRolParte(), direccionIp, null, null);

        AccesoExternoResponse response = new AccesoExternoResponse();
        response.setIdContrato(contrato.getIdContrato());
        response.setNombreContrato(contrato.getNombre());
        response.setEstado(contrato.getEstado().getNombre());
        response.setNumeroVersion(actual.getNumeroVersion());
        response.setFechaUltimaModificacion(actual.getFechaCreacion());
        response.setHash(actual.getHashSha256());
        response.setContenido(actual.getContenido());
        response.setNombreParte(parte.getNombreParte());
        response.setRolParte(parte.getRolParte());
        response.setPuedeFirmar(UsuarioContrato.ROL_FIRMANTE.equals(parte.getRolParte())
                && EstadoContrato.LISTO_PARA_FIRMAR.equals(contrato.getEstado().getNombre()));
        response.setFechaExpiracionToken(parte.getFechaExpiracionToken());
        return response;
    }

    /**
     * Camino alternativo "Sesión expirada por inactividad", botón "Continuar": renueva el token.
     */
    @Transactional
    public AccesoExternoResponse renovarToken(String token) {
        UsuarioContrato parte = buscarParteConAcceso(token);
        parte.setFechaExpiracionToken(LocalDateTime.now().plusMinutes(ParteService.MINUTOS_TOKEN_INVITACION));
        usuarioContratoRepository.save(parte);

        AccesoExternoResponse response = new AccesoExternoResponse();
        response.setIdContrato(parte.getContrato().getIdContrato());
        response.setFechaExpiracionToken(parte.getFechaExpiracionToken());
        return response;
    }

    /**
     * Pasos 2 y 3: token existente y vigente, contrato no archivado.
     * También lo usan las demás operaciones de la parte externa.
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
