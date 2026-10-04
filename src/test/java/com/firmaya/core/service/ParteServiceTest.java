package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.firmaya.core.dto.InvitacionResponse;
import com.firmaya.core.dto.InvitarParteRequest;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.UsuarioContrato;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.UsuarioContratoRepository;
import com.firmaya.core.repository.UsuarioRepository;

class ParteServiceTest {

    private UsuarioContratoRepository usuarioContratoRepository;
    private UsuarioRepository usuarioRepository;
    private ContratoService contratoService;
    private NotificacionService notificacionService;
    private AuditoriaService auditoriaService;
    private ParteService service;
    private Usuario usuario = new Usuario();
    private Contrato contrato;

    @BeforeEach
    void preparar() {
        usuarioContratoRepository = mock(UsuarioContratoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        contratoService = mock(ContratoService.class);
        notificacionService = mock(NotificacionService.class);
        auditoriaService = mock(AuditoriaService.class);
        service = new ParteService(usuarioContratoRepository, usuarioRepository, contratoService,
                notificacionService, auditoriaService, new GeneradorToken(), "http://localhost:3000");

        contrato = new Contrato();
        contrato.setIdContrato(10);
        contrato.setNombre("Locación Corrientes");
        EstadoContrato estado = new EstadoContrato();
        estado.setNombre(EstadoContrato.EN_REVISION);
        contrato.setEstado(estado);
        when(contratoService.buscarContrato(10)).thenReturn(contrato);
        when(usuarioRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void invitarRegistraParteConTokenDe15MinutosYEnviaCorreo() {
        when(notificacionService.enviarCorreo(isNull(), eq(10), eq(ParteService.TIPO_NOTIFICACION),
                eq("ana@mail.com"), anyString(), anyString())).thenReturn(true);

        InvitacionResponse response = service.invitarParte(10, request(), usuario, "10.0.0.1");

        ArgumentCaptor<UsuarioContrato> captor = ArgumentCaptor.forClass(UsuarioContrato.class);
        verify(usuarioContratoRepository, atLeastOnce()).save(captor.capture());
        UsuarioContrato parte = captor.getValue();
        assertEquals(UsuarioContrato.INVITACION_ENVIADA, parte.getEstadoInvitacion());
        assertEquals(parte.getFechaInvitacion().plusMinutes(15), parte.getFechaExpiracionToken());
        assertTrue(response.isCorreoEnviado());
        assertEquals("Invitación enviada exitosamente a ana@mail.com", response.getMensaje());
        assertEquals("http://localhost:3000/acceso/" + parte.getTokenInvitacion(), response.getParte().getEnlace());
        // El correo incluye el nombre del contrato, el mensaje personalizado y el enlace
        verify(notificacionService).enviarCorreo(isNull(), eq(10), anyString(), eq("ana@mail.com"), anyString(),
                contains("Locación Corrientes"));
        verify(notificacionService).enviarCorreo(isNull(), eq(10), anyString(), eq("ana@mail.com"), anyString(),
                contains("Por favor revisar"));
    }

    @Test
    void falloDelCorreoDejaLaParteEnPendienteYDevuelveEnlace() {
        when(notificacionService.enviarCorreo(any(), any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);

        InvitacionResponse response = service.invitarParte(10, request(), usuario, "10.0.0.1");

        assertFalse(response.isCorreoEnviado());
        assertEquals(ParteService.MENSAJE_ERROR_CORREO, response.getMensaje());
        assertEquals(UsuarioContrato.INVITACION_PENDIENTE, response.getParte().getEstadoInvitacion());
    }

    @Test
    void correoYaInvitadoEsRechazado() {
        when(usuarioContratoRepository.existsByContratoIdContratoAndCorreoInvitadoIgnoreCase(10, "ana@mail.com"))
                .thenReturn(true);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.invitarParte(10, request(), usuario, "10.0.0.1"));

        assertEquals("email", ex.getCampo());
        assertEquals(ParteService.MENSAJE_YA_INVITADO, ex.getMessage());
        verify(usuarioContratoRepository, never()).save(any(UsuarioContrato.class));
    }

    @Test
    void contratoArchivadoNoAdmiteInvitaciones() {
        contrato.getEstado().setNombre(EstadoContrato.ARCHIVADO);

        assertThrows(ReglaNegocioException.class, () -> service.invitarParte(10, request(), usuario, "10.0.0.1"));
    }

    @Test
    void correoDeUsuarioInternoVinculaLaParte() {
        Usuario interno = new Usuario();
        when(usuarioRepository.findByEmailIgnoreCase("ana@mail.com")).thenReturn(Optional.of(interno));

        InvitacionResponse response = service.invitarParte(10, request(), usuario, "10.0.0.1");

        ArgumentCaptor<UsuarioContrato> captor = ArgumentCaptor.forClass(UsuarioContrato.class);
        verify(usuarioContratoRepository).save(captor.capture());
        assertEquals(interno, captor.getValue().getUsuario());
        assertNull(response.getParte().getIdParte());
    }

    private InvitarParteRequest request() {
        InvitarParteRequest request = new InvitarParteRequest();
        request.setEmail("ana@mail.com");
        request.setNombre("Ana Martínez");
        request.setRol(UsuarioContrato.ROL_FIRMANTE);
        request.setMensaje("Por favor revisar la cláusula 3");
        return request;
    }
}
