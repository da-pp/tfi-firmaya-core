package com.firmaya.core.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.RolResponse;
import com.firmaya.core.dto.UsuarioGuardadoResponse;
import com.firmaya.core.dto.UsuarioRequest;
import com.firmaya.core.dto.UsuarioResponse;
import com.firmaya.core.entity.Rol;
import com.firmaya.core.entity.Sesion;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.exception.RecursoNoEncontradoException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.RolRepository;
import com.firmaya.core.repository.SesionRepository;
import com.firmaya.core.repository.UsuarioRepository;

/**
 * CU-15 – Gestionar usuarios y roles.
 */
@Service
public class UsuarioService {

    static final String MENSAJE_EMAIL_REGISTRADO = "Este Email ya está registrado en el sistema.";
    static final String MENSAJE_CREADO = "Usuario creado exitosamente. Se envió un correo de activación.";
    static final String MENSAJE_DESACTIVADO = "Usuario desactivado exitosamente.";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SesionRepository sesionRepository;
    private final RecuperacionService recuperacionService;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            SesionRepository sesionRepository, RecuperacionService recuperacionService,
            AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.sesionRepository = sesionRepository;
        this.recuperacionService = recuperacionService;
        this.auditoriaService = auditoriaService;
    }

    // Paso 2
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios() {
        List<UsuarioResponse> respuesta = new ArrayList<>();
        for (Usuario usuario : usuarioRepository.findAll(Sort.by("nombre", "apellido"))) {
            respuesta.add(toResponse(usuario));
        }
        return respuesta;
    }

    // Paso 8: opciones del desplegable de rol
    @Transactional(readOnly = true)
    public List<RolResponse> listarRoles() {
        List<RolResponse> respuesta = new ArrayList<>();
        for (Rol rol : rolRepository.findAll(Sort.by("nombre"))) {
            respuesta.add(new RolResponse(rol.getIdRol(), rol.getNombre()));
        }
        return respuesta;
    }

    /**
     * Pasos 13 y 16 a 21.
     */
    @Transactional
    public UsuarioGuardadoResponse crearUsuario(UsuarioRequest request, Usuario administrador, String direccionIp) {
        String email = request.getEmail().trim();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ReglaNegocioException("email", MENSAJE_EMAIL_REGISTRADO);
        }

        // Paso 18: la cuenta se crea sin contraseña; el usuario la define con el enlace de activación
        Usuario usuario = new Usuario();
        copiarDatos(request, usuario);
        usuario.setIntentosFallidos(0);
        usuarioRepository.save(usuario);

        // Paso 19 (el resultado del envío queda registrado en NOTIFICACION)
        recuperacionService.enviarActivacion(usuario);

        // Paso 21
        auditoriaService.registrarCambio(administrador, "Creación", "usuario", usuario.getIdUsuario(),
                "Creación del usuario " + usuario.getEmail(), direccionIp, null, describir(usuario));

        // Paso 20
        UsuarioGuardadoResponse response = new UsuarioGuardadoResponse();
        response.setUsuario(toResponse(usuario));
        response.setMensaje(MENSAJE_CREADO);
        return response;
    }

    /**
     * Edición de un usuario existente (mismo formulario que el alta).
     * Si pasa de Activo a Inactivo, cierra sus sesiones activas (camino alternativo, paso G).
     */
    @Transactional
    public UsuarioGuardadoResponse editarUsuario(Integer idUsuario, UsuarioRequest request, Usuario administrador,
            String direccionIp) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        String email = request.getEmail().trim();
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdUsuarioNot(email, idUsuario)) {
            throw new ReglaNegocioException("email", MENSAJE_EMAIL_REGISTRADO);
        }

        String datosAntes = describir(usuario);
        boolean seDesactiva = Usuario.ESTADO_ACTIVO.equals(usuario.getEstado())
                && Usuario.ESTADO_INACTIVO.equals(request.getEstado());

        copiarDatos(request, usuario);
        usuarioRepository.save(usuario);

        UsuarioGuardadoResponse response = new UsuarioGuardadoResponse();
        if (seDesactiva) {
            cerrarSesionesActivas(usuario);
            auditoriaService.registrarCambio(administrador, "Desactivación", "usuario", usuario.getIdUsuario(),
                    "Desactivación del usuario " + usuario.getEmail(), direccionIp, datosAntes, describir(usuario));
            response.setMensaje(MENSAJE_DESACTIVADO);
        } else {
            auditoriaService.registrarCambio(administrador, "Edición", "usuario", usuario.getIdUsuario(),
                    "Edición del usuario " + usuario.getEmail(), direccionIp, datosAntes, describir(usuario));
        }
        response.setUsuario(toResponse(usuario));
        return response;
    }

    private void copiarDatos(UsuarioRequest request, Usuario usuario) {
        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new ReglaNegocioException("idRol", "El rol seleccionado no existe"));
        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        usuario.setEmail(request.getEmail().trim());
        usuario.setRol(rol);
        usuario.setEstado(request.getEstado());
    }

    private void cerrarSesionesActivas(Usuario usuario) {
        List<Sesion> sesiones = sesionRepository.findByUsuarioIdUsuarioAndEstado(usuario.getIdUsuario(),
                Sesion.ESTADO_ACTIVA);
        for (Sesion sesion : sesiones) {
            sesion.setEstado(Sesion.ESTADO_CERRADA);
        }
        sesionRepository.saveAll(sesiones);
    }

    // Texto legible para los datos antes/después del registro de auditoría
    private String describir(Usuario usuario) {
        return "Nombre: " + usuario.getNombre()
                + " | Apellido: " + usuario.getApellido()
                + " | Email: " + usuario.getEmail()
                + " | Rol: " + (usuario.getRol() == null ? "" : usuario.getRol().getNombre())
                + " | Estado: " + usuario.getEstado();
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        UsuarioResponse response = new UsuarioResponse();
        response.setIdUsuario(usuario.getIdUsuario());
        response.setNombre(usuario.getNombre());
        response.setApellido(usuario.getApellido());
        response.setEmail(usuario.getEmail());
        if (usuario.getRol() != null) {
            response.setIdRol(usuario.getRol().getIdRol());
            response.setRol(usuario.getRol().getNombre());
        }
        response.setEstado(usuario.getEstado());
        return response;
    }
}
