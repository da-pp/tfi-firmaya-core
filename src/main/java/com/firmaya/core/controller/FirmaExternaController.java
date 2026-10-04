package com.firmaya.core.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.firmaya.core.dto.ConfirmarFirmaRequest;
import com.firmaya.core.dto.FirmaExternaResponse;
import com.firmaya.core.dto.FirmaRegistradaResponse;
import com.firmaya.core.dto.MensajeResponse;
import com.firmaya.core.service.FirmaOtpService;

/**
 * CU-08 – Firmar contrato vía OTP. Ruta pública: el acceso se valida con el enlace de firma.
 */
@RestController
@RequestMapping("/api/externo/firma/{token}")
public class FirmaExternaController {

    private final FirmaOtpService firmaOtpService;

    public FirmaExternaController(FirmaOtpService firmaOtpService) {
        this.firmaOtpService = firmaOtpService;
    }

    @GetMapping
    public FirmaExternaResponse obtenerContratoAFirmar(@PathVariable String token) {
        return firmaOtpService.obtenerContratoAFirmar(token);
    }

    // "Leer y firmar el contrato" y "Reenviar código"
    @PostMapping("/otp")
    public MensajeResponse enviarCodigo(@PathVariable String token) {
        return firmaOtpService.enviarCodigo(token);
    }

    @PostMapping("/confirmar")
    public FirmaRegistradaResponse confirmarFirma(@PathVariable String token,
            @Valid @RequestBody ConfirmarFirmaRequest request, HttpServletRequest httpRequest) {
        return firmaOtpService.confirmarFirma(token, request.getCodigo(), httpRequest.getRemoteAddr());
    }
}
