package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.firmaya.core.dto.UsuarioGuardadoResponse;
import com.firmaya.core.dto.UsuarioRequest;
import com.firmaya.core.entity.Rol;
import com.firmaya.core.entity.Sesion;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.RolRepository;
import com.firmaya.core.repository.SesionRepository;
import com.firmaya.core.repository.UsuarioRepository;

class UsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private RolRepository rolRepository;
    private SesionRepository sesionRepository;
    private RecuperacionService recuperacionService;
    private AuditoriaService auditoriaService;
    private UsuarioService usuarioService;
    private Usuario administrador = new Usuario();

    @BeforeEach
    void preparar() {
        usuarioRepository = mock(UsuarioRepository.class);
        rolRepository = mock(RolRepository.class);
        sesionRepository = mock(SesionRepository.class);
        recuperacionService = mock(RecuperacionService.class);
        auditoriaService = mock(AuditoriaService.class);
        usuarioService = new UsuarioService(usuarioRepository, rolRepository, sesionRepository,
                recuperacionService, auditoriaService);

        Rol abogado = new Rol();
        abogado.setIdRol(2);
        abogado.setNombre("Abogado");
        when(rolRepository.findById(2)).thenReturn(Optional.of(abogado));
    }

    @Test
    void crearUsuarioSinContraseniaEnviaActivacionYAudita() {
        when(recuperacionService.enviarActivacion(any(Usuario.class))).thenReturn(true);

        UsuarioGuardadoResponse response = usuarioService.crearUsuario(request("Activo"), administrador, "10.0.0.1");

        assertEquals("Usuario creado exitosamente. Se envió un correo de activación.", response.getMensaje());
        assertEquals("Abogado", response.getUsuario().getRol());
        verify(recuperacionService).enviarActivacion(any(Usuario.class));
        verify(auditoriaService).registrarCambio(eq(administrador), eq("Creación"), eq("usuario"), any(),
                anyString(), eq("10.0.0.1"), isNull(), anyString());
    }

    @Test
    void emailRepetidoSeRechazaAsociadoAlCampo() {
        when(usuarioRepository.existsByEmailIgnoreCase("maria@mail.com")).thenReturn(true);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> usuarioService.crearUsuario(request("Activo"), administrador, "10.0.0.1"));

        assertEquals("email", ex.getCampo());
        assertEquals(UsuarioService.MENSAJE_EMAIL_REGISTRADO, ex.getMessage());
        verify(recuperacionService, never()).enviarActivacion(any(Usuario.class));
    }

    @Test
    void desactivarCierraSesionesActivas() {
        Usuario existente = usuarioExistente("Activo");
        Sesion sesion = new Sesion();
        sesion.setEstado(Sesion.ESTADO_ACTIVA);
        List<Sesion> sesiones = new ArrayList<>(Arrays.asList(sesion));
        when(sesionRepository.findByUsuarioIdUsuarioAndEstado(5, Sesion.ESTADO_ACTIVA)).thenReturn(sesiones);

        UsuarioGuardadoResponse response = usuarioService.editarUsuario(5, request("Inactivo"), administrador,
                "10.0.0.1");

        assertEquals(UsuarioService.MENSAJE_DESACTIVADO, response.getMensaje());
        assertEquals(Usuario.ESTADO_INACTIVO, existente.getEstado());
        assertEquals(Sesion.ESTADO_CERRADA, sesion.getEstado());
        verify(auditoriaService).registrarCambio(eq(administrador), eq("Desactivación"), eq("usuario"), eq(5),
                anyString(), eq("10.0.0.1"), anyString(), anyString());
    }

    @Test
    void edicionSinDesactivarNoCierraSesiones() {
        usuarioExistente("Activo");

        UsuarioGuardadoResponse response = usuarioService.editarUsuario(5, request("Activo"), administrador,
                "10.0.0.1");

        assertNull(response.getMensaje());
        verify(sesionRepository, never()).findByUsuarioIdUsuarioAndEstado(any(), anyString());
        verify(auditoriaService).registrarCambio(eq(administrador), eq("Edición"), eq("usuario"), eq(5),
                anyString(), eq("10.0.0.1"), anyString(), anyString());
    }

    @Test
    void formularioValidaLetrasYEspaciosYEmail() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        UsuarioRequest valido = request("Activo");
        valido.setNombre("María José");
        assertTrue(validator.validate(valido).isEmpty());

        UsuarioRequest invalido = request("Suspendido");
        invalido.setNombre("Juan2");
        invalido.setEmail("juan@dominio");
        Set<ConstraintViolation<UsuarioRequest>> errores = validator.validate(invalido);
        assertFalse(errores.isEmpty());
        assertEquals(3, errores.size());
    }

    private Usuario usuarioExistente(String estado) {
        Usuario existente = new Usuario();
        existente.setIdUsuario(5);
        existente.setNombre("María");
        existente.setApellido("Gómez");
        existente.setEmail("maria@mail.com");
        existente.setEstado(estado);
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(existente));
        return existente;
    }

    private UsuarioRequest request(String estado) {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("María");
        request.setApellido("Gómez");
        request.setEmail("maria@mail.com");
        request.setIdRol(2);
        request.setEstado(estado);
        return request;
    }
}
