package com.firmaya.core.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.LoginRequest;
import com.firmaya.core.dto.LoginResponse;
import com.firmaya.core.entity.Sesion;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.exception.NoAutenticadoException;
import com.firmaya.core.repository.SesionRepository;
import com.firmaya.core.repository.UsuarioRepository;

@Service
public class AuthService {

    static final String MENSAJE_CREDENCIALES_INVALIDAS = "El correo electrónico o la contraseña son incorrectos.";
    static final String MENSAJE_CUENTA_BLOQUEADA = "Tu cuenta ha sido bloqueada temporalmente. "
            + "Puedes intentarlo de nuevo en 15 minutos o recuperar tu contraseña.";

    private static final int MAXIMO_INTENTOS_FALLIDOS = 5;
    private static final int MINUTOS_BLOQUEO = 15;
    private static final int MINUTOS_SESION = 15;

    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final AuditoriaService auditoriaService;
    private final PasswordEncoder passwordEncoder;
    private final GeneradorToken generadorToken;

    public AuthService(UsuarioRepository usuarioRepository, SesionRepository sesionRepository,
            AuditoriaService auditoriaService, PasswordEncoder passwordEncoder, GeneradorToken generadorToken) {
        this.usuarioRepository = usuarioRepository;
        this.sesionRepository = sesionRepository;
        this.auditoriaService = auditoriaService;
        this.passwordEncoder = passwordEncoder;
        this.generadorToken = generadorToken;
    }

    // noRollbackFor: el contador de intentos fallidos debe guardarse aunque se rechace el login
    @Transactional(noRollbackFor = NoAutenticadoException.class)
    public LoginResponse login(LoginRequest request, String direccionIp) {
        LocalDateTime ahora = LocalDateTime.now();

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.getEmail().trim()).orElse(null);
        if (usuario == null || !Usuario.ESTADO_ACTIVO.equals(usuario.getEstado())) {
            throw new NoAutenticadoException(MENSAJE_CREDENCIALES_INVALIDAS);
        }

        // Cuenta bloqueada por 15 minutos
        if (usuario.getBloqueadoHasta() != null) {
            if (ahora.isBefore(usuario.getBloqueadoHasta())) {
                throw new NoAutenticadoException(MENSAJE_CUENTA_BLOQUEADA);
            }
            // El bloqueo ya venció: se reinicia el contador
            usuario.setBloqueadoHasta(null);
            usuario.setIntentosFallidos(0);
        }

        if (!contraseniaCorrecta(request.getContrasenia(), usuario.getContraseniaHash())) {
            registrarIntentoFallido(usuario, ahora);
        }

        usuario.setIntentosFallidos(0);
        usuarioRepository.save(usuario);

        auditoriaService.registrar(usuario, "Inicio de sesión", "usuario", usuario.getIdUsuario(),
                "Inicio de sesión de " + usuario.getEmail(), direccionIp);

        Sesion sesion = crearSesion(usuario, ahora);

        return toResponse(usuario, sesion);
    }

    private boolean contraseniaCorrecta(String contrasenia, String contraseniaHash) {
        // Un usuario sin contraseña definida todavía no puede iniciar sesión
        if (contraseniaHash == null) {
            return false;
        }
        return passwordEncoder.matches(contrasenia, contraseniaHash);
    }

    private void registrarIntentoFallido(Usuario usuario, LocalDateTime ahora) {
        int intentos = usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos();
        intentos++;
        usuario.setIntentosFallidos(intentos);

        if (intentos >= MAXIMO_INTENTOS_FALLIDOS) {
            usuario.setBloqueadoHasta(ahora.plusMinutes(MINUTOS_BLOQUEO));
            usuarioRepository.save(usuario);
            throw new NoAutenticadoException(MENSAJE_CUENTA_BLOQUEADA);
        }

        usuarioRepository.save(usuario);
        throw new NoAutenticadoException(MENSAJE_CREDENCIALES_INVALIDAS);
    }

    private Sesion crearSesion(Usuario usuario, LocalDateTime ahora) {
        Sesion sesion = new Sesion();
        sesion.setUsuario(usuario);
        sesion.setToken(generadorToken.generar());
        sesion.setEstado(Sesion.ESTADO_ACTIVA);
        sesion.setFechaInicio(ahora);
        sesion.setFechaExpiracion(ahora.plusMinutes(MINUTOS_SESION));
        return sesionRepository.save(sesion);
    }

    private LoginResponse toResponse(Usuario usuario, Sesion sesion) {
        LoginResponse response = new LoginResponse();
        response.setToken(sesion.getToken());
        response.setFechaExpiracion(sesion.getFechaExpiracion());
        response.setIdUsuario(usuario.getIdUsuario());
        response.setNombre(usuario.getNombre());
        response.setApellido(usuario.getApellido());
        response.setRol(usuario.getRol() == null ? null : usuario.getRol().getNombre());
        response.setMensaje("Bienvenido, " + usuario.getNombre() + ".");
        return response;
    }
}
