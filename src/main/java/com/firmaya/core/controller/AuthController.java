package com.firmaya.core.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.firmaya.core.dto.LoginRequest;
import com.firmaya.core.dto.LoginResponse;
import com.firmaya.core.dto.MensajeResponse;
import com.firmaya.core.dto.RecuperarContraseniaRequest;
import com.firmaya.core.dto.RestablecerContraseniaRequest;
import com.firmaya.core.service.AuthService;
import com.firmaya.core.service.RecuperacionService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RecuperacionService recuperacionService;

    public AuthController(AuthService authService, RecuperacionService recuperacionService) {
        this.authService = authService;
        this.recuperacionService = recuperacionService;
    }

    // CU-19 – Iniciar Sesión
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, httpRequest.getRemoteAddr());
    }

    // CU-21 – Recuperar Contraseña: solicitud del enlace (siempre el mismo mensaje)
    @PostMapping("/recuperar")
    public MensajeResponse recuperar(@Valid @RequestBody RecuperarContraseniaRequest request) {
        recuperacionService.solicitarRecuperacion(request.getEmail());
        return new MensajeResponse(RecuperacionService.MENSAJE_SOLICITUD);
    }

    // CU-21 – Recuperar Contraseña: validación del enlace (paso 12)
    @GetMapping("/recuperar/{token}")
    public MensajeResponse validarToken(@PathVariable String token) {
        recuperacionService.validarToken(token);
        return new MensajeResponse("Enlace válido");
    }

    // CU-21 – Recuperar Contraseña: nueva contraseña
    @PostMapping("/restablecer")
    public MensajeResponse restablecer(@Valid @RequestBody RestablecerContraseniaRequest request,
            HttpServletRequest httpRequest) {
        recuperacionService.restablecerContrasenia(request, httpRequest.getRemoteAddr());
        return new MensajeResponse(RecuperacionService.MENSAJE_EXITO);
    }
}
