package com.firmaya.core.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.firmaya.core.dto.AccesoExternoResponse;
import com.firmaya.core.service.AccesoExternoService;

/**
 * CU-04 – Ver contrato como parte invitada. Ruta pública: el acceso se valida con el token.
 */
@RestController
@RequestMapping("/api/externo/acceso/{token}")
public class AccesoExternoController {

    private final AccesoExternoService accesoExternoService;

    public AccesoExternoController(AccesoExternoService accesoExternoService) {
        this.accesoExternoService = accesoExternoService;
    }

    @GetMapping
    public AccesoExternoResponse verContrato(@PathVariable String token, HttpServletRequest httpRequest) {
        return accesoExternoService.verContrato(token, httpRequest.getRemoteAddr());
    }

    @PostMapping("/renovar")
    public AccesoExternoResponse renovarToken(@PathVariable String token) {
        return accesoExternoService.renovarToken(token);
    }
}
