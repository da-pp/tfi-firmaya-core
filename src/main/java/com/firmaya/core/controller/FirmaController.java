package com.firmaya.core.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.firmaya.core.config.SesionInterceptor;
import com.firmaya.core.dto.EstadoFirmasResponse;
import com.firmaya.core.dto.ReintentarFirmasRequest;
import com.firmaya.core.dto.SolicitarFirmasRequest;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.service.SolicitudFirmaService;

/**
 * CU-07 – Solicitar firma de las partes y CU-09 – Consultar estado de firmas pendientes.
 */
@RestController
@RequestMapping("/api/contratos/{idContrato}")
public class FirmaController {

    private final SolicitudFirmaService solicitudFirmaService;

    public FirmaController(SolicitudFirmaService solicitudFirmaService) {
        this.solicitudFirmaService = solicitudFirmaService;
    }

    // CU-07 pasos 2 a 6
    @GetMapping("/firmantes")
    public EstadoFirmasResponse listarFirmantes(@PathVariable Integer idContrato) {
        return solicitudFirmaService.listarFirmantes(idContrato);
    }

    // CU-07 pasos 11 a 19
    @PostMapping("/solicitudes-firma")
    public EstadoFirmasResponse solicitarFirmas(@PathVariable Integer idContrato,
            @Valid @RequestBody SolicitarFirmasRequest request,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario usuario, HttpServletRequest httpRequest) {
        return solicitudFirmaService.solicitarFirmas(idContrato, request, usuario, httpRequest.getRemoteAddr());
    }

    // CU-07 "Reintentar fallidos"
    @PostMapping("/solicitudes-firma/reintentar")
    public EstadoFirmasResponse reintentarFallidos(@PathVariable Integer idContrato,
            @Valid @RequestBody ReintentarFirmasRequest request) {
        return solicitudFirmaService.reintentarFallidos(idContrato, request.getIdsFirma());
    }

    // CU-09 pasos 2 a 10
    @GetMapping("/firmas")
    public EstadoFirmasResponse consultarEstado(@PathVariable Integer idContrato) {
        return solicitudFirmaService.consultarEstado(idContrato);
    }

    // CU-09 pasos 12 a 17
    @PostMapping("/firmas/{idFirma}/reenviar")
    public EstadoFirmasResponse reenviarSolicitud(@PathVariable Integer idContrato, @PathVariable Integer idFirma,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario usuario, HttpServletRequest httpRequest) {
        return solicitudFirmaService.reenviarSolicitud(idContrato, idFirma, usuario, httpRequest.getRemoteAddr());
    }
}
