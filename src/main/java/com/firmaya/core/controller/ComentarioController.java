package com.firmaya.core.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.firmaya.core.config.SesionInterceptor;
import com.firmaya.core.dto.ComentarioRequest;
import com.firmaya.core.dto.ComentariosResponse;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.service.ComentarioService;

/**
 * CU-06 – Añadir comentarios y observaciones.
 * Usuarios internos con sesión y partes externas con su token de acceso.
 */
@RestController
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping("/api/contratos/{idContrato}/comentarios")
    public ComentariosResponse listarComentarios(@PathVariable Integer idContrato) {
        return comentarioService.listarComentarios(idContrato);
    }

    @PostMapping("/api/contratos/{idContrato}/comentarios")
    @ResponseStatus(HttpStatus.CREATED)
    public ComentariosResponse publicarComentario(@PathVariable Integer idContrato,
            @Valid @RequestBody ComentarioRequest request,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario usuario, HttpServletRequest httpRequest) {
        return comentarioService.publicarComentario(idContrato, request, usuario, httpRequest.getRemoteAddr());
    }

    @GetMapping("/api/externo/acceso/{token}/comentarios")
    public ComentariosResponse listarComentariosExterno(@PathVariable String token) {
        return comentarioService.listarComentariosExterno(token);
    }

    @PostMapping("/api/externo/acceso/{token}/comentarios")
    @ResponseStatus(HttpStatus.CREATED)
    public ComentariosResponse publicarComentarioExterno(@PathVariable String token,
            @Valid @RequestBody ComentarioRequest request, HttpServletRequest httpRequest) {
        return comentarioService.publicarComentarioExterno(token, request, httpRequest.getRemoteAddr());
    }
}
