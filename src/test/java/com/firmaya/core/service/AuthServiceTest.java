package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.firmaya.core.dto.LoginRequest;
import com.firmaya.core.dto.LoginResponse;
import com.firmaya.core.entity.Rol;
import com.firmaya.core.entity.Sesion;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.exception.NoAutenticadoException;
import com.firmaya.core.repository.SesionRepository;
import com.firmaya.core.repository.UsuarioRepository;

class AuthServiceTest {

    private static final String EMAIL = "ana@mail.com";
    private static final String CONTRASENIA = "Clave123!";

    private UsuarioRepository usuarioRepository;
    private SesionRepository sesionRepository;
    private AuditoriaService auditoriaService;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthService authService;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        usuarioRepository = mock(UsuarioRepository.class);
        sesionRepository = mock(SesionRepository.class);
        auditoriaService = mock(AuditoriaService.class);
        authService = new AuthService(usuarioRepository, sesionRepository, auditoriaService, passwordEncoder,
                new GeneradorToken());

        Rol rol = new Rol();
        rol.setNombre("Abogado");

        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNombre("Ana");
        usuario.setEmail(EMAIL);
        usuario.setEstado(Usuario.ESTADO_ACTIVO);
        usuario.setIntentosFallidos(0);
        usuario.setContraseniaHash(passwordEncoder.encode(CONTRASENIA));
        usuario.setRol(rol);

        when(usuarioRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(usuario));
        when(sesionRepository.save(any(Sesion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void loginCorrectoCreaSesionDe15MinutosYAudita() {
        usuario.setIntentosFallidos(3);

        LoginResponse response = authService.login(request(CONTRASENIA), "10.0.0.1");

        assertNotNull(response.getToken());
        assertEquals("Bienvenido, Ana.", response.getMensaje());
        assertEquals("Abogado", response.getRol());
        assertEquals(0, usuario.getIntentosFallidos());
        assertTrue(response.getFechaExpiracion().isAfter(LocalDateTime.now().plusMinutes(14)));
        assertTrue(response.getFechaExpiracion().isBefore(LocalDateTime.now().plusMinutes(16)));
        verify(auditoriaService).registrar(eq(usuario), eq("Inicio de sesión"), eq("usuario"), eq(1),
                anyString(), eq("10.0.0.1"));
    }

    @Test
    void contraseniaIncorrectaIncrementaIntentosConMensajeGenerico() {
        NoAutenticadoException ex = assertThrows(NoAutenticadoException.class,
                () -> authService.login(request("OtraClave1!"), "10.0.0.1"));

        assertEquals(AuthService.MENSAJE_CREDENCIALES_INVALIDAS, ex.getMessage());
        assertEquals(1, usuario.getIntentosFallidos());
        assertNull(usuario.getBloqueadoHasta());
        verify(sesionRepository, never()).save(any(Sesion.class));
    }

    @Test
    void quintoIntentoFallidoBloquea15Minutos() {
        usuario.setIntentosFallidos(4);

        NoAutenticadoException ex = assertThrows(NoAutenticadoException.class,
                () -> authService.login(request("OtraClave1!"), "10.0.0.1"));

        assertEquals(AuthService.MENSAJE_CUENTA_BLOQUEADA, ex.getMessage());
        assertTrue(usuario.getBloqueadoHasta().isAfter(LocalDateTime.now().plusMinutes(14)));
    }

    @Test
    void cuentaBloqueadaRechazaAunqueLaContraseniaSeaCorrecta() {
        usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(5));

        NoAutenticadoException ex = assertThrows(NoAutenticadoException.class,
                () -> authService.login(request(CONTRASENIA), "10.0.0.1"));

        assertEquals(AuthService.MENSAJE_CUENTA_BLOQUEADA, ex.getMessage());
    }

    @Test
    void bloqueoVencidoPermiteIngresar() {
        usuario.setIntentosFallidos(5);
        usuario.setBloqueadoHasta(LocalDateTime.now().minusMinutes(1));

        LoginResponse response = authService.login(request(CONTRASENIA), "10.0.0.1");

        assertNotNull(response.getToken());
        assertNull(usuario.getBloqueadoHasta());
        assertEquals(0, usuario.getIntentosFallidos());
    }

    @Test
    void cuentaInexistenteOInactivaDevuelveMensajeGenerico() {
        when(usuarioRepository.findByEmailIgnoreCase("nadie@mail.com")).thenReturn(Optional.empty());
        LoginRequest inexistente = request(CONTRASENIA);
        inexistente.setEmail("nadie@mail.com");

        NoAutenticadoException ex1 = assertThrows(NoAutenticadoException.class,
                () -> authService.login(inexistente, "10.0.0.1"));
        assertEquals(AuthService.MENSAJE_CREDENCIALES_INVALIDAS, ex1.getMessage());

        usuario.setEstado(Usuario.ESTADO_INACTIVO);
        NoAutenticadoException ex2 = assertThrows(NoAutenticadoException.class,
                () -> authService.login(request(CONTRASENIA), "10.0.0.1"));
        assertEquals(AuthService.MENSAJE_CREDENCIALES_INVALIDAS, ex2.getMessage());
    }

    private LoginRequest request(String contrasenia) {
        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setContrasenia(contrasenia);
        return request;
    }
}
