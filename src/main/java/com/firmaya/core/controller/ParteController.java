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
import com.firmaya.core.dto.InvitacionResponse;
import com.firmaya.core.dto.InvitarParteRequest;
import com.firmaya.core.dto.ParteResponse;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.service.ParteService;

/**
 * CU-03 – Invitar a las partes al contrato.
 */
@RestController
@RequestMapping("/api/contratos/{idContrato}/partes")
public class ParteController {

    private final ParteService parteService;

    public ParteController(ParteService parteService) {
        this.parteService = parteService;
    }

    @GetMapping
    public List<ParteResponse> listarPartes(@PathVariable Integer idContrato) {
        return parteService.listarPartes(idContrato);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvitacionResponse invitarParte(@PathVariable Integer idContrato,
            @Valid @RequestBody InvitarParteRequest request,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario usuario, HttpServletRequest httpRequest) {
        return parteService.invitarParte(idContrato, request, usuario, httpRequest.getRemoteAddr());
    }

    @PostMapping("/{idParte}/reenviar")
    public InvitacionResponse reenviarInvitacion(@PathVariable Integer idContrato, @PathVariable Integer idParte) {
        return parteService.reenviarInvitacion(idContrato, idParte);
    }
}
