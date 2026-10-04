package com.firmaya.core.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.firmaya.core.config.SesionInterceptor;
import com.firmaya.core.dto.CompararVersionesResponse;
import com.firmaya.core.dto.HistorialVersionesResponse;
import com.firmaya.core.dto.RestaurarVersionRequest;
import com.firmaya.core.dto.VerificarIntegridadRequest;
import com.firmaya.core.dto.VerificarIntegridadResponse;
import com.firmaya.core.dto.VersionGuardadaResponse;
import com.firmaya.core.dto.VersionResponse;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.service.VersionService;

@RestController
@RequestMapping("/api/contratos/{idContrato}")
public class VersionController {

    private final VersionService versionService;

    public VersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    // CU-11 – Ver historial de versiones
    @GetMapping("/versiones")
    public HistorialVersionesResponse obtenerHistorial(@PathVariable Integer idContrato) {
        return versionService.obtenerHistorial(idContrato);
    }

    // CU-12 – Comparar versiones de contrato
    @GetMapping("/versiones/comparar")
    public CompararVersionesResponse compararVersiones(@PathVariable Integer idContrato,
            @RequestParam Integer versionA, @RequestParam Integer versionB) {
        return versionService.compararVersiones(idContrato, versionA, versionB);
    }

    // CU-11 – Ver una versión en modo solo lectura
    @GetMapping("/versiones/{numeroVersion}")
    public VersionResponse obtenerVersion(@PathVariable Integer idContrato, @PathVariable Integer numeroVersion) {
        return versionService.obtenerVersion(idContrato, numeroVersion);
    }

    // CU-14 – Restaurar una versión anterior
    @PostMapping("/versiones/{numeroVersion}/restaurar")
    @ResponseStatus(HttpStatus.CREATED)
    public VersionGuardadaResponse restaurarVersion(@PathVariable Integer idContrato,
            @PathVariable Integer numeroVersion, @Valid @RequestBody RestaurarVersionRequest request,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario usuario, HttpServletRequest httpRequest) {
        return versionService.restaurarVersion(idContrato, numeroVersion, request.getRazon(), usuario,
                httpRequest.getRemoteAddr());
    }

    // CU-13 – Verificar integridad por hash
    @PostMapping("/verificar-integridad")
    public VerificarIntegridadResponse verificarIntegridad(@PathVariable Integer idContrato,
            @Valid @RequestBody VerificarIntegridadRequest request,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario usuario, HttpServletRequest httpRequest) {
        return versionService.verificarIntegridad(idContrato, request.getHash(), usuario,
                httpRequest.getRemoteAddr());
    }
}
