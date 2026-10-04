package com.firmaya.core.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.firmaya.core.config.SesionInterceptor;
import com.firmaya.core.dto.RolResponse;
import com.firmaya.core.dto.UsuarioGuardadoResponse;
import com.firmaya.core.dto.UsuarioRequest;
import com.firmaya.core.dto.UsuarioResponse;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.service.UsuarioService;

/**
 * CU-15 – Gestionar usuarios y roles (BackOffice).
 */
@RestController
@RequestMapping("/api/admin")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/roles")
    public List<RolResponse> listarRoles() {
        return usuarioService.listarRoles();
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioGuardadoResponse crearUsuario(@Valid @RequestBody UsuarioRequest request,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario administrador,
            HttpServletRequest httpRequest) {
        return usuarioService.crearUsuario(request, administrador, httpRequest.getRemoteAddr());
    }

    @PutMapping("/usuarios/{idUsuario}")
    public UsuarioGuardadoResponse editarUsuario(@PathVariable Integer idUsuario,
            @Valid @RequestBody UsuarioRequest request,
            @RequestAttribute(SesionInterceptor.ATRIBUTO_USUARIO) Usuario administrador,
            HttpServletRequest httpRequest) {
        return usuarioService.editarUsuario(idUsuario, request, administrador, httpRequest.getRemoteAddr());
    }
}
