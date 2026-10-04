package com.firmaya.core.controller;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.firmaya.core.config.SesionInterceptor;
import com.firmaya.core.dto.PreferenciasRequest;
import com.firmaya.core.dto.PreferenciasResponse;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.service.PreferenciaService;

/**
 * CU-20 – Configurar notificaciones (Mi Perfil > Notificaciones).
 */
@RestController
@RequestMapping("/api/perfil/notificaciones")
public class PreferenciaController {

    private final PreferenciaService preferenciaService;

    public PreferenciaController(PreferenciaService preferenciaService) {
        this.preferenciaService = preferenciaService;
    }

    @GetMapping
    public PreferenciasResponse obtenerPreferencias(
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario usuario) {
        return preferenciaService.obtenerPreferencias(usuario);
    }

    @PutMapping
    public PreferenciasResponse guardarPreferencias(@Valid @RequestBody PreferenciasRequest request,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario usuario) {
        return preferenciaService.guardarPreferencias(usuario, request);
    }
}
