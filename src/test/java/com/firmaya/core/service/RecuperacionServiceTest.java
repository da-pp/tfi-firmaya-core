package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.firmaya.core.dto.RestablecerContraseniaRequest;
import com.firmaya.core.entity.TokenSeguridad;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.TokenSeguridadRepository;
import com.firmaya.core.repository.UsuarioRepository;

class RecuperacionServiceTest {

    private static final String EMAIL = "ana@mail.com";
    private static final String TOKEN = "token-de-prueba";

    private UsuarioRepository usuarioRepository;
    private TokenSeguridadRepository tokenSeguridadRepository;
    private NotificacionService notificacionService;
    private AuditoriaService auditoriaService;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private RecuperacionService recuperacionService;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        usuarioRepository = mock(UsuarioRepository.class);
        tokenSeguridadRepository = mock(TokenSeguridadRepository.class);
        notificacionService = mock(NotificacionService.class);
        auditoriaService = mock(AuditoriaService.class);
        recuperacionService = new RecuperacionService(usuarioRepository, tokenSeguridadRepository,
                notificacionService, auditoriaService, new GeneradorToken(), passwordEncoder,
                "http://localhost:3000");

        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNombre("Ana");
        usuario.setEmail(EMAIL);
        usuario.setEstado(Usuario.ESTADO_INACTIVO);
    }

    @Test
    void cuentaExistenteRecibeTokenDe30MinutosPorCorreo() {
        when(usuarioRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(usuario));

        recuperacionService.solicitarRecuperacion(EMAIL);

        ArgumentCaptor<TokenSeguridad> captor = ArgumentCaptor.forClass(TokenSeguridad.class);
        verify(tokenSeguridadRepository).save(captor.capture());
        TokenSeguridad token = captor.getValue();
        assertEquals(TokenSeguridad.TIPO_RECUPERACION, token.getTipoToken());
        assertFalse(token.getUsado());
        assertEquals(token.getFechaCreacion().plusMinutes(30), token.getFechaExpiracion());

        verify(notificacionService).enviarCorreo(eq(usuario), isNull(), anyString(), eq(EMAIL), anyString(),
                contains("http://localhost:3000/restablecer-contrasena/" + token.getValor()));
    }

    @Test
    void cuentaInexistenteNoGeneraTokenNiCorreo() {
        when(usuarioRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

        recuperacionService.solicitarRecuperacion(EMAIL);

        verify(tokenSeguridadRepository, never()).save(any(TokenSeguridad.class));
        verify(notificacionService, never()).enviarCorreo(any(), any(), anyString(), anyString(), anyString(),
                anyString());
    }

    @Test
    void tokenExpiradoOUsadoEsRechazado() {
        TokenSeguridad expirado = token(LocalDateTime.now().minusMinutes(1), false);
        when(tokenSeguridadRepository.findByValorAndTipoTokenIn(eq(TOKEN), any()))
                .thenReturn(Optional.of(expirado));
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> recuperacionService.validarToken(TOKEN));
        assertEquals(RecuperacionService.MENSAJE_TOKEN_INVALIDO, ex.getMessage());

        TokenSeguridad usado = token(LocalDateTime.now().plusMinutes(10), true);
        when(tokenSeguridadRepository.findByValorAndTipoTokenIn(eq(TOKEN), any()))
                .thenReturn(Optional.of(usado));
        assertThrows(ReglaNegocioException.class, () -> recuperacionService.validarToken(TOKEN));
    }

    @Test
    void contraseniaSinRequisitosEsRechazada() {
        prepararTokenVigente();

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> recuperacionService.restablecerContrasenia(request("clavesimple", "clavesimple"), "10.0.0.1"));

        assertTrue(ex.getMessage().contains("al menos 1 mayúscula"));
        assertTrue(ex.getMessage().contains("al menos 1 número"));
        assertTrue(ex.getMessage().contains("al menos 1 carácter especial"));
    }

    @Test
    void contraseniasDistintasSonRechazadas() {
        prepararTokenVigente();

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> recuperacionService.restablecerContrasenia(request("Clave123!", "Clave123?"), "10.0.0.1"));

        assertEquals(RecuperacionService.MENSAJE_NO_COINCIDEN, ex.getMessage());
    }

    @Test
    void restablecerActualizaHashInvalidaTokenYAudita() {
        TokenSeguridad token = prepararTokenVigente();

        recuperacionService.restablecerContrasenia(request("Clave123!", "Clave123!"), "10.0.0.1");

        assertTrue(passwordEncoder.matches("Clave123!", usuario.getContraseniaHash()));
        assertTrue(token.getUsado());
        verify(auditoriaService).registrar(eq(usuario), eq("Cambio de contraseña"), eq("usuario"), eq(1),
                anyString(), eq("10.0.0.1"));
    }

    private TokenSeguridad prepararTokenVigente() {
        TokenSeguridad token = token(LocalDateTime.now().plusMinutes(20), false);
        when(tokenSeguridadRepository.findByValorAndTipoTokenIn(eq(TOKEN), any()))
                .thenReturn(Optional.of(token));
        return token;
    }

    private TokenSeguridad token(LocalDateTime expiracion, boolean usado) {
        TokenSeguridad token = new TokenSeguridad();
        token.setUsuario(usuario);
        token.setValor(TOKEN);
        token.setTipoToken(TokenSeguridad.TIPO_RECUPERACION);
        token.setUsado(usado);
        token.setFechaExpiracion(expiracion);
        return token;
    }

    private RestablecerContraseniaRequest request(String nueva, String confirmacion) {
        RestablecerContraseniaRequest request = new RestablecerContraseniaRequest();
        request.setToken(TOKEN);
        request.setNuevaContrasenia(nueva);
        request.setConfirmarContrasenia(confirmacion);
        return request;
    }
}
