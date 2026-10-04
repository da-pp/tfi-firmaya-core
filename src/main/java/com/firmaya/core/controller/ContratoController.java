package com.firmaya.core.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.firmaya.core.config.SesionInterceptor;
import com.firmaya.core.dto.ContratoDetalleResponse;
import com.firmaya.core.dto.ContratoResumenResponse;
import com.firmaya.core.dto.CrearContratoRequest;
import com.firmaya.core.dto.GuardarVersionRequest;
import com.firmaya.core.dto.VersionGuardadaResponse;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.service.ContratoService;

@RestController
@RequestMapping("/api/contratos")
public class ContratoController {

    private final ContratoService contratoService;

    public ContratoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    // CU-01 – Crear contrato desde plantilla
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VersionGuardadaResponse crearContrato(@Valid @RequestBody CrearContratoRequest request,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario usuario, HttpServletRequest httpRequest) {
        return contratoService.crearContrato(request, usuario, httpRequest.getRemoteAddr());
    }

    // CU-01 postcondición / CU-02 paso 1: lista de contratos del usuario
    @GetMapping
    public List<ContratoResumenResponse> listarContratos(
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario usuario) {
        return contratoService.listarContratosDelUsuario(usuario);
    }

    // CU-02 pasos 2 a 8: contrato con su versión actual
    @GetMapping("/{idContrato}")
    public ContratoDetalleResponse obtenerContrato(@PathVariable Integer idContrato) {
        return contratoService.obtenerContrato(idContrato);
    }

    // CU-02 – Editar contrato en línea: guardar nueva versión
    @PostMapping("/{idContrato}/versiones")
    @ResponseStatus(HttpStatus.CREATED)
    public VersionGuardadaResponse guardarVersion(@PathVariable Integer idContrato,
            @Valid @RequestBody GuardarVersionRequest request,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario usuario, HttpServletRequest httpRequest) {
        return contratoService.guardarVersion(idContrato, request, usuario, httpRequest.getRemoteAddr());
    }
}
