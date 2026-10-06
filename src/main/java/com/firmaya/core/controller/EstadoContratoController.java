package com.firmaya.core.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.firmaya.core.config.SesionInterceptor;
import com.firmaya.core.dto.CambiarEstadoRequest;
import com.firmaya.core.dto.CambioEstadoResponse;
import com.firmaya.core.dto.TransicionesResponse;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.service.EstadoContratoService;

@RestController
@RequestMapping("/api/contratos/{idContrato}")
public class EstadoContratoController {

    private final EstadoContratoService estadoContratoService;

    public EstadoContratoController(EstadoContratoService estadoContratoService) {
        this.estadoContratoService = estadoContratoService;
    }

    @GetMapping("/transiciones")
    public TransicionesResponse obtenerTransiciones(@PathVariable Integer idContrato) {
        return estadoContratoService.obtenerTransiciones(idContrato);
    }

    @PatchMapping("/estado")
    public CambioEstadoResponse cambiarEstado(@PathVariable Integer idContrato,
            @Valid @RequestBody CambiarEstadoRequest request,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario usuario, HttpServletRequest httpRequest) {
        return estadoContratoService.cambiarEstado(idContrato, request, usuario, httpRequest.getRemoteAddr());
    }
}
