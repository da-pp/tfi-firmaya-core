package com.firmaya.core.config;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.firmaya.core.entity.Sesion;
import com.firmaya.core.exception.NoAutenticadoException;
import com.firmaya.core.repository.SesionRepository;

/**
 * Valida el encabezado "Authorization: Bearer {token}" contra la tabla SESION (CU-19).
 * Solo autentica: no controla roles ni permisos.
 * El usuario de la sesión queda disponible en el atributo ATRIBUTO_USUARIO del request.
 */
@Component
public class SesionInterceptor implements HandlerInterceptor {

    public static final String ATRIBUTO_USUARIO = "usuarioSesion";

    private static final String PREFIJO_BEARER = "Bearer ";

    private final SesionRepository sesionRepository;

    public SesionInterceptor(SesionRepository sesionRepository) {
        this.sesionRepository = sesionRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String encabezado = request.getHeader("Authorization");
        if (encabezado == null || !encabezado.startsWith(PREFIJO_BEARER)) {
            throw new NoAutenticadoException("Sesión no válida o expirada");
        }

        String token = encabezado.substring(PREFIJO_BEARER.length()).trim();
        Sesion sesion = sesionRepository.findByTokenAndEstado(token, Sesion.ESTADO_ACTIVA).orElse(null);
        if (sesion == null || LocalDateTime.now().isAfter(sesion.getFechaExpiracion())) {
            throw new NoAutenticadoException("Sesión no válida o expirada");
        }

        request.setAttribute(ATRIBUTO_USUARIO, sesion.getUsuario());
        return true;
    }
}
