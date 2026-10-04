package com.firmaya.core.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.RestablecerContraseniaRequest;
import com.firmaya.core.entity.TokenSeguridad;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.TokenSeguridadRepository;
import com.firmaya.core.repository.UsuarioRepository;

/**
 * CU-21 – Recuperar Contraseña.
 * También envía el enlace de activación del CU-15: el usuario nuevo define su contraseña
 * en la misma pantalla de restablecimiento.
 */
@Service
public class RecuperacionService {

    public static final String MENSAJE_SOLICITUD = "Si el correo electrónico ingresado corresponde a una cuenta "
            + "registrada, recibirás instrucciones para restablecer tu contraseña.";
    public static final String MENSAJE_EXITO = "Tu contraseña se restableció exitosamente.";
    static final String MENSAJE_TOKEN_INVALIDO = "El enlace de recuperación ha expirado o no es válido. Solicita uno nuevo.";
    static final String MENSAJE_NO_COINCIDEN = "Las contraseñas no coinciden.";

    private static final int MINUTOS_TOKEN_RECUPERACION = 30;
    private static final int MINUTOS_TOKEN_ACTIVACION = 15;
    private static final List<String> TIPOS_TOKEN_CONTRASENIA = Arrays.asList(
            TokenSeguridad.TIPO_RECUPERACION, TokenSeguridad.TIPO_ACTIVACION);

    private final UsuarioRepository usuarioRepository;
    private final TokenSeguridadRepository tokenSeguridadRepository;
    private final NotificacionService notificacionService;
    private final AuditoriaService auditoriaService;
    private final GeneradorToken generadorToken;
    private final PasswordEncoder passwordEncoder;
    private final String frontendUrl;

    public RecuperacionService(UsuarioRepository usuarioRepository,
            TokenSeguridadRepository tokenSeguridadRepository, NotificacionService notificacionService,
            AuditoriaService auditoriaService, GeneradorToken generadorToken, PasswordEncoder passwordEncoder,
            @Value("${firmaya.frontend-url}") String frontendUrl) {
        this.usuarioRepository = usuarioRepository;
        this.tokenSeguridadRepository = tokenSeguridadRepository;
        this.notificacionService = notificacionService;
        this.auditoriaService = auditoriaService;
        this.generadorToken = generadorToken;
        this.passwordEncoder = passwordEncoder;
        this.frontendUrl = frontendUrl;
    }

    /**
     * CU-21 pasos 8 a 10. El llamador responde siempre MENSAJE_SOLICITUD, exista o no la cuenta.
     */
    @Transactional
    public void solicitarRecuperacion(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (usuario == null) {
            return;
        }

        String token = crearToken(usuario, TokenSeguridad.TIPO_RECUPERACION, MINUTOS_TOKEN_RECUPERACION);
        String mensaje = "Hola " + usuario.getNombre() + ",\n\n"
                + "Para restablecer tu contraseña ingresá al siguiente enlace:\n" + enlace(token) + "\n\n"
                + "El enlace expira en " + MINUTOS_TOKEN_RECUPERACION + " minutos.";
        notificacionService.enviarCorreo(usuario, null, "Recuperación de contraseña", usuario.getEmail(),
                "FirmaYA - Recuperar contraseña", mensaje);
    }

    /**
     * CU-15 paso 19: correo con instrucciones para que el usuario nuevo establezca su contraseña.
     * Devuelve true si el correo se envió.
     */
    public boolean enviarActivacion(Usuario usuario) {
        String token = crearToken(usuario, TokenSeguridad.TIPO_ACTIVACION, MINUTOS_TOKEN_ACTIVACION);
        String mensaje = "Hola " + usuario.getNombre() + ",\n\n"
                + "Se creó tu cuenta en FirmaYA. Para establecer tu contraseña ingresá al siguiente enlace:\n"
                + enlace(token) + "\n\n"
                + "El enlace expira en " + MINUTOS_TOKEN_ACTIVACION + " minutos.";
        return notificacionService.enviarCorreo(usuario, null, "Activación de cuenta", usuario.getEmail(),
                "FirmaYA - Activación de cuenta", mensaje);
    }

    /**
     * CU-21 paso 12: el token debe existir, no estar usado y no haber expirado.
     */
    @Transactional(readOnly = true)
    public void validarToken(String valor) {
        buscarTokenVigente(valor);
    }

    /**
     * CU-21 pasos 14 a 23.
     */
    @Transactional
    public void restablecerContrasenia(RestablecerContraseniaRequest request, String direccionIp) {
        TokenSeguridad token = buscarTokenVigente(request.getToken());

        validarRequisitos(request.getNuevaContrasenia());
        if (!request.getNuevaContrasenia().equals(request.getConfirmarContrasenia())) {
            throw new ReglaNegocioException(MENSAJE_NO_COINCIDEN);
        }

        // Paso 21
        Usuario usuario = token.getUsuario();
        usuario.setContraseniaHash(passwordEncoder.encode(request.getNuevaContrasenia()));
        usuarioRepository.save(usuario);

        // Paso 22
        token.setUsado(true);
        tokenSeguridadRepository.save(token);

        // Paso 23
        auditoriaService.registrar(usuario, "Cambio de contraseña", "usuario", usuario.getIdUsuario(),
                "Restablecimiento de contraseña de " + usuario.getEmail(), direccionIp);
    }

    private String crearToken(Usuario usuario, String tipo, int minutosValidez) {
        LocalDateTime ahora = LocalDateTime.now();
        TokenSeguridad token = new TokenSeguridad();
        token.setUsuario(usuario);
        token.setTipoToken(tipo);
        token.setValor(generadorToken.generar());
        token.setUsado(false);
        token.setFechaCreacion(ahora);
        token.setFechaExpiracion(ahora.plusMinutes(minutosValidez));
        tokenSeguridadRepository.save(token);
        return token.getValor();
    }

    private String enlace(String token) {
        return frontendUrl + "/restablecer-contrasena/" + token;
    }

    private TokenSeguridad buscarTokenVigente(String valor) {
        TokenSeguridad token = tokenSeguridadRepository
                .findByValorAndTipoTokenIn(valor, TIPOS_TOKEN_CONTRASENIA).orElse(null);
        if (token == null || Boolean.TRUE.equals(token.getUsado())
                || LocalDateTime.now().isAfter(token.getFechaExpiracion())) {
            throw new ReglaNegocioException(MENSAJE_TOKEN_INVALIDO);
        }
        return token;
    }

    // CU-21 paso 14: mínimo 8 caracteres, al menos 1 mayúscula, 1 número y 1 carácter especial
    private void validarRequisitos(String contrasenia) {
        List<String> faltantes = new ArrayList<>();
        if (contrasenia.length() < 8) {
            faltantes.add("mínimo 8 caracteres");
        }
        if (!contrasenia.matches(".*[A-Z].*")) {
            faltantes.add("al menos 1 mayúscula");
        }
        if (!contrasenia.matches(".*[0-9].*")) {
            faltantes.add("al menos 1 número");
        }
        if (!contrasenia.matches(".*[^A-Za-z0-9].*")) {
            faltantes.add("al menos 1 carácter especial");
        }
        if (!faltantes.isEmpty()) {
            throw new ReglaNegocioException("La contraseña no cumple los requisitos: " + String.join(", ", faltantes) + ".");
        }
    }
}
